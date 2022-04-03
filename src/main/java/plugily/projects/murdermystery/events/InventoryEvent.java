package plugily.projects.murdermystery.events;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugily.projects.murdermystery.Main;

import java.util.Objects;

public class InventoryEvent implements Listener {

  private final Main plugin;

  public InventoryEvent(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }


  @EventHandler
  public void onPlayerInteractEvent(PlayerInteractEvent event) {
    if (event.getAction() == Action.PHYSICAL) {
      return;
    }
    ItemStack currentItem = event.getItem();
    if (currentItem == null || currentItem.getItemMeta() == null || !(currentItem.getItemMeta().displayName() instanceof TextComponent)) {
      return;
    }
    if (currentItem.getType() != Material.PAPER) {
      return;
    }
    TextComponent textComponent = (TextComponent) currentItem.getItemMeta().displayName();
    String arenaName = textComponent.content();
    Player player = event.getPlayer();
    String oldArena = plugin.getWaitingRoom().voice(player, arenaName);

    for (ItemStack itemStack : player.getInventory().getContents()) {
      if (itemStack != null) {
        if (itemStack.getType() == Material.MOJANG_BANNER_PATTERN) {
          ItemMeta meta = itemStack.getItemMeta();
          if (meta != null  && meta.displayName() != null && meta.displayName() instanceof TextComponent) {
            TextComponent itemTextComponent = (TextComponent) meta.displayName();
            if (Objects.equals(oldArena, itemTextComponent.content())) {
              changeMaterial(itemStack, Material.PAPER);
            }
          }
        }
      }
    }

    changeMaterial(currentItem, Material.MOJANG_BANNER_PATTERN);
  }

  private void changeMaterial(ItemStack itemStack, Material material) {
    itemStack.setType(material);
  }
}
