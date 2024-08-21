package com.github.esoty6.upgradablefurnaces.block;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntSupplier;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.FurnaceStartSmeltEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import com.github.esoty6.upgradablefurnaces.registry.UpgradableFurnaceManager;

public class FurnaceListener implements Listener {

  private final UpgradableFurnaceManager manager;

  public FurnaceListener(UpgradableFurnaceManager manager) {
    this.manager = manager;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  void onFurnaceBurn(final FurnaceBurnEvent event) {
    var upgradableBlock = this.manager.getBlock(event.getBlock());

    if (!(upgradableBlock instanceof UpgradableFurnace upgradableFurnace)) {
      return;
    }

    if (!upgradableBlock.hasUpgrades()) {
      return;
    }

    event.setBurnTime(upgradableFurnace.applyBurnTimeModifiers(event.getBurnTime()));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  void onFurnaceStartSmelt(final FurnaceStartSmeltEvent event) {
    var upgradableBlock = this.manager.getBlock(event.getBlock());

    if (!(upgradableBlock instanceof UpgradableFurnace upgradableFurnace)) {
      return;
    }

    if (!upgradableBlock.hasUpgrades()) {
      return;
    }

    Furnace furnace = upgradableFurnace.getFurnaceTile();

    if (furnace == null) {
      return;
    }

    event.setTotalCookTime(upgradableFurnace.applyCookTimeModifiers(event.getTotalCookTime()));
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
  void onFurnaceSmelt(FurnaceSmeltEvent event) {
    Boolean isCustomRecipe = applyCustomRecipes(event);

    if (isCustomRecipe) {
      return;
    }

    var upgradableBlock = this.manager.getBlock(event.getBlock());

    if (!(upgradableBlock instanceof UpgradableFurnace upgradableFurnace)) {
      return;
    }

    if (!upgradableBlock.hasUpgrades()) {
      return;
    }

    Furnace furnace = upgradableFurnace.getFurnaceTile();

    if (furnace == null) {
      return;
    }

    Double fortune = upgradableFurnace.getFortune();
    ItemStack result = event.getResult();

    Integer tillFullStack =
        result.getType().getMaxStackSize() - ((furnace.getInventory().getResult() == null ? 0
            : furnace.getInventory().getResult().getAmount()) + result.getAmount());

    if (fortune > 0 && tillFullStack > 0) {
      applyFortune(event, fortune, tillFullStack);
    }
  }

  private void applyFortune(final FurnaceSmeltEvent event, final Double fortune,
      Integer tillFullStack) {
    applyFortune(event, () -> getFortuneResult(fortune), tillFullStack);
  }

  void applyFortune(final FurnaceSmeltEvent event, IntSupplier bonusCalculator,
      Integer tillFullStack) {
    ItemStack result = event.getResult();

    if (tillFullStack == 0) {
      return;
    }

    Integer bonus = Math.min(tillFullStack, bonusCalculator.getAsInt());

    if (bonus <= 0) {
      return;
    }

    result.setAmount(result.getAmount() + bonus);
    event.setResult(result);
  }

  private Boolean applyCustomRecipes(FurnaceSmeltEvent event) {
    ItemStack result = event.getResult();
    ItemStack eventItem = event.getSource();

    if (eventItem.getItemMeta() instanceof Damageable res) {
      if (eventItem.getType().getMaxDurability() < 1) {
        return false;
      }

      double damagePercentage =
          ((double) res.getDamage()) / ((double) eventItem.getType().getMaxDurability());;

      if (damagePercentage < 0.855d) {
        result
            .setAmount((int) Math.max(1, Math.round(result.getAmount() * (1d - damagePercentage))));
      } else {
        result.setAmount(0);
      }

      event.setResult(result);
      return true;
    }

    return false;
  }

  Integer getFortuneResult(Double maxBonus) {
    return ThreadLocalRandom.current().nextInt(-1, maxBonus.intValue() + 1);
  }

}
