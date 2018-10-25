package me.btcback.bonuses.models;

import me.btcback.BotUser;
import me.btcback.Setts;
import me.btcback.admitad.AdmitadCore;
import me.btcback.admitad.Advertiser;
import me.btcback.admitad.models.Tarif;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FirstClickFromListBonus extends Bonus {
    public final String nameBonus = "from_list_bonus";
    public volatile Double sum = (Setts.getInstance().getDbl(this.nameBonus + "_sum") * 0.00000001);
    private volatile BotUser botUser;
    final private String subid2 = "list";
    ArrayList<Long> clickedOrdersAdId;

    public FirstClickFromListBonus(BotUser botUser) {
        this.botUser = botUser;
    }

    @Override
    public Boolean checkCanGetBonus() {
        clickedOrdersAdId = botUser.getClickedAdIdsInShortner(subid2);
        if (clickedOrdersAdId != null && botUser.getJumpsFromList() == 0 & clickedOrdersAdId.size() > 0) {
            return true;
        }
        return false;
    }


    @Override
    public void fixUserBonus(){
        botUser.setJumpsFromList(this.clickedOrdersAdId.size());
        botUser.saveMyJamps();
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
                .filter(advertiser -> advertiser.getIdAdmitad() == clickedOrdersAdId.get(clickedOrdersAdId.size()-1))
                .collect(Collectors.toList());
        ArrayList<String> addInfo = new ArrayList();
        //StringBuffer stringBuffer = new StringBuffer();
        addInfo.add(searchedAdvertizer.get(0).getName());
        addInfo.add(searchedAdvertizer.get(0).getTarifs());
        return addInfo;
    }


}
