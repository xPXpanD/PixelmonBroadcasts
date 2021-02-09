// The one and only. Accept no imitations.
package com.github.xpxpand.pixelmonbroadcasts.commands;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlayerMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.server.command.CommandTreeBase;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.commandAlias;
import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

// Note: /teleport is not shown here, it's just for internal use and there are better alternatives available.
@SuppressWarnings("NullableProblems")
public class HubCommand extends CommandTreeBase
{
    // Forge seems to see the main command as a possible completion by default. Bypass that with a custom list.
    // This is very clean -- we still show up on /help, and the hub command sends people to the specific subcommands.
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos)
    {
        List<String> stringList = new ArrayList<>();

        stringList.add("toggle");
        if (PlayerMethods.hasPermission(sender, "pixelmonbroadcasts.command.staff.teleport"))
            stringList.add("teleport");
        if (PlayerMethods.hasPermission(sender, "pixelmonbroadcasts.command.staff.reload"))
            stringList.add("reload");

        return stringList;
    }

    @Override
    public String getName()
    {
        return "pixelmonbroadcasts";
    }

    // Used in /help and /help COMMANDNAME, so let's make this a proper thing.
    @Override
    public String getUsage(ICommandSender sender)
    {
        return "Shows a clickable list of event options.";
    }

    @Override
    public List<String> getAliases() { return Collections.singletonList(commandAlias); }

    // Ensures this command can always run. Except for on Magma/Mohist, they do weird things and need a perm anyways?
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
            PrintingMethods.sendTranslation(sender, "universal.header");

            // Show an error if the alias isn't set right. Continue after.
            String checkedAlias = commandAlias;
            if (commandAlias == null)
            {
                logger.error("Could not read config node \"§4commandAlias§c\" while executing hub command.");
                logger.error("We'll continue with the command, but aliases will break. Check your config.");

                // Insert a safe default.
                checkedAlias = "pixelmonbroadcasts";
            }

            // Only show to actual players.
            if (sender instanceof EntityPlayer)
            {
                ITextComponent toggleComponent = new TextComponentString(PrintingMethods.getTranslation("hub.toggle_syntax", checkedAlias));
                toggleComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pixelmonbroadcasts toggle"));
                sender.sendMessage(toggleComponent);
                sender.sendMessage(new TextComponentString(PrintingMethods.getTranslation("hub.toggle_info")));
            }

            // Show to players with the permission, or with cheats enabled if in SP. Add a note if run from console. Run last.
            if (!(sender instanceof EntityPlayer) || PlayerMethods.hasPermission(sender, "pixelmonbroadcasts.command.staff.reload"))
            {
                ITextComponent reloadComponent = new TextComponentString(PrintingMethods.getTranslation("hub.reload_syntax", checkedAlias));
                reloadComponent.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/pixelmonbroadcasts reload"));
                sender.sendMessage(reloadComponent);
                sender.sendMessage(new TextComponentString(PrintingMethods.getTranslation("hub.reload_info")));
            }

            // Cap things off with a nice lang file footer.
            PrintingMethods.sendTranslation(sender, "universal.footer");
        }
        else
            sender.sendMessage(new TextComponentString("§cThis command cannot run from command blocks."));
    }
}