package dev.clairton.bukkit.kits.listener;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.AbilityContext;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;

public class CoreListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Kits.getInstance().getManager().createGamer(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Kits.getInstance().getManager().removeGamer(e.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction().equals(Action.PHYSICAL)) return;

        val gamer = Kits.getInstance().getManager().getGamer(e.getPlayer());
        val ability = gamer.getAbility();
        if(ability == null || !ability.handleClicks() || ability.getSkillItem() == null) return;

        val item = e.getItem();
        if(!ability.getSkillItem().isSimilar(item)) return;

        if(e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) ability.onRight(new AbilityContext<>(e, gamer, null, e.getClickedBlock()));
        if(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.LEFT_CLICK_BLOCK)) ability.onLeft(new AbilityContext<>(e, gamer, null, e.getClickedBlock()));

        if(e.isCancelled()) e.getPlayer().updateInventory();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        val gamer = Kits.getInstance().getManager().getGamer(e.getPlayer());
        val ability = gamer.getAbility();

        if(ability == null || ability.getSkillItem() == null) return;

        val drop = e.getItemDrop();
        val inventory = e.getPlayer().getInventory();
        if(inventory.first(ability.getSkillItem()) == -1) {
            if(inventory.firstEmpty() != -1) {
                e.setCancelled(true);
            } else {
                gamer.giveItem(drop.getItemStack());
                drop.remove();
            }
        } else {
            drop.remove();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        val gamer = Kits.getInstance().getManager().getGamer(e.getEntity());
        if(gamer.getAbility() == null || gamer.getAbility().getSkillItem() == null) return;

        gamer.removeItem(gamer.getAbility().getSkillItem());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        val gamer = Kits.getInstance().getManager().getGamer(e.getPlayer());
        if(gamer.getAbility() == null || gamer.getAbility().getSkillItem() == null) return;

        gamer.giveItem(gamer.getAbility().getSkillItem());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDrag(InventoryDragEvent e) {
        if(InventoryType.PLAYER.equals(e.getView().getType()) || InventoryType.CRAFTING.equals(e.getView().getType())) return;
        if(!(e.getWhoClicked() instanceof Player)) return;

        val gamer = Kits.getInstance().getManager().getGamer((Player) e.getWhoClicked());
        val ability = gamer.getAbility();

        if(ability == null || ability.getSkillItem() == null || !ability.getSkillItem().isSimilar(e.getOldCursor())) return;

        val player = gamer.getPlayer();
        val view = e.getView();
        for (Integer rawSlot : e.getRawSlots()) {
            val inventory = rawSlot < view.getTopInventory().getSize() ? view.getTopInventory() : view.getBottomInventory();
            if(player.equals(inventory.getHolder())) continue;

            e.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if(InventoryType.PLAYER.equals(e.getView().getType()) || InventoryType.CRAFTING.equals(e.getView().getType())) return;

        if(!(e.getWhoClicked() instanceof Player)) return;

        val gamer = Kits.getInstance().getManager().getGamer((Player) e.getWhoClicked());
        val ability = gamer.getAbility();

        if(ability == null || ability.getSkillItem() == null) return;
        val skillItem = ability.getSkillItem();

        val player = gamer.getPlayer();
        if(!skillItem.isSimilar(e.getCurrentItem()) && !skillItem.isSimilar(e.getCursor()) && (e.getHotbarButton() == -1 || !skillItem.isSimilar(player.getInventory().getItem(e.getHotbarButton())))) return;

        val click = e.getClick();
        if(click.isShiftClick()) {
            e.setCancelled(true);
            return;
        }

        val rawSlot = e.getRawSlot();

        val view = e.getView();
        val inventory = rawSlot < view.getTopInventory().getSize() ? view.getTopInventory() : view.getBottomInventory();
        if(!player.equals(inventory.getHolder())) e.setCancelled(true);
    }

}
