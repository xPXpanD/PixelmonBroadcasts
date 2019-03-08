// Reloads Pixelmon Broadcast's config, alias included. Does not reload langs.
package rs.expand.pixelmonbroadcasts.commands;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.sendTranslation;

public class Reload implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
            logger.info("§4PBR §f// §5Player " + src.getName() + " started a Pixelmon Broadcasts config reload.");
        else
            logger.info("§4PBR §f// §5A Pixelmon Broadcasts config reload was started through console or blocks.");

        // Load up all the configs and figure out the info alias. Start printing. Methods may insert errors as they go.
        logger.info("");
        logger.info("=============== P I X E L M O N  B R O A D C A S T S ===============");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        boolean loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to initializing more stuff.
        if (loadedCorrectly)
        {
            // (re-)register the main command and alias. Use the result we get back to see if everything worked.
            logger.info("--> §aRe-registering commands with Sponge...");
            if (ConfigMethods.tryRegisterCommands())
                logger.info("--> §aReload completed. All systems nominal.");
        }
        else
            logger.info("--> §cLoad aborted due to critical errors.");

        // We're done, one way or another. Add a footer, and a space to stay consistent.
        logger.info("====================================================================");
        logger.info("");

        // Print a message to chat.
        if (src instanceof Player)
        {
            // Not entirely sure why I made this use the lang, but hey. Two new lines, no harm.
            sendTranslation(src, "universal.header");
            sendTranslation(src, "reload.reload_complete");
            sendTranslation(src, "reload.check_console");
            sendTranslation(src, "universal.footer");
        }
        else
        {
            // These messages, however, are locked in. They won't be visible in-game.
            src.sendMessage(Text.of("§bReloaded the Pixelmon Broadcasts configs!"));
            src.sendMessage(Text.of("§bPlease check the console for any errors."));
        }

        return CommandResult.success();
    }
}
