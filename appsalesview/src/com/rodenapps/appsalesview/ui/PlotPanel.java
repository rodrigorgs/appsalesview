package com.rodenapps.appsalesview.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.rodenapps.appsalesview.model.Item;

public class PlotPanel extends JPanel {
	List<Item> itemList;
	
	public void setItems(List<Item> items) {
		this.itemList = items;
		
		this.setLayout(new BorderLayout());
		
		JFreeChart chart = createChart(itemList);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setFillZoomRectangle(true);
		this.removeAll();
		this.add(chartPanel, BorderLayout.CENTER);
		
		this.updateUI();
	}
		
	public JFreeChart createChart(List<Item> items) {
		XYDataset dataset = createDataset(items, false);
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Sales", "Date", "Units", dataset, true, true, false);
		
		if (true) {
	 		XYPlot plot = chart.getXYPlot();
			NumberAxis axis = (NumberAxis)plot.getRangeAxis();
			axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			
			XYItemRenderer renderer = plot.getRenderer();
			//renderer.setStroke(stroke);
			renderer.setSeriesStroke(0, new BasicStroke(2));
			renderer.setSeriesStroke(1, new BasicStroke(0.5f));
		}
        
		return chart;
	}
	
	private XYDataset createDataset(List<Item> items, boolean income) {
		TimeSeries s = new TimeSeries("Units");
		TimeSeries s2 = new TimeSeries("Money");
		for (Item item : items) {
			s.add(new Day(item.getDate()), item.getUnits());
			s2.add(new Day(item.getDate()), item.getIncome());
		}
		
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s);
        dataset.addSeries(s2);
        return dataset;
	}
}
