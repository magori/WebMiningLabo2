package ch.hesso;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static Properties properties = loadConfig();

    public static void main(String args[]) throws IOException {
        System.out.println("-------------------------START--------------------------------");
        List<Ligne> data = DataLoader.loadData(properties.getProperty("data.file.path"));
        FileCreator fileCreator = new FileCreator(properties.getProperty("path.newFile"), data);
        System.out.println("--------------------------------------------------------------");
        fileCreator.createFile("byCountry", Ligne::getCountry);
        fileCreator.createFile("byCustomer", Ligne::getCustomerID);
        fileCreator.createFile("byInvoice", Ligne::getInvoiceNo);
    }

    private static Properties loadConfig() {
        try (InputStream in = Main.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties applicationProps = new Properties();
            applicationProps.load(in);
            return applicationProps;
        } catch (IOException e) {
            throw new RuntimeException("Le fichier de configuration('config.properties') n'a pas été trouvé, il faut le rajouter dans le même " +
                                               "dossier que le jar !", e);
        }
    }

     static Map<String, Map<String, Integer>> resolveProductDescription(final List<Ligne> data) {
        Map<String, List<String>> map = data.stream()
                                            .collect(
                                                    Collectors.groupingBy(
                                                            Ligne::getStockCode,
                                                            Collectors.mapping(Ligne::getDescription, Collectors.toList())
                                                    ));
        Map<String, Map<String, Integer>> mapN = new HashMap<>();
        for (Map.Entry<String, List<String>> e : map.entrySet()) {
            Map<String, Integer> m = new HashMap<>();
            for (String libelle : e.getValue()) {
                m.putIfAbsent(libelle, 0);
                m.computeIfPresent(libelle, (s, i) -> i + 1);
            }
            mapN.put(e.getKey(), m.entrySet()
                                  .stream()
                                  .sorted(Collections.reverseOrder(Comparator.comparing(Map.Entry::getValue)))
                                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new)));
        }

        return mapN;
    }
}
