package io.github.sawors.tiboise.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.UUID;

public class PostTransitPackage {
    
    ItemStack[] content;
    PostLetterBox destination;
    LocalDateTime timeStamp;
    UUID packageId;
    double distance;
    
    protected PostTransitPackage(UUID packageId, ItemStack[] content, PostLetterBox destination, LocalDateTime time, double distance) {
        this.content = content;
        this.destination = destination;
        this.timeStamp = time;
        this.distance = distance;
        this.packageId = packageId;
    }
    
    protected static @Nullable PostTransitPackage deserialize(String serialized){
        ObjectMapper mapper = new ObjectMapper();
        try{
            return mapper.readValue(serialized, PostTransitPackage.class);
        } catch (JsonProcessingException e){
            return null;
        }
    }
    
    protected void save(){
        try{
            final String ser = this.serialize();
            if(ser == null) return;
            File targetDirectory = getPackageSaveDirectory(destination.getSign().getWorld());
            targetDirectory.mkdirs();
            File saveFile = new File(targetDirectory.getPath()+File.separator+packageId);
            saveFile.createNewFile();
            try(Writer out = new FileWriter(saveFile)){
                out.write(ser);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    
    protected @Nullable String serialize(){
        try{
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e){
            return null;
        }
    }
    
    public static File getPackageSaveDirectory(World world){
        return new File(world.getWorldFolder().getPath()+File.separator+"letters");
    }
}
