package me.neznamy.tab.platforms.fabric;

import com.mojang.authlib.properties.Property;
import me.neznamy.tab.platforms.fabric.hook.PermissionsAPIHook;
import me.neznamy.tab.shared.backend.BackendTabPlayer;
import me.neznamy.chat.component.TabComponent;
import me.neznamy.tab.shared.platform.TabList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * TabPlayer implementation for Fabric.
 */
public class FabricTabPlayer extends BackendTabPlayer {

    /**
     * Constructs new instance with given parameters.
     *
     * @param   platform
     *          Server platform
     * @param   player
     *          Platform's player object
     */
    public FabricTabPlayer(@NotNull FabricPlatform platform, @NotNull ServerPlayer player) {
        super(platform, player, player.getUUID(), player.getGameProfile().getName(),
                FabricTAB.getLevelName(player.level()), platform.getServerVersion().getNetworkId());
    }

    @Override
    public boolean hasPermission(@NotNull String permission) {
        return PermissionsAPIHook.hasPermission(getPlayer().createCommandSourceStack(), permission);
    }

    @Override
    public int getPing() {
        return getPlayer().connection.latency();
    }

    @Override
    public void sendMessage(@NotNull TabComponent message) {
        getPlayer().sendSystemMessage(message.convert());
    }

    @Override
    public boolean hasInvisibilityPotion() {
        return false;
    }

    @Override
    public boolean isDisguised() {
        return false;
    }

    @Override
    @Nullable
    public TabList.Skin getSkin() {
        Collection<Property> properties = getPlayer().getGameProfile().getProperties().get(TabList.TEXTURES_PROPERTY);
        if (properties.isEmpty()) return null; // Offline mode
        Property property = properties.iterator().next();
        return new TabList.Skin(property.value(), property.signature());
    }

    @Override
    @NotNull
    public ServerPlayer getPlayer() {
        return (ServerPlayer) player;
    }

    @Override
    public FabricPlatform getPlatform() {
        return (FabricPlatform) platform;
    }

    @Override
    public boolean isVanished0() {
        return false;
    }

    @Override
    public int getGamemode() {
        return getPlayer().gameMode.getGameModeForPlayer().getId();
    }

    @Override
    public double getHealth() {
        return getPlayer().getHealth();
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return getPlayer().getGameProfile().getName(); // Will make it work properly if someone asks
    }

    /**
     * Sends the packet to the player.
     *
     * @param   packet
     *          Packet to send
     */
    public void sendPacket(@NotNull Packet<?> packet) {
        getPlayer().connection.send(packet);
    }
}
