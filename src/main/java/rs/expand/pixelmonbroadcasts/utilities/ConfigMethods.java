// The probably-too-complicated config handler, mark whatever. A bit less messy, but still needs work.
package rs.expand.pixelmonbroadcasts.utilities;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.plugin.PluginContainer;
import rs.expand.pixelmonbroadcasts.PixelmonBroadcasts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printOptionsNodeError;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printUnformattedMessage;

public class ConfigMethods
{
    // Make a little converter for safely handling Strings that might have an integer value inside.
    private static Integer interpretInteger(final String input)
    {
        if (input != null && input.matches("-?[1-9]\\d*|0"))
            return Integer.parseInt(input);
        else
            return null;
    }

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
                printUnformattedMessage("    §aRegistered main command as §2/pixelmonbroadcasts§a, alias §2/" + commandAlias);
            else
                printUnformattedMessage("    §aRegistered main command as §2/pixelmonbroadcasts§a, no alias.");

            return true;
        }
        else
        {
            printUnformattedMessage("    §cCommand (re-)initialization failed. Please report, this is a bug.");
            printUnformattedMessage("    §cSidemod commands are likely dead. A reboot may work.");

            return false;
        }
    }

    // Called during initial setup, either when the server is booting up or when /pbroadcasts reload has been executed.
    public static boolean tryCreateAndLoadConfigs()
    {
        // Print a message to squeeze between the messages of whatever called the (re-)load.
        printUnformattedMessage("--> §aLoading and validating Pixelmon Broadcasts settings...");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        try
        {
            Files.createDirectory(Paths.get(configPathAsString));
            printUnformattedMessage("--> §aPixelmon Broadcasts folder not found, making a new one for configs...");
        }
        catch (final IOException ignored)
        {}

        // Let's try creating/loading all the configs. Break out with a return if something goes wrong.
        try
        {
            if (Files.notExists(broadcastsPath))
            {
                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                printUnformattedMessage("    §eNo broadcasts file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/broadcasts.conf"),
                        Paths.get(configPathAsString, "broadcasts.conf"));
            }

            if (Files.notExists(messagesPath))
            {
                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                printUnformattedMessage("    §eNo messages file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/messages.conf"),
                        Paths.get(configPathAsString, "messages.conf"));
            }

            if (Files.notExists(settingsPath))
            {

                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                printUnformattedMessage("    §eNo settings file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/settings.conf"),
                        Paths.get(configPathAsString, "settings.conf"));
            }

            // Load configs to memory.
            broadcastsConfig = PixelmonBroadcasts.broadcastsLoader.load();
            messagesConfig = PixelmonBroadcasts.messagesLoader.load();
            settingsConfig = PixelmonBroadcasts.settingsLoader.load();
        }
        catch (final IOException F)
        {
            // Print errors and then throw a stack trace into the console. Ugly, but potentially helpful.
            printUnformattedMessage("    §cOne or more configs could not be set up. Please report this.");
            printUnformattedMessage("    §cAdd any useful info you may have (operating system?). Stack trace:");
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

        // Load in and validate the config version, command alias and ability setting.
        PixelmonBroadcasts.configVersion =
                interpretInteger(settingsConfig.getNode("configVersion").getString());
        PixelmonBroadcasts.commandAlias =
                settingsConfig.getNode("commandAlias").getString();
        PixelmonBroadcasts.showAbilities =
                toBooleanObject(settingsConfig.getNode("showAbilities").getString());

        // Show errors if any of these main variables are broken.
        if (configVersion == null)
            printUnformattedMessage("    §cCould not read config node \"§4configVersion§c\".");
        if (commandAlias == null)
            printUnformattedMessage("    §cCould not read config node \"§4commandAlias§c\". Alias support disabled.");
        if (showAbilities == null)
        {
            printUnformattedMessage("    §cCould not read config node \"§4showAbilities§c\". Falling back, enabling.");
            showAbilities = true;
        }

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
            PixelmonBroadcasts.printNormalCatches = normalCatchOptions.contains("chat");
            PixelmonBroadcasts.notifyNormalCatches = normalCatchOptions.contains("notice");
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
            PixelmonBroadcasts.printNormalBlackouts = normalBlackoutOptions.contains("chat");
            PixelmonBroadcasts.notifyNormalBlackouts = normalBlackoutOptions.contains("notice");
            PixelmonBroadcasts.hoverNormalBlackouts = normalBlackoutOptions.contains("hover");
            PixelmonBroadcasts.revealNormalBlackouts = normalBlackoutOptions.contains("reveal");
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
            PixelmonBroadcasts.printShinySpawns = shinySpawnOptions.contains("chat");
            PixelmonBroadcasts.notifyShinySpawns = shinySpawnOptions.contains("notice");
            PixelmonBroadcasts.hoverShinySpawns = shinySpawnOptions.contains("hover");
        }

        String shinyChallengeOptions = settingsConfig.getNode("shinyChallengeOptions").getString();
        if (shinyChallengeOptions == null)
            optionsErrorArray.add("shinyChallengeOptions");
        else
        {
            shinyChallengeOptions = shinyChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logShinyChallenges = shinyChallengeOptions.contains("log");
            PixelmonBroadcasts.printShinyChallenges = shinyChallengeOptions.contains("chat");
            PixelmonBroadcasts.notifyShinyChallenges = shinyChallengeOptions.contains("notice");
            PixelmonBroadcasts.hoverShinyChallenges = shinyChallengeOptions.contains("hover");
        }

        String shinyCatchOptions = settingsConfig.getNode("shinyCatchOptions").getString();
        if (shinyCatchOptions == null)
            optionsErrorArray.add("shinyCatchOptions");
        else
        {
            shinyCatchOptions = shinyCatchOptions.toLowerCase();
            PixelmonBroadcasts.logShinyCatches = shinyCatchOptions.contains("log");
            PixelmonBroadcasts.printShinyCatches = shinyCatchOptions.contains("chat");
            PixelmonBroadcasts.notifyShinyCatches = shinyCatchOptions.contains("notice");
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
            PixelmonBroadcasts.printShinyVictories = shinyVictoryOptions.contains("chat");
            PixelmonBroadcasts.notifyShinyVictories = shinyVictoryOptions.contains("notice");
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
            PixelmonBroadcasts.printShinyBlackouts = shinyBlackoutOptions.contains("chat");
            PixelmonBroadcasts.notifyShinyBlackouts = shinyBlackoutOptions.contains("notice");
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
            PixelmonBroadcasts.printShinyForfeits = shinyForfeitOptions.contains("chat");
            PixelmonBroadcasts.notifyShinyForfeits = shinyForfeitOptions.contains("notice");
            PixelmonBroadcasts.hoverShinyForfeits = shinyForfeitOptions.contains("hover");
            PixelmonBroadcasts.revealShinyForfeits = shinyForfeitOptions.contains("reveal");
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
            PixelmonBroadcasts.printLegendarySpawns = legendarySpawnOptions.contains("chat");
            PixelmonBroadcasts.notifyLegendarySpawns = legendarySpawnOptions.contains("notice");
            PixelmonBroadcasts.hoverLegendarySpawns = legendarySpawnOptions.contains("hover");
        }

        String legendaryChallengeOptions = settingsConfig.getNode("legendaryChallengeOptions").getString();
        if (legendaryChallengeOptions == null)
            optionsErrorArray.add("legendaryChallengeOptions");
        else
        {
            legendaryChallengeOptions = legendaryChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logLegendaryChallenges = legendaryChallengeOptions.contains("log");
            PixelmonBroadcasts.printLegendaryChallenges = legendaryChallengeOptions.contains("chat");
            PixelmonBroadcasts.notifyLegendaryChallenges = legendaryChallengeOptions.contains("notify");
            PixelmonBroadcasts.hoverLegendaryChallenges = legendaryChallengeOptions.contains("hover");
        }

        String legendaryCatchOptions = settingsConfig.getNode("legendaryCatchOptions").getString();
        if (legendaryCatchOptions == null)
            optionsErrorArray.add("legendaryCatchOptions");
        else
        {
            legendaryCatchOptions = legendaryCatchOptions.toLowerCase();
            PixelmonBroadcasts.logLegendaryCatches = legendaryCatchOptions.contains("log");
            PixelmonBroadcasts.printLegendaryCatches = legendaryCatchOptions.contains("chat");
            PixelmonBroadcasts.notifyLegendaryCatches = legendaryCatchOptions.contains("notice");
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
            PixelmonBroadcasts.printLegendaryVictories = legendaryVictoryOptions.contains("chat");
            PixelmonBroadcasts.notifyLegendaryVictories = legendaryVictoryOptions.contains("notice");
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
            PixelmonBroadcasts.printLegendaryBlackouts = legendaryBlackoutOptions.contains("chat");
            PixelmonBroadcasts.notifyLegendaryBlackouts = legendaryBlackoutOptions.contains("notice");
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
            PixelmonBroadcasts.printLegendaryForfeits = legendaryForfeitOptions.contains("chat");
            PixelmonBroadcasts.notifyLegendaryForfeits = legendaryForfeitOptions.contains("notice");
            PixelmonBroadcasts.hoverLegendaryForfeits = legendaryForfeitOptions.contains("hover");
            PixelmonBroadcasts.revealLegendaryForfeits = legendaryForfeitOptions.contains("reveal");
        }

        /*                    *\
           ultra beast settings
        \*                    */

        // Get options. Extract and set them, if we managed to grab them successfully.
        String ultraBeastSpawnOptions = settingsConfig.getNode("ultraBeastSpawnOptions").getString();
        if (ultraBeastSpawnOptions == null)
            optionsErrorArray.add("ultraBeastSpawnOptions");
        else
        {
            ultraBeastSpawnOptions = ultraBeastSpawnOptions.toLowerCase();
            PixelmonBroadcasts.logUltraBeastSpawns = ultraBeastSpawnOptions.contains("log");
            PixelmonBroadcasts.printUltraBeastSpawns = ultraBeastSpawnOptions.contains("chat");
            PixelmonBroadcasts.notifyUltraBeastSpawns = ultraBeastSpawnOptions.contains("notice");
            PixelmonBroadcasts.hoverUltraBeastSpawns = ultraBeastSpawnOptions.contains("hover");
        }

        String ultraBeastChallengeOptions = settingsConfig.getNode("ultraBeastChallengeOptions").getString();
        if (ultraBeastChallengeOptions == null)
            optionsErrorArray.add("ultraBeastChallengeOptions");
        else
        {
            ultraBeastChallengeOptions = ultraBeastChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logUltraBeastChallenges = ultraBeastChallengeOptions.contains("log");
            PixelmonBroadcasts.printUltraBeastChallenges = ultraBeastChallengeOptions.contains("chat");
            PixelmonBroadcasts.notifyUltraBeastChallenges = ultraBeastChallengeOptions.contains("notice");
            PixelmonBroadcasts.hoverUltraBeastChallenges = ultraBeastChallengeOptions.contains("hover");
        }

        String ultraBeastCatchOptions = settingsConfig.getNode("ultraBeastCatchOptions").getString();
        if (ultraBeastCatchOptions == null)
            optionsErrorArray.add("ultraBeastCatchOptions");
        else
        {
            ultraBeastCatchOptions = ultraBeastCatchOptions.toLowerCase();
            PixelmonBroadcasts.logUltraBeastCatches = ultraBeastCatchOptions.contains("log");
            PixelmonBroadcasts.printUltraBeastCatches = ultraBeastCatchOptions.contains("chat");
            PixelmonBroadcasts.notifyUltraBeastCatches = ultraBeastCatchOptions.contains("notice");
            PixelmonBroadcasts.hoverUltraBeastCatches = ultraBeastCatchOptions.contains("hover");
            PixelmonBroadcasts.revealUltraBeastCatches = ultraBeastCatchOptions.contains("reveal");
        }

        String ultraBeastVictoryOptions = settingsConfig.getNode("ultraBeastVictoryOptions").getString();
        if (ultraBeastVictoryOptions == null)
            optionsErrorArray.add("ultraBeastVictoryOptions");
        else
        {
            ultraBeastVictoryOptions = ultraBeastVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logUltraBeastVictories = ultraBeastVictoryOptions.contains("log");
            PixelmonBroadcasts.printUltraBeastVictories = ultraBeastVictoryOptions.contains("chat");
            PixelmonBroadcasts.notifyUltraBeastVictories = ultraBeastVictoryOptions.contains("notice");
            PixelmonBroadcasts.hoverUltraBeastVictories = ultraBeastVictoryOptions.contains("hover");
            PixelmonBroadcasts.revealUltraBeastVictories = ultraBeastVictoryOptions.contains("reveal");
        }

        String ultraBeastBlackoutOptions = settingsConfig.getNode("ultraBeastBlackoutOptions").getString();
        if (ultraBeastBlackoutOptions == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        else
        {
            ultraBeastBlackoutOptions = ultraBeastBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logUltraBeastBlackouts = ultraBeastBlackoutOptions.contains("log");
            PixelmonBroadcasts.printUltraBeastBlackouts = ultraBeastBlackoutOptions.contains("chat");
            PixelmonBroadcasts.notifyUltraBeastBlackouts = ultraBeastBlackoutOptions.contains("notice");
            PixelmonBroadcasts.hoverUltraBeastBlackouts = ultraBeastBlackoutOptions.contains("hover");
            PixelmonBroadcasts.revealUltraBeastBlackouts = ultraBeastBlackoutOptions.contains("reveal");
        }

        String ultraBeastForfeitOptions = settingsConfig.getNode("ultraBeastForfeitOptions").getString();
        if (ultraBeastForfeitOptions == null)
            optionsErrorArray.add("ultraBeastForfeitOptions");
        else
        {
            ultraBeastForfeitOptions = ultraBeastForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logUltraBeastForfeits = ultraBeastForfeitOptions.contains("log");
            PixelmonBroadcasts.printUltraBeastForfeits = ultraBeastForfeitOptions.contains("chat");
            PixelmonBroadcasts.notifyUltraBeastForfeits = ultraBeastForfeitOptions.contains("notice");
            PixelmonBroadcasts.hoverUltraBeastForfeits = ultraBeastForfeitOptions.contains("hover");
            PixelmonBroadcasts.revealUltraBeastForfeits = ultraBeastForfeitOptions.contains("reveal");
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
            PixelmonBroadcasts.printBossSpawns = bossSpawnOptions.contains("chat");
            PixelmonBroadcasts.notifyBossSpawns = bossSpawnOptions.contains("notice");
            PixelmonBroadcasts.hoverBossSpawns = bossSpawnOptions.contains("hover");
        }

        String bossChallengeOptions = settingsConfig.getNode("bossChallengeOptions").getString();
        if (bossChallengeOptions == null)
            optionsErrorArray.add("bossChallengeOptions");
        else
        {
            bossChallengeOptions = bossChallengeOptions.toLowerCase();
            PixelmonBroadcasts.logBossChallenges = bossChallengeOptions.contains("log");
            PixelmonBroadcasts.printBossChallenges = bossChallengeOptions.contains("chat");
            PixelmonBroadcasts.notifyBossChallenges = bossChallengeOptions.contains("notice");
            PixelmonBroadcasts.hoverBossChallenges = bossChallengeOptions.contains("hover");
        }

        String bossVictoryOptions = settingsConfig.getNode("bossVictoryOptions").getString();
        if (bossVictoryOptions == null)
            optionsErrorArray.add("bossVictoryOptions");
        else
        {
            bossVictoryOptions = bossVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logBossVictories = bossVictoryOptions.contains("log");
            PixelmonBroadcasts.printBossVictories = bossVictoryOptions.contains("chat");
            PixelmonBroadcasts.notifyBossVictories = bossVictoryOptions.contains("notice");
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
            PixelmonBroadcasts.printBossBlackouts = bossBlackoutOptions.contains("chat");
            PixelmonBroadcasts.notifyBossBlackouts = bossBlackoutOptions.contains("notice");
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
            PixelmonBroadcasts.printBossForfeits = bossForfeitOptions.contains("chat");
            PixelmonBroadcasts.notifyBossForfeits = bossForfeitOptions.contains("notice");
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
            PixelmonBroadcasts.printBossTrainerChallenges = bossTrainerChallengeOptions.contains("chat");
            PixelmonBroadcasts.notifyBossTrainerChallenges = bossTrainerChallengeOptions.contains("notice");
        }

        String bossTrainerVictoryOptions = settingsConfig.getNode("bossTrainerVictoryOptions").getString();
        if (bossTrainerVictoryOptions == null)
            optionsErrorArray.add("bossTrainerVictoryOptions");
        else
        {
            bossTrainerVictoryOptions = bossTrainerVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logBossTrainerVictories = bossTrainerVictoryOptions.contains("log");
            PixelmonBroadcasts.printBossTrainerVictories = bossTrainerVictoryOptions.contains("chat");
            PixelmonBroadcasts.notifyBossTrainerVictories = bossTrainerVictoryOptions.contains("notice");
        }

        String bossTrainerBlackoutOptions = settingsConfig.getNode("bossTrainerBlackoutOptions").getString();
        if (bossTrainerBlackoutOptions == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");
        else
        {
            bossTrainerBlackoutOptions = bossTrainerBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logBossTrainerBlackouts = bossTrainerBlackoutOptions.contains("log");
            PixelmonBroadcasts.printBossTrainerBlackouts = bossTrainerBlackoutOptions.contains("chat");
            PixelmonBroadcasts.notifyBossTrainerBlackouts = bossTrainerBlackoutOptions.contains("notice");
        }

        String bossTrainerForfeitOptions = settingsConfig.getNode("bossTrainerForfeitOptions").getString();
        if (bossTrainerForfeitOptions == null)
            optionsErrorArray.add("bossTrainerForfeitOptions");
        else
        {
            bossTrainerForfeitOptions = bossTrainerForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logBossTrainerForfeits = bossTrainerForfeitOptions.contains("log");
            PixelmonBroadcasts.printBossTrainerForfeits = bossTrainerForfeitOptions.contains("chat");
            PixelmonBroadcasts.notifyBossTrainerForfeits = bossTrainerForfeitOptions.contains("notify");
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
            PixelmonBroadcasts.printTrainerChallenges = trainerChallengeOptions.contains("chat");
            PixelmonBroadcasts.notifyTrainerChallenges = trainerChallengeOptions.contains("notice");
        }

        String trainerVictoryOptions = settingsConfig.getNode("trainerVictoryOptions").getString();
        if (trainerVictoryOptions == null)
            optionsErrorArray.add("trainerVictoryOptions");
        else
        {
            trainerVictoryOptions = trainerVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logTrainerVictories = trainerVictoryOptions.contains("log");
            PixelmonBroadcasts.printTrainerVictories = trainerVictoryOptions.contains("chat");
            PixelmonBroadcasts.notifyTrainerVictories = trainerVictoryOptions.contains("notice");
        }

        String trainerBlackoutOptions = settingsConfig.getNode("trainerBlackoutOptions").getString();
        if (trainerBlackoutOptions == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        else
        {
            trainerBlackoutOptions = trainerBlackoutOptions.toLowerCase();
            PixelmonBroadcasts.logTrainerBlackouts = trainerBlackoutOptions.contains("log");
            PixelmonBroadcasts.printTrainerBlackouts = trainerBlackoutOptions.contains("chat");
            PixelmonBroadcasts.notifyTrainerBlackouts = trainerBlackoutOptions.contains("notice");
        }

        String trainerForfeitOptions = settingsConfig.getNode("trainerForfeitOptions").getString();
        if (trainerForfeitOptions == null)
            optionsErrorArray.add("trainerForfeitOptions");
        else
        {
            trainerForfeitOptions = trainerForfeitOptions.toLowerCase();
            PixelmonBroadcasts.logTrainerForfeits = trainerForfeitOptions.contains("log");
            PixelmonBroadcasts.printTrainerForfeits = trainerForfeitOptions.contains("chat");
            PixelmonBroadcasts.notifyTrainerForfeits = trainerForfeitOptions.contains("notice");
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
            PixelmonBroadcasts.printPVPChallenges = pvpChallengeOptions.contains("chat");
            PixelmonBroadcasts.notifyPVPChallenges = pvpChallengeOptions.contains("notice");
        }

        String pvpVictoryOptions = settingsConfig.getNode("pvpVictoryOptions").getString();
        if (pvpVictoryOptions == null)
            optionsErrorArray.add("pvpVictoryOptions");
        else
        {
            pvpVictoryOptions = pvpVictoryOptions.toLowerCase();
            PixelmonBroadcasts.logPVPVictories = pvpVictoryOptions.contains("log");
            PixelmonBroadcasts.printPVPVictories = pvpVictoryOptions.contains("chat");
            PixelmonBroadcasts.notifyPVPVictories = pvpVictoryOptions.contains("notice");
        }

        String pvpDrawOptions = settingsConfig.getNode("pvpDrawOptions").getString();
        if (pvpDrawOptions == null)
            optionsErrorArray.add("pvpDrawOptions");
        else
        {
            pvpDrawOptions = pvpDrawOptions.toLowerCase();
            PixelmonBroadcasts.logPVPDraws = pvpDrawOptions.contains("log");
            PixelmonBroadcasts.printPVPDraws = pvpDrawOptions.contains("chat");
            PixelmonBroadcasts.notifyPVPDraws = pvpDrawOptions.contains("notice");
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
            PixelmonBroadcasts.printBirdTrioSummons = birdTrioSummonOptions.contains("chat");
            PixelmonBroadcasts.notifyBirdTrioSummons = birdTrioSummonOptions.contains("notice");
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
            PixelmonBroadcasts.printNormalHatches = normalHatchOptions.contains("chat");
            PixelmonBroadcasts.notifyNormalHatches = normalHatchOptions.contains("notice");
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
            PixelmonBroadcasts.printShinyHatches = shinyHatchOptions.contains("chat");
            PixelmonBroadcasts.notifyShinyHatches = shinyHatchOptions.contains("notice");
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
            PixelmonBroadcasts.printTrades = tradeOptions.contains("chat");
            PixelmonBroadcasts.notifyTrades = tradeOptions.contains("notice");
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
