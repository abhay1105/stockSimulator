package com.abhay.stockSimGame;


import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

// class will take care of the connection between the server and a client
public class ClientConnection {

    // all variables located here
    private ServerWebSocket              webSocket;
    private Logger                       logger;
    private Player                       player;
    private Game                         game;
    private Vertx                        m_vertx;

    // class constructor
    public ClientConnection(Vertx m_vertx, ServerWebSocket webSocket, Logger logger) {
        this.m_vertx = m_vertx;
        this.webSocket = webSocket;
        this.logger = logger;
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

    // method creates a new player that is associated with this ClientConnection object
    public void createNewPlayer(String playerName) {
        player = new Player(playerName);
        System.out.println("NEW PLAYER CREATED WITH NAME:      " + playerName);
    }

    // method to send the information in order to update a client's account page
    public void sendUpdatedAccountPage(Game game) {
        JsonObject json = new JsonObject();
        json.put("player_name", player.getName());
        JsonArray jsonArray = new JsonArray();
        for (Stock stock: game.getMarket().getStocks()) {
            jsonArray.add(stock.getSymbol());
        }
        json.put("stocks", jsonArray);
        json.put("type", "updated_account_page_info");
        webSocket.writeTextMessage(json.encode());
    }

    // method will be in charge of creating a new game or joining a current one
    public void createOrJoinGame(JsonObject json) throws IOException, InterruptedException {
        // purpose is due to the fact that an updated accountPage should be sent regardless of if they create a
        // game or if they join a game
        game = new Game("multiple-stocks");
        if (json.getString("player_choice").equals("yes")) {
            game.addPlayer(player);
        } else {
            System.out.println("need to join a game. game codes coming soon");
        }
        sendUpdatedAccountPage(game);
    }

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

    // method will send all of the appropriate stock data in intervals based on a specific time length
    public void sendStockDataInterval(JsonObject json) throws InterruptedException {

        JsonArray jsonArray = json.getJsonArray("stock_symbols");
        double intervalTime = json.getDouble("interval_time");
        double amountOfDataPerInterval = json.getDouble("amount_per_interval");
        AtomicBoolean stopTimer = new AtomicBoolean(false);

        long timerID = m_vertx.setPeriodic((long) intervalTime, aLong -> {
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
                        System.out.println(i);
                        BarData barData = game.getStockBySymbol((String) stockSymbol).getHistoricalData().get(i);
                        oneStockIntervalPrices.add(barData.getOpen() / barData.getSplitCoefficient());
                        dataCountIndividual = i + 1;
                    }
                    // this message will allow the client to know whether or not there is more data to come
                    if (dataCountIndividual == game.getStockBySymbol((String) stockSymbol).getHistoricalData().size()) {
                        oneStockInterval.put("data_finished", "true");
                        stopTimer.set(true);
                    } else {
                        oneStockInterval.put("data_finished", "false");
                    }
                    stockName = game.getStockBySymbol((String) stockSymbol).getName();
                }
                oneStockInterval.put("open_prices", oneStockIntervalPrices);
                if (stockName != null) {
                    oneStockInterval.put("stock_name", stockName);
                }
                System.out.println(stockSymbol);
                oneStockInterval.put("stock_symbol", (String) stockSymbol);
                completeIntervalData.add(oneStockInterval);
            }
            dataCountMaster += amountOfDataPerInterval;
            JsonObject finalMessage = new JsonObject();
            finalMessage.put("complete_interval_data", completeIntervalData);
            finalMessage.put("type", "stock_chart_data_interval");
            webSocket.writeTextMessage(finalMessage.encode());
        });

        // stopping the timer once there is no longer enough data in one of the stocks (at least for now, eventually
        // we will add an end time to stop)
        if (stopTimer.get()) {
            System.out.println("stopped timer due to lack of data");
            m_vertx.cancelTimer(timerID);
        }

    }

    // method to buy shares in a stock
    public void buyShares(JsonObject json) {
        String stockSymbol = json.getString("stock_symbol");
        double sharePrice = json.getDouble("share_price");
        int numOfShares = json.getInteger("number_of_shares");
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
        double sharePrice = json.getDouble("share_price");
        int numOfShares = json.getInteger("number_of_shares");
        for (Position position: player.getAccount().getPositions()) {
            if (position.getStock().getSymbol().equals(stockSymbol)) {
                position.sellShares(numOfShares, sharePrice);
                break;
            }
        }
    }

}
