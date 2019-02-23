// Listens for ended battles.
package rs.expand.pixelmonbroadcasts.listeners;

import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rs.expand.pixelmonbroadcasts.enums.EnumBroadcastTypes;
import rs.expand.pixelmonbroadcasts.enums.EnumEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.replacePlaceholdersAndSend;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.printUnformattedMessage;

// TODO: %pokedollars% placeholder.
// TODO: Double battle support, whenever.
// TODO: See if tracking gym leaders is possible. Maybe look into tagging placed leaders with trainer.isGymLeader.
// FIXME: Keep name ordering (from battle start message) persistent regardless of outcome. Pre-sort alphabetically?
// FIXME: In PvP, if both sides use a self-killing move or otherwise die it picks a winner. Make this a draw, somehow.
// FIXME: Similarly, using Explosion to kill something special occasionally prints no message.
public class BattleEndListener
{
    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        // See who won, and who lost. We populate a list, but often use only the first result. Seems reliable, so far...
        final List<BattleParticipant> winners = new ArrayList<>(), losers = new ArrayList<>(), neutrals = new ArrayList<>();
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet())
        {
            //printUnformattedMessage("Looping. Participant is " + entry.getValue().name() + ", result is " + entry.getKey().getName());

            switch (entry.getValue())
            {
                case VICTORY:
                    winners.add(entry.getKey()); break;
                case DEFEAT: // Forfeiting trainer battles also registers as a defeat on the player's side. Be careful.
                    losers.add(entry.getKey()); break;
                case DRAW: case FLEE: // Only added to in a draw, or when fleeing from a wild Pokémon.
                    neutrals.add(entry.getKey()); break;
            }
        }

        // Get our battle results, and whether we have the right amount of participants.
        final boolean endedInDraw =
                neutrals.size() > 1 && event.results.entrySet().iterator().next().getValue() == BattleResults.DRAW;
        final boolean endedInFlee =
                neutrals.size() > 1 && event.results.entrySet().iterator().next().getValue() == BattleResults.FLEE;
        final boolean battleForfeited = event.cause == EnumBattleEndCause.FORFEIT;

        // Set up two participants.
        BattleParticipant participant1, participant2;

        // Check if our battle result was a draw. Both participants should have been set to "DRAW", in this case.
        if (endedInDraw)
        {
            participant1 = neutrals.get(0);
            participant2 = neutrals.get(1);
        }
        // Check if we have a winner AND a loser amongst the participants. Unsure if this is still used.
        else if (!winners.isEmpty() && !losers.isEmpty())
        {
            participant1 = winners.get(0);
            participant2 = losers.get(0);
        }
        // Check if we have a winner and a neutral result amongst the participants. Usually trainer/PvP stuff.
        else if (!winners.isEmpty() && !neutrals.isEmpty())
        {
            participant1 = winners.get(0);
            participant2 = neutrals.get(0);
        }
        // Check if somebody fled. We should have one participant set to "VICTORY" (and added to winners), and one "FLEE".
        else if (endedInFlee)
        {
            // Hardwire participant1 to be the Pokémon participant.
            if (neutrals.get(0) instanceof WildPixelmonParticipant && neutrals.get(1) instanceof PlayerParticipant)
            {
                participant1 = neutrals.get(0);
                participant2 = neutrals.get(1);
            }
            else if (neutrals.get(0) instanceof PlayerParticipant && neutrals.get(1) instanceof WildPixelmonParticipant)
            {
                participant1 = neutrals.get(1);
                participant2 = neutrals.get(0);
            }
            // We got a weird result (two Pokémon?), stop execution.
            else return;
        }
        // We didn't hit anything that was valid, stop execution.
        else return;

        // FIXME: Null checks are there for safety, as names can apparently go null. Actually look into this.
        if (participant1.getName() != null && participant2.getName() != null)
        {
            // If we're still going, set up some more commonly-used variables. World stuff should be the same for both.
            final String worldName = participant1.getWorld().getWorldInfo().getWorldName();
            final BlockPos location = participant1.getEntity().getPosition();

            // Needed to prevent a weird issue where it would sometimes register a loss when catching a Pokémon.
            if (event.cause != EnumBattleEndCause.FORCE)
            {
                // Was our battle between two valid players?
                if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // Create some more shorthand variables for convenience.
                    final EntityPlayer player1Entity = (EntityPlayer) participant1.getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) participant2.getEntity();

                    if (endedInDraw || battleForfeited)
                    {
                        if (logPVPDraws)
                        {
                            // Print a PvP draw message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §7Players §f" + player1Entity.getName() +
                                    "§7 and §f" + player2Entity.getName() +
                                    "§7 ended their battle in a draw, in world \"§f" + worldName +
                                    "§7\", at X:§f" + location.getX() +
                                    "§7 Y:§f" + location.getY() +
                                    "§7 Z:§f" + location.getZ()
                            );
                        }

                        if (printPVPDraws)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Others.DRAW,
                                    null, null, player1Entity, player2Entity);
                        }

                        if (notifyPVPDraws)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Others.DRAW,
                                    null, null, player1Entity, player2Entity);
                        }
                    }
                    else
                    {
                        if (logPVPVictories)
                        {
                            // Print a PvP victory message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §7Player §f" + participant1.getName().getUnformattedText() +
                                    "§7 defeated player §f" + participant2.getName().getUnformattedText() +
                                    "§7 in world \"§f" + worldName +
                                    "§7\", at X:§f" + location.getX() +
                                    "§7 Y:§f" + location.getY() +
                                    "§7 Z:§f" + location.getZ()
                            );
                        }

                        if (printPVPVictories)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Victories.PVP,
                                    null, null, player1Entity, player2Entity);
                        }

                        if (notifyPVPVictories)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Victories.PVP,
                                    null, null, player1Entity, player2Entity);
                        }
                    }
                }
                // Did a trainer win from a player? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof TrainerParticipant && participant2 instanceof PlayerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repeated casts.
                    final TrainerParticipant trainer = (TrainerParticipant) participant1;
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();

                    // Was the battle forfeited? I thiiink only the player can do this, right now.
                    if (battleForfeited)
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            if (logBossTrainerForfeits)
                            {
                                // Print a forfeit message to console.
                                printUnformattedMessage
                                (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a boss trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (printBossTrainerForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.BOSS_TRAINER,
                                        null, null, playerEntity, null);
                            }

                            if (notifyBossTrainerForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.BOSS_TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                        else
                        {
                            if (logTrainerForfeits)
                            {
                                // Print a forfeit message to console.
                                printUnformattedMessage
                                (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a normal trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (printTrainerForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.TRAINER,
                                        null, null, playerEntity, null);
                            }

                            if (notifyTrainerForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                    }
                    else
                    {
                        // Is our trainer a boss trainer?
                        if (trainer.trainer.getBossMode().isBossPokemon())
                        {
                            if (logBossTrainerBlackouts)
                            {
                                // Print a blackout message to console.
                                printUnformattedMessage
                                (
                                        "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                        "§1 was knocked out by a boss trainer in world \"§9" + worldName +
                                        "§1\", at X:§9" + location.getX() +
                                        "§1 Y:§9" + location.getY() +
                                        "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (printBossTrainerBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.BOSS_TRAINER,
                                        null, null, playerEntity, null);
                            }

                            if (notifyBossTrainerBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.BOSS_TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                        else if (printTrainerBlackouts || notifyTrainerBlackouts)
                        {
                            if (logTrainerBlackouts)
                            {
                                // Print a blackout message to console.
                                printUnformattedMessage
                                (
                                        "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                        "§1 was knocked out by a normal trainer in world \"§9" + worldName +
                                        "§1\", at X:§9" + location.getX() +
                                        "§1 Y:§9" + location.getY() +
                                        "§1 Z:§9" + location.getZ()
                                );
                            }

                            if (printTrainerBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.TRAINER,
                                        null, null, playerEntity, null);
                            }

                            if (notifyTrainerBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.TRAINER,
                                        null, null, playerEntity, null);
                            }
                        }
                    }
                }
                // Did a player defeat a trainer? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof PlayerParticipant && participant2 instanceof TrainerParticipant)
                {
                    // We have a trainer, so create some convenient variables to avoid repeated casts.
                    final TrainerParticipant trainer2 = (TrainerParticipant) participant2;
                    final EntityPlayer playerEntity = (EntityPlayer) participant1.getEntity();

                    // Is our trainer a boss trainer?
                    if ((trainer2.trainer.getBossMode().isBossPokemon()))
                    {
                        if (logBossTrainerVictories)
                        {
                            // Print a victory message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant1.getName().getUnformattedText() +
                                    "§1 defeated a boss trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printBossTrainerVictories)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Victories.BOSS_TRAINER,
                                    null, null, playerEntity, null);
                        }

                        if (notifyBossTrainerVictories)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Victories.BOSS_TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                    else
                    {
                        if (logTrainerVictories)
                        {
                            // Print a victory message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant1.getName().getUnformattedText() +
                                    "§1 defeated a normal trainer in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printTrainerVictories)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Victories.TRAINER,
                                    null, null, playerEntity, null);
                        }

                        if (notifyTrainerVictories)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Victories.TRAINER,
                                    null, null, playerEntity, null);
                        }
                    }
                }
                // Did a player lose to a wild Pokémon? Participant orders got figured out earlier, if a winner and loser were present.
                else if (participant1 instanceof WildPixelmonParticipant && participant2 instanceof PlayerParticipant && !endedInFlee)
                {
                    // Create shorthand variables for convenience.
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) participant1.getEntity();
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §c(§4" + localizedName + "§c)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        if (logBossBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a boss §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printBossBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyBossBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryBlackouts || logShinyBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a shiny legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printLegendaryBlackouts || notifyLegendaryBlackouts)
                        {
                            if (printLegendaryBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyLegendaryBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyBlackouts || notifyShinyBlackouts)
                        {
                            if (printShinyBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (logLegendaryBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printLegendaryBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyLegendaryBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logUltraBeastBlackouts || logShinyBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a shiny §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printUltraBeastBlackouts || notifyUltraBeastBlackouts)
                        {
                            if (printUltraBeastBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyUltraBeastBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyBlackouts || notifyShinyBlackouts)
                        {
                            if (printShinyBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyBlackouts)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        if (logUltraBeastBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printUltraBeastBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyUltraBeastBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logShinyBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a shiny §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printShinyBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyShinyBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else
                    {
                        if (logNormalBlackouts)
                        {
                            // Print a blackout message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 was knocked out by a normal §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printNormalBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Blackouts.NORMAL,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyNormalBlackouts)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Blackouts.NORMAL,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                }
                // Did a player flee from battle?
                else if (endedInFlee)
                {
                    // Create shorthand variables for convenience.
                    final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                    final EntityPixelmon pokemonEntity = (EntityPixelmon) participant1.getEntity();
                    final String baseName = pokemonEntity.getPokemonName();
                    final String localizedName = pokemonEntity.getLocalizedName();

                    // If we're in a localized setup, format a string for logging both names.
                    final String nameString =
                        baseName.equals(localizedName) ? baseName : baseName + " §1(§9" + localizedName + "§1)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        if (logBossForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a boss §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printBossForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyBossForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryForfeits || logShinyForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printLegendaryForfeits || notifyLegendaryForfeits)
                        {
                            if (printLegendaryForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyLegendaryForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyForfeits || notifyShinyForfeits)
                        {
                            if (printShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (logLegendaryForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a legendary §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printLegendaryForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyLegendaryForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logUltraBeastForfeits || logShinyForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printUltraBeastForfeits || notifyUltraBeastForfeits)
                        {
                            if (printUltraBeastForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyUltraBeastForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyForfeits || notifyShinyForfeits)
                        {
                            if (printShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyForfeits)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                replacePlaceholdersAndSend(
                                        EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        if (logUltraBeastForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a §9" + nameString +
                                    "§1 Ultra Beast in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printUltraBeastForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyUltraBeastForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logShinyForfeits)
                        {
                            // Print a forfeit message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §1Player §9" + participant2.getName().getUnformattedText() +
                                    "§1 fled from a shiny §9" + nameString +
                                    "§1 in world \"§9" + worldName +
                                    "§1\", at X:§9" + location.getX() +
                                    "§1 Y:§9" + location.getY() +
                                    "§1 Z:§9" + location.getZ()
                            );
                        }

                        if (printShinyForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.PRINT, EnumEvents.Forfeits.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyShinyForfeits)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            replacePlaceholdersAndSend(EnumBroadcastTypes.NOTIFY, EnumEvents.Forfeits.SHINY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                }
            }
        }
    }
}
