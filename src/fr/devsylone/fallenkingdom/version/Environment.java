package fr.devsylone.fallenkingdom.version;

import fr.devsylone.fallenkingdom.utils.ChatUtils;
import fr.devsylone.fkpi.teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Environment {

    private static final boolean HAS_ASYNC_TELEPORT;
    private static final boolean HAS_MULTI_BLOCK_CHANGE;
    private static final boolean HAS_UUID_BY_PLAYER_NAME;
    private static final boolean HAS_ASYNC_CHUNK_LOAD;
    private static final boolean HAS_ADVENTURE_API;
    private static final boolean HAS_MIN_HEIGHT;
    private static final boolean HAS_SPIGOT_BOOK_PAGES;

    static {
        boolean hasAsyncTeleport = false;
        try {
            Entity.class.getMethod("teleportAsync", Location.class);
            hasAsyncTeleport = true;
        } catch (NoSuchMethodException ignored) { }
        HAS_ASYNC_TELEPORT = hasAsyncTeleport;

        boolean hasMultiBlockChange = false;
        try {
            Player.class.getDeclaredMethod("sendMultiBlockChange", java.util.Map.class);
            hasMultiBlockChange = true;
        } catch (NoSuchMethodException ignored) { }
        HAS_MULTI_BLOCK_CHANGE = hasMultiBlockChange;

        boolean hasUuidByPlayerName = false;
        try {
            Bukkit.class.getMethod("getPlayerUniqueId", String.class);
            hasUuidByPlayerName = true;
        } catch (NoSuchMethodException ignored) { }
        HAS_UUID_BY_PLAYER_NAME = hasUuidByPlayerName;

        boolean hasAsyncChunkLoad = false;
        try {
            World.class.getMethod("getChunkAtAsync", int.class, int.class);
            hasAsyncChunkLoad = true;
        } catch (NoSuchMethodException ignored) { }
        HAS_ASYNC_CHUNK_LOAD = hasAsyncChunkLoad;

        boolean hasAdventureApi = false;
        try {
            Class.forName("net.kyori.adventure.text.Component");
            hasAdventureApi = true;
        } catch (ClassNotFoundException ignored) { }
        HAS_ADVENTURE_API = hasAdventureApi;

        boolean hasMinHeight = false;
        try {
            World.class.getMethod("getMinHeight");
            hasMinHeight = true;
        } catch (NoSuchMethodException ignored) { }
        HAS_MIN_HEIGHT = hasMinHeight;

        boolean spigotPages = false;
        try {
            Class.forName(BookMeta.class.getName() + "$Spigot").getMethod("setPages", List.class);
            spigotPages = true;
        } catch (ReflectiveOperationException ignored) { }
        HAS_SPIGOT_BOOK_PAGES = spigotPages;
    }

    public static CompletableFuture<Boolean> teleportAsync(Entity entity, Location location) {
        if (HAS_ASYNC_TELEPORT) {
            return entity.teleportAsync(location);
        }
        return CompletableFuture.completedFuture(entity.teleport(location)); // Sinon synchrone
    }

    public static UUID getPlayerUniqueId(String playerName) {
        if (HAS_UUID_BY_PLAYER_NAME) {
            return Bukkit.getPlayerUniqueId(playerName);
        }
        Player player = Bukkit.getPlayerExact(playerName);
        return player == null ? null : player.getUniqueId();
    }

    public static CompletableFuture<Chunk> getChunkAtAsync(World world, int x, int z) {
        if (HAS_ASYNC_CHUNK_LOAD) {
            return world.getChunkAtAsync(x, z);
        }
        return CompletableFuture.completedFuture(world.getChunkAt(x, z));
    }

    public static void setDeathMessage(PlayerDeathEvent event, Team playerTeam, Team killerTeam) {
        if (HAS_ADVENTURE_API) {
            AdventureFormat.setDeathMessage(event, playerTeam, killerTeam);
            return;
        }

        String deathMessage = event.getDeathMessage();
        if (deathMessage == null) return;
        deathMessage = ChatUtils.PREFIX + deathMessage;
        if (playerTeam != null) {
            deathMessage = deathMessage.replace(event.getEntity().getName(), event.getEntity().getDisplayName() + ChatColor.GRAY);
        }
        if (event.getEntity().getKiller() != null && killerTeam != null) {
            deathMessage = deathMessage.replace(event.getEntity().getKiller().getName(), event.getEntity().getKiller().getDisplayName() + ChatColor.GRAY);
        }
        event.setDeathMessage(deathMessage);
    }

    public static int getMinHeight(World world) {
        return HAS_MIN_HEIGHT ? world.getMinHeight() : 0;
    }

    public static boolean hasMultiBlockChange() {
        return HAS_MULTI_BLOCK_CHANGE;
    }

    public static boolean hasSpigotBookPages() {
        return HAS_SPIGOT_BOOK_PAGES;
    }
}
