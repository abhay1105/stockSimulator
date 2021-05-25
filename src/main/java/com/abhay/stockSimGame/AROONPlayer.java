package com.abhay.stockSimGame;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// class will manage a subclass of player that trades using aroon numbers
public class AROONPlayer extends Player {

    // all variables located here
    private Map<String, ArrayList<Double>> stockDataPoints = new HashMap<>();
    private Map<String, ArrayList<Double>> aroonUp = new HashMap<>();
    private Map<String, ArrayList<Double>> aroonDown = new HashMap<>();
    private Map<String, ArrayList<Boolean>> isBullishMarket = new HashMap<>();
    private int intervalLength = 25;
    private double high, low;
    private int highDistance, lowDistance;
    private double riskFactor = 5;

    // class constructor
    public AROONPlayer() {
        super("TraderKimbo(AROON)", 1000);
    }

    // method to update the data lists
    public void updateDataLists(String stockSymbol, double dataPoint) {
        if (stockDataPoints.keySet().contains(stockSymbol)) {
            stockDataPoints.get(stockSymbol).add(dataPoint);
            updateAroonNumbers(stockSymbol);
        } else {
            stockDataPoints.put(stockSymbol, new ArrayList<>());
            aroonUp.put(stockSymbol, new ArrayList<>());
            aroonDown.put(stockSymbol, new ArrayList<>());
            isBullishMarket.put(stockSymbol, new ArrayList<>());
        }
    }

    // method to update the moving averages every time a data point is added to the list
    public void updateAroonNumbers(String stockSymbol) {
        ArrayList<Double> dataList = stockDataPoints.get(stockSymbol);
        if (dataList.size() >= intervalLength) {
            checkForHighAndLow(dataList);
            calculateAroonNumbers(stockSymbol);
        }
    }

    // method to check for a high and a low in the last interval
    public void checkForHighAndLow(ArrayList<Double> dataList) {
        double newHigh = Double.MIN_VALUE;
        double newLow = Double.MAX_VALUE;
        for (int i = dataList.size() - intervalLength;i < dataList.size();i++) {
            if (dataList.get(i) >= newHigh) {
                newHigh = dataList.get(i);
                highDistance = 0;
            } else {
                highDistance++;
            }
            if (dataList.get(i) <= newLow) {
                newLow = dataList.get(i);
                lowDistance = 0;
            } else {
                lowDistance++;
            }
        }
        high = newHigh;
        low = newLow;
    }

    // method to actually calculate the aroon numbers now that the high, low, highDistance, and lowDistance have been calculated
    public void calculateAroonNumbers(String stockSymbol) {
        aroonUp.get(stockSymbol).add((((double) (intervalLength - highDistance) / intervalLength) * 100));
        aroonDown.get(stockSymbol).add((((double) (intervalLength - lowDistance) / intervalLength) * 100));
        compareAroonUpAndDown(stockSymbol);
    }

    // method will determine behavior of market depending on comparison of aroon up and aroon down
    public void compareAroonUpAndDown(String stockSymbol) {
        ArrayList<Double> aroonUpList = aroonUp.get(stockSymbol);
        ArrayList<Double> aroonDownList = aroonDown.get(stockSymbol);
        // if aroon up is greater than aroon down, then the market is bullish
        // if aroon up is less than aroon down, then the market is bearish
        isBullishMarket.get(stockSymbol).add(aroonUpList.get(aroonUpList.size() - 1) > aroonDownList.get(aroonDownList.size() - 1));
        buyOrSellFromBehavior(stockSymbol);
    }

    // method created to compare the switch from bullish to bearish and vice versa
    public void buyOrSellFromBehavior(String stockSymbol) {
        for (Position position: getAccount().getPositions()) {
            if (position.getStock().getSymbol().equals(stockSymbol) && isBullishMarket.get(stockSymbol).size() >= 2) {
                boolean analysisValue1 = isBullishMarket.get(stockSymbol).get(isBullishMarket.get(stockSymbol).size() - 2);
                boolean analysisValue2 = isBullishMarket.get(stockSymbol).get(isBullishMarket.get(stockSymbol).size() - 1);
                if (analysisValue1 && !analysisValue2) {
                    // we want to sell a stock when the trend has switched to downwards
//                    System.out.println(" kimbo sold a stock ");
                    if (position.getNumOfShares() > 0) {
                        position.sellShares(riskFactor, stockDataPoints.get(stockSymbol).get(stockDataPoints.get(stockSymbol).size() - 1));
                    }
                } else if (!analysisValue1 && analysisValue2) {
                    // we want to buy a stock when the trend has switched to upwards
//                    System.out.println(" kimbo bought a stock ");
                    position.buyShares(riskFactor, stockDataPoints.get(stockSymbol).get(stockDataPoints.get(stockSymbol).size() - 1));
                }
                break;
            }
        }
    }

    // method will return the current value of the computer bot's portfolio
    public double getCurrentValue(Map<String, ArrayList<Double>> entireList) {
        double sum = 0;
        for (Position position: getAccount().getPositions()) {
            ArrayList<Double> dataList = stockDataPoints.get(position.getStock().getSymbol());
            sum += position.getNumOfShares() * dataList.get(dataList.size() - 1);
        }
        return sum;
    }


}
