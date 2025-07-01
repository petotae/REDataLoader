
package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    private static final CSVFormat CSV_FORMAT = CSVFormat.Builder.create(CSVFormat.RFC4180)
        .setHeader()
        .setSkipHeaderRecord(true)
        .setAllowDuplicateHeaderNames(false)
        .build();

    static final private String PATH_TO_FILE = "nsw_property_data.csv";
    public static void main(String[] args) {

        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.println("Hello and welcome!");

        // Path of CSV file to read
        final Path csvFilePath = Paths.get(PATH_TO_FILE);

        // Path of the temporary file to write work in progress CSV results to
        Path tempFile = null;
        try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)){
            System.out.println("File opened");
            String headers = parser.getHeaderNames().toString();
            System.out.println("headers: " + headers);

            // Connect to Supabase
            // String url = "jdbc:postgresql://db.jnghzszlarsaxxhiavcv.supabase.co:5432/postgres";
            String url = "jdbc:postgresql://aws-0-ap-southeast-2.pooler.supabase.com:5432/postgres";
            String user = "postgres.jnghzszlarsaxxhiavcv";
            String password = "iangortoncsw4530";

            try (Connection connection = DriverManager.getConnection(url, user, password)) {

                // Iterate over input CSV records
                int count = 0;
                for (final CSVRecord record : parser)
                {
                    // Get all of the header names and associated values from the record
                    final Map<String, String> recordValues = record.toMap();

                    // TODO:
                    String insert = String.format("INSERT INTO nsw_property_data VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)", 
                        recordValues.get("property_id"), recordValues.get("download_date"), recordValues.get("council_name"), recordValues.get("purchase_price"), 
                        recordValues.get("address"), recordValues.get("post_code"), recordValues.get("property_type"), recordValues.get("strata_lot_number"), 
                        recordValues.get("property_name"), recordValues.get("area"), recordValues.get("area_type"), recordValues.get("contract_date"), 
                        recordValues.get("settlement_date"), recordValues.get("zoning"), recordValues.get("nature_of_property"), recordValues.get("primary_purpose"), 
                        recordValues.get("legal_description"));
                    while (insert.contains(", ,")) {
                        insert = insert.replaceAll(", ,", ", null,");
                    }

                    try (PreparedStatement preparedStatement = connection.prepareStatement(insert)) {
                        preparedStatement.executeUpdate();
                        System.out.println("Data inserted");
                    }

                    // Write the updated values to the output CSV
                    System.out.println(recordValues.toString());
                    count++;
                    if (count > 10) {
                        break;
                    }
                }
                System.out.println("Total records: " + count);



            } catch (SQLException e) {
                System.err.println("Inserting error: " + e.getMessage());
            }


        } catch (IOException e) {
            System.out.println("File open failed ");
        }
    }
}