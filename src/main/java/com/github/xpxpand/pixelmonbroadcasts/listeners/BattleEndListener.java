// Listens for ended battles.
package com.github.xpxpand.pixelmonbroadcasts.listeners;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlaceholderMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
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
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;

// TODO: More extensive battle support. Got it to stop erroring out, but it's not perfect.
// TODO: Maybe see if tracking gym leaders is possible. Maybe look into tagging placed leaders with trainer.isGymLeader.
// FIXME: Pokémon using moves like Teleport to warp away from you show up as YOU having fled.
public class BattleEndListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        if (!event.isCanceled())
        {
            // Was this a PvP battle? TODO: Check doubles.
            if (event.bc.getPlayers().size() == 2)
            {
                if (event.abnormal || event.results.entrySet().iterator().next().getValue() == BattleResults.DRAW)
                {
                    if (event.abnormal)
                        logger.warn("A player-versus-player battle ended abnormally! See draw logging for more info.");

                    // Set up some commonly-used variables. Use the winner's data if necessary.
                    final EntityPlayer player1Entity = (EntityPlayer) event.bc.getPlayers().get(0).getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) event.bc.getPlayers().get(1).getEntity();

                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Draws.PVP, player1Entity.getEntityWorld().getWorldInfo().getWorldName(),
                            player1Entity.getPosition(), player1Entity.getName(), player2Entity.getName());

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Draws.PVP,
                            null, null, player1Entity, player2Entity);
                }
                else
                {
                    // Figure out who won and who lost.
                    final EntityPlayer loser, winner;
                    if (event.bc.getPlayers().get(0).isDefeated)
                    {
                        loser = (EntityPlayer) event.bc.getPlayers().get(0).getEntity();
                        winner = (EntityPlayer) event.bc.getPlayers().get(1).getEntity();
                    }
                    else
                    {
                        loser = (EntityPlayer) event.bc.getPlayers().get(1).getEntity();
                        winner = (EntityPlayer) event.bc.getPlayers().get(0).getEntity();
                    }

                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Victories.PVP, winner.world.getWorldInfo().getWorldName(),
                            winner.getPosition(), winner.getName(), loser.getName());

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Victories.PVP,
                            null, null, winner, loser);
                }
            }
            // Is there a single player in this battle?
            else if (event.bc.getPlayers().size() == 1)
            {
                // Set up participants for re-use.
                final PlayerParticipant player = event.bc.getPlayers().get(0);
                final BattleParticipant opponent = event.bc.getOpponents(player).get(0);

                // FIXME: This can apparently go null? Needs more info.
                // Get a world name String. Cleans things up a little.
                final String worldName = player.getWorld().getWorldInfo().getWorldName();

                // Get the player's entity. Cast to EntityPlayer as this is the type we need to pass in.
                final EntityPlayer playerEntity = (EntityPlayer) player.getEntity();

                // Is our first opponent a trainer?
                if (opponent instanceof TrainerParticipant)
                {
                    // Was this a forfeit? Execute!
                    if (event.cause == EnumBattleEndCause.FORFEIT)
                    {
                        // Is our trainer a boss trainer?
                        if (((TrainerParticipant) opponent).trainer.getBossMode().isBossPokemon())
                        {
                            // Send a log message if we're set up to do logging for this event.
                            PrintingMethods.logEvent(EventData.Forfeits.BOSS_TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss trainer");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.BOSS_TRAINER,
                                    null, null, playerEntity, null);
                        }
                        else
                        {
                            // Send a log message if we're set up to do logging for this event.
                            PrintingMethods.logEvent(EventData.Forfeits.TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "trainer");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                    // Did our player get defeated? Include draws to catch victory-by-suicide, which Pixelmon treats as a loss.
                    else if (event.results.get(player) == BattleResults.DEFEAT || event.results.get(player) == BattleResults.DRAW)
                    {
                        // Is our trainer a boss trainer?
                        if (((TrainerParticipant) opponent).trainer.getBossMode().isBossPokemon())
                        {
                            // Send a log message if we're set up to do logging for this event.
                            PrintingMethods.logEvent(EventData.Blackouts.BOSS_TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss trainer");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.BOSS_TRAINER,
                                    null, null, playerEntity, null);
                        }
                        else
                        {
                            // Send a log message if we're set up to do logging for this event.
                            PrintingMethods.logEvent(EventData.Blackouts.TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "trainer");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                    // Did our player win? Nice.
                    else if (event.results.get(player) == BattleResults.VICTORY)
                    {
                        // Is our trainer a boss trainer?
                        if (((TrainerParticipant) opponent).trainer.getBossMode().isBossPokemon())
                        {
                            // Send a log message if we're set up to do logging for this event.
                            PrintingMethods.logEvent(EventData.Victories.BOSS_TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "boss trainer");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Victories.BOSS_TRAINER,
                                    null, null, playerEntity, null);
                        }
                        else
                        {
                            // Send a log message if we're set up to do logging for this event.
                            PrintingMethods.logEvent(EventData.Victories.TRAINER, worldName, playerEntity.getPosition(),
                                    playerEntity.getName(), "trainer");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Victories.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                }
                // Is our first opponent a wild Pokémon?
                else if (opponent instanceof WildPixelmonParticipant)
                {
                    // Needed to prevent it detecting catches, as they show up as a FORCE result.
                    if (event.cause != EnumBattleEndCause.FORCE)
                    {
                        // Get the Pokémon's entity. Cast to EntityPixelmon as this is the type we need to pass in.
                        final EntityPixelmon pokemonEntity = (EntityPixelmon) opponent.getEntity();

                        // Set up localization stuff for localized setups.
                        final String baseName = pokemonEntity.getPokemonName(), localizedName = pokemonEntity.getLocalizedName();
                        final String nameString = baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";
                        final String enumString = PrintingMethods.getEnumType(pokemonEntity);


                        // Did our player flee from the Pokémon?
                        if (event.cause == EnumBattleEndCause.FLEE)
                        {
                            // Figure out what our wild Pokémon is. Bosses are generic here, no point in splitting them out.
                            if (pokemonEntity.isBossPokemon())
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Forfeits.BOSS, worldName, playerEntity.getPosition(),
                                        playerEntity.getName(), enumString + "boss " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.BOSS,
                                        pokemonEntity, null, playerEntity, null);
                            }
                            else if (EnumSpecies.legendaries.contains(baseName))
                            {
                                if (pokemonEntity.getPokemonData().isShiny())
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Forfeits.SHINY_LEGENDARY, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "shiny legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.SHINY_LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                                else
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Forfeits.LEGENDARY, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else if (EnumSpecies.ultrabeasts.contains(baseName))
                            {
                                if (pokemonEntity.getPokemonData().isShiny())
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Forfeits.SHINY_ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "shiny " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.SHINY_ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                                else
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Forfeits.ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else if (pokemonEntity.getPokemonData().isShiny())
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Forfeits.SHINY, worldName, playerEntity.getPosition(),
                                        playerEntity.getName(), "shiny " + enumString + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Forfeits.SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        // Did our player crush the innocent wild Pokémon? OK. Include draws to catch victory-by-suicide.
                        else if (event.results.get(player) == BattleResults.VICTORY || event.results.get(player) == BattleResults.DRAW)
                        {
                            if (pokemonEntity.isBossPokemon())
                            {
                                switch (pokemonEntity.getBossMode())
                                {
                                    case Ultimate: case Drowned:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Victories.ULTIMATE_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Ultimate)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Victories.ULTIMATE_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Legendary:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Victories.LEGENDARY_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Legendary)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Victories.LEGENDARY_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Epic:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Victories.EPIC_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Epic)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Victories.EPIC_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Rare:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Victories.RARE_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Rare)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Victories.RARE_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Uncommon:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Victories.UNCOMMON_BOSS,worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Uncommon)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Victories.UNCOMMON_BOSS,
                                                pokemonEntity, null, playerEntity, null);
                                    }
                                    default: // Used for common spawns, and a fallback for anything we don't know how to handle.
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Victories.COMMON_BOSS,worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (generic)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Victories.COMMON_BOSS,
                                                pokemonEntity, null, playerEntity, null);
                                    }
                                }
                            }
                            else if (EnumSpecies.legendaries.contains(baseName))
                            {
                                if (pokemonEntity.getPokemonData().isShiny())
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Victories.SHINY_LEGENDARY, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "shiny legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Victories.SHINY_LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                                else
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Victories.LEGENDARY, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Victories.LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else if (EnumSpecies.ultrabeasts.contains(baseName))
                            {
                                if (pokemonEntity.getPokemonData().isShiny())
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Victories.SHINY_ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "shiny " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Victories.SHINY_ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                                else
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Victories.ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Victories.ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else if (pokemonEntity.getPokemonData().isShiny())
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Victories.SHINY, worldName, playerEntity.getPosition(),
                                        playerEntity.getName(), "shiny " + enumString + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Victories.SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        // Did our player get defeated?
                        else if (event.results.get(player) == BattleResults.DEFEAT)
                        {
                            // Figure out what our wild Pokémon is, exactly.
                            if (pokemonEntity.isBossPokemon())
                            {
                                switch (pokemonEntity.getBossMode())
                                {
                                    case Ultimate: case Drowned:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Blackouts.ULTIMATE_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Ultimate)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.ULTIMATE_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Legendary:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Blackouts.LEGENDARY_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Legendary)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.LEGENDARY_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Epic:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Blackouts.EPIC_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Epic)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.EPIC_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Rare:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Blackouts.RARE_BOSS, worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Rare)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.RARE_BOSS,
                                                pokemonEntity, null, playerEntity, null);

                                        break;
                                    }
                                    case Uncommon:
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Blackouts.UNCOMMON_BOSS,worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (Uncommon)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.UNCOMMON_BOSS,
                                                pokemonEntity, null, playerEntity, null);
                                    }
                                    default: // Used for common spawns, and a fallback for anything we don't know how to handle.
                                    {
                                        // Send a log message if we're set up to do logging for this event.
                                        PrintingMethods.logEvent(EventData.Blackouts.COMMON_BOSS,worldName, playerEntity.getPosition(),
                                                playerEntity.getName(), enumString + "boss " + nameString + " (generic)");

                                        // Send enabled broadcasts to people who should receive them.
                                        PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.COMMON_BOSS,
                                                pokemonEntity, null, playerEntity, null);
                                    }
                                }
                            }
                            else if (EnumSpecies.legendaries.contains(baseName))
                            {
                                if (pokemonEntity.getPokemonData().isShiny())
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Blackouts.SHINY_LEGENDARY, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "shiny legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.SHINY_LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                                else
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Blackouts.LEGENDARY, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "legendary " + nameString);

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.LEGENDARY,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else if (EnumSpecies.ultrabeasts.contains(baseName))
                            {
                                if (pokemonEntity.getPokemonData().isShiny())
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Blackouts.SHINY_ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), "shiny " + nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.SHINY_ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                                else
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Blackouts.ULTRA_BEAST, worldName, playerEntity.getPosition(),
                                            playerEntity.getName(), nameString + " Ultra Beast");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.ULTRA_BEAST,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                            else if (pokemonEntity.getPokemonData().isShiny())
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Blackouts.SHINY, worldName, playerEntity.getPosition(),
                                        playerEntity.getName(), "shiny " + enumString + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                            else
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Blackouts.NORMAL, worldName, playerEntity.getPosition(),
                                        playerEntity.getName(), enumString + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Blackouts.NORMAL,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                }
            }
        }
    }
}
