package plugily.projects.murdermystery.arena;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.murdermystery.Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class WaitingRoom  extends BukkitRunnable {
  private static final int MIN_PLAYERS_COUNT = 2;

  private final Main plugin;
  private final Set<Player> players = new HashSet<>();
  private final Map<UUID, String> votingVoices = new HashMap<>();

  private WaitingRoomState state = WaitingRoomState.WAITING;
  private int timer = Integer.MAX_VALUE;

  public WaitingRoom(Main plugin) {
    this.plugin = plugin;
    startAwaiting();
  }

  public boolean isInWaitingRoom(Player player) {
    return isInWaitingRoom(player.identity().uuid());
  }

  public boolean isInWaitingRoom(UUID uuid) {
    for (Player p : players) {
      if(uuid.equals(p.identity().uuid())) {
        return true;
      }
    }
    return false;
  }

  public Set<Player> getPlayers() {
    return players;
  }

  public void addPlayerToWaitingRoom(Player player) {
    players.add(player);
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
    players.remove(player);
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
      if(isInWaitingRoom(playerId)) {
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

  private Set<String> getFreeArenasIds() {
    Set<String> freeArenasIds = new HashSet<>();
    for (Arena arena : ArenaRegistry.getArenas()) {
      if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
        freeArenasIds.add(arena.getMapName());
      }
    }
    return freeArenasIds;
  }

  public String getMaxVoicesArena() {
    Set<String> freeArenasIds = getFreeArenasIds();
    int maxVoicesCount = 0;
    String maxVoicesArena = null;
    for (Map.Entry<String, Integer> arenaVoicesCount : getArenasVoices().entrySet()) {
      String arena = arenaVoicesCount.getKey();
      Integer voicesCount = arenaVoicesCount.getValue();
      if(freeArenasIds.contains(arena) && voicesCount != null && voicesCount > maxVoicesCount) {
        maxVoicesCount = voicesCount;
        maxVoicesArena = arena;
      }
    }
    if (maxVoicesArena == null) {
      int arenasCount = ArenaRegistry.getArenas().size();
      int indexArena = new Random().nextInt(arenasCount);
      maxVoicesArena = ArenaRegistry.getArenas().get(indexArena).getMapName();
    }
    return maxVoicesArena;
  }


  public void startAwaiting() {
    runTaskTimer(plugin, 20L, 20L);
  }

  @Override
  public void run() {
    if(state == WaitingRoomState.WAITING && players.isEmpty()) {
      return;
    }
//
//    plugin.getLogger().log(Level.WARNING, " ======================================= ");
//
//    for (Player player : plugin.getWaitingRoom().getPlayers()) {
//      plugin.getLogger().log(Level.SEVERE, "player : " + player.identity().uuid().toString());
//    }
//    for (Map.Entry<String, Integer> entry : plugin.getWaitingRoom().getArenasVoices().entrySet()) {
//      plugin.getLogger().log(Level.SEVERE, "voices : " + entry.getKey().toString() + " : " + entry.getValue());
//    }

    switch (state) {
      case WAITING:
        if (players.size() < MIN_PLAYERS_COUNT) {
          return;
        }else if (players.size() >= 16) {
          timer = 5;
        }else {
          timer = 60;
        }
        for (Player player : players) {
          player.sendMessage(String.format("Игра начинается через %s секунд(ы)!", timer));
        }

        setState(WaitingRoomState.STARTING);
        break;

      case STARTING:
        if(players.size() < MIN_PLAYERS_COUNT) {
          setState(WaitingRoomState.WAITING);
          for (Player player : players) {
            player.sendMessage("Недостаточное количество игроков");
          }
          break;
        }

        if (timer <= 0) {
          timer = Integer.MAX_VALUE;
          //начинаем игру

          String maxVoicesArena = getMaxVoicesArena();
          Arena arena = ArenaRegistry.getArena(maxVoicesArena);
          System.out.println(arena.getId());
          System.out.println(arena.getMapName());

          for (Player player : new ArrayList<>(players)) {
            player.getInventory().clear();
            ArenaManager.joinAttempt(player, arena);
            // TODO
            removePlayerFromWaitingRoom(player);
          }
          arena.setForceStart(true);
          setState(WaitingRoomState.WAITING);

          break;

        } else {
          timer--;
        }

        if (timer % 15 == 0 || timer == 10 || timer <= 5) {
          for (Player player : players) {
            player.sendMessage(String.format("Игра начинается через %s секунд(ы)!", timer));
          }
        }
        break;
    }
  }

  public void setState(WaitingRoomState state) {
    this.state = state;
  }

  public enum WaitingRoomState {
    WAITING, STARTING;
  }
}
