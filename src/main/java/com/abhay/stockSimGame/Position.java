package com.abhay.stockSimGame;


import java.util.ArrayList;

// class will represent one position in a stock
public class Position {

    // all variables located here
    private Stock stock;
    private int numOfShares;
    private double moneyInvested;
    private Account account;
    private ArrayList<Double> amountOfSharePricesBuyList = new ArrayList<>();

    // class constructor
    public Position(Account account, Stock stock) {
        this.account = account;
        this.stock = stock;
        numOfShares = 0;
        moneyInvested = 0;
    }

    // all getter methods located here
    public Account getAccount() { return account; }
    public Stock getStock() { return stock; }
    public int getNumOfShares() { return this.numOfShares; }
    public double getMoneyInvested() { return moneyInvested; }
    public double getCurrentValue(double sharePrice) { return sharePrice * numOfShares; }
    public double getProfit(double sharePrice) { return getCurrentValue(sharePrice) - getMoneyInvested(); }

    // method to buy shares (aka increase position in stock)
    public void buyShares(double numOfShares, double sharePrice) {
        this.numOfShares += numOfShares;
        for (int i = 0;i < numOfShares;i++) {
            amountOfSharePricesBuyList.add(sharePrice);
        }
        calculateAmountInvested();
        account.subtractMoney(sharePrice * numOfShares);
    }

    // method to sell shares (aka decrease position in stock)
    public void sellShares(double numOfShares, double sharePrice) {
        this.numOfShares -= numOfShares;
        for (int i = 0;i < numOfShares;i++) {
            if (amountOfSharePricesBuyList.size() != 0) {
                amountOfSharePricesBuyList.remove(0);
            }
        }
        calculateAmountInvested();
        account.addMoney(sharePrice * numOfShares);
    }

    // method to calculate the amount of money invested into a specific stock
    public void calculateAmountInvested() {
        moneyInvested = 0;
        for (Double sharePrice: amountOfSharePricesBuyList) {
            moneyInvested += sharePrice;
        }
    }

}

