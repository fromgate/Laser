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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Cmd implements CommandExecutor {

    LUtil u() {
        return Laser.instance.u;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        if ((args.length > 0) && u().checkCmdPerm(sender, args[0])) {
            if (args[0].equalsIgnoreCase("give")) return executeGive(sender, args);
            else
                switch (args.length) {
                    case 1:
                        return executeCmd(sender, args[0]);
                }
        } else u().printMSG(sender, "cmd_cmdpermerr", 'c');
        return true;
    }

    //
    // give <player> laserId  - just give
    // give <player> laserId ammo [amount]
    //
    @SuppressWarnings("deprecation")
    private boolean executeGive(CommandSender sender, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        if (args.length <= 2) {
            if (player == null) return false;
            int pageNum = (args.length == 2 && u().isIntegerGZ(args[1])) ? Integer.parseInt(args[1]) : 1;
            return Arsenal.giveMenu(player, pageNum);
        } else {
            if (player != null && !player.hasPermission("laser.give.other")) return false;
            String playerName = args[2];
            player = Bukkit.getPlayerExact(playerName);
            if (player == null) return u().returnMSG(true, sender, "msg_giveplayerfailed", playerName);
            String laserType = args[2];
            boolean giveAmmo = (args.length > 3 && args[3].equalsIgnoreCase("ammo"));
            int ammoAmount = (args.length > 4 && u().isIntegerGZ(args[4])) ? Integer.parseInt(args[4]) : 1;
            if (giveAmmo) {
                ItemStack ammo = Arsenal.getGunAmmo(player, laserType, ammoAmount);
                if (ammo == null) {
                    u().printMSG(sender, "msg_giveammofailed", laserType, playerName);
                } else {
                    u().giveItemOrDrop(player, ammo);
                    u().printMSG(sender, "msg_giveammo", u().itemToString(ammo), playerName);
                    u().printMSG(player, "msg_givereceiveammo", u().itemToString(ammo), laserType);
                }
            } else {
                if (Arsenal.giveGun(player, laserType)) {
                    u().printMSG(sender, "msg_give", Arsenal.toString(laserType), playerName);
                    u().printMSG(player, "msg_givereceive", laserType);
                } else u().printMSG(sender, "msg_givefailed", laserType, playerName);
            }
        }
        return true;
    }

    private boolean executeCmd(CommandSender sender, String cmd) {
        if (cmd.equalsIgnoreCase("help")) {
            u().PrintHlpList(sender, 1, 1000);
        } else if (cmd.equalsIgnoreCase("list")) {
            Arsenal.printList(sender);
        } else if (cmd.equalsIgnoreCase("reload")) {
            int count = Arsenal.reloadGuns();
            if (count == 0) u().printMSG(sender, "msg_nogunreload");
            else u().printMSG(sender, "msg_gunsreloaded", count);
        } else return false;
        return true;
    }
}

