// The big bad. Slowly optimizing this as I go, it's pretty crazy.
package com.github.xpxpand.pixelmonbroadcasts.utilities;

import com.pixelmonmod.pixelmon.api.overlay.notice.EnumOverlayLayout;
import com.pixelmonmod.pixelmon.api.overlay.notice.NoticeOverlay;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.entities.EntityWormhole;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumMegaPokemon;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.forms.RegionalForms;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import com.github.xpxpand.pixelmonbroadcasts.enums.EventData;

import java.util.ArrayList;
import java.util.List;

import static com.github.xpxpand.pixelmonbroadcasts.PixelmonBroadcasts.*;

@SuppressWarnings("deprecation")
public class PlaceholderMethods
{
    public static void iterateAndBroadcast(final EventData event, final Object object1, final Object object2,
                                           final EntityPlayer player1, final EntityPlayer player2)
    {
        // Make sure options aren't null. If they are, error!!
        if (event.options() != null)
        {
            if (event.options().toLowerCase().contains("chat"))
            {
                // Combine the passed broadcast type's prefix with the broadcast/permission key to make a full key.
                final String broadcast = getBroadcast("chat." + event.key(), object1, object2, player1, player2);

                // Make some Text clones of our broadcast message. We can add to these, or just send them directly.
                ITextComponent simpleBroadcastText;

                // If hovers are enabled, make the line hoverable.
                if (object1 != null && getFlagStatus(event, "hover"))
                {
                    simpleBroadcastText =
                            getHoverableLine(broadcast, object1, event.presentTense(), getFlagStatus(event, "reveal"));
                }
                else
                    simpleBroadcastText = new TextComponentString(broadcast);

                // Set up some variables that we can access later, if we do end up sending a clickable broadcast.
                BlockPos location = null;
                Integer dimension = null;

                // Grab coordinates for making our chat broadcast clickable for those who have access.
                if (object1 != null)
                {
                    if (object1 instanceof EntityPixelmon || object1 instanceof EntityWormhole)
                    {
                        // Grab entity data.
                        dimension = ((Entity) object1).dimension;
                        location = ((Entity) object1).getPosition();

                        // Do our coordinates make sense? If not, we may have breakage. Set back to null.
                        if ((location.getX() == 0 && location.getZ() == 0))
                            location = null;
                    }
                }
                if (player1 != null)
                {
                    // Did we get data yet? Prefer the generally more accurate Pokémon/wormhole data if available.
                    if (location == null)
                        location = player1.getPosition();
                    if (dimension == null)
                        dimension = player1.dimension;
                }

                // Do we have a location? Add a click action.
                if (location != null)
                {
                    // Prep the target position.
                    final int x = location.getX();
                    final int y = location.getY() + 1; // Increment Y by one so people don't get stuck in blocks.
                    final int z = location.getZ();

                    // Attach the on-click.
                    simpleBroadcastText.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/pixelmonbroadcasts teleport " + dimension + ' ' + x + ' ' + y + ' ' + z));
                }
                else
                {
                    logger.error("Could not find a valid location to teleport people to for this event! Please report this.");
                    logger.error("Event: \"" + event.toString() + "\", in the \"" + event.getClass().getSimpleName() + "\" category!");
                }

                // Sift through the online players.
                for (EntityPlayer recipient : PlayerMethods.getOnlinePlayers())
                {
                    // Does the iterated player have the needed notifier permission?
                    if (PlayerMethods.hasPermission(recipient, "pixelmonbroadcasts.notify." + event.key()))
                    {
                        // Does the iterated player want our broadcast? Send it if they do.
                        if (wantsBroadcast(recipient, event.flags()))
                            recipient.sendMessage(simpleBroadcastText);
                    }
                }
            }

            if (event.options().toLowerCase().contains("notice"))
            {
                if (object1 != null)
                {
                    // Combine the passed broadcast type's prefix with the broadcast/permission key to make a full key.
                    final String broadcast = getBroadcast("notice." + event.key(), object1, object2, player1, player2);

                    // Set up a Pokéspec to pass in.
                    final String spec;

                    // Figure out how to get a Pokemon object for our purposes.
                    Pokemon pokemon;
                    if (object1 instanceof Pokemon)
                        pokemon = (Pokemon) object1;
                    else if (object1 instanceof EntityPixelmon)
                        pokemon = ((EntityPixelmon) object1).getPokemonData();
                    else
                        pokemon = null;

                    // Do we have a Pokémon? Do some magic for getting the right sprite.
                    if (pokemon != null)
                    {
                        // Puts up a sprite matching the current Pokémon's species, form, gender and shiny status. Nice, eh?
                        // TODO: Custom texture support is completely untested. Get some confirmation.
                        // TODO: Test function with different server langs.
                        spec = pokemon.getSpecies().getPokemonName()
                                + " form:" + pokemon.getForm()
                                + " gender:" + pokemon.getGender().name().charAt(0)
                                + (pokemon.isShiny() ? " shiny" : " !shiny")
                                + (pokemon.getCustomTexture().isEmpty() ? "" : " texture:" + pokemon.getCustomTexture());
                    }
                    // Create a question mark Unown!
                    else
                        spec = "Unown form:26";

/*                    if (Sponge.getPluginManager().isLoaded("pixelmonoverlay"))
                    {
                        PixelmonOverlayBridge.display
                        (
                                EnumOverlayLayout.LEFT_AND_RIGHT, OverlayGraphicType.PokemonSprite,
                                Collections.singletonList(broadcast), 10L, spec, null
                        );
                    }
                    else
                    {*/
                    // Set up a builder for our notice and populate it.
                    NoticeOverlay.Builder builder =
                            NoticeOverlay.builder().setLayout(EnumOverlayLayout.LEFT_AND_RIGHT).setPokemonSprite(new PokemonSpec(spec));

                    // Add the message here, after applying other stuff. Doing earlier causes an NPE, apparently. Dunno.
                    builder.setLines(broadcast);

                    // Sift through the online players.
                    PlayerMethods.getOnlinePlayers().forEach((recipient) ->
                    {
                        // Does the iterated player have the needed notifier permission?
                        if (PlayerMethods.hasPermission(recipient, "pixelmonbroadcasts.notify." + event.key()))
                        {
                            // Does the iterated player want our broadcast? Send it if we get "true" returned.
                            if (wantsBroadcast(recipient, event.flags()))
                            {
                                // Prints a message to Pixelmon's notice board. (cool box at the top)
                                builder.sendTo((EntityPlayerMP) recipient);

                                // Put the player's UUID and the current time into a hashmap. Check regularly to wipe notices.
                                noticeExpiryMap.put(recipient.getUniqueID(), System.currentTimeMillis());
                            }
                        }
                    });
                }
            }
        }
        else
        {
            logger.error("Could not get settings for this event! Please check settings.conf and reload.");
            logger.error("Event: \"" + event.toString() + "\", in the \"" + event.getClass().getSimpleName() + "\" category!");
        }
    }

    // Checks whether a player can receive a given broadcast. Used for determining which toggles to display.
    public static boolean canReceiveBroadcast(final EntityPlayer player, final EventData event)
    {
        if (event.options() != null)
        {
            // Does the player have the correct permission, or are they in single player?
            if (PlayerMethods.hasPermission(player, "pixelmonbroadcasts.notify." + event.key()))
                return event.options().contains("chat") || event.options().contains("notice");
        }

        return false;
    }

    // Attempts to safely grab a player's world on events that also have a Pokémon, even if the player is somehow null.
    public static EntityPlayerMP getSafePlayer(
            final String eventType, final EntityPlayerMP playerEntity, final EntityPixelmon pokemonEntity)
    {
        if (playerEntity != null)
            return playerEntity;
        else if (pokemonEntity.getOwnerId() != null)
        {
            logger.warn("Event did not have a player! Falling back to event entity's owner.");
            logger.warn("Please report this to the tracker. Include any details you may have.");
            logger.warn("Event type: " + eventType + " (include this as well)");
            return FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(pokemonEntity.getOwnerId());
        }
        else
        {
            logger.error("Event did not have a player, and there's no owner! Halting execution.");
            logger.error("Please report this to the tracker. Include any details you may have.");
            logger.error("Event type: " + eventType + " (include this as well)");
            return null;
        }
    }

    // Checks whether a given flag is set for the given event. Has some hardcoded values on stuff that's off-limits.
    private static boolean getFlagStatus(final EventData event, final String flag)
    {
        if (event instanceof EventData.Challenges && flag.equals("reveal"))
            return false;
        else if (event instanceof EventData.Spawns && flag.equals("reveal"))
            return false;
        else if (event == EventData.Spawns.WORMHOLE && flag.equals("hover"))
            return false;
        else if (event == EventData.Others.TRADE && (flag.equals("hover") || flag.equals("reveal")))
            return false;

        return event.options().toLowerCase().contains(flag);
    }

    // Replaces all placeholders in a provided message.
    private static String getBroadcast(final String key, final Object object1, final Object object2,
                                       final EntityPlayer player1, final EntityPlayer player2)
    {
        // This is slightly hacky, but split the incoming key up into separate nodes so we can read it.
        final Object[] keySet = key.split("\\.");

        // Get the broadcast from the broadcast config, if it's there.
        String broadcast = broadcastsConfig.getNode(keySet).getString();
        /*String broadcast = key + " : %biome% %world% %pokemon% %player% %ivpercent% %xpos% %ypos% %zpos% %shiny% : " +
                                "%biome2% %world2% %pokemon2% %player2% %ivpercent2% %xpos2% %ypos2% %zpos2% %shiny2%";*/

        // Did we get a broadcast?
        if (broadcast != null)
            broadcast = PrintingMethods.convertSectionColors(broadcast);
        // We did not get a broadcast, return the provided key and make sure it's unformatted.
        else
        {
            logger.error("The following broadcast could not be found: §4" + key);
            return key;
        }

        // Set up some variables that we want to be able to access later.
        BlockPos location;
        Pokemon pokemon;

        // Do we have a Pokémon object? Replace Pokémon-specific placeholders.
        if (object1 != null)
        {
            // Figure out what our received object is, exactly.
            if (object1 instanceof EntityPixelmon || object1 instanceof EntityWormhole)
            {
                // Make the entity a bit easier to access. It probably has more info than a Pokemon object would -- use it!
                Entity entity = (Entity) object1;

                // Get a location, and do a sanity check on it to work around possible entity removal issues.
                // (if both are zero, something might have broken -- we'll try getting the info from the player instead)
                location = entity.getPosition();
                if (!(location.getX() == 0 && location.getZ() == 0))
                {
                    // Get the Pokémon's biome, nicely formatted (spaces!) and all. Replace placeholder.
                    final String biome = getFormattedBiome(entity.getEntityWorld(), location);
                    broadcast = broadcast.replaceAll("(?i)%biome%", biome);

                    // Insert a world name.
                    broadcast = broadcast.replaceAll("(?i)%world%", entity.getEntityWorld().getWorldInfo().getWorldName());

                    // Insert coordinates.
                    broadcast = broadcast.replaceAll("(?i)%xpos%", String.valueOf(location.getX()));
                    broadcast = broadcast.replaceAll("(?i)%ypos%", String.valueOf(location.getY()));
                    broadcast = broadcast.replaceAll("(?i)%zpos%", String.valueOf(location.getZ()));
                }
                else
                {
                    logger.warn("The event's Pokémon entity was removed from the world early!");
                    logger.warn("We'll try to get missing info from the player. World info may look weird.");
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
                // First see if the Pokémon is an egg. If it is, be extra careful and don't spoil the name.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                final String pokemonName;
                if (pokemon.isEgg())
                    pokemonName = PrintingMethods.getTranslation("placeholder.pokemon.is_egg");
                else if (pokemon.getFormEnum() == RegionalForms.ALOLAN)
                    pokemonName = PrintingMethods.getTranslation("insert.alolan") + pokemon.getSpecies().getLocalizedName();
                else if (pokemon.getFormEnum() == RegionalForms.GALARIAN)
                    pokemonName = PrintingMethods.getTranslation("insert.galarian") + pokemon.getSpecies().getLocalizedName();
                else
                {
                    boolean isMega = false;
                    try
                    {
                        // Is there a Mega form for this Pokémon?
                        if (EnumMegaPokemon.getMega(pokemon.getSpecies()).numMegaForms > 0)
                            isMega = true;
                    }
                    catch(Exception ignored){}

                    if (isMega)
                        pokemonName = PrintingMethods.getTranslation("insert.mega") + pokemon.getSpecies().getLocalizedName();
                    else
                        pokemonName = pokemon.getSpecies().getLocalizedName();
                }

                // Proceed with insertion.
                broadcast = broadcast.replaceAll("(?i)%pokemon%", pokemonName);
            }

            // Insert IV percentage. If our Pokémon's an egg, be careful and avoid spoiling stuff.
            // FIXME: Could do with an option, or a cleaner way to make this all work.
            if (pokemon.isEgg())
            {
                broadcast =
                        broadcast.replaceAll("(?i)%ivpercent%", PrintingMethods.getTranslation("placeholder.ivpercent.is_egg"));
            }
            else
            {
                // Set up IVs and matching math. These are used everywhere.
                final IVStore IVs = pokemon.getIVs();
                final int totalIVs =
                        IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                        IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
                final int percentIVs = (int) Math.round(totalIVs * 100.0 / 186.0);

                // Return the percentage.
                broadcast = broadcast.replaceAll("(?i)%ivpercent%", String.valueOf(percentIVs) + '%');
            }

            // Insert the "placeholder.shiny" String, if applicable. Gotta be careful with eggs again.
            if (!pokemon.isEgg() && pokemon.isShiny())
                broadcast = broadcast.replaceAll("(?i)%shiny%", PrintingMethods.getTranslation("placeholder.shiny"));
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
                    final EntityPixelmon entity2 = (EntityPixelmon) object2;

                    // Extract a Pokemon object for later use.
                    pokemon2 = entity2.getPokemonData();

                    // Get a location, and do a sanity check on it to work around possible entity removal issues.
                    // (if both are zero, something might have broken -- we'll try getting the info from the player instead)
                    location = entity2.getPosition();
                    if (!(location.getX() == 0 && location.getZ() == 0))
                    {
                        // Get the Pokémon's biome, nicely formatted (spaces!) and all. Replace placeholder.
                        final String biome2 = getFormattedBiome(entity2.getEntityWorld(), location);
                        broadcast = broadcast.replaceAll("(?i)%biome2%", biome2);

                        // Insert a world name.
                        broadcast = broadcast.replaceAll("(?i)%world2%", entity2.getEntityWorld().getWorldInfo().getWorldName());

                        // Insert coordinates.
                        broadcast = broadcast.replaceAll("(?i)%xpos2%", String.valueOf(location.getX()));
                        broadcast = broadcast.replaceAll("(?i)%ypos2%", String.valueOf(location.getY()));
                        broadcast = broadcast.replaceAll("(?i)%zpos2%", String.valueOf(location.getZ()));
                    }
                }
                else
                    pokemon2 = (Pokemon) object2;

                // Insert the Pokémon's name.
                if (broadcast.toLowerCase().contains("%pokemon2%"))
                {
                    // First see if the Pokémon is an egg. If it is, be extra careful and don't spoil the name.
                    // FIXME: Could do with an option, or a cleaner way to make this all work.
                    final String pokemon2Name;
                    if (pokemon2.isEgg())
                        pokemon2Name = PrintingMethods.getTranslation("placeholder.pokemon.is_egg");
                    else if (pokemon2.getFormEnum() == RegionalForms.ALOLAN)
                        pokemon2Name = PrintingMethods.getTranslation("insert.alolan") + pokemon2.getSpecies().getLocalizedName();
                    else if (pokemon2.getFormEnum() == RegionalForms.GALARIAN)
                        pokemon2Name = PrintingMethods.getTranslation("insert.galarian") + pokemon2.getSpecies().getLocalizedName();
                    else
                    {
                        // Most Pokémon don't have Mega forms, so check in a try/catch block and store the result.
                        boolean isMega = false;
                        try
                        {
                            // Is there a Mega form for this Pokémon? Pixelmon seems to always spawn as Mega if available.
                            if (EnumMegaPokemon.getMega(pokemon2.getSpecies()).numMegaForms > 0)
                                isMega = true;
                        }
                        catch(Exception ignored){}

                        if (isMega)
                            pokemon2Name = PrintingMethods.getTranslation("insert.mega") + pokemon2.getSpecies().getLocalizedName();
                        else
                            pokemon2Name = pokemon2.getSpecies().getLocalizedName();
                    }

                    // Proceed with insertion.
                    broadcast = broadcast.replaceAll("(?i)%pokemon2%", pokemon2Name);
                }

                // Insert IV percentage. If our Pokémon's an egg, be careful and avoid spoiling stuff.
                // FIXME: Could do with an option, or a cleaner way to make this all work.
                if (pokemon2.isEgg())
                {
                    broadcast =
                            broadcast.replaceAll("(?i)%ivpercent2%", PrintingMethods.getTranslation("placeholder.ivpercent.is_egg"));
                }
                else
                {
                    // Set up IVs and matching math. These are used everywhere.
                    final IVStore IVs = pokemon2.getIVs();
                    final int totalIVs =
                            IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                            IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
                    final int percentIVs = (int) Math.round(totalIVs * 100.0 / 186.0);

                    // Return the percentage.
                    broadcast = broadcast.replaceAll("(?i)%ivpercent2%", String.valueOf(percentIVs) + '%');
                }

                // Insert the "placeholder.shiny" String, if applicable. Gotta be careful with eggs again.
                if (!pokemon2.isEgg() && pokemon2.isShiny())
                    broadcast = broadcast.replaceAll("(?i)%shiny2%", PrintingMethods.getTranslation("placeholder.shiny"));
                else
                    broadcast = broadcast.replaceAll("(?i)%shiny2%", "");
            }
        }

        // Do we have a player entity? Replace player-specific placeholders as well as some that we might not have yet.
        if (player1 != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player%", player1.getName());

            // Get the player's location. We prefer using the Pokémon's location, but if that fails this should catch it.
            location = player1.getPosition();

            // Get the player's biome, nicely formatted (spaces!) and all. Replace placeholder if it still exists.
            final String biome = getFormattedBiome(player1.getEntityWorld(), location);
            broadcast = broadcast.replaceAll("(?i)%biome%", biome);

            // Insert a world name if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%world%", player1.getEntityWorld().getWorldInfo().getWorldName());

            // Insert coordinates if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%xpos%", String.valueOf(location.getX()));
            broadcast = broadcast.replaceAll("(?i)%ypos%", String.valueOf(location.getY()));
            broadcast = broadcast.replaceAll("(?i)%zpos%", String.valueOf(location.getZ()));
        }

        // Do we have a second player? Replace player-specific placeholders as well as some that we might not have yet.
        if (player2 != null)
        {
            // Insert the player's name.
            broadcast = broadcast.replaceAll("(?i)%player2%", player2.getName());

            // Get the player's location. We prefer using the Pokémon's location, but if that fails this should catch it.
            location = player2.getPosition();

            // Get the player's biome, nicely formatted (spaces!) and all. Replace placeholder if it still exists.
            final String biome2 = getFormattedBiome(player2.getEntityWorld(), location);
            broadcast = broadcast.replaceAll("(?i)%biome2%", biome2);

            // Insert a world name if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%world2%", player2.getEntityWorld().getWorldInfo().getWorldName());

            // Insert coordinates if necessary, still.
            broadcast = broadcast.replaceAll("(?i)%xpos2%", String.valueOf(location.getX()));
            broadcast = broadcast.replaceAll("(?i)%ypos2%", String.valueOf(location.getY()));
            broadcast = broadcast.replaceAll("(?i)%zpos2%", String.valueOf(location.getZ()));
        }

        return broadcast;
    }

    // Checks whether the provided toggle flags are turned on. Only returns false if all toggles are set and turned off.
    public static boolean wantsBroadcast(final EntityPlayer recipient, final String... flags)
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
        // Grab the name. Cast the World object to Sponge's World so we can do this without needing AT trickery.
        // Grab a biome name. This compiles fine if the access transformer is loaded correctly, despite any errors.
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
    private static ITextComponent getHoverableLine(
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
        final int totalIVs =
                IVs.get(StatsType.HP) + IVs.get(StatsType.Attack) + IVs.get(StatsType.Defence) +
                IVs.get(StatsType.SpecialAttack) + IVs.get(StatsType.SpecialDefence) + IVs.get(StatsType.Speed);
        final int percentIVs = (int) Math.round(totalIVs * 100.0 / 186.0);

        // Grab a growth string.
        final EnumGrowth growth = pokemon.getGrowth();
        final String sizeString = PrintingMethods.getTensedTranslation(isPresentTense, "hover.size." + growth.name().toLowerCase());

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
                    statString = PrintingMethods.getTranslation("hover.status.hp");
                    statValue = HPIV;
                    break;
                }
                case 1:
                {
                    statString = PrintingMethods.getTranslation("hover.status.attack");
                    statValue = attackIV;
                    break;
                }
                case 2:
                {
                    statString = PrintingMethods.getTranslation("hover.status.defense");
                    statValue = defenseIV;
                    break;
                }
                case 3:
                {
                    statString = PrintingMethods.getTranslation("hover.status.special_attack");
                    statValue = spAttIV;
                    break;
                }
                case 4:
                {
                    statString = PrintingMethods.getTranslation("hover.status.special_defense");
                    statValue = spDefIV;
                    break;
                }
                case 5:
                {
                    statString = PrintingMethods.getTranslation("hover.status.speed");
                    statValue = speedIV;
                    break;
                }
            }

            if (statValue < 31)
                ivsLine.append(PrintingMethods.getTranslation("hover.status.below_max", statValue, statString));
            else
                ivsLine.append(PrintingMethods.getTranslation("hover.status.maxed_out", statValue, statString));

            if (i < 5)
                ivsLine.append(PrintingMethods.getTranslation("hover.status.separator"));
        }

        // Grab a gender string.
        final String genderString;
        switch (pokemon.getGender())
        {
            case Male:
                genderString = PrintingMethods.getTensedTranslation(isPresentTense, "hover.gender.male"); break;
            case Female:
                genderString = PrintingMethods.getTensedTranslation(isPresentTense, "hover.gender.female"); break;
            default:
                genderString = PrintingMethods.getTensedTranslation(isPresentTense, "hover.gender.none"); break;
        }

        // Get a nature and see which stats we get from it.
        final EnumNature nature = pokemon.getNature();
        final String natureString = PrintingMethods.getTranslation("hover.nature." + nature.name().toLowerCase());
        final String boostedStat = PrintingMethods.getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).increasedStat);
        final String cutStat = PrintingMethods.getTranslatedNatureStat(EnumNature.getNatureFromIndex(nature.index).decreasedStat);
        final String natureCompositeString;

        // Grab the value of the increased stat (could use either). If it's "None", we have a neutral nature type.
        if (nature.increasedStat.equals(StatsType.None))
        {
            natureCompositeString =
                    PrintingMethods.getTensedTranslation(isPresentTense, "hover.nature.balanced", natureString, boostedStat, cutStat);
        }
        else
        {
            natureCompositeString =
                    PrintingMethods.getTensedTranslation(isPresentTense, "hover.nature.special", natureString, boostedStat, cutStat);
        }

        // Populate a List. Every entry will be its own line. May be a bit hacky, but it's easy to work with.
        final List<String> hovers = new ArrayList<>();

        // NOTE: Not shown on spawn/challenge. Shows bogus values on these events, Pixelmon issue, retest at some point.
        if (showIVs)
        {
            hovers.add(PrintingMethods.getTranslation("hover.current_ivs"));
            hovers.add(PrintingMethods.getTranslation("hover.total_ivs", totalIVs, percentIVs));
            hovers.add(PrintingMethods.getTranslation("hover.status.line_start") + ivsLine.toString());
        }

        // Print a header, as well as fancy broadcasts for the size, the gender and the nature.
        hovers.add(PrintingMethods.getTranslation("hover.info"));
        hovers.add(sizeString);
        hovers.add(genderString);
        hovers.add(natureCompositeString);

        // If ability showing is on, also do just that.
        if (showAbilities)
        {
            if (pokemon.getAbility().getName().equals(pokemon.getBaseStats().abilities[2]))
            {
                hovers.add(PrintingMethods.getTensedTranslation(
                        isPresentTense, "hover.hidden_ability", pokemon.getAbility().getTranslatedName().getUnformattedText()));
            }
            else
            {
                hovers.add(PrintingMethods.getTensedTranslation(
                        isPresentTense, "hover.ability", pokemon.getAbility().getTranslatedName().getUnformattedText()));
            }
        }

        ITextComponent broadcastComponent = new TextComponentString(broadcast);
        broadcastComponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(String.join("\n§r", hovers))));

        // Make a finalized broadcast that we can show, and add a hover. Return the whole thing.
        return broadcastComponent;
    }
}
