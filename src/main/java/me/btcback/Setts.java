package me.btcback;

import me.btcback.models.Option;
import me.btcback.pgdb.DBPg;

import java.util.Map;

public class Setts {

    private static final Setts INSTANCE = new Setts();
    private volatile Map<String, Option> setts;
    private volatile Map<String, String> msgTpls;
    private volatile Map<String, String> btns;

    public Setts() {
        this.setts = DBPg.getInstance().getMainSettings();
        this.msgTpls = DBPg.getInstance().getTextTemplates();
        this.btns = DBPg.getInstance().getBtns();
        System.out.println("Settings inited..");
    }

    public void updateSettings(){
        this.setts = DBPg.getInstance().getMainSettings();
        this.msgTpls = DBPg.getInstance().getTextTemplates();
    }

    public synchronized String getMsgTpl(String key){
        return String.valueOf(msgTpls.get(key));
    }

    public synchronized String getBtn(String key){
        return String.valueOf(btns.get(key));
    }

    public synchronized Integer getInt(String key){
        return Integer.parseInt(setts.get(key).getValue());
    }

    public synchronized Double getDbl(String key){
        return Double.parseDouble(setts.get(key).getValue());
    }

    public synchronized String getStr(String key){
        return String.valueOf(setts.get(key).getValue());
    }

    public static Setts getInstance() {
        return INSTANCE;
    }
}
