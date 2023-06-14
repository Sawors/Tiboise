package io.github.sawors.tiboise.core.commands;

import io.github.sawors.tiboise.economy.CoinItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class TTestCommand implements CommandExecutor {
    
    
    
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if(args.length >= 1){
            long amount = Long.parseLong(args[0]);
            CoinItem.splitValue(amount);
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
