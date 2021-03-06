package com.starmediadev.plugins.starquests.objects.actions;

import com.starmediadev.plugins.starquests.objects.*;
import com.starmediadev.plugins.starquests.objects.data.AmountQuestData;
import com.starmediadev.plugins.starquests.storage.StorageHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.*;

import java.util.List;

/**
 * Represents an action for breaking a certain amount of a type of block
 */
public class PotionBrewAction extends EventAmountAction<PotionType, InventoryClickEvent> {
    
    /**
     * Construct a BlockBreakAction
     *
     * @param potionType The material that needs to be broken
     * @param amount     The amount of materials
     */
    public PotionBrewAction(PotionType potionType, int amount) {
        super("potionbrew", potionType, amount);
    }
    
    /**
     * Construct a BlockBreakAction
     *
     * @param potionTypes The materials that needs to be broken
     * @param amount      The amount of materials
     */
    public PotionBrewAction(List<PotionType> potionTypes, int amount) {
        super("potionbrew", potionTypes, amount);
    }
    
    /**
     * Handles the event for the action. This is used internally
     *
     * @param event          The Bukkit Event
     * @param player         The player
     * @param quest          The quest that is being referred to
     * @param questObjective The objective
     * @param storageHandler The storage handler
     * @param questData      The existing quest data. If it doesn't exist, it will be created
     */
    @SuppressWarnings("DuplicatedCode")
    @Override
    protected void handleEvent(InventoryClickEvent event, Player player, Quest quest, QuestObjective questObjective, StorageHandler storageHandler, AmountQuestData questData) {
        Inventory inventory = event.getClickedInventory();
        if (inventory.getType().equals(InventoryType.BREWING)) {
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack != null) {
                ItemMeta itemMeta = event.getCurrentItem().getItemMeta();
                if (itemMeta instanceof PotionMeta potionMeta) {
                    PotionData potionData = potionMeta.getBasePotionData();
                    if (types.contains(potionData.getType())) {
                        questData.increment();
                    }
                }
            }
        }
    }
}
