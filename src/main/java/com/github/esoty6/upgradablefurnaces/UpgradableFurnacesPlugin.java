package com.github.esoty6.upgradablefurnaces;

import java.io.File;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import com.github.esoty6.upgradablefurnaces.listener.WorldListener;
import com.github.esoty6.upgradablefurnaces.registry.FurnaceRegistration;
import com.github.esoty6.upgradablefurnaces.registry.UpgradableFurnaceManager;
import com.github.esoty6.upgradablefurnaces.registry.UpgradeBlocksRegistration;

public class UpgradableFurnacesPlugin extends JavaPlugin {

    private UpgradableFurnaceManager blockManager;

    public UpgradableFurnacesPlugin() {
        super();
    }

    public UpgradableFurnacesPlugin(JavaPluginLoader loader, PluginDescriptionFile description,
            File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onLoad() {
        blockManager = new UpgradableFurnaceManager(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new WorldListener(this, getBlockManager()),
                this);

        blockManager.getRegistry().register(new FurnaceRegistration(this, getBlockManager()));
        blockManager.getRegistry().register(new UpgradeBlocksRegistration(this));

        getServer().getScheduler().runTask(this, this::loadUpgradableFurnaces);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        blockManager.expireCache();
    }

    private void loadUpgradableFurnaces() {
        long startTime = System.nanoTime();

        for (World world : getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                blockManager.loadChunkBlocks(chunk);
            }
        }

        double elapsed = (System.nanoTime() - startTime) / 1_000_000_000D;
        getLogger().info(() -> "Loaded all furnaces in " + elapsed + " seconds");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("UpgradableBlocks v" + getDescription().getVersion());
            return false;
        }

        this.reloadConfig();
        this.blockManager.getRegistry().reload();
        sender.sendMessage("[UpgradableBlocks v" + getDescription().getVersion()
                + "] Reloaded config and registry cache.");
        return true;
    }

    public UpgradableFurnaceManager getBlockManager() {
        return blockManager;
    }

}
