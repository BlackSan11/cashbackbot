package me.btcback;

import me.btcback.admitad.AdmitadCore;
import me.btcback.admitad.OrdersInspector;
import me.btcback.bonuses.BonusInspector;
import me.btcback.finance.BTCCheksSenderThread;
import me.btcback.pgdb.DBPg;
import me.btcback.tlg.DeferredMsgsSenderThread;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import me.btcback.tlg.MainBot;
import me.btcback.tlg.MessegesGenerator;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        AdmitadCore.getInstance();
        /*
        * BTCCheksSenderThread(); //чекает чеки готовые к отправке и отправляет их по адресатам
        *
        * */

        //первичная инициализация
            //БД
        DBPg.getInstance().init();
            //Настройки
        Setts.getInstance().updateSettings();
            //BTC индексы и служба доставки
        //создаем поток обновления настроек и текстовых шаблонов
        UpdateAgent updateAgent = new UpdateAgent();
        updateAgent.start();

        //инициализация инспекторов
        BonusInspector bonusInspector = new BonusInspector();
        bonusInspector.start();

        //инициализация telegram bota
        MessegesGenerator msgGen = new MessegesGenerator();
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        MainBot mainBot = new MainBot(msgGen);

        try {
            botsApi.registerBot(mainBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        BTCCheksSenderThread btcCheckSender = new BTCCheksSenderThread();
        btcCheckSender.start();

        OrdersInspector ordersInspector = new OrdersInspector();
        ordersInspector.start();

        DeferredMsgsSenderThread deferredMsgsSenderThread = new DeferredMsgsSenderThread();
        deferredMsgsSenderThread.start();

        while(true){
            if(updateAgent.getState() == Thread.State.TERMINATED){
                updateAgent = new UpdateAgent();
                updateAgent.start();
            }
            if(bonusInspector.getState() == Thread.State.TERMINATED){
                bonusInspector = new BonusInspector();
                bonusInspector.start();
            }
            if(deferredMsgsSenderThread.getState() == Thread.State.TERMINATED){
                deferredMsgsSenderThread = new DeferredMsgsSenderThread();
                deferredMsgsSenderThread.start();
            }
            if(ordersInspector.getState() == Thread.State.TERMINATED){
                ordersInspector = new OrdersInspector();
                ordersInspector.start();
            }
            if(btcCheckSender.getState() == Thread.State.TERMINATED){
                btcCheckSender = new BTCCheksSenderThread();
                btcCheckSender.start();
            }
            Thread.sleep(120000);
        }
    }
}
