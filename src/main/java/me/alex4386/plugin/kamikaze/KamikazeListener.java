package me.alex4386.plugin.kamikaze;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KamikazeListener implements Listener {
    Map<Block, Player> kamikazeTargets = new HashMap<>();
    Map<Player, Vector> kamikazeVelocity = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        boolean isAirplane = player.isGliding();
        if (isAirplane) {
            // Get the player's velocity vector
            Vector velocity = player.getVelocity();

            // Calculate the direction vector (normalize it to a length of 1)
            Vector direction = velocity.clone().normalize();

            // Get the player's location
            Vector playerLocation = player.getLocation().toVector();

            // Define a step size for checking points along the path
            double stepSize = 0.5; // You can adjust this value as needed

            // targetBlock;
            Block targetBlock = null;

            double length = velocity.clone().multiply(2).length();

            // Check multiple points along the gliding path
            for (double i = stepSize; i <= length; i += stepSize) {
                // Calculate the position of the point along the path
                Vector pointAlongPath = playerLocation.clone().add(direction.clone().multiply(i));
                Location target = pointAlongPath.toLocation(player.getWorld());
                Block block = player.getWorld().getBlockAt(target);

                // Check if the block at that location is solid
                if (block.getType().isSolid()) {
                    // There is a solid block in the gliding direction
                    // You can add your code here to handle this situation
                    targetBlock = block;
                    break; // Exit the loop since we found a block
                }
            }

            if (targetBlock != null) {
                player.sendMessage("[KAMIKAZE] Current Velocity: "+velocity.length());
                player.sendMessage("[KAMIKAZE] Current Energy: "+this.getEnergy(player));

                kamikazeTargetBlock(targetBlock, player);
            }
        }
    }

    public double getMassOfPlayer(Player player) {
        double mass = 0;

        for (ItemStack is : player.getInventory()) {
            if (is == null) continue;
            if (is.getType().isAir()) {
                mass += 0;
            } else if (is.getType().isBlock()) {
                mass += is.getAmount();
            } else if (is.getType().isItem()) {
                mass += 0.1 * is.getAmount();
            }
        }

        return mass;
    }

    public int getExplosiveCount(Player player) {
        int count = 0;

        for (ItemStack is : player.getInventory()) {
            if (is == null) continue;
            if (this.isExplosive(is.getType())) {
                count += is.getAmount();
            }
        }

        return count;
    }

    public boolean isExplosive(Material material) {
        switch (material) {
            case TNT:
            case TNT_MINECART:
            case FIRE_CHARGE:
            case FIREWORK_ROCKET:
            case FIREWORK_STAR:
            case GUNPOWDER:
                return true;
        }

        return false;
    }

    public int getTNTCount(Player player) {
        int count = 0;

        for (ItemStack is : player.getInventory()) {
            if (is == null) continue;
            if (is.getType() == Material.TNT) {
                count += is.getAmount();
            }
        }

        return count;
    }

    public int removeExplosives(Player player) {
        int count = 0;

        for (ItemStack is : player.getInventory()) {
            if (is == null) continue;
            if (this.isExplosive(is.getType())) {
                is.setAmount(0);
                is.setType(Material.AIR);
            }
        }

        return count;
    }


    public double getEnergy(Player player) {
        return this.getMassOfPlayer(player) * Math.pow((player.getVelocity().length() * 10), 2) / 2;
    }

    public float getExplodeEnergy(Player player) {
        if (player.getVelocity().length() * 10 < 9) {
            return 0f;
        }

        return 4f + (1f * this.getExplosiveCount(player)) + (4f * this.getTNTCount(player));
    }


    public void kamikazeTargetBlock(Block targetBlock, Player player) {
        double e = this.getExplodeEnergy(player);
        if (e > 0) {
            kamikazeTargets.put(targetBlock, player);
            kamikazeVelocity.put(player, player.getVelocity());
            targetBlock.getWorld().createExplosion(targetBlock.getLocation(), (float) Math.min(e, 50f), true, true);
            if (player.getGameMode() != GameMode.CREATIVE) this.removeExplosives(player);
        }
    }

    @EventHandler
    public void onKamikazeExplode(BlockExplodeEvent e) {
        Block block = e.getBlock();
        Player player = kamikazeTargets.get(block);

        if (player != null) {
            Vector velocity = kamikazeVelocity.get(player);
            if (velocity != null) {
            }
        }

        kamikazeTargets.remove(block);
        kamikazeVelocity.remove(player);
    }
}
