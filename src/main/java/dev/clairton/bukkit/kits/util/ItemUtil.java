package dev.clairton.bukkit.kits.util;

import dev.clairton.bukkit.kits.Kits;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;

public class ItemUtil {

    public static ItemStack NEXT_PAGE = createItem(Material.ARROW, "§aPróxima página");
    public static ItemStack PREV_PAGE = createItem(Material.ARROW, "§aPágina anterior");

    public static ItemStack fromSection(ConfigurationSection section) {
        if(section == null) return null;

        val rawMaterial = section.getString("type");
        val material = Material.getMaterial(rawMaterial);
        if(material == null) {
            Kits.getInstance().sendFatalLog("O material " + rawMaterial + " é inválido.");
            return null;
        }

        val durability = section.getInt("durability");

        val stack = new ItemStack(material, 1, (short) durability);
        val meta = stack.getItemMeta();

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', section.getString("name")));
        meta.setLore(section.getStringList("lore")
                .stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()));

        meta.spigot().setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        stack.setItemMeta(meta);
        return stack;
    }

    public static ItemStack createItem(Material material, String name) {
        val item = new ItemStack(material);
        val meta = item.getItemMeta();

        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack glow(ItemStack originalItem) {
        val meta = originalItem.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        originalItem.setItemMeta(meta);
        return originalItem;
    }

}
