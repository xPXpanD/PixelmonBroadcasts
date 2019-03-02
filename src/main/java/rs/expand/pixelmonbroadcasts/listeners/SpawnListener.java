// Listens for Pokémon spawns on the Better Spawner.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.entities.EntityWormhole;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.logEvent;

public class SpawnListener
{
    @SubscribeEvent
    public void onSpawnEntityEvent(final SpawnEvent event)
    {
        // Create an entity from the event info that we can check.
        final Entity spawnedEntity = event.action.getOrCreateEntity();

        // Check if the entity is a wormhole.
        if (spawnedEntity instanceof EntityWormhole)
        {
            if (EventData.Spawns.WORMHOLE.checkSettingsOrError("wormholeSpawnOptions"))
            {
                // Grab a specific entity for re-use purposes. This is safe, now.
                final EntityWormhole wormhole = (EntityWormhole) spawnedEntity;

                // Send a log message if we're set up to do logging for this event.
                logEvent(EventData.Spawns.WORMHOLE,
                        wormhole.getEntityWorld().getWorldInfo().getWorldName(), wormhole.getPosition(), "wormhole");

                // Send enabled broadcasts to people who should receive them.
                iterateAndBroadcast(EventData.Spawns.WORMHOLE,
                        null, null, null, null);
            }
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
                final String worldName = pokemonEntity.getEntityWorld().getWorldInfo().getWorldName();

                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemonEntity.setPosition(location.getX(), location.getY(), location.getZ());

                // If we're in a localized setup, log both names.
                final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " (" + localizedName + ")";

                if (pokemonEntity.isBossPokemon())
                {
                    if (EventData.Spawns.BOSS.checkSettingsOrError("bossSpawnOptions"))
                    {
                        // Send a log message if we're set up to do logging for this event.
                        logEvent(EventData.Spawns.BOSS,
                                worldName, location, "boss " + nameString);

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(EventData.Spawns.BOSS,
                                pokemonEntity, null, null, null);
                    }
                }
                else if (EnumSpecies.legendaries.contains(baseName))
                {
                    if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (EventData.Spawns.SHINY_LEGENDARY.checkSettingsOrError(
                                "legendarySpawnOptions", "shinySpawnOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.SHINY_LEGENDARY,
                                    worldName, location, "shiny legendary " + nameString);

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Spawns.SHINY_LEGENDARY,
                                    pokemonEntity, null, null, null);
                        }
                    }
                    else
                    {
                        if (EventData.Spawns.LEGENDARY.checkSettingsOrError("legendarySpawnOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.LEGENDARY,
                                    worldName, location, "legendary " + nameString);

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Spawns.LEGENDARY,
                                    pokemonEntity, null, null, null);
                        }

                    }
                }
                else if (EnumSpecies.ultrabeasts.contains(baseName))
                {
                    if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (EventData.Spawns.SHINY_ULTRA_BEAST.checkSettingsOrError(
                                "ultraBeastSpawnOptions", "shinySpawnOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.SHINY_ULTRA_BEAST,
                                    worldName, location, "shiny " + nameString + " Ultra Beast");

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Spawns.SHINY_ULTRA_BEAST,
                                    pokemonEntity, null, null, null);
                        }
                    }
                    else
                    {
                        if (EventData.Spawns.ULTRA_BEAST.checkSettingsOrError("ultraBeastSpawnOptions"))
                        {
                            // Send a log message if we're set up to do logging for this event.
                            logEvent(EventData.Spawns.ULTRA_BEAST,
                                    worldName, location, "normal " + nameString + " Ultra Beast");

                            // Send enabled broadcasts to people who should receive them.
                            iterateAndBroadcast(EventData.Spawns.ULTRA_BEAST,
                                    pokemonEntity, null, null, null);
                        }
                    }
                }
                else if (pokemonEntity.getPokemonData().isShiny())
                {
                    if (EventData.Spawns.SHINY.checkSettingsOrError("shinySpawnOptions"))
                    {
                        // Send a log message if we're set up to do logging for this event.
                        logEvent(EventData.Spawns.SHINY,
                                worldName, location, "shiny " + nameString);

                        // Send enabled broadcasts to people who should receive them.
                        iterateAndBroadcast(EventData.Spawns.SHINY,
                                pokemonEntity, null, null, null);
                    }
                }
            }
        }
    }
}
