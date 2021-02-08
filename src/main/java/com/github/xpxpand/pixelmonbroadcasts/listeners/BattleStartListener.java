// Listens for started battles.
package com.github.xpxpand.pixelmonbroadcasts.listeners;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlaceholderMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

public class BattleStartListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onBattleStartEvent(final BattleStartedEvent event)
    {
        if (!event.isCanceled())
        {
            // Reject extra participants, for now. Probably needs some work to support double battles, later.
            BattleParticipant participant1 = event.participant1[0];
            BattleParticipant participant2 = event.participant2[0];

            // Are there any players in this battle?
            if (participant1 instanceof PlayerParticipant || participant2 instanceof PlayerParticipant)
            {
                // Did a PvP battle just start? (two players, one on either side)
                if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // If we're still going, set up some more commonly-used variables. World stuff should be the same for both.
                    final EntityPlayer player1Entity = (EntityPlayer) participant1.getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) participant2.getEntity();

                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Challenges.PVP, participant1.getWorld().getWorldInfo().getWorldName(),
                            participant1.getEntity().getPosition(), player1Entity.getName(), player2Entity.getName());

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.PVP,
                            null, null, player1Entity, player2Entity);
                }
                // Are there any trainer NPCs in the battle?
                else if (participant1 instanceof TrainerParticipant || participant2 instanceof TrainerParticipant)
                {
                    // Create some variables for the player and trainer.
                    final PlayerParticipant player;
                    final TrainerParticipant npc;

                    // See which side of the battle has our player, and which side has our NPC trainer. Fill things in.
                    if (participant1 instanceof PlayerParticipant)
                    {
                        player = (PlayerParticipant) participant1;
                        npc = (TrainerParticipant) participant2;
                    }
                    else
                    {
                        player = (PlayerParticipant) participant2;
                        npc = (TrainerParticipant) participant1;
                    }

                    // Set up even more variables.
                    final String worldName = player.getWorld().getWorldInfo().getWorldName();
                    final BlockPos location = player.getEntity().getPosition();
                    final EntityPlayer playerEntity = (EntityPlayer) player.getEntity();

                    if (npc.trainer.getBossMode().isBossPokemon())
                    {
                        // Send a log message if we're set up to do logging for this event.
                        PrintingMethods.logEvent(EventData.Challenges.BOSS_TRAINER,
                                worldName, location, playerEntity.getName(), "boss trainer");

                        // Send enabled broadcasts to people who should receive them.
                        PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.BOSS_TRAINER,
                                null, null, playerEntity, null);
                    }
                    else
                    {
                        // Send a log message if we're set up to do logging for this event.
                        PrintingMethods.logEvent(EventData.Challenges.TRAINER,
                                worldName, location, playerEntity.getName(), "trainer");

                        // Send enabled broadcasts to people who should receive them.
                        PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.TRAINER,
                                null, null, playerEntity, null);
                    }
                }
                // Are there any wild Pokémon in the battle?
                else if (participant1 instanceof WildPixelmonParticipant || participant2 instanceof WildPixelmonParticipant)
                {
                    // Create some variables for the player and Pokémon.
                    final PlayerParticipant player;
                    final WildPixelmonParticipant pokemon;

                    // See which side of the battle has our player, and which side has our Pokémon. Fill things in.
                    if (participant1 instanceof PlayerParticipant)
                    {
                        player = (PlayerParticipant) participant1;
                        pokemon = (WildPixelmonParticipant) participant2;
                    }
                    else
                    {
                        player = (PlayerParticipant) participant2;
                        pokemon = (WildPixelmonParticipant) participant1;
                    }

                    // Set up even more common variables.
                    final EntityPlayer playerEntity = (EntityPlayer) player.getEntity();
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) pokemon.getEntity();

                    // Make sure our Pokémon participant has no owner -- it has to be wild.
                    // I put bosses under this check, as well. Who knows what servers cook up for player parties?
                    if (!pokemonEntity.hasOwner())
                    {
                        // Set up yet more common variables.
                        final String baseName = pokemonEntity.getPokemonName();
                        final String localizedName = pokemonEntity.getLocalizedName();
                        final String worldName = participant1.getWorld().getWorldInfo().getWorldName();
                        final BlockPos location = pokemon.getEntity().getPosition();
                        final String enumString = PrintingMethods.getEnumType(pokemonEntity);

                        // If we're in a localized setup, log both names.
                        final String nameString = baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

                        // Figure out what our Pokémon is, exactly.
                        if (pokemonEntity.isBossPokemon())
                        {
                            switch (pokemonEntity.getBossMode())
                            {
                                case Ultimate:
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Challenges.ULTIMATE_BOSS, worldName, location,
                                            playerEntity.getName(), enumString + "boss " + nameString + " (Ultimate)");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.ULTIMATE_BOSS,
                                            pokemonEntity, null, playerEntity, null);

                                    break;
                                }
                                case Legendary:
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Challenges.LEGENDARY_BOSS, worldName, location,
                                            playerEntity.getName(), enumString + "boss " + nameString + " (Legendary)");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.LEGENDARY_BOSS,
                                            pokemonEntity, null, playerEntity, null);

                                    break;
                                }
                                case Rare:
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Challenges.RARE_BOSS, worldName, location,
                                            playerEntity.getName(), enumString + "boss " + nameString + " (Rare)");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.RARE_BOSS,
                                            pokemonEntity, null, playerEntity, null);

                                    break;
                                }
                                default: // Will be Equal or Uncommon, only the latter should spawn naturally.
                                {
                                    // Send a log message if we're set up to do logging for this event.
                                    PrintingMethods.logEvent(EventData.Challenges.UNCOMMON_BOSS,worldName, location,
                                            playerEntity.getName(), enumString + "boss " + nameString + " (Uncommon)");

                                    // Send enabled broadcasts to people who should receive them.
                                    PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.UNCOMMON_BOSS,
                                            pokemonEntity, null, playerEntity, null);
                                }
                            }
                        }
                        else if (EnumSpecies.legendaries.contains(baseName))
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Challenges.SHINY_LEGENDARY,
                                        worldName, location, playerEntity.getName(), "shiny legendary " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.SHINY_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                            else
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Challenges.LEGENDARY,
                                        worldName, location, playerEntity.getName(), "legendary " + nameString);

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (EnumSpecies.ultrabeasts.contains(baseName))
                        {
                            if (pokemonEntity.getPokemonData().isShiny())
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Challenges.SHINY_ULTRA_BEAST,
                                        worldName, location, playerEntity.getName(), "shiny " + nameString + " Ultra Beast");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.SHINY_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }
                            else
                            {
                                // Send a log message if we're set up to do logging for this event.
                                PrintingMethods.logEvent(EventData.Challenges.ULTRA_BEAST,
                                        worldName, location, playerEntity.getName(), nameString + " Ultra Beast");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (pokemonEntity.getPokemonData().isShiny())
                        {
                            // Send a log message if we're set up to do logging for this event.
                            PrintingMethods.logEvent(EventData.Challenges.SHINY,
                                    worldName, location, playerEntity.getName(), "shiny " + enumString + nameString);

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Challenges.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                }
            }
        }
    }
}
