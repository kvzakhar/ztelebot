package ru.zz.db;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import ru.zz.rent.LastIndicationsHolder;
import ru.zz.rent.PrimaryLightHolder;
import ru.zz.rent.PrimaryWaterHolder;
import static ru.zz.telegrambot.BotHelper.objectMapper;

import java.io.IOException;
import java.util.Optional;

public class DataBaseHelper {
    private static final Logger logger = LogManager.getFormatterLogger(DataBaseHelper.class.getName());

    private MongoClient mongoClient;
    private MongoDatabase db;

    private DataBaseHelper(){
        this.mongoClient = new MongoClient();
        this.db = this.mongoClient.getDatabase("z_advicer_bot");
    };

    public static DataBaseHelper getInstance(){
        return LazyDbHolder.instance;
    };

    public void closeMongoClient() {
        this.mongoClient.close();
    }

    private static class LazyDbHolder {
        public static DataBaseHelper instance = new DataBaseHelper();
    }

    public Optional<String> getToken(){
        Optional<String> token = Optional.empty();

        try {
            token = Optional.ofNullable(this.db.getCollection("bot_data").
                    find(Filters.eq("type", "telegram_api")).first().getString("token"));
        }catch(Exception e){
            logger.error("Can't get token! Error: %s", e.getMessage(), e);
        }

        return token;
    }

    public <T> T getFirstValue(String collection, String field, Bson filter){
        T result = null;
        Optional<Document> document = Optional.ofNullable(this.db.getCollection(collection).find(filter).first());
        if(document.isPresent())
            result = (T)document.get().get(field);

        return result;
    }

    public <T> T getFirstDocByFilter(String collection, Bson filer){
        T result = null;

        Optional<T> doc =
                (Optional<T>) Optional.ofNullable(this.db.getCollection(collection).find(filer).first());
        if (doc.isPresent()) {
            result = doc.get();
        }
        return result;
    }

    public <T> boolean updateField(String collection, String chatId, String field, T value){
        boolean status = true;

        try {
            Document toUpdate = this.db.getCollection(collection).find(Filters.eq("id_chat", chatId)).first();
            this.db.getCollection(collection).updateOne(toUpdate, new Document("$set", new Document(field, value)));
        }catch (Exception e){
            status = false;
            logger.error("Can't update %s at %s! Error: %s", field, collection, e.getMessage(), e);
        }

        return status;
    }

    public <T> boolean insertPrimaryCounters(T obj, String field, String chatId, String owner) {
        boolean status = true;
        boolean primarySet = false;

        try {
            Optional<Document> primaries = Optional.ofNullable(
                    this.db.getCollection("rent_const").find(Filters.eq("id_chat", chatId)).first());

            if(primaries.isPresent()){
                this.db.getCollection("rent_const").updateOne(
                        this.db.getCollection("rent_const").find(Filters.eq("id_chat", chatId)).first(),
                        new Document("$set", new Document(field, obj))
                );

                primarySet = (primaries.get().get("light") != null
                        && primaries.get().get("rent_amount") != null && primaries
                        .get().get("water") != null);
            } else {
                this.db.getCollection("rent_const").
                        insertOne(new Document(field, obj).append("id_chat", chatId).append("owner", owner));
            }

            if (primarySet) {
                primaries = Optional.ofNullable(this.db.getCollection("rent_const")
                        .find(Filters.eq("id_chat", chatId)).first());

                LastIndicationsHolder lastIndications = new LastIndicationsHolder();
                try {
                    lastIndications.setLight(objectMapper.readValue(
                            (String) primaries.get().get("light"), PrimaryLightHolder.class).getIndications());
                    lastIndications.setColdWater(objectMapper.readValue(
                            (String) primaries.get().get("water"), PrimaryWaterHolder.class).getColdWater());
                    lastIndications.setHotWater(objectMapper.readValue(
                            (String) primaries.get().get("water"), PrimaryWaterHolder.class).getHotWater());

                    this.db.getCollection("rent_const").updateOne(
                            this.db.getCollection("rent_const").find(Filters.eq("id_chat", chatId)).first(),
                            new Document("$set", new Document("last_indications",
                                    objectMapper.writeValueAsString(lastIndications))));

                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }catch (Exception e){
            status = false;
            logger.error("Can't insert primary values for %s! Error: %s", e.getMessage(), field, e);

        }

        return status;
    }

    public boolean existRentUser(String chatId){
        boolean result = false;

        Optional<Document> document = Optional.ofNullable(
                this.db.getCollection("rent_const").find(Filters.eq("id_chat", chatId)).first());

        if(document.isPresent()){

        } else{

        }

        return result;
    }

}