package com.abhay.stockSimGame;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormatSymbols;
import java.util.*;

// class will manage a specific symbol in market
public class Stock {

    // all variables located here
    private String name, symbol;
    private double pricePerShare;
    private ArrayList<BarData> historicalData;

    // class constructor
    public Stock(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
        this.historicalData = new ArrayList<>();
    }

    // all getter methods located here
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
    public double getPricePerShare() { return pricePerShare; }
    public ArrayList<BarData> getHistoricalData() { return historicalData; }

    // method to neatly print our historical data
    public void printHistoricalDataSize() {
        System.out.println("Symbol: " + symbol + "      " + historicalData.size() + " data points");
    }

    // method to retrieve historical data about this specific stock over a period of time
    public void retrieveStockHistoricalData() throws IOException, InterruptedException {
        // if the stock data for a specific stock does not already exist in our files, we will make an API call for it
        // in order to retrieve that data and have it long-term

        // here, we would need to figure out if a specific file already exists
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("C:\\Users\\Kalyani Koyya\\Desktop\\Abhay's Desktop\\stockSimGame2.0\\stockData\\TIME_SERIES_DAILY_ADJUSTED_FULL\\" + symbol + ".json")) {

            System.out.println(historicalData.size());

            // we don't want to accidentally add the same data twice
            if (historicalData.size() == 0) {

                JSONObject json = (JSONObject) jsonParser.parse(reader);
                JSONObject timeSeriesData = (JSONObject) json.get("Time Series (Daily)");
                Set<String> keys = timeSeriesData.keySet();

                Map<Date, BarData> dataValues = new HashMap<>();
                DateFormatSymbols dfs = new DateFormatSymbols();

                for (String key: keys) {

                    JSONObject datum = (JSONObject) timeSeriesData.get(key);
                    BarData barData = new BarData(
                            this, key,
                            Double.parseDouble((String) datum.get("1. open")),
                            Double.parseDouble((String) datum.get("2. high")),
                            Double.parseDouble((String) datum.get("3. low")),
                            Double.parseDouble((String) datum.get("4. close")),
                            Double.parseDouble((String) datum.get("5. adjusted close")),
                            Double.parseDouble((String) datum.get("6. volume")),
                            Double.parseDouble((String) datum.get("7. dividend amount")),
                            Double.parseDouble((String) datum.get("8. split coefficient")));

                    int year = Integer.parseInt(key.substring(0, 4));
                    int month = Integer.parseInt(key.substring(5, 7));
                    int day = Integer.parseInt(key.substring(9));

                    String[] months = dfs.getMonths();
                    String monthName = months[month - 1].toUpperCase();

                    Date date = new Date(year, month, day);
                    dataValues.put(date, barData);

                }

                Map<Date, BarData> orderedDataValues = new TreeMap<>(dataValues);
                for (Date date: orderedDataValues.keySet()) {
                    historicalData.add(orderedDataValues.get(date));
                }

            }

        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("error in parsing file");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("error in finding file");
            writeStockFile();
        }

    }

    // method will write a file containing the information gathered from the API about a particular stock
    public void writeStockFile() throws IOException, InterruptedException {

        // if not, we make the request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + symbol + "&outputsize=full&apikey=9MFKMGNE0JRT1HWU"))
                .header("x-rapidapi-key", "SIGN-UP-FOR-KEY")
                .header("x-rapidapi-host", "alpha-vantage.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        // now that the request has been made, let's save it as a file so that we won't need to use a request
        // on it in the future

        // creates a new file in our desired directory
        File newStockFile = new File("C:\\Users\\Kalyani Koyya\\Desktop\\Abhay's Desktop\\stockSimGame2.0\\stockData\\TIME_SERIES_DAILY_ADJUSTED_FULL\\" + symbol + ".json");
        if (newStockFile.createNewFile()) {
            System.out.println(symbol + "    TIME_SERIES_DAILY_ADJUSTED_FULL    FILE CREATED");
        }

        // creates a fileWriter that will write the API result to the file we created above
        FileWriter fileWriter = new FileWriter(newStockFile);
        fileWriter.write(response.body());
        fileWriter.flush();
        fileWriter.close();

        // we will need to call the retrieveStockHistoricalData() function again in order to create the BarData
        // objects for the stock
        retrieveStockHistoricalData();

    }

}

