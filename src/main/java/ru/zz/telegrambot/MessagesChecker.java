package ru.zz.telegrambot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.model.Filters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import ru.zz.api.KeyboardButton;
import ru.zz.api.ReplyKeyboardHide;
import ru.zz.api.ReplyKeyboardMarkup;
import ru.zz.commands.AbstractCommand;
import ru.zz.commands.en.SetPrimaryCountersEnCommand;
import ru.zz.commands.en.SetPrimaryLightEnCommand;
import ru.zz.commands.ru.SetPrimaryCountersRusCommand;
import ru.zz.commands.StartCommand;
import ru.zz.commands.en.SetPrimaryWaterEnCommand;
import ru.zz.commands.ru.SetPrimaryLightRusCommand;
import ru.zz.commands.ru.SetPrimaryWaterRusCommand;
import ru.zz.db.DataBaseHelper;
import ru.zz.jackson.*;
import ru.zz.rent.PrimaryLightHolder;
import ru.zz.rent.PrimaryWaterHolder;
import ru.zz.rent.WaterHolder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static ru.zz.telegrambot.BotHelper.*;

class MessagesChecker implements Runnable{
    private static final Logger logger = LogManager.getFormatterLogger(MessagesChecker.class.getName());

    private String telegramApiUrl;

    protected MessagesChecker(String telegramApiUrl) {
        this.telegramApiUrl = telegramApiUrl;
    }

    @Override
    public void run() {
        callApiGet("getUpdates", this.telegramApiUrl).ifPresent(
            response->{
               Optional<IncomeMessage> message = Optional.empty();
               try {
                   System.out.println(response);
                   message = Optional.ofNullable(objectMapper.readValue(response, IncomeMessage.class));
               }catch(Exception e){
                   logger.error(e.getMessage(), e);
               }

               boolean status = message.isPresent() ? message.get().getOk() : false;

               if(status){
                  // System.out.println(message.get().getResult().get(0).getUpdateId());
                  Response resp = parseMessage(message.get());
                  resp.getResponses().forEach(r->{
                     try {
                         StringBuilder sb = new StringBuilder();
                             sb.append("sendMessage?chat_id=").append(r.getChatId())
                             .append("&parse_mode=HTML&text=").append(URLEncoder.encode(r.getResponseMessage(), "UTF-8"));
                             if (r.isNeedReplyMarkup()) {
                                 sb.append("&reply_markup=").append(r.getReplyMarkup());
                             }
                            //response
                             callApiGet(sb.toString(), this.telegramApiUrl);
                      } catch (Exception e1) {
                         logger.error(e1.getMessage(), e1);
                      }
                  });
                  //confirm last update
                  if (null != resp.getMaxUpdateId()) {
                     callApiGet("getUpdates?offset="+ (resp.getMaxUpdateId() + 1) + "", this.telegramApiUrl);
                  }
               }
            }
        );
    }

    private Response parseMessage(IncomeMessage message){
        List<Result> results = message.getResult();
        Response response = new Response();
        Integer updateId = null;
        String chatId;
        String owner = "";

         for(Result result : results){
             ResponseHolder rh = new ResponseHolder();
             boolean isRus;
             try {
                 updateId = result.getUpdateId();
                 Optional<Message> messageObj = Optional.ofNullable(result.getMessage());
                 if(messageObj.isPresent()){
                     Message msg = messageObj.get();
                     String typeCommand =  "simple_message";
                     List<Entity> entities = msg.getEntities();

                     if (entities.size() > 0) {
                         typeCommand = entities.get(0).getType();
                     }

                     Chat chat = msg.getChat();
                     chatId = String.valueOf(chat.getId());
                     rh.setChatId(chatId);

                     owner = chat.getFirstName() + " " + chat.getLastName();
                     String text = parseText(msg.getText());

                     boolean isMapper = false;
                     isRus = commandMapperRus.containsKey(text);

                     if (commandMapper.containsKey(text) || commandMapperRus.containsKey(text)) {
                         isMapper = true;
                     }

                     System.out.println(text + " " + typeCommand + " ");
                     String answer = (isRus) ? "Я не могу ответить на этот вопрос" : "I can't answer at this question";

                     if(typeCommand.equalsIgnoreCase("bot_command") || isMapper){
                         if (isMapper) {
                             if (isRus) {
                                 text = commandMapperRus.get(text);
                             } else {
                                 text = commandMapper.get(text);
                             }
                         }
                         activeCommand.remove(chatId);
                         chatObjectMapper.remove(chatId);
                         activeCommand.put(chatId, new CommandHolder(text, isRus));

                         AbstractCommand command;

                         switch (text){
                             case "/start":{
                                 command = new StartCommand();
                                 answer = command.getText();
                                 rh.setNeedReplyMarkup(command.getNeedReplyMarkup());
                                 rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(command.getCommandButtons())));
                             }
                                break;
                             case "/rent": {
                                 answer = getRentMenu(chatId, isRus, rh);
                             }
                                 break;
                             case "/setprimarycounters": {
                                 defaultPrimaryButtons(rh, isRus);
                                 command = isRus ? new SetPrimaryCountersRusCommand() : new SetPrimaryCountersEnCommand();
                                 answer = command.getText();
                             }
                                break;
                             case "/setprimarywater":{
                                 chatObjectMapper.put(chatId, new PrimaryWaterHolder());
                                 command = isRus ? new SetPrimaryWaterRusCommand() : new SetPrimaryWaterEnCommand();
                                 answer = command.getText();
                                 rh.setNeedReplyMarkup(command.getNeedReplyMarkup());
                                 rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(command.getCommandButtons())));
                             }
                                break;
                             case "/changehwrate": {
                                 hideKeybord(rh);
                                 answer = (isRus) ? "Для изменения тарифа горячей воды отправьте новое значение"
                                         : "For change hot water rate send simple message with new hot water rate";
                             }
                                break;
                             case "/changecwrate": {
                                 hideKeybord(rh);
                                 answer = (isRus) ? "Для изменения тарифа холодной воды отправьте новое значение"
                                         : "For change cold water rate send simple message with new cold water rate";
                             }
                                break;
                             case "/setprimarylight": {
                                 chatObjectMapper.put(chatId, new PrimaryLightHolder());
                                 command = isRus ? new SetPrimaryLightRusCommand() : new SetPrimaryLightEnCommand();
                                 rh.setNeedReplyMarkup(command.getNeedReplyMarkup());
                                 rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(command.getCommandButtons())));
                                 answer = command.getText();

                             }
                                break;
                             default:
                                 break;
                         }
                     } else {
                         //System.out.println(text + activeCommand.get(chatId).getCommand());
                         if(activeCommand.containsKey(chatId)){
                             answer = processSimpleMessages(chatId, "", text, rh, isRus);
                         }
                     }
                     rh.setResponseMessage(answer);
                     response.getResponses().add(rh);
//                     System.out.println(typeCommand + " " + chatId + " " + text);
                 }
             }
             catch(Exception e){
                 //logger.error(e.getMessage(), e);
                 e.printStackTrace();
             }
         }
         response.setMaxUpdateId(updateId);
         return response;
    }

    private String processSimpleMessages(String chatId, String owner, String text, ResponseHolder rh, boolean isRus) {
        System.out.println("processSimpleMessages " + chatId + " " +text );
        String answer = null;
        boolean isRemoveCommand = true;

        String command = activeCommand.get(chatId).getCommand();

        switch (command){
            case "/changehwrate": {
                Double value;
                try {
                    value = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                   // isRemoveCommand = false;
                    logger.error(e.getMessage(), e);
                    return NaN(isRus);
                }

                try {
                    PrimaryWaterHolder waterHolder = objectMapper.readValue((String) DataBaseHelper.getInstance().
                            getFirstValue("rent_const", "water", Filters.eq("id_chat", chatId)), PrimaryWaterHolder.class);
                    waterHolder.setHotWaterRate(value);

                    if (DataBaseHelper.getInstance().
                            updateField("rent_const", chatId, "water", objectMapper.writeValueAsString(waterHolder))) {
                        rh.setNeedReplyMarkup(true);
                        rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(cacheButtons.get(chatId))));
                        answer = (isRus) ? "Тариф горячей воды изменен" : "Hot water rate updated successfully!";
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    e.printStackTrace();
                }

            }
                break;
            case "/setprimarywater": {
                System.out.println("PSM : /setprimarywater");
                isRemoveCommand = false;

                PrimaryWaterHolder pwh = (PrimaryWaterHolder) chatObjectMapper.get(chatId);
                String typeOfWater = pwh.getTypeOfWater();

                if(null == typeOfWater){//если тип воды не задан
                    //если есть данные
                    if(false){

                    }else {
                        Integer existCounters;
                        switch (text) {
                            case "hot":
                            case "горячая":
                                existCounters = pwh.getCountColdWaterCounter();
                                pwh.setTypeOfWater(text);
                                break;
                            case "cold":
                            case "холодная":
                                existCounters = pwh.getCountHotWaterCounter();
                                pwh.setTypeOfWater(text);
                                break;
                            default: {
                                activeCommand.remove(chatId);
                                defaultPrimaryButtons(rh, isRus);
                                return (isRus) ? "Неверный формат! Вода может быть горячей или холодной. Задайте начальные значения заново"
                                        : "Wrong format! Water can be hot or cold. Set primary water indications and rates again";
                            }
                        }

                        StringBuilder sb = (isRus) ?
                                new StringBuilder("Сколько у вас счетчиков? (Максимум: 5)")
                                : new StringBuilder("What number of counters for ")
                                .append(text).append(" water you have? (Maximum: 5)");

                        if (null != existCounters) {
                            List<List<String>> buttons = new ArrayList<>();
                            if (existCounters == 0) {
                                ++existCounters;
                            }
                            buttons.add(getButtonsList(existCounters.toString()));
                            rh.setNeedReplyMarkup(true);
                            try {
                                rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(buttons)));
                            } catch (JsonProcessingException e) {
                                logger.error(e.getMessage(), e);
                            }
                            answer = (isRus) ?
                               sb.append("\n\nВозможно у вас ").append(existCounters).append(" счетчик(а)?").toString()
                               : sb.append("\n\nMaybe you have ")
                                    .append(existCounters).append(" counter of ").append(text).append(" water").toString();
                        } else {
                            answer = sb.toString();
                            hideKeybord(rh);
                        }
                    }
                }else{
                    if(pwh.isWaterSet()){
                        System.out.println("Water is set");
                        switch (typeOfWater){
                            case "горячая":
                            case "hot": {
                                if ((null == pwh.getHotWater() || pwh.getHotWater().size()
                                        <= pwh.getCountHotWaterCounter()) && !pwh.isWaitValue()) {

                                    Integer counter = null;
                                    Double value = null;

                                    try {
                                        StringBuilder sb = new StringBuilder();

                                        if (null != pwh.getHotWater()) {
                                            pwh.getHotWater().entrySet().forEach(e -> {
                                                if (text.equalsIgnoreCase(e.getValue().getAlias())) {
                                                    sb.append(e.getKey());
                                                }
                                            });
                                        }
                                        if (pwh.getCountHotWaterCounter() > 1) {
                                            counter = (sb.toString().length() > 0) ? Integer.valueOf(sb.toString()) : Integer.valueOf(text);
                                        } else {
                                            // one-tariff counter
                                            try {
                                                value = Double.valueOf(text);
                                            } catch (NumberFormatException e) {
                                                return NaN(isRus);
                                            }
                                        }

                                        if (pwh.getCountHotWaterCounter() > 1 && counter > pwh.getCountHotWaterCounter()) {
                                            return (isRus) ? "Неверный формат! Используйте кнопки ниже для выбора счетчика"
                                                    : "Wrong format! Try again! Use buttons below to choose counter";
                                        }
                                    } catch (NumberFormatException e) {
                                        return (isRus) ? "Неверный формат! Используйте кнопки ниже для выбора счетчика"
                                                : "Wrong format! Try again! Use buttons below to choose counter";
                                    }

                                    pwh.setHotWater();

                                    if (pwh.getCountHotWaterCounter() > 1) {

                                    }else {
                                        pwh.getHotWater().put(1, new WaterHolder(typeOfWater));
                                        pwh.getHotWater().get(1).setPrimaryIndication(value);

                                        pwh.setWaterSet(false);
                                        pwh.setTypeOfWater(null);

                                        List<List<String>> buttons = new ArrayList<>();
                                        if (isRus) {
                                            buttons.add(getButtonsList(getEmoji("E29C85") + " "	+ "горячая", "холодная"));
                                            buttons.add(getButtonsList("главное меню"));
                                        } else {
                                            buttons.add(getButtonsList(getEmoji("E29C85") + " " + "hot", "cold"));
                                            buttons.add(getButtonsList("main menu"));
                                        }

                                        rh.setNeedReplyMarkup(true);
                                        try {
                                            rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(buttons)));
                                        } catch (JsonProcessingException e) {
                                            logger.error(e.getMessage(), e);
                                        }

                                        answer = (isRus) ? "Показания горячей воды заданы успешно"	: "Indications for hot water set successfully";
                                    }
                                }
                            }
                            break;
                            case "холодная":
                            case "cold":{

                            }
                            break;
                            default:
                                break;
                        }

                    } else{
                        if(pwh.isRatesSet()){

                        }else{
                            System.out.println("No rates set");
                            int countCounters = Integer.valueOf(text);

                            try {
                                if (countCounters < 0) {
                                    return (isRus) ? "Количество счетчиков горячей воды не может быть отрицательным (минимум 0)"
                                            : "Number of counters can not be negative";
                                }

                                if(countCounters == 0){

                                }

                                if (countCounters > 5) {
                                    countCounters = 5;
                                }

                                StringBuilder sb = (isRus) ? new StringBuilder("У вас ").append(countCounters).append(
                                        " счетчика.") : new StringBuilder("Ok. You have ").append(countCounters)
                                        .append(" counter of ").append(pwh.getTypeOfWater()).append(" water. ");

                                if(countCounters > 1){
                                    List<List<String>> buttons = new ArrayList<>();

                                    for (int i = 0; i < countCounters; i++) {
                                        buttons.add(getButtonsList(String.valueOf(i + 1)));
                                    }

                                    if (isRus) {
                                        buttons.add(getButtonsList("назад"));
                                        buttons.add(getButtonsList("главное меню"));
                                    } else {
                                        buttons.add(getButtonsList("back"));
                                        buttons.add(getButtonsList("main menu"));
                                    }

                                    cacheButtons.put(chatId, buttons);

                                    rh.setNeedReplyMarkup(true);
                                    rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(buttons)));

                                    answer = sb.append((isRus) ? " Используйте кнопки ниже для задания значений"
                                            : "Please use buttons below to set value").toString();
                                }else {
                                    hideKeybord(rh);
                                    answer = (isRus) ? "Задайте начальное значение"	: sb.append("Please set value for it").toString();
                                }
                            }catch(Exception e){
                                logger.error(e.getMessage(), e);
                                return NaN(isRus);
                            }

                            switch (typeOfWater) {
                                case "hot":
                                case "горячая":
                                    pwh.setCountHotWaterCounter(countCounters);
                                    break;
                                case "cold":
                                case "холодная":
                                    pwh.setCountColdWaterCounter(countCounters);
                                    break;
                                default:
                                    break;
                            }

                            pwh.setWaterSet(true);
                        }
                    }
                }
            }
                break;
            case "/setprimarylight": {
                isRemoveCommand = false;
                PrimaryLightHolder lightObj = (PrimaryLightHolder) chatObjectMapper.get(chatId);

                if(null == lightObj.getTariffType()){
                    try {
                        lightObj.setTariffType(Integer.valueOf(text));
                    } catch (NumberFormatException e) {
                        return (isRus) ? "Не верный тип счетчика. Используйте кнопки ниже"
                                : "Bad type of counter! Use buttons below";
                    }

                    switch (text){
                        case "1":{
                            hideKeybord(rh);
                            answer = (isRus) ? "У вас однотарифный счетчик. Задайте значение"
                                    : "Ok. You have one-tariff counter. Send simple message with value";
                        }
                        break;
                        case "2":{
                            List<List<String>> buttons = new ArrayList<>();

                            if (isRus) {
                                buttons.add(getButtonsList(PrimaryLightHolder.PeriodsRus.ДЕНЬ.name().toLowerCase(),
                                        PrimaryLightHolder.PeriodsRus.НОЧЬ.name().toLowerCase()));
                                buttons.add(getButtonsList("главное меню"));
                            } else {
                                buttons.add(getButtonsList(PrimaryLightHolder.Periods.DAY.name().toLowerCase(),
                                        PrimaryLightHolder.Periods.NIGHT.name().toLowerCase()));
                                buttons.add(getButtonsList("main menu"));
                            }

                            cacheButtons.put(chatId, buttons);

                            try {
                                rh.setNeedReplyMarkup(true);
                                rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(buttons)));
                            } catch (JsonProcessingException e) {
                                logger.error(e.getMessage(), e);
                            }
                            answer = (isRus) ? "У вас двухтарифный счетчик. Выберите период для задания начального значения"
                                    : "Ok. You have two-tariff counter. Set primary value for it. Please use button below";
                        }
                        break;
                        case "3":{

                        }
                        break;
                        default:
                            lightObj.setTariffType(null);
                            return (isRus) ? "Не верный тип счетчика. Используйте кнопки ниже"	:
                                    "Bad type of counter! Use buttons below";
                    }
                }
                else{//Задаем значение счетчика
                    Integer tariffType = lightObj.getTariffType();
                    Map<String, Double> rates = lightObj.getRates();

                    if (tariffType > 1) {
                        List<String> periodsStr = new ArrayList<>();

                        if (isRus) {
                            PrimaryLightHolder.PeriodsRus[] periods = PrimaryLightHolder.PeriodsRus.values();
                            for (PrimaryLightHolder.PeriodsRus period : periods) {
                                periodsStr.add(period.name().toLowerCase());
                            }
                        } else {
                            PrimaryLightHolder.Periods[] periods = PrimaryLightHolder.Periods.values();
                            for (PrimaryLightHolder.Periods period : periods) {
                                periodsStr.add(period.name().toLowerCase());
                            }
                        }

                        if (periodsStr.contains(text)) {
                            lightObj.setPeriod(text);
                            hideKeybord(rh);
                            if (isRus) {
                                return (lightObj.getRates() == null) ?
                                        "Задайте начальное значение для периода ".concat(text)
                                        : "Задайте значение тарифа для периода ".concat(text);
                            } else {
                                return (lightObj.getRates() == null) ?
                                   "Ok. Set start indication value for " + text +" period" : "Ok. Set rate value for " + text + " period";
                            }
                        }
                    }

                    if (rates == null && (null != lightObj.getPeriod() || tariffType == 1)) {
                        switch (lightObj.getTariffType()){
                            case 1:{
                                try {
                                    hideKeybord(rh);
                                    lightObj.getIndications().put("t1",	Double.valueOf(text));
                                    answer = (isRus) ? "Начальные показания электороэнергии заданы успешно. Теперь необходимо задать тариф"
                                            : "Primary start indications set successfully! Please set rate via simple send message with value.";
                                    lightObj.initRates();
                                } catch (NumberFormatException e) {
                                    logger.error(e.getMessage(), e);
                                    return NaN(isRus);
                                }
                            }
                                break;
                            case 2:
                            case 3:
                                break;
                            default:
                                break;
                        }
                    } else if(null != rates && (null != lightObj.getPeriod() || tariffType == 1)){
                        System.out.println("Rates : " + text + " " + rates.toString());
                        switch (lightObj.getTariffType()){
                            case 1:{
                                try {
                                    lightObj.getRates().put("t1", Double.valueOf(text));
                                    answer = (isRus) ?
                                            "Начальный показания и тарифы электроэнергии заданы успешно"
                                            : "Primary indications and rate for light set successfully!";
                                    cacheButtons.remove(chatId);

                                    try {
                                        System.out.println(objectMapper.writeValueAsString(lightObj));
                                        DataBaseHelper.getInstance().insertPrimaryCounters(
                                                objectMapper.writeValueAsString(lightObj),"light", chatId, owner);
                                    } catch (JsonProcessingException e) {
                                        logger.error(e.getMessage(), e);
                                    }

                                    defaultPrimaryButtons(rh, isRus);
                                    chatObjectMapper.remove(chatId);
                                    isRemoveCommand = true;
                                } catch (NumberFormatException e) {
                                    logger.error(e.getMessage(), e);
                                    return NaN(isRus);
                                }
                            }
                                break;
                            case 2:
                            case 3: {

                            }
                                break;
                            default:
                                break;
                        }
                    } else {
                        return (isRus) ? "Выбран не верный период. Используйте кнопки ниже"	: "Wrong period! Use buttons below";
                    }

                }
            }
                break;
            default:
                break;
             //   return (isRus) ? "Я не могу ответить на этот вопрос" : "I can't answer at this question";
        }

        if (isRemoveCommand) {
            activeCommand.remove(chatId);
        }
        return answer;
    }

    private String NaN(boolean isRus) {
        return (isRus) ? "Неверный формат! Значение должно быть числом. Попробуйте снова"
                : "Wrong format! Value must be a number! Try again";
    }

    private static String parseText(String text){
        if (null == text) {
            text = "";
        }
        if (text.contains(getEmoji("E29C85"))) {
            text = text.replace(getEmoji("E29C85"), "");
        }

        text = text.trim().toLowerCase();

        if (text.contains(",")) {
            text = text.replace(",", ".");
        }

        return text;
    }

    private List<String> getButtonsList(String... buttonsNames) {
        return Arrays.asList(buttonsNames);
    }

    private ReplyKeyboardMarkup getButtons(List<List<String>> buttonNames) {

        List<List<KeyboardButton>> keyButtons = new ArrayList<>();

        ReplyKeyboardMarkup rkm = new ReplyKeyboardMarkup();
        rkm.setResize_keyboard(true);
        rkm.setOne_time_keyboard(false);
        rkm.setKeyboard(keyButtons);

/*        buttonNames.stream().forEach(name -> {
            List<KeyboardButton> buttons = new ArrayList<>();
            name.stream().forEach(e -> {
                KeyboardButton key = new KeyboardButton();
                key.setText(e);
                buttons.add(key);
            });
            keyButtons.add(buttons);
        });*/

        keyButtons.add(buttonNames.stream().flatMap(List::stream).map(KeyboardButton::new).collect(Collectors.toList()));

        return rkm;
    }

    private String getRentMenu(String chatId, boolean isRus, ResponseHolder rh){
        String answer = null;
        cacheButtons.remove(chatId);
        List<List<String>> buttons = new ArrayList<>();

        if(DataBaseHelper.getInstance().existRentUser(chatId)){

        } else{
            if(isRus){
                buttons.add(getButtonsList("начальные показания"));
                answer = "Задайте начальные показания для доступа ко всем функциям.\n\nИспользуйте команду:\n\n"
                        + "<b>начальные показания</b> - задать начальные показания счетчиков и стоимость аренды";
            }else{
                buttons.add(getButtonsList("new primary"));

                answer = "For access to all functions for control your rent you must set primary counters. Please use this command:\n"
                        + "new primary (/setprimarycounters)- set starting indications";
            }
        }
        rh.setNeedReplyMarkup(true);

        try {
            rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(buttons)));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }

/*        if (rentData.containsKey(id)) {
            rentData.remove(id);
        }*/

        return answer;
    }

    private void defaultPrimaryButtons(ResponseHolder rh, boolean isRus) {

        List<List<String>> buttons = new ArrayList<>();
        Optional<Document> document = Optional.ofNullable(DataBaseHelper
                .getInstance().getFirstDocByFilter("rent_const", Filters.eq("id_chat", rh.getChatId())));

        String light;
        String water;
        String rentAmount;

        if (isRus) {
            light = "свет";
            water = "вода";
            rentAmount = "арендная плата";
        } else {
            light = "set light";
            water = "set water";
            rentAmount = "set rent amount";
        }

        if (document.isPresent()) {
            if (null != document.get().get("light")) {
                light = getEmoji("E29C85") + " " + light;
            }

            if (null != document.get().get("water")) {
                water = getEmoji("E29C85") + " " + water;
            }

            if (null != document.get().get("rent_amount")) {
                rentAmount = getEmoji("E29C85") + " " + rentAmount;
            }
        }

        buttons.add(getButtonsList(water, rentAmount, light));
        buttons.add(getButtonsList((isRus) ? "главное меню" : "main menu"));

        try {
            rh.setNeedReplyMarkup(true);
            rh.setReplyMarkup(objectMapper.writeValueAsString(getButtons(buttons)));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void hideKeybord(ResponseHolder rh) {
        rh.setNeedReplyMarkup(true);
        try {
            rh.setReplyMarkup(objectMapper.writeValueAsString(new ReplyKeyboardHide()));
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage());
        }
    }
}
