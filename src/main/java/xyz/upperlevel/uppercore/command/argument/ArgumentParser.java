package xyz.upperlevel.uppercore.command.argument;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.uppercore.command.argument.exceptions.ParseException;

import java.util.List;

import static java.util.Collections.emptyList;

public interface ArgumentParser {

    List<Class<?>> getParsable();

    default boolean isParsable(Class<?> type) {
        return getParsable().contains(type);
    }

    int getArgumentsCount();

    Object parse(Class<?> type, List<String> args) throws ParseException;

    default List<String> onTabCompletion(CommandSender sender, Class<?> type, List<String> args) {
        return emptyList();
    }
}
