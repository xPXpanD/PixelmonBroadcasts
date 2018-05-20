// Listens for Pokémon captures with balls.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
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
// TODO: Legendary shiny logic.
public class CatchListener
{
    @SubscribeEvent
    public void onCatchPokemonEvent(final CaptureEvent.SuccessfulCapture event)
    {
        final EntityPixelmon pokemon = event.getPokemon();
        final String pokemonName = pokemon.getLocalizedName();
        final String playerName = event.player.getName();
        final World world = pokemon.getEntityWorld();
        final BlockPos location = event.pokeball.getPosition();

        // Fill this in when we have a message to send to all eligible players.
        final Text finalMessage;

        if (showShinyCatchMessage && pokemon.getIsShiny())
        {
            // Print a catch message to console.
            printBasicMessage
            (
                    "§5PBR §f// §bPlayer §3" + playerName +
                    "§b caught a shiny §3" + pokemonName +
                    "§b in world \"§3" + world.getWorldInfo().getWorldName() +
                    "§b\", at X:§3" + location.getX() +
                    "§b Y:§3" + location.getY() +
                    "§b Z:§3" + location.getZ()
            );

            // Parse placeholders and print!
            if (shinyCatchMessage != null)
            {
                // Set up our message. This is the same for all eligible players, so call it once and store it.
                finalMessage = Text.of(replacePlaceholders(shinyCatchMessage, playerName, pokemon, location));

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify.shinycatch"))
                    {
                        // Does the iterated player have the message enabled? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayerMP) recipient, "showShinyCatch"))
                            recipient.sendMessage(finalMessage);
                    }
                });
            }
            else
                printBasicError("The shiny catch message is broken, broadcast failed.");
        }
        else if (showLegendaryCatchMessage && EnumPokemon.legendaries.contains(pokemonName))
        {
            // Print a catch message to console.
            printBasicMessage
            (
                    "§5PBR §f// §bPlayer §3" + playerName +
                    "§b caught a §3" + pokemonName +
                    "§b in world \"§3" + world.getWorldInfo().getWorldName() +
                    "§b\", at X:§3" + location.getX() +
                    "§b Y:§3" + location.getY() +
                    "§b Z:§3" + location.getZ()
            );

            // Parse placeholders and print!
            if (legendaryCatchMessage != null)
            {
                // Set up our message. This is the same for all eligible players, so call it once and store it.
                finalMessage = Text.of(replacePlaceholders(legendaryCatchMessage, playerName, pokemon, location));

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify.legendarycatch"))
                    {
                        // Does the iterated player have the message enabled? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayerMP) recipient, "showLegendaryCatch"))
                            recipient.sendMessage(finalMessage);
                    }
                });
            }
            else
                printBasicError("The legendary catch message is broken, broadcast failed.");
        }
    }
}
