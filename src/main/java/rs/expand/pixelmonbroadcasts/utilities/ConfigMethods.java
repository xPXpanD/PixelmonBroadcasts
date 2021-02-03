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
            logger.info("    §cCommand (re-)initialization failed; we cannot find the Sponge plugin container.");
            logger.info("    §cSidemod commands are likely dead. A reboot may work. This may be a bug, please report.");

            return false;
        }
    }

    // Called during initial setup, either when the server is booting up or when /pbroadcasts reload has been executed.
    public static boolean tryCreateAndLoadConfigs()
    {
        // Print a message to squeeze between the messages of whatever called the (re-)load.
        logger.info("§f--> §aLoading and validating settings for Pixelmon Broadcasts 0.5...");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        try
        {
            Files.createDirectory(Paths.get(configPathAsString));
            logger.info("§f--> §aPixelmon Broadcasts folder not found, making a new one for configs...");
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
            logger.info("    §cOne or more configs could not be set up. Stack trace:");
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
        EventData.Blackouts.UNCOMMON_BOSS.options = settingsConfig.getNode("uncommonBossBlackoutOptions").getString();
        EventData.Blackouts.RARE_BOSS.options = settingsConfig.getNode("rareBossBlackoutOptions").getString();
        EventData.Blackouts.LEGENDARY_BOSS.options = settingsConfig.getNode("legendaryBossBlackoutOptions").getString();
        EventData.Blackouts.ULTIMATE_BOSS.options = settingsConfig.getNode("ultimateBossBlackoutOptions").getString();
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
        EventData.Challenges.UNCOMMON_BOSS.options = settingsConfig.getNode("uncommonBossChallengeOptions").getString();
        EventData.Challenges.RARE_BOSS.options = settingsConfig.getNode("rareBossChallengeOptions").getString();
        EventData.Challenges.LEGENDARY_BOSS.options = settingsConfig.getNode("legendaryBossChallengeOptions").getString();
        EventData.Challenges.ULTIMATE_BOSS.options = settingsConfig.getNode("ultimateBossChallengeOptions").getString();
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
        EventData.Spawns.UNCOMMON_BOSS.options = settingsConfig.getNode("uncommonBossSpawnOptions").getString();
        EventData.Spawns.RARE_BOSS.options = settingsConfig.getNode("rareBossSpawnOptions").getString();
        EventData.Spawns.LEGENDARY_BOSS.options = settingsConfig.getNode("legendaryBossSpawnOptions").getString();
        EventData.Spawns.ULTIMATE_BOSS.options = settingsConfig.getNode("ultimateBossSpawnOptions").getString();

        /// Victories.
        EventData.Victories.SHINY.options = settingsConfig.getNode("shinyVictoryOptions").getString();
        EventData.Victories.LEGENDARY.options = settingsConfig.getNode("legendaryVictoryOptions").getString();
        EventData.Victories.ULTRA_BEAST.options = settingsConfig.getNode("ultraBeastVictoryOptions").getString();
        EventData.Victories.UNCOMMON_BOSS.options = settingsConfig.getNode("uncommonBossVictoryOptions").getString();
        EventData.Victories.RARE_BOSS.options = settingsConfig.getNode("rareBossVictoryOptions").getString();
        EventData.Victories.LEGENDARY_BOSS.options = settingsConfig.getNode("legendaryBossVictoryOptions").getString();
        EventData.Victories.ULTIMATE_BOSS.options = settingsConfig.getNode("ultimateBossVictoryOptions").getString();
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

        /// Other options.
        EventData.Others.EVOLVE.options = settingsConfig.getNode("evolveOptions").getString();
        EventData.Others.FAINT.options = settingsConfig.getNode("faintOptions").getString();
        EventData.Others.TRADE.options = settingsConfig.getNode("tradeOptions").getString();

        // For these hybrid events, combine options for both of them. Nulls or bad joins won't matter. A bit hacky.
        // Blackouts.
        EventData.Blackouts.SHINY_LEGENDARY.options =
                settingsConfig.getNode("shinyBlackoutOptions").getString() + settingsConfig.getNode("legendaryBlackoutOptions").getString();
        EventData.Blackouts.SHINY_ULTRA_BEAST.options =
                settingsConfig.getNode("shinyBlackoutOptions").getString() + settingsConfig.getNode("ultraBeastBlackoutOptions").getString();

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
        final List<String> optionsErrorList = new ArrayList<>();
        
        // Blackouts.
        if (EventData.Blackouts.NORMAL.options == null)
            optionsErrorList.add("normalBlackoutOptions");
        if (EventData.Blackouts.SHINY.options == null)
            optionsErrorList.add("shinyBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY.options == null)
            optionsErrorList.add("legendaryBlackoutOptions");
        if (EventData.Blackouts.ULTRA_BEAST.options == null)
            optionsErrorList.add("ultraBeastBlackoutOptions");
        if (EventData.Blackouts.UNCOMMON_BOSS.options == null)
            optionsErrorList.add("uncommonBossBlackoutOptions");
        if (EventData.Blackouts.RARE_BOSS.options == null)
            optionsErrorList.add("rareBossBlackoutOptions");
        if (EventData.Blackouts.LEGENDARY_BOSS.options == null)
            optionsErrorList.add("legendaryBossBlackoutOptions");
        if (EventData.Blackouts.ULTIMATE_BOSS.options == null)
            optionsErrorList.add("ultimateBossBlackoutOptions");
        if (EventData.Blackouts.TRAINER.options == null)
            optionsErrorList.add("trainerBlackoutOptions");
        if (EventData.Blackouts.BOSS_TRAINER.options == null)
            optionsErrorList.add("bossTrainerBlackoutOptions");

        // Catches.
        if (EventData.Catches.NORMAL.options == null)
            optionsErrorList.add("normalCatchOptions");
        if (EventData.Catches.SHINY.options == null)
            optionsErrorList.add("shinyCatchOptions");
        if (EventData.Catches.LEGENDARY.options == null)
            optionsErrorList.add("legendaryCatchOptions");
        if (EventData.Catches.ULTRA_BEAST.options == null)
            optionsErrorList.add("ultraBeastCatchOptions");

        // Challenges.
        if (EventData.Challenges.SHINY.options == null)
            optionsErrorList.add("shinyChallengeOptions");
        if (EventData.Challenges.LEGENDARY.options == null)
            optionsErrorList.add("legendaryChallengeOptions");
        if (EventData.Challenges.ULTRA_BEAST.options == null)
            optionsErrorList.add("ultraBeastChallengeOptions");
        if (EventData.Challenges.UNCOMMON_BOSS.options == null)
            optionsErrorList.add("uncommonBossChallengeOptions");
        if (EventData.Challenges.RARE_BOSS.options == null)
            optionsErrorList.add("rareBossChallengeOptions");
        if (EventData.Challenges.LEGENDARY_BOSS.options == null)
            optionsErrorList.add("legendaryBossChallengeOptions");
        if (EventData.Challenges.ULTIMATE_BOSS.options == null)
            optionsErrorList.add("ultimateBossChallengeOptions");
        if (EventData.Challenges.TRAINER.options == null)
            optionsErrorList.add("trainerChallengeOptions");
        if (EventData.Challenges.BOSS_TRAINER.options == null)
            optionsErrorList.add("bossTrainerChallengeOptions");
        if (EventData.Challenges.PVP.options == null)
            optionsErrorList.add("pvpChallengeOptions");

        // Forfeits.
        if (EventData.Forfeits.SHINY.options == null)
            optionsErrorList.add("shinyForfeitOptions");
        if (EventData.Forfeits.LEGENDARY.options == null)
            optionsErrorList.add("legendaryForfeitOptions");
        if (EventData.Forfeits.ULTRA_BEAST.options == null)
            optionsErrorList.add("ultraBeastForfeitOptions");
        if (EventData.Forfeits.BOSS.options == null)
            optionsErrorList.add("bossForfeitOptions");
        if (EventData.Forfeits.TRAINER.options == null)
            optionsErrorList.add("trainerForfeitOptions");
        if (EventData.Forfeits.BOSS_TRAINER.options == null)
            optionsErrorList.add("bossTrainerForfeitOptions");

        // Spawns.
        if (EventData.Spawns.SHINY.options == null)
            optionsErrorList.add("shinySpawnOptions");
        if (EventData.Spawns.LEGENDARY.options == null)
            optionsErrorList.add("legendarySpawnOptions");
        if (EventData.Spawns.ULTRA_BEAST.options == null)
            optionsErrorList.add("ultraBeastSpawnOptions");
        if (EventData.Spawns.WORMHOLE.options == null)
            optionsErrorList.add("wormholeSpawnOptions");
        if (EventData.Spawns.UNCOMMON_BOSS.options == null)
            optionsErrorList.add("uncommonBossSpawnOptions");
        if (EventData.Spawns.RARE_BOSS.options == null)
            optionsErrorList.add("rareBossSpawnOptions");
        if (EventData.Spawns.LEGENDARY_BOSS.options == null)
            optionsErrorList.add("legendaryBossSpawnOptions");
        if (EventData.Spawns.ULTIMATE_BOSS.options == null)
            optionsErrorList.add("ultimateBossSpawnOptions");

        // Victories.
        if (EventData.Victories.SHINY.options == null)
            optionsErrorList.add("shinyVictoryOptions");
        if (EventData.Victories.LEGENDARY.options == null)
            optionsErrorList.add("legendaryVictoryOptions");
        if (EventData.Victories.ULTRA_BEAST.options == null)
            optionsErrorList.add("ultraBeastVictoryOptions");
        if (EventData.Victories.UNCOMMON_BOSS.options == null)
            optionsErrorList.add("uncommonBossVictoryOptions");
        if (EventData.Victories.RARE_BOSS.options == null)
            optionsErrorList.add("rareBossVictoryOptions");
        if (EventData.Victories.LEGENDARY_BOSS.options == null)
            optionsErrorList.add("legendaryBossVictoryOptions");
        if (EventData.Victories.ULTIMATE_BOSS.options == null)
            optionsErrorList.add("ultimateBossVictoryOptions");
        if (EventData.Victories.TRAINER.options == null)
            optionsErrorList.add("trainerVictoryOptions");
        if (EventData.Victories.BOSS_TRAINER.options == null)
            optionsErrorList.add("bossTrainerVictoryOptions");
        if (EventData.Victories.PVP.options == null)
            optionsErrorList.add("pvpVictoryOptions");

        // Draws.
        if (EventData.Draws.PVP.options == null)
            optionsErrorList.add("pvpDrawOptions");

        // Hatches.
        if (EventData.Hatches.NORMAL.options == null)
            optionsErrorList.add("normalHatchOptions");
        if (EventData.Hatches.SHINY.options == null)
            optionsErrorList.add("shinyHatchOptions");
        if (EventData.Hatches.LEGENDARY.options == null)
            optionsErrorList.add("legendaryHatchOptions");
        if (EventData.Hatches.ULTRA_BEAST.options == null)
            optionsErrorList.add("ultraBeastHatchOptions");

        // Other options.
        if (EventData.Others.EVOLVE.options == null)
            optionsErrorList.add("evolveOptions");
        if (EventData.Others.FAINT.options == null)
            optionsErrorList.add("faintOptions");
        if (EventData.Others.TRADE.options == null)
            optionsErrorList.add("tradeOptions");

        // Print errors if something broke.
        if (!optionsErrorList.isEmpty())
            printOptionsNodeError(optionsErrorList);

        // We're done, phew. Tell the calling code.
        return true;
    }
}
