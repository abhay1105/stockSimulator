package com.abhay.stockSimGame;

import java.io.IOException;
import java.util.ArrayList;

// class will take care of the entire management of game
public class Game {

    // Alpha-Vantage API ID
    // 9MFKMGNE0JRT1HWU

    // all variables located here
    private String mode;
    private int numOfPlayers, maxNumOfPlayers;
    private double timePeriod, timeProgress;
    private Market market;
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<String> stockNames;
    private ArrayList<String> stockSymbols;
    private String gameID;
    private double startingBalance;

    // class constructor for testing purposes
    public Game(String mode) throws IOException, InterruptedException {
        this.mode = mode;
        // game ID can be randomly created in here
        createGameID();
        determineMarket(); // will use the settings given in order to create the market the host wants
        retrieveMarketHistoricalData(); // will get any data that we don't already have
    }

    // second constructor for actual game use
    public Game(String mode, ArrayList<String> stockNames, ArrayList<String> stockSymbols, double startingBalance, double timePeriod) throws IOException, InterruptedException {
        this.mode = mode;
        this.stockNames = stockNames;
        this.stockSymbols = stockSymbols;
        this.startingBalance = startingBalance;
        this.timePeriod = timePeriod * 60000; // converting the minutes into milliseconds
        // game ID can be randomly created in here
        createGameID();
        determineMarket(); // will use the settings given in order to create the market the host wants
        retrieveMarketHistoricalData(); // will get any data that we don't already have
    }

    // all getter methods located here
    public String getMode() { return mode; }
    public int getNumOfPlayers() { return numOfPlayers; }
    public int getMaxNumOfPlayers() { return maxNumOfPlayers; }
    public double getTimePeriod() { return timePeriod; }
    public double getTimeProgress() { return timeProgress; }
    public Market getMarket() { return market; }
    public ArrayList<Player> getPlayers() { return players; }
    public String getGameID() { return gameID; }

    // method to add a player and do all actions required to set the player up
    public void addPlayer(Player player) {
        players.add(player);
        createPlayerPositions(player);
        setStartingBalance(player);
    }

    // random integer function
    public int randomNumber(int lowerBound, int upperBound) {
        return (int)(Math.floor((Math.random() * (upperBound - lowerBound + 1))) + lowerBound);
    }

    // method to create a randomly generated game code
    public void createGameID() {
        String code = "";
        for (int i = 0;i < 6;i++) {
            code += (char) randomNumber(65, 90);
        }
        gameID = code;
    }

    // method to determine the stocks the players will trade with (dependent on which mode the player will choose)
    public void determineMarket() {
        ArrayList<Stock> stocks = new ArrayList<>();
        // first two parts of if are for testing purposes
        if (mode.equals("one-stock-test")) {
            stocks.add(new Stock("Tesla Motors", "TSLA"));
            market = new Market(stocks);
        } else if (mode.equals("multiple-stocks-test")) {
            stocks.add(new Stock("Tesla Motors", "TSLA"));
            stocks.add(new Stock("Gamestop", "GME"));
            stocks.add(new Stock("International Business Machines", "IBM"));
            market = new Market(stocks);
        } else {
            for (int i = 0;i < stockNames.size();i++) {
                stocks.add(new Stock(stockNames.get(i), stockSymbols.get(i)));
            }
            market = new Market(stocks);
        }
    }

    // method to retrieve the historical information required in order to play the games with the selected stocks
    public void retrieveMarketHistoricalData() throws IOException, InterruptedException {
        for (Stock stock: market.getStocks()) {
            stock.retrieveStockHistoricalData();
            stock.printHistoricalDataSize();
        }
    }

    // method to create all of the position objects for our players
    public void createPlayerPositions(Player player) {
        for (Stock stock: market.getStocks()) {
            player.getAccount().getPositions().add(new Position(player.getAccount(), stock));
        }
    }

    // method to return a stock based off of its symbol
    public Stock getStockBySymbol(String symbol) {
        for (Stock stock: market.getStocks()) {
            if (stock.getSymbol().equals(symbol)) {
                return stock;
            }
        }
        return null;
    }

    // method will set the initial balance of every player's account to the starting balance value
    public void setStartingBalance(Player player) {
        player.getAccount().setInitialBalance(startingBalance);
        player.getAccount().setCurrentBalance(startingBalance);
    }

}

