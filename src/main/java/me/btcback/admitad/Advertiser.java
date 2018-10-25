package me.btcback.admitad;

import com.sun.org.apache.xpath.internal.SourceTree;
import me.btcback.Setts;
import me.btcback.admitad.models.Tarif;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Advertiser{

    public Long dbId;
    private long idAdmitad;
    private String nameAdmitad;
    private String tarifsAdmitad;
    private String nameCustom;
    public String link;
    public String refLink;
    public String tarifs;
    public String tarifsCustom;
    private Long useAmount;
    private Integer status;

    public Advertiser(Long dbId, Long idAdmitad, String nameAdmitad, String tarifsAdmitadIn, String nameCustom, String tarifsCustom, Long useAmount, Integer status, String link, String refLink) {
        this.dbId = dbId;
        this.idAdmitad = idAdmitad;
        this.nameAdmitad = nameAdmitad;
        this.tarifsAdmitad = tarifsAdmitadIn != null && !tarifsAdmitadIn.equals("") ? enterUserPersent(tarifsAdmitadIn) : "";
        this.nameCustom = nameCustom;
        this.tarifsCustom = tarifsCustom != null && !tarifsCustom.equals("") ? enterUserPersent(tarifsCustom) : "";
        this.useAmount = useAmount;
        this.status = status;
        this.link = link;
        this.refLink = refLink;
    }

    public Advertiser(Long idAdmitad, String nameAdmitad, String tarifsAdmitadIn, String link, String refLink) {
        this.idAdmitad = idAdmitad;
        this.nameAdmitad = nameAdmitad;
        this.tarifsAdmitad = tarifsAdmitadIn != null && !tarifsAdmitadIn.equals("") ? enterUserPersent(tarifsAdmitadIn) : "";
        this.link = link;
        this.refLink = refLink;
    }

    /*public Advertiser(String id, String name, String link, String refLink, String tarifs) {

        this.link = link;
        this.refLink = refLink;
        this.tarifs = tarifs;
    }*/

    public Long getDbId() {
        return dbId;
    }

    public long getIdAdmitad() {
        return idAdmitad;
    }

    public String getNameAdmitad() {
        return nameAdmitad;
    }

    public String getTarifsAdmitad() {
        return tarifsAdmitad;
    }

    public String getNameCustom() {
        return nameCustom;
    }

    public String getLink() {
        return link;
    }

    public String getRefLink() {
        return refLink;
    }

    public String getTarifs() {
        if(tarifsCustom != null && !tarifsCustom.equals("")){
            return tarifsCustom;
        } else {
            return tarifsAdmitad;
        }
    }

    public String getTarifsCustom() {
        return tarifsCustom;
    }

    public Long getUseAmount() {
        return useAmount;
    }

    public String getName(){
        if(nameCustom != null && !nameCustom.equals("")){
            return nameCustom;
        } else {
            return nameAdmitad;
        }
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    private synchronized String enterUserPersent(String paymentSize) {
        Pattern pattern = Pattern.compile("(\\d+[\\.]\\d+)|(\\d+)");
        Matcher matcher = pattern.matcher(paymentSize);
        StringBuffer resultTarifs = new StringBuffer(paymentSize);
        int prevLength = 0;
        String agrVal;
        while (matcher.find()) {
            agrVal = String.valueOf(Double.parseDouble(matcher.group())*Setts.getInstance().getDbl("user_persent"));
            resultTarifs.replace(matcher.start() + prevLength, matcher.end()+ prevLength,agrVal);
            if(agrVal.length() > (matcher.end() - matcher.start())){
                prevLength +=agrVal.length() - (matcher.end() - matcher.start());
            } else if(agrVal.length() < (matcher.end() - matcher.start())){
                prevLength -= (matcher.end() - matcher.start()) - agrVal.length();
            }
        }
        return resultTarifs.toString();
    }

    public void setIdAdmitad(Long idAdmitad) {
        this.idAdmitad = idAdmitad;
    }

    public void setNameAdmitad(String nameAdmitad) {
        this.nameAdmitad = nameAdmitad;
    }

    public void setTarifsAdmitad(String tarifsAdmitadIn) {
        this.tarifsAdmitad = tarifsAdmitadIn != null && !tarifsAdmitadIn.equals("") ? enterUserPersent(tarifsAdmitadIn) : "";
    }

    public void setNameCustom(String nameCustom) {
        this.nameCustom = nameCustom;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setRefLink(String refLink) {
        this.refLink = refLink;
    }

    public void setTarifs(String tarifs) {
        this.tarifs = tarifs;
    }

    public void setTarifsCustom(String tarifsCustom) {
        this.tarifsCustom = tarifsCustom;
    }

    public void setUseAmount(Long useAmount) {
        this.useAmount = useAmount;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

}
