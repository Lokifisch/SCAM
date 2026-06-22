package dev.lokifisch.scam;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ScamPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    private static final double BASE_HEIGHT_M = 1.8;

    private static final double MIN_HEIGHT_M = 1.30;
    private static final double MAX_HEIGHT_M = 2.0;

    // Minecraft's own attribute limits for generic.scale
    private static final double MC_MIN_HEIGHT_M = 0.0625 * BASE_HEIGHT_M; // ~0.1125 m
    private static final double MC_MAX_HEIGHT_M = 16.0  * BASE_HEIGHT_M;  // 28.8 m

    @Override
    public void onEnable() {
        getCommand("setheight").setExecutor(this);
        getCommand("setheight").setTabCompleter(this);
        getLogger().info("SCAM (Scale Character Attribute Mod) enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SCAM disabled.");
    }

    // -------------------------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sendUsage(player);
            return true;
        }

        ParseResult result;
        try {
            result = parseHeight(args[0].trim());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c[SCAM] §r" + e.getMessage());
            return true;
        }

        double heightM = result.meters();
        boolean bypass = player.hasPermission("scam.setheight.bypass");
        double minH = bypass ? MC_MIN_HEIGHT_M : MIN_HEIGHT_M;
        double maxH = bypass ? MC_MAX_HEIGHT_M : MAX_HEIGHT_M;

        if (heightM < minH) {
            player.sendMessage(String.format(
                "§c[SCAM] §rToo small! Minimum is §e%.4fm §r(§e%.2fcm §r/ §e%.3fft§r).",
                minH, minH * 100.0, minH / 0.3048));
            return true;
        }

        if (heightM > maxH) {
            player.sendMessage(String.format(
                "§c[SCAM] §rToo tall! Maximum is §e%.4fm §r(§e%.2fcm §r/ §e%.4fft§r).",
                maxH, maxH * 100.0, maxH / 0.3048));
            return true;
        }

        double scale = heightM / BASE_HEIGHT_M;

        AttributeInstance attr = player.getAttribute(Attribute.GENERIC_SCALE);
        if (attr == null) {
            player.sendMessage("§c[SCAM] §rScale attribute unavailable — requires Paper 1.20.5+.");
            return true;
        }

        try {
            attr.setBaseValue(scale);
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c[SCAM] §rMinecraft rejected that scale value: " + e.getMessage());
            return true;
        }

        player.sendMessage(String.format(
            "§a[SCAM] §rHeight set to §e%.4fm §r(§e%.2fcm §r/ §e%.4fft§r) — scale: §e%.5f",
            heightM, heightM * 100.0, heightM / 0.3048, scale));

        return true;
    }

    // -------------------------------------------------------------------------

    private void sendUsage(Player player) {
        boolean bypass = player.hasPermission("scam.setheight.bypass");
        double minH = bypass ? MC_MIN_HEIGHT_M : MIN_HEIGHT_M;
        double maxH = bypass ? MC_MAX_HEIGHT_M : MAX_HEIGHT_M;
        player.sendMessage("§6[SCAM] §eScale Character Attribute Mod");
        player.sendMessage("§7Usage: §f/setheight <height>");
        player.sendMessage("§7Units: §fm §7/ §fcm §7/ §fft §7(auto-detected if omitted)");
        player.sendMessage("§7Examples: §f1.8 §7→ 1.8 m  §f180 §7→ 180 cm  §f6ft §7→ feet");
        player.sendMessage(String.format("§7Range: §f%.4fm §7(%.2fcm) §7to §f%.4fm §7(%.2fcm)",
            minH, minH * 100.0, maxH, maxH * 100.0));
    }

    /**
     * Parses a height string with optional unit suffix (m, cm, ft/feet).
     *
     * Auto-detection when no suffix is given:
     *   n <= 2.0  → metres   (covers the full valid metre range up to the 2 m cap)
     *   n >  2.0  → centimetres (e.g. 180 → 1.80 m)
     *
     * Feet always require an explicit suffix because the numeric range overlaps.
     */
    private ParseResult parseHeight(String raw) {
        String lower = raw.toLowerCase();

        if (lower.endsWith("feet") || lower.endsWith("ft")) {
            double v = parseDouble(lower.replaceAll("[a-z]+$", "").trim(), raw);
            return new ParseResult(v * 0.3048, "ft");
        }
        if (lower.endsWith("cm")) {
            double v = parseDouble(lower.replaceAll("[a-z]+$", "").trim(), raw);
            return new ParseResult(v / 100.0, "cm");
        }
        if (lower.endsWith("m")) {
            double v = parseDouble(lower.replaceAll("[a-z]+$", "").trim(), raw);
            return new ParseResult(v, "m");
        }

        // No suffix — auto-detect by magnitude
        double v = parseDouble(lower, raw);
        if (v <= MAX_HEIGHT_M) {
            return new ParseResult(v, "m");        // e.g. 1.8  → 1.80 m
        } else {
            return new ParseResult(v / 100.0, "cm"); // e.g. 180 → 1.80 m
        }
    }

    private double parseDouble(String s, String original) {
        try {
            double v = Double.parseDouble(s);
            if (Double.isNaN(v) || Double.isInfinite(v)) throw new IllegalArgumentException();
            if (v <= 0) throw new IllegalArgumentException("Height must be a positive number (got: " + original + ").");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Not a valid number: §f" + original + "§r. Try e.g. §f1.8m§r, §f180cm§r, §f6ft§r.");
        }
    }

    // -------------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("1.8", "180", "6ft", "1.5", "150", "2");
        }
        return List.of();
    }

    // -------------------------------------------------------------------------

    private record ParseResult(double meters, String unit) {}
}
