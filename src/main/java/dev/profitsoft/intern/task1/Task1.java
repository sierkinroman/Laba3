package dev.profitsoft.intern.task1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import dev.profitsoft.intern.task1.model.Fine;
import dev.profitsoft.intern.task1.model.FineType;
import dev.profitsoft.intern.task1.model.FinesStatistic;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class Task1 {

    private static final JsonFactory jsonFactory = new JsonFactory();
    private static final String finesRootDirectory = "." + File.separator + "fines" + File.separator;

    public static void main(String[] args) throws IOException {
        FinesGenerator.createFineFiles(finesRootDirectory);

        System.out.println("Time to process all files (22 files x 15 MB each):");

        // 1 thread
        long start = System.currentTimeMillis();
        List<Fine> fineStatistic = getFineStatistic(1);
        System.out.println("1 thread: " + (System.currentTimeMillis() - start) + " ms.");

        // 2 threads
        start = System.currentTimeMillis();
        fineStatistic = getFineStatistic(2);
        System.out.println("2 threads: " + (System.currentTimeMillis() - start) + " ms.");

        // 4 threads
        start = System.currentTimeMillis();
        fineStatistic = getFineStatistic(4);
        System.out.println("4 threads: " + (System.currentTimeMillis() - start) + " ms.");

        // 8 threads
        start = System.currentTimeMillis();
        fineStatistic = getFineStatistic(8);
        System.out.println("8 threads: " + (System.currentTimeMillis() - start) + " ms.");

        fineStatistic.sort(Comparator.comparing(Fine::getFineAmount).reversed());
        writeStatisticToXml(fineStatistic);
    }

    private static List<Fine> getFineStatistic(int nThreads) {
        Map<FineType, BigDecimal> finesAmountByType = new HashMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        Lock lock = new ReentrantLock();

        for (File fineFile : getFineFiles(new File(finesRootDirectory))) {
            processFile(fineFile, executorService, lock, finesAmountByType);
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return finesAmountByType.entrySet().stream()
                .map(entry -> new Fine(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static void processFile(File fineFile, ExecutorService executorService, Lock lock, Map<FineType, BigDecimal> allStatistic) {
        CompletableFuture.supplyAsync(() -> {
            Map<FineType, BigDecimal> finesAmountByType = new HashMap<>();
            try {
                JsonParser jsonParser = jsonFactory.createParser(fineFile);
                checkCorrectedStartFile(jsonParser);

                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    Fine fine = readFine(jsonParser);
                    finesAmountByType.merge(fine.getType(), fine.getFineAmount(), BigDecimal::add);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return finesAmountByType;
        }, executorService).thenAccept(finesAmountByType -> {
            lock.lock();
            finesAmountByType.forEach((fineType, bigDecimal) ->
                    allStatistic.merge(fineType, bigDecimal, BigDecimal::add));
            lock.unlock();
        });
    }

    private static File[] getFineFiles(File dir) {
        String end = "fines.json";
        return dir.listFiles(file ->
                !file.isDirectory() && file.getName().endsWith(end));
    }

    private static void checkCorrectedStartFile(JsonParser jsonParser) throws IOException {
        if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("File should start like {\"fines\":[");
        }
        jsonParser.nextToken();
        if (!"fines".equals(jsonParser.getCurrentName())) {
            throw new IllegalStateException("File should start like {\"fines\":[");
        }
        if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
            throw new IllegalStateException("File should start like {\"fines\":[");
        }
    }

    private static Fine readFine(JsonParser jsonParser) throws IOException {
        if (jsonParser.currentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Fine object should start from {");
        }

        Fine fine = new Fine();

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String property = jsonParser.getCurrentName();
            jsonParser.nextToken();

            switch (property) {
                case "date_time":
                    fine.setDateTime(
                            LocalDateTime.parse(jsonParser.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    break;
                case "first_name":
                    fine.setFirstName(jsonParser.getText());
                    break;
                case "last_name":
                    fine.setLastName(jsonParser.getText());
                    break;
                case "type":
                    fine.setType(FineType.valueOf(jsonParser.getText()));
                    break;
                case "fine_amount":
                    fine.setFineAmount(BigDecimal.valueOf(jsonParser.getDoubleValue()));
                    break;
            }
        }

        return fine;
    }

    private static void writeStatisticToXml(List<Fine> fineStatistic) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);

        xmlMapper.writeValue(new File("fines_statistic.xml"), new FinesStatistic(fineStatistic));
    }

}
