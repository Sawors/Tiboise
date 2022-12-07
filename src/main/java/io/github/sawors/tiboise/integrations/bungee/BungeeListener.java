package io.github.sawors.tiboise.integrations.bungee;

import io.github.sawors.tiboise.Tiboise;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class BungeeListener implements PluginMessageListener {
    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NotNull [] message) {
        String response = new String(message);
        
        Tiboise.logAdmin("plugin message sent : "+response);
        
        if(channel.equals(Tiboise.getBungeeChannel())){
            try(ByteArrayInputStream in = new ByteArrayInputStream(message); DataInputStream ins = new DataInputStream(in)){
                String subchannel = ins.readUTF();
                Tiboise.logAdmin("subchan: "+subchannel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void movePlayerToServer(Player player, String server){
        if(Tiboise.isBungeeEnabled()){
            try(ByteArrayOutputStream arrayoutput = new ByteArrayOutputStream(); DataOutputStream output = new DataOutputStream(arrayoutput)) {
                output.writeUTF("Connect");
                output.writeUTF(server);
                
                player.sendPluginMessage(Tiboise.getPlugin(), Tiboise.getBungeeChannel(), arrayoutput.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
