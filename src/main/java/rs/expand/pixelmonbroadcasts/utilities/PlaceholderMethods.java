package rs.expand.pixelmonbroadcasts.utilities;

import com.pixelmonmod.pixelmon.api.overlay.notice.EnumOverlayLayout;
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
import org.spongepowered.api.entity.living.player.Player;
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
    public static void iterateAndBroadcast(final EventData event, final Object object1, final Object object2,
                                           final EntityPlayer player1, final EntityPlayer player2)
    {
        if (event.options().toLowerCase().contains("chat"))
        {
            // Combine the passed broadcast type's prefix with the broadcast/permission key to make a full broadcast key.
            final String broadcast =
                    getBroadcast("chat." + event.key(), object1, object2, player1, player2);

            // Make some Text clones of our broadcast message. We can add to these, or just send them directly.
            Text simpleBroadcastText;

            // If hovers are enabled, make the line hoverable.
            if (object1 != null && getUnsafeFlag(event, "hover"))
            {
                simpleBroadcastText =
                        getHoverableLine(broadcast, object1, event.presentTense(), getUnsafeFlag(event, "reveal"));
            }
            else
                simpleBroadcastText = Text.of(broadcast);

            // Set up some variables that we can access later, if we do end up sending a clickable broadcast.
            BlockPos position = null;
            World world = null;

            // Grab coordinates for making our chat broadcast clickable for those who have access.
            if (object1 != null || player1 != null)
            {
                if (object1 != null)
                {
                    if (object1 instanceof EntityPixelmon || object1 instanceof EntityWormhole)
                    {
                        position = ((Entity) object1).getPosition();
                        world = ((Entity) object1).getEntityWorld();

                        // Do our coordinates make sense? If not, we may have breakage. Set back to null.
                        if ((position.getX() == 0 && position.getZ() == 0))
                            position = null;
                    }
                }

                if (player1 != null)
                {
                    // Did we get a position yet? Prefer the generally more accurate Pokémon/wormhole data if available.
                    if (position == null)
                        position = player1.getPosition();

                    // Same deal for world info.
                    if (world == null)
                        world = player1.getEntityWorld();
                }
            }

            // Copy to finals.
            final BlockPos finalPos = position;
            final World finalWorld = world;

            // Sift through the online players.
            for (Player recipient : Sponge.getGame().getServer().getOnlinePlayers())
            {
                // Does the iterated player have the needed notifier permission?
                if (recipient.hasPermission("pixelmonbroadcasts.notify." + event.key()))
                {
                    // If the iterated player also has admin teleport permissions, add a teleport click action.
                    if (position != null && recipient.hasPermission("pixelmonbroadcasts.action.staff.teleport"))
                    {
                        // Does the iterated player want our broadcast? Send it if we get "true" returned.
                        if (checkToggleStatus((EntityPlayer) recipient, event.flags()))
                        {
                            // Send a clickable version of the normal text. Yes, this is a mess.
                            recipient.sendMessage(Text.builder()
                                .append(simpleBroadcastText)
                                .onClick(TextActions.executeCallback(callback ->
                                {
                                    ((EntityPlayer) recipient).setWorld(finalWorld);
                                    ((EntityPlayer) recipient).setPosition(finalPos.getX(), finalPos.getY() + 2, finalPos.getZ());
                                }))
                                .build());
                        }
                    }
                    // Does the iterated player want our broadcast? Send it if we get "true" returned.
                    else if (checkToggleStatus((EntityPlayer) recipient, event.flags()))
                        recipient.sendMessage(simpleBroadcastText);
                }
            }
        }

        if (event.options().toLowerCase().contains("notice"))
        {
            if (object1 != null)
            {
                // Combine the passed broadcast type's prefix with the broadcast/permission key to make a full broadcast key.
                final String broadcast =
                        getBroadcast("notice." + event.key(), object1, object2, player1, player2);

                // Figure out how to get a Pokemon object for our purposes.
                Pokemon pokemon;
                if (object1 instanceof Pokemon)
                    pokemon = (Pokemon) object1;
                else if (object1 instanceof EntityPixelmon)
                    pokemon = ((EntityPixelmon) object1).getPokemonData();
                else
                    pokemon = null;

                // Set up a builder for our notice.
                NoticeOverlay.Builder builder = NoticeOverlay.builder().setLayout(EnumOverlayLayout.LEFT_AND_RIGHT);

                // Do some magic for getting the right sprite.
                if (pokemon != null)
                {
                    // Puts up a sprite matching the current Pokémon's species, form and gender.
                    builder.setPokemonSprite
                    (
                            new PokemonSpec
                            (
                                    pokemon.getSpecies().getPokemonName(),
                                    "form:" + pokemon.getForm(),
                                    "gender:" + pokemon.getGender().ordinal()
                            )
                    );
                }
                else
                {
                    // Creates a question mark Unown using specs.
                    builder.setPokemonSprite(new PokemonSpec("Unown", "form:26"));
                }

                // Add the message here, after applying other stuff. Doing earlier causes an NPE, apparently. Dunno.
                builder.setLines(broadcast);

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

    public static boolean canReceiveBroadcast(final CommandSource src, final EventData event)
    {
        if (src.hasPermission("pixelmonbroadcasts.notify." + event.key()) && event.options() != null)
            return event.options().contains("chat") || event.options().contains("notify");

        return false;
    }

    // Checks if a given flag is set for the given event. Has some hardcoded values on stuff that's off-limits.
    private static boolean getUnsafeFlag(final EventData event, final String flag)
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
        else if (event instanceof EventData.Challenges && flag.equals("reveal"))
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
        else if (event instanceof EventData.Spawns && flag.equals("reveal"))
            return false;
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
        else if (event == EventData.Others.TRADE && (flag.equals("hover") || flag.equals("reveal")))
            return false;

        logger.error("Exit path is fallthrough.");
        return event.options().toLowerCase().contains(flag);
    }

    // Replaces all placeholders in a provided message.
    private static String getBroadcast(final String key, final Object object1, final Object object2,
                                       final EntityPlayer player1, final EntityPlayer player2)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final Object[] keySet = key.split("\\.");

        // Get the broadcast from the broadcast config, if it's there.
        //String broadcast = broadcastsConfig.getNode(keySet).getString();
        // DEBUG:
        String broadcast = key + " : %biome% %world% %pokemon% %player% %ivpercent% %xpos% %ypos% %zpos% %shiny% : " +
                                "%biome2% %world2% %pokemon2% %player2% %ivpercent2% %xpos2% %ypos2% %zpos2% %shiny2%";

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
        if (object1 != null)
        {
            // Figure out what our received object is, exactly.
            if (object1 instanceof EntityPixelmon || object1 instanceof EntityWormhole)
            {
                // Make the entity a bit easier to access. It probably has more info than a Pokemon object would -- use it!
                Entity entity = (Entity) object1;

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
                if (object1 instanceof EntityPixelmon)
                    pokemon = ((EntityPixelmon) entity).getPokemonData();
                else // No more info to extract, here.
                    return broadcast;
            }
            else
                pokemon = (Pokemon) object1;

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
            if (object2 != null)
            {
                // We've got a second Pokémon! See what type this one is.
                final Pokemon pokemon2;
                if (object2 instanceof EntityPixelmon)
                {
                    // Make this one easier to access, too. We'll need it.
                    EntityPixelmon pokemon2Entity = (EntityPixelmon) object2;

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
                    pokemon2 = (Pokemon) object2;

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
        if (player1 != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player%", player1.getName());

            // Get the player's position. We prefer using the Pokémon's position, but if that fails this should catch it.
            position = player1.getPosition();

            // Get the player's biome, nicely formatted (spaces!) and all. Replace placeholder if it still exists.
            final String biome = getFormattedBiome(player1.getEntityWorld(), position);
            broadcast = broadcast.replaceAll("(?i)%biome%", biome);

            // Insert a world name if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%world%", player1.getEntityWorld().getWorldInfo().getWorldName());

            // Insert coordinates if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%xpos%", String.valueOf(position.getX()));
            broadcast = broadcast.replaceAll("(?i)%ypos%", String.valueOf(position.getY()));
            broadcast = broadcast.replaceAll("(?i)%zpos%", String.valueOf(position.getZ()));
        }

        // Do we have a second player? Replace player-specific placeholders as well as some that we might not have yet.
        if (player2 != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player2%", player2.getName());

            // Get the player's position. We prefer using the Pokémon's position, but if that fails this should catch it.
            position = player2.getPosition();

            // Get the player's biome, nicely formatted (spaces!) and all. Replace placeholder if it still exists.
            final String biome2 = getFormattedBiome(player2.getEntityWorld(), position);
            broadcast = broadcast.replaceAll("(?i)%biome2%", biome2);

            // Insert a world name if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%world2%", player2.getEntityWorld().getWorldInfo().getWorldName());

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
            final String broadcast, final Object object1, final boolean isPresentTense, final boolean showIVs)
    {
        // Is our received object of the older EntityPixelmon type, or is it Pokemon?
        Pokemon pokemon =
                object1 instanceof EntityPixelmon ? ((EntityPixelmon) object1).getPokemonData() : (Pokemon) object1;

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
