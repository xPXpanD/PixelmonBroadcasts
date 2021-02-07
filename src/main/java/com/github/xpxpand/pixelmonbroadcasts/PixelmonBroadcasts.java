// Written for Pixelmon Reforged. Running this on Gens is unsupported and ill-advised, just like Gens itself.
package com.github.xpxpand.pixelmonbroadcasts;

import com.github.xpxpand.pixelmonbroadcasts.commands.HubCommand;
import com.github.xpxpand.pixelmonbroadcasts.commands.Reload;
import com.github.xpxpand.pixelmonbroadcasts.commands.Teleport;
import com.github.xpxpand.pixelmonbroadcasts.commands.Toggle;
import com.github.xpxpand.pixelmonbroadcasts.listeners.*;
import com.github.xpxpand.pixelmonbroadcasts.utilities.ConfigMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PlayerMethods;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.overlay.notice.NoticeOverlay;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import info.pixelmon.repack.ninja.leaping.configurate.commented.CommentedConfigurationNode;
import info.pixelmon.repack.ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import info.pixelmon.repack.ninja.leaping.configurate.loader.ConfigurationLoader;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*                                                              *\
       THE WHO-KNOWS-WHEN LIST OF POTENTIALLY AWESOME IDEAS
    TODO: Add new TODOs here. Cross off TODOs if they're done.
      NOTE: Stuff that's here will not necessarily get done.
\*                                                              */

// TODO: Get rid of the shinylegendary/shinyultrabeast key checks, somehow.
// TODO: Maybe play a cry when something spawns. Play with pitch for a distance effect?
// TODO: Maybe move some of the less unique logging info out of EventData and into the listener classes via constants.
// TODO: Implement logging to a custom log file with the right option passed.
// TODO: Ideas for new events: HA, successful breed, event spawns, maaaaybe level.
// TODO: Listen to commands being used, fire the right event if we have a successful hatch/spawn/etcetera.
// TODO: Make a more comprehensive summon check.
// TODO: Custom event setups. Oh boy. Separate file that includes broadcasts and settings?
// TODO: Stop using deprecated API once newer versions are more common.
// TODO: Maybe have Toggle ask for a category first. Would tidy things up with how many events we have.
// TODO: See what we can do with other forms. (Altered ones come to mind)
// TODO: See what we can do with custom textures.
// TODO: Cache calculated results instead of pulling and processing them from the cached config every time.
// TODO: Possibly start gathering people's lang setups once 1.0 rolls around. Mostly for single player.
// FIXME: Biome names are always English. Maybe add to the lang, and use English biome names as keys.
// FIXME: Similarly, Pokémon names seem to be English as well.
// FIXME: Challenges and forfeits can be used to spam servers. Add a persistent tag to avoid repeats?
// FIXME: Formatting codes show up in the client logs. See if we can do this more cleanly.

@Mod
        (
                modid = PixelmonBroadcasts.MOD_ID,
                name = PixelmonBroadcasts.MOD_NAME,
                version = PixelmonBroadcasts.VERSION,
                dependencies = "required-after:pixelmon",
                acceptableRemoteVersions = "*"

                /*                                                                                                    *\
                    Loosely inspired by PixelAnnouncer, which I totally forgot existed up until I wanted to release.
                    After people reminded me that PA was a thing, I ended up making PBR a full-on replacement for it.

                    Thanks for the go-ahead on that, Proxying! Let's make this count.
                    Also, thanks to happyzleaf for the basic PixelmonOverlay integration.                   -- XpanD
                \*                                                                                                    */
        )

public class PixelmonBroadcasts
{
    // Set up base mod info.
    static final String MOD_ID = "pixelmonbroadcasts";
    static final String MOD_NAME = "PixelmonBroadcasts";
    static final String VERSION = "0.6-universal-test1";

    // Set up an internal variable so we can see if we loaded correctly. Slightly dirty, but it works.
    private boolean loadedCorrectly = false;

    // Set up a logger for logging stuff. Yup.
    public static final Logger logger = LogManager.getLogger("Broadcasts");

    // Start setting up some basic variables that we'll fill in remotely when we read the config.
    public static Integer configVersion;
    public static String commandAlias;
    public static Boolean showAbilities;

    // Set up a hashmap for tracking shown Pixelmon notices. Allows us to kill them after a fixed amount of time.
    public static final HashMap<UUID, Long> noticeExpiryMap = new HashMap<>();

    // Create and set up config paths, and grab an OS-specific file path separator. This will usually be a forward slash.
    private static final String fileSystemSeparator = FileSystems.getDefault().getSeparator();
    public static final String configPathAsString = "config" + fileSystemSeparator + "PixelmonBroadcasts" + fileSystemSeparator;
    public static final Path settingsPath = Paths.get(configPathAsString, "settings.conf");
    public static final Path broadcastsPath = Paths.get(configPathAsString, "broadcasts.conf");
    public static final Path messagesPath = Paths.get(configPathAsString, "messages.conf");

    // Set up configuration loaders that we can call on later.
    public static final ConfigurationLoader<CommentedConfigurationNode> broadcastsLoader =
            HoconConfigurationLoader.builder().setPath(broadcastsPath).build();
    public static final ConfigurationLoader<CommentedConfigurationNode> messagesLoader =
            HoconConfigurationLoader.builder().setPath(messagesPath).build();
    public static ConfigurationLoader<CommentedConfigurationNode> settingsLoader =
            HoconConfigurationLoader.builder().setPath(settingsPath).build();

    // Set up a few places for us to load all our settings/messages/broadcasts into later.
    public static CommentedConfigurationNode broadcastsConfig = null;
    public static CommentedConfigurationNode messagesConfig = null;
    public static CommentedConfigurationNode settingsConfig = null;

    @Mod.EventHandler
    public void onFMLPreInitEvent(final FMLPreInitializationEvent event)
    {
        // Load up all the configs and figure out the info alias. Start printing. Methods may insert errors as they go.
        logger.info("");
        logger.info("§f================== P I X E L M O N  B R O A D C A S T S ==================");

        // Load up all configuration files. Creates new configs/folders if necessary. Commit settings to memory.
        // Store whether we actually loaded things up correctly in this bool, which we can check again later.
        loadedCorrectly = ConfigMethods.tryCreateAndLoadConfigs();

        // If we got a good result from the config loading method, proceed to initializing more stuff.
        if (loadedCorrectly)
        {
            // Register relevant listeners with Pixelmon.
            logger.info("§f--> §aRegistering listeners with Pixelmon...");
            Pixelmon.EVENT_BUS.register(new BattleEndListener());
            Pixelmon.EVENT_BUS.register(new BattleStartListener());
            //Pixelmon.EVENT_BUS.register(new BirdSpawnListener());
            Pixelmon.EVENT_BUS.register(new CatchListener());
            Pixelmon.EVENT_BUS.register(new EvolutionListener());
            Pixelmon.EVENT_BUS.register(new HatchListener());
            Pixelmon.EVENT_BUS.register(new PokemonFaintListener());
            Pixelmon.EVENT_BUS.register(new SpawnListener());
            Pixelmon.EVENT_BUS.register(new TradeListener());

            // Register relevant listeners with Forge.
            logger.info("§f--> §aRegistering listeners with Forge...");
            MinecraftForge.EVENT_BUS.register(new DeathCloneListener());

            // Register the main command and alias.
            logger.info("§f--> §aPre-init completed.");
        }
        else
            logger.info("§f--> §cLoad aborted due to critical errors. Mod is not running!");

        // We're done, one way or another. Add a footer, and a space to avoid clutter with other marginal'd mods.
        logger.info("§f==========================================================================");
        logger.info("");
    }

    @Mod.EventHandler
    public void onServerStartingEvent(FMLServerStartingEvent event)
    {
        if (loadedCorrectly)
        {
            logger.info("");
            logger.info("§f================== P I X E L M O N  B R O A D C A S T S ==================");
            logger.info("§f--> §aSetting up a timer for Pixelmon's noticeboard...");

            // Set up a repeating task. It checks if any players need their notices wiped. (happens every 10-12s)
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->
            {
                // Grab current time in milliseconds.
                final long currentTime = System.currentTimeMillis();

                // Iterate through all online players.
                PlayerMethods.getOnlinePlayers().forEach(player ->
                {
                    // Check if our player is using any noticeboard-enabled broadcasts.
                    if (noticeExpiryMap.containsKey(player.getUniqueID()))
                    {
                        // Are we 10 or more seconds ahead of the last time a notice was added? Hide it!
                        if (currentTime - noticeExpiryMap.get(player.getUniqueID()) >= 10000)
                        {
                            // Hide the overlay for the targeted player.
                            NoticeOverlay.hide((EntityPlayerMP) player);

                            // Remove the player's UUID from the map. This prevents needless clears every iteration.
                            noticeExpiryMap.remove(player.getUniqueID());
                        }
                    }
                });
            }, 0, 2, TimeUnit.SECONDS);

            // Register commands.
            logger.info("§f--> §aRegistering commands with Forge...");
            HubCommand hubCommand = new HubCommand();
            hubCommand.addSubcommand(new Reload());
            hubCommand.addSubcommand(new Teleport());
            hubCommand.addSubcommand(new Toggle());
            event.registerServerCommand(hubCommand);

            // Show that we're ready to go!
            logger.info("§f--> §aInit completed. All systems nominal.");

            // Check Pixelmon's config and get whether the legendary spawning message is enabled there.
            final String statusNode = PixelmonConfig.getConfig().getNode("Spawning", "displayLegendaryGlobalMessage").getString();
            final Boolean configLoadedAndReady = BooleanUtils.toBooleanObject(statusNode);

            // Is the config setting we're reading available, /and/ is the setting turned on? Append complaints!
            if (configLoadedAndReady != null && configLoadedAndReady)
            {
                // Complaining, commence.
                logger.info("");
                logger.info("");
                logger.info("§f--> §ePixelmon's \"§6displayLegendaryGlobalMessage§e\" setting is enabled.");
                logger.info("§e    This setting conflicts with Broadcasts, and will now be turned off.");
                logger.info("§e    If you remove the mod at any point, be sure to turn this back on!");

                // Flip the setting in Pixelmon's config and save changes.
                PixelmonConfig.doLegendaryEvent = false;
                PixelmonConfig.saveConfig();
            }

            if (configVersion != null)
            {
                if (configVersion < 50)
                {
                    // More complaining, commence.
                    logger.info("");
                    logger.info("");
                    logger.info("§f--> §eWelcome to the 0.6 update! To finish updating, do the following:");
                    logger.info("§6    1. §eMove any customized PBR configs somewhere safe, if present.");
                    logger.info("§6    2. §eDelete the \"PixelmonBroadcasts\" config folder.");
                    logger.info("§6    3. §eUse \"/pixelmonbroadcasts reload\". This creates new files.");
                    logger.info("§6    4. §eCopy back any old tweaks carefully -- many things changed!");
                    logger.info("§6       --- OR ---");
                    logger.info("§6    4. §eCopy new boss lines into the old files, and move them back in.");
                    logger.info("§6    5. §eIf using an old settings file, set \"configVersion\" to \"50\".");
                    logger.info("");
                    logger.info("§e    Report any bugs here: https://github.com/xPXpanD/PixelmonBroadcasts");
                }
                else if (configVersion < 60)
                {
                    logger.info("");
                    logger.info("");
                    logger.info("§f--> §eWelcome to the 0.6 update! To finish updating, do the following:");
                    logger.info("§6    1. §eMove PBR's message config somewhere safe, if present.");
                    logger.info("§6    2. §eEnsure there's no longer a \"messages.conf\" in the config folder.");
                    logger.info("§6    3. §eUse \"/pixelmonbroadcasts reload\". This creates a new file.");
                    logger.info("§6    4. §eCopy back any old tweaks carefully.");
                    logger.info("§6       --- OR ---");
                    logger.info("§6    4. §eCopy new teleport lines into the old file, and move it back in.");
                    logger.info("§e    If using an old settings file, set \"configVersion\" to \"60\"!");
                    logger.info("");
                    logger.info("§e    Report any bugs here: https://github.com/xPXpanD/PixelmonBroadcasts");
                }

                // TODO: Get this working without it squashing the whole config down.
                /*// Set the config's version value to whatever version we're on right now.
                try
                {
                    settingsConfig.getNode("configVersion").setValue(40);
                    settingsLoader.save(settingsLoader.load());
                }
                catch (IOException F)
                {
                    logger.error("Something broke while updating config version! Please report. Stack trace:");
                    F.printStackTrace();
                }*/
            }

            logger.info("§f==========================================================================");
            logger.info("");
        }
    }
}
