package com.starmediadev.plugins.starquests.objects.registry;

import com.starmediadev.plugins.starquests.QuestManager;
import com.starmediadev.plugins.starquests.QuestUtils;
import com.starmediadev.plugins.starquests.objects.QuestLine;

/**
 * This is the registry for the quest line
 */
public class QuestLineRegistry extends QuestObjectRegistry<QuestLine> {
    /**
     * {@inheritDoc}
     */
    public QuestLineRegistry(QuestManager questManager) {
        super(questManager);
    }
    
    @Override
    protected String getCachedId(QuestLine object) {
        return questManager.getStorageHandler().getCachedQuestLineIds().get(object.getName());
    }
    
    @Override
    protected String generateId() {
        return QuestUtils.generateQuestLineId();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidId(String id) {
        return QuestUtils.isQuestLineId(id);
    }
}
