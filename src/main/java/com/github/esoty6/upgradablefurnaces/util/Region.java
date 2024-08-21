package com.github.esoty6.upgradablefurnaces.util;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import com.github.jikoo.planarwrappers.util.Coords;

public record Region(String worldName, int x, int z) {

  public Region(Chunk chunk) {
    this(chunk.getWorld().getName(), Coords.chunkToRegion(chunk.getX()),
        Coords.chunkToRegion(chunk.getZ()));
  }

  public Region(Block block) {
    this(block.getWorld().getName(), Coords.blockToRegion(block.getX()),
        Coords.blockToRegion(block.getZ()));
  }

  public boolean anyChunkMatch(BiPredicate<Integer, Integer> chunkPredicate) {
    int minChunkX = Coords.regionToChunk(x);
    int minChunkZ = Coords.regionToChunk(z);
    int maxChunkX = Coords.regionToChunk(x + 1);
    int maxChunkZ = Coords.regionToChunk(z + 1);

    for (int chunkX = minChunkX; chunkX < maxChunkX; ++chunkX) {
      for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; ++chunkZ) {
        if (chunkPredicate.test(chunkX, chunkZ)) {
          return true;
        }
      }
    }

    return false;
  }

  public void forEachChunk(BiConsumer<Integer, Integer> chunkConsumer) {
    int minChunkX = Coords.regionToChunk(x);
    int minChunkZ = Coords.regionToChunk(z);
    int maxChunkX = Coords.regionToChunk(x + 1);
    int maxChunkZ = Coords.regionToChunk(z + 1);

    for (int chunkX = minChunkX; chunkX < maxChunkX; ++chunkX) {
      for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; ++chunkZ) {
        chunkConsumer.accept(chunkX, chunkZ);
      }
    }
  }

}
