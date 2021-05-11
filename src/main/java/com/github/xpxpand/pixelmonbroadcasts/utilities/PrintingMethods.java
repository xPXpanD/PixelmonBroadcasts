package com.github.xpxpand.pixelmonbroadcasts.utilities;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.forms.EnumMega;
import com.pixelmonmod.pixelmon.enums.forms.RegionalForms;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.logger;
import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.messagesConfig;

// A collection of methods that are commonly used. One changed word or color here, and half the mod changes. Sweet.
public class PrintingMethods
{
    // Prints an event message to console, if it is enabled. One size fits all, with the help of the LogType enum.
    public static void logEvent(final EventData event, final String worldName, final BlockPos location, final String... inputs)
    {
        // If options() is null, we'll catch that and error in our iterateAndBroadcast call. Don't worry about it here.
        // Note to future me: Passing in § as a char breaks colors, so keep it as a String.
        if (event.options() != null && event.options().toLowerCase().contains("log"))
        {
            if (event instanceof EventData.Spawns) // Spawn logging needs some special logic.
            {
                logger.info
                (
                        "§" + event.color() +
                        "A " + inputs[0] +
                        " has spawned in world \"" + worldName +
                        "\", at X:" + location.getX() +
                        " Y:" + location.getY() +
                        " Z:" + location.getZ()
                );
            }
            else if (event.messages().length == 2) // Generally used for events that have two players.
            {
                // An example from the battle draw event follows.
                logger.info
                (
                        "§" + event.color() +
                //                  player 1    's battle with        player 2    ended in a draw
                        "Player " + inputs[0] + event.messages()[0] + inputs[1] + event.messages()[1] +
                        " in world \"" + worldName +
                        "\", at X:" + location.getX() +
                        " Y:" + location.getY() +
                        " Z:" + location.getZ()
                );
            }
            else
            {
                // An example from the forfeit event follows.
                logger.info
                (
                        "§" + event.color() +
                //                  player      fled from a           pokémon/trainer
                        "Player " + inputs[0] + event.messages()[0] + inputs[1] +
                        " in world \"" + worldName +
                        "\", at X:" + location.getX() +
                        " Y:" + location.getY() +
                        " Z:" + location.getZ()
                );
            }
        }
    }

    // If we can't read a main config options bundle, throw this error. Called during reloads and hybrid checks.
    static void printOptionsNodeError(final List<String> nodes)
    {
        for (final String node : nodes)
            logger.info("    §cCould not read settings.conf node \"§4" + node + "§c\"!");

        logger.info("    §cThe main configuration file contains one or more invalid options.");
        logger.info("    §cCheck the config, and when fixed use §4/pixelmonbroadcasts reload§c.");
    }

    // Converts a String with section colors into Forge's Text representation with proper Forge formatting.
    public static String convertSectionColors(String message)
    {
        StringBuilder output = new StringBuilder();

        // Split our incoming String into an array of characters. Set up some needed variables.
        char[] charSet = message.toCharArray();
        char currentChar, nextChar;
        int charTotal = charSet.length;

        for (int i = 0; i < charTotal; i++)
        {
            currentChar = charSet[i];

            // Do we have a potential formatting character? Don't just replace all ampersands, people may want those.
            if (currentChar == '&')
            {
                // Can we look ahead safely?
                if (i + 1 <= charTotal)
                {
                    // Get the next character in line.
                    nextChar = charSet[i+1];

                    // Is our next character a formatting character? Ignore uppercase, lowercase is standard for formatting.
                    switch (nextChar)
                    {
                        // Colors.
                        case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8':
                        case '9': case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                        // Formatting.
                        case 'k': case 'l': case 'm': case 'n': case 'o':
                        // Reset.
                        case 'r':
                        {
                            // Do we have a valid tag? Replace the ampersand with a proper formatting character.
                            currentChar = '§';
                        }
                    }

                    // Add our character, whether it was swapped or not.
                    output.append(currentChar);
                }
            }

            else
                output.append(currentChar);
        }

        return output.toString();
    }

    // Cleans any formatting tags on the given String. Returns the cleaned reconstituted String.
    public static String clearFormatting(String message)
    {
        // Split our incoming String into a List of Characters. Set up some needed variables.
        List<Character> characters = message.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        char currentChar, nextChar;
        int charTotal = characters.size();

        for (int i = 0; i < charTotal; i++)
        {
            currentChar = characters.get(i);

            // Do we have a potential formatting character? Don't just replace all ampersands, people may want those.
            if (currentChar == '&' || currentChar == '§')
            {
                // Can we look ahead safely?
                if (i + 1 <= charTotal)
                {
                    // Get the next character in line.
                    nextChar = characters.get(i + 1);

                    // Is our next character a formatting character? Ignore uppercase, lowercase is standard for formatting.
                    switch (nextChar)
                    {
                        // Colors.
                        case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8':
                        case '9': case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
                        // Formatting.
                        case 'k': case 'l': case 'm': case 'n': case 'o':
                        // Reset.
                        case 'r':
                        {
                            // Do we have a valid tag? Remove it. (both characters please)
                            characters.remove(i);
                            characters.remove(i);

                            // Decrement the iterator (by one, we never skipped ahead) and the total message length.
                            i--;
                            charTotal = charTotal - 2;
                        }
                    }
                }
            }
        }

        return characters.stream().map(String::valueOf).collect(Collectors.joining());
    }

    // Gets a value matching the given messages key, formats it (ampersands to section characters), and then sends it.
    // Also swaps any provided placeholders with String representations of the Objects given, if present.
    public static void sendTranslation(ICommandSender recipient, String key, Object... params)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final String[] keySet = key.split("\\.");

        // Get the message from the message config, if it's there.
        String message = messagesConfig.getNode((Object[]) keySet).getString();

        // Did we get a message?
        if (message != null)
        {
            // Swallow the message if it's been emptied out.
            if (!message.equals(""))
            {
                // If any parameters are available, find all placeholders in the message and replace them.
                for (int i = 0; i < params.length; i++)
                    message = message.replace("{" + (i+1) + "}", params[i].toString());

                recipient.sendMessage(new TextComponentString(convertSectionColors(message)));
            }
        }
        // We did not get a message, return the provided key and make sure it's unformatted.
        else
            recipient.sendMessage(new TextComponentString("§r" + key));
    }

    // Gets a value matching the given messages key, formats it (ampersands to section characters), and then returns it.
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

            return convertSectionColors(message);
        }
        // We did not get a message, return the provided key and make sure it's unformatted.
        else
            return "§r" + key;
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

    // Gets a translated key to add in front of a log message if we have a regional or mega Pokémon.
    // FIXME: "A Alolan". May not be worth fixing with localization in mind, can't hardcode something in.
    public static String getEnumType(final EntityPixelmon entity)
    {
        if (entity.getFormEnum() == RegionalForms.ALOLAN)
            return PrintingMethods.getTranslation("insert.alolan");
        else if (entity.getFormEnum() == RegionalForms.GALARIAN)
            return PrintingMethods.getTranslation("insert.galarian");
        else if (entity.getFormEnum() instanceof EnumMega && entity.getFormEnum() != EnumMega.Normal)
            return PrintingMethods.getTranslation("insert.mega");
        else
            return "";
    }
    public static String getEnumType(final Pokemon pokemon)
    {
        if (pokemon.getFormEnum() == RegionalForms.ALOLAN)
            return PrintingMethods.getTranslation("insert.alolan");
        else if (pokemon.getFormEnum() == RegionalForms.GALARIAN)
            return PrintingMethods.getTranslation("insert.galarian");
        else if (pokemon.getFormEnum() instanceof EnumMega && pokemon.getFormEnum() != EnumMega.Normal)
            return PrintingMethods.getTranslation("insert.mega");
        else
            return "";
    }
}
