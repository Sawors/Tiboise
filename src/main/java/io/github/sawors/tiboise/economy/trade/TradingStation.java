package io.github.sawors.tiboise.economy.trade;

import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.TiboiseUtils;
import io.github.sawors.tiboise.UtilityBlock;
import io.github.sawors.tiboise.items.ItemSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TradingStation implements Listener, UtilityBlock {
    
    private static final Set<Material> allowedContainer = Set.of(
            Material.CHEST,
            Material.BARREL
    );
    
    private static final String tradingStationIdentifier = "- Shop -";
    
    private UUID owner;
    private TradingStationOptions options;
    private int storedCoins;
    private ItemStack soldItem;
    private int price;
    
    public TradingStation(){
        this.options = new TradingStationOptions();
        this.storedCoins = 0;
        this.soldItem = new ItemStack(Material.AIR);
        this.price = 0;
        this.owner = null;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void recogniseTradingStation(SignChangeEvent event){
        final Block b = event.getBlock();
        final Player p = event.getPlayer();
        if(!event.isCancelled() && b.getState() instanceof Sign sign && b.getBlockData() instanceof Directional wallSign){
            final String ownerId = sign.getPersistentDataContainer().get(getOwnerKey(), PersistentDataType.STRING);
            // prevents other people from editing the sign (but not destroying it however)
            if(!event.getPlayer().isOp() && ownerId != null && !ownerId.equals(p.getUniqueId().toString())) {
                p.sendActionBar(Component.text("You are not the owner of this trading station").color(NamedTextColor.RED));
                event.setCancelled(true);
                return;
            }
            if(event.lines().size() >= 1){
                final Component identifier = event.line(0);
                if(((TextComponent) Objects.requireNonNull(identifier)).content().equals(tradingStationIdentifier)){
                    // sign is recognised as a post sign
                    final Block relative = b.getRelative(wallSign.getFacing().getOppositeFace());
                    if(allowedContainer.contains(relative.getType()) && sign.lines().size() >= 1 && event.line(1) != null && TiboiseUtils.extractContent(sign.line(0)).equals(tradingStationIdentifier)){
                    
                    }
                }
            }
        }
    }
    
    public static boolean checkShopValidity(Block shopContainer){
        return allowedContainer.contains(shopContainer.getType())
                && shopContainer.getState() instanceof PersistentDataHolder holder
                && Objects.equals(holder.getPersistentDataContainer().get(utilityBlockKey, PersistentDataType.STRING), new TradingStation().getUtilityIdentifier());
    }
    
    @EventHandler
    public static void preventNotOwnerDestroying(BlockBreakEvent event){
        if(event.getBlock().getState() instanceof PersistentDataHolder holder){
            final String ownerId = holder.getPersistentDataContainer().get(getOwnerKey(), PersistentDataType.STRING);
            // prevents other people from editing the sign (but not destroying it however)
            if(!event.getPlayer().isOp() && ownerId != null && !ownerId.equals(event.getPlayer().getUniqueId().toString())) {
                event.getPlayer().sendActionBar(Component.text("You are not the owner of this trading station").color(NamedTextColor.RED));
                event.setCancelled(true);
            }
        }
    }
    
    @Override
    public String getUtilityIdentifier() {
        return "trading_station";
    }
    
    public static TradingStation fromBlock(Block block){
        Block container = null;
        if(block.getState() instanceof Sign sign && block.getBlockData() instanceof WallSign wallSign){
            if(checkShopValidity(block.getRelative(wallSign.getFacing().getOppositeFace()))){
                // from sign
                container = block.getRelative(wallSign.getFacing().getOppositeFace());
            }
        } else if(
                allowedContainer.contains(block.getType())
                        && block.getState() instanceof PersistentDataHolder holder
                        && Objects.equals(holder.getPersistentDataContainer().get(utilityBlockKey,PersistentDataType.STRING),new TradingStation().getUtilityIdentifier())
        ) {
            container = block;
        }
        
        if(container instanceof PersistentDataHolder holder){
            PersistentDataContainer persistent = holder.getPersistentDataContainer();
            TradingStation tradingStation = new TradingStation();
            // owner
            String ownerIdString = persistent.get(getOwnerKey(),PersistentDataType.STRING);
            if(ownerIdString != null){
                try{
                    UUID ownerId = UUID.fromString(ownerIdString);
                    tradingStation.setOwner(ownerId);
                } catch (IllegalArgumentException ignored){}
            }
            // options
            String serializedOption = persistent.get(getOptionsKey(),PersistentDataType.STRING);
            if(serializedOption != null){
                TradingStationOptions options = TradingStationOptions.deserialize(serializedOption);
                if(options != null){
                    tradingStation.setOptions(options);
                }
            }
            // item sold
            String itemSoldSerialized = persistent.getOrDefault(getItemSoldKey(),PersistentDataType.STRING,"");
            ItemSerializer serializer = new ItemSerializer();
            ItemStack sold = serializer.deserializeSingleItem(itemSoldSerialized);
            if(sold != null){
                tradingStation.setSoldItem(sold);
            }
            // price
            Integer itemPrice = persistent.get(getItemPriceKey(),PersistentDataType.INTEGER);
            if(itemPrice != null){
                tradingStation.setPrice(itemPrice);
            }
            // stored coins
            Integer coins = persistent.get(getStoredCoinsKey(),PersistentDataType.INTEGER);
            if(coins != null){
                tradingStation.setStoredCoins(coins);
            }
            
            return tradingStation;
        }
        return null;
    }
    
    static NamespacedKey getOptionsKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"trading-station-options");
    }
    static NamespacedKey getItemSoldKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"trading-station-item-sold");
    }
    static NamespacedKey getStoredCoinsKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"trading-station-coins");
    }
    static NamespacedKey getItemPriceKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"trading-station-price");
    }
    protected static NamespacedKey getOwnerKey(){
        return new NamespacedKey(Tiboise.getPlugin(),"trading-station-owner");
    }
    
    public static Set<Material> getAllowedContainer() {
        return allowedContainer;
    }
    
    public TradingStationOptions getOptions() {
        return options;
    }
    
    public void setOptions(TradingStationOptions options) {
        this.options = options;
    }
    
    public int getStoredCoins() {
        return storedCoins;
    }
    
    protected void setStoredCoins(int storedCoins) {
        this.storedCoins = storedCoins;
    }
    
    public ItemStack getSoldItem() {
        return soldItem;
    }
    
    protected void setSoldItem(ItemStack soldItem) {
        this.soldItem = soldItem;
    }
    
    public int getPrice() {
        return price;
    }
    
    protected void setPrice(int price) {
        this.price = price;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    protected void setOwner(UUID owner) {
        this.owner = owner;
    }
}
