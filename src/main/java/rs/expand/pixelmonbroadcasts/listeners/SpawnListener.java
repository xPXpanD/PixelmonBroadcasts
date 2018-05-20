// Listens for Pokémon spawns on the Better Spawner.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

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
            // Make an assumption. This should be safe, now.
            final EntityPixelmon pokemon = (EntityPixelmon) spawnedEntity;

            // Make sure this Pokémon has no owner -- it has to be wild.
            if (!pokemon.hasOwner())
            {
                // TODO: Does getLocalizedName get a language-specific name? Test.
                final String pokemonName = pokemon.getLocalizedName();
                final World world = pokemon.getEntityWorld();
                final BlockPos location = event.action.spawnLocation.location.pos;

                // Fill this in when we have a message to send to all eligible players.
                final Text finalMessage;

                // Is the spawn a boss, and are we supposed to broadcast this?
                if (showBossSpawnMessage && pokemon.isBossPokemon())
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

                    // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                    if (bossSpawnMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        finalMessage = Text.of(replacePlaceholders(bossSpawnMessage, null, pokemon, location));

                        // Sift through the online players.
                        Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                        {
                            // Does the iterated player have the needed notifier permission?
                            if (recipient.hasPermission("pixelmonbroadcasts.notify.bossspawn"))
                            {
                                // Does the iterated player have the message enabled? Send it if we get "true" returned.
                                if (checkToggleStatus((EntityPlayerMP) recipient, "showBossSpawn"))
                                    recipient.sendMessage(finalMessage);
                            }
                        });
                    }
                    else
                        printBasicError("The boss spawn message is broken, broadcast failed.");
                }
                // Is the spawn shiny?
                else if (showShinySpawnMessage && pokemon.getIsShiny())
                {
                    // ...whoa. We've got a shiny that's also a legendary!
                    // Only shown to people who also have the legendary permission, and who have legendary messages turned on.
                    if (showLegendarySpawnMessage && EnumPokemon.legendaries.contains(pokemonName))
                    {
                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §2§l" + pokemonName +
                                "§a (shiny legendary!) has spawned in world \"§2" + world.getWorldInfo().getWorldName() +
                                "§a\", at X:§2" + location.getX() +
                                "§a Y:§2" + location.getY() +
                                "§a Z:§2" + location.getZ()
                        );

                        // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                        if (legendaryShinySpawnMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            finalMessage = Text.of(replacePlaceholders(legendaryShinySpawnMessage, null, pokemon, location));

                            // Sift through the online players.
                            Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                            {
                                // Does the iterated player have the needed notifier permission?
                                if (recipient.hasPermission("pixelmonbroadcasts.notify.legendaryspawn"))
                                {
                                    // Does the iterated player have the message enabled? Send it if we get "true" returned.
                                    if (checkToggleStatus((EntityPlayerMP) recipient, "showLegendarySpawn"))
                                        recipient.sendMessage(finalMessage);
                                }
                            });
                        }
                        else
                            printBasicError("The legendary shiny spawn message is broken, broadcast failed.");
                    }
                    // Still not terrible, I suppose.
                    else
                    {
                        // Print a spawn message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §2" + pokemonName +
                                "§a (shiny) has spawned in world \"§2" + world.getWorldInfo().getWorldName() +
                                "§a\", at X:§2" + location.getX() +
                                "§a Y:§2" + location.getY() +
                                "§a Z:§2" + location.getZ()
                        );

                        // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                        if (shinySpawnMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            finalMessage = Text.of(replacePlaceholders(shinySpawnMessage, null, pokemon, location));

                            // Sift through the online players.
                            Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                            {
                                // Does the iterated player have the needed notifier permission?
                                if (recipient.hasPermission("pixelmonbroadcasts.notify.shinyspawn"))
                                {
                                    // Does the iterated player have the message enabled? Send it if we get "true" returned.
                                    if (checkToggleStatus((EntityPlayerMP) recipient, "showShinySpawn"))
                                        recipient.sendMessage(finalMessage);
                                }
                            });
                        }
                        else
                            printBasicError("The shiny spawn message is broken, broadcast failed.");
                    }
                }
                // Is the spawn legendary?
                else if (showLegendarySpawnMessage && EnumPokemon.legendaries.contains(pokemonName))
                {
                    // Print a spawn message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §2" + pokemonName +
                            "§a has spawned in world \"§2" + world.getWorldInfo().getWorldName() +
                            "§a\", at X:§2" + location.getX() +
                            "§a Y:§2" + location.getY() +
                            "§a Z:§2" + location.getZ()
                    );

                    // Parse placeholders and print! Pass a null object for the player, so our receiving method knows to ignore.
                    if (legendarySpawnMessage != null)
                    {
                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        finalMessage = Text.of(replacePlaceholders(legendarySpawnMessage, null, pokemon, location));

                        // Sift through the online players.
                        Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                        {
                            // Does the iterated player have the needed notifier permission?
                            if (recipient.hasPermission("pixelmonbroadcasts.notify.legendaryspawn"))
                            {
                                // Does the iterated player have the message enabled? Send it if we get "true" returned.
                                if (checkToggleStatus((EntityPlayerMP) recipient, "showLegendarySpawn"))
                                    recipient.sendMessage(finalMessage);
                            }
                        });
                    }
                    else
                        printBasicError("The legendary spawn message is broken, broadcast failed.");
                }
            }
        }
    }
}
