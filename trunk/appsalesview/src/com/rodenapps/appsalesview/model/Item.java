package com.rodenapps.appsalesview.model;

import java.util.Date;
import java.util.Map;

public class Item implements Comparable<Item> {
	public static final ProductType TYPE_SALE = new ProductType(0, "Sale");
	public static final ProductType TYPE_UPDATE = new ProductType(1, "Update");
	public static final ProductType TYPE_PROMO = new ProductType(2, "Promo Code");
	public static final ProductType TYPE_REFUND = new ProductType(3, "Refund");
	
	String title;
	int units;
	double price;
	double dollars;
	Double income;
	Date date;
	String country;
	String currency;
	Integer appId;
	ProductType productType = null;

	public double getDollars() {
		return roundTwoDecimals(dollars);
	}

	public void setDollars(double dollars) {
		this.dollars = dollars;
	}

	public ProductType getProductType() {
		return productType;
	}

	public void setProductType(ProductType productType) {
		this.productType = productType;
	}
	
	public void setProductType(int productTypeCode) {
		switch (productTypeCode) {
		case 0: productType = TYPE_SALE; break;
		case 1: productType = TYPE_UPDATE; break;
		case 2: productType = TYPE_PROMO; break;
		case 3: productType = TYPE_REFUND; break;
		}
	}
	
	public static Object[] getProductTypeList() {
		return new Object[] { TYPE_SALE, TYPE_UPDATE, TYPE_PROMO, TYPE_REFUND };
	}

	public void setIncome(Double theIncome) {
		income = theIncome;
	}
	
	public Double getIncome() {
		if (income == null)
			return roundTwoDecimals(units * price);
		else
			return roundTwoDecimals(income);
	}


	public Item() {
	}
	
	public String toString() {
		String strDate = date.getDate() + "/" + (1 + date.getMonth()) + "/" + (1900 + date.getYear());
		return strDate + " " + appId + " " + country + " " + units + " " + price + " " + productType;  
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getUnits() {
		return units;
	}
	public void setUnits(int units) {
		this.units = units;
	}
	
	static double roundTwoDecimals(double d) {
		return Math.round(d * 100) / 100.0;
	}
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = roundTwoDecimals(price);
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public Integer getAppId() {
		return appId;
	}
	public void setAppId(Integer appleIdentifier) {
		this.appId = appleIdentifier;
	}

	public int compareTo(Item o) {
		int compareDate = this.getDate().compareTo(o.getDate());
		if (compareDate != 0)
			return compareDate;
		else {
			return this.getAppId() - o.getAppId();
		}
	}

//	public String getProductTypeString() {
//		switch (productType) {
//		case TYPE_PROMO: return "Promo Code";
//		case TYPE_REFUND: return "Refund";
//		case TYPE_SALE: return "Sale";
//		case TYPE_UPDATE: return "Update";
//		default: return "";
//		}
//	}

	// pk: appleIdentifier, date, countryCode, royalty price
}