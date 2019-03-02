package rs.expand.pixelmonbroadcasts.utilities;

import com.pixelmonmod.pixelmon.api.overlay.notice.NoticeOverlay;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.EntityWormhole;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.forms.EnumAlolan;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.serializer.TextSerializers;
import rs.expand.pixelmonbroadcasts.enums.EventData;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class PlaceholderMethods
{
    public static void iterateAndBroadcast(
            final EventData event, final Object pokemonObject, final Object pokemon2Object,
            final EntityPlayer playerEntity, final EntityPlayer player2Entity)
    {
        if (event.options().toLowerCase().contains("chat"))
        {
            // Combine the passed broadcast type's prefix with the broadcast/permission key to make a full broadcast key.
            final String broadcast =
                    getBroadcast("chat." + event.key(), pokemonObject, pokemon2Object, playerEntity, player2Entity);

            // Make a Text clone of our broadcast message. We can add to this, or just send it directly.
            final Text broadcastText;

            // If hovers are enabled, make the line hoverable.
            if (pokemonObject != null && getUnsafeFlag(event, "hover"))
            {
                broadcastText =
                        getHoverableLine(broadcast, pokemonObject, event.presentTense(), getUnsafeFlag(event, "reveal"));
            }
            else
                broadcastText = Text.of(broadcast);

            // Sift through the online players.
            Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
            {
                // Does the iterated player have the needed notifier permission?
                if (recipient.hasPermission("pixelmonbroadcasts.notify." + event.key()))
                {
                    // Does the iterated player want our broadcast? Send it if we get "true" returned.
                    if (checkToggleStatus((EntityPlayer) recipient, event.flags()))
                        recipient.sendMessage(broadcastText);
                }
            });
        }

        if (event.options().toLowerCase().contains("notice"))
        {
            if (pokemonObject != null)
            {
                // Combine the passed broadcast type's prefix with the broadcast/permission key to make a full broadcast key.
                final String broadcast =
                        getBroadcast("notice." + event.key(), pokemonObject, pokemon2Object, playerEntity, player2Entity);

                // Figure out how to get a Pokemon object for our purposes.
                final Pokemon pokemon;
                if (pokemonObject instanceof Pokemon)
                    pokemon = (Pokemon) pokemonObject;
                else
                    pokemon = ((EntityPixelmon) pokemonObject).getPokemonData();

                // Set up and format a builder for our notice.
                NoticeOverlay.Builder builder = NoticeOverlay.builder().addLine(broadcast);
                if (pokemon != null)
                    builder.setPokemonSprite(new PokemonSpec(pokemon.getSpecies().getPokemonName()));
                else
                    builder.setPokemonSprite(new PokemonSpec("Unown"));

                // Sift through the online players.
                Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
                {
                    // Does the iterated player have the needed notifier permission?
                    if (recipient.hasPermission("pixelmonbroadcasts.notify." + event.key()))
                    {
                        // Does the iterated player want our broadcast? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayer) recipient, event.flags()))
                        {
                            // Prints a message to Pixelmon's notice board. (cool box at the top)
                            builder.sendTo((EntityPlayerMP) recipient);

                            // Put the player's UUID and the current time into a hashmap. Check regularly to wipe notices.
                            noticeExpiryMap.put(recipient.getUniqueId(), System.currentTimeMillis());
                        }
                    }
                });
            }
        }
    }

    public static boolean canReceiveBroadcast(CommandSource src, EventData event)
    {
        if (src.hasPermission("pixelmonbroadcasts.notify." + event.key()) && event.options() != null)
            return event.options().contains("chat") || event.options().contains("notify");

        return false;
    }

    // Checks if a given flag is set for the given event. Has some hardcoded values on stuff that's off-limits.
    private static boolean getUnsafeFlag(EventData event, String flag)
    {
        if (event == EventData.Blackouts.TRAINER && (flag.equals("hover") || flag.equals("reveal")))
        {
            logger.error("Exit path 1.");
            return false;
        }
        else if (event == EventData.Blackouts.BOSS_TRAINER && (flag.equals("hover") || flag.equals("reveal")))
        {
            logger.error("Exit path 2.");
            return false;
        }
        else if (event instanceof EventData.Challenges && flag.equals("reveal")) // TODO: TEST!
        {
            logger.error("Exit path 3!!");
            return false;
        }
        else if (event == EventData.Challenges.TRAINER && flag.equals("hover"))
        {
            logger.error("Exit path 4.");
            return false;
        }
        else if (event == EventData.Challenges.BOSS_TRAINER && flag.equals("hover"))
        {
            logger.error("Exit path 5.");
            return false;
        }
        else if (event == EventData.Challenges.PVP && flag.equals("hover"))
        {
            logger.error("Exit path 6.");
            return false;
        }
        else if (event == EventData.Forfeits.TRAINER && (flag.equals("hover") || flag.equals("reveal")))
        {
            logger.error("Exit path 7.");
            return false;
        }
        else if (event == EventData.Forfeits.BOSS_TRAINER && (flag.equals("hover") || flag.equals("reveal")))
        {
            logger.error("Exit path 8.");
            return false;
        }
        else if (event instanceof EventData.Spawns && flag.equals("reveal")) // TODO: TEST!
        {
            logger.error("Exit path 9!!");
            return false;
        }
        else if (event == EventData.Spawns.WORMHOLE && flag.equals("hover"))
        {
            logger.error("Exit path 10.");
            return false;
        }
        else if (event == EventData.Victories.TRAINER && (flag.equals("hover") || flag.equals("reveal")))
        {
            logger.error("Exit path 11.");
            return false;
        }
        else if (event == EventData.Victories.BOSS_TRAINER && (flag.equals("hover") || flag.equals("reveal")))
        {
            logger.error("Exit path 12.");
            return false;
        }
        else if (event == EventData.Victories.PVP && (flag.equals("hover") || flag.equals("reveal")))
        {
            logger.error("Exit path 13.");
            return false;
        }
        else if (event instanceof EventData.Others && (flag.equals("hover") || flag.equals("reveal"))) // TODO: TEST!
        {
            logger.error("Exit path 14!!");
            return false;
        }

        logger.error("Exit path is fallthrough.");
        return event.options().toLowerCase().contains(flag);
    }

    // Replaces all placeholders in a provided message.
    private static String getBroadcast(
            final String key, final Object pokemonObject, final Object pokemon2Object, final EntityPlayer playerEntity,
            final EntityPlayer player2Entity)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final String[] keySet = key.split("\\.");

        // Get the broadcast from the broadcast config, if it's there.
        String broadcast = broadcastsConfig.getNode((Object[]) keySet).getString();

        // Did we get a broadcast?
        if (broadcast != null)
            broadcast = TextSerializers.FORMATTING_CODE.replaceCodes(broadcast, '§');
        // We did not get a broadcast, return the provided key and make sure it's unformatted.
        else
        {
            logger.error("The following broadcast could not be found: §4" + key);
            return key;
        }

        // Set up some variables that we want to be able to access later.
        BlockPos position;
        Pokemon pokemon;

        // Do we have a Pokémon object? Replace Pokémon-specific placeholders.
        if (pokemonObject != null)
        {
            // Figure out what our received object is, exactly.
            if (pokemonObject instanceof EntityPixelmon || pokemonObject instanceof EntityWormhole)
            {
                // Make the entity a bit easier to access. It probably has more info than a Pokemon object would -- use it!
                Entity entity = (Entity) pokemonObject;

                // Get a position, and do a sanity check on it to work around possible entity removal issues.
                // (if both are zero, something might have broken -- we'll try getting the info from the player instead)
                position = entity.getPosition();
                if (!(position.getX() == 0 && position.getZ() == 0))
                {
                    // Get the Pokémon's biome, nicely formatted (spaces!) and all. Replace placeholder.
                    final String biome = getFormattedBiome(entity.getEntityWorld(), position);
                    broadcast = broadcast.replaceAll("(?i)%biome%", biome);

                    // Insert a world name.
                    broadcast = broadcast.replaceAll("(?i)%world%", entity.getEntityWorld().getWorldInfo().getWorldName());

                    // Insert coordinates.
                    broadcast = broadcast.replaceAll("(?i)%xpos%", String.valueOf(position.getX()));
                    broadcast = broadcast.replaceAll("(?i)%ypos%", String.valueOf(position.getY()));
                    broadcast = broadcast.replaceAll("(?i)%zpos%", String.valueOf(position.getZ()));
                }
                else
                {
                    logger.error("§6The event's Pokémon entity was removed from the world early!");
                    logger.error("§6We'll try to get missing info from the player. World info may look weird.");
                }

                // Extract a Pokemon object for later use, if possible.
                if (pokemonObject instanceof EntityPixelmon)
                    pokemon = ((EntityPixelmon) entity).getPokemonData();
                else // No more info to extract, here.
                    return broadcast;
            }
            else
                pokemon = (Pokemon) pokemonObject;

            // Insert the Pokémon's name.
            if (broadcast.toLowerCase().contains("%pokemon%"))
            {
                // See if the Pokémon is an egg. If it is, be extra careful and don't spoil the name.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                final String pokemonName;
                if (pokemon.isEgg())
                    pokemonName = getTranslation("placeholder.pokemon.is_egg");
                else if (pokemon.getFormEnum() == EnumAlolan.ALOLAN)
                    pokemonName = "Alolan " + pokemon.getSpecies().getLocalizedName();
                else
                    pokemonName = pokemon.getSpecies().getLocalizedName();

                // Proceed with insertion.
                broadcast = broadcast.replaceAll("(?i)%pokemon%", pokemonName);
            }

            // Insert IV percentage. If our Pokémon's an egg, be careful and avoid spoiling stuff.
            // FIXME: Could do with an option, or a cleaner way to make this all work.
            if (pokemon.isEgg())
            {
                broadcast =
                        broadcast.replaceAll("(?i)%ivpercent%", getTranslation("placeholder.ivpercent.is_egg"));
            }
            else
            {
                // Set up IVs and matching math. These are used everywhere.
                final IVStore IVs = pokemon.getIVs();
                final int totalIVs =
                        IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                                IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
                final int percentIVs = totalIVs * 100 / 186;

                // Return the percentage.
                broadcast = broadcast.replaceAll("(?i)%ivpercent%", String.valueOf(percentIVs) + '%');
            }

            // Insert the "placeholder.shiny" String, if applicable. Gotta be careful with eggs again.
            if (!pokemon.isEgg() && pokemon.isShiny())
                broadcast = broadcast.replaceAll("(?i)%shiny%", getTranslation("placeholder.shiny"));
            else
                broadcast = broadcast.replaceAll("(?i)%shiny%", "");

            // Rinse and repeat the above for a second Pokémon, if present.
            if (pokemon2Object != null)
            {
                // We've got a second Pokémon! See what type this one is.
                final Pokemon pokemon2;
                if (pokemon2Object instanceof EntityPixelmon)
                {
                    // Make this one easier to access, too. We'll need it.
                    EntityPixelmon pokemon2Entity = (EntityPixelmon) pokemon2Object;

                    // Extract a Pokemon object for later use.
                    pokemon2 = pokemon2Entity.getPokemonData();

                    // Get a position, and do a sanity check on it to work around possible entity removal issues.
                    // (if both are zero, something might have broken -- we'll try getting the info from the player instead)
                    position = pokemon2Entity.getPosition();
                    if (!(position.getX() == 0 && position.getZ() == 0))
                    {
                        // Get the Pokémon's biome, nicely formatted (spaces!) and all. Replace placeholder.
                        final String biome2 = getFormattedBiome(pokemon2Entity.getEntityWorld(), position);
                        broadcast = broadcast.replaceAll("(?i)%biome2%", biome2);

                        // Insert a world name.
                        broadcast = broadcast.replaceAll("(?i)%world2%", pokemon2Entity.getEntityWorld().getWorldInfo().getWorldName());

                        // Insert coordinates.
                        broadcast = broadcast.replaceAll("(?i)%xpos2%", String.valueOf(position.getX()));
                        broadcast = broadcast.replaceAll("(?i)%ypos2%", String.valueOf(position.getY()));
                        broadcast = broadcast.replaceAll("(?i)%zpos2%", String.valueOf(position.getZ()));
                    }
                }
                else
                    pokemon2 = (Pokemon) pokemon2Object;

                // Insert the Pokémon's name.
                if (broadcast.toLowerCase().contains("%pokemon2%"))
                {
                    // See if the Pokémon is an egg. If it is, be extra careful and don't spoil the name.
                    // FIXME: Could do with an option, or a cleaner way to make this all work.
                    final String pokemon2Name;
                    if (pokemon2.isEgg())
                        pokemon2Name = getTranslation("placeholder.pokemon.is_egg");
                    else if (pokemon2.getFormEnum() == EnumAlolan.ALOLAN)
                        pokemon2Name = "Alolan " + pokemon2.getSpecies().getLocalizedName();
                    else
                        pokemon2Name = pokemon2.getSpecies().getLocalizedName();

                    // Proceed with insertion.
                    broadcast = broadcast.replaceAll("(?i)%pokemon2%", pokemon2Name);
                }

                // Insert IV percentage. If our Pokémon's an egg, be careful and avoid spoiling stuff.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                if (pokemon2.isEgg())
                {
                    broadcast =
                            broadcast.replaceAll("(?i)%ivpercent2%", getTranslation("placeholder.ivpercent.is_egg"));
                }
                else
                {
                    // Set up IVs and matching math. These are used everywhere.
                    final IVStore IVs = pokemon2.getIVs();
                    final int totalIVs =
                            IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                                    IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
                    final int percentIVs = totalIVs * 100 / 186;

                    // Return the percentage.
                    broadcast = broadcast.replaceAll("(?i)%ivpercent2%", String.valueOf(percentIVs) + '%');
                }

                // Insert the "placeholder.shiny" String, if applicable. Gotta be careful with eggs again.
                if (!pokemon2.isEgg() && pokemon2.isShiny())
                    broadcast = broadcast.replaceAll("(?i)%shiny2%", getTranslation("placeholder.shiny"));
                else
                    broadcast = broadcast.replaceAll("(?i)%shiny2%", "");
            }
        }

        // Do we have a player entity? Replace player-specific placeholders as well as some that we might not have yet.
        if (playerEntity != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player%", playerEntity.getName());

            // Get the player's position. We prefer using the Pokémon's position, but if that fails this should catch it.
            position = playerEntity.getPosition();

            // Get the player's biome, nicely formatted (spaces!) and all. Replace placeholder if it still exists.
            final String biome = getFormattedBiome(playerEntity.getEntityWorld(), position);
            broadcast = broadcast.replaceAll("(?i)%biome%", biome);

            // Insert a world name if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%world%", playerEntity.getEntityWorld().getWorldInfo().getWorldName());

            // Insert coordinates if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%xpos%", String.valueOf(position.getX()));
            broadcast = broadcast.replaceAll("(?i)%ypos%", String.valueOf(position.getY()));
            broadcast = broadcast.replaceAll("(?i)%zpos%", String.valueOf(position.getZ()));
        }

        // Do we have a second player? Replace player-specific placeholders as well as some that we might not have yet.
        if (player2Entity != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player2%", player2Entity.getName());

            // Get the player's position. We prefer using the Pokémon's position, but if that fails this should catch it.
            position = player2Entity.getPosition();

            // Get the player's biome, nicely formatted (spaces!) and all. Replace placeholder if it still exists.
            final String biome2 = getFormattedBiome(player2Entity.getEntityWorld(), position);
            broadcast = broadcast.replaceAll("(?i)%biome2%", biome2);

            // Insert a world name if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%world2%", player2Entity.getEntityWorld().getWorldInfo().getWorldName());

            // Insert coordinates if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%xpos2%", String.valueOf(position.getX()));
            broadcast = broadcast.replaceAll("(?i)%ypos2%", String.valueOf(position.getY()));
            broadcast = broadcast.replaceAll("(?i)%zpos2%", String.valueOf(position.getZ()));
        }

        return broadcast;
    }

    //

    // Checks whether the provided toggle flags are turned on. Only returns false if all toggles are set and turned off.
    public static boolean checkToggleStatus(final EntityPlayer recipient, final String... flags)
    {
        // Loop through the provided set of flags.
        for (String flag : flags)
        {
            // Does the player have a flag set for this notification?
            if (recipient.getEntityData().getCompoundTag("pbToggles").hasKey(flag))
            {
                // Did we find a flag that's present and true? Exit out, we can show stuff!
                if (recipient.getEntityData().getCompoundTag("pbToggles").getBoolean(flag))
                    return true;
            }
            else // Default state for flags is true!
                return true;
        }

        // We hit this only if all passed flags were present, but returned false. (everything relevant turned off)
        return false;
    }

    // Gets a cleaned-up English name of the biome at the provided coordinates. Add spaces when there's multiple words.
    private static String getFormattedBiome(World world, BlockPos location)
    {
        // Grab the name. This compiles fine if the access transformer is loaded correctly, despite any errors.
        String biome = world.getBiomeForCoordsBody(location).biomeName;

        // Add a space in front of every capital letter after the first.
        int capitalCount = 0, iterator = 0;
        while (iterator < biome.length())
        {
            // Is there an upper case character at the checked location?
            if (Character.isUpperCase(biome.charAt(iterator)))
            {
                // Add to the pile.
                capitalCount++;

                // Did we get more than one capital letter on the pile?
                if (capitalCount > 1)
                {
                    // Look back: Was the previous character a space? If not, proceed with adding one.
                    if (biome.charAt(iterator - 1) != ' ')
                    {
                        // Add a space at the desired location.
                        biome = biome.substring(0, iterator) + ' ' + biome.substring(iterator);

                        // Up the main iterator so we do not repeat the check on the character we're at now.
                        iterator++;
                    }
                }
            }

            // Up the iterator for another go, if we're below length().
            iterator++;
        }

        return biome;
    }

    // Sets up a broadcast from the given info, with IV hovers thrown in in place of any placeholders.
    // FIXME: It may be a good idea to toggle off showIVs if we're showing off an egg. Need to think about this more.
    private static Text getHoverableLine(
            final String broadcast, final Object pokemonObject, final boolean isPresentTense, final boolean showIVs)
    {
        // Is our received object of the older EntityPixelmon type, or is it Pokemon?
        Pokemon pokemon =
                pokemonObject instanceof EntityPixelmon ? ((EntityPixelmon) pokemonObject).getPokemonData() : (Pokemon) pokemonObject;

        // We have at least one Pokémon, so start setup for this first one.
        final IVStore IVs = pokemon.getIVs();
        final int HPIV = IVs.get(StatsType.HP);
        final int attackIV = IVs.get(StatsType.Attack);
        final int defenseIV = IVs.get(StatsType.Defence);
        final int spAttIV = IVs.get(StatsType.SpecialAttack);
        final int spDefIV = IVs.get(StatsType.SpecialDefence);
        final int speedIV = IVs.get(StatsType.Speed);
        final BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
        final BigDecimal percentIVs = totalIVs.multiply(
                new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

        // Grab a growth string.
        final EnumGrowth growth = pokemon.getGrowth();
        final String sizeString = getTensedTranslation(isPresentTense, "hover.size." + growth.name().toLowerCase());

        // Get an IV composite StringBuilder.
        final StringBuilder ivsLine = new StringBuilder();
        String statString = "";
        int statValue = 0;
        for (int i = 0; i <= 5; i++)
        {
            switch (i)
            {
                case 0:
                {
                    statString = getTranslation("hover.status.hp");
                    statValue = HPIV;
                    break;
                }
                case 1:
                {
                    statString = getTranslation("hover.status.attack");
                    statValue = attackIV;
                    break;
                }
                case 2:
                {
                    statString = getTranslation("hover.status.defense");
                    statValue = defenseIV;
                    break;
                }
                case 3:
                {
                    statString = getTranslation("hover.status.special_attack");
                    statValue = spAttIV;
                    break;
                }
                case 4:
                {
                    statString = getTranslation("hover.status.special_defense");
                    statValue = spDefIV;
                    break;
                }
                case 5:
                {
                    statString = getTranslation("hover.status.speed");
                    statValue = speedIV;
                    break;
                }
            }

            if (statValue < 31)
                ivsLine.append(getTranslation("hover.status.below_max", statValue, statString));
            else
                ivsLine.append(getTranslation("hover.status.maxed_out", statValue, statString));

            if (i < 5)
                ivsLine.append(getTranslation("hover.status.separator"));
        }

        // Grab a gender string.
        final String genderString;
        switch (pokemon.getGender())
        {
            case Male:
                genderString = getTensedTranslation(isPresentTense, "hover.gender.male"); break;
            case Female:
                genderString = getTensedTranslation(isPresentTense, "hover.gender.female"); break;
            default:
                genderString = getTensedTranslation(isPresentTense, "hover.gender.none"); break;
        }

        // Get a nature and see which stats we get from it.
        final EnumNature nature = pokemon.getNature();
        final String natureString = getTranslation("hover.nature." + nature.name().toLowerCase());
        final String boostedStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).increasedStat);
        final String cutStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).decreasedStat);
        final String natureCompositeString;

        // Grab the value of the increased stat (could use either). If it's "None", we have a neutral nature type.
        if (nature.increasedStat.equals(StatsType.None))
        {
            natureCompositeString =
                    getTensedTranslation(isPresentTense, "hover.nature.balanced", natureString, boostedStat, cutStat);
        }
        else
        {
            natureCompositeString =
                    getTensedTranslation(isPresentTense, "hover.nature.special", natureString, boostedStat, cutStat);
        }

        // Populate a List. Every entry will be its own line. May be a bit hacky, but it's easy to work with.
        final List<String> hovers = new ArrayList<>();

        // NOTE: Not shown on spawn/challenge. Shows bogus values on these events, Pixelmon issue, retest at some point.
        if (showIVs)
        {
            hovers.add(getTranslation("hover.current_ivs"));
            hovers.add(getTranslation("hover.total_ivs", totalIVs, percentIVs));
            hovers.add(getTranslation("hover.status.line_start") + ivsLine.toString());
        }

        // Print a header, as well as fancy broadcasts for the size, the gender and the nature.
        hovers.add(getTranslation("hover.info"));
        hovers.add(sizeString);
        hovers.add(genderString);
        hovers.add(natureCompositeString);

        // If ability showing is on, also do just that.
        if (showAbilities)
        {
            if (pokemon.getAbility().getName().equals(pokemon.getBaseStats().abilities[2]))
            {
                hovers.add(getTensedTranslation(
                        isPresentTense, "hover.hidden_ability", pokemon.getAbility().getTranslatedName().getUnformattedText()));
            }
            else
            {
                hovers.add(getTensedTranslation(
                        isPresentTense, "hover.ability", pokemon.getAbility().getTranslatedName().getUnformattedText()));
            }
        }

        // Make a finalized broadcast that we can show, and add a hover. Return the whole thing.
        return Text.builder(broadcast)
                .onHover(TextActions.showText(Text.of(String.join("\n§r", hovers))))
                .build();
    }
}
