package rs.expand.pixelmonbroadcasts.enums;

import rs.expand.pixelmonbroadcasts.utilities.PrintingMethods;

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

    default boolean checkSettingsOrError(final String node)
    {
        logger.error("Entering valid options check. If we get an error, options are null?");

        if (this.options() == null)
        {
            PrintingMethods.printOptionsNodeError(node);
            return false;
        }
        else return true;
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
        public String key, options;
        public String[] flags;

        // Point to where we're grabbing from.
        Blackouts(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return false; }
        public char color() { return '6'; } // Gold. (orange)
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return new String[] {" was knocked out by a "}; }
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
        public String key, options;
        public String[] flags;

        // Point to where we're grabbing from.
        Catches(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return true; }
        public char color() { return 'b'; } // Aqua. (light blue)
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return new String[] {" caught a "}; }
    }

    enum Challenges implements EventData
    {
        // Challenges.
        SHINY(null, "challenge.shiny", "showShinyChallenge"),
        LEGENDARY(null, "challenge.legendary", "showLegendaryChallenge"),
        SHINY_LEGENDARY(null, "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge"),
        ULTRA_BEAST(null, "challenge.ultrabeast", "showUltraBeastChallenge"),
        SHINY_ULTRA_BEAST(null, "challenge.shinyultrabeast", "showUltraBeastChallenge", "showShinyChallenge"),
        BOSS(null, "challenge.boss", "showBossChallenge"),
        TRAINER(null, "challenge.trainer", "showTrainerChallenge"),
        BOSS_TRAINER(null, "challenge.bosstrainer", "showBossTrainerChallenge");

        // Set up some variables for accessing the Enum's data through.
        public String key, options;
        public String[] flags;

        // Point to where we're grabbing from.
        Challenges(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return true; }
        public char color() { return '9'; } // Blue.
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return new String[] {" started fighting a "}; }
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
        public String key, options;
        public String[] flags;

        // Point to where we're grabbing from.
        Forfeits(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return false; }
        public char color() { return 'e'; } // Yellow
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return new String[] {" fled from a "}; }
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
        public String key, options;
        public String[] flags;

        // Point to where we're grabbing from.
        Spawns(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return false; }
        public char color() { return 'a'; } // Green. (light green)
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return null; } // Message has its own logic due to weird word ordering.
    }

    enum Victories implements EventData
    {
        // Victories.
        SHINY(null, "victory.shiny", "showShinyVictory"),
        LEGENDARY(null, "victory.legendary", "showLegendaryVictory"),
        SHINY_LEGENDARY(null, "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory"),
        ULTRA_BEAST(null, "victory.ultrabeast", "showUltraBeastVictory"),
        SHINY_ULTRA_BEAST(null, "victory.shinyultrabeast", "showUltraBeastVictory", "showShinyVictory"),
        BOSS(null, "victory.boss", "showBossVictory"),
        TRAINER(null, "victory.trainer", "showTrainerVictory"),
        BOSS_TRAINER(null, "victory.bosstrainer", "showBossTrainerVictory");

        // Set up some variables for accessing the Enum's data through.
        public String key, options;
        public String[] flags;

        // Point to where we're grabbing from.
        Victories(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return false; }
        public char color() { return '2'; } // Dark Green.
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return new String[] {" defeated a "}; }
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
        public String key, options;
        public String[] flags;

        // Point to where we're grabbing from.
        Hatches(final String options, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return true; }
        public char color() { return 'd'; } // Light Purple. (pink)
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return new String[] {" hatched a "}; }
    }

    enum PVP implements EventData
    {
        // PvP stuff. In its own little category due to requiring different messages.
        CHALLENGE(null, true, new String[] {" challenged player ", " to a battle"},
                "challenge.pvp", "showPVPChallenge"),
        DRAW(null, false, new String[] {"'s battle with ", " ended in a draw"},
                "draw.pvp", "showPVPDraw"),
        VICTORY(null, false, new String[] {" defeated player "},
                "victory.pvp", "showPVPVictory");

        // Set up some variables for accessing the Enum's data through.
        public boolean presentTense;
        public String key, options;
        public String[] flags, messages;

        // Point to where we're grabbing from.
        PVP(final String options, final boolean presentTense, final String[] messages, final String key, final String... flags)
        {
            this.options = options; // Can change! Null until filled in by a (re-)load.
            this.presentTense = presentTense;
            this.messages = messages;
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override
        public boolean presentTense() { return this.presentTense; }
        public char color() { return '7'; } // Gray. (light gray)
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return this.messages; }
    }

    enum Others implements EventData
    {
        // Miscellaneous events. Trade has its own message logic to avoid needing to pass in a huge list of parameters.
        TRADE(null, true, null, '5', "trade.normal", "showTrade"), // Dark Purple.
        FAINT(null, false, new String[]{}, 'c', "faint.normal", "showFaint"); // Red.

        // Set up some variables for accessing the Enum's data through.
        public boolean presentTense;
        public char color;
        public String key, options;
        public String[] flags, messages;

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
        @Override
        public boolean presentTense() { return this.presentTense; }
        public char color() { return this.color; }
        public String key() { return this.key; }
        public String options() { return this.options; }
        public String[] flags() { return this.flags; }
        public String[] messages() { return this.messages; }
    }
}
