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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

// TODO: Double battle support, whenever.
// TODO: See if tracking gym leaders is possible. Maybe look into tagging placed leaders with trainer.isGymLeader.
// FIXME: In PvP, if both sides use a self-killing move or otherwise die it picks a winner. Make this a draw, somehow.
// FIXME: Similarly, using Explosion to kill something special occasionally prints no message.
// FIXME: Pokémon using moves like Teleport to warp away from you show up as YOU having fled.
public class BattleEndListener
{
    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        // Was this a PvP battle? TODO: Check doubles.
        if (event.bc.getPlayers().size() == 2)
        {
            if (event.abnormal || event.results.entrySet().iterator().next().getValue() == (BattleResults.DRAW))
            {
                if (event.abnormal)
                    logger.warn("A player-versus-player battle ended abnormally! See draw logging for more info.");

                if (EventData.Draws.PVP.checkSettingsOrError("pvpDrawOptions"))
                {
                    // Set up some commonly-used variables. Use the winner's data if necessary.
                    final EntityPlayer player1Entity = (EntityPlayer) event.bc.getPlayers().get(0).getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) event.bc.getPlayers().get(1).getEntity();

                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Draws.PVP, player1Entity.getEntityWorld().getWorldInfo().getWorldName(),
                            player1Entity.getPosition(), player1Entity.getName(), player2Entity.getName());

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Draws.PVP,
                            null, null, player1Entity, player2Entity);
                }
            }
            else
            {
                if (EventData.Victories.PVP.checkSettingsOrError("pvpVictoryOptions"))
                {
                    // Figure out who won and who lost.
                    final EntityPlayer loser, winner;
                    if (event.bc.participants.get(0).isDefeated)
                    {
                        loser = (EntityPlayer) event.bc.participants.get(0).getEntity();
                        winner = (EntityPlayer) event.bc.participants.get(1).getEntity();
                    }
                    else
                    {
                        loser = (EntityPlayer) event.bc.participants.get(1).getEntity();
                        winner = (EntityPlayer) event.bc.participants.get(0).getEntity();
                    }

                    // Send a log message if we're set up to do logging for this event.
                    logEvent(EventData.Victories.PVP, winner.world.getWorldInfo().getWorldName(),
                            winner.getPosition(), winner.getName(), loser.getName());

                    // Send enabled broadcasts to people who should receive them.
                    iterateAndBroadcast(EventData.Victories.PVP,
                            null, null, winner, loser);
                }
            }
        }
        // Is there a single player in this battle? TODO: Doubles support.
        else if (event.bc.getPlayers().size() == 1)
        {
            // Set up participants for re-use.
            final PlayerParticipant player = event.bc.getPlayers().get(0);
            final BattleParticipant opponent = event.bc.getOpponents(player).get(0);

            // Get a world name String. Cleans things up a little.
            final String worldName = player.getWorld().getWorldInfo().getWorldName();

            // Get the player's entity. Cast to EntityPlayer as this is the type we need to pass in.
            final EntityPlayer playerEntity = (EntityPlayer) player.getEntity();

            // Is our first opponent a trainer?
            if (opponent instanceof TrainerParticipant)
            {
                if (event.cause == EnumBattleEndCause.FLEE)
                    logger.error("Got a flee on a trainer battle... This is an issue.");

                // Was this a forfeit? Execute!
                if (event.cause == EnumBattleEndCause.FORFEIT)
                {
                    // Is our trainer a boss trainer?
                    if (((TrainerParticipant) opponent).trainer.getBossMode().isBossPokemon())
                    {
                        if (EventData.Forfeits.BOSS_TRAINER.checkSettingsOrError("bossTrainerForfeitOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Forfeits.BOSS_TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss trainer");

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
                            logEvent(EventData.Forfeits.TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "normal trainer");

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Forfeits.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                }
                // Did our player get defeated?
                else if (event.results.entrySet().iterator().next().getValue() == BattleResults.DEFEAT)
                {
                    // Is our trainer a boss trainer?
                    if (((TrainerParticipant) opponent).trainer.getBossMode().isBossPokemon())
                    {
                        if (EventData.Blackouts.BOSS_TRAINER.checkSettingsOrError("bossTrainerBlackoutOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Blackouts.BOSS_TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss trainer");

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
                            logEvent(EventData.Blackouts.TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "normal trainer");

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Blackouts.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                }
                // Did our player win? Nice.
                else if (event.results.entrySet().iterator().next().getValue() == BattleResults.VICTORY)
                {
                    // Is our trainer a boss trainer?
                    if (((TrainerParticipant) opponent).trainer.getBossMode().isBossPokemon())
                    {
                        if (EventData.Victories.BOSS_TRAINER.checkSettingsOrError("bossTrainerVictoryOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Victories.BOSS_TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss trainer");

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
                            logEvent(EventData.Victories.TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "normal trainer");

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Victories.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                }
            }
            // Is our first opponent a wild Pokémon?
            else if (opponent instanceof WildPixelmonParticipant)
            {
                // Needed to prevent a weird issue where it would sometimes register a loss when catching a Pokémon.
                if (event.cause != EnumBattleEndCause.FORCE)
                {
                    // Get the Pokémon's entity. Cast to EntityPixelmon as this is the type we need to pass in.
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) opponent.getEntity();

                    // Set up localization stuff for localized setups.
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();
                    final String nameString =
                            baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

                    if (event.cause == EnumBattleEndCause.FORFEIT)
                        logger.error("Got a forfeit on a wild Pokémon battle... This may be an issue.");

                    // Did our player flee from the Pokémon?
                    if (event.cause == EnumBattleEndCause.FLEE)
                    {
                        // Figure out what our wild Pokémon is, exactly.
                        if (pokemonEntity.isBossPokemon())
                        {
                            if (EventData.Forfeits.BOSS.checkSettingsOrError("bossForfeitOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Forfeits.BOSS, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Forfeits.BOSS,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (EnumSpecies.legendaries.contains(baseName))
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                if (EventData.Forfeits.SHINY_LEGENDARY.checkSettingsOrError(
                                        "legendaryForfeitOptions", "shinyForfeitOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Forfeits.SHINY_LEGENDARY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Forfeits.SHINY_LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else
                            {
                                if (EventData.Forfeits.LEGENDARY.checkSettingsOrError("legendaryForfeitOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Forfeits.LEGENDARY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Forfeits.LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                        }
                        else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                if (EventData.Forfeits.SHINY_ULTRA_BEAST.checkSettingsOrError(
                                        "ultraBeastForfeitOptions", "shinyForfeitOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Forfeits.SHINY_ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Forfeits.SHINY_ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else
                            {
                                if (EventData.Forfeits.ULTRA_BEAST.checkSettingsOrError("ultraBeastForfeitOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Forfeits.ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "normal " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Forfeits.ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                        }
                        else if (pokemonEntity.getPokemonData().isShiny())
                        {
                            if (EventData.Forfeits.SHINY.checkSettingsOrError("shinyForfeitOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Forfeits.SHINY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Forfeits.SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    // Did our player courageously defeat the innocent wild Pokémon? Rude.
                    else if (event.results.get(player) == BattleResults.VICTORY)
                    {
                        if (pokemonEntity.isBossPokemon())
                        {
                            if (EventData.Victories.BOSS.checkSettingsOrError("bossVictoryOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Victories.BOSS, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Victories.BOSS,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (EnumSpecies.legendaries.contains(baseName))
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                if (EventData.Victories.SHINY_LEGENDARY.checkSettingsOrError(
                                        "legendaryVictoryOptions", "shinyVictoryOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Victories.SHINY_LEGENDARY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Victories.SHINY_LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else
                            {
                                if (EventData.Victories.LEGENDARY.checkSettingsOrError("legendaryVictoryOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Victories.LEGENDARY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Victories.LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                        }
                        else if (EnumSpecies.ultrabeasts.contains(baseName))
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                if (EventData.Victories.SHINY_ULTRA_BEAST.checkSettingsOrError(
                                        "ultraBeastVictoryOptions", "shinyVictoryOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Victories.SHINY_ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Victories.SHINY_ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else
                            {
                                if (EventData.Victories.ULTRA_BEAST.checkSettingsOrError("ultraBeastVictoryOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Victories.ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "normal " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Victories.ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                        }
                        else if (pokemonEntity.getPokemonData().isShiny())
                        {
                            if (EventData.Victories.SHINY.checkSettingsOrError("shinyVictoryOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Victories.SHINY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Spawns.SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    // Did our player get defeated?
                    else if (event.results.get(player) == BattleResults.DEFEAT)
                    {
                        // Figure out what our wild Pokémon is, exactly.
                        if (pokemonEntity.isBossPokemon())
                        {
                            if (EventData.Blackouts.BOSS.checkSettingsOrError("bossBlackoutOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Blackouts.BOSS, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Blackouts.BOSS,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (EnumSpecies.legendaries.contains(baseName))
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                if (EventData.Blackouts.SHINY_LEGENDARY.checkSettingsOrError(
                                        "legendaryBlackoutOptions", "shinyBlackoutOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Blackouts.SHINY_LEGENDARY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Blackouts.SHINY_LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else
                            {
                                if (EventData.Blackouts.LEGENDARY.checkSettingsOrError("legendaryBlackoutOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Blackouts.LEGENDARY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Blackouts.LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }

                            }
                        }
                        else if (EnumSpecies.ultrabeasts.contains(baseName))
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                if (EventData.Blackouts.SHINY_ULTRA_BEAST.checkSettingsOrError(
                                        "ultraBeastBlackoutOptions", "shinyBlackoutOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Blackouts.SHINY_ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Blackouts.SHINY_ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else
                            {
                                if (EventData.Blackouts.ULTRA_BEAST.checkSettingsOrError("ultraBeastBlackoutOptions"))
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    logEvent(EventData.Blackouts.ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "normal " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    iterateAndBroadcast(EventData.Blackouts.ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }

                            }
                        }
                        else if (pokemonEntity.getPokemonData().isShiny())
                        {
                            if (EventData.Blackouts.SHINY.checkSettingsOrError("shinyBlackoutOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Blackouts.SHINY, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "shiny " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Blackouts.SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else
                        {
                            if (EventData.Blackouts.NORMAL.checkSettingsOrError("normalBlackoutOptions"))
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Blackouts.NORMAL, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "normal " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                iterateAndBroadcast(EventData.Blackouts.NORMAL,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else
                        logger.error("Event result for player is: " + event.results.get(player));
                }
                else
                    logger.error("Event cause is force!");
            }
        }
    }
}
