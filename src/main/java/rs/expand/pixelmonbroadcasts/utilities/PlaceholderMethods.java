package rs.expand.pixelmonbroadcasts.utilities;

// Local imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

// Remote imports.
import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.statSeparator;
import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.*;

public class PlaceholderMethods
{
    // Iterates through the online player list, and sends a message to those with the right perms and toggle status.
    // This method also adds a hoverable IV spread if an %ivhover%/%ivhover2% placeholder is present.
    // The logic for this is huge and the location is a bit awkward, but it can only be done here and is optional.
    public static void iterateAndSendEventMessage(
            String message, final String permission, final String flag, final NBTTagCompound nbt, final NBTTagCompound... nbt2)
    {
        // Set up some regularly-accessed bools.
        final boolean player1PlaceholderPresent = message.toLowerCase().contains("%ivhover%");
        final boolean player2PlaceholderPresent = message.toLowerCase().contains("%ivhover2%");

        // Set up a Text that we can store our first placeholder message in, if applicable.
        Text player1Message;

        // Pass the incoming message through yet another placeholder-swapping stage if a valid one is found.
        if (player1PlaceholderPresent || player2PlaceholderPresent)
        {
            // Gets back the whole message with placeholders nuked, a hot word and
            player1Message = getIVHover(message, nbt);

            /*// We have another Pokémon.
            if (nbt.size() > 1)
                temp2 = getIVHover(editableMessage, nbt.get(1));*/
        }
        else
            player1Message = null;

        // Sift through the online players.
        Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
        {
            // Does the iterated player have the needed notifier permission?
            if (recipient.hasPermission("pixelmonbroadcasts.notify." + permission))
            {
                // Does the iterated player have the message enabled? Send it if we get "true" returned.
                if (checkToggleStatus((EntityPlayerMP) recipient, flag))
                {
                    if (!player1PlaceholderPresent && !player2PlaceholderPresent)
                        recipient.sendMessage(Text.of(message));
                    else if (player1Message != null)
                        recipient.sendMessage(player1Message);
                    else
                        recipient.sendMessage(Text.of("ERROR: GOT NULL"));
                }
            }
        });
    }

    // Sets up a message from the given info, with IV hovers thrown in in place of any placeholders.
    private static Text getIVHover(final String message, final NBTTagCompound nbt)
    {
        if (nbt != null)
        {
            // We have at least one Pokémon, so start setup for this first one.
            final int HPIV = nbt.getInteger(NbtKeys.IV_HP);
            final int attackIV = nbt.getInteger(NbtKeys.IV_ATTACK);
            final int defenseIV = nbt.getInteger(NbtKeys.IV_DEFENCE);
            final int spAttIV = nbt.getInteger(NbtKeys.IV_SP_ATT);
            final int spDefIV = nbt.getInteger(NbtKeys.IV_SP_DEF);
            final int speedIV = nbt.getInteger(NbtKeys.IV_SPEED);
            final BigDecimal totalIVs = BigDecimal.valueOf(HPIV + attackIV + defenseIV + spAttIV + spDefIV + speedIV);
            final BigDecimal percentIVs = totalIVs.multiply(
                    new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

            // Grab a growth string.
            final String sizeString;
            switch (nbt.getInteger(NbtKeys.GROWTH))
            {
                case 0:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.pygmy"); break;
                case 1:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.runt"); break;
                case 2:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.small"); break;
                case 3:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.ordinary"); break;
                case 4:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.huge"); break;
                case 5:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.giant"); break;
                case 6:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.enormous"); break;
                case 7:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.ginormous"); break; // NOW with fancy underlining!
                case 8:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.microscopic"); break; // NOW with fancy italicization!
                default:
                    sizeString = getFormattedTranslation("hovers.messages.sizes.unknown");
            }

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
                        statString = getFormattedTranslation("hovers.text.stats.hp");
                        statValue = HPIV;
                        break;
                    }
                    case 1:
                    {
                        statString = getFormattedTranslation("hovers.text.stats.attack");
                        statValue = attackIV;
                        break;
                    }
                    case 2:
                    {
                        statString = getFormattedTranslation("hovers.text.stats.defense");
                        statValue = defenseIV;
                        break;
                    }
                    case 3:
                    {
                        statString = getFormattedTranslation("hovers.text.stats.special_attack");
                        statValue = spAttIV;
                        break;
                    }
                    case 4:
                    {
                        statString = getFormattedTranslation("hovers.text.stats.special_defense");
                        statValue = spDefIV;
                        break;
                    }
                    case 5:
                    {
                        statString = getFormattedTranslation("hovers.text.stats.speed");
                        statValue = speedIV;
                        break;
                    }
                }

                if (i < 5)
                {
                    if (statValue < 31)
                        ivsLine.append(getFormattedTranslation("hovers.text.stats.below_max", statValue, statString, statSeparator));
                    else
                        ivsLine.append(getFormattedTranslation("hovers.text.stats.maxed_out", statValue, statString, statSeparator));
                }
                else
                {
                    if (statValue < 31)
                        ivsLine.append(getFormattedTranslation("hovers.text.stats.below_max", statValue, statString, ""));
                    else
                        ivsLine.append(getFormattedTranslation("hovers.text.stats.maxed_out", statValue, statString, ""));
                }
            }

            // Grab a gender string.
            final String genderString;
            switch (nbt.getInteger(NbtKeys.GENDER))
            {
                case 0:
                    genderString = getFormattedTranslation("hovers.messages.genders.male"); break;
                case 1:
                    genderString = getFormattedTranslation("hovers.messages.genders.female"); break;
                default:
                    genderString = getFormattedTranslation("hovers.messages.genders.none"); break;
            }

            // Set up the nature and see which stats we got.
            final String natureString;
            final int natureNumber = nbt.getInteger(NbtKeys.NATURE);
            final String boostedStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(natureNumber).increasedStat);
            final String cutStat = getTranslatedNatureStat(EnumNature.getNatureFromIndex(natureNumber).decreasedStat);

            // Get a translated name for the nature from the lang.
            switch (natureNumber)
            {
                case 0:
                    natureString = getFormattedTranslation("hovers.text.natures.hardy"); break;
                case 1:
                    natureString = getFormattedTranslation("hovers.text.natures.serious"); break;
                case 2:
                    natureString = getFormattedTranslation("hovers.text.natures.docile"); break;
                case 3:
                    natureString = getFormattedTranslation("hovers.text.natures.bashful"); break;
                case 4:
                    natureString = getFormattedTranslation("hovers.text.natures.quirky"); break;
                case 5:
                    natureString = getFormattedTranslation("hovers.text.natures.lonely"); break;
                case 6:
                    natureString = getFormattedTranslation("hovers.text.natures.brave"); break;
                case 7:
                    natureString = getFormattedTranslation("hovers.text.natures.adamant"); break;
                case 8:
                    natureString = getFormattedTranslation("hovers.text.natures.naughty"); break;
                case 9:
                    natureString = getFormattedTranslation("hovers.text.natures.bold"); break;
                case 10:
                    natureString = getFormattedTranslation("hovers.text.natures.relaxed"); break;
                case 11:
                    natureString = getFormattedTranslation("hovers.text.natures.impish"); break;
                case 12:
                    natureString = getFormattedTranslation("hovers.text.natures.lax"); break;
                case 13:
                    natureString = getFormattedTranslation("hovers.text.natures.timid"); break;
                case 14:
                    natureString = getFormattedTranslation("hovers.text.natures.hasty"); break;
                case 15:
                    natureString = getFormattedTranslation("hovers.text.natures.jolly"); break;
                case 16:
                    natureString = getFormattedTranslation("hovers.text.natures.naive"); break;
                case 17:
                    natureString = getFormattedTranslation("hovers.text.natures.modest"); break;
                case 18:
                    natureString = getFormattedTranslation("hovers.text.natures.mild"); break;
                case 19:
                    natureString = getFormattedTranslation("hovers.text.natures.quiet"); break;
                case 20:
                    natureString = getFormattedTranslation("hovers.text.natures.rash"); break;
                case 21:
                    natureString = getFormattedTranslation("hovers.text.natures.calm"); break;
                case 22:
                    natureString = getFormattedTranslation("hovers.text.natures.gentle"); break;
                case 23:
                    natureString = getFormattedTranslation("hovers.text.natures.sassy"); break;
                case 24:
                    natureString = getFormattedTranslation("hovers.text.natures.careful"); break;
                default:
                    natureString = getFormattedTranslation("hovers.text.natures.unknown"); break;
            }

            // Do the setup for our nature String separately, as it's a bit more involved.
            final String natureCompositeString;
            if (nbt.getInteger(NbtKeys.NATURE) >= 0 && nbt.getInteger(NbtKeys.NATURE) <= 4)
            {
                natureCompositeString =
                        getFormattedTranslation("hovers.messages.natures.balanced", natureString, boostedStat, cutStat);
            }
            else if (nbt.getInteger(NbtKeys.NATURE) >= 5 && nbt.getInteger(NbtKeys.NATURE) <= 24)
            {
                natureCompositeString =
                        getFormattedTranslation("hovers.messages.natures.special", natureString, boostedStat, cutStat);
            }
            else
            {
                natureCompositeString =
                        getFormattedTranslation("hovers.messages.natures.unknown", natureString, boostedStat, cutStat);
            }

            // Populate a List. Every entry will be its own line. May be a bit hacky, but it'll do.
            final List<String> hovers = new ArrayList<>();
            hovers.add(getFormattedTranslation("hovers.messages.current_ivs"));
            hovers.add(getFormattedTranslation("hovers.messages.total_ivs", totalIVs, percentIVs));
            hovers.add(getFormattedTranslation("hovers.text.stats.line_start") + " " + ivsLine.toString());
            hovers.add(getFormattedTranslation("hovers.messages.extra_info"));
            hovers.add(sizeString);
            hovers.add(genderString);
            hovers.add(natureCompositeString);

            // Make a finalized message that we can show, and add a hover. Return the whole thing.
            return Text.builder(message.replaceAll("(?i)%ivhover%", percentIVs.toString()))
                    .onHover(TextActions.showText(Text.of(String.join("\n§r", hovers))))
                    .build();

            /*// Deserialize the message into a String, then swap out the placeholder with what should be there. Rough.
            String deserializedText = message.toString();

            // Put every String in our ArrayList on its own line, and reset formatting. Deserialize for hard insertion.
            String insertableHover = Text.builder(percentIVs.toString())
                    .onHover(TextActions.showText(Text.of(String.join("\n§r", hovers))))
                    .build().toString();

            // Insert the deserialized hover into our main text.
            String placeholderSwappedString = deserializedText.replaceAll("(?i)%ivhover%", insertableHover);

            // Reserialize the bashed-together Texts, and return for printing.
            Text returnable = Text.builder(placeholderSwappedString).toText();
            printBasicMessage("Bashed text: " + returnable.toString());
            return returnable;*/
        }
        else return null;
    }

    // Get translated names for a given nature's positive and negative stats from the lang.
    private static String getTranslatedNatureStat(StatsType stat)
    {
        switch(stat)
        {
            case Attack:
                return getFormattedTranslation("hovers.text.stats.attack");
            case Defence:
                return getFormattedTranslation("hovers.text.stats.defense");
            case SpecialAttack:
                return getFormattedTranslation("hovers.text.stats.special_attack");
            case SpecialDefence:
                return getFormattedTranslation("hovers.text.stats.special_defense");
            case Speed:
                return getFormattedTranslation("hovers.text.stats.speed");
            default: // Should not be reachable.
                return "ERROR";
        }
    }

    // Takes a config String, and replaces any known placeholders with the proper replacements as many times as needed.
    public static String replacePlaceholders(
            String message, final String playerName, final EntityPixelmon pokemon, final BlockPos location)
    {
        // If our message has any placeholders inside, replace them with the provided replacement String.
        // Case-insensitive. If a player cannot be provided, we receive a null object and handle it here.
        if (message.toLowerCase().contains("%pokemon%"))
            message = message.replaceAll("(?i)%pokemon%", pokemon.getLocalizedName());
        if (message.toLowerCase().contains("%world%"))
            message = message.replaceAll("(?i)%world%", pokemon.getEntityWorld().getWorldInfo().getWorldName());
        if (message.toLowerCase().contains("%xpos%"))
            message = message.replaceAll("(?i)%xpos%", String.valueOf(location.getX()));
        if (message.toLowerCase().contains("%ypos%"))
            message = message.replaceAll("(?i)%ypos%", String.valueOf(location.getY()));
        if (message.toLowerCase().contains("%zpos%"))
            message = message.replaceAll("(?i)%zpos%", String.valueOf(location.getZ()));

        // Run some special logic for biome names. This is a bit more involved, so we put the logic here.
        if (message.toLowerCase().contains("%biome%"))
        {
            // Grab the name. This compiles fine if the access transformer is loaded correctly, despite any errors.
            final String biome = pokemon.getEntityWorld().getBiomeForCoordsBody(location).biomeName;
            //final String biome = net.minecraft.util.text.translation.I18n.translateToLocalFormatted(basicBiome);

            // Apply.
            message = message.replaceAll("(?i)%biome%", biome);
        }

        // Also run some special logic for IV percentages. Same idea as with the above.
        if (message.toLowerCase().contains("%ivpercent%"))
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

            // Apply.
            message = message.replaceAll("(?i)%ivpercent%", percentIVs.toString());
        }

        // We pass null for events that can't use a player variable, so let's check for that here.
        if (playerName != null && message.toLowerCase().contains("%player%"))
            message = message.replaceAll("(?i)%player%", playerName);

        // Send back the final formatted message.
        return message;
    }

    // A clone of the above, used for events that have another player.
    public static String replaceAltPlayerPlaceholders(
            String message, final String playerName, final EntityPixelmon pokemon, final BlockPos location)
    {
        // If our message has any placeholders inside, replace them with the provided replacement String.
        // Case-insensitive. If a player cannot be provided, we receive a null object and handle it here.
        if (message.toLowerCase().contains("%pokemon2%"))
            message = message.replaceAll("(?i)%pokemon2%", pokemon.getLocalizedName());
        if (message.toLowerCase().contains("%world2%"))
            message = message.replaceAll("(?i)%world2%", pokemon.getEntityWorld().getWorldInfo().getWorldName());
        if (message.toLowerCase().contains("%xpos2%"))
            message = message.replaceAll("(?i)%xpos2%", String.valueOf(location.getX()));
        if (message.toLowerCase().contains("%ypos2%"))
            message = message.replaceAll("(?i)%ypos2%", String.valueOf(location.getY()));
        if (message.toLowerCase().contains("%zpos2%"))
            message = message.replaceAll("(?i)%zpos2%", String.valueOf(location.getZ()));

        // Run some special logic for biome names. This is a bit more involved, so we put the logic here.
        if (message.toLowerCase().contains("%biome2%"))
        {
            // Grab the name. This compiles fine if the access transformer is loaded correctly, despite any errors.
            final String biome = pokemon.getEntityWorld().getBiomeForCoordsBody(location).biomeName;
            //final String biome = net.minecraft.util.text.translation.I18n.translateToLocalFormatted(basicBiome);

            // Apply.
            message = message.replaceAll("(?i)%biome2%", biome);
        }

        // Also run some special logic for IV percentages. Same idea as with the above.
        if (message.toLowerCase().contains("%ivpercent2%"))
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

            // Apply.
            message = message.replaceAll("(?i)%ivpercent2%", percentIVs.toString());
        }

        // We pass null for events that can't use a player variable, so let's check for that here.
        if (playerName != null && message.toLowerCase().contains("%player2%"))
            message = message.replaceAll("(?i)%player2%", playerName);

        // Send back the final formatted message.
        return message;
    }
}
