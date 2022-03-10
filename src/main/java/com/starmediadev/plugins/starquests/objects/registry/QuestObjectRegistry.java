package com.starmediadev.plugins.starquests.objects.registry;

import com.starmediadev.plugins.starquests.QuestManager;
import com.starmediadev.plugins.starquests.objects.QuestObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A registry for quest objects.
 * @param <Q> The quest object type
 */
public abstract class QuestObjectRegistry<Q extends QuestObject> {
    /**
     * The quest manager
     */
    protected QuestManager questManager;
    /**
     * All registered objects in this registry object
     */
    protected Map<String, Q> registeredObjects = new HashMap<>();
    
    /**
     * Constructs a new QuestObjectRegistry
     * @param questManager The quest manager to be used
     */
    public QuestObjectRegistry(QuestManager questManager) {
        this.questManager = questManager;
    }
    
    /**
     * Registers a new quest object
     * @param questObject The object to register
     */
    public void register(Q questObject) {
        registeredObjects.put(questObject.getId(), questObject);
    }
    
    /**
     * Checks to see if the provided id is a valid one
     * @param id The id to check
     * @return The 
     */
    public abstract boolean isValidId(String id);
    
    /**
     * Gets an object
     * @param identifier This is the identifier, either the ID or a name
     * @return The registered object or null if not found
     */
    public Q get(String identifier) {
        Q object = null;
        if (isValidId(identifier)) {
            object = registeredObjects.get(identifier);
        } 
        if (object == null) {
            for (Q value : registeredObjects.values()) {
                String name = value.getName();
                if (identifier.equalsIgnoreCase(name)) {
                    return value;
                }
            }
        }
        return object;
    }
    
    /**
     * Gets all registered objects
     * @return All registered objects
     */
    public List<Q> getAllRegistered() {
        return new ArrayList<>(registeredObjects.values());
    }
    
    /**
     * Gets the referenced questmanager
     * @return The quest manager
     */
    public QuestManager getQuestManager() {
        return questManager;
    }
}
