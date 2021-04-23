package com.abhay.stockSimGame;

import java.util.ArrayList;

// class will manage all of the stocks that the players are allowed to invest in
public class Market {

    // all variables located here
    private ArrayList<Stock> stocks;

    // class constructor
    public Market(ArrayList<Stock> stocks) {
        this.stocks = stocks;
    }

    // all getter methods located here
    public ArrayList<Stock> getStocks() { return stocks; }


}

