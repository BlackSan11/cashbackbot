package me.btcback.admitad;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Balance {

    private Map<String, Double> balance;
    private Map<String, Double> minOuts;

    public Balance() {
        this.balance = getBalance();
        this.minOuts = getMinOut();
    }

    public Map<String, Double> getBalance() {
        String url = "https://api.admitad.com/me/balance/";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        // add request header
        request.addHeader("Authorization", "Bearer " + AdmitadCore.getInstance().accessToken);
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
            JsonParser parser = new JsonParser();
            JsonArray responseObject = parser.parse(String.valueOf(result)).getAsJsonArray();
            Map<String, Double> balanceMap = new HashMap<>();
            for (JsonElement element : responseObject) {
                JsonObject thisCurrency = element.getAsJsonObject();
                balanceMap.put(thisCurrency.get("currency").getAsString(), thisCurrency.get("balance").getAsDouble());
            }
            return balanceMap;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public Map<String, Double> getMinOut() {
        String url = "https://api.admitad.com/currencies/";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        // add request header
        request.addHeader("Authorization", "Bearer " + AdmitadCore.getInstance().accessToken);
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
            JsonParser parser = new JsonParser();
            JsonArray responseObject = parser.parse(String.valueOf(result)).getAsJsonObject().getAsJsonArray("results");
            Map<String, Double> minOutsMap = new HashMap<>();
            for (JsonElement jsonElement : responseObject) {
                JsonObject thisCurrency = jsonElement.getAsJsonObject();
                minOutsMap.put(thisCurrency.get("code").getAsString(), thisCurrency.get("min_sum").getAsDouble());
            }
            return minOutsMap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void payOut() {
        String url = "https://api.admitad.com/payments/request/RUB/";

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        post.setHeader("Authorization", "Bearer " + AdmitadCore.getInstance().accessToken);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        HttpResponse response;
        BufferedReader bufferedReader;

        try {
            response = client.execute(post);
            bufferedReader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            JsonParser parser = new JsonParser();
            JsonObject authResponse = parser.parse(String.valueOf(result)).getAsJsonObject();
            System.out.println(authResponse);
            /*if (result.toString().equals("")) {
                System.out.println("Admitad give empty response");
            } else {
                JsonParser parser = new JsonParser();
                JsonObject authResponse = parser.parse(String.valueOf(result)).getAsJsonObject();
                try {
                    return authResponse.get("access_token").toString().replace("\"", "");
                } catch (NullPointerException e) {
                    System.out.println("######Не удалось получить токен######");
                    System.out.println("Описание ошибки: " + authResponse.get("error_description"));
                    System.out.println("Код ошибки: " + authResponse.get("error_code"));
                    System.out.println("Название ошибки: " + authResponse.get("error"));
                    System.out.println("######Повторная попытка авторизации через 15 секунд######");
                    System.out.println("----------------------------------------------------------");
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                        System.out.println("Проблема с Thread.sleep");
                    }
                }
            }*/
        } catch (IOException e) {
            System.out.println("Could not complete the request to:  \n Internet be away or other problems with connection of global net.");
            e.printStackTrace();
        }

    }


}
