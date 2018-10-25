package me.btcback.finance;


import me.btcback.Setts;
import me.btcback.tlg.MainBot;
import me.btcback.tlg.Message;
import me.btcback.tlg.MessegesGenerator;
import me.btcback.finance.models.BTCCheck;
import me.btcback.pgdb.DBPg;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BTCOperator {
    MessegesGenerator msgGen;
    private volatile Map<String, Double> btcIndexes;

    public BTCOperator() {
        this.msgGen = new MessegesGenerator();
        System.out.println("BTCOperator inited");
    }

    public synchronized BigDecimal convertToBTC(String currency, Double sum) {
        updateBtcIndex();
        Double costThisCurrency = btcIndexes.get(currency);
        costThisCurrency = costThisCurrency + (costThisCurrency * Double.parseDouble(Setts.getInstance().getStr("btc_cost_up_persent")));
        Double convertedSumTemp = sum / costThisCurrency;
        BigDecimal convertedSum = BigDecimal.valueOf(convertedSumTemp).setScale(8, BigDecimal.ROUND_HALF_UP);
        return convertedSum;
    }

    public synchronized void updateBtcIndex() {
        String url = "https://min-api.cryptocompare.com/data/price?fsym=BTC&tsyms=USD,RUB,UAH,EUR,BYN,KZT,AED,INR";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        try {
            HttpResponse response = client.execute(request);
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            StringBuffer result = new StringBuffer();
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            JsonParser parser = new JsonParser();
            JsonObject indexesJson = parser.parse(result.toString()).getAsJsonObject();
            Map<String, Double> freshIndexes = new HashMap<>();
            for (Map.Entry<String, JsonElement> stringJsonElementEntry : indexesJson.entrySet()) {
                freshIndexes.put(stringJsonElementEntry.getKey().toString(), Double.parseDouble(stringJsonElementEntry.getValue().toString()));
            }
            this.btcIndexes = freshIndexes;
        } catch (IOException e) {
            e.printStackTrace();
            this.btcIndexes = new HashMap<>();
        }
    }


}
