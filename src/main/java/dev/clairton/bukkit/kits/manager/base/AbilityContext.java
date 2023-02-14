package dev.clairton.bukkit.kits.manager.base;

import dev.clairton.bukkit.kits.manager.base.gamer.Gamer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.event.Event;

@Getter
@AllArgsConstructor
public class AbilityContext<S extends Event> {
    private S event;

    private Gamer gamer;
    private Gamer target;

    private Block targetBlock;
}
