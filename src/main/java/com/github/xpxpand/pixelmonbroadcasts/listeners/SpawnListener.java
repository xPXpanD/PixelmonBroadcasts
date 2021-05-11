// Listens for Pokémon spawns on the Better Spawner.
package com.github.xpxpand.pixelmonbroadcasts.listeners;

import com.github.xpxpand.pixelmonbroadcasts.utilities.PlaceholderMethods;
import com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.entities.EntityWormhole;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

import static com.github.xpxpand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

public class SpawnListener
{
    // Drop event priority to lowest, and only proceed if the event is still alive by the time we get to it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSpawnEntityEvent(final SpawnEvent event)
    {
        if (!event.isCanceled())
        {
            // Create an entity from the event info that we can check.
            final Entity spawnedEntity = event.action.getOrCreateEntity();

            // Set up a world name variable that we can reuse throughout.
            final String worldName = spawnedEntity.getEntityWorld().getWorldInfo().getWorldName();

            // Check if the entity is a wormhole.
            if (spawnedEntity instanceof EntityWormhole)
            {
                // Grab a specific entity for re-use purposes. This is safe, now.
                final EntityWormhole wormhole = (EntityWormhole) spawnedEntity;

                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Spawns.WORMHOLE, worldName, wormhole.getPosition(), "wormhole");

                // Send enabled broadcasts to people who should receive them.
                PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.WORMHOLE, wormhole, null, null, null);
            }
            // Check if the entity is a Pokémon. We don't want no stinkin' trainers or the like.
            else if (spawnedEntity instanceof EntityPixelmon)
            {
                // Grab a specific entity for re-use purposes. This is safe, now.
                final EntityPixelmon pokemonEntity = (EntityPixelmon) spawnedEntity;

                // Make sure this Pokémon has no owner -- it has to be wild.
                // I put bosses under this check, as well. Who knows what servers cook up for player parties?
                if (!pokemonEntity.hasOwner())
                {
                    // Create shorthand variables for convenience.
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();
                    final BlockPos location = event.action.spawnLocation.location.pos;
                    final String enumString = PrintingMethods.getEnumType(pokemonEntity);

                    // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                    pokemonEntity.setPosition(location.getX(), location.getY(), location.getZ());

                    // If we're in a localized setup, log both names.
                    final String nameString = baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

                    if (pokemonEntity.isBossPokemon())
                    {
                        switch (pokemonEntity.getBossMode())
                        {
                            case Ultimate: case Drowned:
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Spawns.ULTIMATE_BOSS,
                                        worldName, location, enumString + "boss " + nameString + " (Ultimate)");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.ULTIMATE_BOSS,
                                        pokemonEntity, null, null, null);

                                break;
                            }
                            case Legendary:
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Spawns.LEGENDARY_BOSS,
                                        worldName, location, enumString + "boss " + nameString + " (Legendary)");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.LEGENDARY_BOSS,
                                        pokemonEntity, null, null, null);

                                break;
                            }
                            case Epic:
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Spawns.EPIC_BOSS,
                                        worldName, location, enumString + "boss " + nameString + " (Epic)");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.EPIC_BOSS,
                                        pokemonEntity, null, null, null);

                                break;
                            }
                            case Rare:
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Spawns.RARE_BOSS,
                                        worldName, location, enumString + "boss " + nameString + " (Rare)");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.RARE_BOSS,
                                        pokemonEntity, null, null, null);

                                break;
                            }
                            case Uncommon:
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Spawns.UNCOMMON_BOSS,
                                        worldName, location, enumString + "boss " + nameString + " (Uncommon)");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.UNCOMMON_BOSS,
                                        pokemonEntity, null, null, null);
                            }
                            default: // Used for common spawns, and a fallback for anything we don't know how to handle.
                            {
                                // Send a log message if we're set up to do logging for this event.
                                logEvent(EventData.Spawns.COMMON_BOSS,
                                        worldName, location, enumString + "boss " + nameString + " (generic)");

                                // Send enabled broadcasts to people who should receive them.
                                PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.COMMON_BOSS,
                                        pokemonEntity, null, null, null);
                            }
                        }

                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (pokemonEntity.getPokemonData().isShiny())
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.SHINY_LEGENDARY,
                                    worldName, location, "shiny legendary " + nameString);

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.SHINY_LEGENDARY,
                                    pokemonEntity, null, null, null);
                        }
                        else
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.LEGENDARY,
                                    worldName, location, "legendary " + nameString);

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.LEGENDARY,
                                    pokemonEntity, null, null, null);
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        if (pokemonEntity.getPokemonData().isShiny())
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.SHINY_ULTRA_BEAST,
                                    worldName, location, "shiny " + nameString + " Ultra Beast");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.SHINY_ULTRA_BEAST,
                                    pokemonEntity, null, null, null);
                        }
                        else
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.ULTRA_BEAST,
                                    worldName, location, nameString + " Ultra Beast");

                            // Send enabled broadcasts to people who should receive them.
                            PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.ULTRA_BEAST,
                                    pokemonEntity, null, null, null);
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        // Send a log message if we're set up to do logging for this event.
                        logEvent(EventData.Spawns.SHINY,
                                worldName, location, "shiny " + enumString + nameString);

                        // Send enabled broadcasts to people who should receive them.
                        PlaceholderMethods.iterateAndBroadcast(EventData.Spawns.SHINY,
                                pokemonEntity, null, null, null);
                    }
                }
            }
        }
    }
}
