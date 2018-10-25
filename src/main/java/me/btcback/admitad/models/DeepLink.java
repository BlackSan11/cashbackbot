package me.btcback.admitad.models;

import java.util.ArrayList;

public class DeepLink {
    private volatile String advName;
    private volatile String link;
    private volatile String tarifs;
    private volatile String type;
    private volatile Long advId;
    private volatile String shortLink;

    public DeepLink(String advName, String link, String tarifs, String type) {
        this.advName = advName.replace(".", "");
        this.link = link;
        this.tarifs = tarifs;
        this.type = type;
    }

    public DeepLink(Long advId, String shortLink) {
        this.advId = advId;
        this.shortLink = shortLink;
    }

    public synchronized String getAdvName() {
        return advName;
    }

    public synchronized String getLink() {
        return link;
    }

    public synchronized String getTarifsString() {
        return tarifs;
    }

    public String getType() {
        return type;
    }

    public Long getAdvId() {
        return advId;
    }

    public String getShortLink() {
        return shortLink;
    }

    public void setAdvName(String advName) {
        this.advName = advName;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
