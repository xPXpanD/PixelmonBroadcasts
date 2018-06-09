// Listens for Pokémon spawns on the Better Spawner.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
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
    public void onSpawnPokemonEvent(final SpawnEvent event)
    {
        // See which entity was spawned.
        final Entity spawnedEntity = event.action.getOrCreateEntity();

        // Check if the entity is a Pokémon, not a trainer or the like.
        if (spawnedEntity instanceof EntityPixelmon)
        {
            // Make an assumption. This is safe, now.
            final EntityPixelmon pokemon = (EntityPixelmon) spawnedEntity;

            // Make sure this Pokémon has no owner -- it has to be wild.
            // I put bosses under this check, as well. Who knows what servers cook up for player parties?
            if (!pokemon.hasOwner())
            {
                // Create shorthand variables for convenience.
                final String broadcast;
                final String pokemonName = pokemon.getLocalizedName();
                final BlockPos location = event.action.spawnLocation.location.pos;

                if (pokemon.isBossPokemon())
                {
                    if (logBossSpawns)
                    {
                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §eA boss §6" + pokemonName +
                                "§e has spawned in world \"§6" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§e\", at X:§6" + location.getX() +
                                "§e Y:§6" + location.getY() +
                                "§e Z:§6" + location.getZ()
                        );
                    }

                    if (showBossSpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.boss");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, hoverBossSpawns,
                                    true, false, "spawn.boss", "showBossSpawn");
                        }
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
                {
                    if (logShinyLegendarySpawns)
                    {
                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §aA shiny legendary §2" + pokemonName +
                                "§a has spawned in world \"§2" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§a\", at X:§2" + location.getX() +
                                "§a Y:§2" + location.getY() +
                                "§a Z:§2" + location.getZ()
                        );
                    }

                    if (showShinyLegendarySpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.shiny_legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, hoverShinyLegendarySpawns,
                                    true, false, "spawn.shinylegendary", "showShinyLegendarySpawn");
                        }
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName))
                {
                    if (logLegendarySpawns)
                    {
                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §aA legendary §2" + pokemonName +
                                "§a has spawned in world \"§2" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§a\", at X:§2" + location.getX() +
                                "§a Y:§2" + location.getY() +
                                "§a Z:§2" + location.getZ()
                        );
                    }

                    if (showLegendarySpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, hoverLegendarySpawns,
                                    true, false, "spawn.legendary", "showLegendarySpawn");
                        }
                    }
                }
                else if (pokemon.getIsShiny())
                {
                    if (logShinySpawns)
                    {
                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §bA shiny §3" + pokemonName +
                                "§b has spawned in world \"§3" + pokemon.getEntityWorld().getWorldInfo().getWorldName() +
                                "§b\", at X:§3" + location.getX() +
                                "§b Y:§3" + location.getY() +
                                "§b Z:§3" + location.getZ()
                        );
                    }

                    if (showShinySpawns)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.spawn.shiny");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, null, hoverShinySpawns,
                                    true, false, "spawn.shiny", "showShinySpawn");
                        }
                    }
                }
            }
        }
    }
}
