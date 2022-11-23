package io.github.sawors.tiboise.integrations.bungee;

import io.github.sawors.tiboise.Main;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class BungeeListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        String response = new String(message);
        
        Main.logAdmin("plugin message sent : "+response);
        
        if(channel.equals(Main.getBungeeChannel())){
            try(ByteArrayInputStream in = new ByteArrayInputStream(message); DataInputStream ins = new DataInputStream(in)){
                String subchannel = ins.readUTF();
                Main.logAdmin("subchan: "+subchannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void movePlayerToServer(Player player, String server){
        if(Main.isBungeeEnabled()){
            try(ByteArrayOutputStream arrayoutput = new ByteArrayOutputStream(); DataOutputStream output = new DataOutputStream(arrayoutput)) {
                output.writeUTF("Connect");
                output.writeUTF(server);
                
                player.sendPluginMessage(Main.getPlugin(), Main.getBungeeChannel(), arrayoutput.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
