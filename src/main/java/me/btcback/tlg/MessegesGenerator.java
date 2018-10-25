package me.btcback.tlg;

import me.btcback.Setts;
import me.btcback.admitad.AdmitadCore;
import me.btcback.pgdb.DBPg;

import java.math.BigDecimal;

public class MessegesGenerator {

    public MessegesGenerator() {
    }


    public synchronized String getNotificationAboutPendingAction(double allCash, double cashbackCash, String cashbackInBtc, String currency, String actionDateTime) {

        String resultMsg = String.format(Setts.getInstance().getMsgTpl("pending_msg"), allCash + " " + currency, cashbackCash + " " + currency, cashbackInBtc);
        return resultMsg;
    }

    public synchronized String getNotificationAboutApprovedAction(double allCash, double CashbackCash, String currency, String actionDateTime) {
        String resultMsg = String.format(Setts.getInstance().getMsgTpl("approved_msg"), allCash + " " + currency);
        return resultMsg;
    }

    public synchronized String getNotificationAboutSendCashbackCheck(double allCashBack, String linkToCheck) {
        String resultMsg = String.format(Setts.getInstance().getMsgTpl("send_cashback_check_msg"), linkToCheck, new BigDecimal(allCashBack).setScale(8, BigDecimal.ROUND_HALF_UP).toString());
        return resultMsg;
    }

    public boolean ifURL(String textForValidation) {
        textForValidation = textForValidation.trim();
        //textForValidation = textForValidation.replace("(/.+)$", "");
        if (textForValidation.length() > 3 & textForValidation.contains(".") & textForValidation.matches("(http:\\/\\/|https:\\/\\/|)(((\\w+[-]|\\w+)+\\.)*|)(\\w+|\\w+[-]\\w+)([\\.])\\w+(.*)")) { //если меньше 3-х символов или нет точки или не соответствует шаблону
            return true;
        }
        return false;
    }

}
