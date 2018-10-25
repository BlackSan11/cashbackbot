package me.btcback.tlg;

import me.btcback.BotUser;
import me.btcback.Setts;
import me.btcback.admitad.models.DeepLink;
import me.btcback.finance.models.BTCCheck;
import me.btcback.pgdb.DBPg;
import me.btcback.tlg.models.DeferredMsg;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DeferredMsgsSenderThread extends Thread {

    public void run() {
        while (true) {
            parseWaitToSendDefMsgs();
            try {
                this.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //ищет новые месседжи для рассылки и отправляет
    public Boolean parseWaitToSendDefMsgs() {
        ArrayList<DeferredMsg> waitToSendDefMsgs = DBPg.getInstance().getDeferredMsgsForSend(); //получаем новые месседжи
        if (waitToSendDefMsgs.size() > 0) { //если есть меседжи для отправки
            for (DeferredMsg defMsg : waitToSendDefMsgs) { // по всем месседжам
                if (defMsg.getStatus().equals("wait_to_send")) { //если это обычная рассылка по всем юзверям
                    ArrayList<BotUser> botUsers = DBPg.getInstance().getAllBotUsers();
                    for (BotUser botUser : botUsers) { //оправляем всем пользователям месседжи
                        String linkForText;
                        String linkForButton;
                        String textForMsg;
                        if (defMsg.getInTextLink().contains("ns:")) { //если ссылка не на магазин дип не делаем
                            linkForText = defMsg.getInTextLink().replace("ns:", "");
                        } else {
                            DeepLink deepLink = new DeepLinkGen(defMsg.getInTextLink(), String.valueOf(botUser.getChatId()),"dispatch", defMsg.getSubidMarker()).getShortDeepLink();
                            if (deepLink == null) break; // если такого магазина у нас нет выходим из функции
                            linkForText = deepLink.getLink();
                        }
                        if (defMsg.getInButtonLink().contains("ns:")) { //если ссылка не на магазин дип не делаем
                            linkForButton = defMsg.getInButtonLink().replace("ns:", "");
                        } else {
                            DeepLink deepLink = new DeepLinkGen(defMsg.getInButtonLink(), String.valueOf(botUser.getChatId()),"dispatch", defMsg.getSubidMarker()).getShortDeepLink();
                            if (deepLink == null) return false; // если такого магазина у нас нет выходим из функции
                            linkForButton = deepLink.getLink();
                        }
                        linkForText = "[" + defMsg.getInTextAnkor() + "](" + linkForText + ")";
                        textForMsg = defMsg.getText().replace("{link}", linkForText);
                        ArrayList dynElmsForMsg = new ArrayList();
                        dynElmsForMsg.add(textForMsg);
                        dynElmsForMsg.add(defMsg.getBonusSum());
                        new Message(botUser.getChatId(), "deferred_bonus", dynElmsForMsg).send(
                                getKeyboard(defMsg.getId(),
                                linkForButton, defMsg.getInButtonAnkor()));
                    }
                    defMsg.setStatus("sended");
                    defMsg.changeStatusInDB();
                } else if (defMsg.getStatus().equals("testing")) {
                    String[] usersChatIds = defMsg.getTest_chat_ids().split(",");
                    for (String userChatId : usersChatIds) {
                        String linkForText;
                        String linkForButton;
                        String textForMsg;
                        if (defMsg.getInTextLink().contains("ns:")) { //если ссылка не на магазин дип не делаем
                            linkForText = defMsg.getInTextLink().replace("ns:", "");
                        } else {
                            DeepLink deepLink = new DeepLinkGen(defMsg.getInTextLink(), userChatId,"dispatch", defMsg.getSubidMarker()).getShortDeepLink();
                            if (deepLink == null) return false; // если такого магазина у нас нет выходим из функции
                            linkForText = deepLink.getLink();
                        }
                        if (defMsg.getInButtonLink().contains("ns:")) { //если ссылка не на магазин дип не делаем
                            linkForButton = defMsg.getInButtonLink().replace("ns:", "");
                        } else {
                            DeepLink deepLink = new DeepLinkGen(defMsg.getInButtonLink(), userChatId,"dispatch", defMsg.getSubidMarker()).getShortDeepLink();
                            if (deepLink == null) return false; // если такого магазина у нас нет выходим из функции
                            linkForButton = deepLink.getLink();
                        }
                        linkForText = "[" + defMsg.getInTextAnkor() + "](" + linkForText + ")";
                        textForMsg = defMsg.getText().replace("{link}", linkForText);
                        ArrayList dynElmsForMsg = new ArrayList();
                        dynElmsForMsg.add(textForMsg);
                        dynElmsForMsg.add(defMsg.getBonusSum());
                        new Message(Long.parseLong(userChatId), "deferred_bonus", dynElmsForMsg).send(getKeyboard(defMsg.getId(),linkForButton, defMsg.getInButtonAnkor()));
                    }
                    defMsg.setStatus("sended");
                    defMsg.changeStatusInDB();
                } else {

                }
            }
            return true;
        }
        return false;
    }

    private ReplyKeyboard getKeyboard(Long defMsgId, String buttonUrl, String buttonAnkore) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        ArrayList keyboard = new ArrayList<>();
        ArrayList row = new ArrayList();
        row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("get_bonus_from_deferred_msg")).setCallbackData("get_bdf_id=" + defMsgId +"&"+buttonAnkore+"!!!"+buttonUrl));
        row.add(new InlineKeyboardButton(buttonAnkore).setUrl(buttonUrl));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }


}
