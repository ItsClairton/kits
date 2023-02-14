package dev.clairton.bukkit.kits.menu;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.Ability;
import dev.clairton.bukkit.kits.manager.base.gamer.Gamer;
import dev.clairton.bukkit.kits.util.ItemUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Getter // TODO: Create generic handling for menus (onRight, onLeft, e.p.g)
public class SelectorHolder implements InventoryHolder {

    private final Gamer gamer;

    private Inventory inventory;

    private List<Ability> abilities;

    @Setter
    private int page = 0;

    public SelectorHolder(Gamer gamer) {
        this.gamer = gamer;
        this.inventory = Bukkit.createInventory(this, 6*9, "Seleção de Kits");

        buildInventory();
    }

    public void buildInventory() {
        if(inventory == null) return;

        inventory.clear();

        val totalAbilities = Kits.getInstance().getManager().getAbilities();

        val itemsPerPage = Kits.getInstance().getManager().getItemsPerPage();

        val hasNextPage = totalAbilities.size() > itemsPerPage*(page+1);
        abilities = totalAbilities.subList(itemsPerPage*page, hasNextPage ? itemsPerPage*(page+1) : totalAbilities.size());
        
        var slot = 10;
        for (Ability ability : abilities) {
            val item = gamer.getRawAbility() == ability ? ItemUtil.glow(ability.getSelectorItem().clone()) : ability.getSelectorItem();
            inventory.setItem(slot, item);

            slot++;
            if (slot == 17 || slot == 26 || slot == 35 || slot == 44) slot = slot + 2;
        }

        if(hasNextPage) inventory.setItem(inventory.getSize()-1, ItemUtil.NEXT_PAGE);
        if(page > 0) inventory.setItem(inventory.getSize()-9, ItemUtil.PREV_PAGE);
    }

    public Ability getAbility(ItemStack item) {
        for (Ability ability : abilities) {
            if(ability.getSelectorItem().equals(item)) return ability;
        }

        return null;
    }

    public void nextPage() {
        page++;
        buildInventory();
    }

    public void prevPage() {
        page--;
        buildInventory();
    }

}
