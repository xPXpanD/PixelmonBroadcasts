// Listens for Pokémon hatching from eggs.
package com.github.xpxpand.pixelmonbroadcasts.listeners;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlaceholderMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
import com.pixelmonmod.pixelmon.api.events.EggHatchEvent;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

// FIXME: Eggs don't show IV percentages, so they get an extra space.
public class HatchListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onHatchEvent(final EggHatchEvent event)
    {
        if (!event.isCanceled())
        {
            // Create shorthand variables for convenience.
            final EntityPlayer player = event.pokemon.getOwnerPlayer();
            final BlockPos location = player.getPosition();
            final String baseName = event.pokemon.getSpecies().getPokemonName();
            final String localizedName = event.pokemon.getSpecies().getLocalizedName();
            final String worldName = player.getEntityWorld().getWorldInfo().getWorldName();
            final String enumString = PrintingMethods.getEnumType(event.pokemon);

            // If we're in a localized setup, log both names.
            final String nameString = baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

            if (EnumSpecies.legendaries.contains(baseName))
            {
                if (event.pokemon.isShiny())
                {
                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Hatches.SHINY_LEGENDARY,
                            worldName, location, player.getName(), "shiny legendary " + nameString + " egg");

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Hatches.SHINY_LEGENDARY,
                            event.pokemon, null, player, null);
                }
                else
                {
                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Hatches.LEGENDARY,
                            worldName, location, player.getName(), "legendary " + nameString + " egg");

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Hatches.LEGENDARY,
                            event.pokemon, null, player, null);
                }
            }
            else if (EnumSpecies.ultrabeasts.contains(baseName))
            {
                if (event.pokemon.isShiny())
                {
                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Hatches.SHINY_ULTRA_BEAST,
                            worldName, location, player.getName(), "shiny " + nameString + " Ultra Beast egg");

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Hatches.SHINY_ULTRA_BEAST,
                            event.pokemon, null, player, null);
                }
                else
                {
                    // Send a log message if we're set up to do logging for this event.
                    PrintingMethods.logEvent(EventData.Hatches.ULTRA_BEAST,
                            worldName, location, player.getName(), nameString + " Ultra Beast egg");

                    // Send enabled broadcasts to people who should receive them.
                    PlaceholderMethods.iterateAndBroadcast(EventData.Hatches.ULTRA_BEAST,
                            event.pokemon, null, player, null);
                }
            }
            else if (event.pokemon.isShiny())
            {
                // Send a log message if we're set up to do logging for this event.
                PrintingMethods.logEvent(EventData.Hatches.SHINY,
                        worldName, location, player.getName(), "shiny " + enumString + nameString + " egg");

                // Send enabled broadcasts to people who should receive them.
                PlaceholderMethods.iterateAndBroadcast(EventData.Hatches.SHINY,
                        event.pokemon, null, player, null);
            }
            else
            {
                // Send a log message if we're set up to do logging for this event.
                PrintingMethods.logEvent(EventData.Hatches.NORMAL,
                        worldName, location, player.getName(), enumString + nameString + " egg");

                // Send enabled broadcasts to people who should receive them.
                PlaceholderMethods.iterateAndBroadcast(EventData.Hatches.NORMAL,
                        event.pokemon, null, player, null);
            }
        }
    }
}

