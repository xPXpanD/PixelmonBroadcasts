// The probably-too-complicated config handler. This doesn't feel like the correct way to do it, but it works well enough.
package rs.expand.pixelmonbroadcasts.utilities;

// Remote imports.
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    // If we find a config that's broken during reloads, we set this flag and print an error.
    private static boolean gotConfigError;

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

    // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
    public static void checkConfigDir()
    {
        try
        {
            Files.createDirectory(Paths.get(PixelmonBroadcasts.primaryPath));
            printBasicMessage("--> §aPixelmon Broadcasts folder not found, making a new one for configs...");
        }
        catch (final IOException ignored) {}
    }

    // Called during initial setup, either when the server is booting up or when /pbroadcasts reload has been executed.
    public static void tryCreateConfig(final String callSource, final Path checkPath)
    {
        if (Files.notExists(checkPath))
        {
            try
            {
                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                printBasicMessage("    §eNo configuration files found, creating...");

                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/" + callSource + ".conf"),
                        Paths.get(PixelmonBroadcasts.primaryPath, callSource + ".conf"));
            }
            catch (final IOException F)
            {
                printBasicMessage("    §cFailed to set up config file \"§4" + callSource + "§c\"! Please report this.");
                printBasicMessage("    §cAdd any useful info you may have (operating system?). Stack trace:");
                F.printStackTrace();
            }
        }
        else
        {
            try
            {
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/" + callSource + ".conf"),
                        Paths.get(PixelmonBroadcasts.primaryPath, callSource + ".conf"));
            }
            catch (IOException F)
            {
                printBasicMessage("    §cCould not read configs! Please report this.");
                printBasicMessage("    §cAdd any useful info you may have (operating system?). Stack trace:");
                F.printStackTrace();
            }
        }
    }

    // Loads all configs at once. Fired during a full config reload, and during startup.
    public static void loadAllConfigs()
    {
        loadConfig("broadcasts");
        loadConfig("messages");
        loadConfig("settings");
    }

    // Read the main config file, and then commit settings to memory. Rough but functional is the theme here, folks.
    public static void loadConfig(final String callSource, final boolean... reloadingAll)
    {
        try
        {
            switch (callSource)
            {
                case "broadcasts":
                {
                    tryCreateConfig(callSource, broadcastPath);

                    break;

                    // Do broadcast-y stuff.
                }
                case "messages":
                {
                    tryCreateConfig(callSource, messagePath);

                    break;

                    // Do message-y stuff.
                }
                case "settings":
                {
                    tryCreateConfig(callSource, configPath);
                    final CommentedConfigurationNode settingsConfig = PixelmonBroadcasts.settingLoader.load();

                    // Start committing config stuff to memory. Start with the main variables.
                    //PixelmonBroadcasts.configVersion =
                    //        interpretInteger(settingsConfig.getNode("configVersion").getString());
                    PixelmonBroadcasts.commandAlias =
                            settingsConfig.getNode("commandAlias").getString();

                    // Show errors if any of these main variables are broken.
                    if (commandAlias == null)
                        printBasicMessage("    §cCould not read config node \"§4commandAlias§c\". Alias support disabled.");

                    // Set up some error arrays, so we can print errors to the console on boot/reload if stuff broke.
                    final List<String> optionsErrorArray = new ArrayList<>();

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

                        if (legendarySpawnOptions.contains("log"))
                            PixelmonBroadcasts.logLegendarySpawns = true;
                        if (legendarySpawnOptions.contains("show"))
                            PixelmonBroadcasts.showLegendarySpawns = true;
                        if (legendarySpawnOptions.contains("hover"))
                            PixelmonBroadcasts.hoverLegendarySpawns = true;
                    }

                    String legendaryChallengeOptions = settingsConfig.getNode("legendaryChallengeOptions").getString();
                    if (legendaryChallengeOptions == null)
                        optionsErrorArray.add("legendaryChallengeOptions");
                    else
                    {
                        legendaryChallengeOptions = legendaryChallengeOptions.toLowerCase();

                        if (legendaryChallengeOptions.contains("log"))
                            PixelmonBroadcasts.logLegendaryChallenges = true;
                        if (legendaryChallengeOptions.contains("show"))
                            PixelmonBroadcasts.showLegendaryChallenges = true;
                        if (legendaryChallengeOptions.contains("hover"))
                            PixelmonBroadcasts.hoverLegendaryChallenges = true;
                    }

                    String legendaryCatchOptions = settingsConfig.getNode("legendaryCatchOptions").getString();
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

                    String legendaryDefeatOptions = settingsConfig.getNode("legendaryDefeatOptions").getString();
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

                    String legendaryBlackoutOptions = settingsConfig.getNode("legendaryBlackoutOptions").getString();
                    if (legendaryBlackoutOptions == null)
                        optionsErrorArray.add("legendaryBlackoutOptions");
                    else
                    {
                        legendaryBlackoutOptions = legendaryBlackoutOptions.toLowerCase();

                        if (legendaryBlackoutOptions.contains("log"))
                            PixelmonBroadcasts.logLegendaryBlackouts = true;
                        if (legendaryBlackoutOptions.contains("show"))
                            PixelmonBroadcasts.showLegendaryBlackouts = true;
                        if (legendaryBlackoutOptions.contains("hover"))
                            PixelmonBroadcasts.hoverLegendaryBlackouts = true;
                    }

                    String legendaryForfeitOptions = settingsConfig.getNode("legendaryForfeitOptions").getString();
                    if (legendaryForfeitOptions == null)
                        optionsErrorArray.add("legendaryForfeitOptions");
                    else
                    {
                        legendaryForfeitOptions = legendaryForfeitOptions.toLowerCase();

                        if (legendaryForfeitOptions.contains("log"))
                            PixelmonBroadcasts.logLegendaryForfeits = true;
                        if (legendaryForfeitOptions.contains("show"))
                            PixelmonBroadcasts.showLegendaryForfeits = true;
                        if (legendaryForfeitOptions.contains("hover"))
                            PixelmonBroadcasts.hoverLegendaryForfeits = true;
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

                        if (shinyLegendarySpawnOptions.contains("log"))
                            PixelmonBroadcasts.logShinyLegendarySpawns = true;
                        if (shinyLegendarySpawnOptions.contains("show"))
                            PixelmonBroadcasts.showShinyLegendarySpawns = true;
                        if (shinyLegendarySpawnOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinyLegendarySpawns = true;
                    }

                    String shinyLegendaryChallengeOptions = settingsConfig.getNode("shinyLegendaryChallengeOptions").getString();
                    if (shinyLegendaryChallengeOptions == null)
                        optionsErrorArray.add("shinyLegendaryChallengeOptions");
                    else
                    {
                        shinyLegendaryChallengeOptions = shinyLegendaryChallengeOptions.toLowerCase();

                        if (shinyLegendaryChallengeOptions.contains("log"))
                            PixelmonBroadcasts.logShinyLegendaryChallenges = true;
                        if (shinyLegendaryChallengeOptions.contains("show"))
                            PixelmonBroadcasts.showShinyLegendaryChallenges = true;
                        if (shinyLegendaryChallengeOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinyLegendaryChallenges = true;
                    }

                    String shinyLegendaryCatchOptions = settingsConfig.getNode("shinyLegendaryCatchOptions").getString();
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

                    String shinyLegendaryDefeatOptions = settingsConfig.getNode("shinyLegendaryDefeatOptions").getString();
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

                    String shinyLegendaryBlackoutOptions = settingsConfig.getNode("shinyLegendaryBlackoutOptions").getString();
                    if (shinyLegendaryBlackoutOptions == null)
                        optionsErrorArray.add("shinyLegendaryBlackoutOptions");
                    else
                    {
                        shinyLegendaryBlackoutOptions = shinyLegendaryBlackoutOptions.toLowerCase();

                        if (shinyLegendaryBlackoutOptions.contains("log"))
                            PixelmonBroadcasts.logShinyLegendaryBlackouts = true;
                        if (shinyLegendaryBlackoutOptions.contains("show"))
                            PixelmonBroadcasts.showShinyLegendaryBlackouts = true;
                        if (shinyLegendaryBlackoutOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinyLegendaryBlackouts = true;
                    }

                    String shinyLegendaryForfeitOptions = settingsConfig.getNode("shinyLegendaryForfeitOptions").getString();
                    if (shinyLegendaryForfeitOptions == null)
                        optionsErrorArray.add("shinyLegendaryForfeitOptions");
                    else
                    {
                        shinyLegendaryForfeitOptions = shinyLegendaryForfeitOptions.toLowerCase();

                        if (shinyLegendaryForfeitOptions.contains("log"))
                            PixelmonBroadcasts.logShinyLegendaryForfeits = true;
                        if (shinyLegendaryForfeitOptions.contains("show"))
                            PixelmonBroadcasts.showShinyLegendaryForfeits = true;
                        if (shinyLegendaryForfeitOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinyLegendaryForfeits = true;
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

                        if (shinySpawnOptions.contains("log"))
                            PixelmonBroadcasts.logShinySpawns = true;
                        if (shinySpawnOptions.contains("show"))
                            PixelmonBroadcasts.showShinySpawns = true;
                        if (shinySpawnOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinySpawns = true;
                    }

                    String shinyChallengeOptions = settingsConfig.getNode("shinyChallengeOptions").getString();
                    if (shinyChallengeOptions == null)
                        optionsErrorArray.add("shinyChallengeOptions");
                    else
                    {
                        shinyChallengeOptions = shinyChallengeOptions.toLowerCase();

                        if (shinyChallengeOptions.contains("log"))
                            PixelmonBroadcasts.logShinyChallenges = true;
                        if (shinyChallengeOptions.contains("show"))
                            PixelmonBroadcasts.showShinyChallenges = true;
                        if (shinyChallengeOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinyChallenges = true;
                    }

                    String shinyCatchOptions = settingsConfig.getNode("shinyCatchOptions").getString();
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

                    String shinyDefeatOptions = settingsConfig.getNode("shinyDefeatOptions").getString();
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

                    String shinyBlackoutOptions = settingsConfig.getNode("shinyBlackoutOptions").getString();
                    if (shinyBlackoutOptions == null)
                        optionsErrorArray.add("shinyBlackoutOptions");
                    else
                    {
                        shinyBlackoutOptions = shinyBlackoutOptions.toLowerCase();

                        if (shinyBlackoutOptions.contains("log"))
                            PixelmonBroadcasts.logShinyBlackouts = true;
                        if (shinyBlackoutOptions.contains("show"))
                            PixelmonBroadcasts.showShinyBlackouts = true;
                        if (shinyBlackoutOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinyBlackouts = true;
                    }

                    String shinyForfeitOptions = settingsConfig.getNode("shinyForfeitOptions").getString();
                    if (shinyForfeitOptions == null)
                        optionsErrorArray.add("shinyForfeitOptions");
                    else
                    {
                        shinyForfeitOptions = shinyForfeitOptions.toLowerCase();

                        if (shinyForfeitOptions.contains("log"))
                            PixelmonBroadcasts.logShinyForfeits = true;
                        if (shinyForfeitOptions.contains("show"))
                            PixelmonBroadcasts.showShinyForfeits = true;
                        if (shinyForfeitOptions.contains("hover"))
                            PixelmonBroadcasts.hoverShinyForfeits = true;
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

                        if (bossSpawnOptions.contains("log"))
                            PixelmonBroadcasts.logBossSpawns = true;
                        if (bossSpawnOptions.contains("show"))
                            PixelmonBroadcasts.showBossSpawns = true;
                        if (bossSpawnOptions.contains("hover"))
                            PixelmonBroadcasts.hoverBossSpawns = true;
                    }

                    String bossChallengeOptions = settingsConfig.getNode("bossChallengeOptions").getString();
                    if (bossChallengeOptions == null)
                        optionsErrorArray.add("bossChallengeOptions");
                    else
                    {
                        bossChallengeOptions = bossChallengeOptions.toLowerCase();

                        if (bossChallengeOptions.contains("log"))
                            PixelmonBroadcasts.logBossChallenges = true;
                        if (bossChallengeOptions.contains("show"))
                            PixelmonBroadcasts.showBossChallenges = true;
                        if (bossChallengeOptions.contains("hover"))
                            PixelmonBroadcasts.hoverBossChallenges = true;
                    }

                    String bossDefeatOptions = settingsConfig.getNode("bossDefeatOptions").getString();
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

                    String bossBlackoutOptions = settingsConfig.getNode("bossBlackoutOptions").getString();
                    if (bossBlackoutOptions == null)
                        optionsErrorArray.add("bossBlackoutOptions");
                    else
                    {
                        bossBlackoutOptions = bossBlackoutOptions.toLowerCase();

                        if (bossBlackoutOptions.contains("log"))
                            PixelmonBroadcasts.logBossBlackouts = true;
                        if (bossBlackoutOptions.contains("show"))
                            PixelmonBroadcasts.showBossBlackouts = true;
                        if (bossBlackoutOptions.contains("hover"))
                            PixelmonBroadcasts.hoverBossBlackouts = true;
                    }

                    String bossForfeitOptions = settingsConfig.getNode("bossForfeitOptions").getString();
                    if (bossForfeitOptions == null)
                        optionsErrorArray.add("bossForfeitOptions");
                    else
                    {
                        bossForfeitOptions = bossForfeitOptions.toLowerCase();

                        if (bossForfeitOptions.contains("log"))
                            PixelmonBroadcasts.logBossForfeits = true;
                        if (bossForfeitOptions.contains("show"))
                            PixelmonBroadcasts.showBossForfeits = true;
                        if (bossForfeitOptions.contains("hover"))
                            PixelmonBroadcasts.hoverBossForfeits = true;
                    }

                    /*                 *\
                       trainer settings
                    \*                 */

                    // Get options. Extract and set them, if we managed to grab them successfully.
                    String bossTrainerChallengeOptions = settingsConfig.getNode("bossTrainerChallengeOptions").getString();
                    if (bossTrainerChallengeOptions == null)
                        optionsErrorArray.add("bossTrainerChallengeOptions");
                    else
                    {
                        bossTrainerChallengeOptions = bossTrainerChallengeOptions.toLowerCase();

                        if (bossTrainerChallengeOptions.contains("log"))
                            PixelmonBroadcasts.logBossTrainerChallenges = true;
                        if (bossTrainerChallengeOptions.contains("show"))
                            PixelmonBroadcasts.showBossTrainerChallenges = true;
                    }

                    String bossTrainerDefeatOptions = settingsConfig.getNode("bossTrainerDefeatOptions").getString();
                    if (bossTrainerDefeatOptions == null)
                        optionsErrorArray.add("bossTrainerDefeatOptions");
                    else
                    {
                        bossTrainerDefeatOptions = bossTrainerDefeatOptions.toLowerCase();

                        if (bossTrainerDefeatOptions.contains("log"))
                            PixelmonBroadcasts.logBossTrainerDefeats = true;
                        if (bossTrainerDefeatOptions.contains("show"))
                            PixelmonBroadcasts.showBossTrainerDefeats = true;
                    }

                    String bossTrainerBlackoutOptions = settingsConfig.getNode("bossTrainerBlackoutOptions").getString();
                    if (bossTrainerBlackoutOptions == null)
                        optionsErrorArray.add("bossTrainerBlackoutOptions");
                    else
                    {
                        bossTrainerBlackoutOptions = bossTrainerBlackoutOptions.toLowerCase();

                        if (bossTrainerBlackoutOptions.contains("log"))
                            PixelmonBroadcasts.logBossTrainerBlackouts = true;
                        if (bossTrainerBlackoutOptions.contains("show"))
                            PixelmonBroadcasts.showBossTrainerBlackouts = true;
                    }

                    String bossTrainerForfeitOptions = settingsConfig.getNode("bossTrainerForfeitOptions").getString();
                    if (bossTrainerForfeitOptions == null)
                        optionsErrorArray.add("bossTrainerForfeitOptions");
                    else
                    {
                        bossTrainerForfeitOptions = bossTrainerForfeitOptions.toLowerCase();

                        if (bossTrainerForfeitOptions.contains("log"))
                            PixelmonBroadcasts.logBossTrainerForfeits = true;
                        if (bossTrainerForfeitOptions.contains("show"))
                            PixelmonBroadcasts.showBossTrainerForfeits = true;
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

                        if (trainerChallengeOptions.contains("log"))
                            PixelmonBroadcasts.logTrainerChallenges = true;
                        if (trainerChallengeOptions.contains("show"))
                            PixelmonBroadcasts.showTrainerChallenges = true;
                    }

                    String trainerDefeatOptions = settingsConfig.getNode("trainerDefeatOptions").getString();
                    if (trainerDefeatOptions == null)
                        optionsErrorArray.add("trainerDefeatOptions");
                    else
                    {
                        trainerDefeatOptions = trainerDefeatOptions.toLowerCase();

                        if (trainerDefeatOptions.contains("log"))
                            PixelmonBroadcasts.logTrainerDefeats = true;
                        if (trainerDefeatOptions.contains("show"))
                            PixelmonBroadcasts.showTrainerDefeats = true;
                    }

                    String trainerBlackoutOptions = settingsConfig.getNode("trainerBlackoutOptions").getString();
                    if (trainerBlackoutOptions == null)
                        optionsErrorArray.add("trainerBlackoutOptions");
                    else
                    {
                        trainerBlackoutOptions = trainerBlackoutOptions.toLowerCase();

                        if (trainerBlackoutOptions.contains("log"))
                            PixelmonBroadcasts.logTrainerBlackouts = true;
                        if (trainerBlackoutOptions.contains("show"))
                            PixelmonBroadcasts.showTrainerBlackouts = true;
                    }

                    String trainerForfeitOptions = settingsConfig.getNode("trainerForfeitOptions").getString();
                    if (trainerForfeitOptions == null)
                        optionsErrorArray.add("trainerForfeitOptions");
                    else
                    {
                        trainerForfeitOptions = trainerForfeitOptions.toLowerCase();

                        if (trainerForfeitOptions.contains("log"))
                            PixelmonBroadcasts.logTrainerForfeits = true;
                        if (trainerForfeitOptions.contains("show"))
                            PixelmonBroadcasts.showTrainerForfeits = true;
                    }

                    /*              *\
                       PvP settings
                    \*              */

                    // Get options. Extract and set them, if we managed to grab them successfully.
                    String pvpStartOptions = settingsConfig.getNode("pvpStartOptions").getString();
                    if (pvpStartOptions == null)
                        optionsErrorArray.add("pvpStartOptions");
                    else
                    {
                        pvpStartOptions = pvpStartOptions.toLowerCase();

                        if (pvpStartOptions.contains("log"))
                            PixelmonBroadcasts.logPVPStarts = true;
                        if (pvpStartOptions.contains("show"))
                            PixelmonBroadcasts.showPVPStarts = true;
                    }

                    String pvpDefeatOptions = settingsConfig.getNode("pvpDefeatOptions").getString();
                    if (pvpDefeatOptions == null)
                        optionsErrorArray.add("pvpDefeatOptions");
                    else
                    {
                        pvpDefeatOptions = pvpDefeatOptions.toLowerCase();

                        if (pvpDefeatOptions.contains("log"))
                            PixelmonBroadcasts.logPVPDefeats = true;
                        if (pvpDefeatOptions.contains("show"))
                            PixelmonBroadcasts.showPVPDefeats = true;
                    }

                    String pvpDrawOptions = settingsConfig.getNode("pvpDrawOptions").getString();
                    if (pvpDrawOptions == null)
                        optionsErrorArray.add("pvpDrawOptions");
                    else
                    {
                        pvpDrawOptions = pvpDrawOptions.toLowerCase();

                        if (pvpDrawOptions.contains("log"))
                            PixelmonBroadcasts.logPVPDraws = true;
                        if (pvpDrawOptions.contains("show"))
                            PixelmonBroadcasts.showPVPDraws = true;
                    }

                    /*                *\
                       hatch settings
                    \*                */

                    // Get options. Extract and set them, if we managed to grab them successfully.
                    String hatchOptions = settingsConfig.getNode("hatchOptions").getString();
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

                    String shinyHatchOptions = settingsConfig.getNode("shinyHatchOptions").getString();
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
                    String tradeOptions = settingsConfig.getNode("tradeOptions").getString();
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

                    // Print errors if something broke.
                    if (!optionsErrorArray.isEmpty())
                        printOptionsNodeError(optionsErrorArray);

                    break;
                }
            }
        }
        catch (final Exception F)
        {
            // Spaces added so it falls in line with startup/reload message spacing.
            printBasicMessage("    §cCould not read config file \"§4" + callSource + "§c\".");

            // Did we get the optional "hey we're reloading everything" argument, and is it true? Print.
            if (reloadingAll.length != 0 && reloadingAll[0])
                printBasicMessage("    §cPlease check your config for any missing or invalid entries.");

            gotConfigError = true;
        }
    }
}
