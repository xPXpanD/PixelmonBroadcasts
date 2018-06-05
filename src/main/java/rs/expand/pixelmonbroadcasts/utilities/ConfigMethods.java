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

        String legendaryChallengeOptions = commandConfig.getNode("legendaryChallengeOptions").getString();
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

        String legendaryLoseToOptions = commandConfig.getNode("legendaryLoseToOptions").getString();
        if (legendaryLoseToOptions == null)
            optionsErrorArray.add("legendaryLoseToOptions");
        else
        {
            legendaryLoseToOptions = legendaryLoseToOptions.toLowerCase();

            if (legendaryLoseToOptions.contains("log"))
                PixelmonBroadcasts.logLegendaryLosses = true;
            if (legendaryLoseToOptions.contains("show"))
                PixelmonBroadcasts.showLegendaryLosses = true;
            if (legendaryLoseToOptions.contains("hover"))
                PixelmonBroadcasts.hoverLegendaryLosses = true;
        }

        String legendaryForfeitOptions = commandConfig.getNode("legendaryForfeitOptions").getString();
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

        String shinyLegendaryChallengeOptions = commandConfig.getNode("shinyLegendaryChallengeOptions").getString();
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

        String shinyLegendaryLoseToOptions = commandConfig.getNode("shinyLegendaryLoseToOptions").getString();
        if (shinyLegendaryLoseToOptions == null)
            optionsErrorArray.add("shinyLegendaryLoseToOptions");
        else
        {
            shinyLegendaryLoseToOptions = shinyLegendaryLoseToOptions.toLowerCase();

            if (shinyLegendaryLoseToOptions.contains("log"))
                PixelmonBroadcasts.logShinyLegendaryLosses = true;
            if (shinyLegendaryLoseToOptions.contains("show"))
                PixelmonBroadcasts.showShinyLegendaryLosses = true;
            if (shinyLegendaryLoseToOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyLegendaryLosses = true;
        }

        String shinyLegendaryForfeitOptions = commandConfig.getNode("shinyLegendaryForfeitOptions").getString();
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

        String shinyChallengeOptions = commandConfig.getNode("shinyChallengeOptions").getString();
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

        String shinyLoseToOptions = commandConfig.getNode("shinyLoseToOptions").getString();
        if (shinyLoseToOptions == null)
            optionsErrorArray.add("shinyLoseToOptions");
        else
        {
            shinyLoseToOptions = shinyLoseToOptions.toLowerCase();

            if (shinyLoseToOptions.contains("log"))
                PixelmonBroadcasts.logShinyLosses = true;
            if (shinyLoseToOptions.contains("show"))
                PixelmonBroadcasts.showShinyLosses = true;
            if (shinyLoseToOptions.contains("hover"))
                PixelmonBroadcasts.hoverShinyLosses = true;
        }

        String shinyForfeitOptions = commandConfig.getNode("shinyForfeitOptions").getString();
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

        String bossChallengeOptions = commandConfig.getNode("bossChallengeOptions").getString();
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

        String bossLoseToOptions = commandConfig.getNode("bossLoseToOptions").getString();
        if (bossLoseToOptions == null)
            optionsErrorArray.add("bossLoseToOptions");
        else
        {
            bossLoseToOptions = bossLoseToOptions.toLowerCase();

            if (bossLoseToOptions.contains("log"))
                PixelmonBroadcasts.logBossLosses = true;
            if (bossLoseToOptions.contains("show"))
                PixelmonBroadcasts.showBossLosses = true;
            if (bossLoseToOptions.contains("hover"))
                PixelmonBroadcasts.hoverBossLosses = true;
        }

        String bossForfeitOptions = commandConfig.getNode("bossForfeitOptions").getString();
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
        String bossTrainerChallengeOptions = commandConfig.getNode("bossTrainerChallengeOptions").getString();
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

        String bossTrainerDefeatOptions = commandConfig.getNode("bossTrainerDefeatOptions").getString();
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

        String bossTrainerLoseToOptions = commandConfig.getNode("bossTrainerLoseToOptions").getString();
        if (bossTrainerLoseToOptions == null)
            optionsErrorArray.add("bossTrainerLoseToOptions");
        else
        {
            bossTrainerLoseToOptions = bossTrainerLoseToOptions.toLowerCase();

            if (bossTrainerLoseToOptions.contains("log"))
                PixelmonBroadcasts.logBossTrainerLosses = true;
            if (bossTrainerLoseToOptions.contains("show"))
                PixelmonBroadcasts.showBossTrainerLosses = true;
        }

        String bossTrainerForfeitOptions = commandConfig.getNode("bossTrainerForfeitOptions").getString();
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
        String trainerChallengeOptions = commandConfig.getNode("trainerChallengeOptions").getString();
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

        String trainerDefeatOptions = commandConfig.getNode("trainerDefeatOptions").getString();
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

        String trainerLoseToOptions = commandConfig.getNode("trainerLoseToOptions").getString();
        if (trainerLoseToOptions == null)
            optionsErrorArray.add("trainerLoseToOptions");
        else
        {
            trainerLoseToOptions = trainerLoseToOptions.toLowerCase();

            if (trainerLoseToOptions.contains("log"))
                PixelmonBroadcasts.logTrainerLosses = true;
            if (trainerLoseToOptions.contains("show"))
                PixelmonBroadcasts.showTrainerLosses = true;
        }

        String trainerForfeitOptions = commandConfig.getNode("trainerForfeitOptions").getString();
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
        String pvpStartOptions = commandConfig.getNode("pvpStartOptions").getString();
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

        String pvpDefeatOptions = commandConfig.getNode("pvpDefeatOptions").getString();
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

        String pvpDrawOptions = commandConfig.getNode("pvpDrawOptions").getString();
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
        PixelmonBroadcasts.legendaryChallengeMessage =
                parseRemoteString(commandConfig.getNode("legendaryChallengeMessage").getString());
        PixelmonBroadcasts.legendaryCatchMessage =
                parseRemoteString(commandConfig.getNode("legendaryCatchMessage").getString());
        PixelmonBroadcasts.legendaryDefeatMessage =
                parseRemoteString(commandConfig.getNode("legendaryDefeatMessage").getString());
        PixelmonBroadcasts.legendaryLoseToMessage =
                parseRemoteString(commandConfig.getNode("legendaryLoseToMessage").getString());
        PixelmonBroadcasts.legendaryForfeitMessage =
                parseRemoteString(commandConfig.getNode("legendaryForfeitMessage").getString());
        PixelmonBroadcasts.shinyLegendarySpawnMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendarySpawnMessage").getString());
        PixelmonBroadcasts.shinyLegendaryChallengeMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryChallengeMessage").getString());
        PixelmonBroadcasts.shinyLegendaryCatchMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryCatchMessage").getString());
        PixelmonBroadcasts.shinyLegendaryDefeatMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryDefeatMessage").getString());
        PixelmonBroadcasts.shinyLegendaryLoseToMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryLoseToMessage").getString());
        PixelmonBroadcasts.shinyLegendaryForfeitMessage =
                parseRemoteString(commandConfig.getNode("shinyLegendaryForfeitMessage").getString());
        PixelmonBroadcasts.shinySpawnMessage =
                parseRemoteString(commandConfig.getNode("shinySpawnMessage").getString());
        PixelmonBroadcasts.shinyChallengeMessage =
                parseRemoteString(commandConfig.getNode("shinyChallengeMessage").getString());
        PixelmonBroadcasts.shinyCatchMessage =
                parseRemoteString(commandConfig.getNode("shinyCatchMessage").getString());
        PixelmonBroadcasts.shinyDefeatMessage =
                parseRemoteString(commandConfig.getNode("shinyDefeatMessage").getString());
        PixelmonBroadcasts.shinyLoseToMessage =
                parseRemoteString(commandConfig.getNode("shinyLoseToMessage").getString());
        PixelmonBroadcasts.shinyForfeitMessage =
                parseRemoteString(commandConfig.getNode("shinyForfeitMessage").getString());
        PixelmonBroadcasts.bossSpawnMessage =
                parseRemoteString(commandConfig.getNode("bossSpawnMessage").getString());
        PixelmonBroadcasts.bossChallengeMessage =
                parseRemoteString(commandConfig.getNode("bossChallengeMessage").getString());
        PixelmonBroadcasts.bossDefeatMessage =
                parseRemoteString(commandConfig.getNode("bossDefeatMessage").getString());
        PixelmonBroadcasts.bossLoseToMessage =
                parseRemoteString(commandConfig.getNode("bossLoseToMessage").getString());
        PixelmonBroadcasts.bossForfeitMessage =
                parseRemoteString(commandConfig.getNode("bossForfeitMessage").getString());
        PixelmonBroadcasts.bossTrainerChallengeMessage =
                parseRemoteString(commandConfig.getNode("bossTrainerChallengeMessage").getString());
        PixelmonBroadcasts.bossTrainerDefeatMessage =
                parseRemoteString(commandConfig.getNode("bossTrainerDefeatMessage").getString());
        PixelmonBroadcasts.bossTrainerLoseToMessage =
                parseRemoteString(commandConfig.getNode("bossTrainerLoseToMessage").getString());
        PixelmonBroadcasts.bossTrainerForfeitMessage =
                parseRemoteString(commandConfig.getNode("bossTrainerForfeitMessage").getString());
        PixelmonBroadcasts.trainerChallengeMessage =
                parseRemoteString(commandConfig.getNode("trainerChallengeMessage").getString());
        PixelmonBroadcasts.trainerDefeatMessage =
                parseRemoteString(commandConfig.getNode("trainerDefeatMessage").getString());
        PixelmonBroadcasts.trainerLoseToMessage =
                parseRemoteString(commandConfig.getNode("trainerLoseToMessage").getString());
        PixelmonBroadcasts.trainerForfeitMessage =
                parseRemoteString(commandConfig.getNode("trainerForfeitMessage").getString());
        PixelmonBroadcasts.pvpStartMessage =
                parseRemoteString(commandConfig.getNode("pvpStartMessage").getString());
        PixelmonBroadcasts.pvpDefeatMessage =
                parseRemoteString(commandConfig.getNode("pvpDefeatMessage").getString());
        PixelmonBroadcasts.pvpDrawMessage =
                parseRemoteString(commandConfig.getNode("pvpDrawMessage").getString());
        PixelmonBroadcasts.hatchMessage =
                parseRemoteString(commandConfig.getNode("hatchMessage").getString());
        PixelmonBroadcasts.shinyHatchMessage =
                parseRemoteString(commandConfig.getNode("shinyHatchMessage").getString());
        PixelmonBroadcasts.tradeMessage =
                parseRemoteString(commandConfig.getNode("tradeMessage").getString());

        // Validate our loaded messages.
        if (legendarySpawnMessage == null)
            messageErrorArray.add("legendarySpawnMessage");
        if (legendaryChallengeMessage == null)
            messageErrorArray.add("legendaryChallengeMessage");
        if (legendaryCatchMessage == null)
            messageErrorArray.add("legendaryCatchMessage");
        if (legendaryDefeatMessage == null)
            messageErrorArray.add("legendaryDefeatMessage");
        if (legendaryLoseToMessage == null)
            messageErrorArray.add("legendaryLoseToMessage");
        if (legendaryForfeitMessage == null)
            messageErrorArray.add("legendaryForfeitMessage");
        if (shinyLegendarySpawnMessage == null)
            messageErrorArray.add("shinyLegendarySpawnMessage");
        if (shinyLegendaryChallengeMessage == null)
            messageErrorArray.add("shinyLegendaryChallengeMessage");
        if (shinyLegendaryCatchMessage == null)
            messageErrorArray.add("shinyLegendaryCatchMessage");
        if (shinyLegendaryDefeatMessage == null)
            messageErrorArray.add("shinyLegendaryDefeatMessage");
        if (shinyLegendaryLoseToMessage == null)
            messageErrorArray.add("shinyLegendaryLoseToMessage");
        if (shinyLegendaryForfeitMessage == null)
            messageErrorArray.add("shinyLegendaryForfeitMessage");
        if (shinySpawnMessage == null)
            messageErrorArray.add("shinySpawnMessage");
        if (shinyChallengeMessage == null)
            messageErrorArray.add("shinyChallengeMessage");
        if (shinyCatchMessage == null)
            messageErrorArray.add("shinyCatchMessage");
        if (shinyDefeatMessage == null)
            messageErrorArray.add("shinyDefeatMessage");
        if (shinyLoseToMessage == null)
            messageErrorArray.add("shinyLoseToMessage");
        if (shinyForfeitMessage == null)
            messageErrorArray.add("shinyForfeitMessage");
        if (bossSpawnMessage == null)
            messageErrorArray.add("bossSpawnMessage");
        if (bossChallengeMessage == null)
            messageErrorArray.add("bossChallengeMessage");
        if (bossDefeatMessage == null)
            messageErrorArray.add("bossDefeatMessage");
        if (bossLoseToMessage == null)
            messageErrorArray.add("bossLoseToMessage");
        if (bossForfeitMessage == null)
            messageErrorArray.add("bossForfeitMessage");
        if (bossTrainerChallengeMessage == null)
            messageErrorArray.add("bossTrainerChallengeMessage");
        if (bossTrainerDefeatMessage == null)
            messageErrorArray.add("bossTrainerDefeatMessage");
        if (bossTrainerLoseToMessage == null)
            messageErrorArray.add("bossTrainerLoseToMessage");
        if (bossTrainerForfeitMessage == null)
            messageErrorArray.add("bossTrainerForfeitMessage");
        if (trainerChallengeMessage == null)
            messageErrorArray.add("trainerChallengeMessage");
        if (trainerDefeatMessage == null)
            messageErrorArray.add("trainerDefeatMessage");
        if (trainerLoseToMessage == null)
            messageErrorArray.add("trainerLoseToMessage");
        if (trainerForfeitMessage == null)
            messageErrorArray.add("trainerForfeitMessage");
        if (pvpStartMessage == null)
            messageErrorArray.add("pvpStartMessage");
        if (pvpDefeatMessage == null)
            messageErrorArray.add("pvpDefeatMessage");
        if (pvpDrawMessage == null)
            messageErrorArray.add("pvpDrawMessage");
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
