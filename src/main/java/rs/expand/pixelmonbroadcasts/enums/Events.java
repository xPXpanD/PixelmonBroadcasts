package rs.expand.pixelmonbroadcasts.enums;

// List of all known events and their associated toggles (as loaded from settings.conf) and broadcast/permission keys.
public interface Events
{
    String settings();
    boolean presentTense();
    String key();
    String[] flags();

    enum Blackouts implements Events
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
        public String settings, key;
        public String[] flags;

        // Point to where we're grabbing from.
        Blackouts(final String settings, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return false; } // Blackouts always use past tense.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Catches implements Events
    {
        // Catches.
        NORMAL(null, "catch.normal", "showNormalCatch"),
        SHINY(null, "catch.shiny", "showShinyCatch"),
        LEGENDARY(null, "catch.legendary", "showLegendaryCatch"),
        SHINY_LEGENDARY(null, "catch.shinylegendary", "showLegendaryCatch", "showShinyCatch"),
        ULTRA_BEAST(null, "catch.ultrabeast", "showUltraBeastCatch"),
        SHINY_ULTRA_BEAST(null, "catch.shinyultrabeast", "showUltraBeastCatch", "showShinyCatch");

        // Set up some variables for accessing the Enum's data through.
        public String settings, key;
        public String[] flags;

        // Point to where we're grabbing from.
        Catches(final String settings, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return true; } // Catches always use present tense.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Challenges implements Events
    {
        // Challenges.
        SHINY(null, "challenge.shiny", "showShinyChallenge"),
        LEGENDARY(null, "challenge.legendary", "showLegendaryChallenge"),
        SHINY_LEGENDARY(null, "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge"),
        ULTRA_BEAST(null, "challenge.ultrabeast", "showUltraBeastChallenge"),
        SHINY_ULTRA_BEAST(null, "challenge.shinyultrabeast", "showUltraBeastChallenge", "showShinyChallenge"),
        BOSS(null, "challenge.boss", "showBossChallenge"),
        TRAINER(null, "challenge.trainer", "showTrainerChallenge"),
        BOSS_TRAINER(null, "challenge.bosstrainer", "showBossTrainerChallenge"),
        PVP(null, "challenge.pvp", "showPVPChallenge");

        // Set up some variables for accessing the Enum's data through.
        public String settings, key;
        public String[] flags;

        // Point to where we're grabbing from.
        Challenges(final String settings, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return true; } // Challenges always use present tense.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Forfeits implements Events
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
        public String settings, key;
        public String[] flags;

        // Point to where we're grabbing from.
        Forfeits(final String settings, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return false; } // Forfeits always use past tense.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Spawns implements Events
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
        public String settings, key;
        public String[] flags;

        // Point to where we're grabbing from.
        Spawns(final String settings, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return false; } // Spawns always use past tense.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Victories implements Events
    {
        // Victories.
        SHINY(null, "victory.shiny", "showShinyVictory"),
        LEGENDARY(null, "victory.legendary", "showLegendaryVictory"),
        SHINY_LEGENDARY(null, "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory"),
        ULTRA_BEAST(null, "victory.ultrabeast", "showUltraBeastVictory"),
        SHINY_ULTRA_BEAST(null, "victory.shinyultrabeast", "showUltraBeastVictory", "showShinyVictory"),
        BOSS(null, "victory.boss", "showBossVictory"),
        TRAINER(null, "victory.trainer", "showTrainerVictory"),
        BOSS_TRAINER(null, "victory.bosstrainer", "showBossTrainerVictory"),
        PVP(null, "victory.pvp", "showPVPVictory");

        // Set up some variables for accessing the Enum's data through.
        public String settings, key;
        public String[] flags;

        // Point to where we're grabbing from.
        Victories(final String settings, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return false; } // Victories always use past tense.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Hatches implements Events
    {
        // Hatches.
        NORMAL(null, "hatch.normal", "showNormalHatch"),
        SHINY(null, "hatch.shiny", "showShinyHatch"),
        LEGENDARY(null, "hatch.legendary", "showLegendaryHatch"),
        SHINY_LEGENDARY(null, "hatch.shinylegendary", "showLegendaryHatch", "showShinyHatch"),
        ULTRA_BEAST(null, "hatch.ultrabeast", "showUltraBeastHatch"),
        SHINY_ULTRA_BEAST(null, "hatch.shinyultrabeast", "showUltraBeastHatch", "showShinyHatch");

        // Set up some variables for accessing the Enum's data through.
        public String settings, key;
        public String[] flags;

        // Point to where we're grabbing from.
        Hatches(final String settings, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return true; } // Hatches always use present tense.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Others implements Events
    {
        // Miscellaneous events.
        TRADE(null, true, "trade.normal", "showTrade"),
        DRAW(null, false, "draw.pvp", "showPVPDraw"),
        FAINT(null, false, "faint.normal", "showFaint");

        // Set up some variables for accessing the Enum's data through.
        public String settings, key;
        public boolean presentTense;
        public String[] flags;

        // Point to where we're grabbing from.
        Others(final String settings, final boolean presentTense, final String key, final String... flags)
        {
            this.settings = settings; // Can change! Null until filled in by a (re-)load.
            this.presentTense = presentTense;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public String settings() { return this.settings; }
        public boolean presentTense() { return this.presentTense; }
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }
}
