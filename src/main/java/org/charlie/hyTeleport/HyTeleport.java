// Made by charlie (corkscrew) discord: ch4rles.exe for support

package org.charlie.hyTeleport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class HyTeleport extends JavaPlugin {

    private FileConfiguration messagesConfig;
    private FileConfiguration warpsConfig;

    @Override
    public void onEnable() {
        createMessagesConfig();
        createWarpsConfig();
        this.getCommand("hyteleport").setExecutor(new HyTeleportCommand(this));
        this.getCommand("hytp").setExecutor(new HyTeleportCommand(this));
    }

    private void createMessagesConfig() {
        File messagesFile = new File(getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            messagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    private void createWarpsConfig() {
        File warpsFile = new File(getDataFolder(), "config.yml");
        if (!warpsFile.exists()) {
            warpsFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);
    }

    public void reloadPlugin() {
        createMessagesConfig();
        createWarpsConfig();
        reloadConfig();
    }

    private String getFormattedMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(path, ""));
    }

    public class HyTeleportCommand implements CommandExecutor {

        private final HyTeleport plugin;

        public HyTeleportCommand(HyTeleport plugin) {
            this.plugin = plugin;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("hyteleport.reload")) {
                    plugin.reloadPlugin();
                    sender.sendMessage(getFormattedMessage("reload-success"));
                } else {
                    sender.sendMessage(getFormattedMessage("no-permission"));
                }
                return true;
            }

            if (args.length != 2) {
                sender.sendMessage(getFormattedMessage("usage"));
                return false;
            }

            Player player = Bukkit.getPlayer(args[0]);
            if (player == null) {
                sender.sendMessage(getFormattedMessage("player-not-found"));
                return false;
            }

            String warp = args[1];

            if (!warpsConfig.contains("warps." + warp)) {
                sender.sendMessage(getFormattedMessage("warp-not-found").replace("%warp%", warp));
                return false;
            }

            String permission = warpsConfig.getString("warps." + warp + ".perms");
            if (!sender.hasPermission(permission)) {
                sender.sendMessage(getFormattedMessage("no-permission"));
                return false;
            }

            double x = warpsConfig.getDouble("warps." + warp + ".tp-location-x");
            double y = warpsConfig.getDouble("warps." + warp + ".tp-location-y");
            double z = warpsConfig.getDouble("warps." + warp + ".tp-location-z");

            Location targetLocation = new Location(player.getWorld(), x, y, z);
            applyTeleportEffect(player, targetLocation);
            player.sendMessage(getFormattedMessage("warp-success").replace("%warp%", warp));
            return true;
        }

        private void applyTeleportEffect(Player player, Location targetLocation) {
            Location startLocation = player.getLocation();
            double initialHeight = startLocation.getY();
            double targetHeight = targetLocation.getY();
            double maxHeight = initialHeight + 100;
            final double[] movementSpeed = {0.5};
            double speedMultiplier = 1.05;
            final int[] waitTime = {40};
            final int[] finalWaitTime = {waitTime[0]};

            GameMode originalGameMode = player.getGameMode();
            player.setGameMode(GameMode.SPECTATOR);
            player.setAllowFlight(true);
            player.setFlying(true);
            player.setVelocity(player.getVelocity().zero());
            player.sendTitle("", "", 0, 999999, 0);
            player.setInvisible(true);

            new BukkitRunnable() {
                double currentY = initialHeight;
                double progress = 0.0;
                boolean isAscending = true;
                boolean isWaiting = false;
                boolean isPanning = false;
                boolean isFinalWaiting = false;
                boolean isDescending = false;

                @Override
                public void run() {
                    player.setRotation(90, 90);

                    if (isAscending) {
                        movementSpeed[0] *= speedMultiplier;
                        currentY += movementSpeed[0];
                        if (currentY >= maxHeight) {
                            currentY = maxHeight;
                            isAscending = false;
                            isWaiting = true;
                        }
                        player.teleport(new Location(startLocation.getWorld(), startLocation.getX(), currentY, startLocation.getZ(), 90, 90));
                    } else if (isWaiting) {
                        waitTime[0]--;
                        if (waitTime[0] <= 0) {
                            isWaiting = false;
                            double distance = Math.sqrt(Math.pow(targetLocation.getX() - startLocation.getX(), 2) +
                                    Math.pow(targetLocation.getZ() - startLocation.getZ(), 2));
                            if (!startLocation.getWorld().equals(targetLocation.getWorld()) || distance > 500) {
                                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 1, false, false, false));
                                player.teleport(new Location(targetLocation.getWorld(), targetLocation.getX(), maxHeight, targetLocation.getZ()));
                            }
                            isPanning = true;
                            movementSpeed[0] = 0.01;
                        }
                    } else if (isPanning) {
                        movementSpeed[0] *= speedMultiplier;
                        progress += movementSpeed[0];
                        if (progress >= 1.0) {
                            progress = 1.0;
                            isPanning = false;
                            isFinalWaiting = true;
                        }
                        double currentX = startLocation.getX() + (targetLocation.getX() - startLocation.getX()) * progress;
                        double currentZ = startLocation.getZ() + (targetLocation.getZ() - startLocation.getZ()) * progress;
                        player.teleport(new Location(targetLocation.getWorld(), currentX, maxHeight, currentZ, 90, 90));
                    } else if (isFinalWaiting) {
                        finalWaitTime[0]--;
                        if (finalWaitTime[0] <= 0) {
                            isFinalWaiting = false;
                            isDescending = true;
                            movementSpeed[0] = 0.3;
                        }
                    } else if (isDescending) {
                        movementSpeed[0] *= speedMultiplier * 1.1;
                        currentY -= movementSpeed[0];
                        if (currentY <= targetHeight) {
                            currentY = targetHeight;
                            player.teleport(targetLocation);
                            player.setFlySpeed(0.1f);
                            player.setGameMode(originalGameMode);
                            player.setFlying(false);
                            player.setAllowFlight(originalGameMode == GameMode.CREATIVE);
                            player.setInvisible(false);
                            player.sendTitle("", "", 0, 20, 0);
                            cancel();
                            return;
                        }
                        player.teleport(new Location(targetLocation.getWorld(), targetLocation.getX(), currentY, targetLocation.getZ(), 90, 90));
                    }
                }
            }.runTaskTimer(plugin, 0, 1);
        }
    }
}
