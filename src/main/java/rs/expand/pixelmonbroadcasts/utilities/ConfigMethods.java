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
        // Start committing config stuff to memory.
        PixelmonBroadcasts.configVersion =
                interpretInteger(commandConfig.getNode("configVersion").getString());
        PixelmonBroadcasts.commandAlias =
                commandConfig.getNode("commandAlias").getString();

        // Load up logging settings.
        PixelmonBroadcasts.logLegendarySpawns =
                toBooleanObject(commandConfig.getNode("logLegendarySpawns").getString());
        PixelmonBroadcasts.logLegendaryCatches =
                toBooleanObject(commandConfig.getNode("logLegendaryCatches").getString());
        PixelmonBroadcasts.logLegendaryDefeats =
                toBooleanObject(commandConfig.getNode("logLegendaryDefeats").getString());
        PixelmonBroadcasts.logShinySpawns =
                toBooleanObject(commandConfig.getNode("logShinySpawns").getString());
        PixelmonBroadcasts.logShinyCatches =
                toBooleanObject(commandConfig.getNode("logShinyCatches").getString());
        PixelmonBroadcasts.logShinyDefeats =
                toBooleanObject(commandConfig.getNode("logShinyDefeats").getString());
        PixelmonBroadcasts.logBossSpawns =
                toBooleanObject(commandConfig.getNode("logBossSpawns").getString());
        PixelmonBroadcasts.logBossDefeats =
                toBooleanObject(commandConfig.getNode("logBossDefeats").getString());
        PixelmonBroadcasts.logHatches =
                toBooleanObject(commandConfig.getNode("logHatches").getString());
        PixelmonBroadcasts.logTrades =
                toBooleanObject(commandConfig.getNode("logTrades").getString());

        // Load up broadcast settings.
        PixelmonBroadcasts.showLegendarySpawnMessage =
                toBooleanObject(commandConfig.getNode("showLegendarySpawnMessage").getString());
        PixelmonBroadcasts.showLegendaryCatchMessage =
                toBooleanObject(commandConfig.getNode("showLegendaryCatchMessage").getString());
        PixelmonBroadcasts.showLegendaryDefeatMessage =
                toBooleanObject(commandConfig.getNode("showLegendaryDefeatMessage").getString());
        PixelmonBroadcasts.showShinySpawnMessage =
                toBooleanObject(commandConfig.getNode("showShinySpawnMessage").getString());
        PixelmonBroadcasts.showShinyCatchMessage =
                toBooleanObject(commandConfig.getNode("showShinyCatchMessage").getString());
        PixelmonBroadcasts.showShinyDefeatMessage =
                toBooleanObject(commandConfig.getNode("showShinyDefeatMessage").getString());
        PixelmonBroadcasts.showBossSpawnMessage =
                toBooleanObject(commandConfig.getNode("showBossSpawnMessage").getString());
        PixelmonBroadcasts.showBossDefeatMessage =
                toBooleanObject(commandConfig.getNode("showBossDefeatMessage").getString());
        PixelmonBroadcasts.showHatchMessage =
                toBooleanObject(commandConfig.getNode("showHatchMessage").getString());
        PixelmonBroadcasts.showTradeMessage =
                toBooleanObject(commandConfig.getNode("showTradeMessage").getString());

        // Load up actual messages. Do some initial message parsing to get formatting characters right.
        PixelmonBroadcasts.legendarySpawnMessage =
                parseRemoteString(commandConfig.getNode("legendarySpawnMessage").getString());
        PixelmonBroadcasts.legendaryCatchMessage =
                parseRemoteString(commandConfig.getNode("legendaryCatchMessage").getString());
        PixelmonBroadcasts.legendaryDefeatMessage =
                parseRemoteString(commandConfig.getNode("legendaryDefeatMessage").getString());
        PixelmonBroadcasts.shinySpawnMessage =
                parseRemoteString(commandConfig.getNode("shinySpawnMessage").getString());
        PixelmonBroadcasts.shinyCatchMessage =
                parseRemoteString(commandConfig.getNode("shinyCatchMessage").getString());
        PixelmonBroadcasts.shinyDefeatMessage =
                parseRemoteString(commandConfig.getNode("shinyDefeatMessage").getString());
        PixelmonBroadcasts.bossSpawnMessage =
                parseRemoteString(commandConfig.getNode("bossSpawnMessage").getString());
        PixelmonBroadcasts.bossDefeatMessage =
                parseRemoteString(commandConfig.getNode("bossDefeatMessage").getString());
        PixelmonBroadcasts.hatchMessage =
                parseRemoteString(commandConfig.getNode("hatchMessage").getString());
        PixelmonBroadcasts.tradeMessage =
                parseRemoteString(commandConfig.getNode("tradeMessage").getString());

        // Special combo and legendary+shiny messages.
        PixelmonBroadcasts.shinyLegendarySpawnMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendarySpawnMessage").getString());
        PixelmonBroadcasts.shinyLegendaryCatchMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryCatchMessage").getString());
        PixelmonBroadcasts.shinyLegendaryDefeatMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryDefeatMessage").getString());
        PixelmonBroadcasts.shinyHatchMessage =
                parseRemoteString(commandConfig.getNode("shinyHatchMessage").getString());

        // Start validating the data we get from the main config. Let's do logging bools first.
        final ArrayList<String> booleanErrorArray = new ArrayList<>();
        if (logLegendarySpawns == null)
            booleanErrorArray.add("logLegendarySpawns");
        if (logLegendaryCatches == null)
            booleanErrorArray.add("logLegendaryCatches");
        if (logLegendaryDefeats == null)
            booleanErrorArray.add("logLegendaryDefeats");
        if (logShinySpawns == null)
            booleanErrorArray.add("logShinySpawns");
        if (logShinyCatches == null)
            booleanErrorArray.add("logShinyCatches");
        if (logShinyDefeats == null)
            booleanErrorArray.add("logShinyDefeats");
        if (logBossSpawns == null)
            booleanErrorArray.add("logBossSpawns");
        if (logBossDefeats == null)
            booleanErrorArray.add("logBossDefeats");
        if (logHatches == null)
            booleanErrorArray.add("logHatches");
        if (logTrades == null)
            booleanErrorArray.add("logTrades");

        // Move onto message bools.
        if (showLegendarySpawnMessage == null)
            booleanErrorArray.add("showLegendarySpawnMessage");
        if (showLegendaryCatchMessage == null)
            booleanErrorArray.add("showLegendaryCatchMessage");
        if (showLegendaryDefeatMessage == null)
            booleanErrorArray.add("showLegendaryDefeatMessage");
        if (showShinySpawnMessage == null)
            booleanErrorArray.add("showShinySpawnMessage");
        if (showShinyCatchMessage == null)
            booleanErrorArray.add("showShinyCatchMessage");
        if (showShinyDefeatMessage == null)
            booleanErrorArray.add("showShinyDefeatMessage");
        if (showBossSpawnMessage == null)
            booleanErrorArray.add("showBossSpawnMessage");
        if (showBossDefeatMessage == null)
            booleanErrorArray.add("showBossDefeatMessage");
        if (showHatchMessage == null)
            booleanErrorArray.add("showHatchMessage");
        if (showTradeMessage == null)
            booleanErrorArray.add("showTradeMessage");

        // Now, validate main messages.
        final ArrayList<String> messageErrorArray = new ArrayList<>();
        if (legendarySpawnMessage == null)
            messageErrorArray.add("legendarySpawnMessage");
        if (legendaryCatchMessage == null)
            messageErrorArray.add("legendaryCatchMessage");
        if (legendaryDefeatMessage == null)
            messageErrorArray.add("legendaryDefeatMessage");
        if (shinySpawnMessage == null)
            messageErrorArray.add("shinySpawnMessage");
        if (shinyCatchMessage == null)
            messageErrorArray.add("shinyCatchMessage");
        if (shinyDefeatMessage == null)
            messageErrorArray.add("shinyDefeatMessage");
        if (bossSpawnMessage == null)
            messageErrorArray.add("bossSpawnMessage");
        if (bossDefeatMessage == null)
            messageErrorArray.add("bossDefeatMessage");
        if (hatchMessage == null)
            messageErrorArray.add("hatchMessage");
        if (tradeMessage == null)
            messageErrorArray.add("tradeMessage");

        // Validate special combos and legendary-plus-shiny messages. Falls under the same message array.
        if (shinyLegendarySpawnMessage == null)
            messageErrorArray.add("shinyLegendarySpawnMessage");
        if (shinyLegendaryCatchMessage == null)
            messageErrorArray.add("shinyLegendaryCatchMessage");
        if (shinyLegendaryDefeatMessage == null)
            messageErrorArray.add("shinyLegendaryDefeatMessage");
        if (shinyHatchMessage == null)
            messageErrorArray.add("shinyHatchMessage");

        // Print errors if something broke.
        if (!booleanErrorArray.isEmpty())
            printBooleanNodeError(booleanErrorArray);
        if (!messageErrorArray.isEmpty())
            printMessageNodeError(messageErrorArray);
    }
}
