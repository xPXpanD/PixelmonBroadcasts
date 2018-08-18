// The probably-too-complicated config handler, mark whatever. A bit less messy, but still needs work.
package rs.expand.pixelmonbroadcasts.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.plugin.PluginContainer;

// Local imports.
import rs.expand.pixelmonbroadcasts.PixelmonBroadcasts;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class ConfigMethods
{
    // Unloads all known PixelmonBroadcasts commands and their aliases, and then re-registers them.
    public static boolean registerCommands()
    {
        final PluginContainer pbcContainer = Sponge.getPluginManager().getPlugin("pixelmonbroadcasts").orElse(null);

        // Registers the base command and an alias, if a valid one is found.
        if (pbcContainer != null)
        {
            // Get a Sponge Game object.
            final Game game = Sponge.getGame();

            // Remove all existing mappings owned by Pixelmon Broadcasts, if any exist.
            CommandManager manager = game.getCommandManager();
            manager.getOwnedBy(pbcContainer).forEach(manager::removeMapping);

            // Do we have a valid alias. Register with Sponge. If we don't, just do the base command and nothing else.
            if (commandAlias != null && !commandAlias.equals("pixelmonbroadcasts"))
                Sponge.getCommandManager().register(pbcContainer, basecommand, "pixelmonbroadcasts", commandAlias);
            else
                Sponge.getCommandManager().register(pbcContainer, basecommand, "pixelmonbroadcasts");

            // Print a message with what we did.
            if (commandAlias != null && !commandAlias.equals("pixelmonbroadcasts"))
                printBasicMessage("    §aRegistered main command as §2/pixelmonbroadcasts§a, alias §2/" + commandAlias);
            else
                printBasicMessage("    §aRegistered main command as §2/pixelmonbroadcasts§a, no alias.");

            return true;
        }
        else
        {
            printBasicMessage("    §cCommand (re-)initialization failed. Please report, this is a bug.");
            printBasicMessage("    §cSidemod commands are likely dead. A reboot may work.");

            return false;
        }
    }

    // Called during initial setup, either when the server is booting up or when /pbroadcasts reload has been executed.
    public static boolean tryCreateAndLoadConfigs()
    {
        // Print a message to squeeze between the messages of whatever called the (re-)load.
        printBasicMessage("--> §aLoading and validating Pixelmon Broadcasts settings...");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        try
        {
            Files.createDirectory(Paths.get(configPathAsString));
            printBasicMessage("--> §aPixelmon Broadcasts folder not found, making a new one for configs...");
        }
        catch (final IOException ignored)
        {}

        // Let's try creating/loading all the configs. Break out with a return if something goes wrong.
        try
        {
            if (Files.notExists(settingsPath))
            {

                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                printBasicMessage("    §eNo settings file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/settings.conf"),
                        Paths.get(configPathAsString, "settings.conf"));
            }

            if (Files.notExists(broadcastsPath))
            {
                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                printBasicMessage("    §eNo broadcasts file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/broadcasts.conf"),
                        Paths.get(configPathAsString, "broadcasts.conf"));
            }

            if (Files.notExists(messagesPath))
            {
                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                printBasicMessage("    §eNo messages file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/messages.conf"),
                        Paths.get(configPathAsString, "messages.conf"));
            }

            // Load configs to memory.
            settingsConfig = PixelmonBroadcasts.settingsLoader.load();
            broadcastsConfig = PixelmonBroadcasts.broadcastsLoader.load();
            messagesConfig = PixelmonBroadcasts.messagesLoader.load();
        }
        catch (final IOException F)
        {
            // Print errors and then throw a stack trace into the console. Ugly, but potentially helpful.
            printBasicMessage("    §cOne or more configs could not be set up. Please report this.");
            printBasicMessage("    §cAdd any useful info you may have (operating system?). Stack trace:");
            F.printStackTrace();

            // Exit out of the method early, we can't continue.
            return false;
        }

        // If we haven't returned out of the method yet, no errors were found! Load settings file stuff into memory.
        // TODO: Make this less messy. It works, but there's probably more efficient ways to read this.

        /*               *\
           initial setup
        \*               */

        // To start, set up an error array. Check later, and print errors to the console if stuff broke.
        final List<String> optionsErrorArray = new ArrayList<>();

        // Load in and validate the command alias and config version. (version is not yet implemented, not necessary)
        PixelmonBroadcasts.commandAlias =
                settingsConfig.getNode("commandAlias").getString();

        //PixelmonBroadcasts.configVersion =
        //        interpretInteger(settingsConfig.getNode("configVersion").getString());

        // Show errors if any of these main variables are broken.
        if (commandAlias == null)
            printBasicMessage("    §cCould not read config node \"§4commandAlias§c\". Alias support disabled.");

        /*                 *\
           normal settings
        \*                 */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String normalCatchOptions = settingsConfig.getNode("normalCatchOptions").getString();
        if (normalCatchOptions == null)
            optionsErrorArray.add("normalCatchOptions");
        else
        {
            normalCatchOptions = normalCatchOptions.toLowerCase();
            PixelmonBroadcasts.logNormalCatches = normalCatchOptions.contains("log");
            PixelmonBroadcasts.showNormalCatches = normalCatchOptions.contains("show");
            PixelmonBroadcasts.hoverNormalCatches = normalCatchOptions.contains("hover");
            PixelmonBroadcasts.revealNormalCatches = normalCatchOptions.contains("reveal");
        }

        String normalBlackoutOptions = settingsConfig.getNode("normalBlackoutOptions").getString();
        if (normalBlackoutOptions == null)
            optionsErrorArray.add("normalBlackoutOptions");
        else
        {
            normalBlackoutOptions = normalBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logNormalBlackouts = normalBlackoutOptions.contains("log");
            PixelmonBroadcasts.showNormalBlackouts = normalBlackoutOptions.contains("show");
            PixelmonBroadcasts.hoverNormalBlackouts = normalBlackoutOptions.contains("hover");
            PixelmonBroadcasts.revealNormalBlackouts = normalBlackoutOptions.contains("reveal");
        }

        /*                    *\
           legendary settings
        \*                    */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String legendarySpawnOptions = settingsConfig.getNode("legendarySpawnOptions").getString();
        if (legendarySpawnOptions == null)
            optionsErrorArray.add("legendarySpawnOptions");
        else
        {
            legendarySpawnOptions = legendarySpawnOptions.toLowerCase();
            PixelmonBroadcasts.logLegendarySpawns = legendarySpawnOptions.contains("log");
            PixelmonBroadcasts.showLegendarySpawns = legendarySpawnOptions.contains("show");
            PixelmonBroadcasts.hoverLegendarySpawns = legendarySpawnOptions.contains("hover");
        }

        String legendaryChallengeOptions = settingsConfig.getNode("legendaryChallengeOptions").getString();
        if (legendaryChallengeOptions == null)
            optionsErrorArray.add("legendaryChallengeOptions");
        else
        {
            legendaryChallengeOptions = legendaryChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logLegendaryChallenges = legendaryChallengeOptions.contains("log");
            PixelmonBroadcasts.showLegendaryChallenges = legendaryChallengeOptions.contains("show");
            PixelmonBroadcasts.hoverLegendaryChallenges = legendaryChallengeOptions.contains("hover");
        }

        String legendaryCatchOptions = settingsConfig.getNode("legendaryCatchOptions").getString();
        if (legendaryCatchOptions == null)
            optionsErrorArray.add("legendaryCatchOptions");
        else
        {
            legendaryCatchOptions = legendaryCatchOptions.toLowerCase();
            PixelmonBroadcasts.logLegendaryCatches = legendaryCatchOptions.contains("log");
            PixelmonBroadcasts.showLegendaryCatches = legendaryCatchOptions.contains("show");
            PixelmonBroadcasts.hoverLegendaryCatches = legendaryCatchOptions.contains("hover");
            PixelmonBroadcasts.revealLegendaryCatches = legendaryCatchOptions.contains("reveal");
        }

        String legendaryVictoryOptions = settingsConfig.getNode("legendaryVictoryOptions").getString();
        if (legendaryVictoryOptions == null)
            optionsErrorArray.add("legendaryVictoryOptions");
        else
        {
            legendaryVictoryOptions = legendaryVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logLegendaryVictories = legendaryVictoryOptions.contains("log");
            PixelmonBroadcasts.showLegendaryVictories = legendaryVictoryOptions.contains("show");
            PixelmonBroadcasts.hoverLegendaryVictories = legendaryVictoryOptions.contains("hover");
            PixelmonBroadcasts.revealLegendaryVictories = legendaryVictoryOptions.contains("reveal");
        }

        String legendaryBlackoutOptions = settingsConfig.getNode("legendaryBlackoutOptions").getString();
        if (legendaryBlackoutOptions == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        else
        {
            legendaryBlackoutOptions = legendaryBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logLegendaryBlackouts = legendaryBlackoutOptions.contains("log");
            PixelmonBroadcasts.showLegendaryBlackouts = legendaryBlackoutOptions.contains("show");
            PixelmonBroadcasts.hoverLegendaryBlackouts = legendaryBlackoutOptions.contains("hover");
            PixelmonBroadcasts.revealLegendaryBlackouts = legendaryBlackoutOptions.contains("reveal");
        }

        String legendaryForfeitOptions = settingsConfig.getNode("legendaryForfeitOptions").getString();
        if (legendaryForfeitOptions == null)
            optionsErrorArray.add("legendaryForfeitOptions");
        else
        {
            legendaryForfeitOptions = legendaryForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logLegendaryForfeits = legendaryForfeitOptions.contains("log");
            PixelmonBroadcasts.showLegendaryForfeits = legendaryForfeitOptions.contains("show");
            PixelmonBroadcasts.hoverLegendaryForfeits = legendaryForfeitOptions.contains("hover");
            PixelmonBroadcasts.revealLegendaryForfeits = legendaryForfeitOptions.contains("reveal");
        }

        /*                          *\
           shiny legendary settings
        \*                          */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String shinyLegendarySpawnOptions = settingsConfig.getNode("shinyLegendarySpawnOptions").getString();
        if (shinyLegendarySpawnOptions == null)
            optionsErrorArray.add("shinyLegendarySpawnOptions");
        else
        {
            shinyLegendarySpawnOptions = shinyLegendarySpawnOptions.toLowerCase();
            PixelmonBroadcasts.logShinyLegendarySpawns = shinyLegendarySpawnOptions.contains("log");
            PixelmonBroadcasts.showShinyLegendarySpawns = shinyLegendarySpawnOptions.contains("show");
            PixelmonBroadcasts.hoverShinyLegendarySpawns = shinyLegendarySpawnOptions.contains("hover");
        }

        String shinyLegendaryChallengeOptions = settingsConfig.getNode("shinyLegendaryChallengeOptions").getString();
        if (shinyLegendaryChallengeOptions == null)
            optionsErrorArray.add("shinyLegendaryChallengeOptions");
        else
        {
            shinyLegendaryChallengeOptions = shinyLegendaryChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logShinyLegendaryChallenges = shinyLegendaryChallengeOptions.contains("log");
            PixelmonBroadcasts.showShinyLegendaryChallenges = shinyLegendaryChallengeOptions.contains("show");
            PixelmonBroadcasts.hoverShinyLegendaryChallenges = shinyLegendaryChallengeOptions.contains("hover");
        }

        String shinyLegendaryCatchOptions = settingsConfig.getNode("shinyLegendaryCatchOptions").getString();
        if (shinyLegendaryCatchOptions == null)
            optionsErrorArray.add("shinyLegendaryCatchOptions");
        else
        {
            shinyLegendaryCatchOptions = shinyLegendaryCatchOptions.toLowerCase();
            PixelmonBroadcasts.logShinyLegendaryCatches = shinyLegendaryCatchOptions.contains("log");
            PixelmonBroadcasts.showShinyLegendaryCatches = shinyLegendaryCatchOptions.contains("show");
            PixelmonBroadcasts.hoverShinyLegendaryCatches = shinyLegendaryCatchOptions.contains("hover");
            PixelmonBroadcasts.revealShinyLegendaryCatches = shinyLegendaryCatchOptions.contains("reveal");
        }

        String shinyLegendaryVictoryOptions = settingsConfig.getNode("shinyLegendaryVictoryOptions").getString();
        if (shinyLegendaryVictoryOptions == null)
            optionsErrorArray.add("shinyLegendaryVictoryOptions");
        else
        {
            shinyLegendaryVictoryOptions = shinyLegendaryVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logShinyLegendaryVictories = shinyLegendaryVictoryOptions.contains("log");
            PixelmonBroadcasts.showShinyLegendaryVictories = shinyLegendaryVictoryOptions.contains("show");
            PixelmonBroadcasts.hoverShinyLegendaryVictories = shinyLegendaryVictoryOptions.contains("hover");
            PixelmonBroadcasts.revealShinyLegendaryVictories = shinyLegendaryVictoryOptions.contains("reveal");
        }

        String shinyLegendaryBlackoutOptions = settingsConfig.getNode("shinyLegendaryBlackoutOptions").getString();
        if (shinyLegendaryBlackoutOptions == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        else
        {
            shinyLegendaryBlackoutOptions = shinyLegendaryBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logShinyLegendaryBlackouts = shinyLegendaryBlackoutOptions.contains("log");
            PixelmonBroadcasts.showShinyLegendaryBlackouts = shinyLegendaryBlackoutOptions.contains("show");
            PixelmonBroadcasts.hoverShinyLegendaryBlackouts = shinyLegendaryBlackoutOptions.contains("hover");
            PixelmonBroadcasts.revealShinyLegendaryBlackouts = shinyLegendaryBlackoutOptions.contains("reveal");
        }

        String shinyLegendaryForfeitOptions = settingsConfig.getNode("shinyLegendaryForfeitOptions").getString();
        if (shinyLegendaryForfeitOptions == null)
            optionsErrorArray.add("shinyLegendaryForfeitOptions");
        else
        {
            shinyLegendaryForfeitOptions = shinyLegendaryForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logShinyLegendaryForfeits = shinyLegendaryForfeitOptions.contains("log");
            PixelmonBroadcasts.showShinyLegendaryForfeits = shinyLegendaryForfeitOptions.contains("show");
            PixelmonBroadcasts.hoverShinyLegendaryForfeits = shinyLegendaryForfeitOptions.contains("hover");
            PixelmonBroadcasts.revealShinyLegendaryForfeits = shinyLegendaryForfeitOptions.contains("reveal");
        }

        /*                *\
           shiny settings
        \*                */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String shinySpawnOptions = settingsConfig.getNode("shinySpawnOptions").getString();
        if (shinySpawnOptions == null)
            optionsErrorArray.add("shinySpawnOptions");
        else
        {
            shinySpawnOptions = shinySpawnOptions.toLowerCase();
            PixelmonBroadcasts.logShinySpawns = shinySpawnOptions.contains("log");
            PixelmonBroadcasts.showShinySpawns = shinySpawnOptions.contains("show");
            PixelmonBroadcasts.hoverShinySpawns = shinySpawnOptions.contains("hover");
        }

        String shinyChallengeOptions = settingsConfig.getNode("shinyChallengeOptions").getString();
        if (shinyChallengeOptions == null)
            optionsErrorArray.add("shinyChallengeOptions");
        else
        {
            shinyChallengeOptions = shinyChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logShinyChallenges = shinyChallengeOptions.contains("log");
            PixelmonBroadcasts.showShinyChallenges = shinyChallengeOptions.contains("show");
            PixelmonBroadcasts.hoverShinyChallenges = shinyChallengeOptions.contains("hover");
        }

        String shinyCatchOptions = settingsConfig.getNode("shinyCatchOptions").getString();
        if (shinyCatchOptions == null)
            optionsErrorArray.add("shinyCatchOptions");
        else
        {
            shinyCatchOptions = shinyCatchOptions.toLowerCase();
            PixelmonBroadcasts.logShinyCatches = shinyCatchOptions.contains("log");
            PixelmonBroadcasts.showShinyCatches = shinyCatchOptions.contains("show");
            PixelmonBroadcasts.hoverShinyCatches = shinyCatchOptions.contains("hover");
            PixelmonBroadcasts.revealShinyCatches = shinyCatchOptions.contains("reveal");
        }

        String shinyVictoryOptions = settingsConfig.getNode("shinyVictoryOptions").getString();
        if (shinyVictoryOptions == null)
            optionsErrorArray.add("shinyVictoryOptions");
        else
        {
            shinyVictoryOptions = shinyVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logShinyVictories = shinyVictoryOptions.contains("log");
            PixelmonBroadcasts.showShinyVictories = shinyVictoryOptions.contains("show");
            PixelmonBroadcasts.hoverShinyVictories = shinyVictoryOptions.contains("hover");
            PixelmonBroadcasts.revealShinyVictories = shinyVictoryOptions.contains("reveal");
        }

        String shinyBlackoutOptions = settingsConfig.getNode("shinyBlackoutOptions").getString();
        if (shinyBlackoutOptions == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        else
        {
            shinyBlackoutOptions = shinyBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logShinyBlackouts = shinyBlackoutOptions.contains("log");
            PixelmonBroadcasts.showShinyBlackouts = shinyBlackoutOptions.contains("show");
            PixelmonBroadcasts.hoverShinyBlackouts = shinyBlackoutOptions.contains("hover");
            PixelmonBroadcasts.revealShinyBlackouts = shinyBlackoutOptions.contains("reveal");
        }

        String shinyForfeitOptions = settingsConfig.getNode("shinyForfeitOptions").getString();
        if (shinyForfeitOptions == null)
            optionsErrorArray.add("shinyForfeitOptions");
        else
        {
            shinyForfeitOptions = shinyForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logShinyForfeits = shinyForfeitOptions.contains("log");
            PixelmonBroadcasts.showShinyForfeits = shinyForfeitOptions.contains("show");
            PixelmonBroadcasts.hoverShinyForfeits = shinyForfeitOptions.contains("hover");
            PixelmonBroadcasts.revealShinyForfeits = shinyForfeitOptions.contains("reveal");
        }

        /*               *\
           boss settings
        \*               */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String bossSpawnOptions = settingsConfig.getNode("bossSpawnOptions").getString();
        if (bossSpawnOptions == null)
            optionsErrorArray.add("bossSpawnOptions");
        else
        {
            bossSpawnOptions = bossSpawnOptions.toLowerCase();
            PixelmonBroadcasts.logBossSpawns = bossSpawnOptions.contains("log");
            PixelmonBroadcasts.showBossSpawns = bossSpawnOptions.contains("show");
            PixelmonBroadcasts.hoverBossSpawns = bossSpawnOptions.contains("hover");
        }

        String bossChallengeOptions = settingsConfig.getNode("bossChallengeOptions").getString();
        if (bossChallengeOptions == null)
            optionsErrorArray.add("bossChallengeOptions");
        else
        {
            bossChallengeOptions = bossChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logBossChallenges = bossChallengeOptions.contains("log");
            PixelmonBroadcasts.showBossChallenges = bossChallengeOptions.contains("show");
            PixelmonBroadcasts.hoverBossChallenges = bossChallengeOptions.contains("hover");
        }

        String bossVictoryOptions = settingsConfig.getNode("bossVictoryOptions").getString();
        if (bossVictoryOptions == null)
            optionsErrorArray.add("bossVictoryOptions");
        else
        {
            bossVictoryOptions = bossVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logBossVictories = bossVictoryOptions.contains("log");
            PixelmonBroadcasts.showBossVictories = bossVictoryOptions.contains("show");
            PixelmonBroadcasts.hoverBossVictories = bossVictoryOptions.contains("hover");
            PixelmonBroadcasts.revealBossVictories = bossVictoryOptions.contains("reveal");
        }

        String bossBlackoutOptions = settingsConfig.getNode("bossBlackoutOptions").getString();
        if (bossBlackoutOptions == null)
            optionsErrorArray.add("bossBlackoutOptions");
        else
        {
            bossBlackoutOptions = bossBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logBossBlackouts = bossBlackoutOptions.contains("log");
            PixelmonBroadcasts.showBossBlackouts = bossBlackoutOptions.contains("show");
            PixelmonBroadcasts.hoverBossBlackouts = bossBlackoutOptions.contains("hover");
            PixelmonBroadcasts.revealBossBlackouts = bossBlackoutOptions.contains("reveal");
        }

        String bossForfeitOptions = settingsConfig.getNode("bossForfeitOptions").getString();
        if (bossForfeitOptions == null)
            optionsErrorArray.add("bossForfeitOptions");
        else
        {
            bossForfeitOptions = bossForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logBossForfeits = bossForfeitOptions.contains("log");
            PixelmonBroadcasts.showBossForfeits = bossForfeitOptions.contains("show");
            PixelmonBroadcasts.hoverBossForfeits = bossForfeitOptions.contains("hover");
            PixelmonBroadcasts.revealBossForfeits = bossForfeitOptions.contains("reveal");
        }

        /*                       *\
           boss trainer settings
        \*                       */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String bossTrainerChallengeOptions = settingsConfig.getNode("bossTrainerChallengeOptions").getString();
        if (bossTrainerChallengeOptions == null)
            optionsErrorArray.add("bossTrainerChallengeOptions");
        else
        {
            bossTrainerChallengeOptions = bossTrainerChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logBossTrainerChallenges = bossTrainerChallengeOptions.contains("log");
            PixelmonBroadcasts.showBossTrainerChallenges = bossTrainerChallengeOptions.contains("show");
        }

        String bossTrainerVictoryOptions = settingsConfig.getNode("bossTrainerVictoryOptions").getString();
        if (bossTrainerVictoryOptions == null)
            optionsErrorArray.add("bossTrainerVictoryOptions");
        else
        {
            bossTrainerVictoryOptions = bossTrainerVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logBossTrainerVictories = bossTrainerVictoryOptions.contains("log");
            PixelmonBroadcasts.showBossTrainerVictories = bossTrainerVictoryOptions.contains("show");
        }

        String bossTrainerBlackoutOptions = settingsConfig.getNode("bossTrainerBlackoutOptions").getString();
        if (bossTrainerBlackoutOptions == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");
        else
        {
            bossTrainerBlackoutOptions = bossTrainerBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logBossTrainerBlackouts = bossTrainerBlackoutOptions.contains("log");
            PixelmonBroadcasts.showBossTrainerBlackouts = bossTrainerBlackoutOptions.contains("show");
        }

        String bossTrainerForfeitOptions = settingsConfig.getNode("bossTrainerForfeitOptions").getString();
        if (bossTrainerForfeitOptions == null)
            optionsErrorArray.add("bossTrainerForfeitOptions");
        else
        {
            bossTrainerForfeitOptions = bossTrainerForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logBossTrainerForfeits = bossTrainerForfeitOptions.contains("log");
            PixelmonBroadcasts.showBossTrainerForfeits = bossTrainerForfeitOptions.contains("show");
        }

        /*                  *\
           trainer settings
        \*                  */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String trainerChallengeOptions = settingsConfig.getNode("trainerChallengeOptions").getString();
        if (trainerChallengeOptions == null)
            optionsErrorArray.add("trainerChallengeOptions");
        else
        {
            trainerChallengeOptions = trainerChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logTrainerChallenges = trainerChallengeOptions.contains("log");
            PixelmonBroadcasts.showTrainerChallenges = trainerChallengeOptions.contains("show");
        }

        String trainerVictoryOptions = settingsConfig.getNode("trainerVictoryOptions").getString();
        if (trainerVictoryOptions == null)
            optionsErrorArray.add("trainerVictoryOptions");
        else
        {
            trainerVictoryOptions = trainerVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logTrainerVictories = trainerVictoryOptions.contains("log");
            PixelmonBroadcasts.showTrainerVictories = trainerVictoryOptions.contains("show");
        }

        String trainerBlackoutOptions = settingsConfig.getNode("trainerBlackoutOptions").getString();
        if (trainerBlackoutOptions == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        else
        {
            trainerBlackoutOptions = trainerBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logTrainerBlackouts = trainerBlackoutOptions.contains("log");
            PixelmonBroadcasts.showTrainerBlackouts = trainerBlackoutOptions.contains("show");
        }

        String trainerForfeitOptions = settingsConfig.getNode("trainerForfeitOptions").getString();
        if (trainerForfeitOptions == null)
            optionsErrorArray.add("trainerForfeitOptions");
        else
        {
            trainerForfeitOptions = trainerForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logTrainerForfeits = trainerForfeitOptions.contains("log");
            PixelmonBroadcasts.showTrainerForfeits = trainerForfeitOptions.contains("show");
        }

        /*              *\
           PvP settings
        \*              */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String pvpChallengeOptions = settingsConfig.getNode("pvpChallengeOptions").getString();
        if (pvpChallengeOptions == null)
            optionsErrorArray.add("pvpChallengeOptions");
        else
        {
            pvpChallengeOptions = pvpChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logPVPChallenges = pvpChallengeOptions.contains("log");
            PixelmonBroadcasts.showPVPChallenges = pvpChallengeOptions.contains("show");
        }

        String pvpVictoryOptions = settingsConfig.getNode("pvpVictoryOptions").getString();
        if (pvpVictoryOptions == null)
            optionsErrorArray.add("pvpVictoryOptions");
        else
        {
            pvpVictoryOptions = pvpVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logPVPVictories = pvpVictoryOptions.contains("log");
            PixelmonBroadcasts.showPVPVictories = pvpVictoryOptions.contains("show");
        }

        String pvpDrawOptions = settingsConfig.getNode("pvpDrawOptions").getString();
        if (pvpDrawOptions == null)
            optionsErrorArray.add("pvpDrawOptions");
        else
        {
            pvpDrawOptions = pvpDrawOptions.toLowerCase();
            PixelmonBroadcasts.logPVPDraws = pvpDrawOptions.contains("log");
            PixelmonBroadcasts.showPVPDraws = pvpDrawOptions.contains("show");
        }

        /*                 *\
           summon settings
        \*

        // Get options. Extract and set them, if we managed to grab them successfully.
        String birdTrioSummonOptions = settingsConfig.getNode("birdTrioSummonOptions").getString();
        if (birdTrioSummonOptions == null)
            optionsErrorArray.add("birdTrioSummonOptions");
        else
        {
            birdTrioSummonOptions = birdTrioSummonOptions.toLowerCase();
            PixelmonBroadcasts.logBirdTrioSummons = birdTrioSummonOptions.contains("log");
            PixelmonBroadcasts.showBirdTrioSummons = birdTrioSummonOptions.contains("show");
        }*/

        /*                *\
           hatch settings
        \*                */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String normalHatchOptions = settingsConfig.getNode("normalHatchOptions").getString();
        if (normalHatchOptions == null)
            optionsErrorArray.add("normalHatchOptions");
        else
        {
            normalHatchOptions = normalHatchOptions.toLowerCase();
            PixelmonBroadcasts.logNormalHatches = normalHatchOptions.contains("log");
            PixelmonBroadcasts.showNormalHatches = normalHatchOptions.contains("show");
            PixelmonBroadcasts.hoverNormalHatches = normalHatchOptions.contains("hover");
            PixelmonBroadcasts.revealNormalHatches = normalHatchOptions.contains("reveal");
        }

        String shinyHatchOptions = settingsConfig.getNode("shinyHatchOptions").getString();
        if (shinyHatchOptions == null)
            optionsErrorArray.add("shinyHatchOptions");
        else
        {
            shinyHatchOptions = shinyHatchOptions.toLowerCase();
            PixelmonBroadcasts.logShinyHatches = shinyHatchOptions.contains("log");
            PixelmonBroadcasts.showShinyHatches = shinyHatchOptions.contains("show");
            PixelmonBroadcasts.hoverShinyHatches = shinyHatchOptions.contains("hover");
            PixelmonBroadcasts.revealShinyHatches = shinyHatchOptions.contains("reveal");
        }

        /*                *\
           trade settings
        \*                */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String tradeOptions = settingsConfig.getNode("tradeOptions").getString();
        if (tradeOptions == null)
            optionsErrorArray.add("tradeOptions");
        else
        {
            // TODO: No hover option in here, for now. Might be nice, eventually.
            tradeOptions = tradeOptions.toLowerCase();
            PixelmonBroadcasts.logTrades = tradeOptions.contains("log");
            PixelmonBroadcasts.showTrades = tradeOptions.contains("show");
        }

        /*          *\
           messages
        \*          */

        // Print errors if something broke.
        if (!optionsErrorArray.isEmpty())
            printOptionsNodeError(optionsErrorArray);

        // We're done, phew. Tell the calling code.
        return true;
    }
}
