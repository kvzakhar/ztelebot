package ru.zz.commands.en;

import ru.zz.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetPrimaryWaterEnCommand extends AbstractCommand {

    private static String TEXT = "Choose type of water for setting primary indications";

    public SetPrimaryWaterEnCommand(){}

    @Override
    public String getText() {
        return TEXT;
    }

    @Override
    public boolean getNeedReplyMarkup() {
        return true;
    }

    @Override
    public List<List<String>> getCommandButtons() {
        List<List<String>> buttons = new ArrayList<>();
        buttons.add(Arrays.asList("hot", "cold"));
        buttons.add(Arrays.asList("main menu"));
        return buttons;
    }
}
