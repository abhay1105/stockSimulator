package com.abhay.stockSimGame;


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

}

