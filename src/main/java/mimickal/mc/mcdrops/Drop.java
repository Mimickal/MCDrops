package mimickal.mc.mcdrops;

public class Drop {

    private static final int DEFAULT_VAL = 1;
    private static final String DEFAULT_ITEM = "sponge";

    private int minAmount;
    private int maxAmount;
    private int weight;
    private String itemName;

    public Drop() {
        this.minAmount = DEFAULT_VAL;
        this.maxAmount = DEFAULT_VAL;
        this.weight = DEFAULT_VAL;
        this.itemName = DEFAULT_ITEM;
    }

    public void setMinAmount(int min) {
        this.minAmount = min;
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

    public void setName(String name) {
        this.itemName = name;
    }

}