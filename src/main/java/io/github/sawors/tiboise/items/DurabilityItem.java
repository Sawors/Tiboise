package io.github.sawors.tiboise.items;

public interface DurabilityItem {
    /**
     *
     * @return The ID of the ItemStacks that can be used to repair the item.
     * Please note that it returns ID processed through TiboiseItem.getItemId(ItemStack item)
     */
    public String getRepairMaterialId();
    public int getRepairPointPerItem();
}
