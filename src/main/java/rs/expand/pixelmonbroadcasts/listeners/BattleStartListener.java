// Listens for started battles.
package rs.expand.pixelmonbroadcasts.listeners;

// Remote imports.
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.WildPixelmonParticipant;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

// Local imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PlaceholderMethods.iterateAndSendBroadcast;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

// FIXME: Pokémon using moves like Teleport to warp away from you show up as YOU having fled.
public class BattleStartListener
{
    @SubscribeEvent
    public void onBattleStartEvent(final BattleStartedEvent event)
    {
        // Reject extra participants, for now. Probably needs some work to support double battles, later.
        BattleParticipant participant1 = event.participant1[0];
        BattleParticipant participant2 = event.participant2[0];

        // Are there any players in this battle?
        if (participant1 instanceof PlayerParticipant || participant2 instanceof PlayerParticipant)
        {
            // Did a PvP battle just start? (two players, one on either side)
            if (participant1 instanceof PlayerParticipant && participant2 instanceof PlayerParticipant)
            {
                // Create a list of participants, and then sort them based on their display names.
                // This ensures names and their associated stats are always in the same place.
                // TODO: Actually get this working, it didn't last time.
                //ArrayList<BattleParticipant> participants = new ArrayList<>(Arrays.asList(participant1, participant2));
                //participants.sort(Comparator.comparing(t -> BattleParticipant.class.getName()));

                if (logPVPChallenges)
                {
                    // Create another shorthand variable.
                    final BlockPos location = participant1.getEntity().getPosition();

                    // Print a PvP starting message to console.
                    printUnformattedMessage
                    (
                            "§5PBR §f// §3Player §b" + participant1.getName().getUnformattedText() +
                            "§3 started battling player §b" + participant2.getName().getUnformattedText() +
                            "§3 in world \"§b" + participant1.getWorld().getWorldInfo().getWorldName() +
                            "§3\", at X:§b" + location.getX() +
                            "§3 Y:§b" + location.getY() +
                            "§3 Z:§b" + location.getZ()
                    );
                }

                if (showPVPChallenges)
                {
                    // Get a broadcast from the broadcasts config file, if the key can be found.
                    final String broadcast = getBroadcast("broadcast.challenge.pvp");

                    // Create some more shorthand variables to avoid making this super hard to follow.
                    final EntityPlayer player1Entity = (EntityPlayer) participant1.getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) participant2.getEntity();

                    // Did we find a message? Iterate all available players, and send to those who should receive!
                    if (broadcast != null)
                    {
                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        iterateAndSendBroadcast(broadcast, null, null,
                                player1Entity, player2Entity, false, true, false,
                                "challenge.pvp", "showPVPChallenge");
                    }
                }
            }
            // Are there any trainer NPCs in the battle?
            else if (participant1 instanceof TrainerParticipant || participant2 instanceof TrainerParticipant)
            {
                // Create some variables for the player and trainer.
                final PlayerParticipant player;
                final TrainerParticipant npc;

                // See which side of the battle has our player, and which side has our NPC trainer. Fill things in.
                if (participant1 instanceof PlayerParticipant)
                {
                    player = (PlayerParticipant) participant1;
                    npc = (TrainerParticipant) participant2;
                }
                else
                {
                    player = (PlayerParticipant) participant2;
                    npc = (TrainerParticipant) participant1;
                }

                // Set up even more variables.
                final String worldName = player.getWorld().getWorldInfo().getWorldName();
                final BlockPos location = player.getEntity().getPosition();
                final EntityPlayer playerEntity = (EntityPlayer) player.getEntity();

                if (npc.trainer.getBossMode().isBossPokemon())
                {
                    if (logBossTrainerChallenges)
                    {
                        // Print a challenge message to console.
                        printUnformattedMessage
                        (
                                "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                "§3 challenged a boss trainer in world \"§b" + worldName +
                                "§3\", at X:§b" + location.getX() +
                                "§3 Y:§b" + location.getY() +
                                "§3 Z:§b" + location.getZ()
                        );
                    }

                    if (showBossTrainerChallenges)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        final String broadcast = getBroadcast("broadcast.challenge.boss_trainer");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, null, null, playerEntity,
                                    null, false, true, false,
                                    "challenge.bosstrainer", "showBossTrainerChallenge");
                        }
                    }
                }
                else
                {
                    if (logTrainerChallenges)
                    {
                        // Print a challenge message to console.
                        printUnformattedMessage
                        (
                                "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                "§3 challenged a normal trainer in world \"§b" + worldName +
                                "§3\", at X:§b" + location.getX() +
                                "§3 Y:§b" + location.getY() +
                                "§3 Z:§b" + location.getZ()
                        );
                    }

                    if (showTrainerChallenges)
                    {
                        // Get a broadcast from the broadcasts config file, if the key can be found.
                        final String broadcast = getBroadcast("broadcast.challenge.trainer");

                        // Did we find a message? Iterate all available players, and send to those who should receive!
                        if (broadcast != null)
                        {
                            iterateAndSendBroadcast(broadcast, null, null, playerEntity,
                                    null, false, true, false,
                                    "challenge.trainer", "showTrainerChallenge");
                        }
                    }
                }
            }
            // Are there any wild Pokémon in the battle?
            else if (participant1 instanceof WildPixelmonParticipant || participant2 instanceof WildPixelmonParticipant)
            {
                // Create some variables for the player and Pokémon.
                final PlayerParticipant player;
                final WildPixelmonParticipant pokemon;

                // See which side of the battle has our player, and which side has our Pokémon. Fill things in.
                if (participant1 instanceof PlayerParticipant)
                {
                    player = (PlayerParticipant) participant1;
                    pokemon = (WildPixelmonParticipant) participant2;
                }
                else
                {
                    player = (PlayerParticipant) participant2;
                    pokemon = (WildPixelmonParticipant) participant1;
                }

                // Set up even more common variables.
                final EntityPlayer playerEntity = (EntityPlayer) player.getEntity();
                final EntityPixelmon pokemonEntity = (EntityPixelmon) pokemon.getEntity();
                final String baseName = pokemonEntity.getPokemonName();
                final String localizedName = pokemonEntity.getLocalizedName();
                final BlockPos location = pokemon.getEntity().getPosition();

                // Make sure our Pokémon participant has no owner -- it has to be wild.
                // I put bosses under this check, as well. Who knows what servers cook up for player parties?
                if (!pokemonEntity.hasOwner())
                {
                    // If we're in a localized setup, log both names.
                    final String nameString =
                            baseName.equals(localizedName) ? baseName : baseName + " §3(§b" + localizedName + "§3)";

                    // Is the Pokémon a boss?
                    if (pokemonEntity.isBossPokemon())
                    {
                        if (logBossChallenges)
                        {
                            // Print a challenge message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a boss §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (showBossChallenges)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            final String broadcast = getBroadcast("broadcast.challenge.boss");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverBossChallenges, true, false,
                                        "challenge.boss", "showBossChallenge");
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryChallenges || logShinyChallenges)
                        {
                            // Print a challenge message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a shiny legendary §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (showLegendaryChallenges)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            final String broadcast = getBroadcast("broadcast.challenge.shiny_legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverLegendaryChallenges, true, false,
                                        "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge");
                            }
                        }
                        else if (showShinyChallenges)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            final String broadcast = getBroadcast("broadcast.challenge.shiny_legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverShinyChallenges, true, false,
                                        "challenge.shinylegendary", "showLegendaryChallenge", "showShinyChallenge");
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (logLegendaryChallenges)
                        {
                            // Print a challenge message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a legendary §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (showLegendaryChallenges)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            final String broadcast = getBroadcast("broadcast.challenge.legendary");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverLegendaryChallenges, true, false,
                                        "challenge.legendary", "showLegendaryChallenge");
                            }
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logShinyChallenges)
                        {
                            // Print a challenge message to console.
                            printUnformattedMessage
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a shiny §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (showShinyChallenges)
                        {
                            // Get a broadcast from the broadcasts config file, if the key can be found.
                            final String broadcast = getBroadcast("broadcast.challenge.shiny");

                            // Did we find a message? Iterate all available players, and send to those who should receive!
                            if (broadcast != null)
                            {
                                iterateAndSendBroadcast(broadcast, pokemonEntity, null, playerEntity,
                                        null, hoverShinyChallenges, true, false,
                                        "challenge.shiny", "showShinyChallenge");
                            }
                        }
                    }
                }
            }
        }
    }
}
