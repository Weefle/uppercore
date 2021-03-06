package xyz.upperlevel.uppercore.gui;

import lombok.Data;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.upperlevel.uppercore.Uppercore;
import xyz.upperlevel.uppercore.config.exceptions.InvalidConfigurationException;
import xyz.upperlevel.uppercore.gui.action.Action;
import xyz.upperlevel.uppercore.gui.action.ActionType;
import xyz.upperlevel.uppercore.economy.EconomyManager;
import xyz.upperlevel.uppercore.itemstack.CustomItem;
import xyz.upperlevel.uppercore.config.Config;
import xyz.upperlevel.uppercore.itemstack.ItemResolver;
import xyz.upperlevel.uppercore.gui.link.Link;
import xyz.upperlevel.uppercore.message.Message;
import xyz.upperlevel.uppercore.placeholder.PlaceholderValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Data
public class ConfigIcon {

    private ItemResolver display;
    private Link link;
    private int updateInterval; // 0 or < 0 are considered null

    public ConfigIcon() {
    }

    public ConfigIcon(ItemStack display) {
        this.display = new CustomItem(display);
    }

    public ConfigIcon(ItemStack display, Link link) {
        this.display = new CustomItem(display);
        this.link = link;
    }

    public ConfigIcon(ItemResolver display) {
        this.display = display;
    }

    public ConfigIcon(ItemResolver display, Link link) {
        this.display = display;
        this.link = link;
    }

    public void setDisplay(ItemStack display) {
        this.display = new CustomItem(display);
    }

    public void setDisplay(ItemResolver display) {
        this.display = display;
    }

    public boolean needUpdate() {
        return updateInterval > 0;
    }

    public void onClick(InventoryClickEvent e) {
        if (link != null)
            link.run((Player) e.getWhoClicked());
    }

    public static ConfigIcon deserialize(Plugin plugin, Config config) {
        ConfigIcon result = new ConfigIcon();
        try {
            if (config.has("update-interval"))
                result.updateInterval = config.getInt("update-interval", -1);
            if (config.has("item"))
                result.display = CustomItem.deserialize(config.getConfigRequired("item"));
            if (config.has("click"))
                result.link = IconClick.deserialize(plugin, config.getConfig("click"));
            return result;
        } catch (InvalidConfigurationException e) {
            e.addLocalizer("in gui display");
            throw e;
        }
    }

    public static ConfigIcon of(ItemStack item) {
        return new ConfigIcon(item);
    }

    public static ConfigIcon of(ItemResolver item) {
        return new ConfigIcon(item);
    }

    public static ConfigIcon of(ItemResolver item, Link link) {
        return new ConfigIcon(item, link);
    }

    public static ConfigIcon of(Supplier<ItemStack> item, Link link) {
        return new ConfigIcon(p -> item.get(), link);
    }

    public static ConfigIcon of(ItemStack item, Link link) {
        return new ConfigIcon(item, link);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final ConfigIcon item;

        public Builder() {
            item = new ConfigIcon();
        }

        public Builder(ConfigIcon item) {
            this.item = item;
        }

        public Builder item(ItemStack item) {
            this.item.setDisplay(item);
            return this;
        }

        public Builder item(ItemResolver item) {
            this.item.setDisplay(item);
            return this;
        }

        public Builder link(Link link) {
            item.link = link;
            return this;
        }

        public ConfigIcon build() {
            return item;
        }
    }

    public static class IconClick implements Link {

        private String permission;
        private Message noPermissionError;
        private Sound noPermissionSound;

        private PlaceholderValue<Double> cost;
        private Message noMoneyError;
        private Sound noMoneySound;

        private List<Action> actions = new ArrayList<>();

        private IconClick() {
        }

        public boolean checkPermission(Player player) {
            if (permission != null && !player.hasPermission(permission)) {
                noPermissionError.send(player);
                if (noPermissionSound != null)
                    player.playSound(player.getLocation(), noPermissionSound, 1.0f, 1.0f);
                return false;
            } else
                return true;
        }

        public boolean pay(Player player) {
            double c = cost.resolve(player);
            if (c > 0) {
                Economy eco = EconomyManager.getEconomy();
                if (eco == null) {
                    Uppercore.logger().severe("Cannot use economy: vault not found!");
                    return true;
                }
                EconomyResponse res = eco.withdrawPlayer(player, c);
                if (!res.transactionSuccess()) {
                    noMoneyError.send(player);
                    if (noMoneySound != null)
                        player.playSound(player.getLocation(), noMoneySound, 1.0f, 1.0f);
                    Uppercore.logger().log(Level.INFO, res.errorMessage);
                    return false;
                } else return true;
            }
            return true;
        }

        @SuppressWarnings("unchecked")
        public static IconClick deserialize(Plugin plugin, Config config) {
            IconClick res = new IconClick();
            res.permission = (String) config.get("permission");
            res.noPermissionError = config.getMessage("no-permission-message", "You don't have permission!");
            res.noPermissionSound = config.getSound("no-permission-sound");

            res.cost = PlaceholderValue.doubleValue(config.getString("cost", "0"));
            res.noMoneyError = config.getMessage("no-money-error", "You don't have enough money");
            res.noMoneySound = config.getSound("no-money-sound");

            List<Object> actions = (List<Object>) config.get("actions");
            if (actions == null)
                res.actions = Collections.emptyList();
            else
                res.actions = actions.stream()
                        .map(obj -> ActionType.deserialize(plugin, obj))
                        .collect(Collectors.toList());
            return res;
        }

        @Override
        public void run(Player player) {
            if (checkPermission(player) && pay(player)) {
                for (Action action : actions)
                    action.run(player);
            }
        }

        @Override
        public String toString() {
            StringJoiner joiner = new StringJoiner(", ");
            joiner.add("permission: " + permission);
            joiner.add("noPermissionError: " + noPermissionError);
            joiner.add("noPermissionSound: " + noPermissionSound);
            joiner.add("cost: " + cost);
            joiner.add("noMoneyError: " + noMoneyError);
            joiner.add("noMoneySound: " + noMoneySound);
            return '{' + joiner.toString() + '}';
        }
    }
}