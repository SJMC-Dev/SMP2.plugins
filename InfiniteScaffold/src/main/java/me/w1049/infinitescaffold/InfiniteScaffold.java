package me.w1049.infinitescaffold;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class InfiniteScaffold extends JavaPlugin implements Listener {

    public static NamespacedKey INF_KEY;
    public static NamespacedKey OWNER;
//    public static NamespacedKey COMMANDS;
//    public static NamespacedKey USED;
//    public static NamespacedKey PLAYERNAME;

    public static boolean isInfiniteItem(ItemStack itemStack) {
        if (itemStack.getType() != Material.SCAFFOLDING)
            return false;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;
        return itemMeta.getPersistentDataContainer().getOrDefault(InfiniteScaffold.INF_KEY,
                PersistentDataType.BOOLEAN, false);
    }

    public static boolean canUse(Player player, ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) return false;
        UUID uuid = itemMeta.getPersistentDataContainer().get(InfiniteScaffold.OWNER,
                new UUIDDataType());
        if (uuid == null) return false;
        return player.getUniqueId().equals(uuid);
    }

    @Override
    public void onEnable() {
        INF_KEY = new NamespacedKey(this, "is_infinite");
        OWNER = new NamespacedKey(this, "owner");
//        COMMANDS = new NamespacedKey(this, "commands");
//        USED = new NamespacedKey(this, "is_used");
//        PLAYERNAME = new NamespacedKey(this, "player_name");
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("infinitescaffold")) {
            if (args.length < 1) {
                sender.sendMessage("usage:\n/infinitescaffold give <player>\n/infinitescaffold get");
                return false;
            }
            if (args[0].equals("get")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (player.hasPermission("infinitescaffold")) {
                        player.getInventory().addItem(getItem(player));
                        sender.sendMessage("You got it!");
                        return true;
                    } else {
                        sender.sendMessage("Oh no!");
                        return false;
                    }
                } else {
                    sender.sendMessage("This command can only be run by a player.");
                    return false;
                }
            } else if (args[0].equals("give")) {
                if (args.length < 2) {
                    sender.sendMessage("usage:\n/infinitescaffold give <player>");
                    return false;
                }
                Player target = (getServer().getPlayer(args[1]));
                if (target == null) {
                    sender.sendMessage("[InfiniteScaffold] No such player!");
                    return false;
                }
                target.getInventory().addItem(getItem(target));
                sender.sendMessage("[InfiniteScaffold] Given to " + target.getName() + "!");
                return true;
            }// else if (args[0].equals("example")) {
//                if (sender instanceof Player) {
//                    Player player = (Player) sender;
//                    if (player.hasPermission("infinitescaffold")) {
//                        player.getInventory().addItem(getExampleItem(player));
//                        sender.sendMessage("You got it!");
//                        return true;
//                    } else {
//                        sender.sendMessage("Oh no!");
//                        return false;
//                    }
//                } else {
//                    sender.sendMessage("This command can only be run by a player.");
//                    return false;
//                }
//            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        NamespacedKey key = e.getAdvancement().getKey();
        if (key.getNamespace().equals("smp2") && key.getKey().equals("architect/all_architect_achievements")) {
            Player player = e.getPlayer();
            player.getInventory().addItem(getItem(player));
            String name = player.getName();
            getServer().getScheduler().runTask(this, () -> {
                        getServer().dispatchCommand(getServer().getConsoleSender(),
                                "lp user " + name + " permission set user.buildings.view true");
                        getServer().dispatchCommand(getServer().getConsoleSender(),
                                "plt player setTitle " + name + " 2 0");
                    }
            );
        }
    }

    private ItemStack getItem(Player player) {
        ItemStack itemStack = new ItemStack(Material.SCAFFOLDING, 1);

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null) {
            itemMeta = getServer().getItemFactory().getItemMeta(itemStack.getType());
        }
        assert itemMeta != null;
        PersistentDataContainer c = itemMeta.getPersistentDataContainer();
        c.set(InfiniteScaffold.INF_KEY,
                PersistentDataType.BOOLEAN, true);
        c.set(InfiniteScaffold.OWNER, new UUIDDataType(), player.getUniqueId());

        TextColor color = TextColor.color(0, 134, 209);
        itemMeta.displayName(Component.text("无限脚手架").color(color).decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(("属于【" + player.getName() + "】")).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("稀有级").color(color).decoration(TextDecoration.ITALIC, false).decorate(TextDecoration.BOLD));
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
        itemStack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return itemStack;
    }

//    private ItemStack getExampleItem(Player player) {
//        ItemStack itemStack = new ItemStack(Material.PAPER, 1);
//
//        ItemMeta itemMeta = itemStack.getItemMeta();
//        if (itemMeta == null) {
//            itemMeta = getServer().getItemFactory().getItemMeta(itemStack.getType());
//        }
//        assert itemMeta != null;
//        PersistentDataContainer c = itemMeta.getPersistentDataContainer();
//        c.set(InfiniteScaffold.COMMANDS, PersistentDataType.STRING, "infinitescaffold give <PlayerName>\nsay hello");
//        c.set(InfiniteScaffold.PLAYERNAME, PersistentDataType.STRING, player.getName());
//
//        itemStack.setItemMeta(itemMeta);
//        return itemStack;
//    }

}
