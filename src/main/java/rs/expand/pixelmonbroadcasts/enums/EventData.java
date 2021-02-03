package rs.expand.pixelmonbroadcasts.enums;

// A big list of all event options (as loaded from settings.conf), broadcast/permission keys and logging settings.
public interface EventData
{
    boolean presentTense();
    char color(); // Dark Blue (1), Dark Aqua (3), Dark Red (4) and Dark Gray (8) are still available. Forget black/white.
    String key();
    String options();
    String[] flags();
    String[] messages();

    enum Blackouts implements EventData
    {
        // Blackouts
        NORMAL("blackout.normal", "showNormalBlackout"),
        SHINY("blackout.shiny", "showShinyBlackout"),
        LEGENDARY("blackout.legendary", "showLegendaryBlackout"),
        SHINY_LEGENDARY("blackout.shinylegendary", "showLegendaryBlackout", "showShinyBlackout"),
        ULTRA_BEAST("blackout.ultrabeast", "showUltraBeastBlackout"),
        SHINY_ULTRA_BEAST("blackout.shinyultrabeast", "showUltraBeastBlackout", "showShinyBlackout"),
        UNCOMMON_BOSS("blackout.uncommonboss", "showUncommonBossBlackout"),
        RARE_BOSS("blackout.rareboss", "showRareBossBlackout"),
        LEGENDARY_BOSS("blackout.legendaryboss", "showLegendaryBossBlackout"),
        ULTIMATE_BOSS("blackout.ultimateboss", "showUltimateBossBlackout"),
        TRAINER("blackout.trainer", "showTrainerBlackout"),
        BOSS_TRAINER("blackout.bosstrainer", "showBossTrainerBlackout");

        // Set up some variables for accessing the Enum's data through.
        private final String key;
        public String options;
        private final String[] flags;

        // Point to where we're grabbing from.
        Blackouts(final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
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
        NORMAL("catch.normal", "showNormalCatch"),
        SHINY("catch.shiny", "showShinyCatch"),
        LEGENDARY("catch.legendary", "showLegendaryCatch"),
        SHINY_LEGENDARY("catch.shinylegendary", "showLegendaryCatch", "showShinyCatch"),
        ULTRA_BEAST("catch.ultrabeast", "showUltraBeastCatch"),
        SHINY_ULTRA_BEAST("catch.shinyultrabeast", "showUltraBeastCatch", "showShinyCatch");

        // Set up some variables for accessing the Enum's data through.
        private final String key;
        public String options;
        private final String[] flags;

        // Point to where we're grabbing from.
        Catches(final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
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
        SHINY(null, "challenge.shiny", "showShinyChallenge"),
        LEGENDARY(null, "challenge.legendary", "showLegendaryChallenge"),
        SHINY_LEGENDARY(null, "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge"),
        ULTRA_BEAST(null, "challenge.ultrabeast", "showUltraBeastChallenge"),
        SHINY_ULTRA_BEAST(null, "challenge.shinyultrabeast", "showUltraBeastChallenge", "showShinyChallenge"),
        UNCOMMON_BOSS(null, "challenge.uncommonboss", "showUncommonBossChallenge"),
        RARE_BOSS(null, "challenge.rareboss", "showRareBossChallenge"),
        LEGENDARY_BOSS(null, "challenge.legendaryboss", "showLegendaryBossChallenge"),
        ULTIMATE_BOSS(null, "challenge.ultimateboss", "showUltimateBossChallenge"),
        TRAINER(null, "challenge.trainer", "showTrainerChallenge"),
        BOSS_TRAINER(null, "challenge.bosstrainer", "showBossTrainerChallenge"),
        PVP(new String[] {" started battling player "}, "challenge.pvp", "showPVPChallenge");

        // Set up some variables for accessing the Enum's data through.
        private final String key;
        public String options;
        private final String[] messages, flags;

        // Point to where we're grabbing from.
        Challenges(final String[] messages, final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
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
        SHINY("forfeit.shiny", "showShinyForfeit"),
        LEGENDARY("forfeit.legendary", "showLegendaryForfeit"),
        SHINY_LEGENDARY("forfeit.shinylegendary", "showLegendaryForfeit", "showShinyForfeit"),
        ULTRA_BEAST("forfeit.ultrabeast", "showUltraBeastForfeit"),
        SHINY_ULTRA_BEAST("forfeit.shinyultrabeast", "showUltraBeastForfeit", "showShinyForfeit"),
        BOSS("forfeit.boss", "showBossForfeit"),
        TRAINER("forfeit.trainer", "showTrainerForfeit"),
        BOSS_TRAINER("forfeit.bosstrainer", "showBossTrainerForfeit");

        // Set up some variables for accessing the Enum's data through.
        private final String key;
        public String options;
        private final String[] flags;

        // Point to where we're grabbing from.
        Forfeits(final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
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
        SHINY("spawn.shiny", "showShinySpawn"),
        LEGENDARY("spawn.legendary", "showLegendarySpawn"),
        SHINY_LEGENDARY("spawn.shinylegendary", "showLegendarySpawn", "showShinySpawn"),
        ULTRA_BEAST("spawn.ultrabeast", "showUltraBeastSpawn"),
        SHINY_ULTRA_BEAST("spawn.shinyultrabeast", "showUltraBeastSpawn", "showShinySpawn"),
        WORMHOLE("spawn.wormhole", "showWormholeSpawn"),
        UNCOMMON_BOSS("spawn.uncommonboss", "showUncommonBossSpawn"),
        RARE_BOSS("spawn.rareboss", "showRareBossSpawn"),
        LEGENDARY_BOSS("spawn.legendaryboss", "showLegendaryBossSpawn"),
        ULTIMATE_BOSS("spawn.ultimateboss", "showUltimateBossSpawn");

        // Set up some variables for accessing the Enum's data through.
        private final String key;
        public String options;
        private final String[] flags;

        // Point to where we're grabbing from.
        Spawns(final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Expose values to anything accessing us through the main interface.
        @Override public boolean presentTense() { return true; }
        @Override public char color() { return 'a'; } // Green. (light green)
        @Override public String key() { return this.key; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return this.flags; }
        @Override public String[] messages() { return null; } // Message has its own logic due to weird word ordering.
    }

    enum Victories implements EventData
    {
        // Victories.
        SHINY(null, "victory.shiny", "showShinyVictory"),
        LEGENDARY(null, "victory.legendary", "showLegendaryVictory"),
        SHINY_LEGENDARY(null, "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory"),
        ULTRA_BEAST(null, "victory.ultrabeast", "showUltraBeastVictory"),
        SHINY_ULTRA_BEAST(null, "victory.shinyultrabeast", "showUltraBeastVictory", "showShinyVictory"),
        UNCOMMON_BOSS(null, "victory.uncommonboss", "showUncommonBossVictory"),
        RARE_BOSS(null, "victory.rareboss", "showRareBossVictory"),
        LEGENDARY_BOSS(null, "victory.legendaryboss", "showLegendaryBossVictory"),
        ULTIMATE_BOSS(null, "victory.ultimateboss", "showUltimateBossVictory"),
        TRAINER(null, "victory.trainer", "showTrainerVictory"),
        BOSS_TRAINER(null, "victory.bosstrainer", "showBossTrainerVictory"),
        PVP(new String[] {" defeated player "}, "victory.pvp", "showPVPVictory");

        // Set up some variables for accessing the Enum's data through.
        private final String key;
        public String options;
        private final String[] messages, flags;

        // Point to where we're grabbing from.
        Victories(final String[] messages, final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
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

    enum Draws implements EventData
    {
        // Draws. Currently just PvP, might get more eventually.
        PVP();

        // Set up some variables for accessing the Enum's data through.
        public String options;

        // Point to where we're grabbing from.
        Draws()
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
        }

        // Expose values to anything accessing us through the main interface. All set in advance for now.
        @Override public boolean presentTense() { return false; }
        @Override public char color() { return '7'; } // Gray. (light gray)
        @Override public String key() { return "draw.pvp"; }
        @Override public String options() { return this.options; }
        @Override public String[] flags() { return new String[] {"showPVPDraw"}; }
        @Override public String[] messages() { return new String[] {"'s battle with ", " ended in a draw"}; }
    }

    enum Hatches implements EventData
    {
        // Hatches.
        NORMAL("hatch.normal", "showNormalHatch"),
        SHINY("hatch.shiny", "showShinyHatch"),
        LEGENDARY("hatch.legendary", "showLegendaryHatch"),
        SHINY_LEGENDARY("hatch.shinylegendary", "showLegendaryHatch", "showShinyHatch"),
        ULTRA_BEAST("hatch.ultrabeast", "showUltraBeastHatch"),
        SHINY_ULTRA_BEAST("hatch.shinyultrabeast", "showUltraBeastHatch", "showShinyHatch");

        // Set up some variables for accessing the Enum's data through.
        private final String key;
        public String options;
        private final String[] flags;

        // Point to where we're grabbing from.
        Hatches(final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
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

    enum Others implements EventData
    {
        // Miscellaneous events. Trade has its own message logic to avoid needing to pass in a huge list of parameters.
        EVOLVE(true, null, '3', "evolve", "showEvolve"), // Dark Aqua.
        FAINT(false, new String[] {" lost their "}, 'c', "faint", "showFaint"), // Red.
        //LOOT(null, false, null, '3', "loot", "showLoot"), // Dark Aqua.
        TRADE(true, null, '3', "trade", "showTrade"); // Dark Aqua.

        // Set up some variables for accessing the Enum's data through.
        private final boolean presentTense;
        private final char color;
        private final String key;
        public String options;
        private final String[] flags, messages;

        // Point to where we're grabbing from.
        Others(final boolean presentTense, final String[] messages, final char color,
               final String key, final String... flags)
        {
            this.options = null; // Can change! Null until filled in by a (re-)load.
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
