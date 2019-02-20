package rs.expand.pixelmonbroadcasts.utilities;

// Remote imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;

import static rs.expand.pixelmonbroadcasts.PixelmonBroadcasts.*;

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
    public static void printUnformattedMessage(final String inputString)
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

    // If we can't read a main config options bundle (really just a String), format and throw this error.
    static void printOptionsNodeError(final List<String> nodes)
    {
        for (final String node : nodes)
            printUnformattedMessage("§cCould not read options node \"§4" + node + "§c\".");

        printUnformattedMessage("§cThe main config contains invalid options. Disabling these.");
        printUnformattedMessage("§cCheck the config, and when fixed use §4/pixelmonbroadcasts reload§c.");
    }

    // Gets a key from messages.conf, formats it (ampersands to section characters), and then sends it.
    // Also swaps any provided placeholders with String representations of the Objects given, if present.
    public static void sendTranslation(CommandSource recipient, String key, Object... params)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final String[] keySet = key.split("\\.");

        // Get the message from the message config, if it's there.
        String message = messagesConfig.getNode((Object[]) keySet).getString();

        // Did we get a message?
        if (message != null)
        {
            // If any parameters are available, find all placeholders in the message and replace them.
            for (int i = 0; i < params.length; i++)
                message = message.replace("{" + (i+1) + "}", params[i].toString());

            recipient.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message).toText());
        }
        // We did not get a message, return the provided key and make sure it's unformatted.
        else
            recipient.sendMessage(Text.of("§r" + key));
    }

    // Gets a key from messages.conf, formats it (ampersands to section characters), and then returns it.
    // Also swaps any provided placeholders with String representations of the Objects given, if present.
    public static String getTranslation(String key, final Object... params)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final String[] keySet = key.split("\\.");

        // Get the message from the message config, if it's there.
        String message = messagesConfig.getNode((Object[]) keySet).getString();

        // Did we get a message?
        if (message != null)
        {
            // If any parameters are available, find all placeholders in the message and replace them.
            for (int i = 0; i < params.length; i++)
                message = message.replace("{" + (i+1) + "}", params[i].toString());

            return TextSerializers.FORMATTING_CODE.replaceCodes(message, '§');
        }
        // We did not get a message, return the provided key and make sure it's unformatted.
        else
            return "§r" + key;
    }

    // Gets a key from broadcasts.conf, formats it (ampersands to section characters), and then returns it.
    // Also swaps any provided placeholders with String representations of the Objects given, if present.
    public static String getBroadcast(final String key)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final String[] keySet = key.split("\\.");

        // Get the broadcast from the broadcast config, if it's there.
        final String broadcast = broadcastsConfig.getNode((Object[]) keySet).getString();

        // Did we get a broadcast?
        if (broadcast != null)
            return TextSerializers.FORMATTING_CODE.replaceCodes(broadcast, '§');
            // We did not get a broadcast, return the provided key and make sure it's unformatted.
        else
        {
            printBasicError("The following broadcast could not be found: §4" + key);
            return null;
        }
    }

    // Inserts the correct tense into lang keys that might have multiple tenses. Returns the translation.
    static String getTensedTranslation(final boolean presentTense, final String key, final Object... params)
    {
        // Set up a String to translate and then return.
        final String tensedKey;

        // Splits our input key, adds the correct tense at a constant known location, and then pieces it back together.
        if (presentTense)
            tensedKey = key.substring(0, 6) + "present_tense." + key.substring(6);
        else
            tensedKey = key.substring(0, 6) + "past_tense." + key.substring(6);

        // Send back the translation of our new freshly-tensed key.
        return getTranslation(tensedKey, params);
    }

    // Get translated names for a given nature's positive and negative stats from the lang.
    static String getTranslatedNatureStat(StatsType stat)
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
