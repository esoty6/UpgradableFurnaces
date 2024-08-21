package com.github.esoty6.upgradablefurnaces.listener;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import com.github.esoty6.upgradablefurnaces.block.UpgradableFurnace;
import com.github.esoty6.upgradablefurnaces.registry.UpgradableFurnaceManager;

public class WorldListener implements Listener {

  private final Plugin plugin;
  private final UpgradableFurnaceManager manager;

  public WorldListener(Plugin plugin, UpgradableFurnaceManager manager) {
    this.plugin = plugin;
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  void onChunkLoad(ChunkLoadEvent event) {
    plugin.getServer().getScheduler().runTask(plugin,
        () -> manager.loadChunkBlocks(event.getChunk()));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  void onChunkUnload(ChunkUnloadEvent event) {
    manager.unloadChunkBlocks(event.getChunk());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  void onBlockPlace(BlockPlaceEvent event) {
    manager.createBlock(event.getBlock(), event.getItemInHand(), event.getBlockAgainst(),
        event.getPlayer());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();
    ItemStack drop = manager.destroyBlock(block);

    if (drop == null || drop.getType() == Material.AIR || !event.isDropItems()
        || event.getPlayer().getGameMode() == GameMode.CREATIVE) {
      return;
    }

    Player player = event.getPlayer();
    if (!block.isPreferredTool(player.getInventory().getItemInMainHand())) {
      return;
    }

    event.setDropItems(false);

    List<ItemStack> drops = new ArrayList<>();
    drops.add(drop);

    BlockState state = block.getState();
    if (state instanceof InventoryHolder holder) {
      for (ItemStack content : holder.getInventory().getContents()) {
        if (content != null && content.getType() != Material.AIR) {
          drops.add(content);
        }
      }

      holder.getInventory().clear();
    }

    plugin.getServer().getScheduler().runTask(plugin,
        () -> doBlockDrops(block, state, player, drops));
  }

  private void doBlockDrops(Block block, BlockState state, Player player, List<ItemStack> drops) {
    List<Item> itemEntities = new ArrayList<>();
    World world = block.getWorld();

    for (ItemStack itemStack : drops) {
      itemEntities.add(world.dropItemNaturally(block.getLocation(), itemStack));
    }

    BlockDropItemEvent event =
        new BlockDropItemEvent(block, state, player, new ArrayList<>(itemEntities));
    plugin.getServer().getPluginManager().callEvent(event);

    for (Item itemEntity : itemEntities) {
      if (!event.getItems().contains(itemEntity)) {
        itemEntity.remove();
      }
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  void onPlayerInteract(final PlayerInteractEvent event) {
    if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().isSneaking()
        && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
      Block block = event.getClickedBlock();
      UpgradableFurnace upgradedFurnace = manager.getBlock(block);

      if (upgradedFurnace != null && upgradedFurnace.hasUpgrades()) {
        upgradedFurnace.getInfo(event.getPlayer());
      }
    }
  }

}
