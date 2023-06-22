package io.github.sawors.tiboise.economy.trade;

import com.destroystokyo.paper.MaterialTags;
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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

import static io.github.sawors.tiboise.Tiboise.logAdmin;

public class TradingStation extends OwnedBlock implements Listener, UtilityBlock {
    
    private static final Set<Material> allowedContainer = Set.of(
            Material.CHEST,
            Material.BARREL
    );
    // TODO : add proper caching
    private static final Map<Location, TradingStation> cachedStations = new HashMap<>();
    
    private static final String tradingStationIdentifier = "- Shop -";
    private static final Map<InventoryView,TradingStation> openedCoinsStorage = new HashMap<>();
    
    private UUID owner;
    private TradingStationOptions options;
    private int storedCoins;
    private ItemStack soldItem;
    private int price;
    private Block container;
    private Block sign;
    
    protected TradingStation(){
    
    }
    
    public TradingStation(UUID owner, Block container){
        this.options = new TradingStationOptions();
        this.storedCoins = 0;
        this.soldItem = new ItemStack(Material.DIRT);
        this.price = 0;
        this.owner = null;
        this.container = null;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public static void recogniseTradingStation(SignChangeEvent event){
        
        final Block b = event.getBlock();
        final Player p = event.getPlayer();
        if(!event.isCancelled() && b.getState() instanceof Sign && b.getBlockData() instanceof Directional wallSign){
            logAdmin("good");
            if(event.lines().size() >= 1){
                logAdmin("lines");
                final Component identifier = event.line(0);
                if(((TextComponent) Objects.requireNonNull(identifier)).content().equals(tradingStationIdentifier)){
                    logAdmin("identifier");
                    // sign is recognised as a trading station sign
                    final Block relative = b.getRelative(wallSign.getFacing().getOppositeFace());
                    logAdmin(relative.getType());
                    if(allowedContainer.contains(relative.getType()) && event.lines().size() >= 1 && event.line(0) != null && TiboiseUtils.extractContent(Objects.requireNonNull(event.line(0))).equals(tradingStationIdentifier)){
                        logAdmin("cont");
                        TradingStation station = TradingStation.fromBlock(relative);
                        p.playSound(p.getLocation(),Sound.ENTITY_VILLAGER_WORK_WEAPONSMITH,1,1.25f);
                        if(station != null){
                            station.setOwner(p.getUniqueId());
                            station.setContainer(relative);
                            station.save();
                            logAdmin("save station");
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public static void refreshStoredCoins(InventoryCloseEvent event){
        HumanEntity p = event.getPlayer();
        Inventory i = event.getInventory();
        TradingStation station = openedCoinsStorage.get(event.getView());
        if(station != null){
            int value = CoinItem.evaluateInventoryValue(i.getContents());
            station.setStoredCoins(value);
            station.save();
            openedCoinsStorage.remove(event.getView());
        }
    }
    
    @EventHandler
    public static void preventPuttingAnythingInStoredCoins(InventoryClickEvent event){
        if(openedCoinsStorage.containsKey(event.getView()) && ((openedCoinsStorage.containsKey(event.getView()) && event.getClickedInventory() != null && event.getClickedInventory().equals(event.getView().getTopInventory()) && event.getAction().toString().contains("PLACE")) || event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY))){
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public static void playerEditStation(PlayerInteractEvent event){
        Block b = event.getClickedBlock();
        Player p = event.getPlayer();
        
        if(
                !event.useInteractedBlock().equals(Event.Result.DENY)
                && event.getAction().isRightClick()
                && event.getHand() != null
                && event.getHand().equals(EquipmentSlot.HAND)
                && b != null
                
        ){
            if(     b.getState() instanceof Sign
                    && b.getBlockData() instanceof WallSign wallSign
                    && checkShopValidity(b.getRelative(wallSign.getFacing().getOppositeFace()))
            ){
                // block b is a trading station !
                TradingStation station = fromBlock(b.getRelative(wallSign.getFacing().getOppositeFace()));
                logAdmin("station");
                if(station != null){
                    logAdmin("station V");
                    event.setCancelled(true);
                    event.setUseInteractedBlock(Event.Result.DENY);
                    station.setSign(b);
                    ItemStack itemInHand = p.getInventory().getItemInMainHand();
                    if(!itemInHand.getType().isAir() && Objects.equals(event.getPlayer().getUniqueId(),station.getOwner())){
                        logAdmin("owner");
                        // player interacting is the owner
                        if(itemInHand.hasItemMeta() && itemInHand.getItemMeta().getPersistentDataContainer().has(CoinItem.getCoinValueKey())){
                            int valueModifier = Integer.parseInt(Objects.requireNonNullElse(itemInHand.getItemMeta().getPersistentDataContainer().get(CoinItem.getCoinValueKey(), PersistentDataType.STRING),"0"));
                            int mode = p.isSneaking() ? -1 : 1;
                            
                            int priceadd = Math.max(station.getPrice()+(valueModifier*mode),0);
                            logAdmin(priceadd);
                            station.setPrice(priceadd);
                            station.save();
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_TOOLSMITH,.5f,1.5f);
                        } else {
                            if((MaterialTags.DYES.isTagged(itemInHand.getType()) || itemInHand.getType().equals(Material.INK_SAC) || itemInHand.getType().equals(Material.GLOW_INK_SAC)) && !p.isSneaking()){
                                event.setCancelled(false);
                                event.setUseInteractedBlock(Event.Result.ALLOW);
                                logAdmin("dye");
                            } else {
                                station.setSoldItem(itemInHand);
                                station.save();
                                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_WORK_TOOLSMITH,.5f,1.25f);
                            }
                        }
                    } else {
                        logAdmin("client");
                        // player interacting is not the owner -> buying
                        int purseValue = CoinItem.evaluateInventoryValue(p.getInventory().getContents());
                        int soldPrice = station.getPrice();
                        ItemStack sold = station.getSoldItem();
                        int soldAmount = sold.getAmount();
                        
                        if(station.getContainer() != null && station.getContainer().getState(false) instanceof Container containerState){
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
                            
                            logAdmin("sellable",sellableAmount);
                            if(sellableAmount < soldAmount) {
                                p.sendActionBar(Component.text("This shop is empty").color(NamedTextColor.RED));
                                p.playSound(p.getLocation(),Sound.ENTITY_VILLAGER_TRADE,.5f,.75f);
                                return;
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
                                if(boughtAmount > 0){
                                    totalPrice = soldPrice;
                                }
                            }
                            
                            logAdmin("bought",boughtAmount);
                            if(boughtAmount == 0) {
                                p.sendActionBar(Component.text("This shop has nothing more in stock").color(NamedTextColor.RED));
                                p.playSound(p.getLocation(),Sound.ENTITY_VILLAGER_TRADE,.5f,.75f);
                                return;
                            }
                            
                            logAdmin("purse",purseValue);
                            
                            if(purseValue < totalPrice){
                                p.sendActionBar(Component.text("You do not have enough coins to buy this").color(NamedTextColor.RED));
                                p.playSound(p.getLocation(),Sound.ENTITY_VILLAGER_NO,1,1);
                            } else {
                                int remainingCoins = purseValue-totalPrice;
                                int remainingSoldItems = sellableAmount-boughtAmount;
                                int toTransfer = boughtAmount;
                                // all checks done, player can buy !
                                
                                // give the player what they bought and removing it from the container
                                for(ItemStack bought : containerState.getInventory().getContents()){
                                    if(bought != null){
                                        int bAmount = bought.getAmount();
                                        int remains = Math.max(0,bAmount-toTransfer);
                                        int transferred = bAmount-remains;
                                        toTransfer -= transferred;
                                        
                                        logAdmin("transfer",transferred);
                                        for(ItemStack overflow : p.getInventory().addItem(bought.asQuantity(transferred)).values()){
                                            p.getWorld().dropItem(p.getLocation(),overflow);
                                        }
                                        bought.setAmount(remains);
                                        if(toTransfer <= 0){
                                            break;
                                        }
                                    }
                                    containerState.update();
                                }
                                // removing the money from the player's inventory
                                for(ItemStack item : p.getInventory()){
                                    if(item != null && TiboiseItem.getItemId(item).equals(TiboiseItem.getId(CoinItem.class))){
                                        item.setAmount(0);
                                    }
                                }
                                
                                // adding the paid money to the shop's money storage
                                station.setStoredCoins(station.getStoredCoins()+totalPrice);
                                station.save();
                                
                                
                                // giving back the remaining money to the player
                                for(ItemStack item : CoinItem.splitValue(remainingCoins)){
                                    for(ItemStack overflow : p.getInventory().addItem(item).values()){
                                        p.getWorld().dropItem(p.getLocation(),overflow);
                                    }
                                }
                                
                                p.sendActionBar(Component.text("You have bought "+TiboiseUtils.capitalizeFirstLetter(TiboiseItem.getItemId(station.getSoldItem()).replace("_"," "))+" x"+boughtAmount+" for "+totalPrice+"c").color(NamedTextColor.GOLD));
                                p.playSound(p.getLocation(),Sound.ENTITY_ITEM_PICKUP,1,1.1f);
                                p.playSound(p.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,.5f,1.2f);
                            }
                        }
                    }
                }
            } else if(checkShopValidity(b)) {
                // block b is a trading station !
                TradingStation station = fromBlock(b);
                logAdmin("station core");
                if(station != null) {
                    
                    if(p.isSneaking() && Objects.equals(event.getPlayer().getUniqueId(),station.getOwner())){
                        logAdmin("station V core");
                        event.setCancelled(true);
                        event.setUseInteractedBlock(Event.Result.DENY);
                        // access the money storage
                        ItemStack[] split = CoinItem.splitValue(station.getStoredCoins());
                        int amount = Arrays.stream(split).mapToInt(c -> (int) Math.ceil(c.getAmount()/64.0)).reduce(Integer::sum).orElse(0);
                        int invLines = (int) Math.min((Math.ceil(amount/9.0)+1),6);
                        Inventory inv = Bukkit.createInventory(p,9*invLines,Component.text("Coins stored"));
                        inv.addItem(split);
                        InventoryView view = p.openInventory(inv);
                        openedCoinsStorage.put(view,station);
                    }
                }
                
            }
        }
    }
    
    public static boolean checkShopValidity(Block shopContainer){
        return allowedContainer.contains(shopContainer.getType())
                && shopContainer.getState() instanceof PersistentDataHolder holder
                && Objects.equals(holder.getPersistentDataContainer().get(utilityBlockKey, PersistentDataType.STRING), new TradingStation(null,null).getUtilityIdentifier());
    }
    
    @Override
    public String getUtilityIdentifier() {
        return "trading_station";
    }
    
    /**
     *
     * @param block the CONTAINER block to which the shop sign is attached
     * @return the newly built trading station
     */
    public static TradingStation fromBlock(Block block){
        Block container = null;
        Block sign = null;
        if(block.getState() instanceof Sign && block.getBlockData() instanceof WallSign wallSign){
            logAdmin("wallSign");
            if(checkShopValidity(block.getRelative(wallSign.getFacing().getOppositeFace()))){
                // from sign
                container = block.getRelative(wallSign.getFacing().getOppositeFace());
                sign = block;
            }
        } else if(
                allowedContainer.contains(block.getType())
                && block.getState() instanceof PersistentDataHolder
        ) {
            logAdmin("fromCont");
            container = block;
            
            Vector vectorCheck = new Vector(1,0,0);
            for(int i = 0; i<4;i++){
                Vector rotated = vectorCheck.rotateAroundY(Math.toRadians(90));
                Block relative = block.getRelative(rotated.getBlockX(),rotated.getBlockY(),rotated.getBlockZ());
                if(relative instanceof Sign signState && signState.lines().stream().anyMatch(c -> ((TextComponent) c).content().equals(tradingStationIdentifier))){
                    sign = relative;
                    break;
                }
            }
        }
        if(container.getState() instanceof PersistentDataHolder holder){
            PersistentDataContainer persistent = holder.getPersistentDataContainer();
            TradingStation tradingStation = new TradingStation(null,null);
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
            int itemPrice = Integer.parseInt(persistent.getOrDefault(getItemPriceKey(), PersistentDataType.STRING, "0"));
            tradingStation.setPrice(itemPrice);
            // stored coins
            int coins = Integer.parseInt(persistent.getOrDefault(getStoredCoinsKey(), PersistentDataType.STRING, "0"));
            logAdmin("coins",coins);
            tradingStation.setStoredCoins(coins);
            
            // container
            tradingStation.setContainer(container);
            
            // sign
            if(sign != null){
                tradingStation.setSign(sign);
            }
            
            return tradingStation;
        }
        return null;
    }
    
    public void save(){
        cachedStations.put(getContainer().getLocation(),this);
        logAdmin("save1");
        BlockState state = getContainer().getState(false);
        if(state instanceof PersistentDataHolder holder){
            
            logAdmin(getContainer().getType());
            
            if(this.sign != null){
                setOwnership(sign,getOwner());
            }
            
            logAdmin("save2");
            PersistentDataContainer persistent = holder.getPersistentDataContainer();
            
            persistent.set(utilityBlockKey,PersistentDataType.STRING, getUtilityIdentifier());
            persistent.set(getOptionsKey(),PersistentDataType.STRING,getOptions().serialize());
            setOwnership(getContainer(),getOwner());
            persistent.set(getItemSoldKey(),PersistentDataType.STRING,new ItemSerializer().serialize(getSoldItem(),0));
            persistent.set(getItemPriceKey(),PersistentDataType.STRING,String.valueOf(getPrice()));
            persistent.set(getStoredCoinsKey(),PersistentDataType.STRING,String.valueOf(getStoredCoins()));
            
            state.update();
        }
        setPrice(getPrice());
        setSoldItem(getSoldItem());
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
        if(sign != null && sign.getState(false) instanceof Sign state){
            String itemName = TiboiseUtils.capitalizeFirstLetter(TiboiseItem.getItemId(soldItem).replace("_"," "));
            if(soldItem.getAmount() > 1){
                int maxLineLength = 13;
                if(itemName.length() > maxLineLength){
                    itemName = itemName.substring(0,maxLineLength-2)+"...";
                }
                state.line(2,Component.text(itemName+" "+soldItem.getAmount()+"x"));
            } else {
                int maxLineLength = 17;
                if(itemName.length() > maxLineLength){
                    itemName = itemName.substring(0,maxLineLength-2)+"...";
                }
                state.line(2,Component.text(itemName));
            }
            state.update();
        }
    }
    
    public int getPrice() {
        return price;
    }
    
    protected void setPrice(int price) {
        this.price = price;
        if(sign != null && sign.getState(false) instanceof Sign state){
            logAdmin("signSet");
            state.line(3,Component.text(price+"c"));
            state.update();
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
        if(sign.getState() instanceof Sign){
            this.sign = sign;
        }
    }
}
