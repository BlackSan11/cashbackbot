package me.btcback.pgdb;

import me.btcback.BotUser;
import me.btcback.admitad.Advertiser;
import me.btcback.admitad.models.DeepLink;
import me.btcback.admitad.models.Order;
import me.btcback.models.Option;
import me.btcback.finance.models.BTCCheck;
import me.btcback.tlg.models.DeferredMsg;
import org.postgresql.util.PSQLException;

import java.security.SecureRandom;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DBPg {

    private static final DBPg INSTANCE = new DBPg();


    /*private final String DB_HOST = "195.146.74.90";//"localhost";//
    private final String DB_NAME = "cashbackbotbase";
    private final String DB_USER = "btccashback";//"admin_btcback_me";
    private final String DB_PASS = "1996reeN";//"Chy3nKS093";
    private final String DB_PORT = "5432";*/
    //TODO:ВЫНЕСТИ В ФАЙЛ
    private final String DB_HOST = "localhost";
    private final String DB_NAME = "cashbackbotbase";
    private final String DB_USER = "admin_btcback_me";
    private final String DB_PASS = "Chy3nKS093";
    private final String DB_PORT = "5432";

    public Connection connectionToDB;

    public DBPg() {
        this.connectionToDB = connectToDb();
    }

    public void init() {
        System.out.println("DB inited.");
    }

    public synchronized Connection connectToDb() {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager
                    .getConnection("jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + this.DB_NAME, this.DB_USER, this.DB_PASS);
            connection.setAutoCommit(false);
            return connection;
        } catch (ClassNotFoundException e) {
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }

    public synchronized Statement createStatementMy() {
        if (this.connectionToDB != null) {
            try {
                Statement stmt = this.connectionToDB.createStatement();
                return stmt;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

    }

    public synchronized boolean putDeeplinkToDB(String Place_ID, Long ADV_ID, String DEEP_URL, Long CHAT_ID, String subid, String shortUrl, String from) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatedDateTime = formatForDateNow.format(dateNow);
            //TODO:поменять на переменную
            String sql = "INSERT INTO DEEPLINKS (PLACE_ID,ADV_ID,CREATE_DATE,URL,CHAT_ID, SUBID, short_url, from_c) VALUES (" + Place_ID + ", '" + ADV_ID + "', '" + formatedDateTime + "', '" + DEEP_URL + "', '" + CHAT_ID + "', '" + subid + "', '" + shortUrl + "', '" + from + "');";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean putActionToDB(Long admitad_id, String status, String dataTimeAction, String updateSatatusDateTime, String subid, String user_persent) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatedDateTime = formatForDateNow.format(dateNow);
            String sql = "INSERT INTO AdmitadActions (admitad_id, incpect_datetime, status_after_incpection, datatime_action," +
                    " update_status_datetime, subid, paidout, pay_rate) VALUES ("
                    + admitad_id + ", '" + formatedDateTime + "', '" + status + "', '" + dataTimeAction
                    + "', '" + updateSatatusDateTime + "', '" + subid + "', '0', '" + user_persent + "');";

            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized String getStatusFromActionID(long actionId) {
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT status_after_incpection FROM admitadactions WHERE admitad_id='" + actionId + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("status_after_incpection");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    //ЗАКАЗЫ
    public synchronized Order getOrderWhereActionId(Long actionId) {
        Order resultOrder;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM orders WHERE action_id='" + actionId + "' LIMIT 1;";
                rs = stmt.executeQuery(sql);
                rs.next();
                resultOrder = new Order(
                        rs.getInt("id"),
                        rs.getLong("offer_id"),
                        rs.getDouble("sum_total"),
                        rs.getString("status"),
                        rs.getString("currency"),
                        rs.getString("datetime"),
                        rs.getString("processing_datetime"),
                        rs.getString("subid"),
                        rs.getString("subid1"),
                        rs.getString("subid2"),
                        rs.getString("subid3"),
                        rs.getString("subid4"),
                        rs.getString("status_updated_datetime"),
                        rs.getInt("processed"),
                        rs.getInt("paid"),
                        rs.getString("order_id"),
                        rs.getLong("action_id"),
                        rs.getString("offer_name"),
                        rs.getDouble("user_persent"),
                        rs.getDouble("payment"),
                        rs.getDouble("payment_btc"),
                        rs.getString("closing_date"),
                        rs.getString("tlg_username")
                );
                rs.close();
                stmt.close();
                return resultOrder;
            } catch (PSQLException p) {
                return null;
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized Order getOrderWhereId(Long id) {
        Order resultOrder;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM orders WHERE id='" + id + "' LIMIT 1;";
                rs = stmt.executeQuery(sql);
                rs.next();
                resultOrder = new Order(
                        rs.getInt("id"),
                        rs.getLong("offer_id"),
                        rs.getDouble("sum_total"),
                        rs.getString("status"),
                        rs.getString("currency"),
                        rs.getString("datetime"),
                        rs.getString("processing_datetime"),
                        rs.getString("subid"),
                        rs.getString("subid1"),
                        rs.getString("subid2"),
                        rs.getString("subid3"),
                        rs.getString("subid4"),
                        rs.getString("status_updated_datetime"),
                        rs.getInt("processed"),
                        rs.getInt("paid"),
                        rs.getString("order_id"),
                        rs.getLong("action_id"),
                        rs.getString("offer_name"),
                        rs.getDouble("user_persent"),
                        rs.getDouble("payment"),
                        rs.getDouble("payment_btc"),
                        rs.getString("closing_date"),
                        rs.getString("tlg_username")
                );
                rs.close();
                stmt.close();
                return resultOrder;
            } catch (PSQLException p) {
                return null;
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized ArrayList getPaidOrdersFromDb() {
        Order resultOrder;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM orders WHERE paid='0' AND status='approved';";
                rs = stmt.executeQuery(sql);
                ArrayList<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getLong("id"),
                            rs.getLong("offer_id"),
                            rs.getDouble("sum_total"),
                            rs.getString("status"),
                            rs.getString("currency"),
                            rs.getString("datetime"),
                            rs.getString("processing_datetime"),
                            rs.getString("subid"),
                            rs.getString("subid1"),
                            rs.getString("subid2"),
                            rs.getString("subid3"),
                            rs.getString("subid4"),
                            rs.getString("status_updated_datetime"),
                            rs.getInt("processed"),
                            rs.getInt("paid"),
                            rs.getString("order_id"),
                            rs.getLong("action_id"),
                            rs.getString("offer_name"),
                            rs.getDouble("user_persent"),
                            rs.getDouble("payment"),
                            rs.getDouble("payment_btc"),
                            rs.getString("closing_date"),
                            rs.getString("tlg_username")
                    ));

                }
                rs.close();
                stmt.close();
                return orders;
            } catch (PSQLException p) {
                return new ArrayList();
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return new ArrayList();
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return new ArrayList();
        }
    }

    public synchronized ArrayList getUserOrders(Long chatId) {
        Order resultOrder;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM orders WHERE subid1='" + chatId + "' ORDER BY datetime DESC;";
                rs = stmt.executeQuery(sql);
                ArrayList<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getLong("id"),
                            rs.getLong("offer_id"),
                            rs.getDouble("sum_total"),
                            rs.getString("status"),
                            rs.getString("currency"),
                            rs.getString("datetime"),
                            rs.getString("processing_datetime"),
                            rs.getString("subid"),
                            rs.getString("subid1"),
                            rs.getString("subid2"),
                            rs.getString("subid3"),
                            rs.getString("subid4"),
                            rs.getString("status_updated_datetime"),
                            rs.getInt("processed"),
                            rs.getInt("paid"),
                            rs.getString("order_id"),
                            rs.getLong("action_id"),
                            rs.getString("offer_name"),
                            rs.getDouble("user_persent"),
                            rs.getDouble("payment"),
                            rs.getDouble("payment_btc"),
                            rs.getString("closing_date"),
                            rs.getString("tlg_username")
                    ));

                }
                rs.close();
                stmt.close();
                return orders;
            } catch (PSQLException p) {
                return new ArrayList();
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return new ArrayList();
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return new ArrayList();
        }
    }

    public synchronized Boolean checkOrderInDB(Long actionId) {
        //if(admitadID ) return false;
        Integer isExist;
        Boolean isExistFlag;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT '1' FROM orders WHERE action_id = '" + actionId + "' LIMIT 1;");
                rs.next();
                try {
                    isExist = Integer.parseInt(rs.getString(1));
                } catch (PSQLException e) {
                    isExist = 0;
                    //e.printStackTrace();
                }
                rs.close();
                stmt.close();
                return isExistFlag = (isExist == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public synchronized Boolean checkOrderInDBWhereId(Long id) {
        //if(admitadID ) return false;
        Integer isExist;
        Boolean isExistFlag;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT '1' FROM orders WHERE id = '" + id + "' LIMIT 1;");
                rs.next();
                try {
                    isExist = Integer.parseInt(rs.getString(1));
                } catch (PSQLException e) {
                    isExist = 0;
                    //e.printStackTrace();
                }
                rs.close();
                stmt.close();
                return isExistFlag = (isExist == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public synchronized boolean putOrderToDB(long offer_id, double sum_total, String status, String currency, String datetime, String processing_datetime, String subid, String subid1, String subid2, String subid3, String subid4, String status_updated_datetime, int processed, int paid, String order_id, long action_id, double userPersent, double payment, double paymentBtc, String offerName, String closing_date, String tlg_username) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatedDateTime = formatForDateNow.format(dateNow);
            offerName = offerName.replace("'", "");
            String sql = "INSERT INTO orders (" +
                    "offer_id," +
                    "sum_total," +
                    "status," +
                    "currency," +
                    "datetime," +
                    "processing_datetime," +
                    "subid," +
                    "subid1," +
                    "subid2," +
                    "subid3," +
                    "subid4," +
                    "status_updated_datetime," +
                    "processed," +
                    "paid," +
                    "order_id," +
                    "action_id," +
                    "user_persent," +
                    "payment," +
                    "payment_btc," +
                    "offer_name," +
                    "closing_date," +
                    "tlg_username" +
                    ") VALUES ('" + offer_id + "','" + sum_total + "','" + status + "','" + currency + "','" + datetime + "','" + processing_datetime + "','" + subid + "','" + subid1 + "','" + subid2 + "','" + subid3 + "','" + subid4 + "','" + status_updated_datetime + "','" + processed + "','" + paid + "','" + order_id + "','" + action_id + "','" + userPersent + "','" + payment + "','" + paymentBtc + "','" + offerName + "','" + closing_date + "','" + tlg_username + "');";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    //ЗАКАЗЫ
    public synchronized Advertiser getAdvertizerWhereAdmitadId(Long idAdmitad) {
        Advertiser resultOrder;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM advertizers WHERE id_ad='" + idAdmitad + "' LIMIT 1;";
                rs = stmt.executeQuery(sql);
                rs.next();
                resultOrder = new Advertiser(
                        rs.getLong("id"),
                        rs.getLong("id_ad"),
                        rs.getString("name_ad"),
                        rs.getString("tarifs_ad"),
                        rs.getString("name_cstm"),
                        rs.getString("tarifs_cstm"),
                        rs.getLong("use_amount"),
                        rs.getInt("status"),
                        rs.getString("site_url"),
                        rs.getString("ref_link")
                );
                rs.close();
                stmt.close();
                return resultOrder;
            } catch (PSQLException p) {
                return null;
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized boolean putAdvertizerToDB(Long idAdmitad, String nameAdmitad, String tarifsAdmitad, String siteUrl, String refLink) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            //Date dateNow = new Date();
            //SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //String formatedDateTime = formatForDateNow.format(dateNow);
            nameAdmitad = nameAdmitad.replace("'", "");
            tarifsAdmitad = tarifsAdmitad.replace("'", "");
            String sql = "INSERT INTO advertizers (" +
                    "id_ad," +
                    "name_ad," +
                    "tarifs_ad," +
                    "use_amount," +
                    "status," +
                    "site_url," +
                    "ref_link" +
                    ") VALUES ('" + idAdmitad + "','" + nameAdmitad + "','" + tarifsAdmitad + "','" + 0 + "','" + 1 + "','" + siteUrl + "','" + refLink + "');";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized Boolean checkAdvertizerInDb(Long idAdmitad) {
        Integer isExist;
        Boolean isExistFlag;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT '1' FROM advertizers WHERE id_ad = '" + idAdmitad + "' LIMIT 1;");
                rs.next();
                try {
                    isExist = Integer.parseInt(rs.getString(1));
                } catch (PSQLException e) {
                    isExist = 0;
                    //e.printStackTrace();
                }
                rs.close();
                stmt.close();
                return isExistFlag = (isExist == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public synchronized boolean updateAdvertizerInfo(Long idAdmitad, String nameAdmitad, String tarifsAdmitad, String link, String ref_link) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            nameAdmitad = nameAdmitad.replace("'", "");
            tarifsAdmitad = tarifsAdmitad.replace("'", "");
            String sql;
            sql = "UPDATE advertizers set name_ad = '" + nameAdmitad + "', tarifs_ad = '" + tarifsAdmitad + "', site_url = '" + link + "', ref_link = '" + ref_link + "' where id_ad='" + idAdmitad + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized String getChatIDFromDeeplinkSubID(String subid) {
        if (subid == null | subid.equals("")) {
            return null;
        }
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT chat_id FROM deeplinks WHERE subid='" + subid + "'";
//                System.out.println(sql);
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("chat_id");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized String getAdvIdFromSubid(String subid) {
        if (subid == null | subid.equals("")) {
            return null;
        }
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT adv_id FROM deeplinks WHERE subid='" + subid + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("adv_id");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized String getChatIDFromActionId(long actionId) {

        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT chat_id FROM deeplinks WHERE admitad_id='" + actionId + "'";
//                System.out.println(sql);
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("chat_id");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }


            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }

        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized Boolean checkSubidInDB(String subid) {
        Integer isExist;
        Boolean isExistFlag;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT '1' FROM deeplinks WHERE subid = '" + subid + "' LIMIT 1;");
                rs.next();
                try {
                    isExist = Integer.parseInt(rs.getString(1));
                } catch (PSQLException e) {
                    isExist = 0;
                    //e.printStackTrace();
                }
                rs.close();
                stmt.close();
                return isExistFlag = (isExist == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public synchronized Boolean checkActionInDB(Long admitadID) {

        //if(admitadID ) return false;
        Integer isExist;
        Boolean isExistFlag;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT '1' FROM admitadactions WHERE admitad_id = '" + admitadID + "' LIMIT 1;");
                rs.next();
                try {
                    isExist = Integer.parseInt(rs.getString(1));
                } catch (PSQLException e) {
                    isExist = 0;
                    //e.printStackTrace();
                }
                rs.close();
                stmt.close();
                return isExistFlag = (isExist == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public synchronized Map<String, String> getTextTemplates() {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;

            try {
                rs = stmt.executeQuery("SELECT * FROM message_templates;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                Map<String, String> tempMap = new HashMap<>();
                while (rs.next()) {
                    tempMap.put(rs.getString("identifier"), rs.getString("text"));
                }
                return tempMap;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public synchronized Map<String, String> getBtns() {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;

            try {
                rs = stmt.executeQuery("SELECT * FROM buttons;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                Map<String, String> tempMap = new HashMap<>();
                while (rs.next()) {
                    tempMap.put(rs.getString("identifier"), rs.getString("value"));
                }
                return tempMap;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public synchronized Map<String, Option> getMainSettings() {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;

            try {
                rs = stmt.executeQuery("SELECT * FROM settings;");

            } catch (SQLException e) {

                e.printStackTrace();
            }
            try {
                Map<String, Option> tempMap = new HashMap<>();
                while (rs.next()) {
                    tempMap.put(rs.getString("identifier"), new Option(rs.getInt("id"), rs.getString("value"), rs.getString("description")));
                }
                return tempMap;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public synchronized ArrayList<BotUser> getPotentialBonusUser(String lastInspecting) {
        Statement stmt = createStatementMy();
        ArrayList<BotUser> tempMap = new ArrayList<>();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT * FROM users WHERE (last_activity >= '" + lastInspecting + "') AND (jumps_from_link='0' OR jumps_from_list='0' OR total_admi_actions='0')");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                while (rs.next()) {
                    tempMap.add(new BotUser(
                            rs.getLong("id"),
                            rs.getDouble("btc_balance"),
                            rs.getString("reg_datetime"),
                            rs.getInt("approved_admi_actions"),
                            rs.getInt("jumps_from_link"),
                            rs.getInt("jumps_from_list"),
                            rs.getLong("total_admi_actions"),
                            rs.getLong("chat_id")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
        }
        return tempMap;
    }

    public synchronized ArrayList<DeepLink> getShortDeeps(Long chatId, String from) {
        Statement stmt = createStatementMy();
        ArrayList<DeepLink> tempMap = new ArrayList<>();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT * FROM deeplinks WHERE chat_id = '" + chatId + "' AND from_c = '" + from + "'");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                while (rs.next()) {
                    tempMap.add(new DeepLink(rs.getLong("adv_id"), rs.getString("short_url")));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
        }
        return tempMap;
    }

    public synchronized ArrayList<Long> getUserOrdersIds(Long chatId) {
        Statement stmt = createStatementMy();
        ArrayList<Long> tempMap = new ArrayList<>();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT offer_id FROM orders WHERE subid1 = '" + chatId + "' ORDER BY datetime");
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
            try {
                while(rs.next()){
                    tempMap.add(rs.getLong("offer_id"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
        }
        return tempMap;
    }

    public synchronized String getOrdersLastUpdateStatusDateTime() {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT status_updated_datetime FROM orders WHERE status_updated_datetime=(SELECT MAX(status_updated_datetime) FROM orders);");
                rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            String lastDate;
            try {
                lastDate = rs.getString("status_updated_datetime");
            } catch (SQLException e) {
                lastDate = "2000-01-01 00:00:00";
            }
            try {
                rs.close();
                stmt.close();
                return lastDate;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public synchronized boolean updateStatusWhereActionId(long actionId, String status, Integer payout) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            if (payout == null) {
                sql = "UPDATE admitadactions set status_after_incpection = '" + status + "' where admitad_id='" + actionId + "';";
            } else {
                sql = "UPDATE admitadactions set status_after_incpection = '" + status + "',paidout='" + payout + "' where admitad_id='" + actionId + "';";
            }
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }


        } else {
            return false;
        }
    }

    public synchronized boolean updateOrderStatusInDb(long id, String status, String statusUpdated) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE orders set status = '" + status + "', status_updated_datetime = '" + statusUpdated + "' where id='" + id + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateOrderPaidOutStatus(long id, int paid, int paidOut) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE orders set paid_out = '" + paidOut + "', paid = '" + paid + "' where id='" + id + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateOrderBTCPayment(long id, double btcPayment) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE orders set payment_btc = '" + btcPayment + "' where id='" + id + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateOrderBTCPaymentAI(long action_id, double btcPayment) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE orders set payment_btc = '" + btcPayment + "' where action_id='" + action_id + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updatePaidOutStatusWhereActionId(long actionId, int paidOut) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE admitadactions set paidout = '" + paidOut + "' where admitad_id='" + actionId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized Long getOrderUserPersentFromActionID(long actionId) {
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT pay_rate FROM orders WHERE action_id='" + actionId + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("pay_rate");
                    rs.close();
                    stmt.close();
                    return Long.parseLong(lastDate);
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized boolean updateUserJumpsWhereId(Long userId, Integer jumpsFromLink, Integer jumpsFromList) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE users set jumps_from_link= '" + jumpsFromLink + "', jumps_from_list='" + jumpsFromList + "' WHERE id='" + userId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateUserTotalActionsWhereId(Long userId, Long totalActions) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE users set total_admi_actions='" + totalActions + "' WHERE id='" + userId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    private synchronized String refHashGen() {
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public synchronized boolean regNewUser(Long chatId, String username, String refFromUserChatId) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatedDateTime = formatForDateNow.format(dateNow);
            String refHash = refHashGen();
            String sql = "INSERT INTO users (btc_balance, reg_datetime, chat_id, approved_admi_actions, jumps_from_link, jumps_from_list, total_admi_actions, last_activity, username, ref_hash, from_user_chat_id) VALUES ('0', '" + formatedDateTime + "', '" + chatId + "','0','0','0','0','" + formatedDateTime + "', '"+ username +"', '"+ refHash +"', '"+ refFromUserChatId +"');";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized String getBonus(String cost, String typeBonus) {
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM btc_checks WHERE type='" + typeBonus + "' AND status='wait_to_send' AND sum='" + cost + "' LIMIT 1;";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("link");
                    try {
                        //нет нет нет в режим отправки, реципиент чат ид
                        sql = "UPDATE btc_checks set status = 'sended' where id='" + rs.getString("id") + "';";
                        stmt.executeUpdate(sql);
                        rs.close();
                        this.connectionToDB.commit();
                    } catch (PSQLException e) {
                        return "empty";
                    }
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return "empty";
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized Boolean checkUserInDb(String chatId) {
        Integer isExist;
        Boolean isExistFlag;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT '1' FROM users WHERE chat_id = '" + chatId + "' LIMIT 1;");
                rs.next();
                try {
                    isExist = Integer.parseInt(rs.getString(1));
                } catch (PSQLException e) {
                    isExist = 0;
                    //e.printStackTrace();
                }
                rs.close();
                stmt.close();
                return isExistFlag = (isExist == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        } else {
            return null;
        }
    }

    public synchronized Boolean checkRefHash(String refHash) {
        Integer isExist;
        Boolean isExistFlag;
        Statement stmt = createStatementMy();
        if (stmt != null && refHash != null && !refHash.equals("")) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT '1' FROM users WHERE ref_hash = '" + refHash + "' LIMIT 1;");
                rs.next();
                try {
                    isExist = Integer.parseInt(rs.getString(1));
                } catch (PSQLException e) {
                    isExist = 0;
                    //e.printStackTrace();
                }
                rs.close();
                stmt.close();
                return isExistFlag = (isExist == 1);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized String getPaidOutStatusFromActionID(long actionId) {
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT paidout FROM admitadactions WHERE admitad_id='" + actionId + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("paidout");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized String getChatIdFromComingRef(String refHash) {
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT chat_id FROM users WHERE ref_hash='" + refHash + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("chat_id");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized String getRefCodeFromChatId(long chatId) {
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT ref_hash FROM users WHERE chat_id='" + chatId + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("ref_hash");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized boolean putBTCCheckToDB(Long chatId, String sum) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatedDateTime = formatForDateNow.format(dateNow);
            String sql = "INSERT INTO btc_checks (type, status, recipient, sum) VALUES ('cashback', " +
                    "'wait_to_create', " +
                    "'" + chatId + "', " +
                    "'" + sum + "');";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    private synchronized Object notNullArg(Object arg) {
        return arg == null ? "" : arg;
    }

    public synchronized boolean createBtcCheck(String link, String type, String status, Long recipient, String sum, String recipienUsername) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            Date dateNow = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String formatedDateTime = formatForDateNow.format(dateNow);
            String sql = "INSERT INTO btc_checks (" +
                    "link, " +
                    "type, " +
                    "status, " +
                    "recipient, " +
                    "sum, " +
                    "rec_username" +
                    ") VALUES (" +
                    "'" + notNullArg(link) + "'," +
                    "'" + notNullArg(type) + "', " +
                    "'" + notNullArg(status) + "', " +
                    "'" + notNullArg(recipient) + "', " +
                    "'" + notNullArg(sum) + "'," +
                    "'" + notNullArg(recipienUsername) + "');";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateBtcCheckStatus(long checkIdInDb, String status, Long chatId) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE btc_checks set status = '" + status + "', recipient='" + chatId + "' where id='" + checkIdInDb + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateLastActivity(long chatId) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            Date dateNow = new Date();
            sql = "UPDATE users set last_activity = '" + dateNow + "' where chat_id='" + chatId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateRefHash(long chatId) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE users set ref_hash = '" + refHashGen() + "' where chat_id='" + chatId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateUserBalanceWhereChatId(long chatId, Double sumToAdd) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            Date dateNow = new Date();
            sql = "UPDATE users set btc_balance = btc_balance + '" + sumToAdd + "' where chat_id='" + chatId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean clearUserBalanceWhereChatId(long chatId) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            Date dateNow = new Date();
            sql = "UPDATE users set btc_balance = 0 where chat_id='" + chatId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized ArrayList<BTCCheck> getBtcChecksForSend() {
        Statement stmt = createStatementMy();
        ArrayList<BTCCheck> tempMap = new ArrayList<>();
        if (stmt != null) {
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM btc_checks WHERE status='wait_to_send' AND link IS NOT NULL AND link!='' AND type IS NOT NULL AND type!='' AND recipient IS NOT NULL AND recipient!='';");
                try {
                    while (rs.next()) {
                        tempMap.add(new BTCCheck(
                                rs.getLong("id"),
                                rs.getString("link"),
                                rs.getString("type"),
                                rs.getString("status"),
                                rs.getLong("recipient"),
                                rs.getDouble("sum")));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
        }
        return tempMap;
    }

    public synchronized ArrayList<DeferredMsg> getDeferredMsgsForSend() {
        Statement stmt = createStatementMy();
        ArrayList<DeferredMsg> deferredMsgs = new ArrayList<>();
        if (stmt != null) {
            try {
                ResultSet rs = stmt.executeQuery("SELECT * FROM deferred_msgs WHERE status='wait_to_send' OR status='testing';");
                try {
                    while (rs.next()) {
                        deferredMsgs.add(new DeferredMsg(
                                rs.getLong("id"),
                                rs.getString("text"),
                                rs.getString("text_link"),
                                rs.getString("button_link"),
                                rs.getString("text_link_ankor"),
                                rs.getString("button_link_ankor"),
                                rs.getString("status"),
                                rs.getString("test_chat_ids"),
                                rs.getDouble("bonus_sum"),
                                rs.getString("subid_marker")
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
        }
        return deferredMsgs;
    }

    public synchronized BTCCheck getBonusBtcCheck(String type, double sum) { //если передавать Double sum не сходится
        Statement stmt = createStatementMy();
        if (stmt != null) {
            try {
                ResultSet rs = null;
                rs = stmt.executeQuery("SELECT * FROM btc_checks WHERE type='" + type + "' AND status='wait_to_send' AND link!='' AND (recipient='' OR recipient IS NULL) AND (sum='" + sum + "' OR sum='" + (int) sum + "') LIMIT 1;");
                try {
                    rs.next();
                    return new BTCCheck(
                            rs.getLong("id"),
                            rs.getString("link"),
                            rs.getString("type"),
                            rs.getString("status"),
                            rs.getString("recipient") == null ? 0 : (rs.getString("recipient").equals("") ? 0 : rs.getLong("recipient")),
                            rs.getDouble("sum"));
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new BTCCheck();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new BTCCheck();
            }
        } else {
            return new BTCCheck();
        }
    }

    public synchronized BotUser getBotUserFromChatId(Long chatId) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT * FROM users WHERE chat_id='" + chatId + "' LIMIT 1;");
                try {
                    rs.next();
                    BotUser tempBotUser = new BotUser(
                            rs.getLong("id"),
                            rs.getDouble("btc_balance"),
                            rs.getString("reg_datetime"),
                            rs.getInt("approved_admi_actions"),
                            rs.getInt("jumps_from_link"),
                            rs.getInt("jumps_from_list"),
                            rs.getLong("total_admi_actions"),
                            rs.getLong("chat_id"));
                    return tempBotUser;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public synchronized ArrayList<BotUser> getAllBotUsers() {
        Statement stmt = createStatementMy();
        ArrayList<BotUser> botUsers = new ArrayList<>();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                rs = stmt.executeQuery("SELECT * FROM users;");
                while (rs.next()) {
                    botUsers.add(new BotUser(
                            rs.getLong("id"),
                            rs.getDouble("btc_balance"),
                            rs.getString("reg_datetime"),
                            rs.getInt("approved_admi_actions"),
                            rs.getInt("jumps_from_link"),
                            rs.getInt("jumps_from_list"),
                            rs.getLong("total_admi_actions"),
                            rs.getLong("chat_id")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
        }
        return botUsers;
    }

    public synchronized boolean updateBtcCheckStatusWhereCheckId(long checkIdInDb, String status) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE btc_checks set status = '" + status + "' where id='" + checkIdInDb + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized boolean updateDeferredMsgStatus(long defMsgId, String status) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE deferred_msgs set status = '" + status + "' where id='" + defMsgId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized Boolean checkChatIdInDefMsg(long defMsgId, long chatId) {
        String chatIdsInDefMsg;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT received_chat_ids FROM deferred_msgs WHERE id='" + defMsgId + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    chatIdsInDefMsg = rs.getString("received_chat_ids");
                    rs.close();
                    stmt.close();
                    if (chatIdsInDefMsg.contains(String.valueOf(chatId))) {
                        return true;
                    } else {
                        return false;
                    }
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized String getDefMsgSum(long defMsgId) {
        String bonus_sum;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT bonus_sum FROM deferred_msgs WHERE id='" + defMsgId + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    bonus_sum = rs.getString("bonus_sum");
                    rs.close();
                    stmt.close();
                    return bonus_sum;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public synchronized boolean addRecivedChatIdToDeferredMsg(long defMsgId, Long chatId) {
        Statement stmt = createStatementMy();
        if (stmt != null) {
            String sql;
            sql = "UPDATE deferred_msgs set received_chat_ids = CONCAT(received_chat_ids, '" + chatId + ",') where id='" + defMsgId + "';";
            try {
                stmt.executeUpdate(sql);
                stmt.close();
                this.connectionToDB.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    public synchronized String getUserPersentFromActionID(long actionId) {
        String lastDate;
        Statement stmt = createStatementMy();
        if (stmt != null) {
            ResultSet rs = null;
            try {
                String sql = "SELECT pay_rate FROM admitadactions WHERE admitad_id='" + actionId + "'";
                rs = stmt.executeQuery(sql);
                rs.next();
                try {
                    lastDate = rs.getString("pay_rate");
                    rs.close();
                    stmt.close();
                    return lastDate;
                } catch (PSQLException e) {
                    rs.close();
                    stmt.close();
                    return null;
                }
            } catch (SQLException e) {
                System.out.println("Ошибка sql запроса");
                e.printStackTrace();
                return null;
            }
        } else {
            System.out.println("Ошибка подключения к БД");
            return null;
        }
    }

    public void createTables() {

        Connection c;
        Statement stmt;

        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://localhost:5433/testdb", "pguser", "pguser");
            c.setAutoCommit(false);
            System.out.println("-- Opened database successfully");
            String sql;

            //-------------- CREATE TABLE ---------------
            stmt = c.createStatement();


            //--------------- INSERT ROWS ---------------
            stmt = c.createStatement();
            sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (1, 'Paul', 32, 'California', 20000.00 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
            stmt.executeUpdate(sql);

            sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            System.out.println("-- Records created successfully");


            //-------------- UPDATE DATA ------------------
            stmt = c.createStatement();
            sql = "UPDATE COMPANY set SALARY = 25000.00 where ID=1;";
            stmt.executeUpdate(sql);
            c.commit();
            stmt.close();

            System.out.println("-- Operation UPDATE done successfully");


            //--------------- SELECT DATA ------------------
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM COMPANY;");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int age = rs.getInt("age");
                String address = rs.getString("address");
                float salary = rs.getFloat("salary");
                System.out.println(String.format("ID=%s NAME=%s AGE=%s ADDRESS=%s SALARY=%s", id, name, age, address, salary));
            }
            rs.close();
            stmt.close();
            c.commit();
            System.out.println("-- Operation SELECT done successfully");


            //-------------- DELETE DATA ----------------------
            stmt = c.createStatement();
            sql = "DELETE from COMPANY where ID=2;";
            stmt.executeUpdate(sql);
            c.commit();
            stmt.close();
            System.out.println("-- Operation DELETE done successfully");


            c.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("-- All Operations done successfully");

    }

    public static DBPg getInstance() {
        return INSTANCE;
    }
}
