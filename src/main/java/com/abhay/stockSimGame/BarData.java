package com.abhay.stockSimGame;

// class will help in storing our individual data points
public class BarData {

    // all variables located here
    // these variables will simply assist in making sure we have all of the data from the API call stored, regardless
    // of if we choose to use it
    private Stock stock;
    private String date;
    private double open, high, low, close, adjustedClose, volume, dividendAmount, splitCoefficient;

    // class constructor
    public BarData(Stock stock, String date, double open, double high, double low, double close, double adjustedClose, double volume,
                   double dividendAmount, double splitCoefficient) {
        this.stock = stock;
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.adjustedClose = adjustedClose;
        this.volume = volume;
        this.dividendAmount = dividendAmount;
        this.splitCoefficient = splitCoefficient;
    }

    // by changing the toString() method it will be easier for us to view any information quicker
    @Override
    public String toString() {
        return stock.getSymbol() + "   D:" + date + "   O:" + open + "   H:" + high + "   L:" + low + "   C:" +
                close + "   AC:" + adjustedClose + "   V:" + volume + "   DA:" + dividendAmount + "   SC:" + splitCoefficient;
    }


    // all getter methods located here
    public Stock getStock() { return stock; }
    public String getDate() { return date; }
    public double getOpen() { return open; }
    public double getHigh() { return high; }
    public double getLow() { return low; }
    public double getClose() { return close; }
    public double getAdjustedClose() { return adjustedClose; }
    public double getVolume() { return volume; }
    public double getDividendAmount() { return dividendAmount; }
    public double getSplitCoefficient() { return splitCoefficient; }


}
