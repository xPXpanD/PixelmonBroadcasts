package rs.expand.pixelmonbroadcasts.utilities;

// Remote imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.text.Text;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

// A collection of methods that are commonly used. One changed word or color here, and half the mod changes. Sweet.
public class PrintingMethods
{
    // Remove the ugly prefix from console commands, so we can roll our own. Thanks for the examples, NickImpact!
    private static Optional<ConsoleSource> getConsole()
    {
        if (Sponge.isServerAvailable())
            return Optional.of(Sponge.getServer().getConsole());
        else
            return Optional.empty();
    }

    // If we need to print something without any major formatting, do it here. Good for console lists.
    public static void printBasicMessage(final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§f" + inputString)));
    }

    // If we need to show a basic error, do it here.
    public static void printBasicError(final String inputString)
    {
        getConsole().ifPresent(console ->
                console.sendMessage(Text.of("§4PBR §f// §4Error: §c" + inputString)));
    }

    // If we can't read a main config toggle, format and throw this error.
    static void printBooleanNodeError(final List<String> nodes)
    {
        for (final String node : nodes)
        { printBasicMessage("§cCould not read config node \"§4" + node + "§c\"."); }

        printBasicMessage("§cThe main config contains invalid booleans. Disabling these.");
        printBasicMessage("§cCheck the config, and when fixed use §4/pbreload§c.");
    }

    // If we can't read a main config message, format and throw this error.
    static void printMessageNodeError(final List<String> nodes)
    {
        for (final String node : nodes)
        { printBasicMessage("§cCould not read message node \"§4" + node + "§c\"."); }

        printBasicMessage("§cThe main config contains invalid messages. Hiding these.");
        printBasicMessage("§cCheck the config, and when fixed use §4/pbreload§c.");
    }

    // Check if the given player has the given flag set, and if so, return what the flag is set to.
    public static boolean checkToggleStatus(final EntityPlayerMP recipient, final String flag)
    {
        // Does the player have a flag set for this notification?
        if (recipient.getEntityData().getCompoundTag("pbToggles").hasKey(flag))
        {
            // Return the flag's status.
            return recipient.getEntityData().getCompoundTag("pbToggles").getBoolean(flag);
        }

        // Player does not have the flag, so return the default state ("true").
        return true;
    }

    // Gets a key from the language file, formats it using our own custom parseRemoteString, and then sends it.
    public static void sendFormattedTranslation(CommandSource recipient, String key, Object... params)
    {
        // Format the key grabbed from our language file, replacing ampersands with section symbols.
        final String formattedInput =
                parseRemoteString(new TextComponentTranslation(key, params).getUnformattedComponentText());

        // Send the now-formatted input directly to the player.
        recipient.sendMessage(Text.of(formattedInput));
    }

    // Gets a key from the language file, formats it using our own custom parseRemoteString, and then returns it.
    public static String getFormattedTranslation(String key, Object... params)
    {
        // Format the key grabbed from our language file, replacing ampersands with section symbols.
        return parseRemoteString(new TextComponentTranslation(key, params).getUnformattedComponentText());
    }

    // Takes a config String and changes any ampersands to section symbols, which we can use internally.
    // Only runs during config (re-)loads, after which we commit the parsed strings to memory.
    static String parseRemoteString(final String input)
    {
        // Were we able to read the String being checked from the config?
        if (input != null)
        {
            // Set up a list of valid formatting codes.
            final List<Character> validFormattingCharacters = Arrays.asList
            (
                    // Color numbers.
                    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                    // Color letters, lower and upper case.
                    'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F',
                    // Other formatting codes.
                    'k', 'l', 'm', 'n', 'o', 'r'
            );

            // Start replacing our ampersands.
            final StringBuilder mutableInput = new StringBuilder(input);
            for (int i = 0; i < mutableInput.length(); i++)
            {
                // Is the character that's currently being checked an ampersand?
                if (mutableInput.charAt(i) == '&')
                {
                    // Make sure the iterator is still inside of the input String's length. Let's not check out of bounds.
                    if ((i + 1) < mutableInput.length())
                    {
                        // Look ahead: Does the next character contain a known formatting character? Replace the ampersand!
                        if (validFormattingCharacters.contains(mutableInput.charAt(i + 1)))
                            mutableInput.setCharAt(i, '§');
                    }
                }
            }

            // Replace our old input String with the one that we fixed formatting on.
            return mutableInput.toString();
        }

        // If we could not read from the config, this is hit.
        return null;
    }

    // Takes a config String, and replaces any known placeholders with the proper replacements as many times as needed.
    public static String replacePlaceholders(
            String message, final String playerName, final EntityPixelmon pokemon, final BlockPos location)
    {
        // If our message has any placeholders inside, replace them with the provided replacement String.
        // Case-insensitive. If a player cannot be provided, we receive a null object and handle it here.
        if (message.toLowerCase().contains("%pokemon%"))
            message = message.replaceAll("(?i)" + "%pokemon%", pokemon.getLocalizedName());
        if (message.toLowerCase().contains("%world%"))
            message = message.replaceAll("(?i)" + "%world%", pokemon.getEntityWorld().getWorldInfo().getWorldName());
        if (message.toLowerCase().contains("%xpos%"))
            message = message.replaceAll("(?i)" + "%xpos%", String.valueOf(location.getX()));
        if (message.toLowerCase().contains("%ypos%"))
            message = message.replaceAll("(?i)" + "%ypos%", String.valueOf(location.getY()));
        if (message.toLowerCase().contains("%zpos%"))
            message = message.replaceAll("(?i)" + "%zpos%", String.valueOf(location.getZ()));

        // Run some special logic for biome names. This is a bit more involved, so we put the logic here.
        if (message.toLowerCase().contains("%biome%"))
        {
            // Grab the name. This compiles fine if the access transformer is loaded correctly, despite any errors.
            final String biome = pokemon.getEntityWorld().getBiomeForCoordsBody(location).biomeName;
            //final String biome = net.minecraft.util.text.translation.I18n.translateToLocalFormatted(basicBiome);

            // Apply.
            message = message.replaceAll("(?i)" + "%biome%", biome);
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
            message = message.replaceAll("(?i)" + "%ivpct%", percentIVs.toString());
        }

        // We pass null for events that can't use a player variable, so let's check for that here.
        if (playerName != null && message.toLowerCase().contains("%player%"))
            message = message.replaceAll("(?i)" + "%player%", playerName);

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
            message = message.replaceAll("(?i)" + "%pokemon2%", pokemon.getLocalizedName());
        if (message.toLowerCase().contains("%world2%"))
            message = message.replaceAll("(?i)" + "%world2%", pokemon.getEntityWorld().getWorldInfo().getWorldName());
        if (message.toLowerCase().contains("%xpos2%"))
            message = message.replaceAll("(?i)" + "%xpos2%", String.valueOf(location.getX()));
        if (message.toLowerCase().contains("%ypos2%"))
            message = message.replaceAll("(?i)" + "%ypos2%", String.valueOf(location.getY()));
        if (message.toLowerCase().contains("%zpos2%"))
            message = message.replaceAll("(?i)" + "%zpos2%", String.valueOf(location.getZ()));

        // Run some special logic for biome names. This is a bit more involved, so we put the logic here.
        if (message.toLowerCase().contains("%biome%"))
        {
            // Grab the name. This compiles fine if the access transformer is loaded correctly, despite any errors.
            final String biome = pokemon.getEntityWorld().getBiomeForCoordsBody(location).biomeName;
            //final String biome = net.minecraft.util.text.translation.I18n.translateToLocalFormatted(basicBiome);

            // Apply.
            message = message.replaceAll("(?i)" + "%biome%", biome);
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
            message = message.replaceAll("(?i)" + "%ivpct2%", percentIVs.toString());
        }

        // We pass null for events that can't use a player variable, so let's check for that here.
        if (playerName != null && message.toLowerCase().contains("%player2%"))
            message = message.replaceAll("(?i)" + "%player2%", playerName);

        // Send back the final formatted message.
        return message;
    }
}
