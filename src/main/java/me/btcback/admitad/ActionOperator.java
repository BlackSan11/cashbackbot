package me.btcback.admitad;

import me.btcback.Setts;
import me.btcback.finance.models.BTCCheck;
import me.btcback.tlg.MainBot;
import me.btcback.tlg.Message;
import me.btcback.tlg.MessegesGenerator;
import me.btcback.admitad.models.Action;
import me.btcback.admitad.models.ApiResponseParamsCLO;
import me.btcback.finance.BTCOperator;
import me.btcback.pgdb.DBPg;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ActionOperator {
    public MainBot tgBot;
    public MessegesGenerator msgGen;

    public ActionOperator(MainBot tgBot, MessegesGenerator messegesGenerator) {
        this.tgBot = tgBot;
        this.msgGen = messegesGenerator;
    }

    private String getNullAsEmptyString(JsonElement jsonElement) {
        return jsonElement.isJsonNull() ? "" : jsonElement.getAsString();
    }

    private Double jsonElementToDoubleSave(JsonElement jsonElement) {
        return (getNullAsEmptyString(jsonElement).equals("") ? Double.parseDouble("0") : Double.parseDouble(jsonElement.getAsString()));
    }

    private Integer jsonElementToIntSave(JsonElement jsonElement) {
        return (getNullAsEmptyString(jsonElement).equals("") ? Integer.parseInt("0") : Integer.parseInt(jsonElement.getAsString()));
    }

    public synchronized Map<ApiResponseParamsCLO, Map<Integer, Action>> getPendingActions(String lastPendingDateTime, int limit, int offset) {
        lastPendingDateTime = AdmitadCore.getInstance().toAdmitadDateTime(lastPendingDateTime);
        if (lastPendingDateTime != null) {
            String url = "https://api.admitad.com/statistics/actions/?status_updated_start="
                    + lastPendingDateTime + "&status=pending&limit=" + limit + "&offset=" + offset + "";
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", "Bearer " + AdmitadCore.getInstance().accessToken);
            try {
                HttpResponse response = client.execute(request);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                StringBuffer result = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                /*BufferedReader br = new BufferedReader(new FileReader("c:/file.txt"));
                StringBuffer result = new StringBuffer();
                try {
                    String line2 = br.readLine();
                    while (line2 != null) {
                        result.append(line2);
                        line2 = br.readLine();
                    }
                    String everything = result.toString();
                } finally {
                    br.close();
                }*/
                if (result.toString().equals("")) {
                    System.out.println("Результатов не возвращает");
                    return null;
                } else {
                    Map<Integer, Action> pendingActions = new HashMap<>();
                    JsonParser parser = new JsonParser();
                    JsonObject mainObject = parser.parse(result.toString()).getAsJsonObject();
                    JsonArray allActions = mainObject.getAsJsonArray("results");
                    if (allActions.toString().equals("[]")) return new HashMap<>(); //если результатов нет
                    JsonObject metaDataObject = mainObject.getAsJsonObject("_meta");
                    int i = 0;
                    JsonArray thisActionPositions;
                    for (JsonElement action : allActions) {
                        JsonObject actionObject = action.getAsJsonObject();
                        JsonObject positions = actionObject.get("positions").getAsJsonArray().get(0).getAsJsonObject();
                        pendingActions.put(i,
                                new Action(
                                        jsonElementToDoubleSave(positions.get("amount")),
                                        jsonElementToDoubleSave(positions.get("payment")),
                                        getNullAsEmptyString(actionObject.get("subid")),
                                        getNullAsEmptyString(actionObject.get("action_date")),
                                        Long.parseLong(getNullAsEmptyString(actionObject.get("action_id"))),
                                        getNullAsEmptyString(actionObject.get("currency")),
                                        getNullAsEmptyString(actionObject.get("status_updated")),
                                        "pending",
                                        0
                                )
                        );
                        i++;
                    }
                    Map<ApiResponseParamsCLO, Map<Integer, Action>> actionsResponse = new HashMap<>();
                    actionsResponse.put(new ApiResponseParamsCLO(Integer.parseInt(metaDataObject.get("count").toString()),
                            Integer.parseInt(metaDataObject.get("limit").toString()),
                            Integer.parseInt(metaDataObject.get("offset").toString())), pendingActions);
                    return actionsResponse;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Дата null");
        return null;
    }

    public synchronized Map<ApiResponseParamsCLO, Map<Integer, Action>> getApprovedActions(String lastAprovedInspectorDateTime, int limit, int offset) {
        lastAprovedInspectorDateTime = AdmitadCore.getInstance().toAdmitadDateTime(lastAprovedInspectorDateTime);
        if (lastAprovedInspectorDateTime != null) {
            String url = "https://api.admitad.com/statistics/actions/?status_updated_start="
                    + lastAprovedInspectorDateTime + "&status=approved&limit=" + limit + "&offset=" + offset + "";
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", "Bearer " + AdmitadCore.getInstance().accessToken);
            try {
                //StringBuffer result = new StringBuffer();
                HttpResponse response = client.execute(request);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                StringBuffer result = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                /*BufferedReader br = new BufferedReader(new FileReader("c:/file.txt"));
                try {
                    String line2 = br.readLine();

                    while (line2 != null) {
                        result.append(line2);
                        line2 = br.readLine();
                    }
                    String everything = result.toString();
                } finally {
                    br.close();
                }*/
                if (result.toString().equals("")) {
                    System.out.println("Результатов не возвращает");
                    return null;
                } else {
                    Map<Integer, Action> approvedActions = new HashMap<>();

                    JsonParser parser = new JsonParser();
                    JsonObject mainObject = parser.parse(result.toString()).getAsJsonObject();

                    JsonArray allActions = mainObject.getAsJsonArray("results");
                    if (allActions.toString().equals("[]")) return new HashMap<>(); //если результатов нет
                    JsonObject metaDataObject = mainObject.getAsJsonObject("_meta");
                    int i = 0;
                    JsonArray thisActionPositions;
                    for (JsonElement action : allActions) {
                        JsonObject actionObject = action.getAsJsonObject();
                        JsonObject positions = actionObject.get("positions").getAsJsonArray().get(0).getAsJsonObject();
                        approvedActions.put(i,
                                new Action(
                                        jsonElementToDoubleSave(positions.get("amount")),
                                        jsonElementToDoubleSave(positions.get("payment")),
                                        getNullAsEmptyString(actionObject.get("subid")),
                                        getNullAsEmptyString(actionObject.get("action_date")),
                                        Long.parseLong(getNullAsEmptyString(actionObject.get("action_id"))),
                                        getNullAsEmptyString(actionObject.get("currency")),
                                        getNullAsEmptyString(actionObject.get("status_updated")),
                                        getNullAsEmptyString(actionObject.get("status")),
                                        jsonElementToIntSave(actionObject.get("paid"))
                                )
                        );
                        i++;
                    }
                    Map<ApiResponseParamsCLO, Map<Integer, Action>> actionsResponse = new HashMap<>();
                    actionsResponse.put(new ApiResponseParamsCLO(Integer.parseInt(metaDataObject.get("count").toString()),
                            Integer.parseInt(metaDataObject.get("limit").toString()),
                            Integer.parseInt(metaDataObject.get("offset").toString())), approvedActions);
                    return actionsResponse;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Дата null");
        return null;
    }

    public synchronized Map<ApiResponseParamsCLO, Map<Integer, Action>> getPaidOutActions(String lastPaidInspectingDateTime, int limit, int offset) {
        lastPaidInspectingDateTime = AdmitadCore.getInstance().toAdmitadDateTime(lastPaidInspectingDateTime);
        if (lastPaidInspectingDateTime != null) {
            String url = "https://api.admitad.com/statistics/actions/?status_updated_start="
                    + lastPaidInspectingDateTime + "&status=approved&paid=1&limit=" + limit + "&offset=" + offset + "";
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(url);
            request.addHeader("Authorization", "Bearer " + AdmitadCore.getInstance().accessToken);
            try {
                //StringBuffer result = new StringBuffer();
                HttpResponse response = client.execute(request);
                BufferedReader bufferedReader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                String line = "";
                StringBuffer result = new StringBuffer();
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line);
                }
                /*BufferedReader br = new BufferedReader(new FileReader("c:/file.txt"));
                try {
                    String line2 = br.readLine();

                    while (line2 != null) {
                        result.append(line2);
                        line2 = br.readLine();
                    }
                    String everything = result.toString();
                } finally {
                    br.close();
                }*/
                if (result.toString().equals("")) {
                    System.out.println("Результатов не возвращает");
                    return null;
                } else {
                    Map<Integer, Action> pendingActions = new HashMap<>();

                    JsonParser parser = new JsonParser();
                    JsonObject mainObject = parser.parse(result.toString()).getAsJsonObject();

                    JsonArray allActions = mainObject.getAsJsonArray("results");
                    if (allActions.toString().equals("[]")) return new HashMap<>(); //если результатов нет
                    JsonObject metaDataObject = mainObject.getAsJsonObject("_meta");
                    int i = 0;
                    JsonArray thisActionPositions;
                    for (JsonElement action : allActions) {
                        JsonObject actionObject = action.getAsJsonObject();
                        JsonObject positions = actionObject.get("positions").getAsJsonArray().get(0).getAsJsonObject();
                        pendingActions.put(i,
                                new Action(
                                        jsonElementToDoubleSave(positions.get("amount")),
                                        jsonElementToDoubleSave(positions.get("payment")),
                                        getNullAsEmptyString(actionObject.get("subid")),
                                        getNullAsEmptyString(actionObject.get("action_date")),
                                        Long.parseLong(getNullAsEmptyString(actionObject.get("action_id"))),
                                        getNullAsEmptyString(actionObject.get("currency")),
                                        getNullAsEmptyString(actionObject.get("status_updated")),
                                        getNullAsEmptyString(actionObject.get("status")),
                                        jsonElementToIntSave(actionObject.get("paid"))
                                )
                        );
                        i++;
                    }
                    Map<ApiResponseParamsCLO, Map<Integer, Action>> actionsResponse = new HashMap<>();
                    actionsResponse.put(new ApiResponseParamsCLO(Integer.parseInt(metaDataObject.get("count").toString()),
                            Integer.parseInt(metaDataObject.get("limit").toString()),
                            Integer.parseInt(metaDataObject.get("offset").toString())), pendingActions);
                    return actionsResponse;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Дата null");
        return null;
    }

    public synchronized void parsePaidOutActions() {
        int limit = 10;
        int offset = 0;
        int count = 0;

        while (true) {
            Map<ApiResponseParamsCLO, Map<Integer, Action>> paidOutActions = getPaidOutActions("2018-02-15 00:00:00", limit, offset);
            System.out.println("Inspecting paidout actions");
            if (paidOutActions.size() > 0) {
                //над с этим чето делать
                for (Map.Entry<ApiResponseParamsCLO, Map<Integer, Action>> key : paidOutActions.entrySet()) {
                    count = key.getKey().getCount();
                    limit = key.getKey().getLimit();
                    offset = key.getKey().getOffset();
                    for (Map.Entry<Integer, Action> action : key.getValue().entrySet()) {
                        Action thisAction = action.getValue();
                        if (thisAction.getStatus().equals("approved") & thisAction.getPaidOut() == 1 & DBPg.getInstance().checkActionInDB(thisAction.getActionID())) {
                            String thisActionPaidOutInDb = DBPg.getInstance().getPaidOutStatusFromActionID(thisAction.getActionID());
                            thisActionPaidOutInDb = (thisActionPaidOutInDb != null) ? thisActionPaidOutInDb : "-1";
                            if (thisActionPaidOutInDb.equals("0")) {
                                Double userPersentFromDb = Double.parseDouble(DBPg.getInstance().getUserPersentFromActionID(thisAction.getActionID()));
                                Double cashbackInCurency = thisAction.getCashbackCash() * userPersentFromDb; //умножить на процент из базы;
                                Double cashbackInBtc = Double.parseDouble(new BTCOperator().convertToBTC(thisAction.getCurrency(), cashbackInCurency).toString());
                                new BTCCheck(
                                        Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(thisAction.getSubid())),
                                        cashbackInBtc, "cashback").sendToUserBalance();
                                ArrayList dinElms = new ArrayList();
                                dinElms.add(new BigDecimal(cashbackInBtc).setScale(8, BigDecimal.ROUND_HALF_UP));
                                new Message(
                                        Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(thisAction.getSubid())),
                                        "cashback",
                                        dinElms).send();
                                //DBPg.getInstance().putBTCCheckToDB(Long.parseLong(DBPg.getInstance().getChatIDFromDeeplinkSubID(thisAction.getSubid())), cashbackInBtc);
                                DBPg.getInstance().updatePaidOutStatusWhereActionId(thisAction.getActionID(), 1);
                            } else if (thisActionPaidOutInDb.equals("1")) {

                            } else {

                            }
//                            if (!dbMain.checkActionInDB(thisAction.getActionID()) & dbMain.checkSubidInDB(thisAction.getSubid())) {
//                                String chatID = dbMain.getChatIDFromDeeplinkSubID(thisAction.getSubid());
//                                if (chatID != null) {
//                                    tgBot.sendTextMessageToChatID(chatID, msgGen.getNotificationAboutPendingAction(thisAction.getFullCash(),
//                                            thisAction.getCashbackCash(), thisAction.getCurrency(), thisAction.getDateAction()));
//                                }
//                                dbMain.putActionToDB(thisAction.getActionID(), thisAction.getDateAction(),
//                                        thisAction.getStatusUpdateAction(), thisAction.getSubid(), mainSettings.setts.get("user_persent").getValue());
//                            } else {
//                            }
                        }
                    }
                }
                //END делать

                if ((limit + offset) >= count) {
                    break;
                } else {
                    offset = offset + limit;
                }
            } else {
                break;
            }

        }
    }

    public synchronized void parsePendingActions() {
        int limit = 10;
        int offset = 0;
        int count = 0;

        while (true) {
            Map<ApiResponseParamsCLO, Map<Integer, Action>> pendingActions = getPendingActions("2018-02-15 00:00:00", limit, offset);
            System.out.println("Inspecting pending actions");
            if (pendingActions.size() > 0) {

                //над с этим чето делать
                for (Map.Entry<ApiResponseParamsCLO, Map<Integer, Action>> key : pendingActions.entrySet()) {
                    count = key.getKey().getCount();
                    limit = key.getKey().getLimit();
                    offset = key.getKey().getOffset();
                    for (Map.Entry<Integer, Action> action : key.getValue().entrySet()) {
                        Action thisAction = action.getValue();
                        if (thisAction.getStatus().equals("pending")) {
                            if (!DBPg.getInstance().checkActionInDB(thisAction.getActionID()) & DBPg.getInstance().checkSubidInDB(thisAction.getSubid())) {
                                String chatID = DBPg.getInstance().getChatIDFromDeeplinkSubID(thisAction.getSubid());
                                Double cashbackInCurency = thisAction.getCashbackCash() * Setts.getInstance().getDbl("user_persent");
                                BigDecimal cashbackInBtc = new BTCOperator().convertToBTC(thisAction.getCurrency(), cashbackInCurency);
                                if (chatID != null) {
                                    tgBot.sendTextMessageToChatID(chatID,
                                            msgGen.getNotificationAboutPendingAction(thisAction.getFullCash(),
                                                    (cashbackInCurency), cashbackInBtc.toString(), thisAction.getCurrency(), thisAction.getDateAction()));
                                }
                                DBPg.getInstance().putActionToDB(thisAction.getActionID(),
                                        "pending",
                                        thisAction.getDateAction(),
                                        thisAction.getStatusUpdateAction(),
                                        thisAction.getSubid(),
                                        Setts.getInstance().getStr("user_persent"));
                            } else {

                            }
                        }
                    }
                }
                //END делать
                if ((limit + offset) >= count) {
                    break;
                } else {
                    offset = offset + limit;
                }
            }

        }
    }

    public synchronized void parseApprowedActions() {
        int limit = 10;
        int offset = 0;
        int count = 0;

        while (true) {
            Map<ApiResponseParamsCLO, Map<Integer, Action>> aprovedActions = getApprovedActions("2018-02-15 00:00:00", limit, offset);
            System.out.println("Inspecting approved actions");
            if (aprovedActions.size() > 0) {
                //над с этим чето делать
                for (Map.Entry<ApiResponseParamsCLO, Map<Integer, Action>> key : aprovedActions.entrySet()) {
                    count = key.getKey().getCount();
                    limit = key.getKey().getLimit();
                    offset = key.getKey().getOffset();
                    for (Map.Entry<Integer, Action> action : key.getValue().entrySet()) {
                        Action thisAction = action.getValue();
                        if (thisAction.getStatus().equals("approved") & DBPg.getInstance().checkSubidInDB(thisAction.getSubid())) {
                            if (DBPg.getInstance().checkActionInDB(thisAction.getActionID())) {
                                String thisActionInDBStatus = DBPg.getInstance().getStatusFromActionID(thisAction.getActionID());
                                if (thisActionInDBStatus.equals("approved")) {
                                    //если апрувед в базе и апрувед в адмитаде
                                    //на всякий случай вдруг потеряли
                                } else if (thisActionInDBStatus.equals("pending")) {
                                    //обновляем это действие в БД status: approved, payout:0
                                    DBPg.getInstance().updateStatusWhereActionId(thisAction.getActionID(), "approved", 0);
                                    //уведомляем пользователя что платеж подтвержден, деньги получите примерно
                                    tgBot.sendTextMessageToChatID(DBPg.getInstance().getChatIDFromDeeplinkSubID(thisAction.getSubid()), msgGen.getNotificationAboutApprovedAction(thisAction.getFullCash(),
                                            thisAction.getCashbackCash(), thisAction.getCurrency(), thisAction.getDateAction()));
                                } else {
                                    //если статус данного актион иа не пендинг и не апрув
                                }
                            } else {
                                String chatID = DBPg.getInstance().getChatIDFromDeeplinkSubID(thisAction.getSubid());
                                if (chatID != null) {
                                    DBPg.getInstance().putActionToDB(thisAction.getActionID(), "approved", thisAction.getDateAction(),
                                            thisAction.getStatusUpdateAction(), thisAction.getSubid(), Setts.getInstance().getStr("user_persent"));
                                    tgBot.sendTextMessageToChatID(chatID, msgGen.getNotificationAboutApprovedAction(thisAction.getFullCash(),
                                            thisAction.getCashbackCash(), thisAction.getCurrency(), thisAction.getDateAction()));
                                }

                            }
                        }
                    }
                }
                //END делать

                if ((limit + offset) >= count) {
                    break;
                } else {
                    offset = offset + limit;
                }
            } else {
                break;
            }

        }
    }

}
