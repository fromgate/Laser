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

public class LUtil extends FGUtilCore {

    public LUtil(Laser plg, boolean savelng, String lng, String plgcmd) {
        super(plg, savelng, lng, plgcmd,"laser");
        initMessages();
        initCmd();
        if (savelng) this.SaveMSG();
    }

    private void initCmd() {
        addCmd("help", "config", "hlp_thishelp","&3/laser help",'b',true);
        addCmd("list", "list", "hlp_list","&3/laser list",'b',true);
        addCmd("give", "give", "hlp_list","&3/laser give <laser type> [ammo [amount]]",'b');
        addCmd("reload", "config", "hlp_reload","&3/laser reload",'b',true);
    }

    private void initMessages(){
        addMSG("msg_gunslist", "Laser guns:");
        addMSG("msg_nogunreload", "No laser guns found");
        addMSG("msg_gunsreloaded", "Laser guns loaded: %1%");
        addMSG("hlp_list", "%1% - display list of laser guns");
        addMSG("hlp_reload", "%1% - reload settings and laser guns from configuration files");
        addMSG("msg_give", "You gived a laser gun %1% to %2%");
        addMSG("msg_givereceive", "You received a laser gun %1%");
        addMSG("msg_givefailed", "Failed to give %1% to %2%!");
        addMSG("msg_giveplayerfailed", "Unknown player: %1%");

        addMSG("msg_noammo", "You need more ammo to shoot: %1%");
        addMSG("msg_giveammofailed", "Failed to give ammo of the gun %1% to %2%");
        addMSG("msg_giveammo", "You gived some ammo (%1%) to %3%");
        addMSG("msg_givereceiveammo", "You received some ammo %1% for laser-gun %2%");
    }
}
