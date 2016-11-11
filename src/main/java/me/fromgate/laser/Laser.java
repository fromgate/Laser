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

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;


public class Laser extends JavaPlugin {
    protected static Laser instance;
    LUtil u;
    LaserListener l;
    Cmd cmd;

    String language = "english";
    boolean language_save = false;
    boolean check_updates = true;


    public void reloadCfg() {
        reloadConfig();
        language = getConfig().getString("general.language", "english");
        language_save = getConfig().getBoolean("general.language-save", false);
        check_updates = getConfig().getBoolean("general.version-check", true);
        getConfig().set("general.language", language);
        getConfig().set("general.language-save", language_save);
        getConfig().set("general.version-check", check_updates);
        saveConfig();
    }


    @Override
    public void onEnable() {
        reloadCfg();
        instance = this;
        u = new LUtil(this, language_save, language, "laser");
        u.initUpdateChecker("Laser", "68494", "laser", check_updates);
        l = new LaserListener();
        cmd = new Cmd();
        getCommand("laser").setExecutor(cmd);
        getServer().getPluginManager().registerEvents(l, this);
        Arsenal.init();
        VaultUtil.init();
        PlayEffectUtil.init();


        try {
            MetricsLite metrics = new MetricsLite(this);
            metrics.start();
        } catch (IOException e) {
        }
    }


}
