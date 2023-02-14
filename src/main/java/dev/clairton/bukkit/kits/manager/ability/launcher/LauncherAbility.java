package dev.clairton.bukkit.kits.manager.ability.launcher;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.Ability;
import dev.clairton.bukkit.kits.manager.base.AbilityContext;
import dev.clairton.bukkit.kits.manager.base.gamer.Gamer;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import net.minecraft.server.v1_8_R3.EntityPotion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class LauncherAbility implements Ability, Listener {

    @Getter @Setter
    private ItemStack skillItem;

    @Getter @Setter
    private ItemStack selectorItem;

    private long totalCooldown;
    private boolean randomBad;

    private List<LauncherEffect> effects;

    private final ConcurrentHashMap<UUID, LauncherEffect> instances = new ConcurrentHashMap<>();

    @Override
    public void init(ConfigurationSection section) {
        effects = new ArrayList<>();

        for (String key : section.getConfigurationSection("effects").getKeys(false)) {
            try {
                val instance = new LauncherEffect(section.getConfigurationSection("effects." + key));
                effects.add(instance);
            } catch (RuntimeException ex) {
                Kits.getInstance().log(ChatColor.RED, ex.getMessage());
            }
        }

        totalCooldown = section.getLong("cooldown")*1000;
        randomBad = section.getBoolean("randomBad");
    }

    @Override
    public String getId() {
        return "lançador";
    }

    @Override
    public void registerTasks(Kits instance) {
        Bukkit.getPluginManager().registerEvents(this, instance);
    }

    @Override
    public void unregisterTasks(Kits instance) {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void onRight(AbilityContext<? extends Cancellable> ctx) {
        ctx.getEvent().setCancelled(true);

        val gamer = ctx.getGamer();
        if(gamer.isOnCooldown(this, true)) return;

        throwPotion(gamer, randomEffect(randomBad && ThreadLocalRandom.current().nextBoolean()));
    }

    @Override
    public void onLeft(AbilityContext<? extends Cancellable> ctx) {
        ctx.getEvent().setCancelled(true);

        val gamer = ctx.getGamer();
        if(gamer.isOnCooldown(this, true)) return;

        throwPotion(gamer, randomEffect(!randomBad || ThreadLocalRandom.current().nextBoolean()));
    }

    @EventHandler(ignoreCancelled = true)
    private void onPotionThrow(PotionSplashEvent event) {
        val effect = instances.remove(event.getPotion().getUniqueId());
        if(effect == null) return;

        event.setCancelled(true);

        val thrownPotion = event.getPotion();
        val shooter = thrownPotion.getShooter();

        for (LivingEntity affectedEntity : event.getAffectedEntities()) {
            if(!effect.isBad()) {
                if(affectedEntity == shooter) affectedEntity.addPotionEffect(effect.getEffect(), true);
            } else {
                if(affectedEntity != shooter) affectedEntity.addPotionEffect(effect.getEffect(), true);
            }
        }
    }

    private void throwPotion(Gamer gamer, LauncherEffect effect) {
        if(effect == null) return;

        gamer.scheduleCooldown(this, totalCooldown);

        val player = gamer.getPlayer();
        val ws = ((CraftWorld) player.getWorld()).getHandle();
        val ep = ((CraftPlayer) player).getHandle();

        val splashPotion = new EntityPotion(ws, ep, effect.getItem());
        splashPotion.shooter = ep;

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) onlinePlayer.playSound(player.getLocation(), Sound.SHOOT_ARROW, 1, 0.1F);

        instances.put(splashPotion.getUniqueID(), effect);
        ws.addEntity(splashPotion);

        player.sendMessage(Kits.getInstance().Message("abilities.lançador.launched")
                .replace("%effect%", effect.getName()));
    }

    private LauncherEffect randomEffect(boolean bad) {
        for (LauncherEffect effect : effects) {
            if(effect.isCandidate(bad)) return effect;
        }

        for (LauncherEffect effect : effects) {
            if(effect.isBad() && bad) return effect;
            if(!effect.isBad() && !bad) return effect;
        }

        return null;
    }

}
