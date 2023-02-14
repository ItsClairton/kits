package dev.clairton.bukkit.kits.manager.base.gamer;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.Ability;
import dev.clairton.bukkit.kits.menu.SelectorHolder;
import lombok.*;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ConcurrentHashMap;

@Getter @RequiredArgsConstructor
public class Gamer {

    private final Player player;
    private Ability ability = Kits.getInstance().getManager().getDefaultAbility();

    private final ConcurrentHashMap<String, Cooldown> cooldowns = new ConcurrentHashMap<>();

    public void scheduleCooldown(Ability ability, long cooldown) {
        val notify = Kits.getInstance().Message("canUse")
                .replace("%name%", ability.getName());

        cooldowns.put(ability.getId(), new Cooldown(this, ability.getId(), ability.getName(), notify, System.currentTimeMillis()+cooldown, cooldown/1000));
    }

    public boolean isOnCooldown(Ability ability, boolean notify) {
        val cooldown = cooldowns.get(ability.getId());
        if(cooldown == null) return false;

        return notify ? cooldown.notifyCooldown("cooldown") : cooldown.isValid();
    }

    public void setAbility(Ability newAbility) {
        val oldAbility = ability;

        if(ability != null) ability.onRemove(this);
        if(newAbility != null) newAbility.onPickup(this);

        Kits.getInstance().getManager().checkRegister(newAbility, true);
        this.ability = newAbility;

        Kits.getInstance().getManager().checkRegister(oldAbility, false);
    }

    public void handleSelect(Ability selectedAbility) {
        player.closeInventory();

        if(ability == selectedAbility) {
            player.sendMessage(Kits.getInstance().Message("errors.same"));
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 0.1F);
            return;
        }

        setAbility(selectedAbility);
        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
    }

    public void openAbilityInventory() {
        val holder = new SelectorHolder(this);

        player.openInventory(holder.getInventory());
    }

    public void giveItem(ItemStack item) {
        val inventory = player.getInventory();
        if(inventory.first(item) != -1) return;

        var emptySlot = inventory.firstEmpty();
        if(emptySlot == -1) {
            emptySlot = 0;

            val inventoryItem = inventory.getItem(emptySlot);
            if(inventoryItem != null) {
                inventory.setItem(emptySlot, null);
                player.getWorld().dropItem(player.getLocation(), inventoryItem);
            }
        }

        player.getInventory().setItem(emptySlot, item);
    }

    public void removeItem(ItemStack item) {
        player.getInventory().removeItem(item);
    }

    public boolean hasAbility(Ability targetAbility) {
        return targetAbility == ability;
    }

    public Ability getAbility() {
        return ability == Kits.getInstance().getManager().getDefaultAbility() ? null : ability;
    }

    public Ability getRawAbility() {
        return ability;
    }

}
