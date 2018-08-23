package ru.zz.commands.ru;

import ru.zz.commands.AbstractCommand;
import java.util.List;

public class SetPrimaryCountersRusCommand extends AbstractCommand {

    private static String TEXT =
       "На данных показаниях будут строиться дальнейшие расчеты аренды.\nВы можете использовать следующие команды:\n\n"+
       "<b>вода</b> - задать начальные показания счетчиков воды\n"+
       "<b>арендная плата</b> - задать сумму аренды\n"+
       "<b>свет</b> - задать начальные показания счетчика света";

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
