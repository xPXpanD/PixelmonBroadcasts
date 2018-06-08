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
// TODO: Move messages to a proper config-esque file.
// TODO: Make %biome% and %world% work on trainer and PvP messages.
// TODO: Ideas for new events: HA, successful breed, evolution.
// TODO: See if some of the BattleEnd stuff can be moved to separate more specific events.
// TODO: Consider what to do with forfeit/blackout IV showing. Currently disabled.
// TODO: Add a blackout option for normal Pokémon.

@Plugin
(
        id = "pixelmonbroadcasts",
        name = "PixelmonBroadcasts",
        version = "0.2.0",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds fully custom legendary-like messages for tons of events, and optionally logs them, too.",
        authors = "XpanD"

        /*                                                                                                         *\
            Loosely inspired by PixelAnnouncer, which I totally forgot existed up until I first wanted to release.
            After people reminded me that PA was a thing, I ended up making a full-on replacement for it.

            Thanks for the go-ahead on that, Proxying! Let's make this count.                             -- XpanD
        \*                                                                                                         */
)

// Note: printBasicMessage is a static import for a method from PrintingMethods, for convenience. So are the listeners.
public class PixelmonBroadcasts
{
    // Start setting up some basic variables that we'll fill in remotely when we read the config.
    //public static Integer configVersion;
    public static String commandAlias;

    // Set up logging settings.
    public static boolean logNormalBlackouts;
    public static boolean logNormalCatches;
    public static boolean logNormalHatches;
    public static boolean logLegendarySpawns;
    public static boolean logLegendaryChallenges;
    public static boolean logLegendaryCatches;
    public static boolean logLegendaryVictories;
    public static boolean logLegendaryBlackouts;
    public static boolean logLegendaryForfeits;
    public static boolean logShinyLegendarySpawns;
    public static boolean logShinyLegendaryChallenges;
    public static boolean logShinyLegendaryCatches;
    public static boolean logShinyLegendaryVictories;
    public static boolean logShinyLegendaryBlackouts;
    public static boolean logShinyLegendaryForfeits;
    public static boolean logShinySpawns;
    public static boolean logShinyChallenges;
    public static boolean logShinyCatches;
    public static boolean logShinyVictories;
    public static boolean logShinyBlackouts;
    public static boolean logShinyForfeits;
    public static boolean logShinyHatches;
    public static boolean logBossSpawns;
    public static boolean logBossChallenges;
    public static boolean logBossVictories;
    public static boolean logBossBlackouts;
    public static boolean logBossForfeits;
    public static boolean logBossTrainerChallenges;
    public static boolean logBossTrainerVictories;
    public static boolean logBossTrainerBlackouts;
    public static boolean logBossTrainerForfeits;
    public static boolean logTrainerChallenges;
    public static boolean logTrainerVictories;
    public static boolean logTrainerBlackouts;
    public static boolean logTrainerForfeits;
    public static boolean logPVPStarts;
    public static boolean logPVPVictories;
    public static boolean logPVPDraws;
    public static boolean logTrades;

    // Set up broadcast settings.
    public static boolean showNormalBlackouts;
    public static boolean showNormalCatches;
    public static boolean showNormalHatches;
    public static boolean showLegendarySpawns;
    public static boolean showLegendaryChallenges;
    public static boolean showLegendaryCatches;
    public static boolean showLegendaryVictories;
    public static boolean showLegendaryBlackouts;
    public static boolean showLegendaryForfeits;
    public static boolean showShinyLegendarySpawns;
    public static boolean showShinyLegendaryChallenges;
    public static boolean showShinyLegendaryCatches;
    public static boolean showShinyLegendaryVictories;
    public static boolean showShinyLegendaryBlackouts;
    public static boolean showShinyLegendaryForfeits;
    public static boolean showShinySpawns;
    public static boolean showShinyChallenges;
    public static boolean showShinyCatches;
    public static boolean showShinyVictories;
    public static boolean showShinyHatches;
    public static boolean showShinyBlackouts;
    public static boolean showShinyForfeits;
    public static boolean showBossSpawns;
    public static boolean showBossChallenges;
    public static boolean showBossVictories;
    public static boolean showBossBlackouts;
    public static boolean showBossForfeits;
    public static boolean showBossTrainerChallenges;
    public static boolean showBossTrainerVictories;
    public static boolean showBossTrainerBlackouts;
    public static boolean showBossTrainerForfeits;
    public static boolean showTrainerChallenges;
    public static boolean showTrainerVictories;
    public static boolean showTrainerBlackouts;
    public static boolean showTrainerForfeits;
    public static boolean showPVPStarts;
    public static boolean showPVPVictories;
    public static boolean showPVPDraws;
    public static boolean showTrades;

    // Set up hover settings.
    public static boolean hoverNormalBlackouts;
    public static boolean hoverNormalCatches;
    public static boolean hoverNormalHatches;
    public static boolean hoverLegendarySpawns;
    public static boolean hoverLegendaryChallenges;
    public static boolean hoverLegendaryCatches;
    public static boolean hoverLegendaryVictories;
    public static boolean hoverLegendaryBlackouts;
    public static boolean hoverLegendaryForfeits;
    public static boolean hoverShinyLegendarySpawns;
    public static boolean hoverShinyLegendaryChallenges;
    public static boolean hoverShinyLegendaryCatches;
    public static boolean hoverShinyLegendaryVictories;
    public static boolean hoverShinyLegendaryBlackouts;
    public static boolean hoverShinyLegendaryForfeits;
    public static boolean hoverShinySpawns;
    public static boolean hoverShinyChallenges;
    public static boolean hoverShinyCatches;
    public static boolean hoverShinyVictories;
    public static boolean hoverShinyBlackouts;
    public static boolean hoverShinyForfeits;
    public static boolean hoverShinyHatches;
    public static boolean hoverBossSpawns;
    public static boolean hoverBossChallenges;
    public static boolean hoverBossVictories;
    public static boolean hoverBossBlackouts;
    public static boolean hoverBossForfeits;

    // Create and set up a config path, and grab an OS-specific file path separator. This will usually be a forward slash.
    private static String fileSystemSeparator = FileSystems.getDefault().getSeparator();
    public static String primaryPath = "config" + fileSystemSeparator + "PixelmonBroadcasts" + fileSystemSeparator;
    public static Path broadcastPath = Paths.get(primaryPath, "broadcasts.conf");
    public static Path messagePath = Paths.get(primaryPath, "messages.conf");
    public static Path configPath = Paths.get(primaryPath, "settings.conf");

    private static ConfigurationLoader<CommentedConfigurationNode> broadcastLoader =
            HoconConfigurationLoader.builder().setPath(broadcastPath).build();
    private static ConfigurationLoader<CommentedConfigurationNode> messageLoader =
            HoconConfigurationLoader.builder().setPath(messagePath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> settingLoader =
            HoconConfigurationLoader.builder().setPath(configPath).build();

    public static CommentedConfigurationNode messageConfig = null;
    public static CommentedConfigurationNode broadcastConfig = null;

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

    // Originally ran on pre-init, but I could not get info from Pixelmon's config at that stage. This seems to work.
    @Listener
    public void onGameInitEvent(final GameInitializationEvent event)
    {
        // Load up the primary config and the info command config, and figure out the info alias.
        // We start printing stuff, here. If any warnings/errors pop up they'll be shown here.
        printBasicMessage("");
        printBasicMessage("================== P I X E L M O N   B R O A D C A S T S ==================");

        // Create a config directory if it doesn't exist. Silently swallow an error if it does. I/O is awkward.
        ConfigMethods.checkConfigDir();

        // Load the main config, the file with all of our settings.
        printBasicMessage("--> §aLoading and validating Pixelmon Broadcasts settings...");
        ConfigMethods.loadConfig("settings");

        // Also load the broadcasts and translations. Store the whole thing in memory, so we can read whenever.
        printBasicMessage("--> §aLoading broadcast messages and translations...");
        try { messageConfig = PixelmonBroadcasts.messageLoader.load(); }
        catch (final IOException ignored) {}
        try { broadcastConfig = PixelmonBroadcasts.broadcastLoader.load(); }
        catch (final IOException ignored) {}

        // Register listeners with Pixelmon.
        printBasicMessage("--> §aRegistering listeners with Pixelmon...");
        Pixelmon.EVENT_BUS.register(new BattleEndListener());
        Pixelmon.EVENT_BUS.register(new BattleStartListener());
        Pixelmon.EVENT_BUS.register(new CatchListener());
        Pixelmon.EVENT_BUS.register(new HatchListener());
        Pixelmon.EVENT_BUS.register(new SpawnListener());
        Pixelmon.EVENT_BUS.register(new TradeListener());
        Pixelmon.EVENT_BUS.register(new WildDefeatListener());

        // Check Pixelmon's config and get whether the legendary spawning message is in. Complain if it is.
        printBasicMessage("--> §aChecking Pixelmon config for legendary message settings...");
        Boolean configStatus = toBooleanObject(
                PixelmonConfig.getConfig().getNode("Spawning", "displayLegendaryGlobalMessage").getString());

        // Is the config setting we're reading available?
        if (configStatus != null)
        {
            // Is the setting turned on? Complaining, commence!
            if (configStatus)
            {
                printBasicMessage("    §ePixelmon's \"§6displayLegendaryGlobalMessage§e\" setting is enabled.");
                printBasicMessage("    §eThis setting will now be disabled, as it conflicts with this sidemod.");
                printBasicMessage("    §eIf you remove this mod, revert this in Pixelmon's config!");

                PixelmonConfig.getConfig().getNode("Spawning", "displayLegendaryGlobalMessage").setValue(false);
                PixelmonConfig.saveConfig();
            }
        }

        // Register commands.
        printBasicMessage("--> §aRegistering main command and subcommands...");

        // The method used returns "false" if it fails, and prints some failure-specific errors.
        if (ConfigMethods.registerCommands())
        {
            if (commandAlias != null && !commandAlias.equals("pixelmonbroadcasts"))
                printBasicMessage("    §aRegistered main command as §2/pixelmonbroadcasts§a, alias §2/" + commandAlias);
            else
                printBasicMessage("    §aRegistered main command as §2/pixelmonbroadcasts§a, no alias.");

            printBasicMessage("--> §aPre-init completed. All systems nominal.");
        }

        printBasicMessage("===========================================================================");
        printBasicMessage("");
    }
}