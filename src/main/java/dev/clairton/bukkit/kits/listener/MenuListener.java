package dev.clairton.bukkit.kits.listener;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.menu.SelectorHolder;
import dev.clairton.bukkit.kits.util.ItemUtil;
import lombok.val;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(InventoryClickEvent e) {
        if(!(e.getInventory().getHolder() instanceof SelectorHolder)) return;
        e.setCancelled(true);

        val holder = (SelectorHolder) e.getInventory().getHolder();
        val item = e.getCurrentItem();

        val ability = holder.getAbility(item);
        if(ability != null) {
            val player = (Player) e.getWhoClicked();

            val gamer = Kits.getInstance().getManager().getGamer(player);
            gamer.handleSelect(ability);
            return;
        }

        if(ItemUtil.PREV_PAGE.equals(item)) holder.prevPage();
        if(ItemUtil.NEXT_PAGE.equals(item)) holder.nextPage();
    }

}
