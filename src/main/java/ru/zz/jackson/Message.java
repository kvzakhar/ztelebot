package ru.zz.jackson;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "message_id",
        "from",
        "chat",
        "date",
        "text",
        "entities"
})

public class Message {
    @JsonProperty("message_id")
    private Integer messageId;
    @JsonProperty("from")
    private From from;
    @JsonProperty("chat")
    private Chat chat;
    @JsonProperty("date")
    private Integer date;
    @JsonProperty("text")
    private String text;
    @JsonProperty("entities")
    private List<Entity> entities = new ArrayList<>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("message_id")
    public Integer getMessageId() {
        return messageId;
    }

    @JsonProperty("message_id")
    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }

    @JsonProperty("from")
    public From getFrom() {
        return from;
    }

    @JsonProperty("from")
    public void setFrom(From from) {
        this.from = from;
    }

    @JsonProperty("chat")
    public Chat getChat() {
        return chat;
    }

    @JsonProperty("chat")
    public void setChat(Chat chat) {
        this.chat = chat;
    }

    @JsonProperty("date")
    public Integer getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(Integer date) {
        this.date = date;
    }

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("entities")
    public List<Entity> getEntities() {
        return entities;
    }

    @JsonProperty("entities")
    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
