// Listens for ended battles.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

// TODO: %pokedollars% placeholder.
// TODO: Double battle support, whenever.
// TODO: See if tracking gym leaders is possible. Maybe look into tagging placed leaders with trainer.isGymLeader.
// FIXME: Keep name ordering (from battle start message) persistent regardless of outcome. Pre-sort alphabetically?
// FIXME: In PvP, if both sides use a self-killing move or otherwise die it picks a winner. Make this a draw, somehow.
// FIXME: Similarly, using Explosion to kill something special occasionally prints no message.
public class BattleEndListener
{
    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        // See who won, and who lost. We populate a list, but often use only the first result. Seems reliable, so far...
        final List<BattleParticipant> winners = new ArrayList<>(), losers = new ArrayList<>(), neutrals = new ArrayList<>();
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet())
        {
            //logger.info("Looping. Participant is " + entry.getValue().name() + ", result is " + entry.getKey().getName());

            switch (entry.getValue())
            {
                case VICTORY:
                    winners.add(entry.getKey()); break;
                case DEFEAT: // Forfeiting trainer battles also registers as a defeat on the player's side. Be careful.
                    losers.add(entry.getKey()); break;
                case DRAW: case FLEE: // Only added to in a draw, or when fleeing from a wild Pokémon.
                    neutrals.add(entry.getKey()); break;
            }
        }

        // Get our battle results, and whether we have the right amount of participants.
        final boolean endedInDraw =
                neutrals.size() > 1 && event.results.entrySet().iterator().next().getValue() == BattleResults.DRAW;
        final boolean endedInFlee =
                neutrals.size() > 1 && event.results.entrySet().iterator().next().getValue() == BattleResults.FLEE;
        final boolean battleForfeited = event.cause == EnumBattleEndCause.FORFEIT;

        // Set up two participants.
        BattleParticipant participant1, participant2;

        // Check if our battle result was a draw. Both participants should have been set to "DRAW", in this case.
        if (endedInDraw)
        {
            participant1 = neutrals.get(0);
            participant2 = neutrals.get(1);
        }
        // Check if we have a winner AND a loser amongst the participants. Unsure if this is still used.
        else if (!winners.isEmpty() && !losers.isEmpty())
        {
            participant1 = winners.get(0);
            participant2 = losers.get(0);
        }
        // Check if we have a winner and a neutral result amongst the participants. Usually trainer/PvP stuff.
        else if (!winners.isEmpty() && !neutrals.isEmpty())
        {
            participant1 = winners.get(0);
            participant2 = neutrals.get(0);
        }
        // Check if somebody fled. We should have one participant set to "VICTORY" (and added to winners), and one "FLEE".
        else if (endedInFlee)
        {
            // Hardwire participant1 to be the Pokémon participant.
            if (neutrals.get(0) instanceof WildPixelmonParticipant && neutrals.get(1) instanceof PlayerParticipant)
            {
                participant1 = neutrals.get(0);
                participant2 = neutrals.get(1);
            }
            else if (neutrals.get(0) instanceof PlayerParticipant && neutrals.get(1) instanceof WildPixelmonParticipant)
            {
                participant1 = neutrals.get(1);
                participant2 = neutrals.get(0);
            }
            // We got a weird result (two Pokémon?), stop execution.
            else return;
        }
        // We didn't hit anything that was valid, stop execution.
        else return;

        // TODO: Null checks are there for safety, as names can apparently go null. Actually look into this.
        if (participant1.getName() != null && participant2.getName() != null)
        {
            // If we're still going, set up some more commonly-used variables. World stuff should be the same for both.
            final String worldName = participant1.getWorld().getWorldInfo().getWorldName();
            final BlockPos location = participant1.getEntity().getPosition();

            // Needed to prevent a weird issue where it would sometimes register a loss when catching a Pokémon.
            if (event.cause != EnumBattleEndCause.FORCE)
            {
                // Was our battle between two valid players?
                if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // Create some more shorthand variables for convenience.
                    final EntityPlayer player1Entity = (EntityPlayer) participant1.getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) participant2.getEntity();

                    if (endedInDraw || battleForfeited)
                    {
                        if (EventData.PVP.DRAW.checkSettingsOrError("pvpDrawOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.PVP.DRAW,
                                    worldName, location, player1Entity.getName(), player2Entity.getName());

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.PVP.DRAW,
                                    null, null, player1Entity, player2Entity);
                        }
                    }
                    else
                    {
                        if (EventData.PVP.VICTORY.checkSettingsOrError("pvpVictoryOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.PVP.VICTORY,
                                    worldName, location, player1Entity.getName(), player2Entity.getName());

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.PVP.VICTORY, 
                                    null, null, player1Entity, player2Entity);
                        }
                    }
                }
                // Did a trainer win from a player? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof TrainerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repetition.
                    final TrainerParticipant trainer = (TrainerParticipant) participant1;
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();

                    // Was the battle forfeited? I thiiink only the player can do this, right now.
                    if (battleForfeited)
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            if (EventData.Forfeits.BOSS_TRAINER.checkSettingsOrError("bossTrainerForfeitOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Forfeits.BOSS_TRAINER,
                                        worldName, location, playerEntity.getName(), "boss trainer");

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Forfeits.BOSS_TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                        else
                        {
                            if (EventData.Forfeits.TRAINER.checkSettingsOrError("trainerForfeitOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Forfeits.TRAINER,
                                        worldName, location, playerEntity.getName(), "normal trainer");

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Forfeits.TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                    }
                    else
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            if (EventData.Blackouts.BOSS_TRAINER.checkSettingsOrError("bossTrainerBlackoutOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Blackouts.BOSS_TRAINER,
                                        worldName, location, playerEntity.getName(), "boss trainer");

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Blackouts.BOSS_TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                        else
                        {
                            if (EventData.Blackouts.TRAINER.checkSettingsOrError("trainerBlackoutOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Blackouts.TRAINER,
                                        worldName, location, playerEntity.getName(), "normal trainer");

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Blackouts.TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                    }
                }
                // Did a player defeat a trainer? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof PlayerParticipant && participant2 instanceof TrainerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repeated casts.
                    final TrainerParticipant trainer2 = (TrainerParticipant) participant2;
                    final EntityPlayer playerEntity = (EntityPlayer) participant1.getEntity();

                    // Is our trainer a boss trainer?
                    if ((trainer2.trainer.getBossMode().isBossPokemon()))
                    {
                        if (EventData.Victories.BOSS_TRAINER.checkSettingsOrError("bossTrainerVictoryOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Victories.BOSS_TRAINER,
                                    worldName, location, playerEntity.getName(), "boss trainer");

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Victories.BOSS_TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                    else
                    {
                        if (EventData.Victories.TRAINER.checkSettingsOrError("trainerVictoryOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Victories.TRAINER,
                                    worldName, location, playerEntity.getName(), "trainer");

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Victories.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                }
                // Did a player lose to a wild Pokémon? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof WildPixelmonParticipant && participant2 instanceof PlayerParticipant && !endedInFlee)
                {
                    // Create shorthand variables for convenience.
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) participant1.getEntity();
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();
                    final String playerName = participant2.getName().getUnformattedText();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        if (EventData.Blackouts.BOSS.checkSettingsOrError("bossBlackoutOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Blackouts.BOSS,
                                    worldName, location, playerEntity.getName(), "boss " + nameString);

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Blackouts.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (EventData.Blackouts.SHINY_LEGENDARY.checkSettingsOrError(
                                "legendaryBlackoutOptions", "shinyBlackoutOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Blackouts.SHINY_LEGENDARY,
                                    worldName, location, playerEntity.getName(), "shiny legendary " + nameString);

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Blackouts.SHINY_LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        // Print a blackout message to console, if enabled.
                        if (EventData.Blackouts.LEGENDARY.options.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "legendary" + nameString, worldName, location);

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.LEGENDARY, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        // Print a blackout message to console, if enabled.
                        if (EventData.Blackouts.SHINY_ULTRA_BEAST.options.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "shiny" + nameString + " Ultra Beast", worldName, location);

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.SHINY_ULTRA_BEAST, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        // Print a blackout message to console, if enabled.
                        if (EventData.Blackouts.ULTRA_BEAST.options.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, nameString + " Ultra Beast", worldName, location);
                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.ULTRA_BEAST, pokemonEntity, null, playerEntity, null);
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        // Print a blackout message to console, if enabled.
                        if (EventData.Blackouts.SHINY.options.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "shiny" + nameString, worldName, location);

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.SHINY, pokemonEntity, null, playerEntity, null);
                    }
                    else
                    {
                        // Print a blackout message to console, if enabled.
                        if (EventData.Blackouts.NORMAL.options.toLowerCase().contains("log"))
                            printBlackoutMessage(playerName, "shiny legendary" + nameString, worldName, location);

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.NORMAL, pokemonEntity, null, playerEntity, null);
                    }
                }
                // Did a player flee from battle?
                else if (endedInFlee)
                {
                    // Create shorthand variables for convenience.
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) participant1.getEntity();
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();
                    final String playerName = participant2.getName().getUnformattedText();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §1(§9" + localizedName + "§1)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        // Print a forfeit message to console, if enabled.
                        if (EventData.Forfeits.BOSS.options.toLowerCase().contains("log"))
                            printForfeitMessage(playerName, "boss" + nameString, worldName, location);

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Forfeits.BOSS, pokemonEntity, null, playerEntity, null);
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryForfeits || logShinyForfeits)
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Forfeits.SHINY_LEGENDARY, pokemonEntity, null, playerEntity, null);

                        if (printLegendaryForfeits || notifyLegendaryForfeits)
                        {
                            if (printLegendaryForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Forfeits.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyLegendaryForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Forfeits.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyForfeits || notifyShinyForfeits)
                        {
                            if (printShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Forfeits.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Forfeits.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (EventData.Hatches.SHINY.options.toLowerCase().contains("log"))
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printLegendaryForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, EventData.Forfeits.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyLegendaryForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Forfeits.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logUltraBeastForfeits || logShinyForfeits)
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printUltraBeastForfeits || notifyUltraBeastForfeits)
                        {
                            if (printUltraBeastForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Forfeits.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyUltraBeastForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Forfeits.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyForfeits || notifyShinyForfeits)
                        {
                            if (printShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Forfeits.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Forfeits.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        if (EventData.Hatches.SHINY.options.toLowerCase().contains("log"))
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printUltraBeastForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, EventData.Forfeits.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyUltraBeastForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Forfeits.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (EventData.Hatches.SHINY.options.toLowerCase().contains("log"))
                        {
                            // Print a forfeit message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(
                                EventData.Blackouts.BOSS_TRAINER, null, null, playerEntity, null);

                        if (printShinyForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, EventData.Forfeits.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyShinyForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Forfeits.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                }
            }
        }
    }
}
