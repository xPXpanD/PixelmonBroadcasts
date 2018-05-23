package rs.expand.pixelmonbroadcasts;

// Remote imports.
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
// TODO: Add versioning logic when it becomes necessary.
// TODO: Check if a HA spawn check is possible.
// TODO: Test (shiny) legendary spawn messages.
// TODO: PVP victories? Blackouts in PVP and PVE?

@Plugin
(
        id = "pixelmonbroadcasts",
        name = "PixelmonBroadcasts",
        version = "1.0.0 beta",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds fully custom legendary-like messages for tons of events.",
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
    public static String statSeparator;
    public static String statLineStart;

    // Set up logging settings.
    public static boolean logLegendarySpawns;
    public static boolean logLegendaryCatches;
    public static boolean logLegendaryDefeats;
    public static boolean logShinyLegendarySpawns;
    public static boolean logShinyLegendaryCatches;
    public static boolean logShinyLegendaryDefeats;
    public static boolean logShinySpawns;
    public static boolean logShinyCatches;
    public static boolean logShinyDefeats;
    public static boolean logBossSpawns;
    public static boolean logBossDefeats;
    public static boolean logHatches;
    public static boolean logShinyHatches;
    public static boolean logTrades;

    // Set up broadcast settings.
    public static boolean showLegendarySpawns;
    public static boolean showLegendaryCatches;
    public static boolean showLegendaryDefeats;
    public static boolean showShinyLegendarySpawns;
    public static boolean showShinyLegendaryCatches;
    public static boolean showShinyLegendaryDefeats;
    public static boolean showShinySpawns;
    public static boolean showShinyCatches;
    public static boolean showShinyDefeats;
    public static boolean showBossSpawns;
    public static boolean showBossDefeats;
    public static boolean showHatches;
    public static boolean showShinyHatches;
    public static boolean showTrades;

    // Set up hover settings.
    public static boolean hoverLegendarySpawns;
    public static boolean hoverLegendaryCatches;
    public static boolean hoverLegendaryDefeats;
    public static boolean hoverShinyLegendarySpawns;
    public static boolean hoverShinyLegendaryCatches;
    public static boolean hoverShinyLegendaryDefeats;
    public static boolean hoverShinySpawns;
    public static boolean hoverShinyCatches;
    public static boolean hoverShinyDefeats;
    public static boolean hoverBossSpawns;
    public static boolean hoverBossDefeats;
    public static boolean hoverHatches;
    public static boolean hoverShinyHatches;

    // Set up normal message Strings.
    public static String legendarySpawnMessage;
    public static String legendaryCatchMessage;
    public static String legendaryDefeatMessage;
    public static String shinyLegendarySpawnMessage;
    public static String shinyLegendaryCatchMessage;
    public static String shinyLegendaryDefeatMessage;
    public static String shinySpawnMessage;
    public static String shinyCatchMessage;
    public static String shinyDefeatMessage;
    public static String bossSpawnMessage;
    public static String bossDefeatMessage;
    public static String hatchMessage;
    public static String shinyHatchMessage;
    public static String tradeMessage;

    // Set up special combo and legendary+shiny Strings.

    // Create and set up a config path, and grab an OS-specific file path separator. This will usually be a forward slash.
    public static String primaryPath = "config" + FileSystems.getDefault().getSeparator();
    public static Path primaryConfigPath = Paths.get(primaryPath, "PixelmonBroadcasts.conf");
    public static ConfigurationLoader<CommentedConfigurationNode> primaryConfigLoader =
            HoconConfigurationLoader.builder().setPath(primaryConfigPath).build();

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
        printBasicMessage("--> §aLoading and validating Pixelmon Broadcasts config...");

        // Sets up a config if needed, and then loads in settings. Returns true if the load was a success.
        if (ConfigMethods.tryCreateAndLoadConfig())
        {
            // Register listeners with Pixelmon.
            printBasicMessage("--> §aRegistering listeners with Pixelmon...");
            Pixelmon.EVENT_BUS.register(new SpawnListener());
            Pixelmon.EVENT_BUS.register(new CatchListener());
            Pixelmon.EVENT_BUS.register(new DefeatListener());
            Pixelmon.EVENT_BUS.register(new HatchListener());
            Pixelmon.EVENT_BUS.register(new TradeListener());

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
        }
        else
            printBasicMessage("    §cEncountered a critical error, aborting... If this is a bug, please report.");

        printBasicMessage("===========================================================================");
        printBasicMessage("");
    }
}