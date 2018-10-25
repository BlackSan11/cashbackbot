/*
package me.btcback.tlg;

import me.btcback.Setts;
import me.btcback.admitad.AdmitadCore;
import me.btcback.admitad.Advertiser;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NavPage {

    private Long chatId;
    private String type;

    public NavCatalog(Long chatId) {
        this.chatId = chatId;
    }

    public synchronized void showAllShops(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        ss
        int countOfAdvertisers = AdmitadCore.INSTANCE.activeAdvertisers.size();
        if (countOfAdvertisers >= 1) {
            int rowsCount = countOfAdvertisers / 2;
            if ((countOfAdvertisers % 2) != 0) {
                rowsCount = rowsCount + 1;
            }
            List<KeyboardRow> keyboard = new ArrayList<>();
            KeyboardRow keyboardRow = new KeyboardRow();
            int counterNow = 1;
            for (Map.Entry<Integer, Advertiser> entry : AdmitadCore.INSTANCE.activeAdvertisers.entrySet()) {
                if ((counterNow % 2) != 0) {
                    keyboardRow.add(Setts.INSTANCE.getStr("emoji_start_list") + entry.getValue().getName());
                    if ((entry.getKey() + 1) == countOfAdvertisers) keyboard.add(keyboardRow);
                } else {
                    keyboardRow.add(Setts.INSTANCE.getStr("emoji_start_list") + entry.getValue().getName());
                    keyboard.add(keyboardRow);
                    keyboardRow = new KeyboardRow();
                }
                counterNow++;
            }
            KeyboardRow keyboardControl = new KeyboardRow();
            keyboardControl.add("↩Назад");
            keyboardControl.add("➡Вперед");
            keyboardControl.add("❌Скрыть");
            keyboard.add(keyboardControl);
            replyKeyboardMarkup.setKeyboard(keyboard);
            sendMessage.setChatId(message.getChatId().toString());
            sendMessage.setText("Выберите магазин");
            sendMessage.disableWebPagePreview();
            try {
                sendMessage(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("123");
        }
    }



}
*/
