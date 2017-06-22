package mimickal.mc.mcdrops;

import net.minecraft.item.ItemStack;

public class Drop {

    private int minAmount = 1;
    private int maxAmount = 1;
    private int weight = 1;
    private ItemStack itemStack;

    public int getMinAmount() {
        return this.minAmount;
    }

    public void setMinAmount(int min) {
        this.minAmount = min;
    }

    public int getMaxAmount() {
        return this.maxAmount;
    }

    public void setMaxAmount(int max) {
        this.maxAmount = max;
    }

    public int getWeight() {
        return this.weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

}