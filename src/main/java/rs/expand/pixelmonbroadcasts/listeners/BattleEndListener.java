// Listens for ended battles.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumPokemon;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class BattleEndListener
{
    @SubscribeEvent
    public void onBattleEndEvent(final BattleEndEvent event)
    {
        // TODO: %pokedollars% placeholder.
        // TODO: Double battle support, whenever.
        // TODO: See if tracking gym leaders is possible. Maybe look into marking placed leaders with trainer.isGymLeader.
        // FIXME: Keep name ordering (from battle start message) persistent regardless of outcome. Pre-sort alphabetically?
        // FIXME: In PvP, if both sides use a self-killing move or otherwise die it picks a winner. Make this a draw.
        // See who won, and who lost. We populate a list, but generally only use the first result. Seems reliable...
        final List<BattleParticipant> winners = new ArrayList<>(), losers = new ArrayList<>(), neutrals = new ArrayList<>();
        for (Map.Entry<BattleParticipant, BattleResults> entry : event.results.entrySet())
        {
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
        final boolean endedInDraw = neutrals.size() > 1 && getResult(event.results) == BattleResults.DRAW;
        final boolean endedInFlee = neutrals.size() > 1 && getResult(event.results) == BattleResults.FLEE;
        final boolean hasWinnerAndLoser = !winners.isEmpty() && !losers.isEmpty();
        final boolean battleForfeited = event.cause == EnumBattleEndCause.FORFEIT;

        // Set up two participants.
        final BattleParticipant participant1, participant2;

        // Check if our battle result was a draw. Both participants should have been set to "DRAW", in this case.
        if (endedInDraw)
        {
            participant1 = neutrals.get(0);
            participant2 = neutrals.get(1);
        }
        // Check if somebody fled. We should have one participant set to "VICTORY" (and added to winners), and one "FLEE".
        else if (endedInFlee)
        {
            // Hardwire participant1 to be the Pokémon participant.
            if (neutrals.get(0) instanceof WildPixelmonParticipant)
            {
                participant1 = neutrals.get(0);
                participant2 = neutrals.get(1);
            }
            else
            {
                participant1 = neutrals.get(1);
                participant2 = neutrals.get(0);
            }
        }
        // Check if we have a winner AND a loser amongst the participants. Usually trainer/PvP stuff.
        else if (hasWinnerAndLoser)
        {
            // Should be safe -- haven't managed to get two DEFEAT results yet, it seems to always pick DRAW there.
            participant1 = winners.get(0);
            participant2 = losers.get(0);
        }
        // We didn't hit anything that was valid, stop execution.
        else return;

        // If we're still going, set up some more commonly-used variables.
        final String worldName = participant1.getWorld().getWorldInfo().getWorldName();
        final BlockPos location = participant1.getEntity().getPosition();
        final String participant1Name = participant1.getDisplayName();
        final String participant2Name = participant2.getDisplayName();

        // Needed to prevent a weird issue where it would sometimes register a loss when catching a Pokémon.
        // Seemed like a Pixelmon bug, so I reported it.
        if (event.cause != EnumBattleEndCause.FORCE)
        {
            // Create a shorthand broadcast variable for convenience.
            String broadcast;

            // Was our battle between two valid players?
            if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
            {
                if (endedInDraw || battleForfeited)
                {
                    if (logPVPDraws)
                    {
                        // Print a PvP draw message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §7Players §f" + participant1Name +
                                "§7 and §f" + participant2Name +
                                "§7 ended their battle in a draw, in world \"§f" + worldName +
                                "§7\", at X:§f" + location.getX() +
                                "§7 Y:§f" + location.getY() +
                                "§7 Z:§f" + location.getZ()
                        );
                    }

                    if (showPVPDraws)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.draw.pvp");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            // Replace the placeholders for player 2's side, first. We'll grab the normal ones in the final sweep.
                            broadcast = replacePlayer2Placeholders(broadcast, null, (EntityPlayer) participant2.getEntity());

                            // Swap player 1 placeholders, and then send.
                            iterateAndSendBroadcast(broadcast, null, (EntityPlayer) participant1.getEntity(),
                                    false, false, false, "draw.pvp", "showPVPDraw");
                        }
                    }
                }
                else
                {
                    if (logPVPVictories)
                    {
                        // Print a PvP victory message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §ePlayer §6" + participant1Name +
                                "§e defeated player §6" + participant2Name +
                                "§e in world \"§6" + worldName +
                                "§e\", at X:§6" + location.getX() +
                                "§e Y:§6" + location.getY() +
                                "§e Z:§6" + location.getZ()
                        );
                    }

                    if (showPVPVictories)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.victory.pvp");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            // Replace the placeholders for player 2's side, first. We'll grab the normal ones in the final sweep.
                            broadcast = replacePlayer2Placeholders(broadcast, null, (EntityPlayer) participant2.getEntity());

                            // Swap player 1 placeholders, and then send.
                            iterateAndSendBroadcast(broadcast, null, (EntityPlayer) participant1.getEntity(),
                                    false, false, false, "victory.pvp", "showPVPVictory");
                        }
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
                            printBasicMessage
                            (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a boss trainer in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                            );
                        }

                        if (showBossTrainerForfeits)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.forfeit.boss_trainer");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, null, playerEntity, false, false,
                                        false, "forfeit.bosstrainer", "showBossTrainerForfeit");
                            }
                        }
                    }
                    else
                    {
                        if (logTrainerForfeits)
                        {
                            // Print a forfeit message to console.
                            printBasicMessage
                            (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a normal trainer in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                            );
                        }

                        if (showTrainerForfeits)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.forfeit.trainer");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, null, playerEntity, false, false,
                                        false, "forfeit.trainer", "showTrainerForfeit");
                            }
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
                            printBasicMessage
                            (
                                    "§5PBR §f// §cPlayer §4" + participant2Name +
                                    "§c was knocked out by a boss trainer in world \"§4" + worldName +
                                    "§c\", at X:§4" + location.getX() +
                                    "§c Y:§4" + location.getY() +
                                    "§c Z:§4" + location.getZ()
                            );
                        }

                        if (showBossTrainerBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.boss_trainer");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, null, playerEntity, false, false,
                                        false, "blackout.bosstrainer", "showBossTrainerBlackout");
                            }
                        }
                    }
                    else if (showTrainerBlackouts)
                    {
                        if (logTrainerBlackouts)
                        {
                            // Print a blackout message to console.
                            printBasicMessage
                            (
                                    "§5PBR §f// §cPlayer §4" + participant2Name +
                                    "§c was knocked out by a normal trainer in world \"§4" + worldName +
                                    "§c\", at X:§4" + location.getX() +
                                    "§c Y:§4" + location.getY() +
                                    "§c Z:§4" + location.getZ()
                            );
                        }

                        if (showTrainerBlackouts)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            broadcast = getBroadcast("broadcast.blackout.trainer");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, null, playerEntity, false, false,
                                        false, "blackout.trainer", "showTrainerBlackout");
                            }
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
                        printBasicMessage
                        (
                                "§5PBR §f// §ePlayer §6" + participant1Name +
                                "§e defeated a boss trainer in world \"§6" + worldName +
                                "§e\", at X:§6" + location.getX() +
                                "§e Y:§6" + location.getY() +
                                "§e Z:§6" + location.getZ()
                        );
                    }

                    if (showBossTrainerVictories)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.victory.boss_trainer");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, null, playerEntity, false, false,
                                    false, "victory.bosstrainer", "showBossTrainerVictory");
                        }
                    }
                }
                else
                {
                    if (logTrainerVictories)
                    {
                        // Print a victory message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §ePlayer §6" + participant1Name +
                                "§e defeated a normal trainer in world \"§6" + worldName +
                                "§e\", at X:§6" + location.getX() +
                                "§e Y:§6" + location.getY() +
                                "§e Z:§6" + location.getZ()
                        );
                    }

                    if (showTrainerVictories)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.victory.trainer");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, null, playerEntity, false, false,
                                    false, "victory.trainer", "showTrainerVictory");
                        }
                    }
                }
            }
            // Did a player lose to a wild Pokémon? Participant orders got figured out earlier, if a winner and loser were present.
            else if (participant1 instanceof WildPixelmonParticipant && participant2 instanceof PlayerParticipant && !endedInFlee)
            {
                // Create shorthand variables for convenience.
                final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                final EntityPixelmon pokemon = (EntityPixelmon) participant1.getEntity();
                final String pokemonName = participant1.getDisplayName();

                // Is the Pokémon a boss?
                if (pokemon.isBossPokemon())
                {
                    if (logBossBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a boss §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showBossBlackouts)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.blackout.boss");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverBossBlackouts,
                                    false, revealBossBlackouts, "blackout.boss", "showBossBlackout");
                        }
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
                {
                    if (logShinyLegendaryBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a shiny legendary §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showShinyLegendaryBlackouts)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.blackout.shiny_legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverShinyLegendaryBlackouts,
                                    false, revealShinyLegendaryBlackouts, "blackout.shinylegendary", "showShinyLegendaryBlackout");
                        }
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName))
                {
                    if (logLegendaryBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a legendary §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showLegendaryBlackouts)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.blackout.legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverLegendaryBlackouts,
                                    false, revealLegendaryBlackouts, "blackout.legendary", "showLegendaryBlackout");
                        }
                    }
                }
                else if (pokemon.getIsShiny())
                {
                    if (logShinyBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a shiny §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showShinyBlackouts)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.blackout.shiny");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverShinyBlackouts,
                                    false, revealShinyBlackouts, "blackout.shiny", "showShinyBlackout");
                        }
                    }
                }
                else
                {
                    if (logNormalBlackouts)
                    {
                        // Print a blackout message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §cPlayer §4" + participant2Name +
                                "§c was knocked out by a normal §4" + pokemonName +
                                "§c in world \"§4" + worldName +
                                "§c\", at X:§4" + location.getX() +
                                "§c Y:§4" + location.getY() +
                                "§c Z:§4" + location.getZ()
                        );
                    }

                    if (showNormalBlackouts)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.blackout.normal");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverNormalBlackouts,
                                    false, revealNormalBlackouts, "blackout.normal", "showNormalBlackout");
                        }
                    }
                }
            }
            // Did a player flee from battle? Participant types and orders got figured out earlier, again.
            else if (endedInFlee)
            {
                // Create shorthand variables for convenience.
                final EntityPlayer playerEntity = (EntityPlayer) participant2.getEntity();
                final EntityPixelmon pokemon = (EntityPixelmon) participant1.getEntity();
                final String pokemonName = participant1.getDisplayName();

                // Is the Pokémon a boss?
                if (pokemon.isBossPokemon())
                {
                    if (logBossForfeits)
                    {
                        // Print a forfeit message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a boss §e" + pokemonName +
                                "§6 in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }

                    if (showBossForfeits)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.forfeit.boss");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverBossForfeits,
                                    false, revealBossForfeits, "forfeit.boss", "showBossForfeit");
                        }
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName) && pokemon.getIsShiny())
                {
                    if (logShinyLegendaryForfeits)
                    {
                        // Print a forfeit message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a shiny legendary §e" + pokemonName +
                                "§6 in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }

                    if (showShinyLegendaryForfeits)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.forfeit.shiny_legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverShinyLegendaryForfeits,
                                    false, revealShinyLegendaryForfeits, "forfeit.shinylegendary", "showShinyLegendaryForfeit");
                        }
                    }
                }
                else if (EnumPokemon.legendaries.contains(pokemonName))
                {
                    if (logLegendaryForfeits)
                    {
                        // Print a forfeit message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a legendary §e" + pokemonName +
                                "§6 in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }

                    if (showLegendaryForfeits)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.forfeit.legendary");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverLegendaryForfeits,
                                    false, revealLegendaryForfeits, "forfeit.legendary", "showLegendaryForfeit");
                        }
                    }
                }
                else if (pokemon.getIsShiny())
                {
                    if (logShinyForfeits)
                    {
                        // Print a forfeit message to console.
                        printBasicMessage
                        (
                                "§5PBR §f// §6Player §e" + participant2Name +
                                "§6 fled from a shiny §e" + pokemonName +
                                "§6 in world \"§e" + worldName +
                                "§6\", at X:§e" + location.getX() +
                                "§6 Y:§e" + location.getY() +
                                "§6 Z:§e" + location.getZ()
                        );
                    }

                    if (showShinyForfeits)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        broadcast = getBroadcast("broadcast.forfeit.shiny");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, pokemon, playerEntity, hoverShinyForfeits,
                                    false, revealShinyForfeits, "forfeit.shiny", "showShinyForfeit");
                        }
                    }
                }
            }
        }
    }

    // Returns the first battle result from the provided Map.
    private BattleResults getResult(Map<BattleParticipant, BattleResults> map)
    {
        Map.Entry<BattleParticipant, BattleResults> entry = map.entrySet().iterator().next();
        return entry.getValue();
    }
}
