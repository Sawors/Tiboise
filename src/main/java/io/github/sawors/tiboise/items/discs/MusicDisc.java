package io.github.sawors.tiboise.items.discs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.TiboiseUtils;
import io.github.sawors.tiboise.core.LocalResourcesManager;
import io.github.sawors.tiboise.items.ItemTag;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class MusicDisc extends TiboiseItem implements Listener {
    
    // music indexed according to id:prebuiltTitle
    private static Map<String,String> indexedMusics = new HashMap<>();
    
    //TODO : proper lookup and registering in an index file in the resource pack
    private final static Map<String,String> hashLookup = Map.of(
            "2137740449","benoit - tourne toi",
            "1571894733","led zeppelin - stairway to heaven",
            "1186386219","yes - roundabout",
            "1070507883","magoyond - le pudding à l'arsenic",
            "468158623","captainsparklez - revenge",
            "-385336390","captainsparklez - fallen kingdom",
            "135761661","elo - mr. blue sky"
    );
    
    String music;
    String author;
    NamespacedKey musicKey;
    
    public MusicDisc(String musicName){
        this("unknown",musicName);
        
    }
    
    public static String lookupMusicName(String hash){
        return indexedMusics.get(hash);
    }
    
    public MusicDisc(String author, String musicName){
        this.music = musicName.toLowerCase(Locale.ROOT);
        this.author = author.toLowerCase(Locale.ROOT);
        this.musicKey = new NamespacedKey(NamespacedKey.MINECRAFT,getKey());
        setLore(List.of(
                Component.text(getTitle()).color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
        ));
        addData(getMusicDataKey(),musicKey.asString());
        addData(getAuthorKey(),this.author);
        addData(getNameKey(),this.music);
        
        setDisplayName(Component.text("Music Disc").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        
        addTag(ItemTag.PREVENT_USE_IN_CRAFTING);
        setMaterial(Material.MUSIC_DISC_11);
    }
    
    public MusicDisc(){
        this("cat");
    }
    
    /**
     *
     * @param youtubeUrl the url of the YouTube video
     */
    public static void buildFromSource(@NotNull URL youtubeUrl, Consumer<MusicDisc> whenFinished){
        String videoId = parseVideoId(youtubeUrl);
        
        if(Tiboise.getYtdlpInstallation() == null){
            Bukkit.getLogger().log(Level.WARNING,"yt-dlp path not specified, disc creation cannot be done");
        }
        if(Tiboise.getFFmpegInstallation() == null){
            Bukkit.getLogger().log(Level.WARNING,"ffmpeg path not specified, disc creation cannot be done");
        }
        
        if(videoId != null){
            
            Thread dataBuilder = new Thread(() -> {
                try{
                    logAdmin("start");
                    // download and convert video
                    ProcessBuilder getMetadata = new ProcessBuilder();
                    File metadataDump = new File(LocalResourcesManager.getMusicStorageDirectory().getPath()+File.separator+videoId);
                    metadataDump.mkdirs();
                    getMetadata.directory(metadataDump);
                    getMetadata.command (
                            Tiboise.getYtdlpInstallation()+File.separator+"yt-dlp", // also works with "youtube-dl"
                            "--skip-download",
                            "--no-progress",
                            "--output",videoId,
                            "--write-info-json",
                            "https://www.youtube.com/watch?v="+videoId
                    );
                    File infoJson = new File(metadataDump.getPath()+File.separator+videoId+".info.json");
                    Process metaGetter = getMetadata.start();
                    // timeout the thread after 3 minutes if there is no answer.
                    metaGetter.waitFor(10, TimeUnit.SECONDS);
                    if(metaGetter.exitValue() != 0) {
                        logAdmin("metadata query failed");
                        return;
                    }
                    String jsonMeta = Files.readString(infoJson.toPath(), StandardCharsets.UTF_8);
                    JsonElement data = JsonParser.parseString(jsonMeta);
                    
                    String title = "Unknown";
                    String author = "Unknown";
                    
                    if(data.isJsonObject() && data instanceof JsonObject obj){
                        author = obj.get("channel").toString().replace("\"","");
                        title = cleanupVideoTitle(obj.get("title").toString().replace("\"",""),author);
                    }
                    FileUtils.deleteDirectory(metadataDump);
                    
                    MusicDisc disc = new MusicDisc(author,title);
                    int discId = disc.getTitleHash();
                    
                    File musicStorageDirectory = new File(LocalResourcesManager.getMusicStorageDirectory().getPath()+File.separator+discId);
                    
                    if(musicStorageDirectory.exists()){
                        try{
                            FileUtils.deleteDirectory(musicStorageDirectory);
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    
                    musicStorageDirectory.mkdir();
                    
                    ProcessBuilder getAudio = new ProcessBuilder();
                    getAudio.directory(musicStorageDirectory);
                    // using https://github.com/yt-dlp/yt-dlp
                    getAudio.command (
                            Tiboise.getYtdlpInstallation()+File.separator+"yt-dlp", // also works with "youtube-dl"
                            "--extract-audio",
                            "--audio-format", "vorbis",
                            "--ffmpeg-location",Tiboise.getFFmpegInstallation().getPath(),
                            "--audio-quality", "5",
                            "--output", String.valueOf(disc.getTitleHash()),
                            "--embed-metadata",
                            "--no-progress",
                            "https://www.youtube.com/watch?v="+videoId
                    
                    );
                    //-af pan=1c|c0=0.5*c0+0.5*c1
                    getAudio.redirectOutput(new File("C:/Users/sosol/IdeaProjects/Tiboise/_server/server/plugins/Tiboise/resources/musics/-145730963/out.txt"));
                    getAudio.redirectError(new File("C:/Users/sosol/IdeaProjects/Tiboise/_server/server/plugins/Tiboise/resources/musics/-145730963/err.txt"));
                    Process p = getAudio.start();
                    // timeout the thread after 3 minutes if there is no answer.
                    p.waitFor(120, TimeUnit.SECONDS);
                    if(p.exitValue() != 0) {
                        logAdmin("download failed");
                        return;
                    }
                    logAdmin("download successful !");
                    
                    //ffmpeg mixing to mono
                    ProcessBuilder mixDown = new ProcessBuilder();
                    mixDown.directory(musicStorageDirectory);
                    mixDown.command (
                            Tiboise.getFFmpegInstallation()+File.separator+"ffmpeg",// also works with "youtube-dl"
                            "-y",
                            "-i",discId+".ogg",
                            "-af", "pan=mono|c0=0.5*FL+0.5*FR",
                            "sound.ogg"
                    );
                    //-af pan=1c|c0=0.5*c0+0.5*c1
                    Process ffmpegP = mixDown.start();
                    // timeout the thread after 3 minutes if there is no answer.
                    ffmpegP.waitFor(60, TimeUnit.SECONDS);
                    if(ffmpegP.exitValue() != 0) {
                        logAdmin("mixing failed");
                        return;
                    }
                    logAdmin("mixing successful !");
                    
                    // delete the downloaded stereo version
                    Files.delete(Path.of(musicStorageDirectory.getPath(),discId+".ogg"));
                    
                    // creating a file which name is the title to improve readability
                    new File(musicStorageDirectory.getPath()+File.separator+disc.getTitle()).createNewFile();
                    
                    String scanCit = "";
                    String scanModel = "{}";
                    try(
                            InputStream citIn = Tiboise.getPlugin().getResource("discs/cit.properties");
                            InputStream modelIn = Tiboise.getPlugin().getResource("discs/model.json");
                            Scanner scannerCit = new Scanner(Objects.requireNonNull(citIn));
                            Scanner scannerModel = new Scanner(Objects.requireNonNull(modelIn));
                    ){
                        scanCit = scannerCit.useDelimiter("\\A").next();
                        scanModel = scannerModel.useDelimiter("\\A").next();
                    } catch (IOException | NullPointerException e){
                        e.printStackTrace();
                    }
                    final String citContent = scanCit;
                    final String modelContent = scanModel;
                    
                    
                    createDiscThumbnail(videoId,new File(musicStorageDirectory.getPath()+File.separator+discId+".png"));
                    File cit = new File(musicStorageDirectory.getPath()+File.separator+discId+".properties");
                    File model = new File(musicStorageDirectory.getPath()+File.separator+discId+".json");
                    try{
                        cit.createNewFile();
                        model.createNewFile();

                        Files.writeString(cit.toPath(),citContent.replaceAll("DISC_ID",String.valueOf(discId)),StandardCharsets.UTF_8);
                        Files.writeString(model.toPath(),modelContent.replaceAll("DISC_ID",String.valueOf(discId)),StandardCharsets.UTF_8);
                    } catch (IOException e){
                        e.printStackTrace();
                    }

                    // writing disc id to index
                    try {
                        YamlConfiguration index = YamlConfiguration.loadConfiguration(LocalResourcesManager.getMusicIndexFile());
                        ConfigurationSection section = index.createSection(String.valueOf(discId));
                        section.set(MusicIndexField.AUTHOR.toString(),disc.author);
                        section.set(MusicIndexField.NAME.toString(),disc.music);
                        section.set(MusicIndexField.PREBUILT_TITLE.toString(),disc.getTitle());
                        index.save(LocalResourcesManager.getMusicIndexFile());
                        // load disc index
                        indexedMusics.put(String.valueOf(discId),disc.getTitle());
                        
                        logAdmin("DONE !!!");
                        whenFinished.accept(disc);
                        
                    } catch (IOException | IllegalArgumentException e){
                        e.printStackTrace();
                    }
                } catch (
                        IOException |
                        InterruptedException e){
                    e.printStackTrace();
                }
            });
            
            
            dataBuilder.start();
        }
    }
    
    /**
     *
     * @return An immutable map representing the indexed musics as id:musicTitle
     */
    public static Map<String, String> getIndexedMusics() {
        return Map.copyOf(indexedMusics);
    }
    
    @Override
    public ItemStack get() {
        ItemStack s = super.get();
        
        s.addItemFlags(ItemFlag.HIDE_ITEM_SPECIFICS);
        
        return s;
    }
    
    static NamespacedKey getMusicDataKey() {
        return new NamespacedKey(Tiboise.getPlugin(),"music-disc-data");
    }
    static NamespacedKey getAuthorKey() {
        return new NamespacedKey(Tiboise.getPlugin(),"music-disc-author");
    }
    static NamespacedKey getNameKey() {
        return new NamespacedKey(Tiboise.getPlugin(),"music-disc-name");
    }
    
    String getTitle() {
        return TiboiseUtils.capitalizeFirstLetter((author+" - "+music).toLowerCase(Locale.ROOT));
    }
    
    public enum MusicIndexField {
        AUTHOR, NAME, PREBUILT_TITLE;
        
        
        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.ROOT);
        }
    }
    
    public static Pair<String,String> parseTitleString(String title){
        String editableTitle = title;
        String author;
        String name;
        int separator = editableTitle.indexOf("-");
        
        if(separator > -1 && separator+1<=editableTitle.length()) {
            if(editableTitle.charAt(separator-1) == ' ') editableTitle = editableTitle.replaceFirst(" -","-");
            if(editableTitle.charAt(editableTitle.indexOf("-")+1) == ' ') editableTitle = editableTitle.replaceFirst("- ","-");
            separator = editableTitle.indexOf("-");
            author = editableTitle.substring(0,separator).toLowerCase(Locale.ROOT);
            name = editableTitle.substring(separator+1).toLowerCase(Locale.ROOT);
            
            return Pair.of(author,name);
        } else {
            return Pair.of("unknown","unknown");
        }
    }
    
    public String getKey(){
        return "tiboise.music_disc."+getTitleHash();
    }
    
    public int getTitleHash(){
        return getTitle().toLowerCase(Locale.ROOT).hashCode();
    }
    
    
    @EventHandler
    public void loadIndexOnServerStart(PluginEnableEvent event){
        if(event.getPlugin().equals(Tiboise.getPlugin())){
            loadMusicIndex();
        }
    }
    
    protected static void loadMusicIndex(){
        try{
            YamlConfiguration content = YamlConfiguration.loadConfiguration(LocalResourcesManager.getMusicIndexFile());
            for(String key : content.getKeys(false)){
                ConfigurationSection section = content.getConfigurationSection(key);
                if(section != null){
                    String title = section.getString(MusicIndexField.PREBUILT_TITLE.toString());
                    indexedMusics.put(key,title);
                }
            }
        } catch (IllegalArgumentException e){
            Bukkit.getLogger().log(Level.INFO,"[Tiboise] Music index could not be loaded");
        }
    }
    
    static @Nullable String parseVideoId(@NotNull URL source){
        String parsable = source.toString();
        if(parsable.contains("youtube") && parsable.contains("v=")){
            // https://www.youtube.com/watch?t=143&v=OkpiwHo0Po0&feature=youtu.be
            StringBuilder idBuilder = new StringBuilder();
            for(char c : parsable.substring(parsable.indexOf("v=")+2).toCharArray()){
                if(c == '&') break;
                idBuilder.append(c);
            }
            return idBuilder.toString();
        } else if(parsable.contains("youtu.be/")){
            return parsable.substring(parsable.indexOf(".be/")+4,parsable.contains("?") ? parsable.indexOf("?") : parsable.length());
        }
        
        return null;
    }
    
    static String cleanupVideoTitle(String title, String channelName){
        String lowTitle = title.toLowerCase(Locale.ROOT);
        String lowChannel = channelName.toLowerCase(Locale.ROOT);
        lowTitle = lowTitle
                .replace(lowChannel,"")
                .replace("-","")
                .replaceAll("\\(.*\\)","")
                .replace("|","")
                .replace("  "," ");
        if(lowTitle.endsWith(" ")) lowTitle = lowTitle.substring(0,lowTitle.length()-1);
        
        
        return TiboiseUtils.capitalizeFirstLetter(lowTitle);
    }
    
    private static void createDiscThumbnail(String videoId, File output){
        String url = "http://img.youtube.com/vi/"+videoId+"/0.jpg";
        try{
            URL target = new URL(url);
            try(InputStream in = target.openStream(); FileOutputStream out = new FileOutputStream(output)){
                //out.write(in.readAllBytes());
                BufferedImage image = ImageIO.read(in);
                
                // by default YouTube thumbnails are in 4/3 ratio, so we are cutting only the center square according to this ratio.
                int width = image.getWidth();
                int height = image.getHeight();
                // 105 : 45 for (480x360)
                int startX = (int) ((105.0/480)*width);
                int startY = (int) ((45.0/360)*height);
                // 374 : 314 for (480x360)
                int croppedWidth = (int) (269*(width/480.0));
                int croppedHeight = (int) (269*(height/360.0));
                
                // image is now reduced to the center square of the thumbnail
                image = image.getSubimage(startX,startY,croppedWidth,croppedHeight);
                
                int subdivisions = 4;
                int subSquareSide = croppedHeight/subdivisions;
                // sample every N pixel
                int sampleRate = 16;
                
                int subSquareAmount = subdivisions*subdivisions;
                // concerning the arrays, the sub-squares are determined as follows :
                //
                //  [0][0][2][3]
                //  [4][5][6][7]
                //  [8][9]...
                
                long[][] totalR = new long[subSquareAmount][subSquareAmount];
                long[][] totalG = new long[subSquareAmount][subSquareAmount];
                long[][] totalB = new long[subSquareAmount][subSquareAmount];
                Color[][] averageColors = new Color[subSquareAmount][subSquareAmount];
                int sampleAmount = 0;
                for(int x = 0; x < subSquareSide; x++){
                    for(int y = 0; y < subSquareSide; y++){
                        if(x % sampleRate == 0 && y % sampleRate == 0){
                            // doing the operation for every sub square
                            for(int offsetX = 0; offsetX < subdivisions; offsetX++){
                                for(int offsetY = 0; offsetY < subdivisions; offsetY++){
                                    Color pixel = new Color(image.getRGB((offsetX*subSquareSide)+x,(offsetY*subSquareSide)+y));
                                    totalR[offsetX][offsetY] += pixel.getRed();
                                    totalG[offsetX][offsetY] += pixel.getGreen();
                                    totalB[offsetX][offsetY] += pixel.getBlue();
                                    
                                    image.setRGB((offsetX*subSquareSide)+x,(offsetY*subSquareSide)+y,0x000000);
                                    
                                }
                            }
                            
                            sampleAmount++;
                        }
                    }
                }
                for(int ix = 0; ix<subdivisions; ix++){
                    for(int iy = 0; iy < subdivisions; iy++){
                        
                        // Excluded :
                        // corners
                        // center
                        
                        // 30
                        // 21
                        // 12
                        // 03
                        if (ix!=iy && ix+iy != 3){
                            long r = totalR[ix][iy]/sampleAmount;
                            long g = totalG[ix][iy]/sampleAmount;
                            long b = totalB[ix][iy]/sampleAmount;
                            averageColors[ix][iy] = new Color((int) r,(int) g,(int) b);
                        } else {
                            averageColors[ix][iy] = new Color(0x000000);
                        }
                        
                    }
                }
                
                BufferedImage smol = new BufferedImage(subdivisions,subdivisions,BufferedImage.TYPE_INT_RGB);
                
                
                // paint the colors
                for(int x = 0; x<subdivisions; x++){
                    for(int y = 0; y<subdivisions; y++){
                        
                        Color c = averageColors[x][y];
                        // making the colors more saturated and bright to improve readability
                        int rgb = c.getRGB();
                        if(rgb != Color.BLACK.getRGB()){
                            float[] hsb = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),new float[3]);
                            hsb[1] = Math.min(hsb[1]+.25f,1);
                            hsb[2] = Math.min(hsb[2]+.15f,1);
                            rgb = Color.HSBtoRGB(hsb[0],hsb[1],hsb[2]);
                        }
                        
                        smol.setRGB(x,y,rgb);
                    }
                }
                
                ImageIO.write(smol,"png",out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
