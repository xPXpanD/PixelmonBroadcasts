// Written for Pixelmon Reforged. Running this on Gens is unsupported and ill-advised, just like Gens itself.
package rs.expand.pixelmonbroadcasts;

// Remote imports.
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
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
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

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
// TODO: Round up when close.
// FIXME: Bad event listeners from other mods may cause events to hang (stuck loop), which causes insane spam from us. Fix?
// FIXME: Biome names are always English. Maybe add to the lang, and use English biome names as keys.
// FIXME: Roll over cleanly to a new line if more than 5 toggles are available in a single category?

@Plugin
(
        id = "pixelmonbroadcasts",
        name = "PixelmonBroadcasts",
        version = "0.3",
        dependencies = @Dependency(id = "pixelmon", version = "7.0"),
        description = "Adds fully custom legendary-like messages for tons of events, and optionally logs them, too.",
        authors = "XpanD"

        /*                                                                                                         *\
            Loosely inspired by PixelAnnouncer, which I totally forgot existed up until I wanted to release.
            After people reminded me that PA was a thing, I ended up making PBR a full-on replacement for it.

            Thanks for the go-ahead on that, Proxying! Let's make this count.                             -- XpanD
        \*                                                                                                         */
)

// Note: printBasicMessage is a static import for a method from PrintingMethods, for convenience. So are the listeners.
public class PixelmonBroadcasts
{
    // Set up an internal variable so we can see if we loaded correctly.
    private boolean loadedCorrectly = false;

    // Start setting up some basic variables that we'll fill in remotely when we read the config.
    //public static Integer configVersion;
    public static String commandAlias;

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
    public static boolean logShinyLegendaryBlackouts;
    public static boolean logShinyLegendaryCatches;
    public static boolean logShinyLegendaryChallenges;
    public static boolean logShinyLegendaryForfeits;
    public static boolean logShinyLegendarySpawns;
    public static boolean logShinyLegendaryVictories;
    public static boolean logShinySpawns;
    public static boolean logShinyVictories;
    public static boolean logTrades;
    public static boolean logTrainerBlackouts;
    public static boolean logTrainerChallenges;
    public static boolean logTrainerForfeits;
    public static boolean logTrainerVictories;

    // Set up broadcast settings.
    public static boolean showBossBlackouts;
    public static boolean showBossChallenges;
    public static boolean showBossForfeits;
    public static boolean showBossSpawns;
    public static boolean showBossTrainerBlackouts;
    public static boolean showBossTrainerChallenges;
    public static boolean showBossTrainerForfeits;
    public static boolean showBossTrainerVictories;
    public static boolean showBossVictories;
    //public static boolean showBirdTrioSummons;
    public static boolean showLegendaryBlackouts;
    public static boolean showLegendaryCatches;
    public static boolean showLegendaryChallenges;
    public static boolean showLegendaryForfeits;
    public static boolean showLegendarySpawns;
    public static boolean showLegendaryVictories;
    public static boolean showNormalBlackouts;
    public static boolean showNormalCatches;
    public static boolean showNormalHatches;
    public static boolean showPVPChallenges;
    public static boolean showPVPDraws;
    public static boolean showPVPVictories;
    public static boolean showShinyBlackouts;
    public static boolean showShinyCatches;
    public static boolean showShinyChallenges;
    public static boolean showShinyForfeits;
    public static boolean showShinyHatches;
    public static boolean showShinyLegendaryBlackouts;
    public static boolean showShinyLegendaryCatches;
    public static boolean showShinyLegendaryChallenges;
    public static boolean showShinyLegendaryForfeits;
    public static boolean showShinyLegendarySpawns;
    public static boolean showShinyLegendaryVictories;
    public static boolean showShinySpawns;
    public static boolean showShinyVictories;
    public static boolean showTrades;
    public static boolean showTrainerBlackouts;
    public static boolean showTrainerChallenges;
    public static boolean showTrainerForfeits;
    public static boolean showTrainerVictories;

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
    public static boolean hoverNormalBlackouts;
    public static boolean hoverNormalCatches;
    public static boolean hoverNormalHatches;
    public static boolean hoverShinyBlackouts;
    public static boolean hoverShinyCatches;
    public static boolean hoverShinyChallenges;
    public static boolean hoverShinyForfeits;
    public static boolean hoverShinyHatches;
    public static boolean hoverShinyLegendaryBlackouts;
    public static boolean hoverShinyLegendaryCatches;
    public static boolean hoverShinyLegendaryChallenges;
    public static boolean hoverShinyLegendaryForfeits;
    public static boolean hoverShinyLegendarySpawns;
    public static boolean hoverShinyLegendaryVictories;
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
    public static boolean revealNormalBlackouts;
    public static boolean revealNormalCatches;
    public static boolean revealNormalHatches;
    public static boolean revealShinyBlackouts;
    public static boolean revealShinyCatches;
    public static boolean revealShinyForfeits;
    public static boolean revealShinyHatches;
    public static boolean revealShinyLegendaryBlackouts;
    public static boolean revealShinyLegendaryCatches;
    public static boolean revealShinyLegendaryForfeits;
    public static boolean revealShinyLegendaryVictories;
    public static boolean revealShinyVictories;

    // Create and set up config paths, and grab an OS-specific file path separator. This will usually be a forward slash.
    private static String fileSystemSeparator = FileSystems.getDefault().getSeparator();
    public static String configPathAsString = "config" + fileSystemSeparator + "PixelmonBroadcasts" + fileSystemSeparator;
    public static Path settingsPath = Paths.get(configPathAsString, "settings.conf");
    public static Path messagesPath = Paths.get(configPathAsString, "messages.conf");
    public static Path broadcastsPath = Paths.get(configPathAsString, "broadcasts.conf");

    // Set up configuration loaders that we can call on later.
    public static ConfigurationLoader<CommentedConfigurationNode> settingsLoader =
            HoconConfigurationLoader.builder().setPath(settingsPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> messagesLoader =
            HoconConfigurationLoader.builder().setPath(messagesPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> broadcastsLoader =
            HoconConfigurationLoader.builder().setPath(broadcastsPath).build();

    // Set up a few places for us to load all our settings/messages/broadcasts into later.
    public static CommentedConfigurationNode settingsConfig = null;
    public static CommentedConfigurationNode messagesConfig = null;
    public static CommentedConfigurationNode broadcastsConfig = null;

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
        printBasicMessage("");
        printBasicMessage("=============== P I X E L M O N  B R O A D C A S T S ===============");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        // Store whether we actually loaded things up correctly in this bool, which we can check again later.
        loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to initializing more stuff.
        if (loadedCorrectly)
        {
            // Register listeners with Pixelmon.
            printBasicMessage("--> §aRegistering listeners with Pixelmon...");
            Pixelmon.EVENT_BUS.register(new BattleEndListener());
            Pixelmon.EVENT_BUS.register(new BattleStartListener());
            //Pixelmon.EVENT_BUS.register(new BirdSpawnListener());
            Pixelmon.EVENT_BUS.register(new CatchListener());
            Pixelmon.EVENT_BUS.register(new HatchListener());
            Pixelmon.EVENT_BUS.register(new SpawnListener());
            Pixelmon.EVENT_BUS.register(new TradeListener());
            Pixelmon.EVENT_BUS.register(new WildDefeatListener());

            // (re-)register the main command and alias. Use the result we get back to see if everything worked.
            printBasicMessage("--> §aRegistering commands with Sponge...");
            if (ConfigMethods.registerCommands())
                printBasicMessage("--> §aPre-init completed. All systems nominal.");
        }
        else
            printBasicMessage("--> §cLoad aborted due to critical errors.");

        // We're done, one way or another. Add a footer, and a space to avoid clutter with other marginal'd mods.
        printBasicMessage("====================================================================");
        printBasicMessage("");
    }

    @Listener
    public void onServerStartedEvent(final GameStartedServerEvent event)
    {
        if (loadedCorrectly)
        {
            // Check Pixelmon's config and get whether the legendary spawning message is in.
            final Boolean configStatus = toBooleanObject(
                    PixelmonConfig.getConfig().getNode("Spawning", "displayLegendaryGlobalMessage").getString());

            // Is the config setting we're reading available, /and/ is the setting turned on? Complain!
            if (configStatus != null && configStatus)
            {
                // Complaining, commence.
                printBasicMessage("=============== P I X E L M O N  B R O A D C A S T S ===============");
                printBasicMessage("--> §ePixelmon's \"§6displayLegendaryGlobalMessage§e\" setting is enabled.");
                printBasicMessage("    §eThis setting will now be disabled, as it conflicts with this sidemod.");
                printBasicMessage("    §eIf you remove this mod, revert this in Pixelmon's config!");
                printBasicMessage("====================================================================");

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
                    printBasicMessage("");
                    printBasicMessage("§cSomething went wrong during Pixelmon config reload from disk! Trace:");
                    F.printStackTrace();
                }
            }

            // TODO: Do config version warning messages here, too, when we start needing them.
        }
    }
}