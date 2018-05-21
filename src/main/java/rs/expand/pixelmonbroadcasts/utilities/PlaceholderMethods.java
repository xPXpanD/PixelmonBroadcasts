package rs.expand.pixelmonbroadcasts.utilities;

import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import java.math.BigDecimal;
import java.util.ArrayList;

import static rs.expand.pixelmonbroadcasts.utilities.PrintingMethods.checkToggleStatus;

public class PlaceholderMethods
{
    // Iterates through the online player list, and sends a message to those with the right perms and toggle status.
    // This method also adds a hoverable IV spread if an %ivhover%/%ivhover2% placeholder is present.
    // The logic for this is huge and the location is a bit awkward, but it can only be done here and is optional.
    public static void iterateAndSendEventMessage(
            String message, final String permission, final String flag, final ArrayList<NBTTagCompound> nbt)
    {
        // Create a LiteralText object that we can add hover actions to, if need be.
        Text finalMessage = Text.of(message);

        // Set up some regularly-accessed bools.
        final boolean player1PlaceholderPresent = message.toLowerCase().contains("%ivhover%");
        final boolean player2PlaceholderPresent = message.toLowerCase().contains("%ivhover2%");

        // Pass the incoming message through yet another placeholder-swapping stage if a valid one is found.
        if (player1PlaceholderPresent || player2PlaceholderPresent)
        {
            // We have at least one Pokémon, so start setup for this first one.
            final int player1HPIV = nbt.get(0).getInteger(NbtKeys.IV_HP);
            final int player1AttackIV = nbt.get(0).getInteger(NbtKeys.IV_ATTACK);
            final int player1DefenseIV = nbt.get(0).getInteger(NbtKeys.IV_DEFENCE);
            final int player1SpAttIV = nbt.get(0).getInteger(NbtKeys.IV_SP_ATT);
            final int player1SpDefIV = nbt.get(0).getInteger(NbtKeys.IV_SP_DEF);
            final int player1SpeedIV = nbt.get(0).getInteger(NbtKeys.IV_SPEED);

            final BigDecimal player1TotalIVs = BigDecimal.valueOf(
                    player1HPIV + player1AttackIV + player1DefenseIV + player1SpAttIV + player1SpDefIV + player1SpeedIV);
            final BigDecimal player1PercentIVs = player1TotalIVs.multiply(
                    new BigDecimal("100")).divide(new BigDecimal("186"), 2, BigDecimal.ROUND_HALF_UP);

            if (player1HPIV > 30)
                ivs1 = String.valueOf("§o") + ivs1;
            if (player1AttackIV > 30)
                ivs2 = String.valueOf("§o") + ivs2;
            if (player1DefenseIV > 30)
                ivs3 = String.valueOf("§o") + ivs3;
            if (player1SpAttIV > 30)
                ivs4 = String.valueOf("§o") + ivs4;
            if (player1SpDefIV > 30)
                ivs5 = String.valueOf("§o") + ivs5;
            if (player1SpeedIV > 30)
                ivs6 = String.valueOf("§o") + ivs6;

            final Text actionPair = Text.builder(message)
                        .onHover(TextActions.runCommand("/pixelmonbroadcasts toggle " + flags.get(0)))
                        .build();

            // We have another Pokémon.
            if (nbt.size() > 1)
            {
                final int player2HPIV = nbt.get(1).getInteger(NbtKeys.IV_HP);
                final int player2AttackIV = nbt.get(1).getInteger(NbtKeys.IV_ATTACK);
                final int player2DefenseIV = nbt.get(1).getInteger(NbtKeys.IV_DEFENCE);
                final int player2SpAttIV = nbt.get(1).getInteger(NbtKeys.IV_SP_ATT);
                final int player2SpDefIV = nbt.get(1).getInteger(NbtKeys.IV_SP_DEF);
                final int player2SpeedIV = nbt.get(1).getInteger(NbtKeys.IV_SPEED);
            }

            // Format the IVs for use later, so we can print them.
            String ivs1 = String.valueOf(HPIV + " §2" + shortenedHP + statSeparator);
            String ivs2 = String.valueOf(attackIV + " §2" + shortenedAttack + statSeparator);
            String ivs3 = String.valueOf(defenseIV + " §2" + shortenedDefense + statSeparator);
            String ivs4 = String.valueOf(spAttIV + " §2" + shortenedSpecialAttack + statSeparator);
            String ivs5 = String.valueOf(spDefIV + " §2" + shortenedSpecialDefense + statSeparator);
            String ivs6 = String.valueOf(speedIV + " §2" + shortenedSpeed);

            if (HPIV > 30)
                ivs1 = String.valueOf("§o") + ivs1;
            if (attackIV > 30)
                ivs2 = String.valueOf("§o") + ivs2;
            if (defenseIV > 30)
                ivs3 = String.valueOf("§o") + ivs3;
            if (spAttIV > 30)
                ivs4 = String.valueOf("§o") + ivs4;
            if (spDefIV > 30)
                ivs5 = String.valueOf("§o") + ivs5;
            if (speedIV > 30)
                ivs6 = String.valueOf("§o") + ivs6;

            // Get a bunch of data from our PokemonMethods utility class. Used for messages, later on.
            final ArrayList<String> natureArray = PokemonMethods.getNatureStrings(nbt.getInteger(NbtKeys.NATURE));
            final String natureName = natureArray.get(0).toLowerCase();
            final String plusVal = natureArray.get(1);
            final String minusVal = natureArray.get(2);

            // Grab a gender string.
            final String genderString;
            switch (nbt.getInteger(NbtKeys.GENDER))
            {
                case 0:
                    genderString = "is §2male§a."; break;
                case 1:
                    genderString = "is §2female§a."; break;
                default:
                    genderString = "has §2no gender§a.";
            }

            // Grab a growth string.
            final String sizeString;
            switch (nbt.getInteger(NbtKeys.GROWTH))
            {
                case 0:
                    sizeString = " is §2a pygmy§a."; break;
                case 1:
                    sizeString = " is §2a runt§a."; break;
                case 2:
                    sizeString = " is §2small§a."; break;
                case 3:
                    sizeString = " is §2ordinary§a."; break;
                case 4:
                    sizeString = " is §2huge§a."; break;
                case 5:
                    sizeString = " is §2giant§a."; break;
                case 6:
                    sizeString = " is §2enormous§a."; break;
                case 7:
                    sizeString = " is §2§nginormous§r§a."; break; // NOW with fancy underlining!
                case 8:
                    sizeString = " is §2§omicroscopic§r§a."; break; // NOW with fancy italicization!
                default:
                    sizeString = "'s size is §2unknown§a.";
            }

            // Do the setup for our nature String separately, as it's a bit more involved.
            final String natureString;
            if (nbt.getInteger(NbtKeys.NATURE) >= 0 && nbt.getInteger(NbtKeys.NATURE) <= 4)
                natureString = "is §2" + natureName + "§a, with well-balanced stats.";
            else if (nbt.getInteger(NbtKeys.NATURE) < 0 || nbt.getInteger(NbtKeys.NATURE) > 24)
                natureString = "has an §2unknown §anature...";
            else
                natureString = "is §2" + natureName + "§a, boosting §2" + plusVal + " §aand cutting §2" + minusVal + "§a.";

            // Populate our ArrayList. Every entry will be its own line. May be a bit hacky, but it'll do.
            final ArrayList<String> hovers = new ArrayList<>();
            hovers.add("§bCurrent IVs§f:");
            hovers.add("➡ §a" + totalIVs + "§f/§a186§f (§a" + percentIVs + "%§f)");
            hovers.add("➡ §a" + ivs1 + ivs2 + ivs3 + ivs4 + ivs5 + ivs6);
            hovers.add("§bExtra info§f:");
            hovers.add("➡ §aThis Pokémon" + sizeString);
            hovers.add("➡ §aIt " + genderString);
            hovers.add("➡ §aIt " + natureString);

            // Put every String in our ArrayList on its own line, and reset formatting.
            final Text toPrint = Text.of(String.join("\n§r", hovers));

            // Set up our hover.
            final Text ivBuilder = Text.builder(ivHelper)
                    .onHover(TextActions.showText(toPrint))
                    .build();
        }

        // Sift through the online players.
        Sponge.getGame().getServer().getOnlinePlayers().forEach((recipient) ->
        {
            // Does the iterated player have the needed notifier permission?
            if (recipient.hasPermission("pixelmonbroadcasts.notify." + permission))
            {
                // Does the iterated player have the message enabled? Send it if we get "true" returned.
                if (checkToggleStatus((EntityPlayerMP) recipient, flag))
                    recipient.sendMessage(finalMessage);
            }
        });
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
        if (message.toLowerCase().contains("%ivpct%"))
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
            message = message.replaceAll("(?i)%ivpct%", percentIVs.toString());
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
        if (message.toLowerCase().contains("%ivpct2%"))
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
            message = message.replaceAll("(?i)%ivpct2%", percentIVs.toString());
        }

        // We pass null for events that can't use a player variable, so let's check for that here.
        if (playerName != null && message.toLowerCase().contains("%player2%"))
            message = message.replaceAll("(?i)%player2%", playerName);

        // Send back the final formatted message.
        return message;
    }
}
