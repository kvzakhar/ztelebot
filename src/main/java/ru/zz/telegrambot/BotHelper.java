package ru.zz.telegrambot;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BotHelper {
    private static final Logger logger = LogManager.getLogger(BotHelper.class.getName());
    public static final ObjectMapper objectMapper = new ObjectMapper();

  //  public static ConcurrentMap<String, Optional<RentHolder>> rentData = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, CommandHolder> activeCommand = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, String> commandMapper = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, String> commandMapperRus = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, Object> chatObjectMapper = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, List<List<String>>> cacheButtons = new ConcurrentHashMap<>();

    static {
        commandMapper.put("new primary", "/setprimarycounters");
        commandMapper.put("set light", "/setprimarylight");
        commandMapper.put("set water", "/setprimarywater");
        commandMapper.put("set rent amount", "/setprimaryrentamount");
        commandMapper.put("water", "/setwater");
        commandMapper.put("rent", "/rent");
        commandMapper.put("main menu", "/rent");
        commandMapper.put("calc", "/calc");
        commandMapper.put("home", "/start");
        commandMapper.put("hot water", "/changehwrate");
        commandMapper.put("cold water", "/changecwrate");
        commandMapper.put("outfall", "/changeoutfallrate");
        // russian language
        commandMapperRus.put("начальные показания", "/setprimarycounters");
        commandMapperRus.put("свет", "/setprimarylight");
        commandMapperRus.put("вода", "/setprimarywater");
        commandMapperRus.put("арендная плата", "/setprimaryrentamount");
        commandMapperRus.put("аренда", "/rent");
        commandMapperRus.put("главное меню", "/rent");
        commandMapperRus.put("рассчитать", "/calc");
        commandMapperRus.put("домой", "/start");
        commandMapperRus.put("горячая вода", "/changehwrate");
        commandMapperRus.put("холодная вода", "/changecwrate");
        commandMapperRus.put("водоотвод", "/changeoutfallrate");
    }


    public BotHelper(){}

    public static Optional<String> callApiGet(String method, String url){
        Optional<String> result = Optional.empty();

        BufferedReader in = null;

        try{
            System.setProperty("https.proxyHost", "67.20.79.33");
            System.setProperty("https.proxyPort","443");

            URL address = new URL(url+method);
            HttpsURLConnection con = (HttpsURLConnection) address.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();

            if(responseCode == 200){
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                result = Optional.of(response.toString());
            } else{
                System.out.println(responseCode);
            }


        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }finally {
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return result;
    }


    public static String getEmoji(String hexCode) {
        String done = null;
        try {
            done = new String(Hex.decodeHex(hexCode.toCharArray()), "UTF-8");
        } catch (UnsupportedEncodingException | DecoderException e) {
            logger.error(e.getMessage(), e);
        }
        return done;
    }

}
