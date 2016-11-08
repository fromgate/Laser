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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Arsenal {

    static LUtil u(){
        return Laser.instance.u;
    }
    private static Map<String,LaserGun> guns = new HashMap<String,LaserGun>();

    public static void init(){
        File f = new File(Laser.instance.getDataFolder()+File.separator+"arsenal.yml");
        if (f.exists()) loadGuns();
        else loadGunsResource();
        if (guns.isEmpty()) createFirstGun();
    }


    public static void createFirstGun(){
        try {
            YamlConfiguration cfg = new YamlConfiguration();
            File f = new File(Laser.instance.getDataFolder()+File.separator+"arsenal.yml");
            f.createNewFile();
            LaserGun gun = new LaserGun ("LaserGun","&4Laser_Gun$BLAZE_ROD");
            guns.put("lasergun", gun);
            saveLaserGun ("lasergun",gun,cfg);
            cfg.save(f);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static int reloadGuns(){
        guns.clear();
        loadGuns();
        return guns.size();
    }

    @SuppressWarnings("deprecation")
    public static void loadGunsResource(){
        try{
            YamlConfiguration cfg = new YamlConfiguration();
            InputStream is = Laser.instance.getClass().getResourceAsStream("/arsenal.yml");
            if (is!=null) {
                cfg.load(is);
                for (String key : cfg.getKeys(false)){
                    LaserGun gun = new LaserGun (key,cfg);
                    guns.put(key, gun);
                }
                if (guns.size()>0){
                    File f = new File(Laser.instance.getDataFolder()+File.separator+"arsenal.yml");
                    if (f.exists()) f.delete();
                    f.createNewFile();
                    cfg.save(f);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void loadGuns(){
        try{
            YamlConfiguration cfg = new YamlConfiguration();
            File f = new File(Laser.instance.getDataFolder()+File.separator+"arsenal.yml");
            cfg.load(f);
            for (String key : cfg.getKeys(false)){
                LaserGun gun = new LaserGun (key,cfg);
                guns.put(key, gun);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static List<String> mapToList(Map<String,String> map){
        List<String> list = new ArrayList<String>();
        for (String key : map.keySet())
            list.add(key+"="+map.get(key));
        return list;
    }

    public static Map<String,String> listToMap(List<String> list){
        Map<String,String> map = new HashMap<String,String>();
        if (list == null) return map;
        for (String str : list){
            String key = str;
            String value = "";
            if (str.contains("=")){
                key = str.substring(0, str.indexOf("="));
                value = str.substring(str.indexOf("=")+1);
            }
            map.put(key, value);
        }
        return map;
    }


    public static void saveLaserGun(String type, LaserGun gun, YamlConfiguration cfg){
        cfg.set(type+".name",gun.name);
        cfg.set(type+".item",gun.item);
        //cfg.set(type+".max-durability",gun.max_health);
        cfg.set(type+".shoot-beam.distance",gun.distance);
        cfg.set(type+".shoot-beam.blocks-percolate",gun.distance_dig);

        cfg.set(type+".ammo.item",gun.ammo_item);
        cfg.set(type+".ammo.money",gun.ammo_money);
        cfg.set(type+".ammo.experience",gun.ammo_exp);
        cfg.set(type+".ammo.reload-time",gun.reload_time);

        cfg.set(type+".push-back",gun.pushback);

        cfg.set(type+".blocks.break-enable",gun.minelaser);
        cfg.set(type+".blocks.drop-chance",gun.mine_chance);
        cfg.set(type+".blocks.explode-chance",gun.mine_explode_chance);
        cfg.set(type+".blocks.explode-power",gun.mine_explode_power);
        cfg.set(type+".blocks.ignite-chance",gun.mine_ignite_chance);
        cfg.set(type+".blocks.unbreakable",gun.unbreak_blocks);


        cfg.set(type+".entity.damage",gun.entity_damage);
        cfg.set(type+".entity.knockback",gun.entity_knockback);
        cfg.set(type+".entity.ignite-chance",gun.entity_ignite_chance);
        cfg.set(type+".entity.potions",gun.entity_potion);

        cfg.set(type+".transform.enable",gun.transform);
        cfg.set(type+".transform.blocks",mapToList(gun.block_transform));
        cfg.set(type+".transform.entities",mapToList(gun.mob_transform));

        cfg.set(type+".visual-effect.beam",gun.effect_beam);
        cfg.set(type+".visual-effect.shot",gun.shoot);
        cfg.set(type+".visual-effect.block-break",gun.mine);
        cfg.set(type+".visual-effect.reload",gun.reload);
        cfg.set(type+".visual-effect.entity-hit",gun.entity_effects);
    }

    public static LaserGun getGunByItem(ItemStack item) {
        for (String key : guns.keySet()){
            LaserGun gun = guns.get(key);
            if (gun.isLaserGun (item)) return gun;
        }
        return null;
    }

    public static String getGunType(LaserGun gun) {
        for (String key : guns.keySet())
            if (guns.get(key).equals(gun)) return key;
        return "";
    }

    public static boolean damageEntity (Player damager, LivingEntity e, double damage){
        EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(damager, e, DamageCause.ENTITY_ATTACK, Math.max(damage, 0));
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) e.damage(event.getDamage(), event.getDamager());
        return !event.isCancelled();
    }

    public static boolean breakBlock(Block b, Player p){
        //BlockState state = block.getState();
        BlockBreakEvent event = new BlockBreakEvent(b, p);
        Bukkit.getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }


    public static void potionEffect(LivingEntity le, List<String> plist) {
        for (String potstr : plist) potionEffect(le,potstr);
    }

    public static void potionEffect(LivingEntity le, String potstr) {
        if (potstr.isEmpty()) return;
        Map<String,String> params = parseParams(potstr,"type");
        String pstr = getParam(params,"type","");
        if (pstr.isEmpty()) return;
        int duration = safeLongToInt(u().timeToTicks(u().parseTime(getParam(params, "time", "3s"))));
        int amplifier = Math.max(getParam(params, "level", 1)-1, 0);
        boolean ambient = getParam(params, "ambient", false);
        PotionEffectType pef = parsePotionEffect (pstr.toUpperCase());
        if (pef == null) return;
        PotionEffect pe = new PotionEffect (pef, duration, amplifier,ambient);
        le.addPotionEffect(pe);
    }

    public static PotionEffectType parsePotionEffect (String name) {
        PotionEffectType pef = null;
        try{
            pef = PotionEffectType.getByName(name);
        } catch(Exception e){
        }
        return pef;
    }

    public static String getParam (Map<String,String> params, String key, String defaultvalue){
        if (params.containsKey(key)) return params.get(key);
        return defaultvalue;
    }

    public static int getParam (Map<String,String> params, String key, int defaultvalue){
        if (!params.containsKey(key)) return defaultvalue;
        String istr = params.get(key);
        if (!Laser.instance.u.isInteger(istr)) return defaultvalue;
        return Integer.parseInt(istr);
    }

    public static double getParam (Map<String,String> params, String key, double defaultvalue){
        if (!params.containsKey(key)) return defaultvalue;
        String istr = params.get(key);
        if (!istr.matches("[0-9]+\\.?[0-9]*")) return defaultvalue;
        return Double.parseDouble(istr);
    }



    public static boolean getParam (Map<String,String> params, String key, boolean defaultvalue){
        if (!params.containsKey(key)) return defaultvalue;
        if (params.get(key).equalsIgnoreCase("true")) return true;
        return false;
    }


    public static Map<String,String> parseParams(String param,String defaultkey){
        Map<String,String> params = new HashMap<String,String>();
        if (param.isEmpty()) return params;
        String[]ln = param.split(" ");
        if (ln.length>0)
            for (int i = 0; i < ln.length; i++){
                String key = ln[i];
                String value = "";
                if (ln[i].contains(":")){
                    key = ln[i].substring(0,ln[i].indexOf(":"));
                    value = ln[i].substring(ln[i].indexOf(":")+1);
                } else {
                    value = key;
                    key = defaultkey;
                }
                params.put(key, value);
            }
        return params;
    }

    public static int safeLongToInt(long l) {
        if (l<Integer.MIN_VALUE) return Integer.MIN_VALUE;
        if (l > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) l;
    }

    public static void playEffects(Block b,List<String> effs){
        playEffects (b.getLocation(), effs);
    }

    public static void playEffects(Location loc,List<String> effs){
        for (String str: effs)
            PlayEffectUtil.playEffect (loc, str);
    }


    public static Set<LivingEntity> getEntityBeam (Player p, List<Block> beam){
        Set<LivingEntity> list = new HashSet<LivingEntity>();
        for (Block b : beam)
            for (Entity e : b.getChunk().getEntities()){
                if (!(e instanceof LivingEntity)) continue;
                LivingEntity le = (LivingEntity) e;
                if (le.equals((LivingEntity) p)) continue;
                if (isEntityAffectByBeamBlock(b,le))  list.add(le);
            }
        return list;
    }

    private static boolean isEntityAffectByBeamBlock(Block b, LivingEntity le){
        if (le.getLocation().getBlock().equals(b)) return true;
        if (le.getEyeLocation().getBlock().equals(b)) return true;
        return false;
    }


    public static boolean isReloaded(Player p, String hashcode,String timestr){
        String md = "Laser"+hashcode+p.getName();
        Long time = u().parseTime (timestr);
        if (p.hasMetadata(md)&&((System.currentTimeMillis()-p.getMetadata(md).get(0).asLong())<=time)) return false;
        p.setMetadata(md, new FixedMetadataValue (Laser.instance, System.currentTimeMillis()));
        return true;
    }


    public static boolean giveGun(Player p, String type){
        if (!guns.containsKey(type)) return false;
        ItemStack item = u().parseItemStack(guns.get(type).item);
        if (item == null) return false;
        u().giveItemOrDrop(p, item);
        return true;
    }

    public static ItemStack getGunAmmo (Player p, String type, int amount){
        if (!guns.containsKey(type)) return null;
        ItemStack item = u().parseItemStack(guns.get(type).ammo_item);
        if (item == null) return null;
        item.setAmount(item.getAmount()*Math.max(amount, 1));
        return item;
    }

    public static String toString(String type){
        if (!guns.containsKey(type)) return "";
        return guns.get(type).toString();
    }

    public static void printList(CommandSender p){
        List<String> lst = new ArrayList<String>();
        for (String key : guns.keySet())
            lst.add("&2"+key+" : &a"+guns.get(key).toString());
        u().printPage(p, lst, 1, "msg_gunslist", "", false, 1000);
    }


    @SuppressWarnings("deprecation")
    public static ItemStack setEnchantments (ItemStack item, String enchants){
        ItemStack i = item.clone();
        if (enchants.isEmpty()) return i;
        String [] ln = enchants.split(",");
        for (String ec : ln){
            if (ec.isEmpty()) continue;
            Color clr = colorByName (ec);
            if (clr != null){
                if (u().isIdInList(item.getTypeId(), "298,299,300,301")){
                    LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
                    meta.setColor(clr);
                    i.setItemMeta(meta);
                }
            } else {
                String ench = ec;
                int level = 1;
                if (ec.contains(":")){
                    ench = ec.substring(0,ec.indexOf(":"));
                    level = Math.max(1, u().getMinMaxRandom (ec.substring(ench.length()+1)));
                }
                Enchantment e = Enchantment.getByName(ench.toUpperCase());
                if (e == null) continue;
                i.addUnsafeEnchantment(e, level);
            }
        }
        return i;
    }

    public static Color colorByName(String colorname){
        Color [] clr = {Color.WHITE, Color.SILVER, Color.GRAY, Color.BLACK,
                Color.RED, Color.MAROON, Color.YELLOW, Color.OLIVE,
                Color.LIME, Color.GREEN, Color.AQUA, Color.TEAL,
                Color.BLUE,Color.NAVY,Color.FUCHSIA,Color.PURPLE};
        String [] clrs = {"WHITE","SILVER", "GRAY", "BLACK",
                "RED", "MAROON", "YELLOW", "OLIVE",
                "LIME", "GREEN", "AQUA", "TEAL",
                "BLUE","NAVY","FUCHSIA","PURPLE"};
        for (int i = 0; i<clrs.length;i++)
            if (colorname.equalsIgnoreCase(clrs[i])) return clr[i];
        return null;
    }

    public static List<ItemStack> parseItemStacks (String items){
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        String[] ln = items.split(";");
        for (String item : ln){
            ItemStack stack = u().parseItemStack(item);
            if (stack != null) stacks.add(stack);
        }
        return stacks;
    }

    public static String ammoToString(int money, int xp, String itemstr){
        String str = "";
        if (money>0) str = VaultUtil.isEconomyConected() ? VaultUtil.formatMoney(Integer.toString(money)) : "$"+Integer.toString(money);
        if (xp>0){
            str = str.isEmpty() ? Integer.toString(xp)+" XP" : str+", "+Integer.toString(xp)+" XP";
        }
        ItemStack item = u().parseItemStack(itemstr);
        if ((item !=null)&&(item.getType() != Material.AIR)){
            str = str.isEmpty() ? u().itemToString(item) : str+", "+u().itemToString(item);
        }
        if (str.isEmpty()) str = "N/A";
        return str;
    }

    public static List<String> getPermitedGuns (Player player){
        List<String> permitedGuns = new ArrayList<String>();
        for (String guntype : guns.keySet()){
            if (player.hasPermission("laser.gun.all")||player.hasPermission("laser.gun."+guntype))
                permitedGuns.add(guntype);
        }
        return permitedGuns;

    }

    public static List<ItemStack> getPermitedGunsAndAmmo(Player player){
        List<ItemStack> gunsAndAmmo = new ArrayList<ItemStack>();
        for (String type : getPermitedGuns(player)){
            if (!guns.containsKey(type)) continue;
            ItemStack item = u().parseItemStack(guns.get(type).item);
            if (item == null) continue;
            gunsAndAmmo.add(item);
            ItemStack ammo = u().parseItemStack(guns.get(type).ammo_item);
            if (ammo == null) continue;
            ammo.setAmount((int) (ammo.getAmount()*Math.floor(64.0/(double) ammo.getAmount())));
            gunsAndAmmo.add(ammo);
        }
        return gunsAndAmmo;

    }


    public static boolean giveMenu(Player player, int pageNum){
        if (player == null) return false;
        List<ItemStack> items = getPermitedGunsAndAmmo (player);
        if (items.isEmpty()) return false;
        int pageMax = (int) Math.ceil((double)items.size()/54);
        int page = (pageNum<1||pageNum>pageMax)? 1 : pageNum;
        String title = ChatColor.translateAlternateColorCodes('&',"&4Arsenal &c["+page+"/"+pageMax+"]");
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        for (int i = (page-1)*54; i<Math.min(page*54, items.size()); i++)
            inventory.addItem(items.get(i));
        player.openInventory(inventory);
        return true;
    }



}