// Listens for ended battles.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndSendEventMessage;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.replacePlaceholders;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicError;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printBasicMessage;

// TODO: Implement ties with pvpDrawMessage.
public class BattleEndListener
{
    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        // See who won, and who lost. We populate a list, but generally only use the first result. Seems reliable...
        // TODO: Double battle support, whenever.
        final List<BattleParticipant> winners = new ArrayList<>(), losers = new ArrayList<>(), neutrals = new ArrayList<>();
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet())
        {
            switch (entry.getValue())
            {
                case VICTORY:
                    winners.add(entry.getKey()); break;
                case DEFEAT:
                    losers.add(entry.getKey()); break;
                case DRAW:
                    neutrals.add(entry.getKey()); break;
            }
        }

        // Did our battle end as a draw?
        if (neutrals.size() > 1 && getResult(event.results).equals(BattleResults.DRAW))
        {
            // Create some common variables.
            final BattleParticipant player1 = neutrals.get(0);
            final BattleParticipant player2 = neutrals.get(1);

            // Was our battle between two valid players?
            if (player1 instanceof PlayerParticipant && player2 instanceof PlayerParticipant)
            {
                if (logPVPDraws)
                {
                    // Set up some more variables. Only used here.
                    final String worldName = player1.getWorld().getWorldInfo().getWorldName();
                    final BlockPos location = player1.getEntity().getPosition();

                    // Print a PvP draw message to console.
                    printBasicMessage
                    (
                            "§5PBR §f// §6Players §c" + player1.getDisplayName() +
                            "§6 §c" + player2.getDisplayName() +
                            "§6 ended their battle in a draw, in world \"§c" + worldName +
                            "§6\", at X:§c" + location.getX() +
                            "§6 Y:§c" + location.getY() +
                            "§6 Z:§c" + location.getZ()
                    );
                }

                if (showPVPDraws)
                {
                    // Parse placeholders and print!
                    if (pvpDrawMessage != null)
                    {
                        // Create short variables for convenience.
                        final BlockPos player1Pos = ((PlayerParticipant) player1).player.getPosition();
                        final BlockPos player2Pos = ((PlayerParticipant) player2).player.getPosition();

                        // Set up our message. This is the same for all eligible players, so call it once and store it.
                        String finalMessage;
                        finalMessage = replacePlaceholders(pvpDrawMessage, player1.getDisplayName(),
                                false, false, null, player1Pos);
                        finalMessage = replacePlaceholders(finalMessage, player2.getDisplayName(),
                                false, true, null, player2Pos);

                        // Send off the message, the needed notifier permission and the flag to check.
                        iterateAndSendEventMessage(finalMessage, null,
                                false, false, false, "pvpdraw", "showPVPDraws");
                    }
                    else
                        printBasicError("The PvP battle end message is broken, broadcast failed.");
                }

            }
        }
        // Do we have at least one winner and at least one loser?
        else if (!winners.isEmpty() && !losers.isEmpty())
        {
            // Create some common variables.
            final BattleParticipant winner = winners.get(0);
            final BattleParticipant loser = losers.get(0);
            final String worldName = winner.getWorld().getWorldInfo().getWorldName();

            // Do we have an actual player as a winner?
            if (winner instanceof PlayerParticipant)
            {
                // Did our player win from another player?
                if (loser instanceof PlayerParticipant)
                {
                    // Create some variables for the names of our players.
                    final String winnerName = winner.getDisplayName();
                    final String loserName = loser.getDisplayName();

                    if (logPVPDefeats)
                    {
                        final BlockPos location = winners.get(0).getEntity().getPosition();

                        // Print a PvP defeat message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §c" + winnerName +
                                "§6 defeated player §c" + loserName +
                                "§6 in world \"§c" + worldName +
                                "§6\", at X:§c" + location.getX() +
                                "§6 Y:§c" + location.getY() +
                                "§6 Z:§c" + location.getZ()
                        );
                    }

                    if (showPVPDefeats)
                    {
                        // Parse placeholders and print!
                        if (pvpDefeatMessage != null)
                        {
                            // Create short variables for convenience.
                            final BlockPos winPos = ((PlayerParticipant) winner).player.getPosition();
                            final BlockPos losePos = ((PlayerParticipant) loser).player.getPosition();

                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            String finalMessage;
                            finalMessage = replacePlaceholders(pvpDefeatMessage, winnerName,
                                    false, false, null, winPos);
                            finalMessage = replacePlaceholders(finalMessage, loserName,
                                    false, true, null, losePos);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(finalMessage, null,
                                    false, false, false, "pvpdefeat", "showPVPDefeats");
                        }
                        else
                            printBasicError("The PvP battle end message is broken, broadcast failed.");
                    }
                }
                // Did our player beat a trainer?
                else if (losers.get(0) instanceof TrainerParticipant)
                {
                    // Create some common variables.
                    final TrainerParticipant losingTrainer = (TrainerParticipant) losers.get(0);
                    final BlockPos location = winner.getEntity().getPosition();
                    final String winnerName = winner.getDisplayName();

                    if (losingTrainer.trainer.isGymLeader)
                    {
                        if (logLeaderDefeats)
                        {
                            // Print a defeat message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §9Player §1" + winnerName +
                                    "§9 defeated a §1gym leader §9in world \"§1" + worldName +
                                    "§9\", at X:§1" + location.getX() +
                                    "§9 Y:§1" + location.getY() +
                                    "§9 Z:§1" + location.getZ()
                            );
                        }

                        if (showLeaderDefeats)
                        {
                            // Parse placeholders and print!
                            if (leaderDefeatMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(leaderDefeatMessage,
                                        winnerName, false, false, null, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(
                                        finalMessage, null, false, false,
                                        false, "leaderdefeat", "showLeaderDefeat");
                            }
                            else
                                printBasicError("The gym trainer defeat message is broken, broadcast failed.");
                        }
                    }
                    else
                    {
                        if (logTrainerDefeats)
                        {
                            // Print a defeat message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §9Player §1" + winnerName +
                                    "§9 defeated a §1trainer §9in world \"§1" + worldName +
                                    "§9\", at X:§1" + location.getX() +
                                    "§9 Y:§1" + location.getY() +
                                    "§9 Z:§1" + location.getZ()
                            );
                        }

                        if (showTrainerDefeats)
                        {
                            // Parse placeholders and print!
                            if (trainerDefeatMessage != null)
                            {
                                // Set up our message. This is the same for all eligible players, so call it once and store it.
                                final String finalMessage = replacePlaceholders(trainerDefeatMessage,
                                        winnerName, false, false, null, location);

                                // Send off the message, the needed notifier permission and the flag to check.
                                iterateAndSendEventMessage(
                                        finalMessage, null, false, false,
                                        false, "trainerdefeat", "showTrainerDefeat");
                            }
                            else
                                printBasicError("The trainer defeat message is broken, broadcast failed.");
                        }
                    }
                }
            }
            // Did a trainer win from a player?
            else if (winners.get(0) instanceof TrainerParticipant && losers.get(0) instanceof PlayerParticipant)
            {
                // Create some common variables.
                final String loserName = loser.getDisplayName();
                final BlockPos location = loser.getEntity().getPosition();

                // Is our trainer a gym leader? We've already confirmed that the winner was a trainer.
                if (((TrainerParticipant) winners.get(0)).trainer.isGymLeader)
                {
                    if (logLeaderLosses)
                    {
                        // Print a defeat message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §c" + loserName +
                                "§6 was defeated by a §cgym leader §6in world \"§c" + worldName +
                                "§6\", at X:§c" + location.getX() +
                                "§6 Y:§c" + location.getY() +
                                "§6 Z:§c" + location.getZ()
                        );
                    }

                    if (showLeaderLosses)
                    {
                        // Parse placeholders and print!
                        if (leaderLostToMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(leaderLostToMessage,
                                    loserName, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "leaderloss", "showLeaderLostTo");
                        }
                        else
                            printBasicError("The gym leader loss message is broken, broadcast failed.");
                    }
                }
                else if (showTrainerLosses)
                {
                    if (logTrainerLosses)
                    {
                        // Print a defeat message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §c" + loserName +
                                "§6 was defeated by a §ctrainer §6in world \"§c" + worldName +
                                "§6\", at X:§c" + location.getX() +
                                "§6 Y:§c" + location.getY() +
                                "§6 Z:§c" + location.getZ()
                        );
                    }

                    if (showTrainerLosses)
                    {
                        // Parse placeholders and print!
                        if (trainerLostToMessage != null)
                        {
                            // Set up our message. This is the same for all eligible players, so call it once and store it.
                            final String finalMessage = replacePlaceholders(trainerLostToMessage,
                                    loserName, false, false, null, location);

                            // Send off the message, the needed notifier permission and the flag to check.
                            iterateAndSendEventMessage(
                                    finalMessage, null, false, false,
                                    false, "trainerloss", "showTrainerLostTo");
                        }
                        else
                            printBasicError("The trainer loss message is broken, broadcast failed.");
                    }
                }
            }
        }
    }

    // Returns the first result from the provided Map.
    private BattleResults getResult(Map<BattleParticipant, BattleResults> map)
    {
        printBasicMessage("map: " + map);
        Map.Entry<BattleParticipant, BattleResults> entry = map.entrySet().iterator().next();
        printBasicMessage("entry: " + entry);
        BattleResults result = entry.getValue();
        printBasicMessage("result: " + result);
        return result;
    }
}
