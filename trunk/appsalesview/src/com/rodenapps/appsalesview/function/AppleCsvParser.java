package com.rodenapps.appsalesview.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.rodenapps.appsalesview.model.Item;

// TODO: Use a generic class, such as Reader?
public class AppleCsvParser {
	private File currentFile;
	private static DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	public AppleCsvParser(File file) {
		this.currentFile = file;
	}
	
	public Collection<Item> getItems() throws Exception {
		if (currentFile.isFile())
			return getItems(currentFile);
		else if (currentFile.isDirectory()) {
			File[] fileList = currentFile.listFiles();
			List<Item> items = new ArrayList<Item>();
			for (File f : fileList) {
				items.addAll(getItems(f));
			}
			return items;
		}
		else {
			return new ArrayList<Item>();
		}		
	}
	
	public Collection<Item> getItems(File file) throws Exception {
		List<Item> list = new ArrayList<Item>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		String line = br.readLine();
		if (line == null || !line.startsWith("Provider"))
			return list;
		
		while ((line = br.readLine()) != null) {
			Item item = new Item();
			String[] values = line.split("\t");
			if (!values[11].equals(values[12]))
				return new ArrayList<Item>(); // this isn't a daily sales report
			
			item.setTitle(values[6]);
			item.setUnits(Integer.parseInt(values[9]));
			item.setPrice(Float.parseFloat(values[10]));
			item.setDate(dateFormat.parse(values[11]));
			item.setCountry(values[14]);
			item.setCurrency(values[15]);

			item.setDollars(CurrencyConversion.convertToDollar(item.getCurrency(), item.getPrice()));
			
			item.setAppId(Integer.parseInt(values[19]));
			if (item.getUnits() < 0)
				item.setProductType(Item.TYPE_REFUND);
			else if (values[25].trim().length() != 0)
				item.setProductType(Item.TYPE_PROMO);
			else if (values[8].trim().equals("7"))
				item.setProductType(Item.TYPE_UPDATE);
			else
				item.setProductType(Item.TYPE_SALE);
			
			list.add(item);
		}
		
		br.close();
		
		return list;
	}
}

/*
00 Provider
01 Provider Country
02 Vendor Identifier
03 UPC
04 ISRC
05 Artist / Show
06 Title / Episode / Season
07 Label/Studio/Network
08 Product Type Identifier
09 Units
10 Royalty Price
11 Begin Date
12 End Date
13 Customer Currency
14 Country Code
15 Royalty Currency
16 Preorder
17 Season Pass
18 ISAN
19 Apple Identifier
20 Customer Price
21 CMA
22 Asset/Content Flavor
23 Vendor Offer Code
24 Grid
25 Promo Code
26 Parent Identifier
*/
