package ru.zz.commands.en;

import ru.zz.commands.AbstractCommand;

import java.util.List;

public class SetPrimaryCountersEnCommand extends AbstractCommand{

    private static String TEXT =
            "In these indications will be built further lease payments.\n" +
            "You can set next primary counters via follows commands:\n\n"+
            "set water (/setprimarywater) - set primary counters for water\n"+
            "set rent amount (/setprimaryrentamount) - set rent amount per month\n"+
            "set light (/setprimarylight) - set primary counters for light";

    public SetPrimaryCountersEnCommand(){};

    @Override
    public String getText() {
        return TEXT;
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