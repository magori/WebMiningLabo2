package ch.hesso;

public enum Champs {
    InvoiceNo(0),
    StockCode(1),
    Description(2),
    Quantity(3),
    InvoiceDate(4),
    UnitPrice(5),
    CustomerID(6),
    Country(7);

    private int index;

    Champs(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
