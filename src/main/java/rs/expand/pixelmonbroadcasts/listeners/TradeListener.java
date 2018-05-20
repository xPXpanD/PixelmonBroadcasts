// Listens for successful Pokémon trades.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// Note: All the main class stuff and printing stuff is added through static imports.
public class TradeListener
{
    @SubscribeEvent
    public void onTradeCompletedEvent(final PixelmonTradeEvent event)
    {
        if (showTradeMessage)
        {
            final NBTTagCompound sentNBT = event.pokemon1;
            final NBTTagCompound receivedNBT = event.pokemon2;
            final EntityPlayer player = event.player1;
            final EntityPlayer target = event.player2;

            // Print a trade message to console.
            printBasicMessage
            (
                    "§5PBR §f// §7Player §8" + player.getName() +
                    "§7 has traded a §8" + sentNBT.getString(NbtKeys.NAME) +
                    "§7 for §8" + target.getName() +
                    "§7's §8" + receivedNBT.getString(NbtKeys.NAME)
            );

            // Parse placeholders and print!
            if (tradeMessage != null)
            {
                // Get the sending player's world, and create an entity to pass on.
                final World playerWorld = player.getEntityWorld();
                final EntityPixelmon sentPokemonEntity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(sentNBT, playerWorld);

                // Do the same for the target.
                final World targetWorld = target.getEntityWorld();
                final EntityPixelmon receivedPokemonEntity =
                        (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(receivedNBT, targetWorld);

                // Build up an output message String, and then pass it through both sides of the placeholder parser.
                // This ensures that we have working placeholders for everything on the player AND target sides.
                String baseMessage;
                baseMessage = replacePlaceholders(
                        tradeMessage, player.getName(), sentPokemonEntity, player.getPosition());
                baseMessage = replaceAltPlayerPlaceholders(
                        baseMessage, target.getName(), receivedPokemonEntity, target.getPosition());

                // Save to a final Text so Java stops complaining and lets us use the message below.
                final Text finalMessage = Text.of(baseMessage);

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify.trade"))
                    {
                        // Does the iterated player have the message enabled? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayerMP) recipient, "showTradeMessage"))
                            recipient.sendMessage(finalMessage);
                    }
                });
            }
            else
                printBasicError("The trade message is broken, broadcast failed.");
        }
    }
}
