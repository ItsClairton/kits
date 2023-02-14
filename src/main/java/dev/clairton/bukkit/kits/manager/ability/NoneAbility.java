package dev.clairton.bukkit.kits.manager.ability;

import dev.clairton.bukkit.kits.manager.base.Ability;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class NoneAbility implements Ability {

    @Getter @Setter
    private ItemStack skillItem;

    @Getter @Setter
    private ItemStack selectorItem;

    @Override
    public String getId() {
        return "nenhum";
    }

}
