package me.btcback.admitad.models;

public class Tarif {
    public String actName;
    public String price;

    public Tarif(String actName, String price) {
        this.actName = actName.toLowerCase();
        this.price = price;
    }

    public String getActName() {
        return actName;
    }

    public String getPrice() {
        return price;
    }

    public void setActName(String actName) {
        this.actName = actName;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
