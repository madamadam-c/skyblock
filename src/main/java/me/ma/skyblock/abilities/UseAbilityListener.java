package me.ma.skyblock.abilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.jeff_media.morepersistentdatatypes.DataType;

import me.ma.skyblock.Main;
import me.ma.skyblock.stats.resources.ResourceService;
import me.ma.skyblock.stats.resources.ResourceType;

public class UseAbilityListener implements Listener {
    private final ResourceService resourceService;
    public UseAbilityListener(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    void handleAbility(Player player, Ability ability) {
        var curmana = resourceService.get(player.getUniqueId(), ResourceType.MANA);
        if (curmana.getValue() < ability.getAbilityCost()) {
            player.sendMessage("Not enough mana!");
            return;
        }

        resourceService.set(player.getUniqueId(), ResourceType.MANA, curmana.getValue() - ability.getAbilityCost());
        ability.run(player);
    }

    void handleEvent(Player player, ItemStack item, ActivationType activationType) {
        if (item == null || !item.hasItemMeta()) return;

        if (!item.getItemMeta().getPersistentDataContainer().has(
            Main.getPlugin().getAbilitiesKey(), DataType.asMap(DataType.STRING, DataType.STRING))) return;

        var ability = item.getItemMeta().getPersistentDataContainer().
            get(Main.getPlugin().getAbilitiesKey(), DataType.asMap(DataType.STRING, DataType.STRING)).getOrDefault(activationType.getId(), null);

        if (ability == null) return;
        handleAbility(player, Ability.parse(ability));
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            handleEvent(event.getPlayer(), event.getItem(), ActivationType.RIGHT_CLICK);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            handleEvent(event.getPlayer(), event.getItem(), ActivationType.LEFT_CLICK);
        }
    }
}
