package me.btcback.bonuses.models;

import me.btcback.BotUser;
import me.btcback.Setts;
import me.btcback.admitad.AdmitadCore;
import me.btcback.admitad.Advertiser;
import me.btcback.admitad.models.Order;
import me.btcback.admitad.models.Tarif;
import me.btcback.pgdb.DBPg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FirstBuyBonus extends Bonus {
    public final String nameBonus = "first_buy_bonus";
    private volatile BotUser botUser;
    final public Double sum = (Setts.getInstance().getDbl(this.nameBonus + "_sum") * 0.00000001);
    ArrayList<Long> userOrdersAdIds;
    public FirstBuyBonus(BotUser botUser) {
        this.botUser = botUser;
    }

    @Override
    public Boolean checkCanGetBonus() {
        userOrdersAdIds = DBPg.getInstance().getUserOrdersIds(botUser.getChatId());
        if (userOrdersAdIds != null && botUser.getTotalActions() == 0 & userOrdersAdIds.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void fixUserBonus(){
        botUser.setTotalActions((long) this.userOrdersAdIds.size());
        botUser.saveMyTotalActions();
    }

    public String getNameBonus() {
        return nameBonus;
    }

    public Double getSum() {
        return sum;
    }

    @Override
    public ArrayList<String> getAddInfo() {
        //Advertiser advertiser = AdmitadCore.getInstance().getClickedAdvertizer();
        List<Advertiser> searchedAdvertizer = AdmitadCore.getInstance().activeAdvertisersNew
                .stream()
                .filter(advertiser -> advertiser.getIdAdmitad() == userOrdersAdIds.get(userOrdersAdIds.size()-1))
                .collect(Collectors.toList());
        ArrayList<String> addInfo = new ArrayList();
        addInfo.add(searchedAdvertizer.get(0).getName());
        return addInfo;
    }

}
