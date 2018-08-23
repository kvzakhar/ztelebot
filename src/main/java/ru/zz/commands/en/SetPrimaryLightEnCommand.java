package ru.zz.commands.en;

import ru.zz.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetPrimaryLightEnCommand extends AbstractCommand {
    private static String TEXT = "Which type of tariff is right for you?\n\n1- one-tariff\n2 - two-tariff\n3 - three-tariff";

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
        buttons.add(Arrays.asList("1", "2", "3"));
        buttons.add(Arrays.asList("main menu"));
        return buttons;
    }
}
