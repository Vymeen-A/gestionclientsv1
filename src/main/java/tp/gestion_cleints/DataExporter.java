package tp.gestion_cleints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class DataExporter {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void exportClients(List<Client> clients, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            gson.toJson(clients, writer);
        }
    }

    public static List<Client> importClients(String filePath) throws IOException {
        try (FileReader reader = new FileReader(filePath)) {
            Type listType = new TypeToken<List<Client>>() {
            }.getType();
            return gson.fromJson(reader, listType);
        }
    }
}
