// The probably-too-complicated config handler. This doesn't feel like the correct way to do it, but it works well enough.
package rs.expand.pixelmonbroadcasts.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.plugin.PluginContainer;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

// Local imports.
import rs.expand.pixelmonbroadcasts.PixelmonBroadcasts;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class ConfigMethods
{
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
        // Start committing config stuff to memory. Start with the main variables.
        //PixelmonBroadcasts.configVersion =
        //        interpretInteger(commandConfig.getNode("configVersion").getString());
        PixelmonBroadcasts.commandAlias =
                commandConfig.getNode("commandAlias").getString();
        PixelmonBroadcasts.statSeparator =
                parseRemoteString(commandConfig.getNode("statSeparator").getString());
        PixelmonBroadcasts.statLineStart =
                parseRemoteString(commandConfig.getNode("statLineStart").getString());

        // Show errors if any of these main variables are broken.
        if (commandAlias == null)
            printBasicMessage("    §cCould not read config node \"§4commandAlias§c\". Alias support disabled.");
        if (statSeparator == null)
        {
            printBasicMessage("    §cCould not read config node \"§4statSeparator§c\". Falling back to defaults.");
            statSeparator = "§r, ";
        }
        if (statLineStart == null)
        {
            printBasicMessage("    §cCould not read config node \"§4statLineStart§c\". Falling back to defaults.");
            statLineStart= "➡ ";
        }

        // Set up some error arrays, so we can print errors to the console on boot/reload if stuff broke.
        final List<String> optionsErrorArray = new ArrayList<>();
        final List<String> messageErrorArray = new ArrayList<>();

        /*                    *\
           legendary settings
        \*                    */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String legendarySpawnOptions = commandConfig.getNode("legendarySpawnOptions").getString();
        if (legendarySpawnOptions == null)
            optionsErrorArray.add("legendarySpawnOptions");
        else
        {
            legendarySpawnOptions = legendarySpawnOptions.toLowerCase();

            if (legendarySpawnOptions.contains("log"))
                PixelmonBroadcasts.logLegendarySpawns = true;
            if (legendarySpawnOptions.contains("show"))
                PixelmonBroadcasts.showLegendarySpawns = true;
            if (legendarySpawnOptions.contains("hover"))
                PixelmonBroadcasts.hoverLegendarySpawns = true;
        }

        String legendaryCatchOptions = commandConfig.getNode("legendaryCatchOptions").getString();
        if (legendaryCatchOptions == null)
            optionsErrorArray.add("legendaryCatchOptions");
        else
        {
            legendaryCatchOptions = legendaryCatchOptions.toLowerCase();

            if (legendaryCatchOptions.contains("log"))
                PixelmonBroadcasts.logLegendaryCatches = true;
            if (legendaryCatchOptions.contains("show"))
                PixelmonBroadcasts.showLegendaryCatches = true;
            if (legendaryCatchOptions.contains("hover"))
                PixelmonBroadcasts.hoverLegendaryCatches = true;
        }

        String legendaryDefeatOptions = commandConfig.getNode("legendaryDefeatOptions").getString();
        if (legendaryDefeatOptions == null)
            optionsErrorArray.add("legendaryDefeatOptions");
        else
        {
            legendaryDefeatOptions = legendaryDefeatOptions.toLowerCase();

            if (legendaryDefeatOptions.contains("log"))
                PixelmonBroadcasts.logLegendaryDefeats = true;
            if (legendaryDefeatOptions.contains("show"))
                PixelmonBroadcasts.showLegendaryDefeats = true;
            if (legendaryDefeatOptions.contains("hover"))
                PixelmonBroadcasts.hoverLegendaryDefeats = true;
        }

        /*                          *\
           shiny legendary settings
        \*                          */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String shinyLegendarySpawnOptions = commandConfig.getNode("shinyLegendarySpawnOptions").getString();
        if (shinyLegendarySpawnOptions == null)
            optionsErrorArray.add("shinyLegendarySpawnOptions");
        else
        {
            shinyLegendarySpawnOptions = shinyLegendarySpawnOptions.toLowerCase();

            if (shinyLegendarySpawnOptions.contains("log"))
                PixelmonBroadcasts.logShinyLegendarySpawns = true;
            if (shinyLegendarySpawnOptions.contains("show"))
                PixelmonBroadcasts.showShinyLegendarySpawns = true;
            if (shinyLegendarySpawnOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyLegendarySpawns = true;
        }

        String shinyLegendaryCatchOptions = commandConfig.getNode("shinyLegendaryCatchOptions").getString();
        if (shinyLegendaryCatchOptions == null)
            optionsErrorArray.add("shinyLegendaryCatchOptions");
        else
        {
            shinyLegendaryCatchOptions = shinyLegendaryCatchOptions.toLowerCase();

            if (shinyLegendaryCatchOptions.contains("log"))
                PixelmonBroadcasts.logShinyLegendaryCatches = true;
            if (shinyLegendaryCatchOptions.contains("show"))
                PixelmonBroadcasts.showShinyLegendaryCatches = true;
            if (shinyLegendaryCatchOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyLegendaryCatches = true;
        }

        String shinyLegendaryDefeatOptions = commandConfig.getNode("shinyLegendaryDefeatOptions").getString();
        if (shinyLegendaryDefeatOptions == null)
            optionsErrorArray.add("shinyLegendaryDefeatOptions");
        else
        {
            shinyLegendaryDefeatOptions = shinyLegendaryDefeatOptions.toLowerCase();

            if (shinyLegendaryDefeatOptions.contains("log"))
                PixelmonBroadcasts.logShinyLegendaryDefeats = true;
            if (shinyLegendaryDefeatOptions.contains("show"))
                PixelmonBroadcasts.showShinyLegendaryDefeats = true;
            if (shinyLegendaryDefeatOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyLegendaryDefeats = true;
        }

        /*                *\
           shiny settings
        \*                */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String shinySpawnOptions = commandConfig.getNode("shinySpawnOptions").getString();
        if (shinySpawnOptions == null)
            optionsErrorArray.add("shinySpawnOptions");
        else
        {
            shinySpawnOptions = shinySpawnOptions.toLowerCase();

            if (shinySpawnOptions.contains("log"))
                PixelmonBroadcasts.logShinySpawns = true;
            if (shinySpawnOptions.contains("show"))
                PixelmonBroadcasts.showShinySpawns = true;
            if (shinySpawnOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinySpawns = true;
        }

        String shinyCatchOptions = commandConfig.getNode("shinyCatchOptions").getString();
        if (shinyCatchOptions == null)
            optionsErrorArray.add("shinyCatchOptions");
        else
        {
            shinyCatchOptions = shinyCatchOptions.toLowerCase();

            if (shinyCatchOptions.contains("log"))
                PixelmonBroadcasts.logShinyCatches = true;
            if (shinyCatchOptions.contains("show"))
                PixelmonBroadcasts.showShinyCatches = true;
            if (shinyCatchOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyCatches = true;
        }

        String shinyDefeatOptions = commandConfig.getNode("shinyDefeatOptions").getString();
        if (shinyDefeatOptions == null)
            optionsErrorArray.add("shinyDefeatOptions");
        else
        {
            shinyDefeatOptions = shinyDefeatOptions.toLowerCase();

            if (shinyDefeatOptions.contains("log"))
                PixelmonBroadcasts.logShinyDefeats = true;
            if (shinyDefeatOptions.contains("show"))
                PixelmonBroadcasts.showShinyDefeats = true;
            if (shinyDefeatOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyDefeats = true;
        }

        /*               *\
           boss settings
        \*               */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String bossSpawnOptions = commandConfig.getNode("bossSpawnOptions").getString();
        if (bossSpawnOptions == null)
            optionsErrorArray.add("bossSpawnOptions");
        else
        {
            bossSpawnOptions = bossSpawnOptions.toLowerCase();

            if (bossSpawnOptions.contains("log"))
                PixelmonBroadcasts.logBossSpawns = true;
            if (bossSpawnOptions.contains("show"))
                PixelmonBroadcasts.showBossSpawns = true;
            if (bossSpawnOptions.contains("hover"))
                PixelmonBroadcasts.hoverBossSpawns = true;
        }

        String bossDefeatOptions = commandConfig.getNode("bossDefeatOptions").getString();
        if (bossDefeatOptions == null)
            optionsErrorArray.add("bossDefeatOptions");
        else
        {
            bossDefeatOptions = bossDefeatOptions.toLowerCase();

            if (bossDefeatOptions.contains("log"))
                PixelmonBroadcasts.logBossDefeats = true;
            if (bossDefeatOptions.contains("show"))
                PixelmonBroadcasts.showBossDefeats = true;
            if (bossDefeatOptions.contains("hover"))
                PixelmonBroadcasts.hoverBossDefeats = true;
        }

        /*                *\
           hatch settings
        \*                */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String hatchOptions = commandConfig.getNode("hatchOptions").getString();
        if (hatchOptions == null)
            optionsErrorArray.add("hatchOptions");
        else
        {
            hatchOptions = hatchOptions.toLowerCase();

            if (hatchOptions.contains("log"))
                PixelmonBroadcasts.logHatches = true;
            if (hatchOptions.contains("show"))
                PixelmonBroadcasts.showHatches = true;
            if (hatchOptions.contains("hover"))
                PixelmonBroadcasts.hoverHatches = true;
        }

        String shinyHatchOptions = commandConfig.getNode("shinyHatchOptions").getString();
        if (shinyHatchOptions == null)
            optionsErrorArray.add("shinyHatchOptions");
        else
        {
            shinyHatchOptions = shinyHatchOptions.toLowerCase();

            if (shinyHatchOptions.contains("log"))
                PixelmonBroadcasts.logShinyHatches = true;
            if (shinyHatchOptions.contains("show"))
                PixelmonBroadcasts.showShinyHatches = true;
            if (shinyHatchOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyHatches = true;
        }

        /*                *\
           trade settings
        \*                */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String tradeOptions = commandConfig.getNode("tradeOptions").getString();
        if (tradeOptions == null)
            optionsErrorArray.add("tradeOptions");
        else
        {
            tradeOptions = tradeOptions.toLowerCase();

            // TODO: No hover option in here, for now. Might be nice, eventually.
            if (tradeOptions.contains("log"))
                PixelmonBroadcasts.logTrades = true;
            if (tradeOptions.contains("show"))
                PixelmonBroadcasts.showTrades = true;
        }

        /*          *\
           messages
        \*          */

        // Load up actual messages. Do some initial message parsing to get formatting characters right.
        PixelmonBroadcasts.legendarySpawnMessage =
                parseRemoteString(commandConfig.getNode("legendarySpawnMessage").getString());
        PixelmonBroadcasts.legendaryCatchMessage =
                parseRemoteString(commandConfig.getNode("legendaryCatchMessage").getString());
        PixelmonBroadcasts.legendaryDefeatMessage =
                parseRemoteString(commandConfig.getNode("legendaryDefeatMessage").getString());
        PixelmonBroadcasts.shinyLegendarySpawnMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendarySpawnMessage").getString());
        PixelmonBroadcasts.shinyLegendaryCatchMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryCatchMessage").getString());
        PixelmonBroadcasts.shinyLegendaryDefeatMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryDefeatMessage").getString());
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
        PixelmonBroadcasts.shinyHatchMessage =
                parseRemoteString(commandConfig.getNode("shinyHatchMessage").getString());
        PixelmonBroadcasts.tradeMessage =
                parseRemoteString(commandConfig.getNode("tradeMessage").getString());

        // Validate our loaded messages.
        if (legendarySpawnMessage == null)
            messageErrorArray.add("legendarySpawnMessage");
        if (legendaryCatchMessage == null)
            messageErrorArray.add("legendaryCatchMessage");
        if (legendaryDefeatMessage == null)
            messageErrorArray.add("legendaryDefeatMessage");
        if (shinyLegendarySpawnMessage == null)
            messageErrorArray.add("shinyLegendarySpawnMessage");
        if (shinyLegendaryCatchMessage == null)
            messageErrorArray.add("shinyLegendaryCatchMessage");
        if (shinyLegendaryDefeatMessage == null)
            messageErrorArray.add("shinyLegendaryDefeatMessage");
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
        if (shinyHatchMessage == null)
            messageErrorArray.add("shinyHatchMessage");
        if (tradeMessage == null)
            messageErrorArray.add("tradeMessage");

        // Print errors if something broke.
        if (!optionsErrorArray.isEmpty())
            printOptionsNodeError(optionsErrorArray);
        if (!messageErrorArray.isEmpty())
            printMessageNodeError(messageErrorArray);
    }
}
