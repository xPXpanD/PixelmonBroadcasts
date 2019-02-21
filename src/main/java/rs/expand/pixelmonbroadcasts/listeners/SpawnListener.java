// Listens for Pokémon spawns on the Better Spawner.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.entities.EntityWormhole;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;

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
            // Grab a specific entity for re-use purposes. This is safe, now.
            final EntityWormhole wormhole = (EntityWormhole) spawnedEntity;

            if (logWormholeSpawns)
            {
                // Create shorthand variables for convenience.
                final BlockPos location = event.action.spawnLocation.location.pos;
                final String worldName = wormhole.getEntityWorld().getWorldInfo().getWorldName();

                // Print a spawn message to console.
                printUnformattedMessage
                (
                        "§5PBR §f// §5A §dwormhole §5has spawned in world \"§d" + worldName +
                        "§5\", at X:§d" + location.getX() +
                        "§5 Y:§d" + location.getY() +
                        "§5 Z:§d" + location.getZ()
                );
            }

            if (showWormholeSpawns)
            {
                iterateAndSendBroadcast("spawn.wormhole", wormhole, null, null, null,
                        hoverBossSpawns, true, false, "spawn.boss", "showBossSpawn");
            }
        }
        // Check if the entity is a Pokémon, not a trainer or the like.
        else if (spawnedEntity instanceof EntityPixelmon)
        {
            // Grab a specific entity for re-use purposes. This is safe, now.
            final EntityPixelmon pokemon = (EntityPixelmon) spawnedEntity;

            // Make sure this Pokémon has no owner -- it has to be wild.
            // I put bosses under this check, as well. Who knows what servers cook up for player parties?
            if (!pokemon.hasOwner())
            {
                // Create shorthand variables for convenience.
                final String broadcast;
                final String baseName = pokemon.getPokemonName();
                final String localizedName = pokemon.getLocalizedName();
                final BlockPos location = event.action.spawnLocation.location.pos;

                // Sets the position of the entity we created, as it's 0 on all coordinates by default.
                pokemon.setPosition(location.getX(), location.getY(), location.getZ());

                // If we're in a localized setup, log both names.
                final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §5(§d" + localizedName + "§5)";

                if (pokemon.isBossPokemon())
                {
                    if (logBossSpawns)
                    {
                        // Print a spawn message to console.
                        printUnformattedMessage
                        (
                                "§5PBR §f// §5A boss §d" + nameString +
                                "§5 has spawned in world \"§d" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§5\", at X:§d" + location.getX() +
                                "§5 Y:§d" + location.getY() +
                                "§5 Z:§d" + location.getZ()
                        );
                    }

                    if (printBossSpawns || notifyBossSpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.boss");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            sendChatBroadcasts(broadcast, pokemon, null, null,
                                    null, hoverBossSpawns, true, false,
                                    "spawn.boss", "showBossSpawn");
                        }
                    }
                }
                else if (EnumSpecies.legendaries.contains(baseName) && pokemon.getPokemonData().isShiny())
                {
                    if (logLegendarySpawns || logShinySpawns)
                    {
                        // Print a spawn message to console.
                        printUnformattedMessage
                        (
                                "§5PBR §f// §5A shiny legendary §d" + nameString +
                                "§5 has spawned in world \"§d" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§5\", at X:§d" + location.getX() +
                                "§5 Y:§d" + location.getY() +
                                "§5 Z:§d" + location.getZ()
                        );
                    }

                    if (showLegendarySpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.shiny_legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, null, null,
                                    hoverLegendarySpawns, true, false,
                                    "spawn.shinylegendary", "showLegendarySpawn", "showShinySpawn");
                        }
                    }
                    else if (showShinySpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.shiny_legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, null, null,
                                    hoverShinySpawns, true, false,
                                    "spawn.shinylegendary", "showLegendarySpawn", "showShinySpawn");
                        }
                    }
                }
                else if (EnumSpecies.legendaries.contains(baseName))
                {
                    if (logLegendarySpawns)
                    {
                        // Print a spawn message to console.
                        printUnformattedMessage
                        (
                                "§5PBR §f// §5A legendary §d" + nameString +
                                "§5 has spawned in world \"§d" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§5\", at X:§d" + location.getX() +
                                "§5 Y:§d" + location.getY() +
                                "§5 Z:§d" + location.getZ()
                        );
                    }

                    if (showLegendarySpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, null,
                                    null, hoverLegendarySpawns, true, false,
                                    "spawn.legendary", "showLegendarySpawn");
                        }
                    }
                }
                else if (pokemon.getPokemonData().isShiny())
                {
                    if (logShinySpawns)
                    {
                        // Print a spawn message to console.
                        printUnformattedMessage
                        (
                                "§5PBR §f// §5A shiny §d" + nameString +
                                "§5 has spawned in world \"§d" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§5\", at X:§d" + location.getX() +
                                "§5 Y:§d" + location.getY() +
                                "§5 Z:§d" + location.getZ()
                        );
                    }

                    if (showShinySpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.shiny");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, null, null,
                                    hoverShinySpawns, true, false,
                                    "spawn.shiny", "showShinySpawn");
                        }
                    }
                }
            }
        }
    }
}
