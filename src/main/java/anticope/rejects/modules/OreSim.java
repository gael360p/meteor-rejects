package dev.beezil.oresim;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.ModuleCategory;
import meteordevelopment.meteorclient.settings.*;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public class OreSim extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> minY = sgGeneral.add(new IntSetting.Builder()
        .name("min-y")
        .description("Minimum Y to check for ores.")
        .defaultValue(0)
        .range(-64, 320)
        .sliderRange(-64, 320)
        .build()
    );

    private final Setting<Integer> maxY = sgGeneral.add(new IntSetting.Builder()
        .name("max-y")
        .description("Maximum Y to check for ores.")
        .defaultValue(320)
        .range(-64, 320)
        .sliderRange(-64, 320)
        .build()
    );

    public OreSim() {
        super(ModuleCategory.MISC, "ore-sim", "Simulates ore generation inside a chunk.");
    }

    @Override
    public void onActivate() {
        ChunkPos chunkPos = mc.player.getChunkPos();
        Chunk chunk = mc.world.getChunk(chunkPos.x, chunkPos.z);
        doMathOnChunk(chunk);
        toggle();
    }

    private void doMathOnChunk(Chunk chunk) {
        for (int x = chunk.getPos().getStartX(); x <= chunk.getPos().getEndX(); x++) {
            for (int z = chunk.getPos().getStartZ(); z <= chunk.getPos().getEndZ(); z++) {
                for (int y = minY.get(); y <= maxY.get(); y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    Biome biome = chunk.getBiome(pos).value();
                    RegistryKey<Biome> biomeKey = mc.world.getRegistryManager().get(RegistryKeys.BIOME).getKey(biome).orElse(null);

                    if (biomeKey == null) continue;

                    if (shouldPlace(chunk, pos)) {
                        info("Ore detected at " + pos);
                    }
                }
            }
        }
    }

    private boolean shouldPlace(Chunk chunk, BlockPos pos) {
        BlockState state = chunk.getBlockState(pos);
        if (!state.isAir()) return false;

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.offset(direction);
            BlockState neighborState = chunk.getBlockState(offsetPos);
            if (!neighborState.isOpaque()) {
                return true;
            }
        }

        return false;
    }
}
