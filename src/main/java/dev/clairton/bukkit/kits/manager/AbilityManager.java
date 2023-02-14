package dev.clairton.bukkit.kits.manager;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.ability.NoneAbility;
import dev.clairton.bukkit.kits.manager.ability.fly.FlyAbility;
import dev.clairton.bukkit.kits.manager.ability.launcher.LauncherAbility;
import dev.clairton.bukkit.kits.manager.ability.lumberjack.LumberjackAbility;
import dev.clairton.bukkit.kits.manager.base.Ability;
import dev.clairton.bukkit.kits.manager.base.gamer.Cooldown;
import dev.clairton.bukkit.kits.manager.base.gamer.Gamer;
import dev.clairton.bukkit.kits.menu.SelectorHolder;
import dev.clairton.bukkit.kits.util.ItemUtil;
import lombok.Getter;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AbilityManager {

    @Getter
    private final Ability defaultAbility = new NoneAbility();

    @Getter
    private final List<Ability> abilities = Arrays.asList(defaultAbility, new FlyAbility(), new LumberjackAbility(), new LauncherAbility());

    private final ConcurrentHashMap<UUID, Gamer> gamers = new ConcurrentHashMap<>();

    private final List<Ability> registeredAbilities = new ArrayList<>();

    @Getter
    private int itemsPerPage;

    public AbilityManager() {
        reloadAll();
        setupTimer();
    }

    public Ability getAbility(String name) {
        for (Ability ability : abilities) {
            if(ability.getId().equalsIgnoreCase(name)) return ability;
        }

        return null;
    }

    public Gamer getGamer(Player player) {
        return gamers.get(player.getUniqueId());
    }

    public void createGamer(Player player) {
        gamers.put(player.getUniqueId(), new Gamer(player));
    }

    public void removeGamer(Player player) {
        val gamer = getGamer(player);
        if(gamer == null) return;

        gamer.setAbility(null);
        gamers.remove(player.getUniqueId());
    }

    public void reloadAll() {
        for (Gamer gamer : gamers.values()) {
            val player = gamer.getPlayer();
            val inventory = player.getOpenInventory().getTopInventory();
            if(inventory != null && inventory.getHolder() instanceof SelectorHolder) player.closeInventory();

            if(gamer.getAbility() == null || gamer.getAbility().getSkillItem() == null) continue;
            gamer.removeItem(gamer.getAbility().getSkillItem());
        }

        val config = Kits.getInstance().getConfig();
        for (Ability ability : abilities) {
            val section = config.getConfigurationSection("abilities." + ability.getId());

            val selectorItem = ItemUtil.fromSection(section.getConfigurationSection("selector"));
            val skillItem = ItemUtil.fromSection(section.getConfigurationSection("skill"));

            // TODO: Include selector menu here;

            ability.setSelectorItem(selectorItem);
            ability.setSkillItem(skillItem);
            ability.init(section);
        }

        for (Gamer gamer : gamers.values()) {
            if(gamer.getAbility() == null || gamer.getAbility().getSkillItem() == null) continue;

            gamer.giveItem(gamer.getAbility().getSkillItem());
        }

        itemsPerPage = Kits.getInstance().getConfig().getInt("itemsPerPage");
    }

    public void checkRegister(Ability ability, boolean pickup) {
        if(ability == null || (pickup && registeredAbilities.contains(ability))) return;

        if(pickup) {
            registeredAbilities.add(ability);

            ability.registerTasks(Kits.getInstance());
            Kits.getInstance().log(ChatColor.GREEN, "Tasks and listeners for ability " + ability.getId() + " has been registered, Yay.");
        } else {
            for (Gamer gamer : gamers.values()) {
                if(gamer.hasAbility(ability)) return;
            }

            registeredAbilities.remove(ability);
            ability.unregisterTasks(Kits.getInstance());
            Kits.getInstance().log(ChatColor.YELLOW, "Tasks and listeners for ability " + ability.getId() + " has been unregistered, Yay.");
        }
    }

    private void setupTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Gamer gamer : gamers.values()) {
                    for (Cooldown cooldown : gamer.getCooldowns().values()) {
                        cooldown.sendActionBar();
                    }
                }
            }
        }, 0, 100);
    }

}
