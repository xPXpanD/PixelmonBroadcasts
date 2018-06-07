package rs.expand.pixelmonbroadcasts.utilities;

// Local imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Remote imports.
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class PlaceholderMethods
{
    // Iterates through the online player list, and sends a broadcast to those with the right perms and toggle status.
    // This method also adds a hoverable IV spread if the "hover" option is set for this broadcast.
    public static void iterateAndSendBroadcast(
            String broadcast, final EntityPixelmon pokemon, final EntityPlayer player, final BlockPos location,
            final boolean hasHover, final boolean presentTense, final boolean showIVs, final String permission,
            final String... flags)
    {
        // If our broadcast has any placeholders inside, replace them with the matching value. Case-insensitive.
        // Do we require any coordinates?
        broadcast = broadcast.replaceAll("(?i)%xpos%", String.valueOf(location.getX()));
        broadcast = broadcast.replaceAll("(?i)%ypos%", String.valueOf(location.getY()));
        broadcast = broadcast.replaceAll("(?i)%zpos%", String.valueOf(location.getZ()));

        // Do we have a Pokémon entity? Replace Pokémon-specific placeholders.
        if (pokemon != null)
        {
            // Replace more placeholders.
            if (broadcast.toLowerCase().contains("%pokemon%"))
            {
                // See if the Pokémon is an egg. If it is, be extra careful and don't spoil the name.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                final String pokemonName =
                        pokemon.isEgg ? getTranslation("placeholder.pokemon.is_egg") : pokemon.getLocalizedName();

                // Insert the checked Pokémon name.
                broadcast = broadcast.replaceAll("(?i)%pokemon%", pokemonName);
            }

            // Also run some special logic for IV percentages. Same idea as with the above.
            if (showIVs && broadcast.toLowerCase().contains("%ivpercent%"))
            {
                // Grab the Pokémon's stats.
                final int HPIV = pokemon.stats.ivs.HP;
                final int attackIV = pokemon.stats.ivs.Attack;
                final int defenseIV = pokemon.stats.ivs.Defence;
                final int spAttIV = pokemon.stats.ivs.SpAtt;
                final int spDefIV = pokemon.stats.ivs.SpDef;
                final int speedIV = pokemon.stats.ivs.Speed;

                // Process them.
                final BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
                final BigDecimal percentIVs = totalIVs.multiply(
                        new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

                // See if the Pokémon is an egg. If it is, be extra careful and don't spoil the stats.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                final String pokemonIVs =
                        pokemon.isEgg ? getTranslation("placeholder.ivpercent.is_egg") : percentIVs.toString() + '%';

                // Apply.
                broadcast = broadcast.replaceAll("(?i)%ivpercent%", pokemonIVs);
            }

            // Replace situation-specific placeholders via an external method. Pass data from the Pokémon.
            broadcast = replaceNeutralPlaceholders(broadcast, pokemon.getEntityWorld(), location);
        }

        // Do we have a player entity? Replace player-specific placeholders.
        if (player != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player%", player.getName());

            // Did we not get sent a Pokémon? Try to get some data from the provided player, instead.
            if (pokemon == null)
            {
                // Replace situation-specific placeholders via an external method. Pass data from the player entity.
                broadcast = replaceNeutralPlaceholders(broadcast, player.getEntityWorld(), location);
            }
        }

        // Make a Text out of our broadcast, which we can either send directly or add a hover to, depending on options.
        final Text broadcastAsText;

        // If hovers are enabled, make the line hoverable.
        if (pokemon != null && hasHover)
            broadcastAsText = getHoverableLine(broadcast, pokemon, presentTense, showIVs);
        else
            broadcastAsText = Text.of(broadcast);

        // Sift through the online players.
        Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
        {
            // Does the iterated player have the needed notifier permission?
            if (recipient.hasPermission("pixelmonbroadcasts.notify." + permission))
            {
                // Does the iterated player want our broadcast? Send it if we get "true" returned.
                if (checkToggleStatus((EntityPlayer) recipient, flags))
                    recipient.sendMessage(broadcastAsText);
            }
        });
    }

    // Replaces placeholders that can have multiple sources (players, Pokémon).
    // Can be used for both players, but it's a bit cheeky -- it assumes that both players are in the same world/biome.
    private static String replaceNeutralPlaceholders(String broadcast, final World world, final BlockPos location)
    {
        // Insert a world name.
        broadcast = broadcast.replaceAll("(?i)%world(\\d*?)%", world.getWorldInfo().getWorldName());

        // Insert a biome. Same reasoning as above, for multiple players.
        if (broadcast.toLowerCase().contains("%biome%") || broadcast.toLowerCase().contains("%biome2%"))
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
                            biome = biome.substring(0, iterator) + ' ' + biome.substring(iterator, biome.length());

                            // Up the main iterator so we do not repeat the check on the character we're at now.
                            iterator++;
                        }
                    }
                }

                // Up the iterator for another go, if we're below length().
                iterator++;
            }

            // Apply.
            broadcast = broadcast.replaceAll("(?i)%biome(\\d*?)%", biome);
        }

        return broadcast;
    }

    // Takes a config String, and replaces any known placeholders with the proper replacements as many times as needed.
    public static String replacePlayer2Placeholders(
            String broadcast, final EntityPlayer player, final EntityPixelmon pokemon, final BlockPos location)
    {
        // If our broadcast has any placeholders inside, replace them with the matching value. Case-insensitive.
        // Do we require any coordinates?
        broadcast = broadcast.replaceAll("(?i)%xpos(\\d+)%", String.valueOf(location.getX()));
        broadcast = broadcast.replaceAll("(?i)%ypos(\\d+)%", String.valueOf(location.getY()));
        broadcast = broadcast.replaceAll("(?i)%zpos(\\d+)%", String.valueOf(location.getZ()));

        // Do we have a Pokémon entity? Replace Pokémon-specific placeholders.
        if (pokemon != null)
        {
            // Replace more placeholders.
            if (broadcast.matches(".*%(?i)pokemon(\\d+)%.*"))
            {
                // See if the Pokémon is an egg. If it is, be extra careful and don't spoil the name.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                final String pokemonName =
                        pokemon.isEgg ? getTranslation("placeholder.pokemon.is_egg") : pokemon.getLocalizedName();

                // Insert the checked Pokémon name.
                broadcast = broadcast.replaceAll("(?i)%pokemon(\\d+)%", pokemonName);
            }

            // Also run some special logic for IV percentages. Same idea as with the above.
            if (broadcast.matches(".*%(?i)ivpercent(\\d+)%.*"))
            {
                // Grab the Pokémon's stats.
                final int HPIV = pokemon.stats.ivs.HP;
                final int attackIV = pokemon.stats.ivs.Attack;
                final int defenseIV = pokemon.stats.ivs.Defence;
                final int spAttIV = pokemon.stats.ivs.SpAtt;
                final int spDefIV = pokemon.stats.ivs.SpDef;
                final int speedIV = pokemon.stats.ivs.Speed;

                // Process them.
                final BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
                final BigDecimal percentIVs = totalIVs.multiply(
                        new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

                // See if the Pokémon is an egg. If it is, be extra careful and don't spoil the stats.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                final String pokemonIVs =
                        pokemon.isEgg ? getTranslation("placeholder.ivpercent.is_egg") : percentIVs.toString() + '%';

                // Apply.
                broadcast = broadcast.replaceAll("(?i)%ivpercent(\\d+)%", pokemonIVs);
            }

            // Replace situation-specific placeholders via an external method. Pass data from the Pokémon.
            broadcast = replaceNeutralPlaceholders(broadcast, pokemon.getEntityWorld(), location);
        }

        // Do we have a player entity? Replace player-specific placeholders.
        if (player != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player(\\d+)%", player.getName());

            // Did we not get sent a Pokémon? Try to get some data from the provided player, instead.
            if (pokemon == null)
            {
                // Replace situation-specific placeholders via an external method. Pass data from the player entity.
                broadcast = replaceNeutralPlaceholders(broadcast, player.getEntityWorld(), location);
            }
        }

        // Send back the final formatted broadcast.
        return broadcast;
    }

    // Sets up a broadcast from the given info, with IV hovers thrown in in place of any placeholders.
    // FIXME: It may be a good idea to toggle off showIVs if we're showing off an egg. Need to think about this more.
    private static Text getHoverableLine(
            final String broadcast, final EntityPixelmon pokemon, final boolean presentTense, final boolean showIVs)
    {
        // We have at least one Pokémon, so start setup for this first one.
        final int HPIV = pokemon.stats.ivs.HP;
        final int attackIV = pokemon.stats.ivs.Attack;
        final int defenseIV = pokemon.stats.ivs.Defence;
        final int spAttIV = pokemon.stats.ivs.SpAtt;
        final int spDefIV = pokemon.stats.ivs.SpDef;
        final int speedIV = pokemon.stats.ivs.Speed;
        final BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
        final BigDecimal percentIVs = totalIVs.multiply(
                new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

        // Grab a growth string.
        final EnumGrowth growth = pokemon.getGrowth();
        final String sizeString = getTranslation("hover.status.line_start") +
                getTensedTranslation(presentTense, "hover.size." + growth.name().toLowerCase());

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
            {
                genderString = getTranslation("hover.status.line_start") +
                        getTensedTranslation(presentTense, "hover.gender.male");

                break;
            }
            case Female:
            {
                genderString = getTranslation("hover.status.line_start") +
                        getTensedTranslation(presentTense, "hover.gender.female");

                break;
            }
            default:
            {
                genderString = getTranslation("hover.status.line_start") +
                        getTensedTranslation(presentTense, "hover.gender.none");

                break;
            }
        }

        // Get a nature and see which stats we get from it.
        final EnumNature nature = pokemon.getNature();
        final String natureString = getTranslation("hover.nature." + nature.name().toLowerCase());
        final String boostedStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).increasedStat);
        final String cutStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).decreasedStat);

        // Actually create the nature String.
        final String natureCompositeString;

        // Grab the value of the increased stat (could use either). If it's "None", we have a neutral nature type.
        if (nature.increasedStat.equals(StatsType.None))
        {
            natureCompositeString = getTranslation("hover.status.line_start") +
                    getTensedTranslation(presentTense, "hover.nature.balanced", natureString, boostedStat, cutStat);
        }
        else
        {
            natureCompositeString = getTranslation("hover.status.line_start") +
                    getTensedTranslation(presentTense, "hover.nature.special", natureString, boostedStat, cutStat);
        }

        // Populate a List. Every entry will be its own line. May be a bit hacky, but it's easy to work with.
        final List<String> hovers = new ArrayList<>();

        // FIXME: Not shown on spawn/challenge. Shows bogus values on these events, Pixelmon bug. Enable when fixed.
        if (showIVs)
        {
            hovers.add(getTranslation("hover.current_ivs"));
            hovers.add(getTranslation("hover.status.line_start") + getTranslation("hover.total_ivs", totalIVs, percentIVs));
            hovers.add(getTranslation("hover.status.line_start") + ivsLine.toString());
        }

        // Print a header, as well as fancy broadcasts for the size, the gender and the nature.
        hovers.add(getTranslation("hover.info"));
        hovers.add(sizeString);
        hovers.add(genderString);
        hovers.add(natureCompositeString);

        // Make a finalized broadcast that we can show, and add a hover. Return the whole thing.
        return Text.builder(broadcast)
                .onHover(TextActions.showText(Text.of(String.join("\n§r", hovers))))
                .build();
    }

    // Inserts the correct tense into lang keys that might have multiple tenses. Returns the translation.
    private static String getTensedTranslation(final boolean presentTense, final String key, final Object... params)
    {
        // Set up a String to translate and then return.
        final String tensedKey;

        // Splits our input key, adds the correct tense at a constant known location, and then pieces it back together.
        if (presentTense)
            tensedKey = key.substring(0, 6) + "present." + key.substring(6);
        else
            tensedKey = key.substring(0, 6) + "past." + key.substring(6);

        // Send back the translation of our new freshly-tensed key.
        return getTranslation(tensedKey, params);
    }

    // Get translated names for a given nature's positive and negative stats from the lang.
    private static String getTranslatedNatureStat(StatsType stat)
    {
        switch(stat)
        {
            case Attack:
                return getTranslation("hover.status.attack");
            case Defence:
                return getTranslation("hover.status.defense");
            case SpecialAttack:
                return getTranslation("hover.status.special_attack");
            case SpecialDefence:
                return getTranslation("hover.status.special_defense");
            case Speed:
                return getTranslation("hover.status.speed");
            default:
                return getTranslation("hover.status.none");
        }
    }
}
