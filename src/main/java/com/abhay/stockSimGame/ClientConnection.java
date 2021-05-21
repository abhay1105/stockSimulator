package com.abhay.stockSimGame;


import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

// class will take care of the connection between the server and a client
public class ClientConnection {

    // all variables located here
    private ServerWebSocket              webSocket;
    private Logger                       logger;
    private Player                       player;
    private EMAPlayer                    emaPlayer = new EMAPlayer();
    private Game                         game;
    private Vertx                        m_vertx;
    private ArrayList<Game>              allGames;
    private long                         timerID;

    // class constructor
    public ClientConnection(Vertx m_vertx, ServerWebSocket webSocket, Logger logger, ArrayList<Game> allGames) {
        this.m_vertx = m_vertx;
        this.webSocket = webSocket;
        this.logger = logger;
        this.allGames = allGames;
        webSocket.handler(buffer -> {
            logger.info(buffer.toString());
            try {
                handleMessage(buffer.toString());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    // method to remove a client connection from a running game
    public void removePlayerFromGame() {

    }

    // method takes in every message that is incoming from the Client and processes it
    public void handleMessage(String message) throws IOException, InterruptedException {
        JsonObject json = new JsonObject(message);
        String messageType = json.getString("type");
        switch (messageType) {
            case "new_player":
                createNewPlayer(json.getString("player_name"));
                break;
            case "new_or_current_game":
                createOrJoinGame(json);
                break;
            case "stock_data_request_full":
                sendStockDataFull(json);
                break;
            case "stock_data_request_interval":
                sendStockDataInterval(json);
                break;
            case "buy_request":
                buyShares(json);
                break;
            case "sell_request":
                sellShares(json);
                break;
        }
    }

    // method will help us make sure that our double values are rounded to the nearest cent
    public double roundToNearestPenny(double num) {
        return (double) Math.round(num * 100) / 100;
    }

    // method creates a new player that is associated with this ClientConnection object
    public void createNewPlayer(String playerName) {
        // make sure we send the initial balance along with the player's name here
        player = new Player(playerName, 1000);
        System.out.println("NEW PLAYER CREATED WITH NAME:      " + playerName);
    }

    // method to send the information in order to update a client's account page
    public void sendUpdatedAccountPage(Game game) {
        System.out.println("start of update account page func");
        JsonObject json = new JsonObject();
        json.put("player_name", player.getName());
        JsonArray jsonArray = new JsonArray();
        for (Stock stock: game.getMarket().getStocks()) {
            jsonArray.add(stock.getSymbol());
        }
        json.put("stocks", jsonArray);
        json.put("game_code", game.getGameID());
        json.put("first_player_score", roundToNearestPenny(player.getAccount().getCurrentBalance()));
        json.put("time_remaining", game.getTimePeriod());
        json.put("type", "updated_account_page_info");
        webSocket.writeTextMessage(json.encode());
        System.out.println("end of update account page func");
    }

    // method will be in charge of creating a new game or joining a current one
    public void createOrJoinGame(JsonObject json) throws IOException, InterruptedException {
        // purpose is due to the fact that an updated accountPage should be sent regardless of if they create a
        // game or if they join a game
        if (json.getString("player_choice").equals("yes")) {
            ArrayList<String> stockNames = new ArrayList<>();
            ArrayList<String> stockSymbols = new ArrayList<>();
            for (int i = 0;i < json.getJsonArray("stock_names").size();i++) {
                stockNames.add(json.getJsonArray("stock_names").getString(i));
                stockSymbols.add(json.getJsonArray("stock_symbols").getString(i));
            }
            System.out.println("before game created");
            game = new Game(json.getString("mode"), stockNames, stockSymbols, json.getDouble("starting_balance"), json.getDouble("game_time"));
            System.out.println("after game created");
            game.addPlayer(player);
            // adding any computer opponents after
            game.addPlayer(emaPlayer);
        } else {
            String gameCode = json.getString("game_code");
            for (Game game: allGames) {
                if (gameCode.equals(game.getGameID())) {
                    this.game = game;
                    game.addPlayer(player);
                }
            }
        }
        sendUpdatedAccountPage(game);
        System.out.println("account info sent");
    }

    // method to update the leaderboard of players in the game
    public void sendUpdatedLeaderBoard(JsonObject jsonObject) {
        JsonArray players = new JsonArray();
        JsonArray scores = new JsonArray();
        for (int i = 0;i < game.getPlayers().size();i++) {
            players.add(game.getPlayers().get(i).getName());
            scores.add(game.getPlayers().get(i).getAccount().getCurrentBalance());
            System.out.println("current balance:    " + game.getPlayers().get(i).getAccount().getCurrentBalance());
        }
        jsonObject.put("players", players);
        jsonObject.put("scores", scores);
    }

//    call above function

    // method will send all of the appropriate stock data the server has based on the request made by the client
    public void sendStockDataFull(JsonObject json) {
        String stockSymbol = json.getString("stock_symbol");
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        String stockName = null;
        if (game.getStockBySymbol(stockSymbol) != null) {
            for (BarData barData: game.getStockBySymbol(stockSymbol).getHistoricalData()) {
                jsonArray.add(barData.getOpen() / barData.getSplitCoefficient());
                if ((barData.getOpen() / barData.getSplitCoefficient()) > 2000) {
                    System.out.println(barData.toString());
                }
            }
            stockName = game.getStockBySymbol(stockSymbol).getName();
        }
        jsonObject.put("open_prices", jsonArray);
        if (stockName != null) {
            jsonObject.put("stock_name", stockName);
        }
        jsonObject.put("stock_symbol", stockSymbol);
        jsonObject.put("type", "stock_chart_data_full");
        webSocket.writeTextMessage(jsonObject.encode());
    }

    // used in sendStockDataInterval() method
    private int dataCountMaster = 0;

    // used in sendStockDataInterval() method
    private double totalProgressOfMilliseconds = 0;

    // method will send all of the appropriate stock data in intervals based on a specific time length
    public void sendStockDataInterval(JsonObject json) {

        JsonArray jsonArray = json.getJsonArray("stock_symbols");
        double intervalTime = json.getDouble("interval_time");
        double amountOfDataPerInterval = json.getDouble("amount_per_interval");

        // will run every second regardless, but will technically only send data every time a full interval has been completed
        timerID = m_vertx.setPeriodic(1000, aLong -> {

            // setting account statistics at 0 every loop of timer so that we can update them accordingly by adding all
            // the individual statistics of the stocks
            double currentValueAccount = 0;
            double moneyInvestedAccount = 0;

            // runs every time a full interval is ran
            if (totalProgressOfMilliseconds % intervalTime == 0) {

                // keeps track of number of milliseconds so far
                totalProgressOfMilliseconds += 1000;

                // this chunk of code is expected to go into the actual timer body
                JsonArray completeIntervalData = new JsonArray();
                for (Object stockSymbol: jsonArray) {
                    int dataCountIndividual = dataCountMaster;
                    JsonObject oneStockInterval = new JsonObject();
                    JsonArray oneStockIntervalPrices = new JsonArray();
                    String stockName = null;
                    if (game.getStockBySymbol((String) stockSymbol) != null) {
                        // or statement in for loop will either give us the next blank data points based on the client's request or
                        // if there is not enough, it will simply give the remaining items in the data ArrayList
                        double endOfInterval = dataCountIndividual + amountOfDataPerInterval;
                        for (int i = dataCountIndividual;i < endOfInterval && i < game.getStockBySymbol((String) stockSymbol).getHistoricalData().size();i++) {
                            BarData barData = game.getStockBySymbol((String) stockSymbol).getHistoricalData().get(i);
                            oneStockIntervalPrices.add(barData.getOpen() / barData.getSplitCoefficient());
                            emaPlayer.updateDataLists((String) stockSymbol, barData.getOpen() / barData.getSplitCoefficient());
                            dataCountIndividual = i + 1;
                        }
                        // this message will allow the client to know whether or not there is more data to come
                        if (dataCountIndividual == game.getStockBySymbol((String) stockSymbol).getHistoricalData().size()) {
                            oneStockInterval.put("data_finished", "true");
                        } else {
                            oneStockInterval.put("data_finished", "false");
                        }
                        stockName = game.getStockBySymbol((String) stockSymbol).getName();
                    }
                    oneStockInterval.put("open_prices", oneStockIntervalPrices);
                    if (stockName != null) {
                        oneStockInterval.put("stock_name", stockName);
                    }
                    oneStockInterval.put("stock_symbol", (String) stockSymbol);
                    for (Position position: player.getAccount().getPositions()) {
                        if (position.getStock().getSymbol().equals(stockSymbol)) {
                            oneStockInterval.put("number_of_shares", position.getNumOfShares());
                            oneStockInterval.put("current_value_stock", roundToNearestPenny(position.getNumOfShares() * oneStockIntervalPrices.getDouble(oneStockIntervalPrices.size() - 1)));
                            oneStockInterval.put("amount_invested_stock", roundToNearestPenny(position.getMoneyInvested()));
                            currentValueAccount += position.getNumOfShares() * oneStockIntervalPrices.getDouble(oneStockIntervalPrices.size() - 1);
                            moneyInvestedAccount += position.getMoneyInvested();
                        }
                    }
                    completeIntervalData.add(oneStockInterval);
                }
                dataCountMaster += amountOfDataPerInterval;
                JsonObject finalMessage = new JsonObject();
                finalMessage.put("complete_interval_data", completeIntervalData);
                finalMessage.put("current_value_account", roundToNearestPenny(currentValueAccount));
                finalMessage.put("money_invested_account", roundToNearestPenny(moneyInvestedAccount));
                finalMessage.put("profit_or_loss_account", roundToNearestPenny(currentValueAccount - moneyInvestedAccount));
                finalMessage.put("account_balance", roundToNearestPenny(player.getAccount().getCurrentBalance()));
                finalMessage.put("total_num_of_milliseconds_left", game.getTimePeriod() - totalProgressOfMilliseconds);
                sendUpdatedLeaderBoard(finalMessage);
                finalMessage.put("type", "stock_chart_data_interval");
                webSocket.writeTextMessage(finalMessage.encode());
            } else {
                // this section will run every second, except when the full interval of time has been complete
                JsonObject finalMessage = new JsonObject();
                totalProgressOfMilliseconds += 1000;
                finalMessage.put("total_num_of_milliseconds_left", game.getTimePeriod() - totalProgressOfMilliseconds);
                finalMessage.put("type", "empty_timer_run");
                webSocket.writeTextMessage(finalMessage.encode());
            }

            // stopping the timer once there is no longer enough data in one of the stocks (at least for now, eventually
            // we will add an end time to stop)
            if (game.getTimePeriod() == totalProgressOfMilliseconds) {
                System.out.println("stopped timer due to lack of data");
                m_vertx.cancelTimer(timerID);
            }

        });

    }

    // method to buy shares in a stock
    public void buyShares(JsonObject json) {
        String stockSymbol = json.getString("stock_symbol");
        System.out.println(stockSymbol);
        double sharePrice = Double.parseDouble(json.getString("share_price"));
        System.out.println(sharePrice);
        double numOfShares = json.getDouble("number_of_shares");
        System.out.println(numOfShares);
        for (Position position: player.getAccount().getPositions()) {
            if (position.getStock().getSymbol().equals(stockSymbol)) {
                position.buyShares(numOfShares, sharePrice);
                break;
            }
        }
    }

    // method to sell shares in a stock
    public void sellShares(JsonObject json) {
        String stockSymbol = json.getString("stock_symbol");
        double sharePrice = Double.parseDouble(json.getString("share_price"));
        double numOfShares = json.getDouble("number_of_shares");
        for (Position position: player.getAccount().getPositions()) {
            if (position.getStock().getSymbol().equals(stockSymbol)) {
                position.sellShares(numOfShares, sharePrice);
                break;
            }
        }
    }

}
