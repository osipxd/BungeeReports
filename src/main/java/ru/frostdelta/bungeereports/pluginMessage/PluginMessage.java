package ru.frostdelta.bungeereports.pluginMessage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import ru.frostdelta.bungeereports.Loader;
import ru.frostdelta.bungeereports.executor.Executor;
import ru.frostdelta.bungeereports.gui.UserUI;

public class PluginMessage implements PluginMessageListener {

    private Loader plugin;
    private int players;

    public PluginMessage(Loader instance){

        plugin = instance;

    }


    private final UserUI UserUI = new UserUI(plugin);

    public void sendMessage(Player player){
        if(!plugin.isEnabled()) return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("PlayerList");
        out.writeUTF(plugin.getConfig().getString("bungee.servername"));

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());

    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord") || !plugin.isEnabled()) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        String subchannel = in.readUTF();


        if(subchannel.equalsIgnoreCase("PlayerCount")){
            String server = in.readUTF();
            int playercount = in.readInt();

            players = playercount;
            sendMessage(player);
            return;
        }

        if(subchannel.equalsIgnoreCase("PlayerList")) {
            String server = in.readUTF();

            String[] playerList = in.readUTF().split(", ");


            if(player.hasPermission("bungeereports.player") && Executor.getSenders().contains(player)){
                player.openInventory(UserUI.openGUI(player, players, playerList));
                Executor.getSenders().remove(players);
                return;
            }
        }

    }
}
