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
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;

// Local imports.
import org.spongepowered.api.text.Text;
import rs.expand.pixelmonbroadcasts.commands.*;
import rs.expand.pixelmonbroadcasts.listeners.*;
import rs.expand.pixelmonbroadcasts.utilities.ConfigMethods;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

/*                                                              *\
       THE WHO-KNOWS-WHEN LIST OF POTENTIALLY AWESOME IDEAS
    TODO: Add new TODOs here. Cross off TODOs if they're done.
      NOTE: Stuff that's here will not necessarily get made.
\*                                                              */

// TODO: Title-style display option?
// TODO: Add versioning logic when it becomes necessary.
// TODO: Check if a HA spawn check is possible.
// FIXME: Make the console message code less ugly. Works fine, but not the cleanest way of doing it.
// TODO: Actually implement logging settings.
// TODO: Test:-- legendary spawn/catch/defeat, shiny catch/defeat, boss defeat
// TODO: PVP victories? Blackouts in PVP and PVE?

@Plugin
(
        id = "pixelmonbroadcasts",
        name = "PixelmonBroadcasts",
        version = "0.1",
        dependencies = @Dependency(id = "pixelmon"),
        description = "Adds fully custom legendary-like messages for tons of events.",
        authors = "XpanD"

        /*                                                                                                   *\
            Loosely inspired by PixelAnnouncer, which I totally forgot existed up until I wanted to release.
            This whole thing started as a separate project just for legendary spawn/catch/defeat messages.
            After people reminded me that PA was a thing, I ended up making a full-on replacement for it.

            Thanks for the go-ahead on that whole thing, Proxying!
        \*                                                                                                   */
)

// Note: printBasicMessage is a static import for a method from PrintingMethods, for convenience. So are the listeners.
public class PixelmonBroadcasts
{
    // Start setting up some basic variables that we'll fill in remotely when we read the config.
    public static Integer configVersion;
    public static String commandAlias;

    // Set up logging settings.
    public static Boolean logLegendarySpawns;
    public static Boolean logLegendaryCatches;
    public static Boolean logLegendaryDefeats;
    public static Boolean logShinySpawns;
    public static Boolean logShinyCatches;
    public static Boolean logShinyDefeats;
    public static Boolean logBossSpawns;
    public static Boolean logBossDefeats;
    public static Boolean logHatches;
    public static Boolean logTrades;

    // Set up broadcast settings.
    public static Boolean showLegendarySpawnMessage;
    public static Boolean showLegendaryCatchMessage;
    public static Boolean showLegendaryDefeatMessage;
    public static Boolean showShinySpawnMessage;
    public static Boolean showShinyCatchMessage;
    public static Boolean showShinyDefeatMessage;
    public static Boolean showBossSpawnMessage;
    public static Boolean showBossDefeatMessage;
    public static Boolean showHatchMessage;
    public static Boolean showTradeMessage;

    // Set up normal message Strings.
    public static String legendarySpawnMessage;
    public static String legendaryCatchMessage;
    public static String legendaryDefeatMessage;
    public static String shinySpawnMessage;
    public static String shinyCatchMessage;
    public static String shinyDefeatMessage;
    public static String bossSpawnMessage;
    public static String bossDefeatMessage;
    public static String hatchMessage;
    public static String tradeMessage;

    // Set up special legendary + shiny message Strings.
    public static String legendaryShinySpawnMessage;
    public static String legendaryShinyCatchMessage;
    public static String legendaryShinyDefeatMessage;

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

        // Returns true if the load was a success.
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
                    PixelmonConfig.getConfig().getNode("Spawning", "displayLegendaryGlobalMessage").setValue(false);
                    PixelmonConfig.saveConfig();

                    printBasicMessage("    §ePixelmon's \"§6displayLegendaryGlobalMessage§e\" setting is enabled.");
                    printBasicMessage("    §eThis setting will now be disabled, as it conflicts with this sidemod.");
                    printBasicMessage("    §eIf you remove this mod, make sure you check Pixelmon's config!");
                }
            }

            // Register commands.
            printBasicMessage("--> §aRegistering §2/pixelmonbroadcasts§a command and subcommands...");

            // The method used returns "false" if it fails, and prints some failure-specific errors.
            if (ConfigMethods.registerCommands())
            {
                if (commandAlias == null)
                    printBasicMessage("    §cCould not read config node \"§4commandAlias§c\". Alias support disabled.");
                else if (!commandAlias.equals("pixelmonbroadcasts"))
                    printBasicMessage("    §2Created alias: §a/" + commandAlias);

                printBasicMessage("--> §aPre-init completed. All systems nominal.");
            }
        }
        else
            printBasicMessage("    §cEncountered a critical error, aborting... If this is a bug, please report.");

        printBasicMessage("===========================================================================");
        printBasicMessage("");
    }
}