package mimickal.mc.mcdrops;

public class Drop {

    private int minAmount = 1;
    private int maxAmount = 1;
    private int weight = 1;
    private String itemName = "";

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