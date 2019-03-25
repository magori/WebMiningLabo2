package ch.hesso;

import java.util.List;

public class Ligne {
    private String invoiceNo;
    private String stockCode;
    private String description;
    private Integer quantity;
    private String customerID;
    private String country;


    public Ligne(List<String> list) {
        this.setCustomerID(list.get(Champs.CustomerID.getIndex()).trim());
        this.setCountry(list.get(Champs.Country.getIndex()).trim());
        this.setDescription(list.get(Champs.Description.getIndex()).trim());
        this.setInvoiceNo(list.get(Champs.InvoiceNo.getIndex()).trim());
        this.setQuantity(Integer.valueOf(list.get(Champs.Quantity.getIndex()).trim()));
        this.setStockCode(list.get(Champs.StockCode.getIndex()).trim().toUpperCase());
    }

    public Ligne setInvoiceNo(final String invoiceNo) {
        this.invoiceNo = invoiceNo;
        return this;
    }


    public Ligne setStockCode(final String stockCode) {
        this.stockCode = stockCode;
        return this;

    }

    public Ligne setDescription(final String description) {
        this.description = description;
        return this;

    }

    public Ligne setQuantity(final Integer quantity) {
        this.quantity = quantity;
        return this;

    }

    public Ligne setCustomerID(final String customerID) {
        this.customerID = customerID;
        return this;

    }

    public Ligne setCountry(final String country) {
        this.country = country;
        return this;

    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public String getStockCode() {
        return stockCode;
    }

    public String getDescription() {
        return description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getCustomerID() {
        return customerID;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return "Ligne{" +
                "invoiceNo='" + invoiceNo + '\'' +
                ", stockCode='" + stockCode + '\'' +
                ", description='" + description + '\'' +
                ", quantity='" + quantity + '\'' +
                ", customerID='" + customerID + '\'' +
                ", country='" + country + '\'' +
                '}';
    }
}
