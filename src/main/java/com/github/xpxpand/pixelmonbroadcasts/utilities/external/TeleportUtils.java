package com.github.xpxpand.pixelmonbroadcasts.utilities.external;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

/**
 * @author CraftSteamG
 *
 * Originally from the Cable libs, stripped and modified by XpanD for use in Broadcasts. Thanks!
 */
public class TeleportUtils
{
    public static void teleport(EntityPlayer player, int dimension, BlockPos pos) {
        int from = player.getEntityWorld().provider.getDimension();

        if (dimension == from) {
            player.moveToBlockPosAndAngles(pos, player.rotationYaw, player.rotationPitch);
        } else {
            player.changeDimension(dimension, new SimpleTeleporter(pos, player));
        }
    }

    private static class SimpleTeleporter implements ITeleporter
    {
        private final BlockPos pos;
        private final EntityPlayer player;

        private SimpleTeleporter(BlockPos pos, EntityPlayer player) {
            this.pos = pos;
            this.player = player;
        }

        @Override
        public void placeEntity(World world, Entity entity, float yaw) {
            entity.dismountRidingEntity();
            entity.moveToBlockPosAndAngles(this.pos, player.rotationYaw, player.rotationPitch);
        }
    }
}