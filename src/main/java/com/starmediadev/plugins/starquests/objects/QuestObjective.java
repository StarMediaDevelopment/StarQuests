package com.starmediadev.plugins.starquests.objects;

import com.starmediadev.plugins.starmcutils.util.MCUtils;
import com.starmediadev.plugins.starquests.QuestManager;
import com.starmediadev.plugins.starquests.objects.actions.QuestAction;
import com.starmediadev.plugins.starquests.objects.rewards.QuestReward;
import com.starmediadev.plugins.starquests.storage.StorageHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class QuestObjective extends QuestObject {
    protected final QuestAction<?> questAction;
    
    public QuestObjective(String title, Quest parentQuest, QuestAction<?> questAction) {
        super(title);
        this.addPrerequisite(parentQuest);
        this.questAction = questAction;
    }
    
    public QuestObjective(String id, String title, QuestAction<?> questAction) {
        super(id, title);
        this.questAction = questAction;
    }
    
    @Override
    public void addPrerequisite(QuestObject questObject) {
        if (questObject instanceof Quest) {
            this.prerequisiteObjects.clear();
            this.prerequisiteObjects.add(questObject);
        }
    }
    
    public Quest getQuest() {
        if (this.prerequisiteObjects.size() == 1) {
            for (QuestObject object : this.prerequisiteObjects) {
                return (Quest) object;
            }
        }
        return null;
    }
    
    public QuestAction<?> getQuestAction() {
        return questAction;
    }
    
    public void complete(UUID uniqueId) {
        QuestManager questManager = getQuestManager();
        StorageHandler storageHandler = questManager.getStorageHandler();
        Quest quest = getQuest();
        if (!storageHandler.isQuestObjectiveComplete(uniqueId, quest.getId(), this.getId())) {
            storageHandler.setCompletedObjective(uniqueId, quest, this);
            Player player = Bukkit.getPlayer(uniqueId);
            if (player != null) {
                player.sendMessage(MCUtils.color("Completed Objective: " + getTitle()));
                for (QuestReward reward : getRewards()) {
                    try {
                        reward.applyReward(player);
                    } catch (Exception e) {
                        player.sendMessage(MCUtils.color("&cCould not apply reward " + e.getMessage()));
                    }
                }
            }
        }
        if (quest.isComplete(uniqueId)) {
            quest.complete(uniqueId);
        }
    }
    
    @Override
    public boolean meetsPrequisites(UUID player) {
        for (QuestObject object : this.prerequisiteObjects) {
            return object.isAvailable(player);
        }
        return true;
    }
    
    @Override
    public boolean isComplete(UUID player) {
        QuestManager questManager = getQuestManager();
        Quest quest = getQuest();
        if (questManager.isQuestComplete(player, quest)) {
            return true;
        }
        return questManager.isQuestObjectiveComplete(player, quest, this);
    }
    
    public void setQuest(Quest quest) {
        addPrerequisite(quest);
    }
}