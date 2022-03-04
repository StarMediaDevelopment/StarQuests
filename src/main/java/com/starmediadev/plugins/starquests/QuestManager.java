package com.starmediadev.plugins.starquests;

import com.starmediadev.plugins.starquests.objects.Quest;
import com.starmediadev.plugins.starquests.objects.QuestObject;
import com.starmediadev.plugins.starquests.storage.StorageHandler;

public class QuestManager {
    
    private StorageHandler storageHandler;
    
    public QuestManager(StorageHandler storageHandler) {
        this.storageHandler = storageHandler;
    }
    
    public StorageHandler getStorageHandler() {
        return storageHandler;
    }
    
    public QuestObject get(String id) {
        return null; //TODO
    }
    
    public Quest getQuest(String questId) {
        return null;
    }
}
