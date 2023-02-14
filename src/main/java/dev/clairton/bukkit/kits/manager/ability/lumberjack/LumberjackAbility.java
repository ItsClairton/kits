package dev.clairton.bukkit.kits.manager.ability.lumberjack;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.Ability;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.var;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LumberjackAbility implements Ability, Listener {

    @Getter @Setter
    private ItemStack skillItem;

    @Getter @Setter
    private ItemStack selectorItem;

    private long blocksLimit;
    private boolean autoPickup;

    private final List<Material> types = Arrays.asList(Material.LOG, Material.LOG_2);

    @Override
    public void init(ConfigurationSection section) {
        this.blocksLimit = section.getLong("blocks");
        this.autoPickup = section.getBoolean("autoPickup");
    }

    @Override
    public String getId() {
        return "lenhador";
    }

    @Override
    public boolean handleClicks() {
        return false;
    }

    @Override
    public void registerTasks(Kits instance) {
        Bukkit.getPluginManager().registerEvents(this, Kits.getInstance());
    }

    @Override
    public void unregisterTasks(Kits instance) {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        val gamer = Kits.getInstance().getManager().getGamer(e.getPlayer());

        val mainBlock = e.getBlock();
        if(!types.contains(mainBlock.getType())) return;

        val handItem = gamer.getPlayer().getItemInHand();
        if(!gamer.hasAbility(this) || !getSkillItem().equals(handItem)) return;

        val player = e.getPlayer();

        val blocks = new ArrayList<Block>();
        getBlocks(mainBlock, blocks);

        player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);

        Bukkit.getScheduler().runTaskAsynchronously(Kits.getInstance(), () -> {
            for (Block block : blocks) {
                for (int i = 0; i < 3; i++) player.getWorld().playEffect(randomLocation(block.getLocation().clone()), Effect.HAPPY_VILLAGER, 20);
            }
        });

        for (Block block : blocks) {
            if(autoPickup) {
                val drops = block.getDrops(getSkillItem()).toArray(new ItemStack[0]);

                block.setType( Material.AIR, true);
                player.getInventory().addItem(drops).forEach((ignore, remaining) -> player.getWorld().dropItem(player.getLocation(), remaining));
            } else {
                block.breakNaturally();
            }
        }
    }

    private void getBlocks(Block block, List<Block> blocks) {
        if(blocks.size() >= blocksLimit) return;

        // Yes, this checks for all sides, to avoid chances of custom trees not having the woods connected.
        for (BlockFace face : BlockFace.values()) {
            if(face == BlockFace.SELF) continue;

            val foundBlock = block.getRelative(face);

            if(!blocks.contains(foundBlock) && (foundBlock.getType().equals(Material.LOG) || foundBlock.getType().equals(Material.LOG_2))) {
                blocks.add(foundBlock);
                getBlocks(foundBlock, blocks);
            }
        }
    }

    private Location randomLocation(Location location) {
        return location.clone().add(generateRandomFloat((float) -0.1, (float) 1), generateRandomFloat((float) -1, (float) 2), generateRandomFloat((float) -0.1, (float) 1));
    }

    private float generateRandomFloat(float min, float max) {
        var result = ThreadLocalRandom.current().nextFloat() * (max - min) + min;
        if (result >= max) result = Float.intBitsToFloat(Float.floatToIntBits(max) - 1);
        return result;
    }

}
