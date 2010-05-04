package com.rodenapps.appsalesview.model;

public class ProductType {
	int code;
	String label;
	
	public int getCode() {
		return code;
	}
	public ProductType(int code, String label) {
		super();
		this.code = code;
		this.label = label;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String toString() {
		return label;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ProductType) {
			ProductType other = (ProductType)obj;
			return code == other.code;
		}
		else
			return false;
	}
}
