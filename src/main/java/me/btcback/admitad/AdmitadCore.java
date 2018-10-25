package me.btcback.admitad;

import me.btcback.Setts;
import me.btcback.admitad.models.DeepLink;
import me.btcback.admitad.models.Tarif;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.btcback.pgdb.DBPg;
import org.apache.commons.collections4.ListUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdmitadCore {

    private static final AdmitadCore INSTANCE = new AdmitadCore();

    public volatile String accessToken;
    public volatile String myPlaceID;
    private volatile Auth authntification;
    private volatile String myPlaceName = Setts.getInstance().getStr("admitad_playce_name");
    public volatile ArrayList<Advertiser> activeAdvertisersNew = new ArrayList<>();

    public AdmitadCore() {

        this.authntification = new Auth();
        this.accessToken = authntification.getAdmitadAuthToken();
        this.myPlaceID = getActiveID();//;getActivePalceID();
        this.activeAdvertisersNew = getActiveAdvertisersNew();
        System.out.println("Admitad inited");
    }

    private static List<Advertiser> sortAvertizers(ArrayList<Advertiser> advertisers) {
        String[] sortedIds = Setts.getInstance().getStr("offers_sort").split(",");
        ArrayList<Advertiser> tempAdvertizer = new ArrayList<>();
        for (String sortedId : sortedIds) {
            for (Iterator<Advertiser> iterator = advertisers.iterator(); iterator.hasNext(); ) {
                Advertiser advertizer = iterator.next();
                if (advertizer.getIdAdmitad() == Long.parseLong(sortedId)) {
                    iterator.remove();
                    tempAdvertizer.add(advertizer);
                }
            }
        }
        List<Advertiser> result = Stream.concat(tempAdvertizer.stream(), advertisers.stream())
                .collect(Collectors.toList());
        return result;
    }

    public synchronized String getActiveID() {
        String url = "https://api.admitad.com/websites/?status=active";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        // add request header
        request.addHeader("Authorization", "Bearer " + this.accessToken);
        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader rd = null;
        try {
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuffer result = new StringBuffer();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonParser parser = new JsonParser();
        JsonObject responseObject = parser.parse(String.valueOf(result).trim()).getAsJsonObject();
        JsonArray temp = responseObject.getAsJsonArray("results");
        for (JsonElement jsonElement : temp) {
            JsonObject place = jsonElement.getAsJsonObject();
            if (place.get("name").getAsString().equals(myPlaceName)) {
                return place.get("id").getAsString();
            }
        }
        return null;
    }

    public synchronized ArrayList<Advertiser> getActiveAdvertisersNew() {
        int limit = 400;
        int offset = 0;
        ArrayList<Advertiser> advertisers = new ArrayList<>();
        while (true) {
            String url = "https://api.admitad.com/advcampaigns/website/" + this.myPlaceID + "/?limit=" + limit + "&offset=" + offset + "&connection_status=active&has_tool=deeplink";
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            // add request header
            request.addHeader("Authorization", "Bearer " + this.accessToken);
            request.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            HttpResponse response = null;
            try {
                response = client.execute(request);
                BufferedReader rd = null;
                rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
                JsonParser jsonParser = new JsonParser();
                JsonArray advertizers = jsonParser.parse(result.toString()).getAsJsonObject().getAsJsonArray("results");
                JsonObject advertizersMeta = jsonParser.parse(result.toString()).getAsJsonObject().get("_meta").getAsJsonObject();
                if (advertizersMeta.get("count").getAsInt() <= (advertizersMeta.get("offset").getAsInt() - limit)) {
                    break;
                } else {
                    offset += limit;
                }
                Advertiser advertizerObj;
                for (JsonElement advertizer : advertizers) {
                    StringBuffer admitadTarifsDescription = new StringBuffer();
                    //получаем список ставок
                    JsonArray thisAdvertizerActions = advertizer.getAsJsonObject().getAsJsonArray("actions");
                    for (JsonElement thisAdvertizerAction : thisAdvertizerActions) {
                        admitadTarifsDescription.append("- " + thisAdvertizerAction.getAsJsonObject().get("name").getAsString() + " " + thisAdvertizerAction.getAsJsonObject().get("payment_size").getAsString() + "\n\r");
                    }
                    //получаем объект рекламодателя
                    JsonObject thisAdvInAdmitad = advertizer.getAsJsonObject();
                    //чекаем такого рекламодателя в БД
                    Boolean ifAdvExist = DBPg.getInstance().checkAdvertizerInDb(thisAdvInAdmitad.get("id").getAsLong());
                    if(ifAdvExist != null){ //если мы получили корректный ответ от базы
                        if(ifAdvExist){ // если такой рекламодатель уже есть в БД
                            DBPg.getInstance().updateAdvertizerInfo(thisAdvInAdmitad.get("id").getAsLong(), thisAdvInAdmitad.get("name").getAsString(), admitadTarifsDescription.toString(), thisAdvInAdmitad.get("site_url").getAsString(), thisAdvInAdmitad.get("gotolink").getAsString());
                            Advertiser thisAdvInDb = DBPg.getInstance().getAdvertizerWhereAdmitadId(thisAdvInAdmitad.get("id").getAsLong());
                            advertizerObj = thisAdvInDb;
                        }

                        else{ //если такого рекламодателя в БД нет
                            DBPg.getInstance().putAdvertizerToDB(thisAdvInAdmitad.get("id").getAsLong(), thisAdvInAdmitad.get("name").getAsString(), admitadTarifsDescription.toString(), thisAdvInAdmitad.get("site_url").getAsString(), thisAdvInAdmitad.get("gotolink").getAsString());
                            advertizerObj = new Advertiser(
                                    thisAdvInAdmitad.get("id").getAsLong(),
                                    thisAdvInAdmitad.get("name").getAsString(),
                                    admitadTarifsDescription.toString(),
                                    thisAdvInAdmitad.get("site_url").getAsString(),
                                    thisAdvInAdmitad.get("gotolink").getAsString()
                            );
                        }
                        advertisers.add(advertizerObj);
                    } else {
                        return null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        advertisers = (ArrayList<Advertiser>) sortAvertizers(advertisers);
        return advertisers;
    }

   /* public synchronized ArrayList<String> getClickedAdvertizer(long chatId, String subid2) {
        ArrayList<Advertiser> adversInfo = new ArrayList<Advertiser>();
        String url = "https://api.admitad.com/statistics/sub_ids/?order_by=-clicks&limit=1&subid1=" + chatId + "&subid2=" + subid2;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        // add request header
        request.addHeader("Authorization", "Bearer " + this.accessToken);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        HttpResponse response = null;
        try {
            response = client.execute(request);
            BufferedReader rd = null;
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JsonParser jsonParser = new JsonParser();
            JsonObject advertizer = jsonParser.parse(result.toString()).getAsJsonObject().getAsJsonArray("results").getAsJsonArray().get(0).getAsJsonObject();
            String advId = DBPg.getInstance().getAdvIdFromSubid(advertizer.get("subid").getAsString());
            List<Advertiser> searchedAdvertizer = this.activeAdvertisersNew
                    .stream()
                    .filter(advertiser -> advertiser.getIdAdmitad() == Long.parseLong(advId))
                    .collect(Collectors.toList());
            return searchedAdvertizer.get(0);
        } catch (IOException e) {
            e.printStackTrace();
            // return null;
        }
        return null;
    }*/

    public synchronized Advertiser getBuyAdvertizer(long chatId, String subid2) {
        ArrayList<Advertiser> adversInfo = new ArrayList<Advertiser>();
        String url = "https://api.admitad.com/statistics/sub_ids/?order_by=-sales&limit=1&subid1=" + chatId + "&subid2=" + subid2;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        // add request header
        request.addHeader("Authorization", "Bearer " + this.accessToken);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        HttpResponse response = null;
        try {
            response = client.execute(request);
            BufferedReader rd = null;
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JsonParser jsonParser = new JsonParser();
            JsonObject advertizer = jsonParser.parse(result.toString()).getAsJsonObject().getAsJsonArray("results").getAsJsonArray().get(0).getAsJsonObject();
            String advId = DBPg.getInstance().getAdvIdFromSubid(advertizer.get("subid").getAsString());
            List<Advertiser> searchedAdvertizer = this.activeAdvertisersNew
                    .stream()
                    .filter(advertiser -> advertiser.getIdAdmitad() == Long.parseLong(advId))
                    .collect(Collectors.toList());
            return searchedAdvertizer.get(0);
        } catch (IOException e) {
            e.printStackTrace();
            // return null;
        }
        return null;
    }


    /*public synchronized Integer getChatIdClicksCount(String[] subids) {
        StringBuffer subidResult = new StringBuffer();
        for (int i = 0; i < subids.length; i++) {
            if (i > 0) {
                subidResult.append("&subid" + (i + 1) + "=" + subids[i]);
            } else {
                subidResult.append("subid" + (i + 1) + "=" + subids[i]);
            }
        }
        String url = "https://api.admitad.com/statistics/sub_ids/?total=1&" + subidResult;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Bearer " + this.accessToken);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        HttpResponse response = null;
        try {
            response = client.execute(request);
            BufferedReader rd = null;
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            if (result.toString().equals("")) {
                System.out.println("Результатов не возвращает");
                return null;
            } else {
                JsonParser parser = new JsonParser();
                JsonObject mainObject = parser.parse(result.toString()).getAsJsonArray().get(0).getAsJsonObject();
                if (mainObject.toString().equals("{}")) return 0; //если результатов нет
                return mainObject.get("clicks").getAsInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }*/

    public synchronized ArrayList<Long> getClickedAdIds(Long chatId, String from) {
        ArrayList<DeepLink> shortDeeps = DBPg.getInstance().getShortDeeps(chatId, from);
        if(shortDeeps == null) return null;
        String url = "https://btcback.me/yourls-api.php";//?signature=" + Setts.getInstance().getStr("shortner_signature") + "&action=shorturl&url=" + longUrl;
        int clickCount = 0;
        ArrayList<Long> clickedAdIds = new ArrayList<>();
        for (DeepLink deep : shortDeeps) {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
            urlParameters.add(new BasicNameValuePair("signature", Setts.getInstance().getStr("shortner_signature")));
            urlParameters.add(new BasicNameValuePair("action", "url-stats"));
            urlParameters.add(new BasicNameValuePair("shorturl", deep.getShortLink()));
            try {
                post.setEntity(new UrlEncodedFormEntity(urlParameters));
                try {
                    HttpResponse response = client.execute(post);
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    StringBuffer result = new StringBuffer();
                    while ((line = bufferedReader.readLine()) != null) {
                        result.append(line);
                    }
                    if (result.toString().equals("")) {
                        System.out.println("Shornter troble");
                        return null;
                    } else {
                        if(!result.toString().contains("short URL not found")){
                            if(Long.parseLong(result.toString().split("<clicks>")[1].split("</clicks>")[0]) > 0){
                                clickedAdIds.add(deep.getAdvId());
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (UnsupportedEncodingException e) {
                System.out.println("Не удалось преобразовать URL");
                e.printStackTrace();
                System.exit(0);
                return null;
            }
        }
        return clickedAdIds;
    }

    public synchronized Long getChatIdBuyActionsCount(Long chatId) {
        String url = "https://api.admitad.com/statistics/sub_ids/?total=1&subid1=" + chatId;
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Bearer " + this.accessToken);
        request.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        HttpResponse response = null;
        try {
            response = client.execute(request);
            BufferedReader rd = null;
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            if (result.toString().equals("")) {
                System.out.println("Результатов не возвращает");
                return null;
            } else {
                JsonParser parser = new JsonParser();
                JsonObject mainObject = parser.parse(result.toString()).getAsJsonArray().get(0).getAsJsonObject();
                if (mainObject.toString().equals("{}")) return Long.valueOf(0); //если результатов нет
                return mainObject.get("actions_sum_total").getAsLong();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized String getAccessToken() {
        return this.accessToken;
    }

    public synchronized String toAdmitadDateTime(String notFormatDateString) { //in -> yyyy-MM-dd HH:mm:ss to out -> dd.MM.yyyy+HH:mm:ss
        Date notFormatDate;
        String formatedDateTime;
        DateFormat inputFormatDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        SimpleDateFormat outputFormatDateTime = new SimpleDateFormat("dd.MM.yyyy+HH:mm:ss");
        try {
            notFormatDate = inputFormatDateTime.parse(notFormatDateString);
            formatedDateTime = outputFormatDateTime.format(notFormatDate);
            return formatedDateTime;
        } catch (java.text.ParseException e) {
            System.out.println("Неправильно указан формат даты");
            e.printStackTrace();
            return null;
        }
    }

    public static AdmitadCore getInstance() {
        return INSTANCE;
    }

    public String getToken(){
        return this.accessToken;
    }
}