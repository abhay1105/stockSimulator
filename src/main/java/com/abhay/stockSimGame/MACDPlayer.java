package com.abhay.stockSimGame;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// class will manage a subclass of player that trades using difference between slow and fast moving averages
public class MACDPlayer extends Player {

    // all variables located here
    private Map<String, ArrayList<Double>> stockDataPoints = new HashMap<>();
    private Map<String, ArrayList<Double>> slowMovingAverages = new HashMap<>();
    private Map<String, ArrayList<Double>> fastMovingAverages = new HashMap<>();
    private Map<String, ArrayList<Double>> trendAnalysisValues = new HashMap<>();
    private int slowIntervalLength = 10;
    private int fastIntervalLength = 5;
    private double riskFactor = 5;

    // class constructor
    public MACDPlayer() {
        super("TraderJim(MACD)", 1000);
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
        checkForLargeDataset(stockSymbol, dataList, fastIntervalLength, fastMovingAverages);
        checkForLargeDataset(stockSymbol, dataList, slowIntervalLength, slowMovingAverages);
    }

    // method to check if our data sets are large enough to calculate the moving averages
    public void checkForLargeDataset(String stockSymbol, ArrayList<Double> dataList, int intervalLength, Map<String, ArrayList<Double>> movingAverages) {
        if (dataList.size() >= intervalLength) {
            int sum = 0;
            for (int i = dataList.size() - intervalLength; i < dataList.size(); i++) {
                sum += dataList.get(i);
            }
            movingAverages.get(stockSymbol).add((double) (sum / intervalLength));
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
                if (trendAnalysisValues.get(stockSymbol).size() >= 3) {
//                    System.out.print("TREND ANALYSIS:   ");
//                    for (Double doub: trendAnalysisValues.get(stockSymbol)) {
//                        System.out.print(doub + "    ");
//                    }
//                    System.out.println();
                    double firstAnalysisValue = trendAnalysisValues.get(stockSymbol).get(trendAnalysisValues.get(stockSymbol).size() - 2);
                    double secondAnalysisValue = trendAnalysisValues.get(stockSymbol).get(trendAnalysisValues.get(stockSymbol).size() - 1);
                    if (firstAnalysisValue == 0) {
                        // called the third value, but it is technically the one before the first value
                        // essentially, if zero is involved as an analysis value, we then want to increase our range so that we can look around zero
                        double thirdAnalysisValue = trendAnalysisValues.get(stockSymbol).get(trendAnalysisValues.get(stockSymbol).size() - 3);
                        compareAnalysisValues(stockSymbol, position, currentNumOfShares, thirdAnalysisValue, secondAnalysisValue);
                    } else {
                        compareAnalysisValues(stockSymbol, position, currentNumOfShares, firstAnalysisValue, secondAnalysisValue);
                    }
                }
                break;
            }
        }
    }

    // method created to compare the analysis values depending on what type of trend change we are looking at
    public void compareAnalysisValues(String stockSymbol, Position position, double currentNumOfShares, double analysisValue1, double analysisValue2) {
        if (analysisValue1 > 0 && analysisValue2 < 0) {
            // we want to sell a stock when the trend has switched to downwards
            System.out.println(" jim sold a stock ");
            if (currentNumOfShares > 0) {
                position.sellShares(riskFactor, stockDataPoints.get(stockSymbol).get(stockDataPoints.get(stockSymbol).size() - 1));
            }
        } else if (analysisValue1 < 0 && analysisValue2 > 0) {
            // we want to buy a stock when the trend has switched to upwards
            System.out.println(" jim bought a stock ");
            position.buyShares(riskFactor, stockDataPoints.get(stockSymbol).get(stockDataPoints.get(stockSymbol).size() - 1));
        }
    }

    // method will return the current value of the computer bot's portfolio
    public double getCurrentValue() {
        double sum = 0;
        for (Position position: getAccount().getPositions()) {
            ArrayList<Double> dataList = stockDataPoints.get(position.getStock().getSymbol());
            sum += position.getNumOfShares() * dataList.get(dataList.size() - 1);
        }
        return sum;
    }

}
