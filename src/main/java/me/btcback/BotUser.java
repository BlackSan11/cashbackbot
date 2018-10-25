package me.btcback;

import me.btcback.admitad.AdmitadCore;
import me.btcback.pgdb.DBPg;

import java.util.ArrayList;

public class BotUser {
    public Long id;
    public Long chatId;
    public Double btcBalance;
    public String regDateTime;
    public int approvedActions;
    public int jumpsFromLink;
    public int jumpsFromList;
    public Long totalActions;


    public BotUser(Long id, Double btcBalance, String regDateTime, int approvedActions, int jumpsFromLink, int jumpsFromList, Long totalActions, Long chatId) {
        this.id = id;
        this.btcBalance = btcBalance;
        this.regDateTime = regDateTime;
        this.approvedActions = approvedActions;
        this.jumpsFromLink = jumpsFromLink;
        this.jumpsFromList = jumpsFromList;
        this.totalActions = totalActions;
        this.chatId = chatId;
    }

    public Long getId() {
        return id;
    }

    public Double getBtcBalance() {
        return btcBalance;
    }

    public String getRegDatetime() {
        return regDateTime;
    }

    public int getApprovedActions() {
        return approvedActions;
    }

    public int getJumpsFromLink() {
        return jumpsFromLink;
    }

    public int getJumpsFromList() {
        return jumpsFromList;
    }

    public Long getTotalActions() {
        return totalActions;
    }

    public Long getChatId() {
        return chatId;
    }


    /*public Integer getClickCountInAdmitad(String from) {
        String[] tempParams = {String.valueOf(getChatId()), from};
        Integer clickCount = AdmitadCore.INSTANCE.getChatIdClicksCount(tempParams);
        return clickCount;
    }*/

    public ArrayList<Long> getClickedAdIdsInShortner(String from) {
        return AdmitadCore.getInstance().getClickedAdIds(this.chatId, from);
    }

    public Long getActionsTotalInAdmitad() {
        Long clickCount = AdmitadCore.getInstance().getChatIdBuyActionsCount(this.chatId);
        return clickCount;
    }

    public void setJumpsFromLink(int jumpsFromLink) {
        this.jumpsFromLink = jumpsFromLink;
    }

    public void setJumpsFromList(int jumpsFromList) {
        this.jumpsFromList = jumpsFromList;
    }

    public void setTotalActions(Long totalActions) {
        this.totalActions = totalActions;
    }

    public void saveMyJamps() {
        DBPg.getInstance().updateUserJumpsWhereId(this.id, this.jumpsFromLink, this.jumpsFromList);
    }

    public void saveMyTotalActions() {
        DBPg.getInstance().updateUserTotalActionsWhereId(this.id, this.totalActions);
    }
}
