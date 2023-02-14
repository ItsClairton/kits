package dev.clairton.bukkit.kits.manager.base;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.gamer.Gamer;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;

public interface Ability {

    String getId();

    ItemStack getSelectorItem();
    void setSelectorItem(ItemStack item);

    ItemStack getSkillItem();
    void setSkillItem(ItemStack item);

    default String getName() {
        return ChatColor.stripColor(getSelectorItem().getItemMeta().getDisplayName());
    }

    default void init(ConfigurationSection section) {}

    default void onRight(AbilityContext<? extends Cancellable> ignored) {}
    default void onLeft(AbilityContext<? extends Cancellable> ignored) {}

    default void onPickup(Gamer gamer) {
        val skillItem = getSkillItem();
        if(skillItem == null) return; // Future support for skills that don't need an item.

        gamer.giveItem(skillItem);
    }

    default void onRemove(Gamer gamer) {
        val skillItem = getSkillItem();
        if(skillItem == null) return;

        gamer.removeItem(skillItem);
    }

    // This is not the case, but if there are a lot of skills, it is recommended to register skill tasks according to players' demand to avoid unnecessary checks (e.g. in movement events, etc).
    default void registerTasks(Kits instance) {}

    default void unregisterTasks(Kits instance) {}

    default boolean handleClicks() {
        return true;
    }

}
