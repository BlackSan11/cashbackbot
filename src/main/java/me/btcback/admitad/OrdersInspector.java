package me.btcback.admitad;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.btcback.Setts;
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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class OrdersInspector extends Thread {
    @Override
    public void run() {
        while (true){
            parseOdersInAdmitad();
            parsePaidInDB();
            try {
                this.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseOdersInAdmitad() {
        System.out.println("Parse Orders Admitad start..");
        ArrayList<Order> orders = getNewOrders();
        for (Order orderFromAdmitad : orders) {
            Boolean ifExist = DBPg.getInstance().checkOrderInDB(orderFromAdmitad.getActionId());
            if (ifExist != null) {
                if (ifExist) { //ЕСЛИ ТАКОЙ ЗАКАЗ УЖЕ ЕСТЬ В БД
                    Order thisOrderInDb = DBPg.getInstance().getOrderWhereActionId(orderFromAdmitad.getActionId());
                    if (!thisOrderInDb.getStatus().equals(orderFromAdmitad.getStatus())) { //ЕСЛИ СТАТУС В БД И СТАТУС В АДМИТАДЕ ОТЛИЧАЮТСЯ
                        if (orderFromAdmitad.getStatus().equals("approved") & thisOrderInDb.getStatus().equals("pending")) { //если статус сменился на approved
                            //отправляем уведомление что заказ подтвержден, выплатим через сколькото
                            DBPg.getInstance().updateOrderStatusInDb(thisOrderInDb.getDbId(), orderFromAdmitad.getStatus(), orderFromAdmitad.getStatusUpdatedDateTime());
                            double userPaymentCurr = orderFromAdmitad.getPayment() * Setts.getInstance().getDbl("user_persent");
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName()); //{1} - название магазина
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency()); //{2} - полная сумма службы и код валюты
                            dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency()); //{3} - пользовательская сумма cashback в валюте
                            dinElms.add(new BigDecimal(thisOrderInDb.getPaymentBtc()).setScale(8, RoundingMode.HALF_UP).toString()); //{4} - сумма кэшбека в BTC
                            new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "approved", dinElms).send();

                            if (thisOrderInDb.getPaid() == 0 & orderFromAdmitad.getPaid() == 1) { //ЕСЛИ ДЕНЬГИ ВЫВЕДЕНЫ С АДМИТАД
                                double userPaymentBtc = Double.parseDouble(new BTCOperator().convertToBTC(thisOrderInDb.getCurrency(), userPaymentCurr).toString());
                                DBPg.getInstance().updateOrderPaidOutStatus(thisOrderInDb.getDbId(), 1, 1);
                                new BTCCheck(
                                        Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())),
                                        userPaymentBtc, "cashback").sendToUserBalance();
                                dinElms = new ArrayList();
                                dinElms.add(orderFromAdmitad.getOfferName());
                                dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP).toString());
                                new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "cashback", dinElms).send();
                            }

                        } else if (orderFromAdmitad.getStatus().equals("declined") && (thisOrderInDb.getStatus().equals("pending") || thisOrderInDb.getStatus().equals("approved"))) { //ЕСЛИ ЗАКАЗ ОТКЛОНЕН
                            DBPg.getInstance().updateOrderStatusInDb(thisOrderInDb.getDbId(), orderFromAdmitad.getStatus(), orderFromAdmitad.getStatusUpdatedDateTime());
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName());
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                            new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "declined", dinElms).send();
                        } else {

                        }
                    } else {

                    }
                } else { //если такого заказа нет в БД
                    double userPaymentCurr = orderFromAdmitad.getPayment() * Setts.getInstance().getDbl("user_persent");
                    double userPaymentBtc = Double.parseDouble(new BTCOperator().convertToBTC(orderFromAdmitad.getCurrency(), userPaymentCurr).toString());
                    DBPg.getInstance().putOrderToDB(
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
                            Setts.getInstance().getDbl("user_persent"),
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
                            new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "pending", dinElms).send();
                            break;
                        }
                        case "approved": { //ЕСЛИ ЗАКАЗ ПОДТВЕРЖДЕН
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName());
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                            dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency());
                            dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP).toString());
                            new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "approved", dinElms).send();
                            if (orderFromAdmitad.getPaid() == 1) {
                                Order thisOrderInDb = DBPg.getInstance().getOrderWhereActionId(orderFromAdmitad.getActionId());
                                DBPg.getInstance().updateOrderPaidOutStatus(thisOrderInDb.getDbId(), 1, 1);
                                new BTCCheck(
                                        Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())),
                                        userPaymentBtc, "cashback").sendToUserBalance();
                                DBPg.getInstance().updateOrderBTCPaymentAI(orderFromAdmitad.getActionId(), userPaymentBtc);
                                dinElms = new ArrayList();
                                dinElms.add(orderFromAdmitad.getOfferName());
                                dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(userPaymentCurr + " " + orderFromAdmitad.getCurrency());
                                dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP).toString());
                                new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "cashback", dinElms).send();
                            }
                            break;
                        }
                        case "declined": { //ЕСЛИ ЗАКАЗ ОТКЛОНЕН
                            ArrayList dinElms = new ArrayList();
                            dinElms.add(orderFromAdmitad.getOfferName());
                            dinElms.add(orderFromAdmitad.getSumTotal() + " " + orderFromAdmitad.getCurrency());
                            new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(orderFromAdmitad.getSubid())), "declined", dinElms).send();
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
        }
    }

    private void parsePaidInDB() {
        System.out.println("Parse Orders paid=0 started..");
        ArrayList<Order> ordersInDB = DBPg.getInstance().getPaidOrdersFromDb();
        for (Order thisOrderInDb : ordersInDB) {
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpGet requestGet;
            HttpResponse httpResponse;
            BufferedReader httpResponseReader;
            StringBuffer resultBuffer;
            String requestUrl = "https://api.admitad.com/statistics/actions/?action_id=" + thisOrderInDb.getActionId();
            requestGet = new HttpGet(requestUrl);
            requestGet.addHeader("Authorization", "Bearer " + AdmitadCore.getInstance().accessToken);
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
                }*/
                JsonParser ordersJsonParser = new JsonParser();
                JsonObject ordersJsonObj = ordersJsonParser.parse(resultBuffer.toString()).getAsJsonObject();
                JsonArray orderJsonArray = ordersJsonObj.getAsJsonArray("results");
                if (orderJsonArray == null | orderJsonArray.toString().equals("[]") | orderJsonArray.size() < 1) break;
                JsonObject thisOrderInAdmitad = orderJsonArray.get(0).getAsJsonObject();
                if (getNullAsEmptyInt(thisOrderInAdmitad.get("paid")) == 1 & thisOrderInDb.getPaid() == 0 & thisOrderInDb.getActionId() == getNullAsEmptyLong(thisOrderInAdmitad.get("action_id")) & getNullAsEmptyString(thisOrderInAdmitad.get("status")).equals("approved")) {
                    if (DBPg.getInstance().updateOrderPaidOutStatus(thisOrderInDb.getDbId(), 1, 1)) {
                        double userPaymentCurr = thisOrderInDb.getPayment() * thisOrderInDb.getUserPersent();
                        double userPaymentBtc = Double.parseDouble(new BTCOperator().convertToBTC(thisOrderInDb.getCurrency(), userPaymentCurr).toString());
                        new BTCCheck(
                                Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(thisOrderInDb.getSubid())),
                                userPaymentBtc, "cashback").sendToUserBalance();
                        DBPg.getInstance().updateOrderBTCPayment(thisOrderInDb.getDbId(), userPaymentBtc);
                        ArrayList dinElms = new ArrayList();
                        dinElms.add(thisOrderInDb.getOfferName()); //{1} - название магазина
                        dinElms.add(thisOrderInDb.getSumTotal() + " " + thisOrderInDb.getCurrency()); //{2} - сумма покупки
                        dinElms.add(userPaymentCurr + " " + thisOrderInDb.getCurrency()); //{3} - кэшбек в валюте
                        //TODO:преобразовать к нормальному виду дробь
                        dinElms.add(new BigDecimal(userPaymentBtc).setScale(8, RoundingMode.HALF_UP)); //{4} - кэшбек в BTC
                        new Message(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(thisOrderInDb.getSubid())), "cashback", dinElms).send();
                    }
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getNullAsEmptyString(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
    }

    private int getNullAsEmptyInt(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? -1 : jsonElement.getAsInt();
    }

    private long getNullAsEmptyLong(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? -1 : jsonElement.getAsLong();
    }

    private double getNullAsEmptyDouble(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? -1 : jsonElement.getAsDouble();
    }

    public ArrayList<Order> getNewOrders() {
        String lastInspectingOrdersDateTime = "2017-01-01 01:01:02";
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
            requestGet.addHeader("Authorization", "Bearer " + AdmitadCore.getInstance().getAccessToken());
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
                }*/
                if (resultBuffer.toString().equals("")) {
                    System.out.println("Результатов не возвращает");
                    return new ArrayList<>();
                }
                JsonParser ordersJsonParser = new JsonParser();
                JsonObject ordersJsonObj = ordersJsonParser.parse(resultBuffer.toString()).getAsJsonObject();
                JsonObject metaDataObject = ordersJsonObj.getAsJsonObject("_meta");
                JsonArray ordersJsonArray = ordersJsonObj.getAsJsonArray("results");
                if (ordersJsonArray == null || ordersJsonArray.toString().equals("[]")) break; //если результатов нет
                JsonObject thisOrderFromResponse;
                for (JsonElement jsonElement : ordersJsonArray) {
                    thisOrderFromResponse = jsonElement.getAsJsonObject();
                    Boolean ifDeepExist = DBPg.getInstance().checkSubidInDB(getNullAsEmptyString(thisOrderFromResponse.get("subid")));
                    if (ifDeepExist != null) {
                        if (ifDeepExist) {
                            Order thisOrderInDb = DBPg.getInstance().getOrderWhereActionId(getNullAsEmptyLong(thisOrderFromResponse.get("action_id")));
                            Double userPersent;
                            Double userPaymentBtc;
                            String tlgUsername;
                            if (thisOrderInDb != null) {
                                userPersent = thisOrderInDb.getUserPersent();
                                userPaymentBtc = thisOrderInDb.getPaymentBtc();
                                tlgUsername = thisOrderInDb.getTlgUsername();
                            } else {
                                userPersent = Setts.getInstance().getDbl("user_persent");
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

}