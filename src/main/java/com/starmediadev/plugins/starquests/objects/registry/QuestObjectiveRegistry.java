package com.starmediadev.plugins.starquests.objects.registry;

import com.starmediadev.plugins.starquests.QuestManager;
import com.starmediadev.plugins.starquests.QuestUtils;
import com.starmediadev.plugins.starquests.objects.QuestObjective;

public class QuestObjectiveRegistry extends QuestObjectRegistry<QuestObjective> {
    /**
     * {@inheritDoc}
     */
    public QuestObjectiveRegistry(QuestManager questManager) {
        super(questManager);
    }
    
    @Override
    protected String generateId() {
        return QuestUtils.generateObjectiveId();
    }
    
    /**
     * Registers a quest objective and adds it to the quest that it is a part of.
     * @param questObject The objective to register
     */
    @Override
    public void register(QuestObjective questObject) {
        questObject.getQuest().addObjective(questObject);
        super.register(questObject);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidId(String id) {
        return QuestUtils.isObjectiveId(id);
    }
}
