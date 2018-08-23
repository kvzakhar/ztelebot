package ru.zz.commands.ru;

import ru.zz.commands.AbstractCommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetPrimaryWaterRusCommand extends AbstractCommand {
    private static String TEXT = "Выберите тип воды";

    public SetPrimaryWaterRusCommand(){}

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
        buttons.add(Arrays.asList("горячая", "холодная"));
        buttons.add(Arrays.asList("главное меню"));
        return buttons;
    }
}
