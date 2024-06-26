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

package plugily.projects.murdermystery.handlers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import plugily.projects.commonsbox.minecraft.compat.ServerVersion;
import plugily.projects.commonsbox.minecraft.misc.MiscUtils;
import plugily.projects.commonsbox.string.StringFormatUtils;
import plugily.projects.murdermystery.ConfigPreferences;
import plugily.projects.murdermystery.Main;
import plugily.projects.murdermystery.arena.Arena;
import plugily.projects.murdermystery.handlers.language.LanguageManager;

/**
 * @author Plajer
 * <p>
 * Created at 03.08.2018
 */
public class ChatManager {

  private final String pluginPrefix;
  private final Main plugin;

  public ChatManager(Main plugin) {
    this.plugin = plugin;
    pluginPrefix = colorMessage("In-Game.Plugin-Prefix");
  }

  /**
   * @return game prefix
   */
  public String getPrefix() {
    return pluginPrefix;
  }

  public String colorMessage(String message) {
    return colorRawMessage(LanguageManager.getLanguageMessage(message));
  }

  public String colorRawMessage(String message) {
    if(message == null) {
      return "";
    }

    if(ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_16_R1) && message.indexOf('#') >= 0) {
      message = MiscUtils.matchColorRegex(message);
    }

    return ChatColor.translateAlternateColorCodes('&', message);
  }

  public String colorMessage(String message, Player player) {
    String returnString = LanguageManager.getLanguageMessage(message);
    if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      returnString = PlaceholderAPI.setPlaceholders(player, returnString);
    }
    return colorRawMessage(returnString);
  }

  public void broadcast(Arena arena, String message) {
    if (message != null && !message.isEmpty()) {
      for(Player p : arena.getPlayers()) {
        p.sendMessage(pluginPrefix + message);
      }
    }
  }

  public String formatMessage(Arena arena, String message, int integer) {
    String returnString = message;
    returnString = StringUtils.replace(returnString, "%NUMBER%", Integer.toString(integer));
    returnString = colorRawMessage(formatPlaceholders(returnString, arena));
    return returnString;
  }

  public String formatMessage(Arena arena, String message, Player player) {
    String returnString = message;
    returnString = StringUtils.replace(returnString, "%PLAYER%", player.getName());
    returnString = colorRawMessage(formatPlaceholders(returnString, arena));
    if(plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
      returnString = PlaceholderAPI.setPlaceholders(player, returnString);
    }
    return returnString;
  }

  private String formatPlaceholders(String message, Arena arena) {
    String returnString = message;

    int timer = arena.getTimer();

    returnString = StringUtils.replace(returnString, "%ARENANAME%", arena.getMapName());
    returnString = StringUtils.replace(returnString, "%TIME%", Integer.toString(timer));
    returnString = StringUtils.replace(returnString, "%FORMATTEDTIME%", StringFormatUtils.formatIntoMMSS(timer));
    returnString = StringUtils.replace(returnString, "%PLAYERSIZE%", Integer.toString(arena.getPlayers().size()));
    returnString = StringUtils.replace(returnString, "%MAXPLAYERS%", Integer.toString(arena.getMaximumPlayers()));
    returnString = StringUtils.replace(returnString, "%MINPLAYERS%", Integer.toString(arena.getMinimumPlayers()));
    return returnString;
  }

  public void broadcastAction(Arena a, Player p, ActionType action) {
    String message;
    switch(action) {
      case JOIN:
        message = formatMessage(a, colorMessage("In-Game.Messages.Join"), p);
        break;
      case LEAVE:
        message = formatMessage(a, colorMessage("In-Game.Messages.Leave"), p);
        break;
      case DEATH:
        if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.DISABLE_DEATH_MESSAGE)) {
          return;
        }

        message = formatMessage(a, colorMessage("In-Game.Messages.Death"), p);
        break;
      default:
        return; //likely won't ever happen
    }
    for(Player player : a.getPlayers()) {
      player.sendMessage(pluginPrefix + message);
    }
  }

  public enum ActionType {
    JOIN, LEAVE, DEATH
  }

}
