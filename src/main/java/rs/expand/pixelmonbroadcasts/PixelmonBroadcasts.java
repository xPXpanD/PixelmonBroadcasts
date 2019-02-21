// Written for Pixelmon Reforged. Running this on Gens is unsupported and ill-advised, just like Gens itself.
package rs.expand.pixelmonbroadcasts;

// Remote imports.
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.overlay.notice.NoticeOverlay;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.game.state.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

// Local imports.
import rs.expand.pixelmonbroadcasts.commands.*;
import rs.expand.pixelmonbroadcasts.listeners.*;
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printUnformattedMessage;

/*                                                              *\
       THE WHO-KNOWS-WHEN LIST OF POTENTIALLY AWESOME IDEAS
    TODO: Add new TODOs here. Cross off TODOs if they're done.
      NOTE: Stuff that's here will not necessarily get done.
\*                                                              */

// TODO: Title-style display option?
// TODO: Implement logging to a custom log file with the right option passed.
// TODO: Ideas for new events: HA, successful breed, evolution, event spawns, maaaaybe level.
// TODO: See if some of the BattleEnd stuff can be moved to separate and more specific events.
// TODO: Maybe make events clickable? Staff node, teleport people to the source. Dunno.
// TODO: Add a logger for individual player Pokémon being knocked out, for the Nuzlocke crowd.
// TODO: Listen to commands being used, fire the right event if we have a successful hatch/spawn/etc..
// TODO: Make a more comprehensive summon check.
// TODO: Round up when close on stuff like IVs. Currently rounds down, even if at .99.
// FIXME: Bad event listeners from other mods may cause events to hang (stuck loop), which causes insane spam from us. Fix?
// FIXME: Biome names are always English. Maybe add to the lang, and use English biome names as keys.
// FIXME: Roll over cleanly to a new line if more than 5 toggles are available in a single category?

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

// Note: printUnformattedMessage is a static import for a method from PrintingMethods, for convenience. So are the listeners.
public class PixelmonBroadcasts
{
    // Set up an internal variable so we can see if we loaded correctly.
    private boolean loadedCorrectly = false;

    // Start setting up some basic variables that we'll fill in remotely when we read the config.
    public static Integer configVersion;
    public static String commandAlias;
    public static Boolean showAbilities;

    // Set up a hashmap for tracking shown Pixelmon notices. Allows us to kill them after a fixed amount of time.
    public static HashMap<UUID, Long> noticeExpiryMap = new HashMap<>();

    // Set up logging settings.
    public static boolean logBossBlackouts;
    public static boolean logBossChallenges;
    public static boolean logBossForfeits;
    public static boolean logBossSpawns;
    public static boolean logBossTrainerBlackouts;
    public static boolean logBossTrainerChallenges;
    public static boolean logBossTrainerForfeits;
    public static boolean logBossTrainerVictories;
    public static boolean logBossVictories;
    //public static boolean logBirdTrioSummons;
    public static boolean logLegendaryBlackouts;
    public static boolean logLegendaryCatches;
    public static boolean logLegendaryChallenges;
    public static boolean logLegendaryForfeits;
    public static boolean logLegendarySpawns;
    public static boolean logLegendaryVictories;
    public static boolean logUltraBeastBlackouts;
    public static boolean logUltraBeastCatches;
    public static boolean logUltraBeastChallenges;
    public static boolean logUltraBeastForfeits;
    public static boolean logUltraBeastSpawns;
    public static boolean logUltraBeastVictories;
    public static boolean logWormholeSpawns;
    public static boolean logNormalBlackouts;
    public static boolean logNormalCatches;
    public static boolean logNormalHatches;
    public static boolean logPVPChallenges;
    public static boolean logPVPDraws;
    public static boolean logPVPVictories;
    public static boolean logShinyBlackouts;
    public static boolean logShinyCatches;
    public static boolean logShinyChallenges;
    public static boolean logShinyForfeits;
    public static boolean logShinyHatches;
    public static boolean logShinySpawns;
    public static boolean logShinyVictories;
    public static boolean logTrades;
    public static boolean logTrainerBlackouts;
    public static boolean logTrainerChallenges;
    public static boolean logTrainerForfeits;
    public static boolean logTrainerVictories;

    // Set up chat broadcast settings.
    public static boolean printBossBlackouts;
    public static boolean printBossChallenges;
    public static boolean printBossForfeits;
    public static boolean printBossSpawns;
    public static boolean printBossTrainerBlackouts;
    public static boolean printBossTrainerChallenges;
    public static boolean printBossTrainerForfeits;
    public static boolean printBossTrainerVictories;
    public static boolean printBossVictories;
    //public static boolean printBirdTrioSummons;
    public static boolean printLegendaryBlackouts;
    public static boolean printLegendaryCatches;
    public static boolean printLegendaryChallenges;
    public static boolean printLegendaryForfeits;
    public static boolean printLegendarySpawns;
    public static boolean printLegendaryVictories;
    public static boolean printUltraBeastBlackouts;
    public static boolean printUltraBeastCatches;
    public static boolean printUltraBeastChallenges;
    public static boolean printUltraBeastForfeits;
    public static boolean printUltraBeastSpawns;
    public static boolean printUltraBeastVictories;
    public static boolean printWormholeSpawns;
    public static boolean printNormalBlackouts;
    public static boolean printNormalCatches;
    public static boolean printNormalHatches;
    public static boolean printPVPChallenges;
    public static boolean printPVPDraws;
    public static boolean printPVPVictories;
    public static boolean printShinyBlackouts;
    public static boolean printShinyCatches;
    public static boolean printShinyChallenges;
    public static boolean printShinyForfeits;
    public static boolean printShinyHatches;
    public static boolean printShinySpawns;
    public static boolean printShinyVictories;
    public static boolean printTrades;
    public static boolean printTrainerBlackouts;
    public static boolean printTrainerChallenges;
    public static boolean printTrainerForfeits;
    public static boolean printTrainerVictories;

    // Set up noticeboard broadcast settings.
    public static boolean notifyBossBlackouts;
    public static boolean notifyBossChallenges;
    public static boolean notifyBossForfeits;
    public static boolean notifyBossSpawns;
    public static boolean notifyBossTrainerBlackouts;
    public static boolean notifyBossTrainerChallenges;
    public static boolean notifyBossTrainerForfeits;
    public static boolean notifyBossTrainerVictories;
    public static boolean notifyBossVictories;
    //public static boolean notifyBirdTrioSummons;
    public static boolean notifyLegendaryBlackouts;
    public static boolean notifyLegendaryCatches;
    public static boolean notifyLegendaryChallenges;
    public static boolean notifyLegendaryForfeits;
    public static boolean notifyLegendarySpawns;
    public static boolean notifyLegendaryVictories;
    public static boolean notifyUltraBeastBlackouts;
    public static boolean notifyUltraBeastCatches;
    public static boolean notifyUltraBeastChallenges;
    public static boolean notifyUltraBeastForfeits;
    public static boolean notifyUltraBeastSpawns;
    public static boolean notifyUltraBeastVictories;
    public static boolean notifyWormholeSpawns;
    public static boolean notifyNormalBlackouts;
    public static boolean notifyNormalCatches;
    public static boolean notifyNormalHatches;
    public static boolean notifyPVPChallenges;
    public static boolean notifyPVPDraws;
    public static boolean notifyPVPVictories;
    public static boolean notifyShinyBlackouts;
    public static boolean notifyShinyCatches;
    public static boolean notifyShinyChallenges;
    public static boolean notifyShinyForfeits;
    public static boolean notifyShinyHatches;
    public static boolean notifyShinySpawns;
    public static boolean notifyShinyVictories;
    public static boolean notifyTrades;
    public static boolean notifyTrainerBlackouts;
    public static boolean notifyTrainerChallenges;
    public static boolean notifyTrainerForfeits;
    public static boolean notifyTrainerVictories;

    // Set up hover settings.
    public static boolean hoverBossBlackouts;
    public static boolean hoverBossChallenges;
    public static boolean hoverBossForfeits;
    public static boolean hoverBossSpawns;
    public static boolean hoverBossVictories;
    public static boolean hoverLegendaryBlackouts;
    public static boolean hoverLegendaryCatches;
    public static boolean hoverLegendaryChallenges;
    public static boolean hoverLegendaryForfeits;
    public static boolean hoverLegendarySpawns;
    public static boolean hoverLegendaryVictories;
    public static boolean hoverUltraBeastBlackouts;
    public static boolean hoverUltraBeastCatches;
    public static boolean hoverUltraBeastChallenges;
    public static boolean hoverUltraBeastForfeits;
    public static boolean hoverUltraBeastSpawns;
    public static boolean hoverUltraBeastVictories;
    public static boolean hoverNormalBlackouts;
    public static boolean hoverNormalCatches;
    public static boolean hoverNormalHatches;
    public static boolean hoverShinyBlackouts;
    public static boolean hoverShinyCatches;
    public static boolean hoverShinyChallenges;
    public static boolean hoverShinyForfeits;
    public static boolean hoverShinyHatches;
    public static boolean hoverShinySpawns;
    public static boolean hoverShinyVictories;

    // Set up reveal settings.
    public static boolean revealBossBlackouts;
    public static boolean revealBossForfeits;
    public static boolean revealBossVictories;
    public static boolean revealLegendaryBlackouts;
    public static boolean revealLegendaryCatches;
    public static boolean revealLegendaryForfeits;
    public static boolean revealLegendaryVictories;
    public static boolean revealUltraBeastBlackouts;
    public static boolean revealUltraBeastCatches;
    public static boolean revealUltraBeastForfeits;
    public static boolean revealUltraBeastVictories;
    public static boolean revealNormalBlackouts;
    public static boolean revealNormalCatches;
    public static boolean revealNormalHatches;
    public static boolean revealShinyBlackouts;
    public static boolean revealShinyCatches;
    public static boolean revealShinyForfeits;
    public static boolean revealShinyHatches;
    public static boolean revealShinyVictories;

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
        printUnformattedMessage("");
        printUnformattedMessage("=============== P I X E L M O N  B R O A D C A S T S ===============");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        // Store whether we actually loaded things up correctly in this bool, which we can check again later.
        loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to initializing more stuff.
        if (loadedCorrectly)
        {
            // Register listeners with Pixelmon.
            printUnformattedMessage("--> §aRegistering listeners with Pixelmon...");
            Pixelmon.EVENT_BUS.register(new BattleEndListener());
            Pixelmon.EVENT_BUS.register(new BattleStartListener());
            //Pixelmon.EVENT_BUS.register(new BirdSpawnListener());
            Pixelmon.EVENT_BUS.register(new CatchListener());
            Pixelmon.EVENT_BUS.register(new HatchListener());
            Pixelmon.EVENT_BUS.register(new SpawnListener());
            Pixelmon.EVENT_BUS.register(new TradeListener());
            Pixelmon.EVENT_BUS.register(new WildDefeatListener());

            // (re-)register the main command and alias. Use the result we get back to see if everything worked.
            printUnformattedMessage("--> §aRegistering commands with Sponge...");
            if (ConfigMethods.registerCommands())
                printUnformattedMessage("--> §aPre-init completed. All systems nominal.");
        }
        else
            printUnformattedMessage("--> §cLoad aborted due to critical errors.");

        // We're done, one way or another. Add a footer, and a space to avoid clutter with other marginal'd mods.
        printUnformattedMessage("====================================================================");
        printUnformattedMessage("");
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
                printUnformattedMessage("=============== P I X E L M O N  B R O A D C A S T S ===============");
                printUnformattedMessage("--> §ePixelmon's \"§6displayLegendaryGlobalMessage§e\" setting is enabled.");
                printUnformattedMessage("    §eThis setting will now be disabled, as it conflicts with this sidemod.");
                printUnformattedMessage("    §eIf you remove this mod, revert this in Pixelmon's config!");
                printUnformattedMessage("====================================================================");

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
                    printUnformattedMessage("");
                    printUnformattedMessage("§cSomething went wrong during Pixelmon config reload from disk! Trace:");
                    F.printStackTrace();
                }

                if (configVersion != null && configVersion < 30)
                    printUnformattedMessage("");
            }

            if (configVersion != null && configVersion < 30)
            {
                // More complaining, commence.
                printUnformattedMessage("=============== P I X E L M O N  B R O A D C A S T S ===============");
                printUnformattedMessage("--> §ePixelmon Broadcast has a new feature! We can now show abilities.");
                printUnformattedMessage("    §ePlease open your \"§6settings.conf§e\" file and add the following:");
                printUnformattedMessage("");
                printUnformattedMessage("    showAbilities = true (or false, pick one)");
                printUnformattedMessage("");
                printUnformattedMessage("    §eAlso change the value of \"§6configVersion§e\" to \"§630§e\".");
                printUnformattedMessage("====================================================================");

                // TODO: Get this working without it squashing the whole config down.
                /*// Set the config's version value to 30.
                try
                {
                    settingsConfig.getNode("configVersion").setValue(30);
                    settingsLoader.save(settingsLoader.load());
                }
                catch (IOException F)
                {
                    printBasicError("Something broke while updating config version! Please report. Stack trace:");
                    F.printStackTrace();
                }*/
            }
        }
    }
}