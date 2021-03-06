package ch.hesso;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class FileCreator {

    private final String pathNewFile;
    private final List<Ligne> data;
    private final Map<String, Map<String, Integer>> productDescription;

    public FileCreator(final String pathNewFile, final List<Ligne> data) {
        this.pathNewFile = pathNewFile;
        this.data = data;
        this.productDescription = Main.resolveProductDescription(this.data);
        System.out.println("Nb products: " + this.productDescription.size());
    }

    public void createFile(final String fileName, final Function<Ligne, String> fieldGrouping) throws IOException {
        final TimeWatcher timeWatcher = TimeWatcher.start();
        final List<String> idsProducts = new ArrayList<>(this.productDescription.keySet());
        final Map<String, List<Ligne>> map = groupeBy(fieldGrouping, this.data);

        System.out.println("Nb ligne in new file(" + fileName + ".csv): " + map.size());

        List<String> newList = map.entrySet()
                                  .stream()
                                  .parallel()
                                  .map(entry -> createLigne(idsProducts, entry))
                                  .collect(Collectors.toList());

        final String head = createHead(productDescription);
        System.out.println("Processing Time: " + timeWatcher.format());

        Path path = createNewFile(fileName, head, newList);
        System.out.println("Total time for create the file: " + timeWatcher.end().format());
        System.out.println("File created: " + path);
        System.out.println("--------------------------------------------");
    }

    private Map<String, List<Ligne>> groupeBy(final Function<Ligne, String> champs, final List<Ligne> data) {
        return data.stream()
                   .filter(p -> !champs.apply(p).isEmpty())
                   .collect(Collectors.groupingBy(champs));
    }

    private String createHead(final Map<String, Map<String, Integer>> productDescrition) {
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

    private Path createNewFile(final String fileName, final String head, final List<String> newList) throws IOException {
        Path newFilePath = Paths.get(this.pathNewFile + "\\" + fileName + ".csv");
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
        return newFilePath;
    }

    private static String createLigne(final Collection<String> allProducts, final Map.Entry<String, List<Ligne>> entry) {
        Set<String> stockCodes = createStockCodes(entry.getValue());
        return allProducts.stream()
                          .reduce(entry.getKey(), (acc, product) -> acc + "," + stockCodes.contains(product));
    }

    private static Set<String> createStockCodes(final List<Ligne> data) {
        return data.stream().map(Ligne::getStockCode).collect(Collectors.toSet());
    }
}
