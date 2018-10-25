package me.btcback.models;

public class Option {
    private volatile int Id;
    private volatile String value;
    private volatile String description;

    public Option( int Id, String value, String description) {
        this.Id = Id;
        this.value = value;
        this.description = description;
    }

    public int getId() {
        return Id;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}
