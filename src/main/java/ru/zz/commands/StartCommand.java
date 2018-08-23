package ru.zz.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StartCommand extends AbstractCommand {
    private static String TEXT_RUS =
       "Для продолжения на русском языке используйте команду:\n" + "<b>аренда</b> - расчет арендной платы за месяц\n";

    private static String TEXT_EN =
       "For english tap rent:\n" + "<b>rent</b> (/rent) - calculating rent\n" ;

    public StartCommand(){}

    @Override
    public String getText() {
        return TEXT_RUS + TEXT_EN;
    }

    @Override
    public boolean getNeedReplyMarkup() {
        return true;
    }

    @Override
    public List<List<String>> getCommandButtons() {
        List<List<String>> buttons = new ArrayList<>();
        buttons.add(Arrays.asList("rent", "аренда"));
        return buttons;
    }
}
