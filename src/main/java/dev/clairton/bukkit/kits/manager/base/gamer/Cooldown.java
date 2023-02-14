package dev.clairton.bukkit.kits.manager.base.gamer;

import dev.clairton.bukkit.kits.Kits;
import lombok.AllArgsConstructor;
import lombok.val;
import net.minecraft.server.v1_8_R3.ChatMessage;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;

import java.text.DecimalFormat;

@AllArgsConstructor
public class Cooldown {

    private Gamer gamer;

    private String id;
    private String displayName;
    private String notify;

    private long epoch;
    private long totalDuration;

    private static final DecimalFormat df = new DecimalFormat("0.##");

    public boolean isValid() {
        if(System.currentTimeMillis() > epoch) {
            gamer.getCooldowns().remove(id);
            return false;
        }

        return true;
    }

    public boolean notifyCooldown(String key) {
        if(!isValid()) return false;

        val player = gamer.getPlayer();
        val remaining = (epoch - System.currentTimeMillis()) / 1000D;
        player.sendMessage(Kits.getInstance().Message(key).replace("%time%", df.format(remaining) + "s"));
        return true;
    }

    public void sendActionBar() {
        if(!isValid()) {
            val player = gamer.getPlayer();
            val ep = ((CraftPlayer) player).getHandle();

            if(ep.playerConnection == null || ep.playerConnection.isDisconnected()) return;
            ep.playerConnection.sendPacket(new PacketPlayOutChat(new ChatMessage(notify), (byte) 2));
            return;
        }

        val remaining = (epoch - System.currentTimeMillis()) / 1000D;
        val percentage = (remaining * 100) / totalDuration;
        val totalTraits = 20 - Math.max(percentage > 0D ? 1 : 0, percentage / 5);

        val sb = new StringBuilder();
        sb.append(displayName).append(" ");

        for (int greenTraits = 0; greenTraits < totalTraits; greenTraits++) sb.append("§a").append(":");
        for (int redTraits = 0; redTraits < 20-totalTraits; redTraits++) sb.append("§c").append(":");

        sb.append(" §f").append(df.format(remaining)).append("s");

        val player = gamer.getPlayer();
        val ep = ((CraftPlayer) player).getHandle();

        if(ep.playerConnection == null || ep.playerConnection.isDisconnected()) return;
        ep.playerConnection.sendPacket(new PacketPlayOutChat(new ChatMessage(sb.toString()), (byte) 2));
    }

}
