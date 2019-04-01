package ch.hesso;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DataLoader {

    public static List<Ligne> loadData(String pahtFile) {
        List<Ligne> data = readFileAndSplitData(pahtFile);
        return cleanData(data);
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

    private static List<Ligne> cleanData(List<Ligne> dataFromFile) {
        System.out.println("Nb ligne in data file: " + dataFromFile.size());
        final Map<String, Long> notCorrectName = findNotCorrectDescription(dataFromFile);

        dataFromFile = dataFromFile.stream()
                                   .filter(ligne -> !ligne.getDescription().isEmpty())
                                   .filter(ligne -> !ligne.getStockCode().isEmpty())
                                   .filter(ligne -> !ligne.getCustomerID().isEmpty())
                                   .filter(ligne -> !ligne.getInvoiceNo().isEmpty())
                                   .filter(ligne -> !ligne.getCountry().isEmpty())
                                   .filter(ligne -> ligne.getQuantity() > 0)
                                   .filter(ligne -> !notCorrectName.containsKey(ligne.getDescription()))
                                   .collect(Collectors.toList());


        uniformStockCode(dataFromFile);
        System.out.println("Nb ligne after cleaning in file: " + dataFromFile.size());
        return dataFromFile;
    }

    private static void uniformStockCode(final List<Ligne> data) {
        Map<String, Map<String, Integer>> productDescription = Main.resolveProductDescription(data);

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

    private static Map<String, Long> findNotCorrectDescription(final List<Ligne> data) {
        Map<String, Map<String, Integer>> productDescription = Main.resolveProductDescription(data);
        final Map<String, Long> notCorrectName = productDescription.entrySet()
                                                                   .stream()
                                                                   .filter(stringMapEntry -> stringMapEntry.getValue().size() > 1)
                                                                   .flatMap(e -> e.getValue().entrySet().stream())
                                                                   .filter(e -> e.getKey().length() < 15)
                                                                   .filter(e->!e.getKey().isEmpty())
                                                                   .map(e -> e.getKey().toLowerCase())
                                                                   .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        System.out.println("Incorrect description: "+ notCorrectName.keySet().stream().collect(Collectors.joining(",")));
        return notCorrectName;
    }
}
