package me.btcback.admitad.models;

public class Action {


    public double fullCash = 0;
    public double cashbackCash = 0;
    public String dateAction = "";
    public String statusUpdateAction = "";
    public String subid = "";
    public String currency = "";
    public long actionID = 0;
    public String status = "";
    public int paidOut;

    public Action(double fullCash, double cashbackCash, String subid, String dateAction,
                         long actionID, String currency, String statusUpdateAction, String status, int paidOut) {
        this.fullCash = fullCash;
        this.cashbackCash = cashbackCash;
        this.subid = subid;
        this.dateAction = dateAction;
        this.actionID = actionID;
        this.currency = currency;
        this.statusUpdateAction = statusUpdateAction;
        this.status = status;
        this.paidOut = paidOut;
    }

    public double getFullCash() {
        return fullCash;
    }

    public double getCashbackCash() {
        return cashbackCash;
    }

    public String getSubid() {
        return subid;
    }

    public String getDateAction() {
        return dateAction;
    }

    public long getActionID() {
        return actionID;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatusUpdateAction() {
        return statusUpdateAction;
    }

    public String getStatus() {
        return status;
    }

    public int getPaidOut() {
        return paidOut;
    }
}
