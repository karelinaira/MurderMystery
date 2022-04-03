package plugily.projects.murdermystery.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.murdermystery.Main;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class WaitingRoom  extends BukkitRunnable {
  private final Main plugin;
  private final Set<UUID> players = new HashSet<>();
  private final Map<UUID, String> votingVoices = new HashMap<>();

  public WaitingRoom(Main plugin) {
    this.plugin = plugin;
    startAwaiting();
  }

  public boolean isInWaitingRoom(Player player) {
    return players.contains(player.identity().uuid());
  }

  public Set<UUID> getPlayers() {
    return players;
  }

  public void addPlayerToWaitingRoom(Player player) {
    players.add(player.identity().uuid());
    for(int i =0; i <ArenaRegistry.getArenas().size(); i++){
      Arena arena = ArenaRegistry.getArenas().get(i);
      ItemStack itemStack = new ItemStack(Material.PAPER, 1);
      ItemMeta itemMeta = itemStack.getItemMeta();
      TextComponent textComponent = Component.text(arena.getMapName());
      itemMeta.displayName(textComponent);
      itemStack.setItemMeta(itemMeta);
      player.getInventory().setItem(i, itemStack);
    }
  }

  public void removePlayerFromWaitingRoom(Player player) {
    votingVoices.remove(player.identity().uuid());
    players.remove(player.identity().uuid());
  }

  public String voice(Player player, String arena) {
      String oldArena = votingVoices.put(player.identity().uuid(), arena);
      player.sendMessage("Вы проголосовали за карту " + arena);
      return oldArena;
  }

  public Map<String, Integer> getArenasVoices() {
    Map<String, Integer> result = new HashMap<>();
    for (Map.Entry<UUID, String> entry : votingVoices.entrySet()) {
      UUID playerId = entry.getKey();
      if(players.contains(playerId)) {
        String arena = entry.getValue();
        Integer voicesCount = result.get(arena);
        if (voicesCount == null) {
          voicesCount = 0;
        }
        result.put(arena, voicesCount + 1);
      }
    }
    return result;
  }

  public String getMaxVoicesArena(Collection<String> busyArenasIds) {
    int maxVoicesCount = 0;
    String maxVoicesArena = null;
    for (Map.Entry<String, Integer> arenaVoicesCount : getArenasVoices().entrySet()) {
      String arena = arenaVoicesCount.getKey();
      Integer voicesCount = arenaVoicesCount.getValue();
      if(!busyArenasIds.contains(arena) && voicesCount != null && voicesCount > maxVoicesCount) {
        maxVoicesCount = voicesCount;
        maxVoicesArena = arena;
      }
    }
    return maxVoicesArena;
  }


  public void startAwaiting() {
    runTaskTimer(plugin, 20L, 20L);
  }

  @Override
  public void run() {

  }
}
