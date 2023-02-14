package dev.clairton.bukkit.kits.command;

import dev.clairton.bukkit.kits.Kits;
import dev.clairton.bukkit.kits.manager.base.Ability;
import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitCommand implements CommandExecutor, TabCompleter {

    public KitCommand(Kits instance) {
        val cmd = instance.getCommand("kit");

        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String lb, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Esse comando só está disponível in-game.");
            return true;
        }

        val player = (Player) sender;
        val gamer = Kits.getInstance().getManager().getGamer(player);
        if(args.length == 0) {
            gamer.openAbilityInventory();
            return true;
        }

        if(args[0].equalsIgnoreCase("reload") && sender.hasPermission("kits.reload")) {
            Kits.getInstance().reloadConfig();

            player.sendMessage(Kits.getInstance().Message("reload"));
            return true;
        }

        val ability = Kits.getInstance().getManager().getAbility(args[0]);
        if(ability == null) {
            player.sendMessage(Kits.getInstance().Message("errors.unknown"));
            player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 0.1F);
            return true;
        }

        gamer.handleSelect(ability);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String lb, String[] args) {
        val options = Kits.getInstance().getManager().getAbilities()
                .stream()
                .map(Ability::getId)
                .collect(Collectors.toList());

        val data = new ArrayList<String>();
        StringUtil.copyPartialMatches(String.join(" ", args), options, data);
        data.sort(String::compareTo);

        return data;
    }

}
