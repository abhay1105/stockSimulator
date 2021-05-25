package com.abhay.stockSimGame;


import java.util.ArrayList;
import java.util.Map;

// class will manage a player in the game
public class Player {

    // all variables located here
    private String name;
    private Account account;

    // class constructor
    public Player(String name, double initialBalance) {
        this.name = name;
        this.account = new Account(initialBalance);
    }

    // all getter methods located here
    public String getName() { return name; }
    public Account getAccount() { return account; }

    public double getCurrentValue(Map<String, ArrayList<Double>> entireList) {
        double sum = 0;
        for (Position position: account.getPositions()) {
            ArrayList<Double> dataList = entireList.get(position.getStock().getSymbol());
            sum += position.getNumOfShares() * dataList.get(dataList.size() - 1);
        }
        return sum;
    }

}

