// Written for Pixelmon Reforged. Running this on Gens is unsupported and ill-advised, just like Gens itself.
package rs.expand.pixelmonbroadcasts;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.overlay.notice.NoticeOverlay;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import rs.expand.pixelmonbroadcasts.commands.BaseCommand;
import rs.expand.pixelmonbroadcasts.commands.Reload;
import rs.expand.pixelmonbroadcasts.commands.Toggle;
import rs.expand.pixelmonbroadcasts.listeners.*;
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

/*                                                              *\
       THE WHO-KNOWS-WHEN LIST OF POTENTIALLY AWESOME IDEAS
    TODO: Add new TODOs here. Cross off TODOs if they're done.
      NOTE: Stuff that's here will not necessarily get done.
\*                                                              */

// TODO: Implement logging to a custom log file with the right option passed.
// TODO: Ideas for new events: HA, successful breed, evolution, event spawns, maaaaybe level.
// TODO: See if some of the BattleEnd stuff can be moved to separate and more specific events.
// TODO: Listen to commands being used, fire the right event if we have a successful hatch/spawn/etc..
// TODO: Make a more comprehensive summon check.
// TODO: Round up when close on stuff like IVs. Currently rounds down, even if at .99.
// TODO: Maybe play a cry when something spawns. Slow it down?
// FIXME: Bad event listeners from other mods may cause events to hang (stuck loop), which causes insane spam from us. Fix?
// FIXME: Biome names are always English. Maybe add to the lang, and use English biome names as keys.
// FIXME: Roll over cleanly to a new line if more than 5 toggles are available in a single category?
// FIXME: Challenges and forfeits can be used to spam servers. Add a persistent tag to avoid repeats?

@Plugin
(
        id = "pixelmonbroadcasts",
        name = "PixelmonBroadcasts",
        version = "0.4",
        dependencies = @Dependency(id = "pixelmon", version = "7.0"),
        description = "Adds fully custom legendary-like messages for tons of events, and optionally logs them, too.",
        authors = "XpanD"

        /*                                                                                                         *\
            Loosely inspired by PixelAnnouncer, which I totally forgot existed up until I wanted to release.
            After people reminded me that PA was a thing, I ended up making PBR a full-on replacement for it.

            Thanks for the go-ahead on that, Proxying! Let's make this count.                             -- XpanD
        \*                                                                                                         */
)

public class PixelmonBroadcasts
{
    // Set up an internal variable so we can see if we loaded correctly.
    private boolean loadedCorrectly = false;

    // Set up a logger for logging stuff. Yup.
    public static final Logger logger = LogManager.getLogger("Pixelmon Broadcasts");

    // Start setting up some basic variables that we'll fill in remotely when we read the config.
    public static Integer configVersion;
    public static String commandAlias;
    public static Boolean showAbilities;

    // Set up a hashmap for tracking shown Pixelmon notices. Allows us to kill them after a fixed amount of time.
    public static HashMap<UUID, Long> noticeExpiryMap = new HashMap<>();

    // Create and set up config paths, and grab an OS-specific file path separator. This will usually be a forward slash.
    private static String fileSystemSeparator = FileSystems.getDefault().getSeparator();
    public static String configPathAsString = "config" + fileSystemSeparator + "PixelmonBroadcasts" + fileSystemSeparator;
    public static Path broadcastsPath = Paths.get(configPathAsString, "broadcasts.conf");
    public static Path messagesPath = Paths.get(configPathAsString, "messages.conf");
    public static Path settingsPath = Paths.get(configPathAsString, "settings.conf");

    // Set up configuration loaders that we can call on later.
    public static ConfigurationLoader<CommentedConfigurationNode> broadcastsLoader =
            HoconConfigurationLoader.builder().setPath(broadcastsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> messagesLoader =
            HoconConfigurationLoader.builder().setPath(messagesPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> settingsLoader =
            HoconConfigurationLoader.builder().setPath(settingsPath).build();

    // Set up a few places for us to load all our settings/messages/broadcasts into later.
    public static CommentedConfigurationNode broadcastsConfig = null;
    public static CommentedConfigurationNode messagesConfig = null;
    public static CommentedConfigurationNode settingsConfig = null;

    /*                       *\
         Utility commands.
    \*                       */
    private static CommandSpec togglepreferences = CommandSpec.builder()
            .permission("pixelmonbroadcasts.command.toggle")
            .arguments(GenericArguments.optionalWeak(GenericArguments.string(Text.of("setting"))))
            .executor(new Toggle())
            .build();

    private static CommandSpec reloadconfigs = CommandSpec.builder()
            .permission("pixelmonbroadcasts.command.staff.reload")
            .executor(new Reload())
            .build();

    public static CommandCallable basecommand = CommandSpec.builder()
            .child(reloadconfigs, "reload")
            .child(togglepreferences, "toggle")
            .executor(new BaseCommand())
            .build();

    @Listener
    public void onGamePreInitEvent(final GamePreInitializationEvent event)
    {
        // Load up all the configs and figure out the info alias. Start printing. Methods may insert errors as they go.
        logger.info("");
        logger.info("=============== P I X E L M O N  B R O A D C A S T S ===============");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        // Store whether we actually loaded things up correctly in this bool, which we can check again later.
        loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to initializing more stuff.
        if (loadedCorrectly)
        {
            // Register listeners with Pixelmon.
            logger.info("--> §aRegistering listeners with Pixelmon...");
            Pixelmon.EVENT_BUS.register(new BattleEndListener());
            Pixelmon.EVENT_BUS.register(new BattleStartListener());
            //Pixelmon.EVENT_BUS.register(new BirdSpawnListener());
            Pixelmon.EVENT_BUS.register(new CatchListener());
            Pixelmon.EVENT_BUS.register(new HatchListener());
            Pixelmon.EVENT_BUS.register(new PokemonFaintListener());
            Pixelmon.EVENT_BUS.register(new SpawnListener());
            Pixelmon.EVENT_BUS.register(new TradeListener());

            // (re-)register the main command and alias. Use the result we get back to see if everything worked.
            logger.info("--> §aRegistering commands with Sponge...");
            if (ConfigMethods.tryRegisterCommands())
                logger.info("--> §aPre-init completed. All systems nominal.");
        }
        else
            logger.info("--> §cLoad aborted due to critical errors.");

        // We're done, one way or another. Add a footer, and a space to avoid clutter with other marginal'd mods.
        logger.info("====================================================================");
        logger.info("");
    }

    @Listener
    public void onServerStartedEvent(final GameStartedServerEvent event)
    {
        if (loadedCorrectly)
        {
            // Set up a repeating task. It checks if any players need their notices wiped. (happens every 10-12s)
            // Won't do much if Pixelmon's notice board (the thing that shows messages at the top) isn't being sent to.
            final ScheduledExecutorService noticeClearTimer = Executors.newSingleThreadScheduledExecutor();
            final Server server = Sponge.getGame().getServer();
            noticeClearTimer.scheduleWithFixedDelay(() ->
            {
                // Grab current time in milliseconds.
                final long currentTime = System.currentTimeMillis();

                // Iterate through all online players.
                server.getOnlinePlayers().forEach(player ->
                {
                    // Check if our player is using any noticeboard-enabled broadcasts.
                    if (noticeExpiryMap.containsKey(player.getUniqueId()))
                    {
                        // Are we 10 or more seconds ahead of the last time a notice was added? Hide it!
                        if (currentTime - noticeExpiryMap.get(player.getUniqueId()) >= 10000)
                        {
                            // Hide the overlay for the targeted player.
                            NoticeOverlay.hide((EntityPlayerMP) player);

                            // Remove the player's UUID from the map. This prevents needless clears every iteration.
                            noticeExpiryMap.remove(player.getUniqueId());
                        }
                    }
                });
            }, 0, 2, TimeUnit.SECONDS);

            // Check Pixelmon's config and get whether the legendary spawning message is in.
            final Boolean configStatus = toBooleanObject(
                    PixelmonConfig.getConfig().getNode("Spawning", "displayLegendaryGlobalMessage").getString());

            // Is the config setting we're reading available, /and/ is the setting turned on? Complain!
            if (configStatus != null && configStatus)
            {
                // Complaining, commence.
                logger.info("=============== P I X E L M O N  B R O A D C A S T S ===============");
                logger.info("--> §ePixelmon's \"§6displayLegendaryGlobalMessage§e\" setting is enabled.");
                logger.info("    §eThis setting will now be disabled as it conflicts with this mod.");
                logger.info("    §eIf you remove this mod, revert this in Pixelmon's config!");
                logger.info("====================================================================");

                // Flip the setting in Pixelmon's config.
                PixelmonConfig.getConfig().getNode("Spawning", "displayLegendaryGlobalMessage").setValue(false);
                PixelmonConfig.saveConfig();

                // Force a config reload from disk.
                try
                {
                    PixelmonConfig.reload(true);
                }
                catch (IOException F)
                {
                    logger.info("");
                    logger.info("§cSomething went wrong during Pixelmon config reload from disk! Trace:");
                    F.printStackTrace();
                }

                if (configVersion != null && configVersion < 30)
                    logger.info("");
            }

            if (configVersion != null && configVersion < 30)
            {
                // More complaining, commence.
                logger.info("=============== P I X E L M O N  B R O A D C A S T S ===============");
                logger.info("--> §ePixelmon Broadcast has a new feature! We can now show abilities.");
                logger.info("    §ePlease open your \"§6settings.conf§e\" file and add the following:");
                logger.info("");
                logger.info("    showAbilities = true (or false, pick one)");
                logger.info("");
                logger.info("    §eAlso change the value of \"§6configVersion§e\" to \"§630§e\".");
                logger.info("====================================================================");

                // TODO: Get this working without it squashing the whole config down.
                /*// Set the config's version value to 30.
                try
                {
                    settingsConfig.getNode("configVersion").setValue(30);
                    settingsLoader.save(settingsLoader.load());
                }
                catch (IOException F)
                {
                    logger.error("Something broke while updating config version! Please report. Stack trace:");
                    F.printStackTrace();
                }*/
            }
        }
    }
}