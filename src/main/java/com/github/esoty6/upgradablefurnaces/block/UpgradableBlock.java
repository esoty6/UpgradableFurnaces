package com.github.esoty6.upgradablefurnaces.block;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import com.github.esoty6.upgradablefurnaces.config.FurnaceConfig;
import com.github.esoty6.upgradablefurnaces.registry.FurnaceRegistration;

public abstract class UpgradableBlock {

  private final FurnaceRegistration registration;
  private final Block block;
  private final ConfigurationSection storage;

  private ItemStack itemStack;
  protected boolean dirty = false;

  protected UpgradableBlock(final FurnaceRegistration registration, final Block block,
      final ItemStack itemStack, final ConfigurationSection storage) {
    this.registration = registration;
    this.block = block;
    this.itemStack = itemStack.clone();

    if (this.itemStack.getAmount() > 1) {
      this.itemStack.setAmount(1);
    }

    this.storage = storage;
    updateStorage();
  }

  public Block getBlock() {
    return block;
  }

  public ItemStack getItemStack() {
    return itemStack;
  }

  public boolean isCorrectBlockType() {
    return isCorrectType(getBlock().getType());
  }

  public boolean isCorrectType(Material material) {
    return getRegistration().getMaterials().contains(material);
  }

  public void tick() {}

  public void setItemStack(ItemStack itemStack) {
    this.itemStack = itemStack;
    updateStorage();
  }

  public boolean isDirty() {
    updateStorage();
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public void updateStorage() {
    if (!itemStack.equals(getStorage().getItemStack("itemstack"))) {
      getStorage().set("itemstack", itemStack);
      dirty = true;
    }
  }

  protected ConfigurationSection getStorage() {
    return storage;
  }

  public FurnaceConfig getConfig() {
    return getRegistration().getConfig();
  }

  public FurnaceRegistration getRegistration() {
    return registration;
  }

  public boolean hasUpgrades() {
    return !getPersistentDataContainer().isEmpty();
  }

  public boolean hasUpgrade(NamespacedKey key) {
    return getPersistentDataContainer().has(key);
  }

  protected PersistentDataContainer getPersistentDataContainer() {
    return itemStack.getItemMeta().getPersistentDataContainer();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{block=" + block + ",itemStack=" + itemStack + "}";
  }

}
