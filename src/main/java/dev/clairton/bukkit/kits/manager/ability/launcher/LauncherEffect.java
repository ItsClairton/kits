package dev.clairton.bukkit.kits.manager.ability.launcher;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.ItemStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public class LauncherEffect {
    private final String name;
    private final PotionEffectType type;
    private final PotionEffect effect;
    private final ItemStack item;

    private final int percentage;
    private final boolean bad;

    public LauncherEffect(ConfigurationSection section) {
        this.name = section.getString("name");

        this.percentage = section.getInt("percentage");
        this.bad = section.getBoolean("isBad");

        this.type = PotionEffectType.getByName(section.getName());
        if(type == null) throw new RuntimeException("invalid effect type");

        this.effect = new PotionEffect(type, section.getInt("time"), section.getInt("amplifier"));
        this.item = CraftItemStack.asNMSCopy(new Potion(PotionType.getByEffect(type)).splash().toItemStack(1));
    }

    public boolean isCandidate(boolean requiresBad) {
        if(requiresBad && !bad) return false;
        if(!requiresBad && bad) return false;

        return ThreadLocalRandom.current().nextInt(100) <= percentage;
    }

}
