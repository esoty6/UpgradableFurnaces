package com.github.esoty6.upgradablefurnaces.block;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import com.github.esoty6.upgradablefurnaces.config.FurnaceConfig;
import com.github.esoty6.upgradablefurnaces.constants.Key;
import com.github.esoty6.upgradablefurnaces.constants.Upgrade;
import com.github.esoty6.upgradablefurnaces.registry.FurnaceRegistration;
import com.github.esoty6.upgradablefurnaces.util.MathHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

public class UpgradableFurnace extends UpgradableBlock {

  public UpgradableFurnace(final FurnaceRegistration registration, final Block block,
      ItemStack itemStack, final ConfigurationSection storage) {
    super(registration, block, itemStack, storage);

    itemStack = getItemStack();

    setDirty(true);
  }

  @Override
  public FurnaceRegistration getRegistration() {
    return (FurnaceRegistration) super.getRegistration();
  }

  @Override
  public FurnaceConfig getConfig() {
    return getRegistration().getConfig();
  }

  public Furnace getFurnaceTile() {
    BlockState state = getBlock().getState();
    return state instanceof Furnace furnace ? furnace : null;
  }

  public Double getFuelPenaltyModifier() {
    if (getConfig().isStepMethodEnabled()) {
      return getConfig().getModifier(getCurrentLevel(Upgrade.EFFICIENCY), Upgrade.EFFICIENCY)
          .get("fuelPenalty");
    }

    return getCurrentLevel(Upgrade.EFFICIENCY).doubleValue();
  }

  public Double getCookModifier() {
    if (getConfig().isStepMethodEnabled()) {
      return getConfig().getModifier(getCurrentLevel(Upgrade.EFFICIENCY), Upgrade.EFFICIENCY)
          .get("modifier");
    }

    return getCurrentLevel(Upgrade.EFFICIENCY).doubleValue();
  }

  public Double getBurnModifier() {
    if (getConfig().isStepMethodEnabled()) {
      return getConfig()
          .getModifier(getCurrentLevel(Upgrade.FUEL_EFFICIENCY), Upgrade.FUEL_EFFICIENCY)
          .get("modifier");
    }

    return getCurrentLevel(Upgrade.FUEL_EFFICIENCY).doubleValue();
  }

  public Double getFortuneModifier() {
    if (getConfig().isStepMethodEnabled()) {
      return getConfig().getModifier(getCurrentLevel(Upgrade.FORTUNE), Upgrade.FORTUNE)
          .get("modifier");
    }

    return getCurrentLevel(Upgrade.FORTUNE).doubleValue();
  }

  public Double getFortune() {
    try {
      getFortuneModifier();
    } catch (Exception e) {
      return 0d;
    }

    if (getConfig().isStepMethodEnabled()) {
      Double modifier = getFortuneModifier() / 100;
      Double d = ThreadLocalRandom.current().nextDouble();

      if (d > modifier) {
        return 0d;
      }
    }

    return getFortuneModifier();
  }

  @Override
  public void setItemStack(ItemStack itemStack) {
    super.setItemStack(itemStack);
    getFurnaceTile().update();
  }

  short applyCookTimeModifiers(double totalCookTime) {
    try {
      getCookModifier();
    } catch (Exception e) {
      return (short) totalCookTime;
    }
    if (getConfig().isStepMethodEnabled()) {
      Double modifier = getCookModifier();

      Double newTotalCookTime = totalCookTime * (1d + modifier / 100);

      return MathHelper.clampPositiveShort(newTotalCookTime);
    }

    return MathHelper
        .clampPositiveShort(MathHelper.sigmoid(totalCookTime, -getCookModifier(), 2.0));
  }

  short applyBurnTimeModifiers(int burnTime) {
    try {
      getBurnModifier();
    } catch (Exception e) {
      return (short) burnTime;
    }

    if (getConfig().isStepMethodEnabled()) {
      Double modifier = getBurnModifier();
      Double fuelPenaltyModifier;
      try {
        fuelPenaltyModifier = getFuelPenaltyModifier();
      } catch (Exception e) {
        fuelPenaltyModifier = null;
      }

      Double newBurnTime = burnTime * (1d + modifier / 100);

      if (fuelPenaltyModifier != null) {
        newBurnTime *= 1d - fuelPenaltyModifier / 100;
      }

      return MathHelper.clampPositiveShort(newBurnTime);
    }

    Double baseTicks = MathHelper.sigmoid(burnTime, getBurnModifier(), 3.0);

    return applyCookTimeModifiers(baseTicks);
  }

  public Integer getCurrentLevel(Upgrade upgrade) {
    return getRegistration().getStoredData(getItemStack().getItemMeta(), upgrade,
        Key.CURRENT_LEVEL_KEY, PersistentDataType.INTEGER);
  }

  public Double getNextLevel(Upgrade upgrade) {
    return getRegistration().getStoredData(getItemStack().getItemMeta(), upgrade,
        Key.NEXT_LEVEL_KEY, PersistentDataType.DOUBLE);
  }

  public Double getLevelProgress(Upgrade upgrade) {
    return getRegistration().getStoredData(getItemStack().getItemMeta(), upgrade,
        Key.LEVEL_PROGRESS_KEY, PersistentDataType.DOUBLE);
  }

  public void getInfo(Player player) {
    TextComponent info = new TextComponent();
    TextComponent lore = new TextComponent();

    info.setColor(ChatColor.GREEN);
    info.addExtra("\n===========");
    info.addExtra(" Upgrades ");
    info.addExtra("===========\n");

    for (String line : getItemStack().getItemMeta().getLore()) {
      lore.addExtra(line);
      lore.addExtra("\n");
    }

    TextComponent location = new TextComponent();
    location.setColor(ChatColor.GOLD);
    location.addExtra("Block at: ");
    location.addExtra("x=" + getFurnaceTile().getLocation().getX());
    location.addExtra(", y=" + getFurnaceTile().getLocation().getY());
    location.addExtra(", z=" + getFurnaceTile().getLocation().getZ());

    lore.addExtra(location);
    info.addExtra(lore);
    info.addExtra("\n");

    player.spigot().sendMessage(info);
  }

  public void maxLevelAcquired(Upgrade upgrade, Player player) {
    player.spigot().sendMessage(upgrade.getMessage());
  }

  @Override
  public String toString() {
    return "UpgradableFurnace{" + "block=" + getBlock() + "itemStack=" + getItemStack() + '}';
  }

}
