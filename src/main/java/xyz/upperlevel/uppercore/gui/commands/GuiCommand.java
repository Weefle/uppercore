package xyz.upperlevel.uppercore.gui.commands;

import xyz.upperlevel.uppercore.command.DefaultPermission;
import xyz.upperlevel.uppercore.command.NodeCommand;
import xyz.upperlevel.uppercore.command.WithChildPermission;
import xyz.upperlevel.uppercore.command.WithPermission;

@WithPermission(value = "gui", def = DefaultPermission.OP)
@WithChildPermission(desc = "Allows you to perform all gui-related commands")
public class GuiCommand extends NodeCommand {

    public GuiCommand() {
        super("gui");
        setDescription("Commands for guis.");

        register(new OpenGuiCommand());
        register(new CloseGuiCommand());
        register(new GuiListCommand());
    }
}
