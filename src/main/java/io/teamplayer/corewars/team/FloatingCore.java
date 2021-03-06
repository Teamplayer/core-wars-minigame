package io.teamplayer.corewars.team;

import com.comphenix.packetwrapper.WrapperPlayServerEntityLook;
import io.teamplayer.corewars.CoreWars;
import io.teamplayer.teamcore.immutable.ImmutableLocation;
import io.teamplayer.teamcore.util.ColoredBlockUtil;
import io.teamplayer.teamcore.util.TeamRunnable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * The object responsible for a little floating wool block that visually represents a RespawnCore
 */
class FloatingCore {

    private static final List<FloatingCore> floatingCores = new LinkedList<>();
    private static int rotateTick = 0;

    private final ImmutableLocation location;
    private final RespawnCore respawnCore;

    private final ArmorStand armorStand;

    private boolean destroy = false;

    static {
        ((TeamRunnable) () -> {
            floatingCores.removeIf(fc -> fc.destroy);

            for (FloatingCore core : floatingCores) {
                final WrapperPlayServerEntityLook packet = new WrapperPlayServerEntityLook();

                packet.setEntityID(core.armorStand.getEntityId());
                packet.setPitch(180F);
                packet.setYaw(rotateTick % 360);
                packet.setOnGround(false);

                Bukkit.getOnlinePlayers().forEach(packet::sendPacket);
            }

            rotateTick++;
        }).runTaskTimerAsync(CoreWars.getInstance(), 1);
    }

    FloatingCore(Location location, RespawnCore respawnCore) {
        this.location = ImmutableLocation.from(location);
        this.respawnCore = respawnCore;

        location.add(0, -1.7, 0);
        armorStand = ((ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND));

        armorStand.setVisible(false);

        Optional<Material> coreMaterial = ColoredBlockUtil.transformMaterialColor(Material.WHITE_WOOL,
                respawnCore.getVisualTeam().getType().getDyeColor());
        armorStand.getEquipment().setHelmet(new ItemStack(coreMaterial.orElse(Material.WHITE_WOOL)));

        floatingCores.add(this);
    }

    void destroy() {
        armorStand.remove();
        destroy = true;
    }
}
