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

        try (CSVParser parser = CSVParser.parse(csvFilePath, StandardCharsets.UTF_8, CSV_FORMAT)) {
            System.out.println("File opened");
            String headers = parser.getHeaderNames().toString();
            System.out.println("headers: " + headers);

            // Connect to Supabase
            String url = "jdbc:postgresql://aws-0-ap-southeast-2.pooler.supabase.com:5432/postgres";
            String user = "postgres.jnghzszlarsaxxhiavcv";
            String password = "iangortoncsw4530";

            try (Connection connection = DriverManager.getConnection(url, user, password)) {

                // Iterate over input CSV records
                int count = 0;

                // Set up insert statement for records
                String insert = "INSERT INTO nsw_property_data2 (property_id, download_date, council_name, purchase_price, address, post_code, property_type, strata_lot_number, property_name, area, area_type, contract_date, settlement_date, zoning, nature_of_property, primary_purpose, legal_description) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

                PreparedStatement preparedStatement = connection.prepareStatement(insert);

                for (final CSVRecord record : parser) {
                    // Get all of the header names and associated values from the record
                    final Map<String, String> recordValues = record.toMap();

                    setUpPreparedStatement(preparedStatement, recordValues);
                    preparedStatement.addBatch();
                    count++;
                    if (count % 500 == 0) {
                        preparedStatement.executeBatch();
                        preparedStatement.clearBatch();
                    }
                }
                System.out.println("Total records: " + count);
                connection.commit();
            } catch (SQLException e) {
                System.err.println("Inserting error: " + e.getMessage());
            }


        } catch (IOException e) {
            System.out.println("File open failed ");
        }
    }

    private static void setUpPreparedStatement(PreparedStatement preparedStatement, Map<String, String> recordValues) throws SQLException {
        if (emptyRecordValue(recordValues.get("property_id"))) {
            preparedStatement.setNull(1, 0);
        } else {
            preparedStatement.setInt(1, Integer.parseInt(recordValues.get("property_id")));
        }
        preparedStatement.setString(2, recordValues.get("download_date"));
        preparedStatement.setString(3, recordValues.get("council_name"));
        if (emptyRecordValue(recordValues.get("purchase_price"))) {
            preparedStatement.setNull(4, 0);
        } else {
            preparedStatement.setInt(4, Integer.parseInt(recordValues.get("purchase_price")));
        }
        preparedStatement.setString(5, recordValues.get("address"));
        if (emptyRecordValue(recordValues.get("post_code"))) {
            preparedStatement.setNull(6, 0);
        } else {
            preparedStatement.setInt(6, Integer.parseInt(recordValues.get("post_code")));
        }
        preparedStatement.setString(7, recordValues.get("property_type"));
        preparedStatement.setString(8, recordValues.get("strata_lot_number"));
        preparedStatement.setString(9, recordValues.get("property_name"));
        if (emptyRecordValue(recordValues.get("area"))) {
            preparedStatement.setNull(10, 0);
        } else {
            preparedStatement.setFloat(10, Float.parseFloat(recordValues.get("area")));
        }
        preparedStatement.setString(11, recordValues.get("area_type"));
        preparedStatement.setString(12, recordValues.get("contract_date"));
        preparedStatement.setString(13, recordValues.get("settlement_date"));
        preparedStatement.setString(14, recordValues.get("zoning"));
        preparedStatement.setString(15, recordValues.get("nature_of_property"));
        preparedStatement.setString(16, recordValues.get("primary_purpose"));
        preparedStatement.setString(17, recordValues.get("legal_description"));
    }

    private static Boolean emptyRecordValue(String recordValue) {
        return recordValue.equals("");
    }
}
