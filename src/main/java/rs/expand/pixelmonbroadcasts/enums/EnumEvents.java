package rs.expand.pixelmonbroadcasts.enums;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.getBroadcast;

// List of all internally-known events and their associated broadcast/permission keys and other settings.
public interface EnumEvents
{
    boolean hasHover();
    boolean presentTense();
    boolean showIVs();
    String key();
    String[] flags();

    enum Blackouts implements EnumEvents
    {
        // Blackouts.
        NORMAL(hoverNormalBlackouts, revealNormalBlackouts,
                "blackout.normal", "showNormalBlackout"),
        SHINY(hoverShinyBlackouts, revealShinyBlackouts,
                "blackout.shiny", "showShinyBlackout"),
        LEGENDARY(hoverLegendaryBlackouts, revealLegendaryBlackouts,
                "blackout.legendary", "showLegendaryBlackout"),
        SHINY_LEGENDARY_AS_LEGENDARY(hoverLegendaryBlackouts, revealLegendaryBlackouts,
                "blackout.shinylegendary", "showLegendaryBlackout", "showShinyBlackout"),
        SHINY_LEGENDARY_AS_SHINY(hoverShinyBlackouts, revealShinyBlackouts,
                "blackout.shinylegendary", "showLegendaryBlackout", "showShinyBlackout"),
        ULTRA_BEAST(hoverUltraBeastBlackouts, revealUltraBeastBlackouts,
                "blackout.ultrabeast", "showUltraBeastBlackout"),
        SHINY_ULTRA_BEAST_AS_ULTRA_BEAST(hoverUltraBeastBlackouts, revealUltraBeastBlackouts,
                "blackout.shinyultrabeast", "showUltraBeastBlackout", "showShinyBlackout"),
        SHINY_ULTRA_BEAST_AS_SHINY(hoverShinyBlackouts, revealShinyBlackouts,
                "blackout.shinyultrabeast", "showUltraBeastBlackout", "showShinyBlackout"),
        BOSS(hoverBossBlackouts, revealBossBlackouts,
                "blackout.boss", "showBossBlackout"),
        TRAINER(false, false,
                "blackout.trainer", "showTrainerBlackout"),
        BOSS_TRAINER(false, false,
                "blackout.bosstrainer", "showBossTrainerBlackout");

        // Set up some variables for accessing the Enum's data through.
        public boolean hasHover, showIVs;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Blackouts(final boolean hasHover, final boolean showIVs, final String key, final String... flags)
        {
            this.hasHover = hasHover;
            this.showIVs = showIVs;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return this.hasHover; }
        public boolean presentTense() { return false; } // Blackouts always use past tense.
        public boolean showIVs() { return this.showIVs; }
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Catches implements EnumEvents
    {
        // Catches.
        NORMAL(hoverNormalCatches, revealNormalCatches,
                "catch.normal", "showNormalCatch"),
        SHINY(hoverShinyCatches, revealShinyCatches,
                "catch.shiny", "showShinyCatch"),
        LEGENDARY(hoverLegendaryCatches, revealLegendaryCatches,
                "catch.legendary", "showLegendaryCatch"),
        SHINY_LEGENDARY_AS_LEGENDARY(hoverLegendaryCatches, revealLegendaryCatches,
                "catch.shinylegendary", "showLegendaryCatch", "showShinyCatch"),
        SHINY_LEGENDARY_AS_SHINY(hoverShinyCatches, revealShinyCatches,
                "catch.shinylegendary", "showLegendaryCatch", "showShinyCatch"),
        ULTRA_BEAST(hoverUltraBeastCatches, revealUltraBeastCatches,
                "catch.ultrabeast", "showUltraBeastCatch"),
        SHINY_ULTRA_BEAST_AS_ULTRA_BEAST(hoverUltraBeastCatches, revealUltraBeastCatches,
                "catch.shinyultrabeast", "showUltraBeastCatch", "showShinyCatch"),
        SHINY_ULTRA_BEAST_AS_SHINY(hoverShinyCatches, revealShinyCatches,
                "catch.shinyultrabeast", "showUltraBeastCatch", "showShinyCatch");

        // Set up some variables for accessing the Enum's data through.
        public boolean hasHover, showIVs;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Catches(final boolean hasHover, final boolean showIVs, final String key, final String... flags)
        {
            this.hasHover = hasHover;
            this.showIVs = showIVs;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return this.hasHover; }
        public boolean presentTense() { return true; } // Catches always use present tense.
        public boolean showIVs() { return this.showIVs; }
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Challenges implements EnumEvents
    {
        // Challenges.
        SHINY(hoverShinyChallenges,
                "challenge.shiny", "showShinyChallenge"),
        LEGENDARY(hoverLegendaryChallenges,
                "challenge.legendary", "showLegendaryChallenge"),
        SHINY_LEGENDARY_AS_LEGENDARY(hoverLegendaryChallenges,
                "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge"),
        SHINY_LEGENDARY_AS_SHINY(hoverShinyChallenges,
                "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge"),
        ULTRA_BEAST(hoverUltraBeastChallenges,
                "challenge.ultrabeast", "showUltraBeastChallenge"),
        SHINY_ULTRA_BEAST_AS_ULTRA_BEAST(hoverUltraBeastChallenges,
                "challenge.shinyultrabeast", "showUltraBeastChallenge", "showShinyChallenge"),
        SHINY_ULTRA_BEAST_AS_SHINY(hoverShinyChallenges,
                "challenge.shinyultrabeast", "showUltraBeastChallenge", "showShinyChallenge"),
        BOSS(hoverBossChallenges,
                "challenge.boss", "showBossChallenge"),
        TRAINER(false,
                "challenge.trainer", "showTrainerChallenge"),
        BOSS_TRAINER(false,
                "challenge.bosstrainer", "showBossTrainerChallenge"),
        PVP(false,
                "challenge.pvp", "showPVPChallenge");

        // Set up some variables for accessing the Enum's data through.
        public boolean hasHover;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Challenges(final boolean hasHover, final String key, final String... flags)
        {
            this.hasHover = hasHover;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return this.hasHover; }
        public boolean presentTense() { return true; } // Challenges always use present tense.
        public boolean showIVs() { return false; } // Challenges never reveal IVs due to Pixelmon issues with grabbing them.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Forfeits implements EnumEvents
    {
        // Forfeits.
        SHINY(hoverShinyForfeits, revealShinyForfeits,
            "forfeit.shiny", "showShinyForfeit"),
        LEGENDARY(hoverLegendaryForfeits, revealLegendaryForfeits,
            "forfeit.legendary", "showLegendaryForfeit"),
        SHINY_LEGENDARY_AS_LEGENDARY(hoverLegendaryForfeits, revealLegendaryForfeits,
            "forfeit.shinylegendary", "showLegendaryForfeit", "showShinyForfeit"),
        SHINY_LEGENDARY_AS_SHINY(hoverShinyForfeits, revealShinyForfeits,
            "forfeit.shinylegendary", "showLegendaryForfeit", "showShinyForfeit"),
        ULTRA_BEAST(hoverUltraBeastForfeits, revealUltraBeastForfeits,
            "forfeit.ultrabeast", "showUltraBeastForfeit"),
        SHINY_ULTRA_BEAST_AS_ULTRA_BEAST(hoverUltraBeastForfeits, revealUltraBeastForfeits,
            "forfeit.shinyultrabeast", "showUltraBeastForfeit", "showShinyForfeit"),
        SHINY_ULTRA_BEAST_AS_SHINY(hoverShinyForfeits, revealShinyForfeits,
            "forfeit.shinyultrabeast", "showUltraBeastForfeit", "showShinyForfeit"),
        BOSS(hoverBossForfeits, revealBossForfeits,
            "forfeit.boss", "showBossForfeit"),
        TRAINER(false, false,
            "forfeit.trainer", "showTrainerForfeit"),
        BOSS_TRAINER(false, false,
            "forfeit.bosstrainer", "showBossTrainerForfeit");

        // Set up some variables for accessing the Enum's data through.
        public boolean hasHover, showIVs;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Forfeits(boolean hasHover, final boolean showIVs, final String key, final String... flags)
        {
            this.hasHover = hasHover;
            this.showIVs = showIVs;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return this.hasHover; }
        public boolean presentTense() { return false; } // Forfeits always use past tense.
        public boolean showIVs() { return this.showIVs; }
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Spawns implements EnumEvents
    {
        // Spawns.
        SHINY(hoverShinySpawns,
                "spawn.shiny", "showShinySpawn"),
        LEGENDARY(hoverLegendarySpawns,
                "spawn.legendary", "showLegendarySpawn"),
        SHINY_LEGENDARY_AS_LEGENDARY(hoverLegendarySpawns,
                "spawn.shinylegendary", "showLegendarySpawn", "showShinySpawn"),
        SHINY_LEGENDARY_AS_SHINY(hoverShinySpawns,
                "spawn.shinylegendary", "showLegendarySpawn", "showShinySpawn"),
        ULTRA_BEAST(hoverUltraBeastSpawns,
                "spawn.ultrabeast", "showUltraBeastSpawn"),
        SHINY_ULTRA_BEAST_AS_ULTRA_BEAST(hoverUltraBeastSpawns,
                "spawn.shinyultrabeast", "showUltraBeastSpawn", "showShinySpawn"),
        SHINY_ULTRA_BEAST_AS_SHINY(hoverShinySpawns,
                "spawn.shinyultrabeast", "showUltraBeastSpawn", "showShinySpawn"),
        WORMHOLE(false,
                "spawn.wormhole", "showWormholeSpawn"),
        BOSS(hoverBossSpawns,
                "spawn.boss", "showBossSpawn");

        // Set up some variables for accessing the Enum's data through.
        public boolean hasHover;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Spawns(final boolean hasHover, final String key, final String... flags)
        {
            this.hasHover = hasHover;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return this.hasHover; }
        public boolean presentTense() { return false; } // Spawns always use past tense.
        public boolean showIVs() { return false; } // Spawns never reveal IVs due to Pixelmon issues with grabbing them.
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Victories implements EnumEvents
    {
        // Victories.
        SHINY(hoverShinyVictories, revealShinyVictories,
                "victory.shiny", "showShinyVictory"),
        LEGENDARY(hoverLegendaryVictories, revealLegendaryVictories,
                "victory.legendary", "showLegendaryVictory"),
        SHINY_LEGENDARY_AS_LEGENDARY(hoverLegendaryVictories, revealLegendaryVictories,
                "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory"),
        SHINY_LEGENDARY_AS_SHINY(hoverShinyVictories, revealShinyVictories,
                "victory.shinylegendary", "showLegendaryVictory", "showShinyVictory"),
        ULTRA_BEAST(hoverUltraBeastVictories, revealUltraBeastVictories,
                "victory.ultrabeast", "showUltraBeastVictory"),
        SHINY_ULTRA_BEAST_AS_ULTRA_BEAST(hoverUltraBeastVictories, revealUltraBeastVictories,
                "victory.shinyultrabeast", "showUltraBeastVictory", "showShinyVictory"),
        SHINY_ULTRA_BEAST_AS_SHINY(hoverShinyVictories, revealShinyVictories,
                "victory.shinyultrabeast", "showUltraBeastVictory", "showShinyVictory"),
        BOSS(hoverBossVictories, revealBossVictories,
                "victory.boss", "showBossVictory"),
        TRAINER(false, false,
                "victory.trainer", "showTrainerVictory"),
        BOSS_TRAINER(false, false,
                "victory.bosstrainer", "showBossTrainerVictory"),
        PVP(false, false,
                "victory.pvp", "showPVPVictory");

        // Set up some variables for accessing the Enum's data through.
        public boolean hasHover, showIVs;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Victories(final boolean hasHover, final boolean showIVs, final String key, final String... flags)
        {
            this.hasHover = hasHover;
            this.showIVs = showIVs;
            this.key = getBroadcast(key);
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return this.hasHover; }
        public boolean presentTense() { return false; } // Victories always use past tense.
        public boolean showIVs() { return this.showIVs; }
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Hatches implements EnumEvents
    {
        // Hatches.
        NORMAL(hoverNormalHatches, revealNormalHatches,
                "hatch.normal", "showNormalHatch"),
        SHINY(hoverShinyHatches, revealShinyHatches,
                "hatch.shiny", "showShinyHatch");

        // Set up some variables for accessing the Enum's data through.
        public boolean hasHover, showIVs;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Hatches(final boolean hasHover, final boolean showIVs, final String key, final String... flags)
        {
            this.hasHover = hasHover;
            this.showIVs = showIVs;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return this.hasHover; }
        public boolean presentTense() { return true; } // Hatches always use present tense.
        public boolean showIVs() { return this.showIVs; }
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }

    enum Others implements EnumEvents
    {
        // Miscellaneous events.
        TRADE(true, "trade.normal", "showTrade"),
        DRAW(false, "draw.pvp", "showPVPDraw");

        // Set up some variables for accessing the Enum's data through.
        public boolean presentTense;
        public String key;
        public String[] flags;

        // Point to where we're grabbing from.
        Others(final boolean presentTense, final String key, final String... flags)
        {
            this.presentTense = presentTense;
            this.key = key;
            this.flags = flags;
        }

        // Return values to the main interface so we can access them without needing to access this specific Enum.
        @Override
        public boolean hasHover() { return false; } // None of the miscellaneous events support hovers... yet?
        public boolean presentTense() { return this.presentTense; }
        public boolean showIVs() { return false; } // None of the miscellaneous events support revealing IVs... yet?
        public String key() { return this.key; }
        public String[] flags() { return this.flags; }
    }
}
