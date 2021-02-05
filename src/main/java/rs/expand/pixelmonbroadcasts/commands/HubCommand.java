// The one and only. Accept no imitations.
package rs.expand.pixelmonbroadcasts.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.server.command.CommandTreeBase;

import java.util.Collections;
import java.util.List;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.commandAlias;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.getTranslation;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.sendTranslation;

@SuppressWarnings("NullableProblems")
public class HubCommand extends CommandTreeBase
{
/*    public BaseCommand()
    {
        addSubcommand(new Reload());
        addSubcommand(new Teleport());
        addSubcommand(new Toggle());
    }*/

    @Override
    public String getName()
    {
        return "pixelmonbroadcasts";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pixelmonbroadcasts <option>";
    }

    @Override
    public List<String> getAliases() { return Collections.singletonList(commandAlias); }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) { return true; }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (args.length > 0)
        {
            // Route subcommands to the correct classes. Shank the first argument.
            switch (args[0].toLowerCase())
            {
                // Subcommands.
                case "reload":
                    new Reload().execute(server, sender, args);
                    break;
                case "teleport":
                    new Teleport().execute(server, sender, args);
                    break;
                case "toggle":
                    new Toggle().execute(server, sender, args);
                    break;

                // Nope, they want the list.
                default:
                    showList(sender);
            }
        }
        else showList(sender);
    }

    // Show the list of available subcommands.
    private void showList(ICommandSender sender)
    {
        // Are we being called by a player or something else that can receive multiple messages? Go away, command blocks.
        if (!(sender instanceof CommandBlockBaseLogic))
        {
            // Add a header from our language file.
            sendTranslation(sender, "universal.header");

            // Show an error if the alias isn't set right. Continue after.
            String checkedAlias = commandAlias;
            if (commandAlias == null)
            {
                logger.error("Could not read config node \"§4commandAlias§c\" while executing hub command.");
                logger.error("We'll continue with the command, but aliases will break. Check your config.");

                // Insert a safe default.
                checkedAlias = "pixelmonbroadcasts";
            }

            // Set up a flag to see where we're running from. Gets set to true if we weren't called by a player.
            final boolean calledRemotely = !(sender instanceof EntityPlayer);

            ITextComponent toggleComponent = new TextComponentString(getTranslation("hub.toggle_syntax", checkedAlias));
            toggleComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pixelmonbroadcasts toggle"));
            sender.sendMessage(toggleComponent);

            if (sender instanceof EntityPlayer)
                sender.sendMessage(new TextComponentString(getTranslation("hub.toggle_info")));
            else
            {
                // Message locked in, as it's not visible in-game. Keeps the lang workload down, with minimal loss.
                sender.sendMessage(new TextComponentString(
                        "➡ §eAllows in-game players to toggle event messages by clicking them."));
            }

            if (calledRemotely || sender.canUseCommand(4, "pixelmonbroadcasts.command.staff.reload"))
            {
                ITextComponent reloadComponent = new TextComponentString(getTranslation("hub.reload_syntax", checkedAlias));
                reloadComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pixelmonbroadcasts reload"));
                sender.sendMessage(reloadComponent);

                sender.sendMessage(new TextComponentString(getTranslation("hub.reload_info")));

                if (calledRemotely)
                {
                    // Messages locked in, as they're not visible in-game. Keeps the lang workload down, with minimal loss.
                    sender.sendMessage(new TextComponentString(""));
                    sender.sendMessage(new TextComponentString("§6Please note: §eThe \"toggle\" sub-command will only work when used in-game."));
                }
            }

            // Cap things off with a nice lang file footer.
            sendTranslation(sender, "universal.footer");
        }
        else
            sender.sendMessage(new TextComponentString("§cThis command cannot run from command blocks."));
    }
}