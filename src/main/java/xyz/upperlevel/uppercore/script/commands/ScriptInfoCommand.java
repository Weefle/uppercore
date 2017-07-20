package xyz.upperlevel.uppercore.script.commands;

import org.bukkit.command.CommandSender;
import xyz.upperlevel.uppercore.command.Argument;
import xyz.upperlevel.uppercore.command.Command;
import xyz.upperlevel.uppercore.command.Executor;
import xyz.upperlevel.uppercore.script.PrecompiledScript;
import xyz.upperlevel.uppercore.script.Script;

import javax.script.ScriptEngine;

import static org.bukkit.ChatColor.*;
import static xyz.upperlevel.uppercore.script.ScriptSystem.getEngineName;

public class ScriptInfoCommand extends Command {
    public ScriptInfoCommand() {
        super("info");
        setDescription("Shows info about a script.");
    }

    @Executor
    public void run(CommandSender sender, @Argument("script") Script script) {
        ScriptEngine engine = script.getEngine();
        //TODO: add script execution time logging
        sender.sendMessage(AQUA + "engine: " + GOLD + getEngineName(engine));
        sender.sendMessage(AQUA + "compiled: " + GOLD + (script instanceof PrecompiledScript));
    }
}