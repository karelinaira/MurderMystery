/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2020  Plugily Projects - maintained by Tigerpanzer_02, 2Wild4You and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.murdermystery.events;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import plugily.projects.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.arena.ArenaRegistry;
import plugily.projects.murdermystery.arena.ArenaState;

/**
 * @author Plajer
 * <p>
 * Created at 05.08.2018
 */
public class LobbyEvent implements Listener {

  public LobbyEvent(Main plugin) {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onFoodLose(FoodLevelChangeEvent event) {
    if(event.getEntity().getType() != EntityType.PLAYER) {
      return;
    }
    Arena arena = ArenaRegistry.getArena((Player) event.getEntity());
    if(arena != null && (arena.getArenaState() == ArenaState.STARTING || arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onLobbyDamage(EntityDamageEvent event) {
    if(event.getEntity().getType() != EntityType.PLAYER) {
      return;
    }
    Player player = (Player) event.getEntity();
    Arena arena = ArenaRegistry.getArena(player);
    if(arena == null || arena.getArenaState() == ArenaState.IN_GAME) {
      return;
    }
    event.setCancelled(true);
    player.setFireTicks(0);
    player.setHealth(VersionUtils.getMaxHealth(player));
  }

  @EventHandler
  public void onItemFrameRotate(PlayerInteractEntityEvent event) {
    Player player = event.getPlayer();
    Arena arena = ArenaRegistry.getArena(player);
    if(arena == null || arena.getArenaState() == ArenaState.IN_GAME) {
      return;
    }
    if(event.getRightClicked() instanceof ItemFrame && !((ItemFrame) event.getRightClicked()).getItem().getType().equals(Material.AIR)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onHangingBreak(HangingBreakByEntityEvent event) {
    if(event.getEntity().getType() != EntityType.PLAYER) {
      return;
    }
    Player player = (Player) event.getEntity();
    Arena arena = ArenaRegistry.getArena(player);
    if(arena == null || arena.getArenaState() == ArenaState.IN_GAME) {
      return;
    }
    event.setCancelled(true);
  }

}
