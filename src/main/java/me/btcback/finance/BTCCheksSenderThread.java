package me.btcback.finance;

import me.btcback.finance.models.BTCCheck;
import me.btcback.pgdb.DBPg;
import me.btcback.tlg.Message;

import java.math.BigDecimal;
import java.util.ArrayList;

public class BTCCheksSenderThread extends Thread {

    public void run(){
        while(true){
            parseWaitToSendChecks();
            try {
                this.sleep(60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //ищет чеки готовые к отправке и отправляет их адресатам
    public void parseWaitToSendChecks() {
        ArrayList<BTCCheck> waitToSendChecks = DBPg.getInstance().getBtcChecksForSend();
        if (waitToSendChecks.size() > 0) { //если есть чеки для отправки
            for (BTCCheck check : waitToSendChecks) {
                check.setStatus(BTCCheck.STATUS_SENDED);
                if(check.saveStatus()){ // сохраняем статус чека в бд и отправляем юзверищу
                    //если сохранили успешно, отправляем чек юзверю
                    ArrayList msgDinElms = new ArrayList();
                    msgDinElms.add(BigDecimal.valueOf(check.getSum()).setScale(8, BigDecimal.ROUND_HALF_UP).toString());
                    msgDinElms.add(check.getLink());
                    new Message(check.getRecipient(), check.getType(), msgDinElms).send();
                }
                //формируем месседж
            }
        }
    }

}
