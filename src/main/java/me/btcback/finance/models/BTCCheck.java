package me.btcback.finance.models;

import me.btcback.Setts;
import me.btcback.pgdb.DBPg;
import me.btcback.tlg.Message;

import java.math.BigDecimal;
import java.util.ArrayList;

public class BTCCheck {
    final public static String STATUS_WAIT_TO_CREATE = "wait_to_create";
    final public static String STATUS_SENDED = "sended";
    final public static String STATUS_WAIT_TO_SEND = "wait_to_send";
    private Long id;
    private String link;
    private String type;
    private String status;
    private Long recipient;
    private Double sum;
    private String recipienUsername;

    public BTCCheck() {

    }

    public BTCCheck(Long recipient, Double sum, String type) {
        this.recipient = recipient;
        this.sum = sum;
        this.type = type;
    }

    public BTCCheck(Long recipient, Double sum) {
        this.recipient = recipient;
        this.sum = sum;
    }

    public BTCCheck(Long recipient, Double sum, String type, String username) {
        this.recipient = recipient;
        this.sum = sum;
        this.type = type;
        this.recipienUsername = username;
    }

    public BTCCheck(String type, String status, Long recipient, Double sum) {
        this.type = type;
        this.status = status;
        this.recipient = recipient;
        this.sum = sum;
    }


    public BTCCheck(Long id, String link, String type, String status, Long recipient, Double sum) {
        this.id = id;
        this.link = link.replace("_", "\\_");
        this.type = type;
        this.status = status;
        this.recipient = recipient;
        this.sum = sum;
    }

    public Long getId() {
        return id;
    }

    public String getLink() {
        return link;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public Long getRecipient() {
        return recipient;
    }

    public Double getSum() {
        return sum;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRecipient(Long recipient) {
        this.recipient = recipient;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    //создание заявки на выплату чека
    private synchronized void createOrderOncreateCheck() {
        DBPg.getInstance().createBtcCheck("", this.type, STATUS_WAIT_TO_CREATE, this.recipient, BigDecimal.valueOf(this.sum).setScale(8, BigDecimal.ROUND_HALF_UP).toString(), "@" + this.recipienUsername);
    }

    //дерьмище какоето
    private Object notNullArg(Object arg) {
        return arg == null ? "" : arg;
    }

    //отправка чека в службу доставки
    public synchronized void addCheckToDelivery() {
        //BTCCheck bounusCheck = DBPg.getInstance().getBonusBtcCheck(this.type, this.sum);
        // if (bounusCheck.isEmpty()) {
        createOrderOncreateCheck();
        //} else {
        //    Boolean updateCheck = DBPg.getInstance().updateBtcCheckStatus(bounusCheck.getId(), STATUS_WAIT_TO_SEND, this.recipient);
        //}
    }

    public synchronized void sendToUserBalance() {
        if (DBPg.getInstance().updateUserBalanceWhereChatId(this.recipient, this.sum)) {
        }
    }

    /**
     * сохраняет его текущий статус в БД
     */
    public boolean saveStatus() {
        return DBPg.getInstance().updateBtcCheckStatusWhereCheckId(this.getId(), this.status);
    }

    //проверяет пуст ли чек
    boolean isEmpty() {
        if (this.id == null & this.link == null & this.type == null & this.status == null & this.recipient == null & this.sum == null) {
            return true;
        }
        return false;
    }

}
