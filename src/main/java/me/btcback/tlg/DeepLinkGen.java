package me.btcback.tlg;

import me.btcback.Setts;
import me.btcback.admitad.AdmitadCore;
import me.btcback.admitad.Advertiser;
import me.btcback.admitad.models.DeepLink;
import me.btcback.pgdb.DBPg;
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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DeepLinkGen {

    private volatile String refUrl;
    private volatile Long advId;
    private volatile String advName;
    private volatile String inputUrl;
    private volatile String subid;
    private volatile String subid1;
    private volatile String subid2;
    private volatile String subid3;
    private volatile Advertiser advertiserNow;

    /**
     * Юзаем если не знаем реферальную ссылку рекламодателя но знаем его Id в Admitad
     *
     * @param advId  - ид рекламодателя в Admitad
     * @param subid1 - чат ид юзверя
     * @param subid2 - откуда переход(link, list)
     */
    public DeepLinkGen(Long advId, String subid1, String subid2) {
        this.advId = advId;
        this.subid = subIdGen();
        this.subid1 = subid1;
        this.subid2 = subid2;
        this.advertiserNow = getAdvFromAdvAdmitadId(advId);
        this.refUrl = advertiserNow.getRefLink();
        this.advName = advertiserNow.getName();
    }

    /**
     * Юзаем если есть только сырая ссылка
     *
     * @param inputUrl сырая ссылка
     * @param subid1   - чат ид юзверя
     */
    public DeepLinkGen(String inputUrl, String subid1, String subid2) {
        this.inputUrl = inputUrl.trim();
        this.subid = subIdGen();
        this.subid1 = subid1;
        this.subid2 = subid2;
        this.advertiserNow = getAdvFromKeyFromLink(this.inputUrl);
        if (advertiserNow != null) {
            this.refUrl = advertiserNow.getRefLink();
            this.advName = advertiserNow.getName();
        }
    }

    public DeepLinkGen(String inputUrl, String subid1, String subid2, String subid3) {
        this.inputUrl = inputUrl.trim();
        this.subid = subIdGen();
        this.subid1 = subid1;
        this.subid2 = subid2;
        this.subid3 = subid3;
        this.advertiserNow = getAdvFromKeyFromLink(this.inputUrl);
        if (advertiserNow != null) {
            this.refUrl = advertiserNow.getRefLink();
            this.advName = advertiserNow.getName();
        }
    }

    /**
     * Если реферальная ссылка не задана в конструкторе получаем ее из ID рекламодателя в Admitad, который передаем в конструктор как advId
     *
     * @return сокращенная реферальная ссылка
     */
    public synchronized DeepLink getShortDeepLink() {
        DeepLink responseDeep = null;
        if (this.refUrl != null) {
            String deeplink = this.refUrl + "?subid=" + this.subid + "&subid1=" + this.subid1 + "&subid2=" + this.subid2;
            if (subid3 != null) deeplink += "&subid3=" + this.subid3;
            if (inputUrl != null) deeplink += "&ulp=" + this.inputUrl;
            String type = "";
            if (subid2.equals("link")) {
                if (this.inputUrl.matches("(http:\\/\\/|https:\\/\\/|)([\\w-_\\.]+)[\\/].+")) {
                    type = "new_deep_from_product_link";
                } else {
                    type = "new_deep_from_link";
                }
            } else if (subid2.equals("dispatch")) {
                type = "new_deep_from_dispatch";
            } else {
                type = "new_deep_from_list";
            }
            String shortDeep = shortUrl(deeplink);
            responseDeep = new DeepLink(this.advName, shortDeep, this.advertiserNow.getTarifs(), type);
            DBPg.getInstance().putDeeplinkToDB(AdmitadCore.getInstance().myPlaceID, this.advertiserNow.getIdAdmitad(), deeplink, Long.parseLong(this.subid1), this.subid, shortDeep, this.subid2);
        }
        return responseDeep;
    }

    /*
     * search advetizer from admitadAdvertizer id in AdmitadCore activeAdvetisers
     * */
    private synchronized Advertiser getAdvFromAdvAdmitadId(long advId) {
        List<Advertiser> searchedAdvertizer = AdmitadCore.getInstance().activeAdvertisersNew
                .stream()
                .filter(advertiser -> advertiser.getIdAdmitad() == advId)
                .collect(Collectors.toList());
        return searchedAdvertizer.get(0);
    }


    /**
     * Получаем ключ для поиска рекламодателя из присланной ссылки
     *
     * @param urlForCreateSearchKey
     * @return
     */
    private synchronized String getKeyForSearchFromInputUrl(String urlForCreateSearchKey) {
        String tempStr = urlForCreateSearchKey
                .toLowerCase()
                .trim()
                .replace("http://", "")
                .replace("https://", "")
                .replaceAll("[/]{1}.+$", "");
        String[] splitStr = tempStr.split("\\.");
        int lengthSplitStrArray = splitStr.length;
        if (lengthSplitStrArray > 1) {
            return splitStr[splitStr.length - 2] + "." + splitStr[splitStr.length - 1].replace("/", "");
        } else {
            return null;
        }
    }


    /**
     * Получаем ссылку рекламодателя по поисковому ключу
     *
     * @param urlToSearch
     * @return
     */
    private synchronized Advertiser getAdvFromKeyFromLink(String urlToSearch) {
        String keyForSearch = getKeyForSearchFromInputUrl(urlToSearch);
        if (keyForSearch != null) {
            List<Advertiser> searchedAdvertizer = AdmitadCore.getInstance().activeAdvertisersNew
                    .stream()
                    .filter(advertiser -> advertiser.getLink().contains(keyForSearch))
                    .collect(Collectors.toList());
            return searchedAdvertizer.size() > 0 ? searchedAdvertizer.get(0) : null;
        } else {
            return null;
        }
    }

    private synchronized String shortUrl(String longUrl) {
        String url = "https://btcback.me/yourls-api.php";//?signature=" + Setts.getInstance().getStr("shortner_signature") + "&action=shorturl&url=" + longUrl;
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("signature", Setts.getInstance().getStr("shortner_signature")));
        urlParameters.add(new BasicNameValuePair("action", "shorturl"));
        urlParameters.add(new BasicNameValuePair("url", longUrl));
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
                    return result.toString().split("<shorturl>")[1].split("</shorturl>")[0];
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

    private synchronized String subIdGen() {

        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(35);
        for (int i = 0; i < 35; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public void setSubid3(String subid3) {
        this.subid3 = subid3;
    }
}
