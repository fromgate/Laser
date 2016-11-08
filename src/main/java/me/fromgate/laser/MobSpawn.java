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

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobSpawn {

    public static List<LivingEntity> mobSpawn(LivingEntity sle, Map<String, String> params) {
        List<LivingEntity> newmob = new ArrayList<LivingEntity>();
        newmob.add(sle);
        if (sle == null) return newmob;
        if (sle.getType() == EntityType.PLAYER) return newmob;

        Location loc = sle.getLocation();
        Vector sle_velocity = sle.getVelocity();

        String mob = Arsenal.getParam(params, "type", "PIG");
        String hparam = Arsenal.getParam(params, "health", "0");
        double health = u().getMinMaxRandom(hparam);
        String dtheffect = Arsenal.getParam(params, "dtheffect", "");
        String chest = Arsenal.getParam(params, "chest", "");
        String leg = Arsenal.getParam(params, "leg", "");
        String helm = Arsenal.getParam(params, "helm", "");
        String boot = Arsenal.getParam(params, "boot", "");
        String weapon = Arsenal.getParam(params, "weapon", "");
        String poteff = Arsenal.getParam(params, "potion", "");
        String name = Arsenal.getParam(params, "name", "");
        String drop = Arsenal.getParam(params, "drop", "");
        String xp = Arsenal.getParam(params, "xp", "");
        String money = Arsenal.getParam(params, "money", "");
        String growl = Arsenal.getParam(params, "growl", "");
        String cry = Arsenal.getParam(params, "cry", "");
        String equip = Arsenal.getParam(params, "equip", "");
        double dmg = Arsenal.getParam(params, "dmg", 1.0D);
        List<LivingEntity> mobs = spawnMob(loc, mob);
        for (LivingEntity le : mobs) {
            le.teleport(sle);
            le.setVelocity(sle_velocity);
            setMobHealth(le, health == 0 ? (sle.getHealth() / sle.getMaxHealth()) * le.getMaxHealth() : health);
            setMobName(le, name);
            potionEffect(le, poteff);
            if (equip.isEmpty()) setMobEquipment(le, helm, chest, leg, boot, weapon);
            else setMobEquipment(le, equip);
            setMobDrop(le, drop);
            setMobXP(le, xp);
            setMobMoney(le, money);
            setMobDmgMultiplier(le, dmg);
            setMobGrowl(le, growl);
            setMobCry(le, cry);
            setDeathEffect(le, dtheffect);
        }
        newmob.addAll(mobs);
        return newmob;
    }


    @SuppressWarnings("deprecation")
    private static List<LivingEntity> spawnMob(Location loc, String mobstr) {
        List<LivingEntity> mobs = new ArrayList<LivingEntity>();
        String[] ln = mobstr.split(":");
        if (ln.length < 1) return mobs;

        for (int i = 0; i < Math.min(2, ln.length); i++) {
            String mbs = ln[i];
            String name = "";
            if (mbs.contains("$")) {
                name = mbs.substring(0, mbs.indexOf("$"));
                mbs = mbs.substring(name.length() + 1);
            }


            EntityType et = EntityType.fromName(mbs);
            if (mbs.equalsIgnoreCase("horse")) et = EntityType.HORSE;
            if (et == null) {
                u().logOnce("mobspawnunknowntype_" + mobstr, "Unknown mob type " + mbs + " (" + mobstr + ")");
                continue;
            }
            Entity e = loc.getWorld().spawnEntity(loc, et);
            if (e == null) {
                u().logOnce("mobspawnfail_" + mobstr, "Cannot spawn mob " + mbs + " (" + mobstr + ")");
                continue;
            }

            if (!(e instanceof LivingEntity)) {
                e.remove();
                u().logOnce("mobspawnnotmob_" + mobstr, "Cannot spawn mob " + mbs + " (" + mobstr + ")");
                continue;
            }
            LivingEntity mob = (LivingEntity) e;
            setMobName(mob, name);
            mobs.add(mob);
        }
        if (mobs.size() == 2) mobs.get(1).setPassenger(mobs.get(0));
        return mobs;
    }


    public static void setMobName(LivingEntity e, String name) {
        if (name.isEmpty()) return;
        if ((e.getCustomName() != null) && (!e.getCustomName().isEmpty())) return;
        e.setCustomName(ChatColor.translateAlternateColorCodes('&', name.replace("_", " ")));
        e.setCustomNameVisible(true);
    }

    public static void setMobXP(LivingEntity e, String xp) {
        if (xp.isEmpty()) return;
        e.setMetadata("Laser-xp", new FixedMetadataValue(Laser.instance, xp));
    }

    public static void setMobMoney(LivingEntity e, String money) {
        if (money.isEmpty()) return;
        e.setMetadata("Laser-money", new FixedMetadataValue(Laser.instance, money));
    }


    public static void setMobDrop(LivingEntity e, String drop) {
        //id:data*amount,id:dat*amount%chance;id:data*amount;id:dat*amount%chance;id:data*amount;id:dat*amount%chance
        if (drop.isEmpty()) return;
        String stack = parseRandomItemsStr(drop);
        if (stack.isEmpty()) return;
        setMobDropStack(e, stack);
    }

    private static void setMobDmgMultiplier(LivingEntity e, double dmg) {
        if (dmg < 0) return;
        e.setMetadata("Laser-dmg", new FixedMetadataValue(Laser.instance, dmg));
    }

    private static void setMobCry(LivingEntity e, String cry) {
        if (cry.isEmpty()) return;
        e.setMetadata("Laser-cry", new FixedMetadataValue(Laser.instance, cry));
    }

    private static void setMobGrowl(LivingEntity e, String growl) {
        if (growl.isEmpty()) return;
        e.setMetadata("Laser-growl", new FixedMetadataValue(Laser.instance, growl));
    }

    public static void setMobDropStack(LivingEntity e, String stack) {
        if (stack.isEmpty()) return;
        e.setMetadata("Laser-drop", new FixedMetadataValue(Laser.instance, stack));
    }

    private static void setDeathEffect(LivingEntity e, String dtheffect) {
        if (dtheffect.isEmpty()) return;
        e.setMetadata("Laser-deatheffect", new FixedMetadataValue(Laser.instance, dtheffect));
    }


    public static void setMobHealth(LivingEntity e, double health) {
        if (health > 0) {
            try {
                e.setMaxHealth(health);
                e.setHealth(health);
            } catch (Throwable ex) {
                u().logOnce("mob_health", "Failed to set mob health. This feature is not compatible with CB 1.5.2 (and older)...");
            }
        }
    }

    public static void setMobEquipment(LivingEntity e, String equip) {
        if (equip.isEmpty()) return;
        if (!u().isWordInList(e.getType().name(), "zombie,skeleton")) return;
        String[] ln = equip.split(";");
        if (ln.length == 0) return;
        String[] eq = {"", "", "", "", ""};
        for (int i = 0; i < Math.min(ln.length, 5); i++) eq[i] = ln[i];
        setMobEquipment(e, eq[0], eq[1], eq[2], eq[3], eq[4]);
    }

    public static void setMobEquipment(LivingEntity e, String helm, String chest, String leg, String boot, String weapon) {
        if (!u().isWordInList(e.getType().name(), "zombie,skeleton")) return;
        if (!helm.isEmpty()) {
            ItemStack item = getRndItem(helm);
            if (item != null) e.getEquipment().setHelmet(item);
        }
        if (!chest.isEmpty()) {
            ItemStack item = getRndItem(chest);
            if (item != null) e.getEquipment().setChestplate(item);
        }
        if (!leg.isEmpty()) {
            ItemStack item = getRndItem(leg);
            if (item != null) e.getEquipment().setLeggings(item);
        }
        if (!boot.isEmpty()) {
            ItemStack item = getRndItem(boot);
            if (item != null) e.getEquipment().setBoots(item);
        }
        if (!weapon.isEmpty()) {
            ItemStack item = getRndItem(weapon);
            if (item != null) e.getEquipment().setItemInHand(item);
        }
    }

    private static LUtil u() {
        return Laser.instance.u;
    }


    public static void potionEffect(LivingEntity e, String potion) {
        if (potion.isEmpty()) return;
        String[] pts = potion.split(",");
        for (String pot : pts) {
            String pef = "";
            int level = 1;
            String[] ln = pot.split(":");
            pef = ln[0];
            PotionEffectType pet = Arsenal.parsePotionEffect(pef);
            if (pet == null) continue;
            if ((ln.length == 2) && Laser.instance.u.isInteger(ln[1])) level = Integer.parseInt(ln[1]);
            PotionEffect pe = new PotionEffect(pet, Integer.MAX_VALUE, level, true);
            e.addPotionEffect(pe);
        }
    }

    public static ItemStack getRndItem(String str) {
        if (str.isEmpty()) return new ItemStack(Material.AIR);
        String[] ln = str.split(",");
        if (ln.length == 0) return new ItemStack(Material.AIR);
        ItemStack item = u().parseItemStack(ln[u().tryChance(ln.length)]);
        if (item == null) return new ItemStack(Material.AIR);
        item.setAmount(1);
        return item;
    }


    public static String parseRandomItemsStr(String items) {
        if (items.isEmpty()) return "";
        String[] loots = items.split("/");
        Map<String, Integer> drops = new HashMap<String, Integer>();
        int maxchance = 0;
        int nochcount = 0;
        for (String loot : loots) {
            String[] ln = loot.split("%");
            if (ln.length > 0) {
                String stacks = ln[0];
                if (stacks.isEmpty()) continue;
                int chance = -1;
                if ((ln.length == 2) && u().isInteger(ln[1])) {
                    chance = Integer.parseInt(ln[1]);
                    maxchance += chance;
                } else nochcount++;
                drops.put(stacks, chance);
            }
        }

        if (drops.isEmpty()) return "";
        int eqperc = (nochcount * 100) / drops.size();
        maxchance = maxchance + eqperc * nochcount;
        int rnd = u().tryChance(maxchance);
        int curchance = 0;
        for (String stack : drops.keySet()) {
            curchance = curchance + (drops.get(stack) < 0 ? eqperc : drops.get(stack));
            if (rnd <= curchance) return stack;
        }
        return "";
    }
}
