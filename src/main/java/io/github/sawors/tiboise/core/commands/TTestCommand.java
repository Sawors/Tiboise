package io.github.sawors.tiboise.core.commands;

import io.github.gaeqs.javayoutubedownloader.JavaYoutubeDownloader;
import io.github.gaeqs.javayoutubedownloader.decoder.MultipleDecoderMethod;
import io.github.gaeqs.javayoutubedownloader.stream.StreamOption;
import io.github.gaeqs.javayoutubedownloader.stream.YoutubeVideo;
import io.github.gaeqs.javayoutubedownloader.stream.download.StreamDownloader;
import io.github.gaeqs.javayoutubedownloader.stream.download.StreamDownloaderNotifier;
import io.github.gaeqs.javayoutubedownloader.tag.AudioQuality;
import io.github.gaeqs.javayoutubedownloader.tag.Encoding;
import io.github.gaeqs.javayoutubedownloader.tag.StreamType;
import io.github.sawors.tiboise.Tiboise;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegOutputBuilder;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Locale;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class TTestCommand implements CommandExecutor {
    
    private static final AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000F, 16, 1, 2, 48000F, false);
    
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        
        //final VoicechatServerApi api = VoiceChatIntegrationPlugin.getVoicechatServerApi();
        
        ////logAdmin(shorts.length);
        
        //playLocationalAudio(api,src.toPath(),((Player) commandSender).getLocation().getBlock());
        
        if(true){
            //Extracts and decodes all streams.
            YoutubeVideo video = JavaYoutubeDownloader.decodeOrNull("https://youtu.be/tzx8gCkIKkU", MultipleDecoderMethod.OR, "html", "embedded");
            //Gets the option with the greatest quality that has video and audio.
            final StreamOption option = video.getStreamOptions().stream()
                    .filter(target -> target.getType().hasAudio() && !target.getType().hasVideo() && target.getType().getAudioEncoding().equals(Encoding.AAC) )
                    .min(Comparator.comparingInt(o -> o.getType().getAudioQuality().ordinal())).orElse(null);
            if(option == null) return false;
            for(StreamOption op : video.getStreamOptions()){
                logAdmin("op",op.getType().getAudioEncoding());
            }
            logAdmin("option",option.getType().toString());
            final StreamType type = option.getType();
            final Encoding encoding = type.getAudioEncoding();
            // BE CAREFUL the quality are ranked from highest to lowest
            final AudioQuality quality = type.getAudioQuality();
            final String title = video.getTitle();
            logAdmin(encoding.name());
            logAdmin(quality.name());
            
            
            //Prints the option type.
            System.out.println(option.getType());
            //Creates the file. folder/title.extension
            File file = new File(Tiboise.getPlugin().getDataFolder()+File.separator+title.substring(title.indexOf("/")+1).replaceAll(" ","_")+"."+type.getContainer().name().toLowerCase(Locale.ROOT));
            try{
                logAdmin(file.getCanonicalPath());
                if(file.exists()) file.delete();
                //file.createNewFile();
            } catch (IOException ignored){}
            //Creates the downloader.
            
            StreamDownloader downloader = new StreamDownloader(option, file, new StreamDownloaderNotifier() {
                
                int lastPercent = 0;
                
                @Override
                public void onStart(StreamDownloader downloader) {
                    logAdmin("strt");
                }
                
                @Override
                public void onDownload(StreamDownloader downloader) {
                    final int percent = 100*downloader.getCount()/downloader.getLength();
                    if(percent > lastPercent){
                        lastPercent = percent;
                        logAdmin(percent);
                    }
                }
                
                @Override
                public void onFinish(StreamDownloader downloader) {
                    if(false){
                        return;
                    }
                    logAdmin("starting");
                    File installation = Tiboise.getFFmpegInstallation();
                    logAdmin(installation);
                    if(installation != null){
                        logAdmin("starting ffmpeg conversion");
                        try {
                            FFmpeg fFmpeg = new FFmpeg(installation.getCanonicalPath()+File.separator+"ffmpeg.exe");
                            FFprobe fFprobe = new FFprobe(installation.getCanonicalPath()+File.separator+"ffprobe.exe");
                            FFmpegOutputBuilder builder = new FFmpegBuilder()
                                    .setInput(fFprobe.probe(file.getCanonicalPath()))
                                    .overrideOutputFiles(true)
                                    .addOutput(Tiboise.getPlugin().getDataFolder().getCanonicalPath()+File.separator+"out.ogg")
                                    .setFormat("ogg")
                                    .setAudioChannels(1)
                                    ;
                            
                            FFmpegExecutor executor = new FFmpegExecutor(fFmpeg,fFprobe);
                            
                            executor.createJob(builder.done(), new ProgressListener() {
                                @Override
                                public void progress(Progress progress) {
                                    logAdmin(progress.status);
                                }
                            }).run();
                            
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        logAdmin("ffmpeg installation not provided, abandonning the conversion");
                        file.delete();
                    }
                    //OpusDecoder decoder = api.createDecoder();
                    // Iterating over every player on the server
                    
                    /*logAdmin("bitrate",Integer.parseInt(quality.name().replaceAll("k",""))*1000);
                    try( FileInputStream in = new FileInputStream(file)){
                        ;
                        //logAdmin(Arrays.toString(file2.readAllBytes()));
                        logAdmin(file);
                        logAdmin(file.exists());

                        //OpusDecoder enc = api.createDecoder();
                        //logAdmin(enc.decode(sound.readAllBytes()));

                        //logAdmin(ot.size());

                        final byte[] src = Files.readAllBytes(file.toPath());
                        final byte[] bytes = src;
                        
                        
                        
                        logAdmin(bytes.length);
                        Files.copy(in, new File(file.getParentFile().getPath()+File.separator+"decoded.raw").toPath());
                        //logAdmin(Arrays.toString(bytes));
                        short[] shorts = new short[bytes.length/2];
                        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

                        

                        *//*OpusDecoder dc = api.createDecoder();
                        short[] decoded = dc.decode(bytes);*//*
                        logAdmin(src.length);
                        logAdmin(bytes.length);
                        logAdmin(shorts.length);
                        
                        
                    } catch (
                            IOException e) {
                        e.printStackTrace();
                    }*/
                }
                
                @Override
                public void onError(StreamDownloader downloader, Exception ex) {
                    ex.printStackTrace();
                }
            });
            //Runs the downloader.
            new Thread(downloader).start();
            
           
            
            
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
