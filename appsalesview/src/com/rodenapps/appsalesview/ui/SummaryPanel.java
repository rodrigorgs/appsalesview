package com.rodenapps.appsalesview.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.rodenapps.appsalesview.model.Item;

public class SummaryPanel extends JPanel {
	List<Item> itemList;
	int sumUnits = 0;
	double sumIncome = 0;
	JLabel label;
	
	public SummaryPanel() {
		super();
		
		itemList = new ArrayList<Item>();
		
		createGUI();
		update();
	}
	
	public void setItems(List<Item> items) {
		this.itemList = items;
		update();
	}
	
	private void createGUI() {
		label = new JLabel();
		this.add(label);
	}
	
	private void update() {
		sumUnits = 0;
		sumIncome = 0;
		Date maxDate = new Date(0);
		for (Item item : itemList) {
			sumIncome += item.getIncome();
			sumUnits += item.getUnits();
			
			Date date = item.getDate();
			if (date.after(maxDate))
				maxDate = date;
		}

		int sumUnitsLastDay = 0;
		double sumIncomeLastDay = 0;
		if (maxDate != null) {
			for (Item item : itemList) {
				if (item.getDate().equals(maxDate)) {
					sumIncomeLastDay += item.getIncome();
					sumUnitsLastDay += item.getUnits();
				}			
			}
		}
		
		label.setText("Total: " + sumUnits + " units (approx. US$ " + String.format("%.2f", sumIncome) + "). " +
				"Last date: " + sumUnitsLastDay + " units (approx. US$ " + String.format("%.2f", sumIncomeLastDay) + ").");
	}
}
