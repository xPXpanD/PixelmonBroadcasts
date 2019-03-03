// The probably-too-complicated config handler, mark whatever. A bit less messy, but still needs work.
package rs.expand.pixelmonbroadcasts.utilities;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.plugin.PluginContainer;
import rs.expand.pixelmonbroadcasts.PixelmonBroadcasts;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printOptionsNodeError;

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
    public static boolean tryRegisterCommands()
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
                logger.info("    §aRegistered main command as §2/pixelmonbroadcasts§a, alias §2/" + commandAlias);
            else
                logger.info("    §aRegistered main command as §2/pixelmonbroadcasts§a, no alias.");

            return true;
        }
        else
        {
            logger.info("    §cCommand (re-)initialization failed. Please report, this is a bug.");
            logger.info("    §cSidemod commands are likely dead. A reboot may work.");

            return false;
        }
    }

    // Called during initial setup, either when the server is booting up or when /pbroadcasts reload has been executed.
    public static boolean tryCreateAndLoadConfigs()
    {
        // Print a message to squeeze between the messages of whatever called the (re-)load.
        logger.info("--> §aLoading and validating Pixelmon Broadcasts settings...");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        try
        {
            Files.createDirectory(Paths.get(configPathAsString));
            logger.info("--> §aPixelmon Broadcasts folder not found, making a new one for configs...");
        }
        catch (final IOException ignored)
        {}

        // Let's try creating/loading all the configs. Break out with a return if something goes wrong.
        try
        {
            if (Files.notExists(broadcastsPath))
            {
                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                logger.info("    §eNo broadcasts file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/broadcasts.conf"),
                        Paths.get(configPathAsString, "broadcasts.conf"));
            }

            if (Files.notExists(messagesPath))
            {
                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                logger.info("    §eNo messages file found, creating...");
                Files.copy(ConfigMethods.class.getResourceAsStream("/assets/messages.conf"),
                        Paths.get(configPathAsString, "messages.conf"));
            }

            if (Files.notExists(settingsPath))
            {

                // Create a new config since the file wasn't found. Add spaces to match startup/reload message spacing.
                logger.info("    §eNo settings file found, creating...");
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
            logger.info("    §cOne or more configs could not be set up. Please report this.");
            logger.info("    §cAdd any useful info you may have (operating system?). Stack trace:");
            F.printStackTrace();

            // Exit out of the method early, we can't continue.
            return false;
        }

        // If we haven't returned out of the method yet, no errors were found! Load settings file stuff into memory.

        /*               *\
           initial setup
        \*               */

        // Load in and validate the config version, command alias and ability setting.
        PixelmonBroadcasts.configVersion =
                interpretInteger(settingsConfig.getNode("configVersion").getString());
        PixelmonBroadcasts.commandAlias =
                settingsConfig.getNode("commandAlias").getString();
        PixelmonBroadcasts.showAbilities =
                toBooleanObject(settingsConfig.getNode("showAbilities").getString());

        // Show errors if any of these main variables are broken.
        if (configVersion == null)
            logger.info("    §cCould not read config node \"§4configVersion§c\".");
        if (commandAlias == null)
            logger.info("    §cCould not read config node \"§4commandAlias§c\". Alias support disabled.");
        if (showAbilities == null)
        {
            logger.info("    §cCould not read config node \"§4showAbilities§c\". Falling back, enabling.");
            showAbilities = true;
        }

        // Write the options in settings.conf to our event data enum for later use.
        /// Blackouts.
        EventData.Blackouts.NORMAL.options = settingsConfig.getNode("normalBlackoutOptions").getString();
        EventData.Blackouts.SHINY.options = settingsConfig.getNode("shinyBlackoutOptions").getString();
        EventData.Blackouts.LEGENDARY.options = settingsConfig.getNode("legendaryBlackoutOptions").getString();
        EventData.Blackouts.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastBlackoutOptions").getString();
        EventData.Blackouts.BOSS.options = settingsConfig.getNode("bossBlackoutOptions").getString();
        EventData.Blackouts.TRAINER.options = settingsConfig.getNode("trainerBlackoutOptions").getString();
        EventData.Blackouts.BOSS_TRAINER.options = settingsConfig.getNode("bossTrainerBlackoutOptions").getString();

        /// Catches.
        EventData.Catches.NORMAL.options = settingsConfig.getNode("normalCatchOptions").getString();
        EventData.Catches.SHINY.options = settingsConfig.getNode("shinyCatchOptions").getString();
        EventData.Catches.LEGENDARY.options = settingsConfig.getNode("legendaryCatchOptions").getString();
        EventData.Catches.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastCatchOptions").getString();

        /// Challenges.
        EventData.Challenges.SHINY.options = settingsConfig.getNode("shinyChallengeOptions").getString();
        EventData.Challenges.LEGENDARY.options = settingsConfig.getNode("legendaryChallengeOptions").getString();
        EventData.Challenges.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastChallengeOptions").getString();
        EventData.Challenges.BOSS.options = settingsConfig.getNode("bossChallengeOptions").getString();
        EventData.Challenges.TRAINER.options = settingsConfig.getNode("trainerChallengeOptions").getString();
        EventData.Challenges.BOSS_TRAINER.options = settingsConfig.getNode("bossTrainerChallengeOptions").getString();
        EventData.Challenges.PVP.options = settingsConfig.getNode("pvpChallengeOptions").getString();

        /// Forfeits.
        EventData.Forfeits.SHINY.options = settingsConfig.getNode("shinyForfeitOptions").getString();
        EventData.Forfeits.LEGENDARY.options = settingsConfig.getNode("legendaryForfeitOptions").getString();
        EventData.Forfeits.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastForfeitOptions").getString();
        EventData.Forfeits.BOSS.options = settingsConfig.getNode("bossForfeitOptions").getString();
        EventData.Forfeits.TRAINER.options = settingsConfig.getNode("trainerForfeitOptions").getString();
        EventData.Forfeits.BOSS_TRAINER.options = settingsConfig.getNode("bossTrainerForfeitOptions").getString();

        /// Spawns.
        EventData.Spawns.SHINY.options = settingsConfig.getNode("shinySpawnOptions").getString();
        EventData.Spawns.LEGENDARY.options = settingsConfig.getNode("legendarySpawnOptions").getString();
        EventData.Spawns.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastSpawnOptions").getString();
        EventData.Spawns.WORMHOLE.options = settingsConfig.getNode("wormholeSpawnOptions").getString();
        EventData.Spawns.BOSS.options = settingsConfig.getNode("bossSpawnOptions").getString();

        /// Victories.
        EventData.Victories.SHINY.options = settingsConfig.getNode("shinyVictoryOptions").getString();
        EventData.Victories.LEGENDARY.options = settingsConfig.getNode("legendaryVictoryOptions").getString();
        EventData.Victories.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastVictoryOptions").getString();
        EventData.Victories.BOSS.options = settingsConfig.getNode("bossVictoryOptions").getString();
        EventData.Victories.TRAINER.options = settingsConfig.getNode("trainerVictoryOptions").getString();
        EventData.Victories.BOSS_TRAINER.options = settingsConfig.getNode("bossTrainerVictoryOptions").getString();
        EventData.Victories.PVP.options = settingsConfig.getNode("pvpVictoryOptions").getString();

        /// Hatches.
        EventData.Hatches.NORMAL.options = settingsConfig.getNode("normalHatchOptions").getString();
        EventData.Hatches.SHINY.options = settingsConfig.getNode("shinyHatchOptions").getString();
        EventData.Hatches.LEGENDARY.options = settingsConfig.getNode("legendaryHatchOptions").getString();
        EventData.Hatches.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastHatchOptions").getString();

        // Draws.
        EventData.Draws.PVP.options = settingsConfig.getNode("pvpDrawOptions").getString();

        /// Other options. TODO: Implement fainting.
        EventData.Others.TRADE.options = settingsConfig.getNode("tradeOptions").getString();
        EventData.Others.FAINT.options = settingsConfig.getNode("faintOptions").getString();

        // For these hybrid events, combine options for both of them. Nulls or bad joins won't matter. A bit hacky.
        // Blackouts.
        EventData.Blackouts.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinyBlackouts§Options").getString() + settingsConfig.getNode("legendaryBlackouts§Options").getString();
        EventData.Blackouts.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinyBlackouts§Options").getString() + settingsConfig.getNode("ultraBeastBlackouts§Options").getString();

        // Catches.
        EventData.Catches.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinyCatchOptions").getString() + settingsConfig.getNode("legendaryCatchOptions").getString();
        EventData.Catches.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinyCatchOptions").getString() + settingsConfig.getNode("ultraBeastCatchOptions").getString();

        // Challenges.
        EventData.Challenges.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinyChallengeOptions").getString() + settingsConfig.getNode("legendaryChallengeOptions").getString();
        EventData.Challenges.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinyChallengeOptions").getString() + settingsConfig.getNode("ultraBeastChallengeOptions").getString();

        // Forfeits.
        EventData.Forfeits.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinyForfeitOptions").getString() + settingsConfig.getNode("legendaryForfeitOptions").getString();
        EventData.Forfeits.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinyForfeitOptions").getString() + settingsConfig.getNode("ultraBeastForfeitOptions").getString();

        // Spawns.
        EventData.Spawns.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinySpawnOptions").getString() + settingsConfig.getNode("legendarySpawnOptions").getString();
        EventData.Spawns.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinySpawnOptions").getString() + settingsConfig.getNode("ultraBeastSpawnOptions").getString();

        // Victories.
        EventData.Victories.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinyVictoryOptions").getString() + settingsConfig.getNode("legendaryVictoryOptions").getString();
        EventData.Victories.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinyVictoryOptions").getString() + settingsConfig.getNode("ultraBeastVictoryOptions").getString();

        // Hatches.
        EventData.Hatches.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinyHatchOptions").getString() + settingsConfig.getNode("legendaryHatchOptions").getString();
        EventData.Hatches.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinyHatchOptions").getString() + settingsConfig.getNode("ultraBeastHatchOptions").getString();        
        
        /// Do validation for logging purposes.
        // To start, set up an error array. Check later, and print errors to the console if stuff broke.
        final List<String> optionsErrorArray = new ArrayList<>();

        // TODO: ACTUALLY IMPLEMENT THESE PROPERLY!!
        
        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");
        if (EventData.Blackouts.BOSS.options == null)
            optionsErrorArray.add("bossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");

        // Catches.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");

        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");
        if (EventData.Blackouts.BOSS.options == null)
            optionsErrorArray.add("bossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");

        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");
        if (EventData.Blackouts.BOSS.options == null)
            optionsErrorArray.add("bossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");

        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");
        if (EventData.Blackouts.BOSS.options == null)
            optionsErrorArray.add("bossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");

        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");
        if (EventData.Blackouts.BOSS.options == null)
            optionsErrorArray.add("bossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");

        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");
        if (EventData.Blackouts.BOSS.options == null)
            optionsErrorArray.add("bossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");

        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorArray.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorArray.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorArray.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.SHINY_LEGENDARY.options == null)
            optionsErrorArray.add("shinyLegendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorArray.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options == null)
            optionsErrorArray.add("shinyUltraBeastBlackoutOptions");
        if (EventData.Blackouts.BOSS.options == null)
            optionsErrorArray.add("bossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorArray.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorArray.add("bossTrainerBlackoutOptions");

        // Print errors if something broke.
        if (!optionsErrorArray.isEmpty())
            printOptionsNodeError(optionsErrorArray);

        // We're done, phew. Tell the calling code.
        return true;
    }
}
