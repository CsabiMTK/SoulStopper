package com.example.iceandtorchcontrol;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.Monster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class IceAndTorchControl implements ModInitializer {

    private static final int RADIUS = 50;

    @Override
    public void onInitialize() {
        ServerTickEvents.END_WORLD_TICK.register(this::onWorldTick);
    }

    private void onWorldTick(ServerWorld world) {
        // Remove ice melting by canceling light-based melting manually
        BlockPos.iterateOutwards(BlockPos.ORIGIN, 128, 128, 128).forEach(pos -> {
            if (!world.isChunkLoaded(pos)) return;
            if (world.getBlockState(pos).getBlock() == Blocks.ICE ||
                world.getBlockState(pos).getBlock() == Blocks.SNOW ||
                world.getBlockState(pos).getBlock() == Blocks.FROSTED_ICE ||
                world.getBlockState(pos).getBlock() == Blocks.SNOW_BLOCK) {
                int light = world.getLightLevel(LightType.BLOCK, pos);
                if (light >= 11) {
                    // Reset light level or prevent melt manually by placing again
                    world.setBlockState(pos, world.getBlockState(pos));
                }
            }
        });

        // Despawn hostile mobs near torches
        world.getEntities().forEach(entity -> {
            if (!(entity instanceof Monster)) return;

            BlockPos entityPos = entity.getBlockPos();

            BlockPos.iterateOutwards(entityPos, RADIUS, RADIUS, RADIUS).forEach(pos -> {
                if (!world.isChunkLoaded(pos)) return;
                if (world.getBlockState(pos).isOf(Blocks.TORCH) ||
                    world.getBlockState(pos).isOf(Blocks.WALL_TORCH) ||
                    world.getBlockState(pos).isOf(Blocks.SOUL_TORCH) ||
                    world.getBlockState(pos).isOf(Blocks.SOUL_WALL_TORCH)) {
                    entity.discard();
                }
            });
        });
    }
}