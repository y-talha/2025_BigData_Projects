package org.graalvm.demos.springr;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Controller
public class PlotController {
    @Value(value = "classpath:plot.R")
    private Resource rSource;

    @Autowired
    private Function<DataHolder, String> plotFunction;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    private List<Document> sortedDocs; // Row-1, Row-2 ... sıralı liste
    private int currentRow = 0;

    public PlotController() {
        mongoClient = MongoClients.create("mongodb://localhost:27017");
        database = mongoClient.getDatabase("swe307db");
        collection = database.getCollection("project1");

        // Burada tüm dökümanları çek ve Row numarasına göre sırala
        List<Document> allDocs = collection.find().into(new ArrayList<>());
        allDocs.sort((d1, d2) -> {
            String row1 = d1.getString(""); // Row isminin olduğu alan
            String row2 = d2.getString("");
            int num1 = Integer.parseInt(row1.replace("Row-", ""));
            int num2 = Integer.parseInt(row2.replace("Row-", ""));
            return Integer.compare(num1, num2);
        });
        sortedDocs = allDocs;
    }

    @Bean
    Function<DataHolder, String> getPlotFunction(@Autowired Context ctx)
            throws IOException {
        Source source = Source.newBuilder("R", rSource.getURL()).build();
        return ctx.eval(source).as(Function.class);
    }

    @RequestMapping(value = "/plot", produces = "image/svg+xml")
    public ResponseEntity<String> load() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Refresh", "1");

        System.out.print(" ------ ");
        System.out.println(sortedDocs.size());
        double col7Value = 0.0;

        if (!sortedDocs.isEmpty()) {
            Document doc = sortedDocs.get(currentRow);

            if (doc.get("Col-7") instanceof Number) {
                col7Value = ((Number) doc.get("Col-7")).doubleValue();
            }
            //int rowSize=sortedDocs.size() + 1;

            currentRow = (currentRow + 1) % sortedDocs.size();
        }

        System.out.print(col7Value);
        System.out.print("     -     ");
        System.out.println(currentRow);


        String svg = "";
        synchronized (plotFunction) {
            svg = plotFunction.apply(new DataHolder(col7Value));
        }

        return new ResponseEntity<>(svg, responseHeaders, HttpStatus.OK);
    }

    @Bean
    public Context getGraalVMContext() {
        return Context.newBuilder().allowAllAccess(true).build();
    }
}


