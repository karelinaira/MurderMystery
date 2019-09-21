/*
 * MurderMystery - Find the murderer, kill him and survive!
 * Copyright (C) 2019  Plajer's Lair - maintained by Plajer and contributors
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

package pl.plajer.murdermystery.handlers.setup.components;

import com.github.stefvanschie.inventoryframework.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import pl.plajer.murdermystery.Main;
import pl.plajer.murdermystery.arena.Arena;
import pl.plajer.murdermystery.handlers.ChatManager;
import pl.plajer.murdermystery.handlers.setup.SetupInventory;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.item.ItemBuilder;

/**
 * @author Plajer
 * <p>
 * Created at 25.05.2019
 */
public class PlayerAmountComponents implements SetupComponent {

  private SetupInventory setupInventory;

  @Override
  public void prepare(SetupInventory setupInventory) {
    this.setupInventory = setupInventory;
  }

  @Override
  public void injectComponents(StaticPane pane) {
    FileConfiguration config = setupInventory.getConfig();
    Arena arena = setupInventory.getArena();
    Main plugin = setupInventory.getPlugin();
    pane.addItem(new GuiItem(new ItemBuilder(Material.COAL).amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("minimumplayers"))
      .name(ChatManager.colorRawMessage("&e&lSet Minimum Players Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "(how many players are needed")
      .lore(ChatColor.DARK_GRAY + "for game to start lobby countdown)")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".minimumplayers"))
      .build(), e -> {
      if (e.getClick().isRightClick()) {
        e.getInventory().getItem(e.getSlot()).setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if (e.getClick().isLeftClick()) {
        e.getInventory().getItem(e.getSlot()).setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if (e.getInventory().getItem(e.getSlot()).getAmount() <= 1) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 2! Game is designed for 2 or more players!"));
        e.getInventory().getItem(e.getSlot()).setAmount(2);
      }
      config.set("instances." + arena.getId() + ".minimumplayers", e.getCurrentItem().getAmount());
      arena.setMinimumPlayers(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 3, 0);

    pane.addItem(new GuiItem(new ItemBuilder(Material.REDSTONE)
      .amount(setupInventory.getSetupUtilities().getMinimumValueHigherThanZero("maximumplayers"))
      .name(ChatManager.colorRawMessage("&e&lSet Maximum Players Amount"))
      .lore(ChatColor.GRAY + "LEFT click to decrease")
      .lore(ChatColor.GRAY + "RIGHT click to increase")
      .lore(ChatColor.DARK_GRAY + "(how many players arena can hold)")
      .lore("", setupInventory.getSetupUtilities().isOptionDone("instances." + arena.getId() + ".maximumplayers"))
      .build(), e -> {
      if (e.getClick().isRightClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() + 1);
      }
      if (e.getClick().isLeftClick()) {
        e.getCurrentItem().setAmount(e.getCurrentItem().getAmount() - 1);
      }
      if (e.getInventory().getItem(e.getSlot()).getAmount() <= 1) {
        e.getWhoClicked().sendMessage(ChatManager.colorRawMessage("&c&l✖ &cWarning | Please do not set amount lower than 2! Game is designed for 2 or more players!"));
        e.getInventory().getItem(e.getSlot()).setAmount(2);
      }
      config.set("instances." + arena.getId() + ".maximumplayers", e.getCurrentItem().getAmount());
      arena.setMaximumPlayers(e.getCurrentItem().getAmount());
      ConfigUtils.saveConfig(plugin, config, "arenas");
      new SetupInventory(arena, setupInventory.getPlayer()).openInventory();
    }), 4, 0);
  }

}
