package dev.clairton.bukkit.kits.manager.ability.fly;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.Ability;
import dev.clairton.bukkit.kits.manager.base.AbilityContext;
import dev.clairton.bukkit.kits.manager.base.gamer.Gamer;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.ConcurrentHashMap;

public class FlyAbility implements Ability {

    private long notifyRadius;
    private long totalCooldown;
    private long duration;

    @Getter @Setter
    private ItemStack skillItem;

    @Getter @Setter
    private ItemStack selectorItem;

    private BukkitTask checkerTask;
    private BukkitTask particlesTask;

    private final ConcurrentHashMap<Player, Long> cache = new ConcurrentHashMap<>();

    @Override
    public String getId() {
        return "voar";
    }

    @Override
    public void init(ConfigurationSection section) {
        notifyRadius = section.getLong("notifyRadius");
        totalCooldown = section.getLong("cooldown") * 1000;
        duration = section.getLong("duration") * 1000;
    }

    @Override
    public void registerTasks(Kits instance) {
        checkerTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Kits.getInstance(), () -> {
            cache.forEach((player, time) -> {
                val currentDuration = (time - System.currentTimeMillis()) / 1000;
                if(currentDuration <= 0) {
                    Bukkit.getScheduler().runTask(Kits.getInstance(), () -> {
                        if(player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
                            player.setFlying(false);
                            player.setAllowFlight(false);
                            player.setFallDistance(0);
                        }
                    });

                    cache.remove(player);
                    return;
                }

                if(currentDuration < 5) {
                    player.sendMessage(Kits.getInstance().Message("abilities.voar.countdown")
                            .replace("%time%", String.valueOf(currentDuration)));

                    player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
                }
            });
        }, 20L, 20L);

        particlesTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Kits.getInstance(), () -> {
            for (Player player : cache.keySet()) {
                if(player.isFlying()) player.getWorld().spigot().playEffect(
                        player.getLocation(),
                        Effect.CLOUD, 0, 10,
                        (float) 0, (float) 0, (float) 0,
                        0.00004f, 3, 32);
            }
        }, 1L, 1L);
    }

    @Override
    public void unregisterTasks(Kits instance) {
        if(checkerTask != null) checkerTask.cancel();
        if(particlesTask != null) particlesTask.cancel();

        cache.clear();
    }

    @Override
    public void onRight(AbilityContext<? extends Cancellable> ctx) {
        ctx.getEvent().setCancelled(true);

        val gamer = ctx.getGamer();
        if(gamer.isOnCooldown(this, true)) return;

        val player = gamer.getPlayer();

        gamer.scheduleCooldown(this, totalCooldown);
        cache.put(player, System.currentTimeMillis()+duration);

        val notify = Kits.getInstance().Message("abilities.voar.warn");

        player.setAllowFlight(true);
        player.setFlying(true);

        player.playSound(player.getLocation(), Sound.WITHER_DEATH, 1, 1);
        player.getNearbyEntities(notifyRadius, notifyRadius, notifyRadius)
                .forEach((entity) -> {
                    if(!entity.getType().equals(EntityType.PLAYER)) return;
                    val target = (Player) entity;

                    target.playSound(target.getLocation(), Sound.WITHER_DEATH, 1, 1);
                    target.sendMessage(notify);
                });
    }

    @Override
    public void onRemove(Gamer gamer) {
        val skillItem = getSkillItem();
        if(skillItem == null) return;

        gamer.removeItem(skillItem);

        val player = gamer.getPlayer();
        if(cache.remove(player) == null) return;

        if(player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE) {
            player.setFlying(false);
            player.setAllowFlight(false);
            player.setFallDistance(0);
        }
    }

}
