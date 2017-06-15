package mimickal.mc.mcdrops;

import com.google.gson.stream.JsonReader;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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

    private static ArrayList<Drop> drops;

    public static void loadDropTable() {
        JsonReader json = openDropsTable();

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
