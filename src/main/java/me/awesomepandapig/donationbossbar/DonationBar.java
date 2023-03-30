package me.awesomepandapig.donationbossbar;

import com.google.common.net.HttpHeaders;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

import static me.awesomepandapig.donationbossbar.Colors.colorize;

public final class DonationBar {
    private BossBar bar;
    private BukkitTask bukkitTask;

    DonationBar() {
    }

    public void createBar(String token, String id, String mainTitleColor, String mainBarColor, String goalMsg, String goalTitleColor, String goalBarColor) {
        this.bar = Bukkit.createBossBar(colorize("&cWelcome to Donation Bossbar"), BarColor.PINK, BarStyle.SOLID);
        this.bar.setVisible(true);

        this.bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(DonationBossBarPlugin.getInstance(), () -> {
            try {
                final JsonObject jsonObject = this.requestJson(id, token);
                Bukkit.getScheduler().runTask(DonationBossBarPlugin.getInstance(), () -> {

                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    double totalAmountRaised = data.get("totalAmountRaised").getAsDouble();
                    double fundraiserGoalAmount = data.get("fundraiserGoalAmount").getAsDouble();
                    double progress = totalAmountRaised / fundraiserGoalAmount;
                    if (progress >= 1) {
                        // the goal has been reached
                        this.bar.setProgress(1);
                        this.bar.setColor(BarColor.valueOf(goalBarColor));
                        this.bar.setTitle(colorize(goalTitleColor + goalMsg));
                    } else {
                        this.bar.setProgress(progress);
                        this.bar.setColor(BarColor.valueOf(mainBarColor));
                        this.bar.setTitle(colorize(mainTitleColor + "Raised $" + this.formatNumber(totalAmountRaised) + " of $" + this.formatNumber(fundraiserGoalAmount)));
                    }

                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0L, 20 * 30L);
    }

    private JsonObject requestJson(String id, String token) throws IOException {
        URL url = new URL("https://tiltify.com/api/v3/campaigns/" + id);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("GET");

        JsonElement data;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            data = JsonParser.parseReader(reader);
        }
        if (data == null) throw new RuntimeException("Could not get the response, the data is null.");
        if (data.isJsonObject()) return data.getAsJsonObject();
        throw new RuntimeException("Invalid response!");
    }

    private String formatNumber(double number) {
        return NumberFormat.getInstance(Locale.US).format(number);
    }

    public void addPlayer(Player player) {
        this.bar.addPlayer(player);
    }

    public @Nullable BossBar getBossBar() {
        return this.bar;
    }

    public void attemptToCancel() {
        if (this.bukkitTask == null) return;
        if (this.bukkitTask.isCancelled()) return;
        this.bukkitTask.cancel();
    }
}
