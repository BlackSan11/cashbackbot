package me.btcback.bonuses;

import me.btcback.BotUser;
import me.btcback.Setts;
import me.btcback.bonuses.models.FirstBuyBonus;
import me.btcback.bonuses.models.FirstClickFromLinkBonus;
import me.btcback.bonuses.models.FirstClickFromListBonus;
import me.btcback.finance.models.BTCCheck;
import me.btcback.bonuses.models.Bonus;
import me.btcback.pgdb.DBPg;
import me.btcback.tlg.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BonusInspector extends Thread {
    private String lastInspectingDateTime;
    private String newInspectingDateTime = "";


    public BonusInspector() {
        this.lastInspectingDateTime = getDateTime();
    }

    @Override
    public void run() {
        System.out.println("Bonus inspector started..");
        while (true) {
            parseActiveUsers();
            try {
                this.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseActiveUsers() {
        this.newInspectingDateTime = getDateTime();
        //берем юзверей от даты и по столбцам
        ArrayList<BotUser> potentialGetBonusUsers = DBPg.getInstance().getPotentialBonusUser("2018-03-16 03:53:11");
        //проверяем бонусы
        if (potentialGetBonusUsers.size() > 0) {
            for (BotUser potentialGetBonusUser : potentialGetBonusUsers) {
                checkActiveBonuses(potentialGetBonusUser);
            }
        }
        this.lastInspectingDateTime = this.newInspectingDateTime;
    }

    private String getDateTime() {
        Date dateNow = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formatedDateTime = formatForDateNow.format(dateNow);
        return formatedDateTime;
    }

    private void checkActiveBonuses(BotUser botUser) {
        ArrayList<Bonus> bonuses = new ArrayList<>();
        bonuses.add(new FirstClickFromLinkBonus(botUser));
        bonuses.add(new FirstClickFromListBonus(botUser));
        bonuses.add(new FirstBuyBonus(botUser));
        //если бонус имеет место быть
        for (Bonus bonus : bonuses) {
            if (bonus.checkCanGetBonus()) {
                bonus.fixUserBonus();
                new BTCCheck(botUser.getChatId(), bonus.getSum(), bonus.getNameBonus()).sendToUserBalance();
                ArrayList dinElms = bonus.getAddInfo();
                dinElms.add(new BigDecimal(bonus.getSum() / 0.00000001).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                if(bonus.getNameBonus().equals("first_buy_bonus")){
                    ArrayList keyboard = new ArrayList();
                    ArrayList row = new ArrayList();
                    ArrayList row2 = new ArrayList();
                    row2.add(new InlineKeyboardButton(Setts.getInstance().getBtn("earnings_3step_btn")).setCallbackData("invite_frends_btn"));
                    keyboard.add(row);
                    keyboard.add(row2);
                    InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup().setKeyboard(keyboard);
                    new Message(botUser.chatId, bonus.getNameBonus(), dinElms).send(inlineKeyboard);
                } else if(bonus.getNameBonus().equals("from_list_bonus")){

                    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                    ArrayList keyboard = new ArrayList<>();
                    ArrayList row = new ArrayList();
                    row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("pre_from_link_btn").replace("{1}", String.valueOf(Setts.getInstance().getInt("from_link_bonus_sum")))).setCallbackData("pre_from_link_btn"));

                    keyboard.add(row);
                    keyboardMarkup.setKeyboard(keyboard);

                    new Message(botUser.chatId, bonus.getNameBonus(), dinElms).send(keyboardMarkup);
                } else if(bonus.getNameBonus().equals("from_link_bonus")){

                    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                    ArrayList keyboard = new ArrayList<>();
                    ArrayList row = new ArrayList();
                    row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("pre_first_buy_btn").replace("{1}", String.valueOf(Setts.getInstance().getInt("first_buy_bonus_sum")))).setCallbackData("pre_first_buy_btn"));

                    keyboard.add(row);
                    keyboardMarkup.setKeyboard(keyboard);

                    new Message(botUser.chatId, bonus.getNameBonus(), dinElms).send(keyboardMarkup);
                }
                else{
                    new Message(botUser.chatId, bonus.getNameBonus(), dinElms).send();
                }

            }
        }
    }
}
