package ru.zz.telegrambot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.zz.db.DataBaseHelper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class.getName());

    public static void main( String[] args ){

/*        StringBuilder sb = new StringBuilder();

         DataBaseHelper.getInstance().getToken().ifPresent(e->{
            sb.append("https://api.telegram.org/bot").append(e).append("/");}
        );*/

     //   DataBaseHelper.getInstance().getToken().map(Main::getUrl).
       // sb.append("https://api.telegram.org/bot427644232:AAFeylSqK8Q4KHKcAcVwfNejp2DaUPNR8-M/");
     //   String telegramApiUrl = sb.toString();
        String telegramApiUrl = "https://api.telegram.org/bot719532567:AAFk594zjt0SyefoIR47e-1HtEQTzyCaFq0/";


        if (telegramApiUrl.length() == 0) {
            logger.error("Telegram api url doesn't set! Start is failed!");
            System.exit(0);
        }

        ScheduledExecutorService scheduleEx = Executors.newSingleThreadScheduledExecutor();
        scheduleEx.scheduleAtFixedRate(new MessagesChecker(telegramApiUrl), 0, 2000, TimeUnit.MILLISECONDS);
    }

    private static String getUrl(String s){
        StringBuilder sb = new StringBuilder();
        sb.append("https://api.telegram.org/bot").append(s).append("/");
        return sb.toString();
    }
}
