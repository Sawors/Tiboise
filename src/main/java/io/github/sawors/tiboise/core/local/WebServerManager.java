package io.github.sawors.tiboise.core.local;

import com.google.common.net.HttpHeaders;
import com.sun.net.httpserver.HttpServer;
import io.github.sawors.tiboise.Tiboise;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.net.InetSocketAddress;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class WebServerManager implements Listener {
    
    // webserver
    private static final String resourcePackContext = "/download/"+ResourcePackManager.getPackFileName();
    private static final String resourcePackHashContext = "/download/"+ResourcePackManager.getHashFileName();
    private static final String modPackFileName = "TiboiseProfile.zip";
    private static final String modPackContext = "/download/"+modPackFileName;
    private static File modpackFile;
    
    protected static void initialize(){
        
        File resourcePackBundled = new File(LocalResourcesManager.getWebServerDirectory().getPath()+File.separator+ResourcePackManager.getPackFileName());
        File resourcePackBundledHash = new File(LocalResourcesManager.getWebServerDirectory().getPath()+File.separator+ResourcePackManager.getHashFileName());
        
        modpackFile = new File(LocalResourcesManager.getWebServerDirectory().getPath()+File.separator+modPackFileName);
        
        if(resourcePackBundled.exists()){
            new BukkitRunnable(){
                @Override
                public void run() {
                    HttpServer server;
                    try {
                        server = HttpServer.create(new InetSocketAddress(LocalResourcesManager.getWebServerPort()),8);
                    } catch (
                            IOException e) {
                        throw new RuntimeException(e);
                    }
                    // context for downloading the resource pack
                    server.createContext(resourcePackContext, exchange -> {
                        try(OutputStream out = exchange.getResponseBody(); InputStream in = new FileInputStream(resourcePackBundled)) {
                            exchange.sendResponseHeaders(200, resourcePackBundled.length());
                            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename="+ResourcePackManager.getPackFileName());
                            exchange.setAttribute(HttpHeaders.CONTENT_TYPE, "application/zip");
                            out.write(in.readAllBytes());
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
                    // context for downloading the sha1 text file
                    server.createContext(resourcePackHashContext, exchange -> {
                        try(OutputStream out = exchange.getResponseBody(); InputStream in = new FileInputStream(resourcePackBundledHash)) {
                            exchange.sendResponseHeaders(200, resourcePackBundledHash.length());
                            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename="+ResourcePackManager.getHashFileName());
                            exchange.setAttribute(HttpHeaders.CONTENT_TYPE, "text/plain");
                            out.write(in.readAllBytes());
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
                    if(modpackFile != null && modpackFile.exists()){
                        server.createContext(modPackContext, exchange -> {
                            try(OutputStream out = exchange.getResponseBody(); InputStream in = new FileInputStream(modpackFile)) {
                                exchange.sendResponseHeaders(200, modpackFile.length());
                                exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename="+modPackFileName);
                                exchange.setAttribute(HttpHeaders.CONTENT_TYPE, "application/zip");
                                out.write(in.readAllBytes());
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        });
                    }
                    server.start();
                    logAdmin("Webserver started on port : "+LocalResourcesManager.getWebServerPort());
                }
            }.runTaskAsynchronously(Tiboise.getPlugin());
        }
    }
    
    public static String getPackSource(){
        //return LocalResourcesManager.getWebServerSrc()+":"+LocalResourcesManager.getWebServerPort()+resourcePackContext;
        return "http://mc.sawors.com:8123"+resourcePackContext;
    }
    
    public static String getPackHashSource(){
        //return LocalResourcesManager.getWebServerSrc()+":"+LocalResourcesManager.getWebServerPort()+resourcePackHashContext;
        return "http://mc.sawors.com:8123"+resourcePackHashContext;
    }
}
