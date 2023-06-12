package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.items.discs.MusicDisc;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import java.net.MalformedURLException;
import java.net.URL;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class TTestCommand implements CommandExecutor {
    
    private static final AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000F, 16, 1, 2, 48000F, false);
    
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        
        //final VoicechatServerApi api = VoiceChatIntegrationPlugin.getVoicechatServerApi();
        
        ////logAdmin(shorts.length);
        
        //playLocationalAudio(api,src.toPath(),((Player) commandSender).getLocation().getBlock());
        
        if(true){
            if(args.length > 0){
                String video = args[0];
                if(video.length() > 1){
                    try{
                        MusicDisc.buildFromSource(new URL(video));
                    } catch (MalformedURLException e){
                        e.printStackTrace();
                    }
                }
                return true;
            } else {
                logAdmin(MusicDisc.getIndexedMusics());
            }
            
           
            
            
            /*// download in-memory to OutputStream
            OutputStream os = new ByteArrayOutputStream();
            RequestVideoStreamDownload request = new RequestVideoStreamDownload(format, os);
            Response<Void> response = downloader.downloadVideoStream(request);*/
            
            
            /*if(false){
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
            }*/
            
    
            
    
            return true;
        }
        
        
        
        
        
        
        
        
        
        
        
        
        return false;
    }
    
    
    
    
    
    
    
    
    
    
    
    /*private static short[] readSoundFile(Path file) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        return VoiceChatIntegrationPlugin.getVoicechatApi().getAudioConverter().bytesToShorts(convertFormat(file, FORMAT));
    }
    
    private static byte[] convertFormat(Path file, AudioFormat audioFormat) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        AudioInputStream finalInputStream = null;
        
        if (getFileExtension(file.toFile().toString()).equals("wav")) {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(file.toFile());
            finalInputStream = AudioSystem.getAudioInputStream(audioFormat, inputStream);
        } else if (getFileExtension(file.toFile().toString()).equals("mp3")) {
            
            AudioInputStream inputStream = new MpegAudioFileReader().getAudioInputStream(file.toFile());
            AudioFormat baseFormat = inputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getFrameRate(), false);
            AudioInputStream convertedInputStream = new MpegFormatConversionProvider().getAudioInputStream(decodedFormat, inputStream);
            finalInputStream = AudioSystem.getAudioInputStream(audioFormat, convertedInputStream);
            
        }
        
        assert finalInputStream != null;
        
        return adjustVolume(finalInputStream.readAllBytes(), 1);
    }
    
    private static byte[] adjustVolume(byte[] audioSamples, double volume) {
        
        byte[] array = new byte[audioSamples.length];
        for (int i = 0; i < array.length; i+=2) {
            // convert byte pair to int
            short buf1 = audioSamples[i+1];
            short buf2 = audioSamples[i];
            
            buf1 = (short) ((buf1 & 0xff) << 8);
            buf2 = (short) (buf2 & 0xff);
            
            short res= (short) (buf1 | buf2);
            res = (short) (res * volume);
            
            // convert back
            array[i] = (byte) res;
            array[i+1] = (byte) (res >> 8);
            
        }
        return array;
    }
    
    private static String getFileExtension(String s) {
        int index = s.lastIndexOf(".");
        if (index > 0) {
            return s.substring(index + 1);
        } else {
            return "";
        }
    }
    
    public void playLocationalAudio(VoicechatServerApi api, Path soundFilePath, Block block) {
        UUID id = UUID.nameUUIDFromBytes(block.getLocation().toString().getBytes());
        
        LocationalAudioChannel audioChannel = api.createLocationalAudioChannel(id, api.fromServerLevel(block.getWorld()), api.createPosition(block.getLocation().getX() + 0.5d, block.getLocation().getY() + 0.5d, block.getLocation().getZ() + 0.5d));
        
        if (audioChannel == null) return;
        
        audioChannel.setCategory("MUSIC");
        audioChannel.setDistance(32);
        
        
        Collection<ServerPlayer> playersInRange = api.getPlayersInRange(api.fromServerLevel(block.getWorld()), api.createPosition(block.getLocation().getX() + 0.5d, block.getLocation().getY() + 0.5d, block.getLocation().getZ() + 0.5d), 32);
        
        de.maxhenkel.voicechat.api.audiochannel.AudioPlayer audioPlayer = playChannel(api, audioChannel, block, soundFilePath, playersInRange);
        
    }
    
    private de.maxhenkel.voicechat.api.audiochannel.AudioPlayer playChannel(VoicechatServerApi api, AudioChannel audioChannel, Block block, Path soundFilePath, Collection<ServerPlayer> playersInRange) {
        try {
            short[] audio = readSoundFile(soundFilePath);
            AudioPlayer audioPlayer = api.createAudioPlayer(audioChannel, api.createEncoder(), audio);
            audioPlayer.startPlaying();
            logAdmin("playing");
            logAdmin(audioChannel.getCategory());
            return audioPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getLogger().info("Error Occurred At: " + block.getLocation());
            for (ServerPlayer serverPlayer : playersInRange) {
                Player bukkitPlayer = (Player) serverPlayer.getPlayer();
                bukkitPlayer.sendMessage(ChatColor.RED + "An error has occurred while trying to play this disc.");
            }
            return null;
        }
    }*/
    
}
