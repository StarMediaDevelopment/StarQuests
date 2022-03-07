package com.starmediadev.plugins.starquests.storage;

import com.starmediadev.plugins.starmcutils.util.Config;
import com.starmediadev.plugins.starquests.StarQuests;
import com.starmediadev.plugins.starquests.objects.Quest;
import com.starmediadev.plugins.starquests.objects.QuestLine;
import com.starmediadev.plugins.starquests.objects.QuestObjective;
import com.starmediadev.plugins.starquests.objects.data.QuestData;
import com.starmediadev.utils.collection.ListMap;
import com.starmediadev.utils.collection.MultiMap;
import com.starmediadev.utils.helper.ReflectionHelper;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class YamlStorageHandler implements StorageHandler {
    
    private final StarQuests plugin = StarQuests.getInstance();
    private ListMap<UUID, QuestData> questDataMap = new ListMap<>();
    private ListMap<UUID, String> completedQuests = new ListMap<>();
    private MultiMap<UUID, String, List<String>> completedQuestObjectives = new MultiMap<>();
    private Config completedQuestsConfig, completedQuestObjectivesConfig, questDataConfig;
    
    
    @Override
    public void addQuestData(UUID uniqueId, QuestData value) {
        List<QuestData> questDataList = questDataMap.get(uniqueId);
        if (questDataList == null || questDataList.isEmpty()) {
            questDataMap.add(uniqueId, value);
        } else {
            boolean hasMatch = false;
            for (QuestData questData : questDataList) {
                if (questData.equals(value)) {
                    int index = questDataList.indexOf(questData);
                    questDataList.set(index, value);
                    hasMatch = true;
                    break;
                }
            }
            
            if (!hasMatch) {
                questDataList.add(value);
            }
        }
    }
    
    @Override
    public QuestData getQuestData(UUID uniqueId, Quest quest, QuestObjective questObjective) {
        if (questDataMap.containsKey(uniqueId)) {
            for (QuestData questData : questDataMap.get(uniqueId)) {
                if (questData.getQuestId().equals(quest.getId()) && questData.getQuestObjectiveId().equals(questObjective.getId())) {
                    return questData;
                }
            }
        }
        return null;
    }
    
    @Override
    public void removeQuestData(UUID uniqueId, Quest quest, QuestObjective questObjective) {
        if (questDataMap.containsKey(uniqueId)) {
            List<QuestData> questDataList = questDataMap.get(uniqueId);
            QuestData toRemove = null;
            for (QuestData questData : questDataList) {
                if (questData.getQuestId().equals(quest.getId()) && questData.getQuestObjectiveId().equals(questObjective.getId())) {
                    toRemove = questData;
                    break;
                }
            }
            
            if (toRemove != null) {
                questDataList.remove(toRemove);
            }
            questDataMap.put(uniqueId, questDataList);
        }
    }
    
    @Override
    public void removeCompletedObjectives(UUID uniqueId, Quest quest) {
        Map<String, List<String>> allCompletedObjectives = this.completedQuestObjectives.get(uniqueId);
        if (allCompletedObjectives != null && !allCompletedObjectives.isEmpty()) {
            allCompletedObjectives.remove(quest.getId());
        }
    }
    
    @Override
    public void removeCompletedObjective(UUID uuid, Quest quest, QuestObjective questObjective) {
        Map<String, List<String>> allCompletedObjectives = this.completedQuestObjectives.get(uuid);
        if (allCompletedObjectives != null && !allCompletedObjectives.isEmpty()) {
            List<String> completedObjectives = allCompletedObjectives.get(quest.getId());
            completedObjectives.remove(questObjective.getId());
        }
    }
    
    @Override
    public void setCompletedObjective(UUID uniqueId, Quest quest, QuestObjective questObjective) {
        List<String> completedObjectives = completedQuestObjectives.get(uniqueId, quest.getId());
        if (completedObjectives == null) {
            completedObjectives = new ArrayList<>();
            completedQuestObjectives.put(uniqueId, quest.getId(), completedObjectives);
        }
        completedObjectives.add(questObjective.getId());
        removeQuestData(uniqueId, quest, questObjective);
    }
    
    @Override
    public void setCompletedQuest(UUID uniqueId, Quest quest) {
        completedQuests.add(uniqueId, quest.getId());
        for (QuestObjective objective : quest.getObjectives()) {
            removeQuestData(uniqueId, quest, objective);
        }
        removeCompletedObjectives(uniqueId, quest);
    }
    
    @Override
    public boolean isQuestComplete(UUID uniqueId, String id) {
        if (completedQuests.containsKey(uniqueId)) {
            return completedQuests.get(uniqueId).contains(id);
        }
        return false;
    }
    
    @Override
    public boolean isQuestObjectiveComplete(UUID uniqueId, String questId, String objectiveId) {
        if (completedQuestObjectives.containsKey(uniqueId)) {
            List<String> completedObjectives = completedQuestObjectives.get(uniqueId, questId);
            if (completedObjectives != null) {
                return completedObjectives.contains(objectiveId);
            }
        }
        return false;
    }
    
    @Override
    public void setCompletedQuestLine(UUID uniqueId, QuestLine questLine) {
        //TODO
    }
    
    @Override
    public boolean isCompletedQuestLine(UUID uuid, QuestLine questLine) {
        return false; //TODO
    }
    
    @Override
    public void setup() {
        completedQuestsConfig = new Config(plugin, "completedquests.yml");
        completedQuestObjectivesConfig = new Config(plugin, "completedobjectives.yml");
        questDataConfig = new Config(plugin, "questdata.yml");
        
        completedQuestsConfig.setup();
        completedQuestObjectivesConfig.setup();
        questDataConfig.setup();
    }
    
    @Override
    public void saveData() {
        completedQuestsConfig.set("players", null);
        completedQuestsConfig.save();
        
        completedQuestObjectivesConfig.set("players", null);
        completedQuestObjectivesConfig.save();
        
        questDataConfig.set("players", null);
        questDataConfig.save();
        
        completedQuests.forEach((player, questsCompleted) -> completedQuestsConfig.set("players." + player.toString(), questsCompleted));
        
        for (UUID player : completedQuestObjectives.keySet()) {
            Map<String, List<String>> questsAndObjectives = completedQuestObjectives.get(player);
            Set<String> questIds = questsAndObjectives.keySet();
            for (String questId : questIds) {
                List<String> objectives = questsAndObjectives.get(questId);
                completedQuestObjectivesConfig.set("players." + player.toString() + ".quests." + questId + ".objectives", objectives);
            }
        }
        
        questDataMap.forEach((player, questDataList) -> {
            for (QuestData questData : questDataList) {
                Map<String, Object> data = new HashMap<>();
                data.put("className", questData.getClass().getName());
                Set<Field> fields = ReflectionHelper.getClassFields(questData.getClass());
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (ReflectionHelper.isFinal(field)) {
                        ReflectionHelper.makeNonFinal(field);
                    }
                    
                    try {
                        Object value;
                        if (field.getType().equals(UUID.class)) {
                            value = field.get(questData).toString();
                        } else {
                            value = field.get(questData);
                        }
                        data.put(field.getName(), value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                
                data.forEach((key, value) -> questDataConfig.set("players." + player.toString() + ".quests." + questData.getQuestId() + ".objectives." + questData.getQuestObjectiveId() + ".data." + key, value));
            }
        });
        
        this.completedQuestsConfig.save();
        this.completedQuestObjectivesConfig.save();
        this.questDataConfig.save();
    }
    
    @Override
    public void loadData() {
        if (completedQuestsConfig.contains("players")) {
            ConfigurationSection playersSection = completedQuestsConfig.getConfigurationSection("players");
            if (playersSection != null) {
                Set<String> players = playersSection.getKeys(false);
                if (players != null && !players.isEmpty()) {
                    for (String player : players) {
                        List<String> completedQuests = playersSection.getStringList(player);
                        this.completedQuests.put(UUID.fromString(player), completedQuests);
                    }
                }
            }
        }
    
        if (completedQuestObjectivesConfig.contains("players")) {
            ConfigurationSection playersSection = completedQuestObjectivesConfig.getConfigurationSection("players");
            if (playersSection != null) {
                Set<String> players = playersSection.getKeys(false);
                if (players != null && !players.isEmpty()) {
                    for (String player : players) {
                        ConfigurationSection questsSection = playersSection.getConfigurationSection(player + ".quests");
                        if (questsSection != null) {
                            Set<String> quests = questsSection.getKeys(false);
                            if (quests != null && !quests.isEmpty()) {
                                for (String quest : quests) {
                                    List<String> objectives = questsSection.getStringList(quest + ".objectives");
                                    this.completedQuestObjectives.put(UUID.fromString(player), quest, objectives);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (questDataConfig.contains("players")) {
            ConfigurationSection playersSection = questDataConfig.getConfigurationSection("players");
            if (playersSection != null) {
                Set<String> players = playersSection.getKeys(false);
                if (players != null && !players.isEmpty()) {
                    for (String player : players) {
                        ConfigurationSection questsSection = playersSection.getConfigurationSection(player + ".quests");
                        if (questsSection != null) {
                            Set<String> quests = questsSection.getKeys(false);
                            if (quests != null && !quests.isEmpty()) {
                                for (String quest : quests) {
                                    ConfigurationSection objectivesSection = questsSection.getConfigurationSection(quest + ".objectives");
                                    if (objectivesSection != null) {
                                        Set<String> objectives = objectivesSection.getKeys(false);
                                        if (objectives != null && !objectives.isEmpty()) {
                                            for (String objective : objectives) {
                                                ConfigurationSection dataSection = objectivesSection.getConfigurationSection(objective + ".data");
                                                if (dataSection != null) {
                                                    Set<String> dataKeys = dataSection.getKeys(false);
                                                    if (dataKeys != null && !dataKeys.isEmpty()) {
                                                        Map<String, Object> data = new HashMap<>();
                                                        for (String dataKey : dataKeys) {
                                                            data.put(dataKey, dataSection.get(dataKey));
                                                        }
                                                        
                                                        String className = (String) data.get("className");
                                                        try {
                                                            Class<?> dataClass = Class.forName(className);
                                                            Constructor<?> constructor = dataClass.getDeclaredConstructor();
                                                            constructor.setAccessible(true);
                                                            QuestData dataInstance = (QuestData) constructor.newInstance();
                                                            
                                                            Set<Field> fields = ReflectionHelper.getClassFields(dataClass);
                                                            for (Field field : fields) {
                                                                field.setAccessible(true);
                                                                if (ReflectionHelper.isFinal(field)) {
                                                                    ReflectionHelper.makeNonFinal(field);
                                                                }
                                                                
                                                                if (field.getType().equals(UUID.class)) {
                                                                    field.set(dataInstance, UUID.fromString((String) data.get(field.getName())));
                                                                } else {
                                                                    field.set(dataInstance, data.get(field.getName()));
                                                                }
                                                            }
                                                            
                                                            addQuestData(UUID.fromString(player), dataInstance);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void reload() {
        this.completedQuests.clear();
        this.completedQuestObjectives.clear();
        this.questDataMap.clear();
        this.completedQuestsConfig.reload();
        this.completedQuestObjectivesConfig.reload();
        this.questDataConfig.reload();
        loadData();
    }
}
