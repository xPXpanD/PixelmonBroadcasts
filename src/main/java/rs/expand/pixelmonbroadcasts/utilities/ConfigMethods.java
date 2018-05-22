// The probably-too-complicated config handler. This doesn't feel like the correct way to do it, but it works well enough.
package rs.expand.pixelmonbroadcasts.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.plugin.PluginContainer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

// Local imports.
import rs.expand.pixelmonbroadcasts.PixelmonBroadcasts;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// Note: All the main class stuff and printing stuff is added through static imports.
public class ConfigMethods
{
    // Make a little converter for safely handling possibly null Strings that have an integer value inside.
    private static Integer interpretInteger(final String input)
    {
        if (input != null && input.matches("-?[1-9]\\d*|0"))
            return Integer.parseInt(input);
        else
            return null;
    }

    // Called during initial setup, either when the server is booting up or when /pbroadcasts reload has been executed.
    public static boolean tryCreateAndLoadConfig()
    {
        if (Files.notExists(PixelmonBroadcasts.primaryConfigPath))
        {
            // Spaces added so it falls in line with startup/reload message spacing.
            try
            {
                // Create a new config since a file wasn't found.
                printBasicMessage("    §eNo primary configuration file found, creating...");

                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/PixelmonBroadcasts.conf"),
                        Paths.get(PixelmonBroadcasts.primaryPath, "PixelmonBroadcasts.conf"));

                // Now that we've created a config, proceed to normal loading.
                loadConfig(PixelmonBroadcasts.primaryConfigLoader.load());

                // We're done! Let's tell our caller.
                return true;
            }
            catch (final IOException F)
            {
                printBasicMessage("    §cPrimary config setup has failed! Please report this.");
                printBasicMessage("    §cAdd any useful info you may have (operating system?). Stack trace:");

                F.printStackTrace();
            }
        }
        else // Seems like we're good already, try a normal load.
        {
            try
            {
                // Let's do this.
                loadConfig(PixelmonBroadcasts.primaryConfigLoader.load());

                // We're done! Let's tell our caller, if it wants to know.
                return true;
            }
            catch (IOException F)
            {
                printBasicMessage("    §cCould not read primary config! Please report this.");
                printBasicMessage("    §cAdd any useful info you may have (operating system?). Stack trace:");
                F.printStackTrace();
            }
        }

        // If we fall through, something broke. Let's avoid printing a "loading successful" message, that'd be silly.
        return false;
    }

    // Unloads all known PixelmonBroadcasts commands and their aliases, and then re-registers them.
    public static boolean registerCommands()
    {
        final PluginContainer pbcContainer = Sponge.getPluginManager().getPlugin("pixelmonbroadcasts").orElse(null);

        // Registers the base command and an alias, if a valid one is found.
        if (pbcContainer != null)
        {
            CommandManager manager = Sponge.getGame().getCommandManager();
            manager.getOwnedBy(pbcContainer).forEach(manager::removeMapping);

            if (commandAlias != null && !commandAlias.equals("pixelmonbroadcasts"))
                Sponge.getCommandManager().register(pbcContainer, basecommand, "pixelmonbroadcasts", commandAlias);
            else
                Sponge.getCommandManager().register(pbcContainer, basecommand, "pixelmonbroadcasts");

            return true;
        }
        else
        {
            printBasicMessage("    §cCommand (re-)initialization failed. Please report, this is a bug.");
            printBasicMessage("    §cSidemod commands are likely dead. A reboot/reload may work.");

            return false;
        }
    }

    // Read the main config file, and then commit settings to memory. Rough but functional is the theme here, folks.
    private static void loadConfig(final CommentedConfigurationNode commandConfig)
    {
        // TODO: Validation group for main settings, or comment.
        // Start committing config stuff to memory. These are validated during startup/reload.
        PixelmonBroadcasts.configVersion =
                interpretInteger(commandConfig.getNode("configVersion").getString());
        PixelmonBroadcasts.commandAlias =
                commandConfig.getNode("commandAlias").getString();
        PixelmonBroadcasts.statSeparator =
                parseRemoteString(commandConfig.getNode("statSeparator").getString());

        // Load up config settings.
        /*                      *\
            legendary settings
        \*                      */
        final String legendarySpawnOptions = commandConfig.getNode("legendarySpawnOptions").getString().toLowerCase();
        if (legendarySpawnOptions.contains("log"))
            PixelmonBroadcasts.logLegendarySpawns = true;
        if (legendarySpawnOptions.contains("show"))
            PixelmonBroadcasts.showLegendarySpawns = true;
        if (legendarySpawnOptions.contains("hover"))
            PixelmonBroadcasts.hoverLegendarySpawns = true;
    }
}
