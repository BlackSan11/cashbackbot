package me.btcback.tlg.models;

import me.btcback.pgdb.DBPg;

public class DeferredMsg {
    private Long id;
    private String text;
    private String inTextLink;
    private String inButtonLink;
    private String inTextAnkor;
    private String inButtonAnkor;
    private String status;
    private String test_chat_ids;
    private String subidMarker;
    private Double bonusSum;

    public DeferredMsg(Long id, String text, String inTextLink, String inButtonLink, String inTextAnkor, String inButtonAnkor, String status, String test_chat_ids, Double bonusSum, String subidMarker) {
        this.id = id;
        this.text = text;
        this.inTextLink = inTextLink;
        this.inButtonLink = inButtonLink;
        this.inTextAnkor = inTextAnkor;
        this.inButtonAnkor = inButtonAnkor;
        this.status = status;
        this.test_chat_ids = test_chat_ids;
        this.bonusSum = bonusSum;
        this.subidMarker = subidMarker;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getInTextLink() {
        return inTextLink;
    }

    public String getInButtonLink() {
        return inButtonLink;
    }

    public String getInTextAnkor() {
        return inTextAnkor;
    }

    public String getInButtonAnkor() {
        return inButtonAnkor;
    }

    public String getStatus() {
        return status;
    }

    public String getTest_chat_ids() {
        return test_chat_ids;
    }

    public Double getBonusSum() {
        return bonusSum;
    }

    public String getSubidMarker() {
        return subidMarker;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void changeStatusInDB(){
        DBPg.getInstance().updateDeferredMsgStatus(getId(), getStatus());
    }
}
