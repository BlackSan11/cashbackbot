package me.btcback.bonuses.models;

import me.btcback.BotUser;
import me.btcback.admitad.Advertiser;

import java.util.ArrayList;

public abstract class Bonus {
    private volatile BotUser botUser;
    public volatile Double sum;

    public void Bonus(BotUser botUser){
     this.botUser = botUser;
    }

    public abstract Boolean checkCanGetBonus();

    public abstract void fixUserBonus();

    public abstract String getNameBonus();

    public abstract Double getSum();

    public abstract ArrayList getAddInfo();
}
