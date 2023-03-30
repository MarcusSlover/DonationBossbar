package me.awesomepandapig.donationbossbar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static me.awesomepandapig.donationbossbar.Colors.colorize;

public final class DonationBossBarPlugin extends JavaPlugin {

    private DonationBar donationBar;

    public static DonationBossBarPlugin getInstance() {
        return DonationBossBarPlugin.getPlugin(DonationBossBarPlugin.class);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.createBossBar(); // create the boss bar before registering the event listener

        this.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                if (!DonationBossBarPlugin.this.donationBar.getBossBar().getPlayers().contains(event.getPlayer())) {
                    DonationBossBarPlugin.this.donationBar.addPlayer(event.getPlayer());
                }
            }
        }, this);
    }

    @Override
    public void onDisable() {
        if (this.donationBar == null) {
            return; // if donation bar is null, then it hasn't been created yet
        }
        this.donationBar.getBossBar().removeAll();
    }

    public void createBossBar() {
        this.reloadConfig();

        if (this.donationBar == null) { // if null then create a new instance
            this.donationBar = new DonationBar();
        }

        // remove all players from the boss bar and cancel the task
        this.donationBar.getBossBar().removeAll();
        this.donationBar.attemptToCancel();

        // get the config values
        String token = this.getConfig().getString("access-token");
        String id = this.getConfig().getString("campaign-id");
        String mainTitleColor = this.getConfig().getString("main-title-color");
        String mainBarColor = this.getConfig().getString("main-bar-color");
        String goalMsg = this.getConfig().getString("goal-message");
        String goalTitleColor = this.getConfig().getString("goal-title-color");
        String goalBarColor = this.getConfig().getString("goal-bar-color");

        // create the boss bar
        this.donationBar = new DonationBar();
        this.donationBar.createBar(token, id, mainTitleColor, mainBarColor, goalMsg, goalTitleColor, goalBarColor);

        // add all online players to the boss bar
        Bukkit.getOnlinePlayers().forEach(this.donationBar::addPlayer);
    }

    /**
     * The donation boss bar command.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("donationbb")) {
            if (!sender.hasPermission("donationbb")) {
                sender.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command" +
                        ". Please contact the server administrators if you believe that this is in error.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage(colorize("&2[&aDonation Bossbar&2]\n&aVersion:&7 1.0\n&aDeveloper:&7 awesomepandapig\n&aCommands:&7 /donationbb reload"));
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                sender.sendMessage(colorize("&aReloaded Donation Bossbar&r"));
                this.createBossBar();
            }
        }
        return false;
    }
}
