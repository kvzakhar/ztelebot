package ru.zz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

public class KeyboardButton {

    /**
     * Text of the button. If none of the optional fields are used, it will be
     * sent to the bot as a message when the button is pressed
     */
    private String text;

    /**
     * Optional. If True, the user's phone number will be sent as a contact when
     * the button is pressed. Available in private chats only
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean request_contact;

    /**
     *
     * Optional. If True, the user's current location will be sent when the
     * button is pressed. Available in private chats only
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean request_location;

    public KeyboardButton(){

    }

    public KeyboardButton(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getRequest_contact() {
        return request_contact;
    }

    public void setRequest_contact(Boolean request_contact) {
        this.request_contact = request_contact;
    }

    public Boolean getRequest_location() {
        return request_location;
    }

    public void setRequest_location(Boolean request_location) {
        this.request_location = request_location;
    }

}
