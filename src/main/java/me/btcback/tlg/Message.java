package me.btcback.tlg;

import me.btcback.Setts;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class Message extends TelegramLongPollingBot {

    private volatile Long chatId;
    private volatile Integer messageId;
    private volatile String tplPrefix;
    private volatile String message;
    private volatile ArrayList dinamicElements;

    public Message(Long chatId, String tplPrefix, ArrayList dinamicElements) {
        this.chatId = chatId;
        this.tplPrefix = tplPrefix;
        this.dinamicElements = dinamicElements;
    }

    public Message(Long chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }
    public Message(Long chatId) {
        this.chatId = chatId;
    }

    /**
     * Using for delite message
     *
     * @param chatId
     * @param messageId
     */
    public Message(Long chatId, Integer messageId) {
        this.chatId = chatId;
        this.messageId = messageId;
    }

    public synchronized String generateFromTpl() {
        String msgTpl = Setts.getInstance().getMsgTpl(this.tplPrefix + "_msg");
        if (this.dinamicElements.size() > 0) {
            for (int i = 0; i < this.dinamicElements.size(); i++) {
                String repStr = "{" + (i + 1) + "}";
                msgTpl = msgTpl.replace(repStr, String.valueOf(this.dinamicElements.get(i)));
            }
        }
        return msgTpl;
    }

    /*
     * Отправляет сообщению пользователю
     * если текст сообщения не задан через конструктор
     * берет шаблон
     * @param o - нет
     * @author Bogdan
     * */
    public synchronized void send() {
        String text = this.message != null ? this.message : generateFromTpl();
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(this.chatId)
                .disableWebPagePreview()
                .setText(text)
                .setParseMode(ParseMode.MARKDOWN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardTwoRow = new KeyboardRow();
        keyboardFirstRow.add(Setts.getInstance().getBtn("list_show_btn"));
        keyboardTwoRow.add(Setts.getInstance().getBtn("show_balance_btn"));
        keyboardTwoRow.add(Setts.getInstance().getBtn("show_orders_history_btn"));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardTwoRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            message.setParseMode(null);
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException ee) {
                System.out.println("Problem with send message. Markdown?");
                ee.printStackTrace();
            }
        }
    }

    public synchronized void sendWithLink() {
        String text = this.message != null ? this.message : generateFromTpl();
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(this.chatId)
                .setText(text)
                .setParseMode(ParseMode.MARKDOWN);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        KeyboardRow keyboardTwoRow = new KeyboardRow();
        keyboardFirstRow.add(Setts.getInstance().getBtn("list_show_btn"));
        keyboardTwoRow.add(Setts.getInstance().getBtn("show_balance_btn"));
        keyboardTwoRow.add(Setts.getInstance().getBtn("show_orders_history_btn"));
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardTwoRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            message.setParseMode(null);
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException ee) {
                System.out.println("Problem with send message. Markdown?");
                ee.printStackTrace();
            }
        }
    }

    public synchronized Boolean del() {
        DeleteMessage deleteMessage = new DeleteMessage()
                .setChatId(String.valueOf(this.chatId))
                .setMessageId(this.messageId);
        try {
            Boolean executeFlag = execute(deleteMessage); // Call method to send the message
            System.out.println(executeFlag);
            return executeFlag;
        } catch (TelegramApiException e) {
            e.printStackTrace();
            return true;
            /*System.out.println("Problem with send message. Markdown?");*/

        }
    }

    public synchronized void send(ReplyKeyboard keyboard) {
        String text = this.message != null ? this.message : generateFromTpl();

        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(this.chatId)
                .disableWebPagePreview()
                .setReplyMarkup(keyboard)
                .setText(text)
                .setParseMode(ParseMode.MARKDOWN);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            message.setParseMode(null);
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException ee) {
                System.out.println("Problem with send message. Markdown?");
                ee.printStackTrace();
            }
        }
    }

    public synchronized String getUsername(){
        GetChat getChat = new GetChat();
        getChat.setChatId(this.chatId);
        try {
            String username = execute(getChat).getUserName(); // Call method to send the message
            if(username == null){
                username = "not set";
            } else{
                username = "@" + username;
            }
            return username;
        } catch (TelegramApiException ee) {
            System.out.println("Problem with send message. Markdown?");
            ee.printStackTrace();
            return "er";
        }
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return Setts.getInstance().getStr("bot_username");
    }

    @Override
    public String getBotToken() {
        return Setts.getInstance().getStr("bot_token");
    }
}
