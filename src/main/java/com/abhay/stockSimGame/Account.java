package com.abhay.stockSimGame;

import java.util.ArrayList;

// class will manage all of a player's positions
public class Account {

    // all variables located here
    private ArrayList<Position> positions;
    private double initialBalance, investedAmount, profitOrLoss, currentValue, currentBalance;

    // class constructor
    public Account(double initialBalance) {
        this.initialBalance = initialBalance;
        positions = new ArrayList<>();
        investedAmount = 0;
        profitOrLoss = 0;
        currentValue = 0;
        currentBalance = initialBalance;
    }

    // all getter methods located here
    public ArrayList<Position> getPositions() { return positions; }
    public double getInitialBalance() { return initialBalance; }

    public double getInvestedAmount() {
        double total = 0;
        for (Position position: positions) {
            total += position.getMoneyInvested();
        }
        investedAmount = total;
        return total;
    }

    public double getProfitOrLoss(double sharePrice) {
        double total = 0;
        for (Position position: positions) {
            total += position.getProfit(sharePrice);
        }
        profitOrLoss = total;
        return total;
    }

    public double getCurrentValue(double sharePrice) {
        double total = 0;
        for (Position position: positions) {
            total += position.getCurrentValue(sharePrice);
        }
        currentValue = total;
        return total;
    }

    public double getCurrentBalance() { return currentBalance; }

    // all setter methods located here
    public void setInitialBalance(double balance) { initialBalance = balance; }
    public void setCurrentBalance(double balance) { currentBalance = balance; }

    // both methods below will only be used when buying or selling shares in a position

    // method to subtract money from our account's current balance
    public void subtractMoney(double money) {
        currentBalance -= money;
    }

    // method to add money to our account's current balance
    public void addMoney(double money) {
        currentBalance += money;
    }

}

