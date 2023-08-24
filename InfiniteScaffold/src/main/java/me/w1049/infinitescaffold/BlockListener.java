package me.w1049.infinitescaffold;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.jeff_media.customblockdata.CustomBlockData;
import io.papermc.paper.event.block.BlockBreakBlockEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;


public class BlockListener implements Listener {

    private final Plugin plugin;

    public BlockListener(Plugin plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        EquipmentSlot hand = e.getHand();
        ItemStack itemStack = e.getItemInHand();
        if (InfiniteScaffold.isInfiniteItem(itemStack)) {
            if (InfiniteScaffold.canUse(player, itemStack)) {
                player.getInventory().setItem(hand, itemStack);
                setInfinite(e.getBlockPlaced());
            } else {
                player.sendMessage("You can't use this!");
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        if (isInfinite(block)) {
            e.setDropItems(false);
            removeInfinite(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDestroy(BlockDestroyEvent e) {
        Block block = e.getBlock();
        if (isInfinite(block)) {
            e.setWillDrop(false);
            removeInfinite(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        Block block = e.getBlock();
        if (isInfinite(block)) {
            removeInfinite(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBreakBlock(BlockBreakBlockEvent e) {
        Block block = e.getBlock();
        if (isInfinite(block)) {
            e.getDrops().clear();
            removeInfinite(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlocksBreakByExplosion(EntityExplodeEvent e) {
        for (Block block : e.blockList()) {
            if (isInfinite(block)) {
                removeInfinite(block);
                block.setType(Material.AIR);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFallBlock(EntityChangeBlockEvent e) {
        if (e.getEntity().getType() == EntityType.FALLING_BLOCK) {
            FallingBlock fallingBlock = (FallingBlock) e.getEntity();
            if (e.getTo() != fallingBlock.getBlockData().getMaterial()) {
                if (isInfinite(e.getBlock())) {
                    fallingBlock.setDropItem(false);
                    fallingBlock.getPersistentDataContainer().set(InfiniteScaffold.INF_KEY,
                            PersistentDataType.BOOLEAN, true);
                    removeInfinite(e.getBlock());
                }
            } else {
                if (fallingBlock.getPersistentDataContainer().getOrDefault(InfiniteScaffold.INF_KEY,
                        PersistentDataType.BOOLEAN, false)) {
                    setInfinite(e.getBlock());
                }
            }
        }
    }

//    @EventHandler(priority = EventPriority.NORMAL)
//    public void onRightClick(PlayerInteractEvent e) {
//        ItemStack itemStack = e.getItem();
//        if (itemStack == null) return;
//        ItemMeta itemMeta = itemStack.getItemMeta();
//        if (itemMeta == null) return;
//        plugin.getLogger().info(itemMeta.toString());
//        PersistentDataContainer c = itemMeta.getPersistentDataContainer();
//        if (c.has(InfiniteScaffold.COMMANDS, PersistentDataType.STRING)) {
//            plugin.getLogger().info("OK, COMMANDS");
//            if (c.getOrDefault(InfiniteScaffold.USED, PersistentDataType.BOOLEAN, false)) return;
//            c.set(InfiniteScaffold.USED, PersistentDataType.BOOLEAN, true);
//            itemStack.setItemMeta(itemMeta);
//            String name = c.getOrDefault(InfiniteScaffold.PLAYERNAME, PersistentDataType.STRING, "");
//            String commands = c.getOrDefault(InfiniteScaffold.COMMANDS, PersistentDataType.STRING, "").replaceAll("<PlayerName>", name);
//            plugin.getServer().getScheduler().runTask(plugin, () -> {
//                        CommandSender sender = plugin.getServer().getConsoleSender();
//                        for (String s : commands.split("\n")) {
//                            plugin.getServer().dispatchCommand(sender, s);
//                        }
//                    }
//            );
//        }
//
//    }

    public void setInfinite(Block block) {
        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        customBlockData.set(InfiniteScaffold.INF_KEY, PersistentDataType.BOOLEAN, true);
    }

    public void removeInfinite(Block block) {
        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        customBlockData.clear();
    }

    public boolean isInfinite(Block block) {
        if (!CustomBlockData.hasCustomBlockData(block, plugin)) return false;
        CustomBlockData customBlockData = new CustomBlockData(block, plugin);
        return customBlockData.getOrDefault(InfiniteScaffold.INF_KEY,
                PersistentDataType.BOOLEAN, false);
    }

}
