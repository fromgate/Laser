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

import java.util.Map;

import me.fromgate.playeffect.Effects;
import me.fromgate.playeffect.PlayEffect;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public class PlayEffectUtil {
    private static boolean isConnected = false;

    public static void init(){
        isConnected = checkPlayEffect();
    }

    public static void playEffect(Location loc, String param){
        if (isConnected){
            Map<String,String> params = Effects.parseParams(param);
            String effect = Effects.getParam(params, "effect", "FLAME");
            PlayEffect.play(effect,loc.add(0.5, 0.5, 0.5),params);
        } else {
            loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
        }
    }

    private static boolean checkPlayEffect(){
        return (Bukkit.getServer().getPluginManager().getPlugin("PlayEffect") != null);
    }
}
