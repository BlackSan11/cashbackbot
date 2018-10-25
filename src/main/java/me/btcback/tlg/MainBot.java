package me.btcback.tlg;

import me.btcback.BotUser;
import me.btcback.Setts;
import me.btcback.admitad.AdmitadCore;
import me.btcback.admitad.Advertiser;
import me.btcback.admitad.models.DeepLink;
import me.btcback.admitad.models.Order;
import me.btcback.finance.models.BTCCheck;
import me.btcback.pgdb.DBPg;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainBot extends TelegramLongPollingBot {

    MessegesGenerator msgGen;
    String text;

    public MainBot(MessegesGenerator msgGen) {
        this.msgGen = msgGen;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            DBPg.getInstance().updateLastActivity(update.getMessage().getChatId()); //обновляем дату последней активности пользователя
            text = update.getMessage().getText();
            if (text.contains("/start")) {
                String refFromChatId = "";
                initKeyboard(update.getMessage());
                //new me.btcback.tlg.Message(update.getMessage().getChatId(), "start", new ArrayList()).send();
                if (!DBPg.getInstance().checkUserInDb(update.getMessage().getChatId().toString())) {
                    //new me.btcback.tlg.Message(update.getMessage().getChatId(),update.getMessage().getText()).send();
                    if(text.length() > 6 && DBPg.getInstance().checkRefHash(text.substring(text.indexOf(" ") + 1))){
                        String refFromRefHash = text.substring(text.indexOf(" ") + 1);
                        if(refFromRefHash != null){
                            refFromChatId = DBPg.getInstance().getChatIdFromComingRef(refFromRefHash);
                            ArrayList dinElms2 = new ArrayList();
                            dinElms2.add(Setts.getInstance().getDbl("from_refer_bonus_sum"));
                            new me.btcback.tlg.Message(Long.parseLong(refFromChatId), "bonus_from_refer", dinElms2).send();
                            new BTCCheck(Long.parseLong(refFromChatId), (Setts.getInstance().getDbl("from_refer_bonus_sum") * 0.00000001)).sendToUserBalance();
                        }
                    }
                    DBPg.getInstance().regNewUser(update.getMessage().getChatId(), "@" + update.getMessage().getFrom().getUserName(),refFromChatId);
                    //TODO: добавить в базу users поле username
                    BTCCheck bonusCheck = new BTCCheck(update.getMessage().getChatId(), (Setts.getInstance().getDbl("start_bonus_sum") * 0.00000001), "start_bonus");
                    bonusCheck.sendToUserBalance();
                    ArrayList dinElms = new ArrayList();
                    dinElms.add(bonusCheck.getSum() / 0.00000001);

                    InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                    ArrayList keyboard = new ArrayList<>();
                    ArrayList row = new ArrayList();
                    row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("pre_from_list_btn").replace("{1}", String.valueOf(Setts.getInstance().getInt("from_list_bonus_sum")))).setCallbackData("pre_from_list_btn"));

                    keyboard.add(row);
                    keyboardMarkup.setKeyboard(keyboard);

                    new me.btcback.tlg.Message(update.getMessage().getChatId(), "start_bonus", dinElms).send(keyboardMarkup);
                }
            }
            //ОТКРЫТЬ КАТАЛОГ
            else if (text.equals(Setts.getInstance().getBtn("list_show_btn"))) {
                showShopsCatalog(update.getMessage().getChatId(), 1);
            }
            //ИНФОРМАЦИЯ ПО БАЛАНСУ
            else if (text.equals(Setts.getInstance().getBtn("show_balance_btn"))) {
                showBalance(update.getMessage().getChatId());
            }
            //ИНФОРМАЦИЯ О БОНУСАХ
            else if (text.equals(Setts.getInstance().getBtn("bonus_info_btn"))) {
                ArrayList msgDinParams = new ArrayList();
                msgDinParams.add(Setts.getInstance().getStr("from_link_bonus_sum"));
                msgDinParams.add(Setts.getInstance().getStr("from_list_bonus_sum"));
                msgDinParams.add(Setts.getInstance().getStr("first_buy_bonus_sum"));
                new me.btcback.tlg.Message(update.getMessage().getChatId(), "about_bonuses", msgDinParams).send();
            }
            else if(text.equals("1996reeNN")){
                ArrayList<BotUser> allUsers = DBPg.getInstance().getAllBotUsers();
                for (BotUser allUser : allUsers) {
                    DBPg.getInstance().updateRefHash(allUser.getChatId());
                }
            }
            //КНОПКА СКРЫТЬ
            else if (text.equals(Setts.getInstance().getBtn("to_home_btn"))) {
                initKeyboard(update.getMessage());
            }
            //КНОПКА ИСТОРИЯ ЗАКАЗОВ
            else if (text.equals(Setts.getInstance().getBtn("show_orders_history_btn"))) {
                showShopsHistory(update.getMessage().getChatId(), Long.parseLong("0"), Long.parseLong("0"));
            }
            //ИЛИ ЛИНК ИЛИ ХЕРНЯ
            else {
                if (msgGen.ifURL(update.getMessage().getText())) {
                    DeepLink deeplink = new DeepLinkGen(update.getMessage().getText(), update.getMessage().getChatId().toString(), "link").getShortDeepLink();
                    if (deeplink != null) {
                        ArrayList msgDinParams = new ArrayList();
                        msgDinParams.add(deeplink.getAdvName());
                        msgDinParams.add(deeplink.getLink());
                        msgDinParams.add(deeplink.getTarifsString());
                        new me.btcback.tlg.Message(update.getMessage().getChatId(), deeplink.getType(), msgDinParams).send();
                    } else {
                        new me.btcback.tlg.Message(update.getMessage().getChatId(), "link_not_response", new ArrayList()).send();
                    }
                } else {
                    new me.btcback.tlg.Message(update.getMessage().getChatId(), "error_cmd", new ArrayList()).send();
                }
            }

        } else if (update.hasCallbackQuery()) {
            // Set variables
            String callBackData = update.getCallbackQuery().getData();
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatIdLocal = update.getCallbackQuery().getMessage().getChatId();

            DBPg.getInstance().updateLastActivity(chatIdLocal);
            //если это кнопка магазин
            if (callBackData.contains("shop_from_list_id=")) {
                //get shop admitadId
                String advId = getCalbackDataParamVal(callBackData);
                //generate dynamic string for message builder and send to user
                DeepLink deepLink = new DeepLinkGen(Long.parseLong(advId), String.valueOf(chatIdLocal), "list").getShortDeepLink();
                if (deepLink != null) {
                    ArrayList msgDinElms = new ArrayList();
                    msgDinElms.add(deepLink.getAdvName());
                    msgDinElms.add(deepLink.getLink());
                    msgDinElms.add(deepLink.getTarifsString());
                    new me.btcback.tlg.Message(chatIdLocal, deepLink.getType(), msgDinElms).send();
                } else {
                    new me.btcback.tlg.Message(chatIdLocal, "link_not_response", new ArrayList()).send();
                }
            }
            //кнопка навигации НАЗАД
            else if (callBackData.contains("list_prev_btn_page")) {
                int pageNumNow = Integer.parseInt(getCalbackDataParamVal(callBackData));
                if (pageNumNow > 1) {
                    me.btcback.tlg.Message msg = new me.btcback.tlg.Message(chatIdLocal, messageId);
                    showShopsCatalog(chatIdLocal, pageNumNow - 1, messageId);
                }
            }
            //кнопка навигации ВПЕРЕД
            else if (callBackData.contains("list_next_btn_page")) {
                int pageNumNow = Integer.parseInt(getCalbackDataParamVal(callBackData));
                showShopsCatalog(chatIdLocal, pageNumNow + 1, messageId);
            }
            //кнопка вывести средства с баланса
            else if (callBackData.contains("pay_out_btn")) {
                new me.btcback.tlg.Message(chatIdLocal, messageId).del();
                BotUser botUser = DBPg.getInstance().getBotUserFromChatId(chatIdLocal);
                if (botUser != null) { //если такой юзверь есть
                    Double balanceNow = botUser.getBtcBalance();
                    if (balanceNow > 0 & balanceNow >= Setts.getInstance().getDbl("min_paid_out_sum")) { //если баланс юзверя больше 0
                        payOutConfrim(chatIdLocal);
                    } else {
                        ArrayList msgDinElms = new ArrayList();
                        msgDinElms.add(Setts.getInstance().getStr("min_paid_out_sum"));
                        new me.btcback.tlg.Message(chatIdLocal, "paid_out_sum_error", msgDinElms).send();
                    }
                }
            }
            //кнопка подтвердить вывод с баланса
            else if (callBackData.contains("pay_out_confrim_yes_btn")) {
                String usernameInTelegram = update.getCallbackQuery().getFrom().getUserName();
                usernameInTelegram = (usernameInTelegram != null ? usernameInTelegram : "empty");
                //updateUserBalanceWhereChatId
                new me.btcback.tlg.Message(chatIdLocal, messageId).del();
                BotUser botUser = DBPg.getInstance().getBotUserFromChatId(chatIdLocal);
                if (botUser != null) { //если такой юзверь есть
                    Double balanceNow = botUser.getBtcBalance();
                    if (balanceNow > 0) { //если баланс юзверя больше 0
                        DBPg.getInstance().updateUserBalanceWhereChatId(chatIdLocal, (balanceNow * -1));
                        new BTCCheck(chatIdLocal, balanceNow, "paid_out_from_balance", usernameInTelegram).addCheckToDelivery();
                        ArrayList dinElms = new ArrayList();
                        dinElms.add(BigDecimal.valueOf(balanceNow).setScale(8, BigDecimal.ROUND_HALF_UP).toString());
                        new me.btcback.tlg.Message(chatIdLocal, "check_wait_to_create", dinElms).send();
                    } else {
                        new me.btcback.tlg.Message(chatIdLocal, "На вышем балансе не достаточно средств для вывода.").send();
                    }
                }
            }
            //кнопка отменить вывод с баланса
            else if (callBackData.contains("pay_out_confrim_no_btn")) {
                new me.btcback.tlg.Message(chatIdLocal, messageId).del();
            }
            //кнопка получить бонус в рассылочном месседже
            else if (callBackData.contains("get_bdf_id")) {
                Long defMsgId = Long.parseLong(callBackData.substring(callBackData.indexOf("=") + 1, callBackData.indexOf("&")));
                String[] twoBtn = getCalbackDataParamVal(callBackData).split("&");//Long.parseLong(getCalbackDataParamVal(callBackData));
                twoBtn = twoBtn[1].split("!!!");
                Boolean userChatIdInDefMsg = DBPg.getInstance().checkChatIdInDefMsg(defMsgId, chatIdLocal);
                if (userChatIdInDefMsg != null) {
                    if (!userChatIdInDefMsg) {
                        DBPg.getInstance().addRecivedChatIdToDeferredMsg(defMsgId, chatIdLocal);
                        String bonusSumInSat = DBPg.getInstance().getDefMsgSum(defMsgId);
                        if (bonusSumInSat != null) {
                            Double bonusSum = Double.parseDouble(bonusSumInSat) * 0.00000001;
                            new BTCCheck(chatIdLocal, bonusSum).sendToUserBalance();
                            ArrayList msgDinElms = new ArrayList();
                            msgDinElms.add(bonusSumInSat);
                            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                            ArrayList keyboard = new ArrayList<>();
                            ArrayList row = new ArrayList();
                            row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("earnings_3step_btn")).setCallbackData("invite_frends_btn"));
                            keyboard.add(row);
                            keyboardMarkup.setKeyboard(keyboard);
                            new me.btcback.tlg.Message(chatIdLocal, "bonus_from_def_msgs_sended", msgDinElms).send(keyboardMarkup);
                        } else {
                            new me.btcback.tlg.Message(chatIdLocal, "Бонус более не действует.").send();
                        }
                    } else {
                        new me.btcback.tlg.Message(chatIdLocal, "bonus_from_def_msgs_not_found", new ArrayList()).send();
                    }
                } else {
                    new me.btcback.tlg.Message(chatIdLocal, "Бонус более не действует.").send();
                }
                InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
                ArrayList keyboard = new ArrayList<>();
                ArrayList row = new ArrayList();
                row.add(new InlineKeyboardButton(twoBtn[0]).setUrl(twoBtn[1]));
                keyboard.add(row);
                keyboardMarkup.setKeyboard(keyboard);
                EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup()
                        .setChatId(chatIdLocal)
                        .setReplyMarkup(keyboardMarkup)
                        .setMessageId(messageId);
                try {
                    execute(editMessageReplyMarkup);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                //НАЖАТИЕ ПО ЗАКАЗУ ИЗ ИСТОРИИ ЗАКАЗОВ
            } else if (callBackData.contains("more_history_order_id")) {
                Long moreOrderHistory = Long.parseLong(getCalbackDataParamVal(callBackData));
                Order thisOrderInDb = DBPg.getInstance().getOrderWhereId(moreOrderHistory);
                if (thisOrderInDb != null) {
                    String thisStatus = null;
                    switch (thisOrderInDb.getStatus()) {
                        case "approved":
                            thisStatus = "В ожидании";
                            if (thisOrderInDb.getPaid() == 1) {
                                thisStatus = "Подтвержден";
                            }
                            break;
                        case "pending":
                            thisStatus = "В ожидании";
                            break;
                        case "declined":
                            thisStatus = "Отменен";
                            break;

                    }
                    SimpleDateFormat historyOutDTFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    String btcCashback = "";
                    if(thisOrderInDb.getPaid() != 1){
                        btcCashback = "~" + new BigDecimal(thisOrderInDb.getPaymentBtc()).setScale(8, RoundingMode.HALF_UP).toString();
                    } else{
                        btcCashback = new BigDecimal(thisOrderInDb.getPaymentBtc()).setScale(8, RoundingMode.HALF_UP).toString();
                    }
                    ArrayList dinElms = new ArrayList();
                    dinElms.add(thisOrderInDb.getOfferName()); //{1} - название магазина
                    dinElms.add(thisOrderInDb.getSumTotal() + " " + thisOrderInDb.getCurrency()); //{2} - полная сумма покупки + код валюты
                    dinElms.add(thisOrderInDb.getPayment() * thisOrderInDb.getUserPersent() + " " + thisOrderInDb.getCurrency()); //{3} - сумма кэшбека + код валюты
                    dinElms.add(btcCashback); //{4} - сумма кэшбека в BTC
                    dinElms.add(thisStatus); //{5} - статус Подтвержден / В ожидании / Отменен
                    dinElms.add(thisOrderInDb.getOfferOrderId()); //{6} - Номер заказа в системе оффера
                    dinElms.add(historyOutDTFormat.format(thisOrderInDb.getDatetimeDate()).toString()); //{7} - дата заказа
                    new me.btcback.tlg.Message(chatIdLocal, "order_more_info", dinElms).send();
                }

                // new me.btcback.tlg.Message();
            }
            else if (callBackData.contains("invite_frends_btn")) {
                String inviteCode = DBPg.getInstance().getRefCodeFromChatId(chatIdLocal);
                ArrayList dinElms = new ArrayList();
                ArrayList dinElms2 = new ArrayList();
                dinElms.add("t.me/"+Setts.getInstance().getStr("bot_username")+"?start=" + inviteCode);
                dinElms2.add(Setts.getInstance().getInt("from_refer_bonus_sum"));
                new me.btcback.tlg.Message(chatIdLocal, "pre_refer", dinElms2).send();
                new me.btcback.tlg.Message(chatIdLocal, "refer_for_resend", dinElms).sendWithLink();
            }
            else if (callBackData.contains("pre_from_list_btn")) {
                ArrayList dinElms = new ArrayList();
                dinElms.add(Setts.getInstance().getInt("from_list_bonus_sum"));
                new me.btcback.tlg.Message(chatIdLocal, "pre_from_list_bonus", dinElms).send();
            }
            else if (callBackData.contains("pre_from_link_btn")) {
                ArrayList dinElms = new ArrayList();
                dinElms.add(Setts.getInstance().getInt("from_link_bonus_sum"));
                new me.btcback.tlg.Message(chatIdLocal, "pre_from_link_bonus", dinElms).send();
            }
            else if (callBackData.contains("pre_first_buy_btn")) {
                ArrayList dinElms = new ArrayList();
                dinElms.add(Setts.getInstance().getInt("first_buy_bonus_sum"));
                new me.btcback.tlg.Message(chatIdLocal, "pre_first_buy_bonus", dinElms).send();
            }
            else {
            }
        } else {

        }
    }

    /*
    @param callBackData NOT NULL, NOT EMPTY
    * */
    private String getCalbackDataParamVal(String callBackData) {
        return callBackData.substring(callBackData.indexOf("=") + 1);
    }

    private String getParamVal(String callBackData) {
        return callBackData.substring(callBackData.indexOf(" ") + 1);
    }

    @Override
    public String getBotUsername() {
        // TODO
        return Setts.getInstance().getStr("bot_username");//"TestCash2BOT";
    }

    @Override
    public String getBotToken() {
        // TODO
        return Setts.getInstance().getStr("bot_token");//"533450694:AAF0jlVDrUhVlAmClYrS6iiISjAYeaKYWsc";
    }

    public synchronized void sendTextMessageToChatID(String chatID, String msgText) {
        SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                .setChatId(chatID)
                .disableWebPagePreview()
                .setText(msgText)
                .setParseMode(ParseMode.MARKDOWN);
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            System.out.println("Проблема отправки сообщения");
            e.printStackTrace();
        }
    }

    public synchronized void initKeyboard(Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
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
        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setText(Setts.getInstance().getMsgTpl("start_msg"));
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<Advertiser> getPageAdvertizers(int pageNum) {
        int advertiserCount = AdmitadCore.getInstance().activeAdvertisersNew.size();
        if (advertiserCount > 0 & pageNum > 0) {
            //считаем общее количество кнопок на этой странице
            int maxButtonsInThisPageCount = Setts.getInstance().getInt("list_rows_count") * Setts.getInstance().getInt("list_cols_count");
            //считаем стартовый индекс
            int startAdvertiserIndex = ((pageNum - 1) * maxButtonsInThisPageCount);
            if (startAdvertiserIndex >= advertiserCount | startAdvertiserIndex < 0) return new ArrayList<>();
            int finishAdvertiserIndex = startAdvertiserIndex + maxButtonsInThisPageCount;
            if (finishAdvertiserIndex > advertiserCount) {
                finishAdvertiserIndex = advertiserCount;
            }
            return AdmitadCore.getInstance().activeAdvertisersNew.subList(startAdvertiserIndex, finishAdvertiserIndex);
        } else {
            return new ArrayList<>();
        }
    }

    private synchronized List<List<InlineKeyboardButton>> catalogPageBuilder(Long chatId, int pageNum) {
        ArrayList keyboard = new ArrayList();
        ArrayList row = new ArrayList();
        //ArrayList navBar = new ArrayList();
        //формируем строки клавиатуры
        List<Advertiser> pageAdvertizers = getPageAdvertizers(pageNum);
        int countOfAdvertisers = pageAdvertizers.size();
        if (countOfAdvertisers > 0) {
            int rowsCount = countOfAdvertisers / Setts.getInstance().getInt("list_cols_count");
            if ((countOfAdvertisers % 2) != 0) {
                rowsCount = rowsCount + 1;
            }
            int counterNow = 1;
            for (Advertiser advertiser : pageAdvertizers) {
                if ((counterNow % Setts.getInstance().getInt("list_cols_count")) != 0) {
                    row.add(new InlineKeyboardButton(Setts.getInstance().getStr("emoji_start_list") + advertiser.getName()).setCallbackData("shop_from_list_id=" + advertiser.getIdAdmitad()));
                    //TODO: посмотреть что будет при пустом смайле.
                    if (countOfAdvertisers == counterNow) keyboard.add(row);
                } else {
                    row.add(new InlineKeyboardButton(Setts.getInstance().getStr("emoji_start_list") + advertiser.getName()).setCallbackData("shop_from_list_id=" + advertiser.getIdAdmitad()));
                    keyboard.add(row);
                    row = new ArrayList();
                }
                counterNow++;
            }
            ArrayList navPanel = new ArrayList();
            if (getPageAdvertizers(pageNum - 1).size() > 0) {
                navPanel.add(new InlineKeyboardButton(Setts.getInstance().getBtn("list_prev_btn")).setCallbackData("list_prev_btn_page=" + pageNum));
            }
            if (getPageAdvertizers(pageNum + 1).size() > 0) {
                navPanel.add(new InlineKeyboardButton(Setts.getInstance().getBtn("list_next_btn")).setCallbackData("list_next_btn_page=" + pageNum));
            }
            if (navPanel.size() > 0) {
                keyboard.add(navPanel);
            }
        }

        return keyboard;
    }

    public synchronized void showShopsCatalog(Long chatId, int page) {
        List<List<InlineKeyboardButton>> keyboard = catalogPageBuilder(chatId, page);
        if (keyboard.size() != 0) {
            SendMessage sendMessage = new SendMessage();
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup().setKeyboard(keyboard);
            sendMessage.setReplyMarkup(inlineKeyboard);
            sendMessage.setChatId(chatId);
            sendMessage.setText(Setts.getInstance().getMsgTpl("with_list_message"));
            sendMessage.disableWebPagePreview()
                    .setParseMode("Markdown");
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void showShopsCatalog(Long chatId, int page, Integer messageId) {
        List<List<InlineKeyboardButton>> keyboard = catalogPageBuilder(chatId, page);
        if (keyboard.size() != 0) {
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup().setKeyboard(keyboard);
            EditMessageReplyMarkup editMessageReplyMarkup = new EditMessageReplyMarkup()
                    .setChatId(chatId)
                    .setReplyMarkup(inlineKeyboard)
                    .setMessageId(messageId);
            try {
                execute(editMessageReplyMarkup);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void showBalance(long chatId) {
        BotUser botUser = DBPg.getInstance().getBotUserFromChatId(chatId);
        ArrayList keyboard = new ArrayList();
        ArrayList row = new ArrayList();
        ArrayList row2 = new ArrayList();
        row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("pay_out_btn")).setCallbackData("pay_out_btn"));
        row2.add(new InlineKeyboardButton(Setts.getInstance().getBtn("earnings_btn")).setCallbackData("invite_frends_btn"));
        keyboard.add(row);
        keyboard.add(row2);
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup().setKeyboard(keyboard);
        if (keyboard.size() > 0) {
            ArrayList dinElements = new ArrayList();
            if (botUser.getBtcBalance() > 0) {
                dinElements.add(BigDecimal.valueOf(botUser.getBtcBalance()).setScale(8, BigDecimal.ROUND_HALF_UP).toString());
                dinElements.add(Setts.getInstance().getStr("min_paid_out_sum"));
            } else {
                dinElements.add(botUser.getBtcBalance());
                dinElements.add(Setts.getInstance().getStr("min_paid_out_sum"));
            }
            new me.btcback.tlg.Message(chatId, "balance_show", dinElements).send(inlineKeyboard);
        }
    }

    public synchronized void payOutConfrim(long chatId) {
        BotUser botUser = DBPg.getInstance().getBotUserFromChatId(chatId);
        ArrayList keyboard = new ArrayList();
        ArrayList row = new ArrayList();
        row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("pay_out_confrim_yes_btn")).setCallbackData("pay_out_confrim_yes_btn"));
        row.add(new InlineKeyboardButton(Setts.getInstance().getBtn("pay_out_confrim_no_btn")).setCallbackData("pay_out_confrim_no_btn"));
        keyboard.add(row);
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup().setKeyboard(keyboard);
        if (keyboard.size() > 0) {
            ArrayList dinElements = new ArrayList();
            dinElements.add(new BigDecimal(botUser.getBtcBalance()).setScale(8, BigDecimal.ROUND_HALF_UP));
            new me.btcback.tlg.Message(chatId, "balance_pay_out_confrim", dinElements).send(inlineKeyboard);
        }
    }

    public synchronized void showShopsHistory(Long chatId, Long moreInfoOrderId, Long messageId) {
        ArrayList<Order> userOrders = DBPg.getInstance().getUserOrders(chatId);
        if (userOrders.size() > 0) {
            ArrayList keyboard = new ArrayList();
            ArrayList row;
            for (Order userOrder : userOrders) {
                SimpleDateFormat historyOutDTFormat = new SimpleDateFormat("dd.MM.yyyy");
                String[] status = Setts.getInstance().getStr("order_status_markers").split(",");
                String statusMarker = "";
                switch (userOrder.getStatus()){
                    case "approved":
                        if(userOrder.getPaid() == 1){
                            statusMarker = status[0];
                        } else {
                            statusMarker = status[1];
                        }
                        break;
                    case "pending":
                        statusMarker = status[1];
                        break;
                    case "declined":
                        statusMarker = status[2];
                        break;

                }
                String btnText = statusMarker + historyOutDTFormat.format(userOrder.getDatetimeDate()).toString() + ": " + userOrder.getSumTotal() + " " + userOrder.getCurrency() + " (" + userOrder.getOfferName() + ")";
                row = new ArrayList();
                row.add(new InlineKeyboardButton(btnText).setCallbackData("more_history_order_id=" + userOrder.getDbId()));
                keyboard.add(row);

            }
            InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup().setKeyboard(keyboard);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setReplyMarkup(inlineKeyboard);
            sendMessage.setChatId(chatId);
            sendMessage.setText(Setts.getInstance().getMsgTpl("with_orders_history"));
            sendMessage.disableWebPagePreview()
                    .setParseMode("Markdown");
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage
                    .setChatId(chatId)
                    .setText(Setts.getInstance().getMsgTpl("orders_history_empty_msg"))
                    .setParseMode("Markdown");
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}