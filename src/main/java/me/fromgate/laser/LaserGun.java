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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class LaserGun {

    String name;
    String item;
    String ammo_item = "";
    int ammo_money = 0;
    int ammo_exp = 0;
    String reload_time = "3s";
    int distance=32;
    int distance_dig = 32;
    double pushback = 0.5;
    boolean minelaser = true;
    int mine_chance = 50;
    List<String> unbreak_blocks = new ArrayList<String>();
    int mine_explode_chance = 0;
    float mine_explode_power=3;
    int mine_ignite_chance= 0;
    double entity_damage=10;
    List<String> entity_potion = new ArrayList<String>();
    double entity_knockback = 1.5;
    int entity_ignite_chance = 0;
    List<String> entity_effects = new ArrayList<String>();
    boolean transform = false;
    Map<String,String> block_transform = new HashMap<String,String>();
    Map<String,String> mob_transform = new HashMap<String,String>();

    boolean item_throw = true;
    //String throwable_items = "STONE*10";

    List<String> effect_beam = new ArrayList<String>(); // каждая строка вида: eff:<тип> param1:value1
    List<String> shoot = new ArrayList<String>();
    List<String> mine = new ArrayList<String>();
    List<String> reload = new ArrayList<String>();

    public LaserGun(String name, String item){
        this.name = name;
        this.item = item;
        effect_beam.add("effect:firework type:ball color:red");
        reload.add("effect:cloud num:5");
        reload.add("effect:sound type:fuse");
        entity_effects.add("effect:flamenew num:5");
    }


    public LaserGun (String type, YamlConfiguration cfg){
        name = cfg.getString(type+".name");
        item = cfg.getString(type+".item");
        //max_health = cfg.getInt(type+".max-durability");
        distance = cfg.getInt(type+".shoot-beam.distance",20);
        distance_dig = cfg.getInt(type+".shoot-beam.blocks-percolate",20);

        pushback = cfg.getDouble(type+".push-back",0D);

        ammo_item = cfg.getString(type+".ammo.item","DIAMOND");
        ammo_money = cfg.getInt(type+".ammo.money",0);
        ammo_exp = cfg.getInt(type+".ammo.experience",0);
        reload_time = cfg.getString(type+".ammo.reload-time","3s");

        minelaser = cfg.getBoolean(type+".blocks.break-enable",true);
        mine_chance = cfg.getInt(type+".blocks.drop-chance",30);
        mine_explode_chance=cfg.getInt(type+".blocks.explode-chance",0);
        mine_explode_power = (float) cfg.getDouble(type+".blocks.explode-power",3);
        mine_ignite_chance = cfg.getInt(type+".blocks.ignite-chance",30);
        unbreak_blocks = cfg.getStringList(type+".blocks.unbreakable");

        entity_damage = cfg.getDouble(type+".entity.damage",10);
        entity_knockback = cfg.getDouble(type+".entity.knockback",0);
        entity_ignite_chance = cfg.getInt(type+".entity.ignite-chance",50);
        entity_potion = cfg.getStringList(type+".entity.potions");

        transform = cfg.getBoolean(type+".transform.enable",false);
        block_transform = Arsenal.listToMap(cfg.getStringList(type+".transform.blocks"));
        mob_transform = Arsenal.listToMap(cfg.getStringList(type+".transform.entities"));

        effect_beam = cfg.getStringList(type+".visual-effect.beam");
        shoot = cfg.getStringList(type+".visual-effect.shot");
        mine = cfg.getStringList(type+".visual-effect.block-break");
        entity_effects = cfg.getStringList(type+".visual-effect.entity-hit");
        reload = cfg.getStringList(type+".visual-effect.reload");

    }

    public boolean isLaserGun(ItemStack item) {
        return u().compareItemStr(item, this.item);
    }

    private LUtil u() {
        return Laser.instance.u;
    }

    public void shoot(Player p) {
        List<Block> beam = getBeam(p);
        if (beam.isEmpty()) return;

        if (reload_time.isEmpty()||Arsenal.isReloaded(p, Integer.toString(hashCode()), reload_time)){
            if (pay (p)){
                playShootReload (p,beam,true);
                transformBlocks(beam);
                processMineBeam (beam);
                processVelocity (p);
                processEntityBeam (p,beam);

                //itemThrow(p, u().parseItemStack(throwable_items), 1);

            } else {
                u().printMSG(p, "msg_noammo", Arsenal.ammoToString(this.ammo_money,this.ammo_exp, this.ammo_item));
                playShootReload (p,beam,false);
            }
        } else {
            playShootReload (p,beam,false);
        }
    }

    private void processVelocity(Player p){
        if (pushback == 0) return;
        Vector v = p.getLocation().getDirection();
        v=v.normalize();
        v=v.multiply(pushback);
        p.setFallDistance(0);
        p.setVelocity(v.multiply(-1));
    }

    private void processEntityBeam(final Player p, List<Block> beam){
        Set<LivingEntity> entities = Arsenal.getEntityBeam(p, beam);
        if (entities.isEmpty()) return;
        entities=transformMobs(entities);
        for (LivingEntity le: entities){
            if (le.hasMetadata("Laser-transformed")) continue;
            if (!Arsenal.damageEntity(p, le, entity_damage)) continue;
            if (u().rollDiceChance(entity_ignite_chance)) le.setFireTicks(200+u().getRandomInt(800));
            if (entity_knockback!=0) le.setVelocity(p.getLocation().getDirection().multiply(entity_knockback));
            if (!entity_potion.isEmpty()) Arsenal.potionEffect(le, entity_potion);
            if (!entity_effects.isEmpty()) Arsenal.playEffects(le.getLocation(),entity_effects);
        }
    }

    /*
    private void itemThrow(final Player p, ItemStack items, final double power){
        for (int i = 0; i<items.getAmount();i++){
            final ItemStack is = items.clone();
            is.setAmount(1);
            Bukkit.getScheduler().runTaskLater(Laser.instance, new Runnable(){
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    if (is.getType().isBlock()){
                        FallingBlock fb = p.getWorld().spawnFallingBlock(p.getEyeLocation(), is.getType(), is.getData().getData());
                        fb.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(power));
                    } else {
                        Item item = p.getWorld().dropItem(p.getEyeLocation(), is);
                        item.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(power));
                    }
                }
            }, i*2);
        }
    } */

    private void processMineBeam(List<Block> beam){
        if (beam == null) return;
        if (beam.isEmpty()) return;
        //int count = 0;
        for (Block b : beam){
            Arsenal.playEffects(b, effect_beam);
            if ((b.getType()!=Material.AIR)&&(this.minelaser)){
                if  (u().rollDiceChance(mine_explode_chance)) b.getWorld().createExplosion(b.getLocation().add(0.5,0.5,0.5), mine_explode_power, u().rollDiceChance(mine_ignite_chance));
                else if (u().rollDiceChance(mine_chance)) b.breakNaturally();
                else b.setType(Material.AIR);
                if (u().rollDiceChance(mine_ignite_chance)) b.setType(Material.FIRE);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isBlockUnbreackable(Block b){
        if (b.getType() == Material.BEDROCK) return true;
        for (String bstr: unbreak_blocks){
            if (u().compareItemStr(b.getTypeId(), b.getData(), bstr)) return true;
        }
        return false;
    }

    private List<Block> getBeam(Player p) {
        List<Block> beam = new ArrayList<Block>();
        BlockIterator bi = new BlockIterator (p, distance);
        int count = 0;
        while (bi.hasNext()) {
            Block b = bi.next();
            if (!b.isEmpty()){
                if ((count==0)||(count<distance_dig)) count++;
                else break;
                if (!Arsenal.breakBlock(b, p)) break;
                if (isBlockUnbreackable(b)) break;
            }
            beam.add(b);
        }
        return beam;
    }

    private void playShootReload(Player p, List<Block> beam, boolean shoot){
        Location loc = p.getEyeLocation();
        if (beam.size()>=2) loc = beam.get(1).getLocation();
        if (shoot) Arsenal.playEffects(loc, this.shoot);
        else Arsenal.playEffects(loc, reload);
    }

    @SuppressWarnings("deprecation")
    private boolean transformBlocks(List<Block> beam){
        if (!transform) return false;
        if (block_transform.isEmpty()) return false;
        for (Block b : beam){
            for (String bstr : block_transform.keySet()){
                ItemStack ib = u().parseItemStack(bstr);
                if (b.getType().equals(ib.getType())){
                    ItemStack pb = u().parseItemStack(block_transform.get(bstr));
                    if ((pb != null)&&(pb.getType().isBlock()))
                        b.setTypeIdAndData(pb.getTypeId(), pb.getData().getData(), false);
                    break;
                }
            }
        }
        return true;
    }

    private Set<LivingEntity> transformMobs(Set<LivingEntity> entities){
        if (!transform) return entities;
        if (mob_transform.isEmpty()) return entities;
        Set<LivingEntity> newmobs = new HashSet<LivingEntity>();
        for (LivingEntity e : entities){
            if (mob_transform.containsKey(e.getType().name())){
                String param = mob_transform.get(e.getType().name());
                Map<String,String> params = Arsenal.parseParams(param,"type");
                newmobs.addAll(MobSpawn.mobSpawn(e, params));
                e.remove();
                e.setMetadata("Laser-transformed", new FixedMetadataValue(Laser.instance,true));
            }
        }
        entities.addAll(newmobs);
        return entities;
    }

    public String toString(){
        return name+" ["+ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', item))+"]";
    }

    public boolean pay(Player player){
        if (this.ammo_money>0){
            if (!VaultUtil.isEconomyConected()) {
                return false;
            }
            if (VaultUtil.getBalance(player.getName())<this.ammo_money) {
                return false;
            }
        }
        if ((this.ammo_exp>0)&&(player.getTotalExperience()<this.ammo_exp)) {
            return false;
        }

        if ((!this.ammo_item.isEmpty())&&(!u().hasItemInInventory(player, this.ammo_item))) {
            return false;
        }

        if (this.ammo_money>0)  {
            VaultUtil.withdrawPlayer(player.getName(), this.ammo_money);
        }
        if (this.ammo_exp>0) {
            player.setTotalExperience(player.getTotalExperience()-this.ammo_exp);
        }
        if (!this.ammo_item.isEmpty()) {
            u().removeItemInInventory(player, this.ammo_item);
        }
        return true;
    }

}
