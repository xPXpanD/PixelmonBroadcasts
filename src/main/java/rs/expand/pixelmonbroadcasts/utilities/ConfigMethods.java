// The probably-too-complicated config handler, mark whatever. A bit less messy, but still needs work.
package rs.expand.pixelmonbroadcasts.utilities;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.plugin.PluginContainer;
import rs.expand.pixelmonbroadcasts.PixelmonBroadcasts;
import rs.expand.pixelmonbroadcasts.enums.Events;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;

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
        // TODO: Make this less messy. It works, but there's probably more efficient ways to read this.

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

        // Write the toggles in settings.conf to our event data enum for later use.
        /// Blackouts.
        Events.Blackouts.NORMAL.settings = settingsConfig.getNode("normalBlackoutOptions").getString();
        Events.Blackouts.SHINY.settings = settingsConfig.getNode("shinyBlackoutOptions").getString();
        Events.Blackouts.LEGENDARY.settings = settingsConfig.getNode("legendaryBlackoutOptions").getString();
        Events.Blackouts.ULTRA_BEAST.settings = settingsConfig.getNode("ultraBeastBlackoutOptions").getString();
        Events.Blackouts.BOSS.settings = settingsConfig.getNode("bossBlackoutOptions").getString();
        Events.Blackouts.TRAINER.settings = settingsConfig.getNode("trainerBlackoutOptions").getString();
        Events.Blackouts.BOSS_TRAINER.settings = settingsConfig.getNode("bossTrainerBlackoutOptions").getString();

        /// Catches.
        Events.Catches.NORMAL.settings = settingsConfig.getNode("normalCatchOptions").getString();
        Events.Catches.SHINY.settings = settingsConfig.getNode("shinyCatchOptions").getString();
        Events.Catches.LEGENDARY.settings = settingsConfig.getNode("legendaryCatchOptions").getString();
        Events.Catches.ULTRA_BEAST.settings = settingsConfig.getNode("ultraBeastCatchOptions").getString();

        /// Challenges.
        Events.Challenges.SHINY.settings = settingsConfig.getNode("shinyChallengeOptions").getString();
        Events.Challenges.LEGENDARY.settings = settingsConfig.getNode("legendaryChallengeOptions").getString();
        Events.Challenges.ULTRA_BEAST.settings = settingsConfig.getNode("ultraBeastChallengeOptions").getString();
        Events.Challenges.BOSS.settings = settingsConfig.getNode("bossChallengeOptions").getString();
        Events.Challenges.TRAINER.settings = settingsConfig.getNode("trainerChallengeOptions").getString();
        Events.Challenges.BOSS_TRAINER.settings = settingsConfig.getNode("bossTrainerChallengeOptions").getString();
        Events.Challenges.PVP.settings = settingsConfig.getNode("pvpChallengeOptions").getString();

        /// Forfeits.
        Events.Forfeits.SHINY.settings = settingsConfig.getNode("shinyForfeitOptions").getString();
        Events.Forfeits.LEGENDARY.settings = settingsConfig.getNode("legendaryForfeitOptions").getString();
        Events.Forfeits.ULTRA_BEAST.settings = settingsConfig.getNode("ultraBeastForfeitOptions").getString();
        Events.Forfeits.BOSS.settings = settingsConfig.getNode("bossForfeitOptions").getString();
        Events.Forfeits.TRAINER.settings = settingsConfig.getNode("trainerForfeitOptions").getString();
        Events.Forfeits.BOSS_TRAINER.settings = settingsConfig.getNode("bossTrainerForfeitOptions").getString();

        /// Spawns.
        Events.Spawns.SHINY.settings = settingsConfig.getNode("shinySpawnOptions").getString();
        Events.Spawns.LEGENDARY.settings = settingsConfig.getNode("legendarySpawnOptions").getString();
        Events.Spawns.ULTRA_BEAST.settings = settingsConfig.getNode("ultraBeastSpawnOptions").getString();
        Events.Spawns.WORMHOLE.settings = settingsConfig.getNode("wormholeSpawnOptions").getString();
        Events.Spawns.BOSS.settings = settingsConfig.getNode("bossSpawnOptions").getString();

        /// Victories.
        Events.Victories.SHINY.settings = settingsConfig.getNode("shinyVictoryOptions").getString();
        Events.Victories.LEGENDARY.settings = settingsConfig.getNode("legendaryVictoryOptions").getString();
        Events.Victories.ULTRA_BEAST.settings = settingsConfig.getNode("ultraBeastVictoryOptions").getString();
        Events.Victories.BOSS.settings = settingsConfig.getNode("bossVictoryOptions").getString();
        Events.Victories.TRAINER.settings = settingsConfig.getNode("trainerVictoryOptions").getString();
        Events.Victories.BOSS_TRAINER.settings = settingsConfig.getNode("bossTrainerVictoryOptions").getString();
        Events.Victories.PVP.settings = settingsConfig.getNode("pvpVictoryOptions").getString();

        /// Hatches.
        Events.Hatches.NORMAL.settings = settingsConfig.getNode("normalHatchOptions").getString();
        Events.Hatches.SHINY.settings = settingsConfig.getNode("shinyHatchOptions").getString();
        Events.Hatches.LEGENDARY.settings = settingsConfig.getNode("legendaryHatchOptions").getString();
        Events.Hatches.ULTRA_BEAST.settings = settingsConfig.getNode("ultraBeastHatchOptions").getString();

        /// Other toggles.
        Events.Others.TRADE.settings = settingsConfig.getNode("tradeOptions").getString();
        Events.Others.DRAW.settings = settingsConfig.getNode("pvpDrawOptions").getString();
        Events.Others.FAINT.settings = settingsConfig.getNode("faintOptions").getString();

        // For these hybrid events, combine toggles for both of them. Nulls or bad joins won't matter. A bit hacky.
        // Blackouts.
        Events.Blackouts.SHINY_LEGENDARY.settings =
                settingsConfig.getNode("shinyBlackouts§Options").getString() + settingsConfig.getNode("legendaryBlackouts§Options").getString();
        Events.Blackouts.SHINY_ULTRA_BEAST.settings =
                settingsConfig.getNode("shinyBlackouts§Options").getString() + settingsConfig.getNode("ultraBeastBlackouts§Options").getString();

        // Catches.
        Events.Catches.SHINY_LEGENDARY.settings =
                settingsConfig.getNode("shinyCatchOptions").getString() + settingsConfig.getNode("legendaryCatchOptions").getString();
        Events.Catches.SHINY_ULTRA_BEAST.settings =
                settingsConfig.getNode("shinyCatchOptions").getString() + settingsConfig.getNode("ultraBeastCatchOptions").getString();

        // Challenges.
        Events.Challenges.SHINY_LEGENDARY.settings =
                settingsConfig.getNode("shinyChallengeOptions").getString() + settingsConfig.getNode("legendaryChallengeOptions").getString();
        Events.Challenges.SHINY_ULTRA_BEAST.settings =
                settingsConfig.getNode("shinyChallengeOptions").getString() + settingsConfig.getNode("ultraBeastChallengeOptions").getString();

        // Forfeits.
        Events.Forfeits.SHINY_LEGENDARY.settings =
                settingsConfig.getNode("shinyForfeitOptions").getString() + settingsConfig.getNode("legendaryForfeitOptions").getString();
        Events.Forfeits.SHINY_ULTRA_BEAST.settings =
                settingsConfig.getNode("shinyForfeitOptions").getString() + settingsConfig.getNode("ultraBeastForfeitOptions").getString();

        // Spawns.
        Events.Spawns.SHINY_LEGENDARY.settings =
                settingsConfig.getNode("shinySpawnOptions").getString() + settingsConfig.getNode("legendarySpawnOptions").getString();
        Events.Spawns.SHINY_ULTRA_BEAST.settings =
                settingsConfig.getNode("shinySpawnOptions").getString() + settingsConfig.getNode("ultraBeastSpawnOptions").getString();

        // Victories.
        Events.Victories.SHINY_LEGENDARY.settings =
                settingsConfig.getNode("shinyVictoryOptions").getString() + settingsConfig.getNode("legendaryVictoryOptions").getString();
        Events.Victories.SHINY_ULTRA_BEAST.settings =
                settingsConfig.getNode("shinyVictoryOptions").getString() + settingsConfig.getNode("ultraBeastVictoryOptions").getString();

        // Hatches.
        Events.Hatches.SHINY_LEGENDARY.settings =
                settingsConfig.getNode("shinyHatchOptions").getString() + settingsConfig.getNode("legendaryHatchOptions").getString();
        Events.Hatches.SHINY_ULTRA_BEAST.settings =
                settingsConfig.getNode("shinyHatchOptions").getString() + settingsConfig.getNode("ultraBeastHatchOptions").getString();

        // We're done, phew. Tell the calling code.
        return true;
    }
}
