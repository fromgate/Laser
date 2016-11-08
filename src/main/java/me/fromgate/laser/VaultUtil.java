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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

public class VaultUtil {
    private static boolean vault_perm = false;
    private static boolean vault_eco = false;
    private static Permission permission = null;
    private static Economy economy = null;

    public static String formatMoney(String value){
        if (!isEconomyConected()) return value;
        return economy.format(Double.parseDouble(value)); // Integer???
    }

    public static void init() {
        if (checkVault()){
            vault_perm = setupPermissions();
            vault_eco = setupEconomy();
        }
    }

    public static boolean isEconomyConected(){
        return vault_eco;
    }

    public static boolean isPermissionConected(){
        return vault_perm;
    }


    private static boolean setupPermissions(){
        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    private static boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }


    public static double getBalance(String account){
        if (!isEconomyConected()) return 0;
        return economy.getBalance(account);
    }

    public static boolean hasMoney(String account, double amount){
        if (!isEconomyConected()) return false;
        return economy.getBalance(account)>=amount;
    }

    public static void withdrawPlayer(String account, double amount){
        if (!isEconomyConected()) return;
        economy.withdrawPlayer(account, amount);
    }

    public static void depositPlayer(String account, double amount){
        if (!isEconomyConected()) return;
        economy.depositPlayer(account, amount);
    }

    public static boolean playerAddGroup (Player p, String group){
        if (!isPermissionConected()) return false;
        return permission.playerAddGroup(p, group);
    }

    public static boolean playerInGroup(Player p, String group) {
        if (!isPermissionConected()) return false;
        return permission.playerInGroup(p, group);
    }

    public static boolean playerRemoveGroup(Player p, String group) {
        if (!isPermissionConected()) return false;
        return permission.playerRemoveGroup(p, group);
    }

    private static boolean checkVault(){
        return (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null);
    }
}
