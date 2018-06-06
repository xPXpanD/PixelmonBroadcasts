package rs.expand.pixelmonbroadcasts.utilities;

// Local imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Remote imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.statLineStart;
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.statSeparator;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class PlaceholderMethods
{
    // Iterates through the online player list, and sends a message to those with the right perms and toggle status.
    // This method also adds a hoverable IV spread if the "hover" option is set for this message.
    public static void iterateAndSendEventMessage(
            final String message, final EntityPixelmon pokemon, final boolean hasHover, final boolean presentTense,
            final boolean showIVs, final String permission, final String... flags)
    {
        // Make a Text out of our message, which we can either send directly or add a hover to, depending on options.
        Text finalMessage;

        // If the "hover" option is set, add the hover to our Text.
        if (hasHover)
            finalMessage = getHoverableLine(message, pokemon, presentTense, showIVs);
        else
            finalMessage = Text.of(message);

        // Sift through the online players.
        Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
        {
            // Does the iterated player have the needed notifier permission?
            if (recipient.hasPermission("pixelmonbroadcasts.notify." + permission))
            {
                // Does the iterated player want our message? Send it if we get "true" returned.
                if (checkToggleStatus((EntityPlayerMP) recipient, flags))
                    recipient.sendMessage(finalMessage);
            }
        });
    }

    // Sets up a message from the given info, with IV hovers thrown in in place of any placeholders.
    // FIXME: It may be a good idea to toggle off showIVs if we're showing off an egg. Need to think about this.
    private static Text getHoverableLine(
            final String message, final EntityPixelmon pokemon, final boolean presentTense, final boolean showIVs)
    {
        if (pokemon != null)
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
            final String sizeString = statLineStart +
                    getTensedTranslation(presentTense, "hovers.messages.sizes." + growth.name().toLowerCase());

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
                        statString = getTranslation("hovers.text.stats.hp");
                        statValue = HPIV;
                        break;
                    }
                    case 1:
                    {
                        statString = getTranslation("hovers.text.stats.attack");
                        statValue = attackIV;
                        break;
                    }
                    case 2:
                    {
                        statString = getTranslation("hovers.text.stats.defense");
                        statValue = defenseIV;
                        break;
                    }
                    case 3:
                    {
                        statString = getTranslation("hovers.text.stats.special_attack");
                        statValue = spAttIV;
                        break;
                    }
                    case 4:
                    {
                        statString = getTranslation("hovers.text.stats.special_defense");
                        statValue = spDefIV;
                        break;
                    }
                    case 5:
                    {
                        statString = getTranslation("hovers.text.stats.speed");
                        statValue = speedIV;
                        break;
                    }
                }


                if (statValue < 31)
                    ivsLine.append(getTranslation("hovers.text.stats.below_max", statValue, statString));
                else
                    ivsLine.append(getTranslation("hovers.text.stats.maxed_out", statValue, statString));

                if (i < 5)
                    ivsLine.append(statSeparator);
            }

            // Grab a gender string.
            final String genderString;
            switch (pokemon.getGender())
            {
                case Male:
                {
                    genderString = statLineStart +
                            getTensedTranslation(presentTense, "hovers.messages.genders.male");
                    break;
                }
                case Female:
                {
                    genderString = statLineStart +
                            getTensedTranslation(presentTense, "hovers.messages.genders.female");
                    break;
                }
                default:
                    genderString = statLineStart +
                            getTensedTranslation(presentTense, "hovers.messages.genders.none");
                    break;
            }

            // Get a nature and see which stats we get from it.
            final EnumNature nature = pokemon.getNature();
            final String natureString = getTranslation("hovers.text.natures." + nature.name().toLowerCase());
            final String boostedStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).increasedStat);
            final String cutStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).decreasedStat);

            // Actually create the nature String.
            final String natureCompositeString;

            // Grab the value of the increased stat (could use either). If it's "None", we have a neutral nature type.
            if (nature.increasedStat.equals(StatsType.None))
            {
                natureCompositeString = statLineStart + getTensedTranslation(
                        presentTense, "hovers.messages.natures.balanced", natureString, boostedStat, cutStat);
            }
            else
            {
                natureCompositeString = statLineStart + getTensedTranslation(
                        presentTense, "hovers.messages.natures.special", natureString, boostedStat, cutStat);
            }

            // Populate a List. Every entry will be its own line. May be a bit hacky, but it's easy to work with.
            final List<String> hovers = new ArrayList<>();

            // FIXME: Only shown on defeat/hatch. Shows bogus values on other events, Pixelmon bug. Enable when fixed.
            if (showIVs)
            {
                hovers.add(getTranslation("hovers.messages.current_ivs"));
                hovers.add(statLineStart + getTranslation("hovers.messages.total_ivs", totalIVs, percentIVs));
                hovers.add(statLineStart + ivsLine.toString());
            }

            // Print a header, as well as fancy messages for the size, the gender and the nature.
            hovers.add(getTranslation("hovers.messages.info"));
            hovers.add(sizeString);
            hovers.add(genderString);
            hovers.add(natureCompositeString);

            // Make a finalized message that we can show, and add a hover. Return the whole thing.
            return Text.builder(message)
                    .onHover(TextActions.showText(Text.of(String.join("\n§r", hovers))))
                    .build();
        }
        else return null;
    }

    // Inserts the correct tense into lang keys that might have multiple tenses. Returns the translation.
    private static String getTensedTranslation(final boolean presentTense, final String key, final Object... params)
    {
        // Set up a String to translate and then return.
        final String tensedKey;

        // Splits our input key, adds the correct tense at a constant known location, and then pieces it back together.
        if (presentTense)
            tensedKey = key.substring(0, 16) + "present_tense." + key.substring(16);
        else
            tensedKey = key.substring(0, 16) + "past_tense." + key.substring(16);

        // Send back the translation of our new freshly-tensed key.
        return getTranslation(tensedKey, params);
    }

    // Get translated names for a given nature's positive and negative stats from the lang.
    private static String getTranslatedNatureStat(StatsType stat)
    {
        switch(stat)
        {
            case Attack:
                return getTranslation("hovers.text.stats.attack");
            case Defence:
                return getTranslation("hovers.text.stats.defense");
            case SpecialAttack:
                return getTranslation("hovers.text.stats.special_attack");
            case SpecialDefence:
                return getTranslation("hovers.text.stats.special_defense");
            case Speed:
                return getTranslation("hovers.text.stats.speed");
            default: // Should not be reachable.
                return "ERROR";
        }
    }

    // Takes a config String, and replaces any known placeholders with the proper replacements as many times as needed.
    public static String replacePlaceholders(
            String message, final String playerName, final boolean showIVs, final boolean checkOtherPlayer,
            final EntityPixelmon pokemon, final BlockPos location)
    {
        // Insert a "2" into placeholders when we were told to check another player.
        String insert = "";
        if (checkOtherPlayer)
            insert = "2";

        // If our message has any placeholders inside, replace them with the provided replacement String.
        // Case-insensitive, and null objects are automatically ignored.
        if (message.toLowerCase().contains("%xpos" + insert + "%"))
            message = message.replaceAll("(?i)%xpos" + insert + "%", String.valueOf(location.getX()));
        if (message.toLowerCase().contains("%ypos" + insert + "%"))
            message = message.replaceAll("(?i)%ypos" + insert + "%", String.valueOf(location.getY()));
        if (message.toLowerCase().contains("%zpos" + insert + "%"))
            message = message.replaceAll("(?i)%zpos" + insert + "%", String.valueOf(location.getZ()));

        // Do we have a Pokémon entity?
        if (pokemon != null)
        {
            // Replace more placeholders.
            if (message.toLowerCase().contains("%pokemon" + insert + "%"))
            {
                // See if the Pokémon is an egg. If it is, be extra careful and don't spoil the name.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                final String pokemonName =
                        pokemon.isEgg ? getTranslation("universal.placeholders.pokemon.is_egg") : pokemon.getLocalizedName();

                // Insert the checked Pokémon name.
                message = message.replaceAll("(?i)%pokemon" + insert + "%", pokemonName);
            }
            if (message.toLowerCase().contains("%world" + insert + "%"))
            {
                message = message.replaceAll(
                        "(?i)%world" + insert + "%", pokemon.getEntityWorld().getWorldInfo().getWorldName());
            }

            // Run some special logic for biome names. This is a bit more involved, so we put the logic here.
            if (message.toLowerCase().contains("%biome" + insert + "%"))
            {
                // Grab the name. This compiles fine if the access transformer is loaded correctly, despite any errors.
                String biome = pokemon.getEntityWorld().getBiomeForCoordsBody(location).biomeName;

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
                message = message.replaceAll("(?i)%biome" + insert + "%", biome);
            }

            // Also run some special logic for IV percentages. Same idea as with the above.
            if (showIVs && message.toLowerCase().contains("%ivpercent" + insert + "%"))
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
                        pokemon.isEgg ? getTranslation("universal.placeholders.ivpercent.is_egg") : percentIVs.toString() + '%';

                // Insert the checked IV percentage.

                // Apply.
                message = message.replaceAll("(?i)%ivpercent" + insert + "%", pokemonIVs);
            }
        }

        // Were we given a valid player name?
        if (playerName != null && message.toLowerCase().contains("%player" + insert + "%"))
            message = message.replaceAll("(?i)%player" + insert + "%", playerName);

        // Send back the final formatted message.
        return message;
    }
}
