package rs.expand.pixelmonbroadcasts.enums;

import rs.expand.pixelmonbroadcasts.utilities.PrintingMethods;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

// A big list of all event options (as loaded from settings.conf), broadcast/permission keys and logging settings.
public interface EventData
{
    boolean presentTense();
    char color(); // Dark Blue (1), Dark Aqua (3), Dark Red (4) and Dark Gray (8) are still available. Forget black/white.
    String key();
    String options();
    String[] flags();
    String[] messages();

    default boolean checkSettingsOrError(final String... nodes)
    {
        logger.error("Entering valid options check. If we get an error, options are null?");

        if (this.options() == null)
        {
            if (nodes.length == 1)
                PrintingMethods.printOptionsNodeError(nodes[0]);
            else // TODO: TEST
                PrintingMethods.printOptionsNodeError(Stream.of(nodes).collect(Collectors.toList()));

            return false;
        }

        return true;
    }

    enum Blackouts implements EventData
    {
        // Blackouts.
        NORMAL(null, "blackout.normal", "showNormalBlackout"),
        SHINY(null, "blackout.shiny", "showShinyBlackout"),
        LEGENDARY(null, "blackout.legendary", "showLegendaryBlackout"),
        SHINY_LEGENDARY(null, "blackout.shinylegendary", "showLegendaryBlackout", "showShinyBlackout"),
        ULTRA_BEAST(null, "blackout.ultrabeast", "showUltraBeastBlackout"),
        SHINY_ULTRA_BEAST(null, "blackout.shinyultrabeast", "showUltraBeastBlackout", "showShinyBlackout"),
        BOSS(null, "blackout.boss", "showBossBlackout"),
        TRAINER(null, "blackout.trainer", "showTrainerBlackout"),
        BOSS_TRAINER(null, "blackout.bosstrainer", "showBossTrainerBlackout");

        // Set up some variables for accessing the Enum's data through.
        private String key;
        public String options;
        private String[] flags;

        // Point to where we're grabbing from.
        Blackouts(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return false; }
        @Override public char color() { return '6'; } // Gold. (orange)
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }
        @Override public String[] messages() { return new String[] {" was knocked out by a "}; }
    }

    enum Catches implements EventData
    {
        // Catches.
        NORMAL(null, "catch.normal", "showNormalCatch"),
        SHINY(null, "catch.shiny", "showShinyCatch"),
        LEGENDARY(null, "catch.legendary", "showLegendaryCatch"),
        SHINY_LEGENDARY(null, "catch.shinylegendary", "showLegendaryCatch", "showShinyCatch"),
        ULTRA_BEAST(null, "catch.ultrabeast", "showUltraBeastCatch"),
        SHINY_ULTRA_BEAST(null, "catch.shinyultrabeast", "showUltraBeastCatch", "showShinyCatch");

        // Set up some variables for accessing the Enum's data through.
        private String key;
        public String options;
        private String[] flags;

        // Point to where we're grabbing from.
        Catches(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return true; }
        @Override public char color() { return 'b'; } // Aqua. (light blue)
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }
        @Override public String[] messages() { return new String[] {" caught a "}; }
    }

    enum Challenges implements EventData
    {
        // Challenges.
        SHINY(null, null, "challenge.shiny", "showShinyChallenge"),
        LEGENDARY(null, null, "challenge.legendary", "showLegendaryChallenge"),
        SHINY_LEGENDARY(null, null, "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge"),
        ULTRA_BEAST(null, null, "challenge.ultrabeast", "showUltraBeastChallenge"),
        SHINY_ULTRA_BEAST(null, null, "challenge.shinyultrabeast", "showUltraBeastChallenge", "showShinyChallenge"),
        BOSS(null, null, "challenge.boss", "showBossChallenge"),
        TRAINER(null, null, "challenge.trainer", "showTrainerChallenge"),
        BOSS_TRAINER(null, null, "challenge.bosstrainer", "showBossTrainerChallenge"),
        PVP(null, new String[] {" started battling player "}, "challenge.pvp", "showPVPChallenge");

        // Set up some variables for accessing the Enum's data through.
        private String key;
        public String options;
        private String[] messages, flags;

        // Point to where we're grabbing from.
        Challenges(final String options, final String[] messages, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.messages = messages;
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return true; }
        @Override public char color() { return '9'; } // Blue.
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }

        // Return a default message unless we're on a challenge that should use special logging.
        @Override public String[] messages() { return messages == null ? new String[] {" started fighting a "} : messages; }
    }

    enum Forfeits implements EventData
    {
        // Forfeits.
        SHINY(null, "forfeit.shiny", "showShinyForfeit"),
        LEGENDARY(null, "forfeit.legendary", "showLegendaryForfeit"),
        SHINY_LEGENDARY(null, "forfeit.shinylegendary", "showLegendaryForfeit", "showShinyForfeit"),
        ULTRA_BEAST(null, "forfeit.ultrabeast", "showUltraBeastForfeit"),
        SHINY_ULTRA_BEAST(null, "forfeit.shinyultrabeast", "showUltraBeastForfeit", "showShinyForfeit"),
        BOSS(null, "forfeit.boss", "showBossForfeit"),
        TRAINER(null, "forfeit.trainer", "showTrainerForfeit"),
        BOSS_TRAINER(null, "forfeit.bosstrainer", "showBossTrainerForfeit");

        // Set up some variables for accessing the Enum's data through.
        private String key;
        public String options;
        private String[] flags;

        // Point to where we're grabbing from.
        Forfeits(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return false; }
        @Override public char color() { return 'e'; } // Yellow
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }
        @Override public String[] messages() { return new String[] {" fled from a "}; }
    }

    enum Spawns implements EventData
    {
        // Spawns.
        SHINY(null, "spawn.shiny", "showShinySpawn"),
        LEGENDARY(null, "spawn.legendary", "showLegendarySpawn"),
        SHINY_LEGENDARY(null, "spawn.shinylegendary", "showLegendarySpawn", "showShinySpawn"),
        ULTRA_BEAST(null, "spawn.ultrabeast", "showUltraBeastSpawn"),
        SHINY_ULTRA_BEAST(null, "spawn.shinyultrabeast", "showUltraBeastSpawn", "showShinySpawn"),
        WORMHOLE(null, "spawn.wormhole", "showWormholeSpawn"),
        BOSS(null, "spawn.boss", "showBossSpawn");

        // Set up some variables for accessing the Enum's data through.
        private String key;
        public String options;
        private String[] flags;

        // Point to where we're grabbing from.
        Spawns(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return false; }
        @Override public char color() { return 'a'; } // Green. (light green)
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }
        @Override public String[] messages() { return null; } // Message has its own logic due to weird word ordering.
    }

    enum Victories implements EventData
    {
        // Victories.
        SHINY(null, null, "victory.shiny", "showShinyVictory"),
        LEGENDARY(null, null, "victory.legendary", "showLegendaryVictory"),
        SHINY_LEGENDARY(null, null, "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory"),
        ULTRA_BEAST(null, null, "victory.ultrabeast", "showUltraBeastVictory"),
        SHINY_ULTRA_BEAST(null, null, "victory.shinyultrabeast", "showUltraBeastVictory", "showShinyVictory"),
        BOSS(null, null, "victory.boss", "showBossVictory"),
        TRAINER(null, null, "victory.trainer", "showTrainerVictory"),
        BOSS_TRAINER(null, null, "victory.bosstrainer", "showBossTrainerVictory"),
        PVP(null, new String[] {" defeated player "}, "victory.pvp", "showPVPVictory");

        // Set up some variables for accessing the Enum's data through.
        private String key;
        public String options;
        private String[] messages, flags;

        // Point to where we're grabbing from.
        Victories(final String options, final String[] messages, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.messages = messages;
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return false; }
        @Override public char color() { return '2'; } // Dark Green.
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }

        // Return a default message unless we're on a victory that should use special logging.
        @Override public String[] messages() { return messages == null ? new String[] {" defeated a "} : messages; }
    }

    enum Hatches implements EventData
    {
        // Hatches.
        NORMAL(null, "hatch.normal", "showNormalHatch"),
        SHINY(null, "hatch.shiny", "showShinyHatch"),
        LEGENDARY(null, "hatch.legendary", "showLegendaryHatch"),
        SHINY_LEGENDARY(null, "hatch.shinylegendary", "showLegendaryHatch", "showShinyHatch"),
        ULTRA_BEAST(null, "hatch.ultrabeast", "showUltraBeastHatch"),
        SHINY_ULTRA_BEAST(null, "hatch.shinyultrabeast", "showUltraBeastHatch", "showShinyHatch");

        // Set up some variables for accessing the Enum's data through.
        private String key;
        public String options;
        private String[] flags;

        // Point to where we're grabbing from.
        Hatches(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return true; }
        @Override public char color() { return 'd'; } // Light Purple. (pink)
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }
        @Override public String[] messages() { return new String[] {" hatched a "}; }
    }

    enum Draws implements EventData
    {
        // Draws. Currently just PvP, might get more eventually.
        PVP(null);

        // Set up some variables for accessing the Enum's data through.
        public String options;

        // Point to where we're grabbing from.
        Draws(final String options)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
        }

        // Expose values to anything accessing us through the main interface. All set in advance for now.
        @Override public boolean presentTense() { return false; }
        @Override public char color() { return '7'; } // Gray. (light gray)
        @Override public String key() { return "draw.pvp"; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return new String[] {"showPVPDraw"}; }
        @Override public String[] messages() { return new String[] {"'s battle with ", " ended in a draw"}; }
    }

    enum Others implements EventData
    {
        // Miscellaneous events. Trade has its own message logic to avoid needing to pass in a huge list of parameters.
        TRADE(null, true, null, '5', "trade.normal", "showTrade"), // Dark Purple.
        FAINT(null, false, new String[]{}, 'c', "faint.normal", "showFaint"); // Red.

        // Set up some variables for accessing the Enum's data through.
        private boolean presentTense;
        private char color;
        private String key;
        public String options;
        private String[] flags, messages;

        // Point to where we're grabbing from.
        Others(final String options, final boolean presentTense, final String[] messages, final char color,
               final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.presentTense = presentTense;
            this.messages = messages;
            this.color = color;
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return this.presentTense; }
        @Override public char color() { return this.color; }
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }
        @Override public String[] messages() { return this.messages; }
    }
}
