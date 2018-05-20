// Listens for legendary Pokémon that get defeated.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.BeatWildPixelmonEvent;
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
public class DefeatListener
{
    @SubscribeEvent
    public void onBeatWildLegendaryEvent(final BeatWildPixelmonEvent event)
    {
        final EntityPixelmon pokemon = (EntityPixelmon) event.wpp.getEntity();
        final String pokemonName = pokemon.getLocalizedName();
        final String playerName = event.player.getName();
        final World world = pokemon.getEntityWorld();
        final BlockPos location = pokemon.getPosition();

        // Fill this in when we have a message to send to all eligible players.
        final Text finalMessage;

        if (showBossDefeatMessage && pokemon.isBossPokemon())
        {
            // Print a defeat message to console.
            printBasicMessage
            (
                    "§5PBR §f// §ePlayer §6" + playerName +
                    "§e beat a §6" + pokemonName +
                    "§e boss in world \"§6" + world.getWorldInfo().getWorldName() +
                    "§e\", at X:§6" + location.getX() +
                    "§e Y:§6" + location.getY() +
                    "§e Z:§6" + location.getZ()
            );

            // Parse placeholders and print!
            if (bossDefeatMessage != null)
            {
                // Set up our message. This is the same for all eligible players, so call it once and store it.
                finalMessage = Text.of(replacePlaceholders(bossDefeatMessage, playerName, pokemon, location));

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify.bossdefeat"))
                    {
                        // Does the iterated player have the message enabled? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayerMP) recipient, "showBossDefeat"))
                            recipient.sendMessage(finalMessage);
                    }
                });
            }
            else
                printBasicError("The boss defeat message is broken, broadcast failed.");
        }
        else if (showShinyDefeatMessage && pokemon.getIsShiny())
        {
            // Print a defeat message to console.
            printBasicMessage
            (
                    "§5PBR §f// §ePlayer §6" + playerName +
                    "§e beat a shiny §6" + pokemonName +
                    "§e in world \"§6" + world.getWorldInfo().getWorldName() +
                    "§e\", at X:§6" + location.getX() +
                    "§e Y:§6" + location.getY() +
                    "§e Z:§6" + location.getZ()
            );

            // Parse placeholders and print!
            if (shinyDefeatMessage != null)
            {
                // Set up our message. This is the same for all eligible players, so call it once and store it.
                finalMessage = Text.of(replacePlaceholders(shinyDefeatMessage, playerName, pokemon, location));

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify.shinydefeat"))
                    {
                        // Does the iterated player have the message enabled? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayerMP) recipient, "showShinyDefeat"))
                            recipient.sendMessage(finalMessage);
                    }
                });
            }
            else
                printBasicError("The shiny defeat message is broken, broadcast failed.");
        }
        else if (showLegendaryDefeatMessage && EnumPokemon.legendaries.contains(pokemonName))
        {
            // Print a defeat message to console.
            printBasicMessage
            (
                    "§5PBR §f// §ePlayer §6" + playerName +
                    "§e defeated a §6" + pokemonName +
                    "§e in world \"§6" + world.getWorldInfo().getWorldName() +
                    "§e\", at X:§6" + location.getX() +
                    "§e Y:§6" + location.getY() +
                    "§e Z:§6" + location.getZ()
            );

            // Parse placeholders and print!
            if (legendaryDefeatMessage != null)
            {
                // Set up our message. This is the same for all eligible players, so call it once and store it.
                finalMessage = Text.of(replacePlaceholders(legendaryDefeatMessage, playerName, pokemon, location));

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify.legendarydefeat"))
                    {
                        // Does the iterated player have the message enabled? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayerMP) recipient, "showLegendaryDefeat"))
                            recipient.sendMessage(finalMessage);
                    }
                });
            }
            else
                printBasicError("The legendary defeat message is broken, broadcast failed.");
        }
    }
}
