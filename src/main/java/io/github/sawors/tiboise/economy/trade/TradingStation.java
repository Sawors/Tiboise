package io.github.sawors.tiboise.economy.trade;

import io.github.sawors.tiboise.OwnedBlock;
import io.github.sawors.tiboise.Tiboise;
import io.github.sawors.tiboise.TiboiseUtils;
import io.github.sawors.tiboise.UtilityBlock;
import io.github.sawors.tiboise.economy.CoinItem;
import io.github.sawors.tiboise.items.ItemSerializer;
import io.github.sawors.tiboise.items.TiboiseItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class TradingStation extends OwnedBlock implements Listener, UtilityBlock {
    
    private static final Set<Material> allowedContainer = Set.of(
            Material.CHEST,
            Material.BARREL
    );
    
    private static final Map<Location, TradingStation> cachedStations = new HashMap<>();
    
    private static final String tradingStationIdentifier = "- Shop -";
    
    private UUID owner;
    private TradingStationOptions options;
    private int storedCoins;
    private ItemStack soldItem;
    private int price;
    private Block container;
    private Block sign;
    
    public TradingStation(){
        this.options = new TradingStationOptions();
        this.storedCoins = 0;
        this.soldItem = new ItemStack(Material.AIR);
        this.price = 0;
        this.owner = null;
        this.container = null;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void recogniseTradingStation(SignChangeEvent event){
        final Block b = event.getBlock();
        final Player p = event.getPlayer();
        if(!event.isCancelled() && b.getState() instanceof Sign sign && b.getBlockData() instanceof Directional wallSign){
            if(event.lines().size() >= 1){
                final Component identifier = event.line(0);
                if(((TextComponent) Objects.requireNonNull(identifier)).content().equals(tradingStationIdentifier)){
                    // sign is recognised as a trading station sign
                    final Block relative = b.getRelative(wallSign.getFacing().getOppositeFace());
                    if(allowedContainer.contains(relative.getType()) && sign.lines().size() >= 1 && event.line(1) != null && TiboiseUtils.extractContent(sign.line(0)).equals(tradingStationIdentifier)){
                        TradingStation station = TradingStation.fromBlock(relative);
                        if(station != null){
                            station.save();
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void playerEditStation(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        Player p = event.getPlayer();
        if(
                event.getAction().isRightClick()
                && event.getHand() != null
                && event.getHand().equals(EquipmentSlot.HAND)
                && b != null
                && b.getState() instanceof Sign
                && b.getBlockData() instanceof WallSign wallSign
                && checkShopValidity(b.getRelative(wallSign.getFacing().getOppositeFace()))
        ){
            // block b is a trading station !
            TradingStation station = fromBlock(b.getRelative(wallSign.getFacing().getOppositeFace()));
            if(station != null){
                station.setSign(b);
                if(Objects.equals(event.getPlayer().getUniqueId(),station.getOwner())){
                    // player interacting is the owner
                    ItemStack itemInHand = p.getInventory().getItemInMainHand();
                    if(itemInHand.hasItemMeta() && itemInHand.getItemMeta().getPersistentDataContainer().has(CoinItem.getCoinValueKey())){
                        int valueModifier = Integer.parseInt(Objects.requireNonNullElse(itemInHand.getItemMeta().getPersistentDataContainer().get(CoinItem.getCoinValueKey(), PersistentDataType.STRING),"0"));
                        int mode = p.isSneaking() ? -1 : 1;
                        
                        station.setPrice(Math.min(Math.max(station.getPrice()+(valueModifier*mode),0),itemInHand.getType().getMaxStackSize()));
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,.5f,1);
                    } else {
                        station.setSoldItem(itemInHand);
                    }
                } else {
                    // player interacting is not the owner -> buying
                    int purseValue = CoinItem.evaluateInventoryValue(p.getInventory().getContents());
                    int soldPrice = station.getPrice();
                    ItemStack sold = station.getSoldItem();
                    int soldAmount = sold.getAmount();
                    
                    if(station.getContainer() != null && station.getContainer().getState() instanceof Container containerState){
                        int sellableAmount = 0;
                        boolean exactMatch = station.getOptions().isExactMatch();
                        for(ItemStack item : containerState.getSnapshotInventory().getContents()){
                            if(item != null){
                                if(exactMatch && item.asOne().equals(sold.asOne())){
                                    sellableAmount += item.getAmount();
                                } else if(TiboiseItem.getItemId(item).equals(TiboiseItem.getItemId(sold))){
                                    sellableAmount += item.getAmount();
                                }
                            }
                        }
                        
                        int totalPrice = 0;
                        int boughtAmount = 0;
                        if(p.isSneaking()){
                            for(int i = soldAmount; i <= sellableAmount && i<=sold.getMaxStackSize(); i+=soldAmount){
                                boughtAmount += soldAmount;
                                totalPrice += soldPrice;
                            }
                        } else {
                            boughtAmount = soldAmount;
                            totalPrice = soldPrice;
                        }
                        
                        if(purseValue < totalPrice){
                            p.sendActionBar(Component.text("You do not have enough coins to buy this").color(NamedTextColor.GOLD));
                        } else {
                            // all checks done, player can buy !
                            for(ItemStack overflow : p.getInventory().addItem(sold.asQuantity(boughtAmount)).values()){
                                p.getWorld().dropItem(p.getLocation(),overflow);
                            }
                            
                            // removing items from shop inventory
                            for(ItemStack i : containerState.getInventory()){
                            
                            }
                            // removing money from player's inventory
                            
                            p.sendActionBar(Component.text("You have bought "+TiboiseUtils.capitalizeFirstLetter(TiboiseItem.getItemId(soldItem).replace("_"," "))+" x"+boughtAmount).color(NamedTextColor.GOLD));
                        }
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
    
    @Override
    public String getUtilityIdentifier() {
        return "trading_station";
    }
    
    public static TradingStation fromBlock(Block block){
        Block container = null;
        if(block.getState() instanceof Sign && block.getBlockData() instanceof WallSign wallSign){
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
            String ownerIdString = persistent.get(ownerKey ,PersistentDataType.STRING);
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
            
            // container
            tradingStation.setContainer(container);
            
            return tradingStation;
        }
        return null;
    }
    
    public void save(){
        cachedStations.put(container.getLocation(),this);
        if(container.getState() instanceof PersistentDataHolder holder){
            PersistentDataContainer persistent = holder.getPersistentDataContainer();
            
            persistent.set(getOptionsKey(),PersistentDataType.STRING,getOptions().serialize());
            persistent.set(ownerKey,PersistentDataType.STRING,getOwner().toString());
            persistent.set(getItemSoldKey(),PersistentDataType.STRING,new ItemSerializer().serialize(getSoldItem(),0));
            persistent.set(getItemPriceKey(),PersistentDataType.INTEGER,getPrice());
            persistent.set(getStoredCoinsKey(),PersistentDataType.INTEGER, getStoredCoins());
        }
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
        if(sign != null && sign.getState() instanceof Sign state){
            String itemName = TiboiseUtils.capitalizeFirstLetter(TiboiseItem.getItemId(soldItem).replace("_"," "));
            if(itemName.length() > 11){
                itemName = itemName.substring(0,11-3)+"...";
            }
            state.line(2,Component.text(itemName+" "+soldItem.getAmount()+"x"));
        }
    }
    
    public int getPrice() {
        return price;
    }
    
    protected void setPrice(int price) {
        this.price = price;
        if(sign != null && sign.getState() instanceof Sign state){
            state.line(3,Component.text(price+"c"));
        }
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    protected void setOwner(UUID owner) {
        this.owner = owner;
    }
    
    public Block getContainer() {
        return container;
    }
    
    protected void setContainer(Block container) {
        this.container = container;
    }
    
    protected void setSign(Block sign){
        if(sign instanceof Sign){
            this.sign = sign;
        }
    }
}
