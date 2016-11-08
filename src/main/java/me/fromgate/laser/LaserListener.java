/*
 *  Laser, Minecraft bukkit plugin
 *  (c)2013-2016, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/bukkit-plugins/laser/
 *
 *  This file is part of Laser.
 *
 *  Laser is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Laser is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OkGlass.  If not, see <http://www.gnorg/licenses/>.
 *
 */

package me.fromgate.laser;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class LaserListener implements Listener {

    LUtil u() {
        return Laser.instance.u;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        u().updateMsg(event.getPlayer());
    }

    @EventHandler
    public void shootLaserGun(PlayerInteractEvent event) {
        if ((event.getAction() != Action.RIGHT_CLICK_AIR) && (event.getAction() != Action.RIGHT_CLICK_BLOCK)) return;
        Player p = event.getPlayer();
        if (p.getItemInHand() == null) return;
        LaserGun gun = Arsenal.getGunByItem(p.getItemInHand());
        if (gun == null) return;
        String guntype = Arsenal.getGunType(gun);
        if (guntype.isEmpty()) return;
        if (p.hasPermission("laser.gun.all") || p.hasPermission("laser.gun." + guntype))
            gun.shoot(p);
    }

    @EventHandler
    public void shootLaserGunEntity(PlayerInteractEntityEvent event) {
        Player p = event.getPlayer();
        if (p.getItemInHand() == null) return;
        LaserGun gun = Arsenal.getGunByItem(p.getItemInHand());
        if (gun == null) return;
        String guntype = Arsenal.getGunType(gun);
        if (guntype.isEmpty()) return;
        if (p.hasPermission("laser.gun.all") || p.hasPermission("laser.gun." + guntype))
            gun.shoot(p);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDropLoot(EntityDeathEvent event) {
        if (event.getEntity().hasMetadata("Laser-transformed")) {
            event.setDroppedExp(0);
            event.getDrops().clear();
            return;
        }
        if (event.getEntity().hasMetadata("Laser-drop")) {
            List<ItemStack> stacks = Arsenal.parseItemStacks(event.getEntity().getMetadata("Laser-drop").get(0).asString());
            if (stacks != null) {
                event.getDrops().clear();
                event.getDrops().addAll(stacks);
            }
        }
        if (event.getEntity().hasMetadata("Laser-xp")) {
            int xp = u().getMinMaxRandom(event.getEntity().getMetadata("Laser-xp").get(0).asString());
            event.setDroppedExp(xp);
        }
        if (event.getEntity().hasMetadata("Laser-money")) {
            if (!VaultUtil.isEconomyConected()) return;
            Player killer = getKiller(event.getEntity().getLastDamageCause());
            if (killer != null) {
                int money = u().getMinMaxRandom(event.getEntity().getMetadata("Laser-money").get(0).asString());
                VaultUtil.depositPlayer(killer.getName(), money);
                Laser.instance.u.printMSG(killer, "msg_mobbounty", 'e', '6', VaultUtil.formatMoney(Integer.toString(money)), event.getEntity().getType().name());
            }
        }
    }

    public Player getKiller(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent evdmg = (EntityDamageByEntityEvent) event;
            if (evdmg.getDamager().getType() == EntityType.PLAYER) return (Player) evdmg.getDamager();
            if (evdmg.getCause() == DamageCause.PROJECTILE) {
                Projectile prj = (Projectile) evdmg.getDamager();
                if (prj.getShooter() instanceof Player) return (Player) prj.getShooter();
            }
        }
        return null;
    }


}
