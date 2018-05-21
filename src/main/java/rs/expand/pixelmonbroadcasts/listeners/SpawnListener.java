// Listens for Pokémon spawns on the Better Spawner.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// Note: All the main class stuff and printing stuff is added through static imports.
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
            if (!pokemon.hasOwner())
            {
                // TODO: Does getLocalizedName get a language-specific name? Test this to be sure.
                final String pokemonName = pokemon.getLocalizedName();
                final World world = pokemon.getEntityWorld();
                final BlockPos location = event.action.spawnLocation.location.pos;

                if (pokemon.isBossPokemon())
                {
                    if (logBossSpawns)
                    {
                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §2" + pokemonName +
                                "§a (boss) has spawned in world \"§2" + world.getWorldInfo().getWorldName() +
                                "§a\", at X:§2" + location.getX() +
                                "§a Y:§2" + location.getY() +
                                "§a Z:§2" + location.getZ()
                        );
                    }

                    if (showBossSpawnMessage)
                    {
                        // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                        if (bossSpawnMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(bossSpawnMessage, null, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, "bossspawn", "showBossSpawn");
                        }
                        else
                            printBasicError("The boss spawn message is broken, broadcast failed.");
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName))
                {
                    if (logLegendarySpawns)
                    {
                        // Add "shiny" to our console message if we have a shiny legendary.
                        String shinyAddition = "§2";
                        if (pokemon.getIsShiny())
                            shinyAddition = "§aA §2shiny ";

                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// " + shinyAddition + pokemonName +
                                "§a has spawned in world \"§2" + world.getWorldInfo().getWorldName() +
                                "§a\", at X:§2" + location.getX() +
                                "§a Y:§2" + location.getY() +
                                "§a Z:§2" + location.getZ()
                        );
                    }

                    if (showLegendarySpawnMessage)
                    {
                        if (pokemon.getIsShiny())
                        {
                            // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                            if (shinyLegendarySpawnMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                // We use the normal legendary permission for shiny legendaries, as per the config's explanation.
                                final String finalMessage = replacePlaceholders(shinyLegendarySpawnMessage, null, pokemon, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(finalMessage, "legendaryspawn", "showLegendarySpawn");
                            }
                            else
                                printBasicError("The shiny legendary spawn message is broken, broadcast failed.");
                        }
                        else
                        {
                            // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                            if (legendarySpawnMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(legendarySpawnMessage, null, pokemon, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(finalMessage, "legendaryspawn", "showLegendarySpawn");
                            }
                            else
                                printBasicError("The legendary spawn message is broken, broadcast failed.");
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
                                "§5PBR §f// §aA §2shiny " + pokemonName +
                                "§a has spawned in world \"§2" + world.getWorldInfo().getWorldName() +
                                "§a\", at X:§2" + location.getX() +
                                "§a Y:§2" + location.getY() +
                                "§a Z:§2" + location.getZ()
                        );
                    }

                    if (showShinySpawnMessage)
                    {
                        // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                        if (shinySpawnMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(shinySpawnMessage, null, pokemon, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, "shinyspawn", "showShinySpawn");
                        }
                        else
                            printBasicError("The shiny spawn message is broken, broadcast failed.");
                    }
                }
            }
        }
    }
}
