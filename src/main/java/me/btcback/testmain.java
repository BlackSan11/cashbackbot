package me.btcback;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.btcback.admitad.AdmitadCore;
import me.btcback.admitad.models.Order;
import me.btcback.finance.BTCOperator;
import me.btcback.finance.models.BTCCheck;
import me.btcback.pgdb.DBPg;
import me.btcback.tlg.Message;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class testmain {
    private final String DB_HOST = "localhost";//
    private final String DB_NAME = "cashbackbotbase";
    private final String DB_USER = "admin_btcback_me";
    private final String DB_PASS = "Chy3nKS093";
    private final String DB_PORT = "5432";
    public static void main(String[] args) {

    }

    public Connection connectionToDB;


    public void init() {
        System.out.println("DB inited.");
    }

    public synchronized Connection connectToDb() {
        System.out.println("dssdds");
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager
                    .getConnection("jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + this.DB_NAME, this.DB_USER, this.DB_PASS);
            connection.setAutoCommit(false);
//            System.out.println("Подключились к БД");
            return connection;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public synchronized Statement createStatementMy() {
        if (this.connectionToDB != null) {
            try {
                Statement stmt = this.connectionToDB.createStatement();
                return stmt;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

    }
}
/*
}
    private static void parseOdersInAdmitad() {
        ArrayList<Order> orders = getNewOrders();
        for (Order orderFromAdmitad : orders) {
            Boolean ifExist = DBPg.INSTANCE.checkOrderInDB(orderFromAdmitad.getActionId());
            if (ifExist != null) {
                if (ifExist) { //ЕСЛИ ТАКОЙ ЗАКАЗ УЖЕ ЕСТЬ В БД
                    Order thisOrderInDb = DBPg.INSTANCE.getOrderWhereActionId(orderFromAdmitad.getActionId());

                    if (!thisOrderInDb.getStatus().equals(orderFromAdmitad.getStatus())) { //ЕСЛИ СТАТУС В БД И СТАТУС В АДМИТАДЕ ОТЛИЧАЮТСЯ
                        if (orderFromAdmitad.getStatus().equals("approved") & thisOrderInDb.getStatus().equals("pending")) { //если статус сменился на approved
                            //отправляем уведомление что заказ подтвержден, выплатим через сколькото
                            DBPg.INSTANCE.updateOrderStatusInDb(orderFromAdmitad.getDbId(), orderFromAdmitad.getStatus(), orderFromAdmitad.getStatusUpdatedDateTime());
                            double userPaymentCurr = orderFromAdmitad.getPayment() * Setts.INSTANCE.getDbl("user_persent");
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName()); //{1} - название магазина
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency()); //{2} - полная сумма службы и код валюты
                            dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency()); //{3} - пользовательская сумма cashback в валюте
                            dinElms.add(new BigDecimal(thisOrderInDb.getPaymentBtc()).setScale(8, RoundingMode.HALF_UP).toString()); //{4} - сумма кэшбека в BTC
                            new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "approved", dinElms).send();
                            if (thisOrderInDb.getPaid() == 0 & orderFromAdmitad.getPaid() == 1) { //ЕСЛИ ДЕНЬГИ ВЫВЕДЕНЫ С АДМИТАД
                                double userPaymentBtc = Double.parseDouble(new BTCOperator().convertToBTC(thisOrderInDb.getCurrency(), userPaymentCurr).toString());
                                DBPg.INSTANCE.updateOrderPaidOutStatus(thisOrderInDb.getDbId(), 1, 1);
                                new BTCCheck(
                                        Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())),
                                        userPaymentBtc, "cashback").sendToUserBalance();
                                dinElms = new ArrayList();
                                dinElms.add(orderFromAdmitad.getOfferName());
                                dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP).toString());
                                new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "cashback", dinElms).send();
                            }
                        } else if (orderFromAdmitad.getStatus().equals("declined") & thisOrderInDb.getStatus().equals("pending")) { //ЕСЛИ ЗАКАЗ ОТКЛОНЕН
                            DBPg.INSTANCE.updateOrderStatusInDb(orderFromAdmitad.getDbId(), orderFromAdmitad.getStatus(), orderFromAdmitad.getStatusUpdatedDateTime());
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName());
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                            new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "declined", dinElms).send();
                        } else {

                        }
                    } else {

                    }
                } else { //если такого заказа нет в БД
                    double userPaymentCurr = orderFromAdmitad.getPayment() * Setts.INSTANCE.getDbl("user_persent");
                    double userPaymentBtc = Double.parseDouble(new BTCOperator().convertToBTC(orderFromAdmitad.getCurrency(), userPaymentCurr).toString());
                    DBPg.INSTANCE.putOrderToDB(
                            orderFromAdmitad.getOfferId(),
                            orderFromAdmitad.getSumTotal(),
                            orderFromAdmitad.getStatus(),
                            orderFromAdmitad.getCurrency(),
                            orderFromAdmitad.getDatetime(),
                            orderFromAdmitad.getProcessingDateTime(),
                            orderFromAdmitad.getSubid(),
                            orderFromAdmitad.getSubid1(),
                            orderFromAdmitad.getSubid2(),
                            orderFromAdmitad.getSubid3(),
                            orderFromAdmitad.getSubid4(),
                            orderFromAdmitad.getStatusUpdatedDateTime(),
                            orderFromAdmitad.getProcessed(),
                            orderFromAdmitad.getPaid(),
                            orderFromAdmitad.getOfferOrderId(),
                            orderFromAdmitad.getActionId(),
                            Setts.INSTANCE.getDbl("user_persent"),
                            orderFromAdmitad.getPayment(),
                            userPaymentBtc,
                            orderFromAdmitad.getOfferName(),
                            orderFromAdmitad.getClosingDate(),
                            orderFromAdmitad.getSubid1().equals("") ? "" : new Message(Long.parseLong(orderFromAdmitad.getSubid1())).getUsername()
                    );
                    switch (orderFromAdmitad.getStatus()) {
                        case "pending": { //ЕСЛИ ЗАКАЗ В СТАТУСЕ ОЖИДАНИЯ ОДТВЕРЖДЕНИЯ
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName());
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                            dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency());
                            dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP).toString());
                            new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "pending", dinElms).send();
                            break;
                        }
                        case "approved": { //ЕСЛИ ЗАКАЗ ПОДТВЕРЖДЕН
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName());
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                            dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency());
                            dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP).toString());
                            new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "approved", dinElms).send();
                            if (orderFromAdmitad.getPaid() == 1) {
                                DBPg.INSTANCE.updateOrderPaidOutStatus(orderFromAdmitad.getDbId(), 1, 1);
                                new BTCCheck(
                                        Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())),
                                        userPaymentBtc, "cashback").sendToUserBalance();
                                DBPg.INSTANCE.updateOrderBTCPaymentAI(orderFromAdmitad.getActionId(), userPaymentBtc);
                                dinElms = new ArrayList();
                                dinElms.add(orderFromAdmitad.getOfferName());
                                dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP).toString());
                                new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "cashback", dinElms).send();
                            }
                            break;
                        }
                        case "declined": { //ЕСЛИ ЗАКАЗ ОТКЛОНЕН
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName());
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                            new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "declined", dinElms).send();
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
        }
    }

    private static void parsePaidInDB() {
        ArrayList<Order> ordersInDB = DBPg.INSTANCE.getPaidOrdersFromDb();
        for (Order thisOrderInDb : ordersInDB) {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet requestGet;
            HttpResponse httpResponse;
            BufferedReader httpResponseReader;
            StringBuffer resultBuffer;
            String requestUrl = "https://api.admitad.com/statistics/actions/?action_id=" + thisOrderInDb.getActionId();
            requestGet = new HttpGet(requestUrl);
            requestGet.addHeader("Authorization", "Bearer " + AdmitadCore.INSTANCE.accessToken);
            try {
                httpClient = HttpClientBuilder.create().build();
                httpResponse = httpClient.execute(requestGet);
                httpResponseReader = new BufferedReader(
                        new InputStreamReader(httpResponse.getEntity().getContent()));
                resultBuffer = new StringBuffer();
                String line = "";
                while ((line = httpResponseReader.readLine()) != null) {
                    resultBuffer.append(line);
                }
                */
/*resultBuffer = new StringBuffer();
                BufferedReader br = new BufferedReader(new FileReader("c:/file.txt"));
                try {
                    String line2 = br.readLine();
                    while (line2 != null) {
                        resultBuffer.append(line2);
                        line2 = br.readLine();
                    }
                } finally {
                    br.close();
                }*//*

                JsonParser ordersJsonParser = new JsonParser();
                JsonObject ordersJsonObj = ordersJsonParser.parse(resultBuffer.toString()).getAsJsonObject();
                JsonArray orderJsonArray = ordersJsonObj.getAsJsonArray("results");
                if (orderJsonArray == null | orderJsonArray.toString().equals("[]") | orderJsonArray.size() < 1) break;
                JsonObject thisOrderInAdmitad = orderJsonArray.get(0).getAsJsonObject();
                if (getNullAsEmptyInt(thisOrderInAdmitad.get("paid")) == 1 & thisOrderInDb.getPaid() == 0 & thisOrderInDb.getActionId() == getNullAsEmptyLong(thisOrderInAdmitad.get("action_id")) & getNullAsEmptyString(thisOrderInAdmitad.get("status")).equals("approved")) {
                    if (DBPg.INSTANCE.updateOrderPaidOutStatus(thisOrderInDb.getDbId(), 1, 1)) {
                        double userPaymentCurr = thisOrderInDb.getPayment() * thisOrderInDb.getUserPersent();
                        double userPaymentBtc = Double.parseDouble(new BTCOperator().convertToBTC(thisOrderInDb.getCurrency(), userPaymentCurr).toString());
                        new BTCCheck(
                                Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(thisOrderInDb.getSubid())),
                                userPaymentBtc, "cashback").sendToUserBalance();
                        DBPg.INSTANCE.updateOrderBTCPayment(thisOrderInDb.getDbId(), userPaymentBtc);
                        ArrayList dinElms = new ArrayList();
                        dinElms.add(thisOrderInDb.getOfferName()); //{1} - название магазина
                        dinElms.add(thisOrderInDb.getSumTotal() + " " + thisOrderInDb.getCurrency()); //{2} - сумма покупки
                        dinElms.add(userPaymentCurr + " " + thisOrderInDb.getCurrency()); //{3} - кэшбек в валюте
                        //TODO:преобразовать к нормальному виду дробь
                        dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP)); //{4} - кэшбек в BTC
                        new Message(Long.parseLong(DBPg.INSTANCE.getChatIDFromDeeplinkSubID(thisOrderInDb.getSubid())), "cashback", dinElms).send();
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getNullAsEmptyString(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
    }

    private static int getNullAsEmptyInt(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? -1 : jsonElement.getAsInt();
    }

    private static long getNullAsEmptyLong(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? -1 : jsonElement.getAsLong();
    }

    private static double getNullAsEmptyDouble(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? -1 : jsonElement.getAsDouble();
    }

    public static ArrayList<Order> getNewOrders() {
        String lastInspectingOrdersDateTime = DBPg.INSTANCE.getOrdersLastUpdateStatusDateTime();
        if (lastInspectingOrdersDateTime != null) {
            SimpleDateFormat lastInspectingOrdersDateTimeDTFormatFromDB = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat lastInspectingOrdersDateTimeDTFormatNeed = new SimpleDateFormat("dd.MM.yyyy+HH:mm:ss");
            try {
                Date lastInspectingOrdersDT = lastInspectingOrdersDateTimeDTFormatFromDB.parse(lastInspectingOrdersDateTime);
                lastInspectingOrdersDateTime = lastInspectingOrdersDateTimeDTFormatNeed.format(lastInspectingOrdersDT);
            } catch (ParseException e) {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
        int limit = 100;
        int offset = 0;
        int count = 0;
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet requestGet;
        HttpResponse httpResponse;
        BufferedReader httpResponseReader;
        StringBuffer resultBuffer;
        ArrayList<Order> orders = new ArrayList<>();
        boolean flag = true;
        while (flag) {
            String requestUrl = "https://api.admitad.com/statistics/actions/?status_updated_start=" +
                    lastInspectingOrdersDateTime + "&limit=" + limit + "&offset=" + offset + "";
            requestGet = new HttpGet(requestUrl);
            requestGet.addHeader("Authorization", "Bearer " + AdmitadCore.INSTANCE.accessToken);
            try {
                httpClient = HttpClientBuilder.create().build();
                httpResponse = httpClient.execute(requestGet);
                httpResponseReader = new BufferedReader(
                        new InputStreamReader(httpResponse.getEntity().getContent()));
                resultBuffer = new StringBuffer();
                String line = "";
                while ((line = httpResponseReader.readLine()) != null) {
                    resultBuffer.append(line);
                }
                */
/*resultBuffer = new StringBuffer();
                BufferedReader br = new BufferedReader(new FileReader("c:/file.txt"));
                try {
                    String line2 = br.readLine();
                    while (line2 != null) {
                        resultBuffer.append(line2);
                        line2 = br.readLine();
                    }
                    String everything = resultBuffer.toString();
                } finally {
                    br.close();
                }*//*

                if (resultBuffer.toString().equals("")) {
                    System.out.println("Результатов не возвращает");
                    return new ArrayList<>();
                }
                JsonParser ordersJsonParser = new JsonParser();
                JsonObject ordersJsonObj = ordersJsonParser.parse(resultBuffer.toString()).getAsJsonObject();
                JsonObject metaDataObject = ordersJsonObj.getAsJsonObject("_meta");
                JsonArray ordersJsonArray = ordersJsonObj.getAsJsonArray("results");
                if (ordersJsonArray == null | ordersJsonArray.toString().equals("[]")) break; //если результатов нет
                JsonObject thisOrderFromResponse;
                for (JsonElement jsonElement : ordersJsonArray) {
                    thisOrderFromResponse = jsonElement.getAsJsonObject();
                    Boolean ifDeepExist = DBPg.INSTANCE.checkSubidInDB(getNullAsEmptyString(thisOrderFromResponse.get("subid")));
                    if (ifDeepExist != null) {
                        if (ifDeepExist) {
                            Order thisOrderInDb = DBPg.INSTANCE.getOrderWhereActionId(getNullAsEmptyLong(thisOrderFromResponse.get("action_id")));
                            Double userPersent;
                            Double userPaymentBtc;
                            String tlgUsername;
                            if (thisOrderInDb != null) {
                                userPersent = thisOrderInDb.getUserPersent();
                                userPaymentBtc = thisOrderInDb.getPaymentBtc();
                                tlgUsername = thisOrderInDb.getTlgUsername();
                            } else {
                                userPersent = Setts.INSTANCE.getDbl("user_persent");
                                userPaymentBtc = Double.parseDouble(new BTCOperator().convertToBTC(getNullAsEmptyString(thisOrderFromResponse.get("currency")), getNullAsEmptyDouble(thisOrderFromResponse.get("payment")) * userPersent).toString());
                                Long chatId = getNullAsEmptyLong(thisOrderFromResponse.get("subid1"));
                                if (chatId != -1 & chatId != null & !chatId.equals("")) {
                                    tlgUsername = new Message(chatId).getUsername();
                                } else {
                                    tlgUsername = "not set";
                                }
                            }
                            orders.add(new Order(
                                    thisOrderFromResponse.get("advcampaign_id").getAsLong(),
                                    getNullAsEmptyDouble(thisOrderFromResponse.get("cart")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("status")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("currency")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("action_date")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("closing_date")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("subid")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("subid1")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("subid2")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("subid3")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("subid4")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("status_updated")),
                                    thisOrderFromResponse.get("processed").getAsInt(),
                                    getNullAsEmptyInt(thisOrderFromResponse.get("paid")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("order_id")),
                                    getNullAsEmptyLong(thisOrderFromResponse.get("action_id")),
                                    getNullAsEmptyString(thisOrderFromResponse.get("advcampaign_name")),
                                    userPersent,
                                    getNullAsEmptyDouble(thisOrderFromResponse.get("payment")),
                                    userPaymentBtc,
                                    getNullAsEmptyString(thisOrderFromResponse.get("closing_date")),
                                    tlgUsername
                            ));
                        }
                    }
                }
                count = metaDataObject.get("count").getAsInt();
                limit = metaDataObject.get("limit").getAsInt();
                offset = metaDataObject.get("offset").getAsInt();
                if (limit + offset >= count) {
                    break;
                } else {
                    offset += limit;
                }
            } catch (IOException e) {
                e.printStackTrace();
                flag = false;
            }
        }
        return orders;
    }
*/


   /* public static synchronized void getAdvertizers() {
        ArrayList<Advertiser> adversInfo = new ArrayList<Advertiser>();
        System.out.println(AdmitadCore.INSTANCE.myPlaceID);
        String url = "https://api.admitad.com/advcampaigns/?limit=2000&offset=0&has_tool=deeplink&website=" + AdmitadCore.INSTANCE.myPlaceID;
        System.out.println(url);
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);
        // add request header
        request.addHeader("Authorization", "Bearer " + AdmitadCore.INSTANCE.accessToken);
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
            JsonObject advertizersMeta = jsonParser.parse(result.toString()).getAsJsonObject();
            System.out.println(advertizersMeta.get("_meta"));
            int counterAll = 0;
            for (JsonElement advertizer : advertizers) {
                JsonObject thisAdvertizer = advertizer.getAsJsonObject();
                if (!Boolean.parseBoolean(thisAdvertizer.get("connected").getAsString()) &
                        thisAdvertizer.get("action_type").getAsString().equals("sale") &
                        (thisAdvertizer.get("regions").toString().contains("RU") |
                        thisAdvertizer.get("regions").toString().contains("UA")|
                        thisAdvertizer.get("regions").toString().contains("BY")|
                        thisAdvertizer.get("regions").toString().contains("KZ"))) {
                    getAdvertizers(thisAdvertizer.get("id").getAsLong());
                    System.out.println(++counterAll + "| adv_id:" + thisAdvertizer.get("name").getAsString()+ "| adv_regs:" + thisAdvertizer.get("regions"));
                    Thread.sleep(300);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void getAdvertizers(Long advId) {
        String url = "https://api.admitad.com/advcampaigns/" + advId + "/attach/" + AdmitadCore.INSTANCE.myPlaceID + "/";
        System.out.println(url);
        HttpClient client = HttpClientBuilder.create().build();
        // add request header
        HttpPost post = new HttpPost(url);

        post.setHeader("Authorization", "Bearer " + AdmitadCore.INSTANCE.accessToken);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        HttpResponse response = null;
        try {
            response = client.execute(post);
            BufferedReader rd = null;
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            JsonParser jsonParser = new JsonParser();
            JsonObject advertizers = jsonParser.parse(result.toString()).getAsJsonObject();
            System.out.println(advertizers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

