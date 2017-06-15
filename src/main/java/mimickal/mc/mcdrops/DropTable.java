package mimickal.mc.mcdrops;

import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DropTable {

    private static final String TABLE_PATH = "config/" + DropsMod.MODID + "/dropTable.json";
    private static final String EXAMPLE_DROP_TABLE =
            "{\n" +
            "   \"apple\": { \"weight\": 5 },\n" +
            "   \"coal\": { \n" +
            "       \"weight\": 16,\n" +
            "       \"min\" : 4,\n" +
            "       \"max\" : 10\n" +
            "   },\n" +
            "   \"gold_ingot\": {\n" +
            "       \"weight\": 6,\n" +
            "       \"max\": 3\n" +
            "   },\n" +
            "   \"torch\": {\n" +
            "       \"weight\": 15,\n" +
            "       \"count\": 16\n" +
            "   }\n" +
            "}\n";
    private static final Drop DEFAULT_DROP = new Drop();
    static {
        DEFAULT_DROP.setMinAmount(1);
        DEFAULT_DROP.setMaxAmount(1);
        DEFAULT_DROP.setWeight(0);
        DEFAULT_DROP.setName("sponge");
    }

    private static Random rng = new Random();
    private static List<Drop> drops = new ArrayList<>();
    private static int totalWeight = 0;

    public static Drop getRandomDrop() {
        int roll = rng.nextInt(totalWeight);

        // Sum drop weights in order until we find the weight interval this roll falls between
        int currentWeightSum = 0;

        for (Drop curDrop : drops) {
            if (currentWeightSum <= roll && roll < currentWeightSum + curDrop.getWeight()) {
                return curDrop;
            } else {
                currentWeightSum += curDrop.getWeight();
            }
        }

        return DEFAULT_DROP;
    }

    public static void loadDropTable() {
        JsonReader json = openDropsTable();

        try {
            json.beginObject(); // Start of table

            while (json.hasNext()) {
                Drop nextDrop = parseDrop(json);
                drops.add(nextDrop);
            }

            json.endObject(); // End of table
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Drop parseDrop(JsonReader json) throws IOException {
        Drop drop = new Drop();

        String itemName = json.nextName();
        drop.setName(itemName);

        json.beginObject(); // Start of drop info object

        while (json.hasNext()) {
            String propertyName = json.nextName();
            int propertyVal = json.nextInt();

            if ("weight".equals(propertyName)) {
                drop.setWeight(propertyVal);
                totalWeight += propertyVal;
            } else if ("min".equals(propertyName)) {
                drop.setMinAmount(propertyVal);
            } else if ("max".equals(propertyName)) {
                drop.setMaxAmount(propertyVal);
            } else if ("count".equals(propertyName)) {
                drop.setMinAmount(propertyVal);
                drop.setMaxAmount(propertyVal);
            } else {
                throw new RuntimeException("Malformed Drops JSON file: \"" +
                        propertyName + "\" is not a valid drop property");
            }
        }

        json.endObject(); // End of drop info object

        return drop;
    }

    /* Attempts to create a reader for the drops table json file.
     * Creates and loads a default drop table file, if one doesn't exist.
     */
    private static JsonReader openDropsTable() {
        JsonReader reader;

        try {
            reader = new JsonReader(new FileReader(TABLE_PATH));
        } catch (FileNotFoundException e) {
            try {
                FileUtils.writeStringToFile(new File(TABLE_PATH), EXAMPLE_DROP_TABLE);
                reader = new JsonReader(new FileReader(TABLE_PATH));
            } catch (IOException e2) {
                throw new RuntimeException("Couldn't read dropTable file", e2);
            }
        }

        return reader;
    }

}
