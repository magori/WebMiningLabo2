package ch.hesso;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    private static String pathDataFile = "D:\\HesSo\\ProjetHes\\WebMining\\Labo2\\data\\part2 - market basket analysis\\data.csv";
    private static String pathOutPut = "D:\\HesSo\\ProjetHes\\WebMining\\Labo2\\data\\part2 - market basket analysis";

    public static void main(String args[]) throws IOException {
        List<Ligne> dataFromFile = readFileAndSplitData(pathDataFile);
        dataFromFile = cleanData(dataFromFile);
        createFile("byCustomer", dataFromFile, Ligne::getCustomerID);
        createFile("byCountry", dataFromFile, Ligne::getCountry);
        createFile("byInvoice", dataFromFile, Ligne::getInvoiceNo);
    }

    private static List<Ligne> cleanData(List<Ligne> dataFromFile) {
        System.out.println("Nb ligne in file : " + dataFromFile.size());
        dataFromFile = dataFromFile.stream()
                                   .filter(ligne -> !ligne.getDescription().isEmpty())
                                   .filter(ligne -> ligne.getQuantity() > 0)
                                   .collect(Collectors.toList());

        uniformStockCode(dataFromFile);
        System.out.println("Nb ligne after filtered in file: " + dataFromFile.size());
        return dataFromFile;
    }

    private static Map<String, List<Ligne>> groupeBy(final Function<Ligne, String> champs, final List<Ligne> data) {
        return data.stream()
                   .filter(p -> !champs.apply(p).isEmpty())
                   .collect(Collectors.groupingBy(champs));
    }

    private static void createFile(final String fileName, final List<Ligne> data, final Function<Ligne, String> fieldGrouping) throws IOException {
        final Instant start = Instant.now();
        final Map<String, Map<String, Integer>> productDescription = resolveProductDescription(data);
        final List<String> idsProducts = new ArrayList<>(productDescription.keySet());
        final Map<String, List<Ligne>> map = groupeBy(fieldGrouping, data);

        System.out.println("Nb products: " + productDescription.size());
        System.out.println("Map (" + fileName + ") size: " + map.size());

        List<String> newList = map.entrySet()
                                  .stream()
                                  .parallel()
                                  .map(entry -> createLigne(idsProducts, entry))
                                  .collect(Collectors.toList());

        final String head = createHead(productDescription);
        System.out.println("Processing Time: " + Duration.between(start, Instant.now()).toMillis() + "ms");

        createNewFile(fileName, head, newList);
        long timeElapsed = Duration.between(start, Instant.now()).toMillis();
        System.out.println("Total Time: " + timeElapsed + "ms");
    }

    private static void uniformStockCode(final List<Ligne> data) {
        Map<String, Map<String, Integer>> productDescription = resolveProductDescription(data);
        final Map<String, List<String>> mapTitleCode = productDescription.entrySet()
                                                                         .stream()
                                                                         .collect(Collectors.groupingBy(
                                                                                 o -> o.getValue().keySet().iterator().next(),
                                                                                 Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
        Map<String, String> mapStockCode = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : mapTitleCode.entrySet()) {
            entry.getValue().forEach(v -> mapStockCode.put(v, entry.getValue().get(0)));
        }
        data.forEach(ligne -> ligne.setStockCode(mapStockCode.get(ligne.getStockCode())));
    }

    private static String createHead(final Map<String, Map<String, Integer>> productDescrition) {
        return productDescrition
                .values()
                .stream()
                .map(v -> {
                    String desc = v.keySet().iterator().next();
                    if (v.keySet().size() > 1 && v.values().iterator().next().equals(v.values().iterator().next())) {
                        String desc2 = v.keySet().iterator().next();
                        if (desc2.length() > desc.length()) {
                            desc = desc2;
                        }
                    }
                    return desc;
                })
                .reduce("ID", (acc, description) -> acc + "," + description);
    }

    private static Map<String, Map<String, Integer>> resolveProductDescription(final List<Ligne> data) {
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

    private static void createNewFile(final String fileName, final String head, final List<String> newList) throws IOException {
        Path newFilePath = Paths.get(pathOutPut + "\\" + fileName + ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(newFilePath)) {
            writer.write(head + "\n");
            newList.forEach(ligne -> {
                try {
                    writer.write(ligne + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static String createLigne(final Collection<String> allProducts, final Map.Entry<String, List<Ligne>> entry) {
        Set<String> stockCodes = createStockCodes(entry.getValue());
        return allProducts.stream()
                          .reduce(entry.getKey(), (acc, product) -> acc + "," + stockCodes.contains(product));
    }

    private static Set<String> createStockCodes(final List<Ligne> data) {
        return data.stream().map(Ligne::getStockCode).collect(Collectors.toSet());
    }

    private static List<Ligne> readFileAndSplitData(String fileName) {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            return stream.map(ligne -> new ArrayList<>(Arrays.asList(ligne.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))))
                         .skip(1)
                         .map(Ligne::new)
                         .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
            productDescrition.entrySet().stream().filter(entry -> entry.getKey().length() == 0).forEach(System.out::println);

            StringBuilder s =new StringBuilder();
        idsProducts.forEach(s1 -> s.append(","+productDescrition.get(s1).keySet().iterator().next()));
        System.out.println(s);
     */
}
