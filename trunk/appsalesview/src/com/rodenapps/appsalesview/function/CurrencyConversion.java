package com.rodenapps.appsalesview.function;

import java.util.HashMap;

public class CurrencyConversion {
	public final static HashMap<String, Double> map = new HashMap<String, Double>() {{
		put("USD", 1.0);
		put("CAD", 1.0);
		put("GBP", 0.70 / 0.36);
		put("EUR", 0.70 / 0.48);
		put("AUD", 0.70 / 0.76);
		put("JPY", 0.70 / 81.0);
	}};
	
	public static double convertToDollar(String currency, double amount) {
		Double factor = map.get(currency);
		if (factor == null)
			factor = 1.0;
		return amount * factor;
	}
}
