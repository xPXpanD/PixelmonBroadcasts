// Listens for Pokémon captures with balls.
package com.github.xpxpand.pixelmonbroadcasts.listeners;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlaceholderMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

public class CatchListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onCatchPokemonEvent(final CaptureEvent.SuccessfulCapture event)
    {
        if (!event.isCanceled())
        {
            // Get a sanitized player, to work around a weird issue where "event.player" can go null.
            final EntityPlayerMP player = PlaceholderMethods.getSafePlayer("EventData.Catches", event.player, event.getPokemon());

            // Did we get a player back? If not, things broke horribly.
            if (player != null)
            {
                // Create more shorthand variables for convenience.
                final EntityPixelmon pokemonEntity = event.getPokemon();
                final Pokemon pokemon = pokemonEntity.getPokemonData();
                final BlockPos location = event.pokeball.getPosition();
                final String baseName = pokemonEntity.getSpecies().getPokemonName();
                final String localizedName = pokemonEntity.getSpecies().getLocalizedName();
                final String worldName = player.world.getWorldInfo().getWorldName();
                final String enumString = PrintingMethods.getEnumType(pokemonEntity);

                // Sets the position of the entity we created, as entity coordinates get real weird in this event. Dunno why.
                pokemonEntity.setPosition(location.getX(), location.getY(), location.getZ());

                // If we're in a localized setup, log both names.
                final String nameString = baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

                if (EnumSpecies.legendaries.contains(baseName))
                {
                    if (pokemon.isShiny())
                    {
                        // Send a log message if we're set up to do logging for this event.
                        PrintingMethods.logEvent(EventData.Catches.SHINY_LEGENDARY,
                                worldName, location, player.getName(), "shiny legendary " + nameString);

                        // Send enabled broadcasts to people who should receive them.
                        PlaceholderMethods.iterateAndBroadcast(EventData.Catches.SHINY_LEGENDARY,
                                pokemon, null, player, null);
                    }
                    else
                    {
                        // Send a log message if we're set up to do logging for this event.
                        PrintingMethods.logEvent(EventData.Catches.LEGENDARY,
                                worldName, location, player.getName(), "legendary " + nameString);

                        // Send enabled broadcasts to people who should receive them.
                        PlaceholderMethods.iterateAndBroadcast(EventData.Catches.LEGENDARY,
                                pokemon, null, player, null);
                    }
                }
                else if (EnumSpecies.ultrabeasts.contains(baseName))
                {
                    if (pokemon.isShiny())
                    {
                        // Send a log message if we're set up to do logging for this event.
                        PrintingMethods.logEvent(EventData.Catches.SHINY_ULTRA_BEAST,
                                worldName, location, player.getName(), "shiny " + nameString + " Ultra Beast");

                        // Send enabled broadcasts to people who should receive them.
                        PlaceholderMethods.iterateAndBroadcast(EventData.Catches.SHINY_ULTRA_BEAST,
                                pokemon, null, player, null);
                    }
                    else
                    {
                        // Send a log message if we're set up to do logging for this event.
                        PrintingMethods.logEvent(EventData.Catches.ULTRA_BEAST,
                                worldName, location, player.getName(), nameString + " Ultra Beast");

                        // Send enabled broadcasts to people who should receive them.
                        PlaceholderMethods.iterateAndBroadcast(EventData.Catches.ULTRA_BEAST,
                                pokemon, null, player, null);
                    }
                }
                else if (pokemon.isShiny())
                {
                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Catches.SHINY,
                            worldName, location, player.getName(), "shiny " + enumString + nameString);

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Catches.SHINY,
                            pokemon, null, player, null);
                }
                else
                {
                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Catches.NORMAL,
                            worldName, location, player.getName(), enumString + nameString);

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Catches.NORMAL,
                            pokemon, null, player, null);
                }
            }
        }
    }
}
