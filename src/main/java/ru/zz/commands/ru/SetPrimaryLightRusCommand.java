package ru.zz.commands.ru;

import ru.zz.commands.AbstractCommand;

import java.util.List;


public class SetPrimaryLightRusCommand extends AbstractCommand {

    @Override
    public String getText() {
        return null;
    }

    @Override
    public boolean getNeedReplyMarkup() {
        return false;
    }

    @Override
    public List<List<String>> getCommandButtons() {
        return null;
    }
}
