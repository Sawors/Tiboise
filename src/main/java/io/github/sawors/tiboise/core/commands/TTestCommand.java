package io.github.sawors.tiboise.core.commands;

import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.YoutubeCallback;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.request.RequestVideoStreamDownload;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.audiochannel.AudioPlayer;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.core.CarryManager;
import io.github.sawors.tiboise.integrations.voicechat.VoiceChatIntegrationPlugin;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;

public class TTestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(commandSender instanceof Player p){
            if(args.length >= 1 && args[0].equals("c")){
                Block b = p.getTargetBlock(8);
                CarryManager.makePlayerCarryBlock(p,b);
                Tiboise.logAdmin(b.getType());
            } else {
                CarryManager.makePlayerDropCarry(p);
            }
            
            return true;
        }
        
        
        if(false){
            // init downloader with default config
            YoutubeDownloader downloader = new YoutubeDownloader();
            
            Config config = downloader.getConfig();
            config.setMaxRetries(1);
    
            String videoId = args.length > 0 ? args[0] : "dQw4w9WgXcQ";
            // async parsing
            RequestVideoInfo request = new RequestVideoInfo(videoId)
                    .callback(new YoutubeCallback<VideoInfo>() {
                        @Override
                        public void onFinished(VideoInfo videoInfo) {
                            System.out.println("Finished parsing");
                        }
                
                        @Override
                        public void onError(Throwable throwable) {
                            System.out.println("Error: " + throwable.getMessage());
                        }
                    })
                    .async();
            Response<VideoInfo> response = downloader.getVideoInfo(request);
            VideoInfo video = response.data(); // will block thread
            List<AudioFormat> audioFormats = video.audioFormats();
            audioFormats.forEach(it -> {
                System.out.println(it.audioQuality() + " : " + it.url());
            });
    
    
    
    
    
    
    
            File outputDir = Tiboise.getPlugin().getDataFolder();
            Format format = audioFormats.get(0);
            File outputFile = new File(outputDir + File.separator + videoId + ".wav");
// download in-memory to OutputStream
            
            try{
                outputFile.createNewFile();
            } catch (IOException e1){
                e1.printStackTrace();
            }
            try(OutputStream os = new FileOutputStream(outputFile)){
                RequestVideoStreamDownload requestVideoStreamDownload = new RequestVideoStreamDownload(format, os);
                downloader.downloadVideoStream(requestVideoStreamDownload);
                VoicechatServerApi api = VoiceChatIntegrationPlugin.getVoicechatServerApi();
                UUID channelId = UUID.randomUUID();
                Player p = null;
                Location pLoc = p.getLocation();
                ServerLevel level = api.fromServerLevel(p.getWorld());
                Position position = api.createPosition(pLoc.getX(),pLoc.getY(),pLoc.getZ());
                LocationalAudioChannel channel = api.createLocationalAudioChannel(channelId, level, position);
                
                if(channel != null){
                    channel.setDistance(8);
                    //api.getPlayersInRange(level,position,8);
    
                    Tiboise.logAdmin(channel.getLocation().getY());
                    try(AudioInputStream in = AudioSystem.getAudioInputStream(outputFile)){
                        byte[] result = in.readAllBytes();
                        
                        OpusDecoder decoder = api.createDecoder();
                        OpusEncoder encoder = api.createEncoder();
                        short[] sound = new short[4098];
                        try{
                            javax.sound.sampled.AudioFormat aformat = in.getFormat();
                            DataLine.Info info = new DataLine.Info(Clip.class,aformat);
    
                            final AudioPlayer audioPlayer = api.createAudioPlayer(channel,encoder,ByteBuffer.wrap(in.readAllBytes()).asShortBuffer().array());
                            audioPlayer.startPlaying();
    
    
                            Tiboise.logAdmin("start");
                            new BukkitRunnable(){
                                @Override
                                public void run() {
                                    audioPlayer.stopPlaying();
                                    decoder.close();
                                    encoder.close();
                                    Tiboise.logAdmin("stop");
                                }
                            }.runTaskLater(Tiboise.getPlugin(),80);
                            
                        }catch (RuntimeException e){
                            e.printStackTrace();
                        }
                        
                    } catch (
                            UnsupportedAudioFileException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
    
            
    
            return true;
        }
        return false;
    }
}
