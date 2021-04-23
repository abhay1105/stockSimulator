package com.abhay.stockSimGame;

import java.io.IOException;
import java.util.ArrayList;

// class will take care of the entire management of game
public class Game {

    // some good tips to note will be located here

    // the starting screen will have some sort of feature to either join a game or create a game (so generating number
    //    and letter game codes will be important later) these codes can be verified in some sort of arrayList or something
    // I can use an animation timer in order to simulate the smaller time frame (this will also make it easier for me to
    //    redraw the same time length of graphs when the user switches a stock from a list view)
    // I can maybe add a checkbox or a dropdown in order to select a game mode
    // *******We can have a separate screen on our starting page so that a user can set their desired options for a new game,
    //    however none of the options will need to be registered until they actually press new game

    // Alpha-Vantage API ID
    // 9MFKMGNE0JRT1HWU


    // all variables located here
    private String mode;
    private int numOfPlayers, maxNumOfPlayers;
    private double timePeriod, timeProgress;
    private Market market;
    private ArrayList<Player> players = new ArrayList<>();
    private String gameID;

    // class constructor
    public Game(String mode) throws IOException, InterruptedException {
        this.mode = mode;
        // game ID can be randomly created in here
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

    // method to add a player
    public void addPlayer(Player player) {
        players.add(player);
        createPlayerPositions(player);
    }

    // method to determine the stocks the players will trade with (dependent on which mode the player will choose)
    public void determineMarket() {
        ArrayList<Stock> stocks = new ArrayList<>();
        stocks.add(new Stock("Tesla Motors", "TSLA"));
        if (mode.equals("one-stock")) {
            market = new Market(stocks);
        } else if (mode.equals("multiple-stocks")) {
            stocks.add(new Stock("Gamestop", "GME"));
            stocks.add(new Stock("International Business Machines", "IBM"));
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

}

