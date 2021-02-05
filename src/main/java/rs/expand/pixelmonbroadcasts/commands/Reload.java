// Reloads Pixelmon Broadcast's config, alias included. Does not reload langs.
package rs.expand.pixelmonbroadcasts.commands;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.sendTranslation;

public class Reload extends HubCommand
{
    @Override
    public String getName()
    {
        return "reload";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/pixelmonbroadcasts reload";
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender)
    {
        if (sender.canUseCommand(4, "pixelmonbroadcasts.action.staff.reload"))
            return true;
        else
        {
            sendTranslation(sender, "universal.no_permissions");
            return false;
        }
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args)
    {
        if (sender instanceof EntityPlayer)
            logger.info("§5Player " + sender.getName() + " started a Pixelmon Broadcasts config reload.");
        else
            logger.info("§5A Pixelmon Broadcasts config reload was started through console or blocks.");

        // Load up all the configs and figure out the info alias. Start printing. Methods may insert errors as they go.
        logger.info("");
        logger.info("§f=============== P I X E L M O N  B R O A D C A S T S ===============");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        boolean loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to initializing more stuff.
        if (loadedCorrectly)
        {
            // (re-)register the main command and alias. Use the result we get back to see if everything worked.
            logger.info("§f--> §aReload completed. All systems nominal.");
        }
        else
            logger.info("§f--> §cLoad aborted due to critical errors. Check your configs and logs.");

        // We're done, one way or another. Add a footer, and a space to stay consistent.
        logger.info("§f====================================================================");
        logger.info("");

        // Print a message to chat.
        if (sender instanceof EntityPlayer)
        {
            // Not entirely sure why I made this use the lang, but hey. Two new lines, no harm.
            sendTranslation(sender, "universal.header");
            sendTranslation(sender, "reload.reload_complete");
            sendTranslation(sender, "reload.check_console");
            sendTranslation(sender, "universal.footer");
        }
        else
        {
            // These messages, however, are locked in. They won't be visible in-game.
            sender.sendMessage(new TextComponentString("§bReloaded the Pixelmon Broadcasts configs!"));
            sender.sendMessage(new TextComponentString("§bPlease check the console for any errors."));
        }
    }
}
