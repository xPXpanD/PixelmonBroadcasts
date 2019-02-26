// Listens for started battles.
package rs.expand.pixelmonbroadcasts.listeners;

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
import rs.expand.pixelmonbroadcasts.enums.EventData;

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

                    // Print a PvP starting message to console, if enabled.
                    logger.info
                    (
                            "§5PBR §f// §3Player §b" + participant1.getName().getUnformattedText() +
                            "§3 started battling player §b" + participant2.getName().getUnformattedText() +
                            "§3 in world \"§b" + participant1.getWorld().getWorldInfo().getWorldName() +
                            "§3\", at X:§b" + location.getX() +
                            "§3 Y:§b" + location.getY() +
                            "§3 Z:§b" + location.getZ()
                    );
                }

                if (printPVPChallenges || notifyPVPChallenges)
                {
                    // Create some more shorthand variables to avoid making this messier than it needs to be.
                    final EntityPlayer player1Entity = (EntityPlayer) participant1.getEntity();
                    final EntityPlayer player2Entity = (EntityPlayer) participant2.getEntity();

                    if (printPVPChallenges)
                    {
                        // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                        doBroadcast(EnumBroadcastTypes.PRINT, EventData.Challenges.BOSS,
                                null, null, player1Entity, player2Entity);
                    }

                    if (notifyPVPChallenges)
                    {
                        // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                        doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Challenges.BOSS,
                                null, null, player1Entity, player2Entity);
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
                        // Print a challenge message to console, if enabled.
                        logger.info
                        (
                                "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                "§3 challenged a boss trainer in world \"§b" + worldName +
                                "§3\", at X:§b" + location.getX() +
                                "§3 Y:§b" + location.getY() +
                                "§3 Z:§b" + location.getZ()
                        );
                    }

                    if (printBossTrainerChallenges)
                    {
                        // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                        doBroadcast(EnumBroadcastTypes.PRINT, EventData.Challenges.BOSS_TRAINER,
                                null, null, playerEntity, null);
                    }

                    if (notifyBossTrainerChallenges)
                    {
                        // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                        doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Challenges.BOSS_TRAINER,
                                null, null, playerEntity, null);
                    }
                }
                else
                {
                    if (logTrainerChallenges)
                    {
                        // Print a challenge message to console, if enabled.
                        logger.info
                        (
                                "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                "§3 challenged a normal trainer in world \"§b" + worldName +
                                "§3\", at X:§b" + location.getX() +
                                "§3 Y:§b" + location.getY() +
                                "§3 Z:§b" + location.getZ()
                        );
                    }

                    if (printTrainerChallenges)
                    {
                        // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                        doBroadcast(EnumBroadcastTypes.PRINT, EventData.Challenges.TRAINER,
                                null, null, playerEntity, null);
                    }

                    if (notifyTrainerChallenges)
                    {
                        // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                        doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Challenges.TRAINER,
                                null, null, playerEntity, null);
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
                            // Print a challenge message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a boss §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (printBossChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, EventData.Challenges.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyBossChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Challenges.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logLegendaryChallenges || logShinyChallenges)
                        {
                            // Print a challenge message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a shiny legendary §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (printLegendaryChallenges || notifyLegendaryChallenges)
                        {
                            if (printLegendaryChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Challenges.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyLegendaryChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Challenges.SHINY_LEGENDARY_AS_LEGENDARY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyChallenges || notifyShinyChallenges)
                        {
                            if (printShinyChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Challenges.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Challenges.SHINY_LEGENDARY_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.legendaries.contains(baseName))
                    {
                        if (logLegendaryChallenges)
                        {
                            // Print a challenge message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a legendary §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (printLegendaryChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, EventData.Challenges.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyLegendaryChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Challenges.LEGENDARY,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName) && pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logUltraBeastChallenges || logShinyChallenges)
                        {
                            // Print a challenge message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a shiny §b" + nameString +
                                    "§3 Ultra Beast in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (printUltraBeastChallenges || notifyUltraBeastChallenges)
                        {
                            if (printUltraBeastChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Challenges.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyUltraBeastChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Challenges.SHINY_ULTRA_BEAST_AS_ULTRA_BEAST,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                        else if (printShinyChallenges || notifyShinyChallenges)
                        {
                            if (printShinyChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                                doBroadcast(
                                        EnumBroadcastTypes.PRINT, EventData.Challenges.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }

                            if (notifyShinyChallenges)
                            {
                                // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                                doBroadcast(
                                        EnumBroadcastTypes.NOTIFY, EventData.Challenges.SHINY_ULTRA_BEAST_AS_SHINY,
                                        pokemonEntity, null, playerEntity, null);
                            }
                        }
                    }
                    else if (EnumSpecies.ultrabeasts.contains(baseName))
                    {
                        if (logUltraBeastChallenges)
                        {
                            // Print a challenge message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a legendary §b" + nameString +
                                    "§3 Ultra Beast in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (printUltraBeastChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, EventData.Challenges.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyUltraBeastChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Challenges.ULTRA_BEAST,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                    else if (pokemonEntity.getPokemonData().isShiny())
                    {
                        if (logShinyChallenges)
                        {
                            // Print a challenge message to console, if enabled.
                            logger.info
                            (
                                    "§5PBR §f// §3Player §b" + player.getName().getUnformattedText() +
                                    "§3 engaged a shiny §b" + nameString +
                                    "§3 in world \"§b" + pokemon.getWorld().getWorldInfo().getWorldName() +
                                    "§3\", at X:§b" + location.getX() +
                                    "§3 Y:§b" + location.getY() +
                                    "§3 Z:§b" + location.getZ()
                            );
                        }

                        if (printBossChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted chats.
                            doBroadcast(EnumBroadcastTypes.PRINT, EventData.Challenges.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }

                        if (notifyBossChallenges)
                        {
                            // Print our broadcast with placeholders replaced, if it exists. Send to permitted noticeboards.
                            doBroadcast(EnumBroadcastTypes.NOTIFY, EventData.Challenges.BOSS,
                                    pokemonEntity, null, playerEntity, null);
                        }
                    }
                }
            }
        }
    }
}
