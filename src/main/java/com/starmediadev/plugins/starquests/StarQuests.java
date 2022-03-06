package com.starmediadev.plugins.starquests;

import com.starmediadev.plugins.starquests.cmds.QuestCommands;
import com.starmediadev.plugins.starquests.storage.StorageHandler;
import com.starmediadev.plugins.starquests.storage.YamlStorageHandler;
import com.starmediadev.plugins.starquests.cmds.QuestAdminCmds;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.bukkit.BukkitCommandHandler;

public class StarQuests extends JavaPlugin {
    
    private static StarQuests instance;
    
    public static StarQuests getInstance() {
        return instance;
    }
    
    private QuestManager questManager;
    
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        StorageHandler storageHandler = new YamlStorageHandler();
        questManager = new QuestManager(storageHandler);
        getServer().getServicesManager().register(QuestManager.class, questManager, this, ServicePriority.Highest);
        getServer().getPluginManager().registerEvents(new ActionListener(), this);
    
        CommandHandler commandHandler = BukkitCommandHandler.create(this);
        commandHandler.registerDependency(StarQuests.class, this);
        commandHandler.register(new QuestAdminCmds());
        commandHandler.register(new QuestCommands());
    }
    
    public QuestManager getQuestManager() {
        return questManager;
    }
}