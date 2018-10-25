package me.btcback.admitad.models;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Order {
    private long dbId;
    private long offerId;
    private double sumTotal;
    private String status;
    private String currency;
    private Date datetime;
    private Date processingDateTime;
    private String subid;
    private String subid1;
    private String subid2;
    private String subid3;
    private String subid4;
    private String tlgUsername;
    private Date statusUpdatedDateTime;
    private int processed;
    private int paid;
    private String offerOrderId;
    private long actionId;
    private SimpleDateFormat respDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat respDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private String offerName;
    private Double userPersent;
    private Double payment;
    private Double payment_btc;
    private Date closing_date;


    public Order(long offerId, double sumTotal, String status, String currency, String datetime, String processingDateTime, String subid, String subid1, String subid2, String subid3, String subid4, String statusUpdatedDateTime, int processed, int paid, String offerOrderId, long actionId, String offerName, double userPersent, double payment, double payment_btc, String closing_date, String tlgUsername
    ) {
        this.offerId = offerId;
        this.sumTotal = sumTotal;
        this.status = status;
        this.currency = currency;
        if (datetime != null & !datetime.equals("")) {
            try {
                this.datetime = respDateTimeFormat.parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (processingDateTime != null & !processingDateTime.equals("")) {
            try {
                this.processingDateTime = respDateFormat.parse(processingDateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.subid = subid;
        this.subid1 = subid1;
        this.subid2 = subid2;
        this.subid3 = subid3;
        this.subid4 = subid4;
        if (statusUpdatedDateTime != null & !statusUpdatedDateTime.equals("")) {
            try {
                this.statusUpdatedDateTime = respDateTimeFormat.parse(statusUpdatedDateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.processed = processed;
        this.paid = paid;
        this.offerOrderId = offerOrderId;
        this.actionId = actionId;
        this.offerName = offerName;
        this.userPersent = userPersent;
        this.payment = payment;
        this.payment_btc= payment_btc;
        if (closing_date != null & !closing_date.equals("")) {
            try {
                this.closing_date = respDateFormat.parse(closing_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.tlgUsername = tlgUsername;
    }

    public Order(long dbId, long offerId, double sumTotal, String status, String currency, String datetime, String processingDateTime, String subid, String subid1, String subid2, String subid3, String subid4, String statusUpdatedDateTime, int processed, int paid, String offerOrderId, long actionId, String offerName, double userPersent, double payment, double payment_btc, String closing_date, String tlgUsername
    ) {
        this.dbId = dbId;
        this.offerId = offerId;
        this.sumTotal = sumTotal;
        this.status = status;
        this.currency = currency;
        if (datetime != null & !datetime.equals("")) {
            try {
                this.datetime = respDateTimeFormat.parse(datetime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (processingDateTime != null & !processingDateTime.equals("")) {
            try {
                this.processingDateTime = respDateFormat.parse(processingDateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.subid = subid;
        this.subid1 = subid1;
        this.subid2 = subid2;
        this.subid3 = subid3;
        this.subid4 = subid4;
        if (statusUpdatedDateTime != null & !statusUpdatedDateTime.equals("")) {
            try {
                this.statusUpdatedDateTime = respDateTimeFormat.parse(statusUpdatedDateTime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.processed = processed;
        this.paid = paid;
        this.offerOrderId = offerOrderId;
        this.actionId = actionId;
        this.offerName = offerName;
        this.userPersent = userPersent;
        this.payment = payment;
        this.payment_btc= payment_btc;
        if (closing_date != null & !closing_date.equals("")) {
            try {
                this.closing_date = respDateFormat.parse(closing_date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        this.tlgUsername = tlgUsername;
    }

    public long getDbId() {
        return dbId;
    }

    public long getOfferId() {
        return offerId;
    }

    public double getSumTotal() {
        return sumTotal;
    }

    public String getStatus() {
        return status;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDatetime() {
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return formatForDateNow.format(datetime);
    }

    public Date getDatetimeDate() {
        return this.datetime;
    }

    public String getProcessingDateTime() {
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return (processingDateTime == null ? "" : formatForDateNow.format(processingDateTime));
    }

    public String getSubid() {
        return subid;
    }

    public String getSubid1() {
        return subid1;
    }

    public String getSubid2() {
        return subid2;
    }

    public String getSubid3() {
        return subid3;
    }

    public String getSubid4() {
        return subid4;
    }

    public String getTlgUsername() {
        return tlgUsername;
    }

    public String getStatusUpdatedDateTime() {
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return formatForDateNow.format(statusUpdatedDateTime);
    }

    public int getProcessed() {
        return processed;
    }

    public int getPaid() {
        return paid;
    }

    public String getOfferOrderId() {
        return offerOrderId;
    }

    public long getActionId() {
        return actionId;
    }
    public String getOfferName(){
        return offerName;
    }

    public Double getUserPersent() {
        return userPersent;
    }

    public Double getPayment() {
        return payment;
    }

    public Double getPaymentBtc() {
        return payment_btc;
    }

    public String getClosingDate() {
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd");
        return (closing_date == null ? "" : formatForDateNow.format(closing_date));
    }

    public void setDbId(int id){
        this.dbId = id;
    }
}
