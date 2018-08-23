package ru.zz.commands;

import java.util.List;

public abstract class AbstractCommand {

    public abstract String getText();
    public abstract boolean getNeedReplyMarkup();
    public abstract List<List<String>> getCommandButtons();

}
