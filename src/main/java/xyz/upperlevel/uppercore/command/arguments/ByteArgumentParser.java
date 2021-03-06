package xyz.upperlevel.uppercore.command.arguments;

import xyz.upperlevel.uppercore.command.argument.ArgumentParser;
import xyz.upperlevel.uppercore.command.argument.exceptions.ParseException;

import java.util.List;

import static java.lang.Byte.parseByte;
import static java.util.Arrays.asList;

public class ByteArgumentParser implements ArgumentParser {

    @Override
    public List<Class<?>> getParsable() {
        return asList(Byte.class, byte.class);
    }

    @Override
    public int getArgumentsCount() {
        return 1;
    }

    @Override
    public Object parse(Class<?> type, List<String> args) throws ParseException {
        try {
            return parseByte(args.get(0));
        } catch (Exception e) {
            throw new ParseException(args.get(0), "number");
        }
    }
}
