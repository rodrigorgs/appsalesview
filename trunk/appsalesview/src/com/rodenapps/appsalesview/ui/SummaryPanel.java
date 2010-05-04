package com.rodenapps.appsalesview.ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
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
		for (Item item : itemList) {
			sumUnits += item.getUnits();
		}
		
		sumIncome = 0;
		for (Item item : itemList) {
			sumIncome += item.getIncome();
		}		
		
		label.setText("Total: " + sumUnits + " units (approx. US$ " + String.format("%.2f", sumIncome) + ")");
	}
}
