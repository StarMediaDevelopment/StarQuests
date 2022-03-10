package com.starmediadev.plugins.starquests.objects.actions;

import com.starmediadev.plugins.starquests.QuestManager;
import com.starmediadev.plugins.starquests.objects.Quest;
import com.starmediadev.plugins.starquests.objects.QuestObjective;
import com.starmediadev.plugins.starquests.objects.data.QuestData;
import com.starmediadev.plugins.starquests.storage.StorageHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDeathEvent;

import java.lang.reflect.Method;

/**
 * An abstract class for actions based on an amount of an event. This can be any event
 * @param <K> The type to be completed
 * @param <D> The quest data type
 * @param <E> The bukkit event type
 */
public abstract class EventAmountAction<K, D extends QuestData, E extends Event> extends QuestAction<E> {
    
    /**
     * This is the type of thing being tracked
     */
    protected K type;
    /**
     * The amount that needs to reached
     */
    protected int amount;
    
    /**
     * Constructs an EventAmountAction
     * @param actionId Used internally, should be provided in the constructor of the child classes
     * @param type The type to be tracked
     * @param amount The amount to be reached
     */
    public EventAmountAction(String actionId, K type, int amount) {
        super(actionId);
        this.type = type;
        this.amount = amount;
    }
    
    /**
     * This method is called by the onAction method to make handling of these a little cleaner
     * @param event The Bukkit Event
     * @param quest The quest that is being referred to
     * @param questObjective The objective
     * @param storageHandler The storage handler
     * @param questData The existing quest data. If it doesn't exist, it will be created
     * @return The current amount, Used by the onAction method
     */
    protected abstract int handleEvent(E event, Quest quest, QuestObjective questObjective, StorageHandler storageHandler, D questData);
    
    /**
     * Overridden from QuestAction, handles what happens when an action happens
     * @param e The Bukkit Event
     * @param quest The quest
     * @param questObjective The objective
     */
    @Override
    public void onAction(E e, Quest quest, QuestObjective questObjective) {
        QuestManager questManager = quest.getQuestManager();
        StorageHandler storageHandler = questManager.getStorageHandler();
        Player player = null;
        try {
            if (e instanceof EntityDeathEvent ede) {
                player = ede.getEntity().getKiller();
            } else {
                for (Method method : e.getClass().getDeclaredMethods()) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType != null) {
                        if (Player.class.getName().equals(returnType.getName())) {
                            if (method.getParameters().length == 0) {
                                player = (Player) method.invoke(e);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        
        if (player == null) {
            throw new RuntimeException("There was a problem getting the player in an EventAmountAction of type " + getClass());
        }
        
        if (quest.isAvailable(player.getUniqueId())) {
            D questData = null;
            try {
                questData = (D) storageHandler.getQuestData(player.getUniqueId(), quest, questObjective);
            } catch (ClassCastException ex) {
            }
            
            int currentProgress = handleEvent(e, quest, questObjective, storageHandler, questData);
            
            if (currentProgress >= amount) {
                if (!questObjective.isComplete(player.getUniqueId())) {
                    questObjective.complete(player.getUniqueId());
                }
            }
        }
    }
}