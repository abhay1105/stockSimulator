package com.abhay.stockSimGame;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// class will manage a subclass of player that trades using the EMA indicator (exponential moving average)
public class EMAPlayer extends Player {

    // all variables located here
    private Map<String, ArrayList<Double>> stockDataPoints = new HashMap<>();
    private Map<String, ArrayList<Double>> slowMovingAverages = new HashMap<>();
    private Map<String, ArrayList<Double>> fastMovingAverages = new HashMap<>();
    private Map<String, ArrayList<Double>> trendAnalysisValues = new HashMap<>();
    private int slowIntervalLength = 10;
    private int fastIntervalLength = 5;

    // class constructor
    public EMAPlayer() {
        super("TraderBob", 1000);
    }

    // method to update the data lists
    public void updateDataLists(String stockSymbol, double dataPoint) {
        if (stockDataPoints.keySet().contains(stockSymbol)) {
            stockDataPoints.get(stockSymbol).add(dataPoint);
            updateMovingAverages(stockSymbol);
            if (slowMovingAverages.get(stockSymbol).size() > 0) {
                trendAnalysisValues.get(stockSymbol).add(determineMarketTrend(stockSymbol));
                buyOrSellShare(stockSymbol);
            }
        } else {
            stockDataPoints.put(stockSymbol, new ArrayList<>());
            slowMovingAverages.put(stockSymbol, new ArrayList<>());
            fastMovingAverages.put(stockSymbol, new ArrayList<>());
            trendAnalysisValues.put(stockSymbol, new ArrayList<>());
        }
    }

    // method to update the moving averages every time a data point is added to the list
    public void updateMovingAverages(String stockSymbol) {
        ArrayList<Double> dataList = stockDataPoints.get(stockSymbol);
        if (dataList.size() >= fastIntervalLength) {
            int sum = 0;
            for (int i = dataList.size() - fastIntervalLength;i < dataList.size();i++) {
                sum += dataList.get(i);
            }
            fastMovingAverages.get(stockSymbol).add((double) (sum / fastIntervalLength));
        }
        if (dataList.size() >= slowIntervalLength) {
            int sum = 0;
            for (int i = dataList.size() - slowIntervalLength;i < dataList.size();i++) {
                sum += dataList.get(i);
            }
            slowMovingAverages.get(stockSymbol).add((double) (sum / slowIntervalLength));
        }
    }

    // method to determine whether or not the market has an upward or downward trend
    public double determineMarketTrend(String stockSymbol) {
        ArrayList<Double> fastMovingAverageList = fastMovingAverages.get(stockSymbol);
        ArrayList<Double> slowMovingAverageList = slowMovingAverages.get(stockSymbol);
        // if value below is positive, then the market is on an upward trend; else, it is on a downward trend
        return fastMovingAverageList.get(fastMovingAverageList.size() - 1) - slowMovingAverageList.get(slowMovingAverageList.size() - 1);
    }

    // method will buy or sell a stock if it notices a trend has changed
    public void buyOrSellShare(String stockSymbol) {
        ArrayList<Position> allPositions = getAccount().getPositions();
        for (Position position: allPositions) {
            if (position.getStock().getSymbol().equals(stockSymbol)) {
                double currentNumOfShares = position.getNumOfShares();
                if (trendAnalysisValues.get(stockSymbol).size() >= 2) {
                    double firstAnalysisValue = trendAnalysisValues.get(stockSymbol).get(trendAnalysisValues.get(stockSymbol).size() - 2);
                    double secondAnalysisValue = trendAnalysisValues.get(stockSymbol).get(trendAnalysisValues.get(stockSymbol).size() - 1);
                    if (firstAnalysisValue > 0 && secondAnalysisValue < 0) {
                        // we want to sell a stock when the trend has switched to downwards
                        System.out.println(" bob sold a stock ");
                        if (currentNumOfShares > 0) {
                            position.sellShares(1, stockDataPoints.get(stockSymbol).get(stockDataPoints.get(stockSymbol).size() - 1));
                        }
                    } else if (firstAnalysisValue < 0 && secondAnalysisValue > 0) {
                        // we want to buy a stock when the trend has switched to upwards
                        System.out.println(" bob bought a stock ");
                        position.buyShares(1, stockDataPoints.get(stockSymbol).get(stockDataPoints.get(stockSymbol).size() - 1));
                    }
                }
            }
            break;
        }
    }

}
