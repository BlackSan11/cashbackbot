package me.btcback.admitad;

import com.google.common.io.BaseEncoding;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.btcback.Setts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Auth {

    private String admitadAuthToken;

    public Auth() {
        this.admitadAuthToken = this.setAdmitadToken(
                Setts.getInstance().getStr("admitad_auth_link"),
                Setts.getInstance().getStr("admitad_key_ident"),
                Setts.getInstance().getStr("admitad_key_secret"),
                Setts.getInstance().getStr("admitad_scope_list"));
    }

    public synchronized String setAdmitadToken(String authLink, String keyIdent, String keySecret, String scope) {
        String preBase64 = keyIdent + ":" + keySecret;
        String base64Header = null;
        try {
            base64Header = BaseEncoding.base64().encode(preBase64.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            System.out.println("Не удалось закодировать строку в Base64");
            e.printStackTrace();
            System.exit(0);
        }
        String url = authLink;

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        post.setHeader("Authorization", "Basic " + base64Header);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        urlParameters.add(new BasicNameValuePair("client_id", keyIdent));
        urlParameters.add(new BasicNameValuePair("scope", scope));

        try {
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
        } catch (UnsupportedEncodingException e) {
            System.out.println("Не удалось преобразовать URL");
            e.printStackTrace();
            System.exit(0);
        }
        HttpResponse response;
        BufferedReader bufferedReader;
        String accessToken;
        while (true) {
            try {
                response = client.execute(post);
                bufferedReader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                StringBuffer result = new StringBuffer();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                if (result.toString().equals("")) {
                    System.out.println("Admitad give empty response");
                    continue;
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
                }
            } catch (IOException e) {
                System.out.println("Could not complete the request to: " + authLink + "\n Internet be away or other problems with connection of global net.");
                e.printStackTrace();
            }
        }
    }

    public String getAdmitadAuthToken() {
        return admitadAuthToken;
    }


}
