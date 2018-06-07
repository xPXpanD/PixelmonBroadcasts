// Reloads Pixelmon Broadcast's config, alias included. Does not reload langs.
package rs.expand.pixelmonbroadcasts.commands;

// Remote imports.
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

// Local imports.
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class Reload implements CommandExecutor
{
    @SuppressWarnings("NullableProblems")
    public CommandResult execute(final CommandSource src, final CommandContext args)
    {
        if (src instanceof Player)
        {
            printBasicMessage(
                    "§4PBR §f// §dPlayer §5" + src.getName() + "§d reloaded the Pixelmon Broadcasts config!");
        }

        // Load/create config.
        ConfigMethods.tryCreateAndLoadConfig();

        // Re-register alias, if applicable.
        if (commandAlias != null)
            ConfigMethods.registerCommands();

        if (src instanceof Player)
        {
            // Not entirely sure why I made this use the lang, but hey. Two lines, no harm.
            sendTranslation(src, "universal.header");
            sendTranslation(src, "reload.reload_complete");
            sendTranslation(src, "reload.check_console");
            sendTranslation(src, "universal.footer");
        }
        else
        {
            // These messages, however, are locked in. They won't be visible in-game.
            src.sendMessage(Text.of("§bReloaded the main Pixelmon Broadcasts config!"));
            src.sendMessage(Text.of("§bPlease check the console for any errors."));
        }

        return CommandResult.success();
    }
}
