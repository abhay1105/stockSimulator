package com.abhay.stockSimGame;


// class will represent one position in a stock
public class Position {

    // all variables located here
    private Stock stock;
    private int numOfShares;
    private double moneyInvested;
    private Account account;

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
        moneyInvested += sharePrice * numOfShares;
        account.subtractMoney(sharePrice * numOfShares);
        System.out.println("money invested   " + moneyInvested + "    " + this.numOfShares);
    }

    // method to sell shares (aka decrease position in stock)
    public void sellShares(int numOfShares, double sharePrice) {
        this.numOfShares -= numOfShares;
        account.addMoney(sharePrice * numOfShares);
        System.out.println("money invested   " + moneyInvested + "    " + numOfShares);
    }

}

