package com.rodenapps.appsalesview.ui;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import com.rodenapps.appsalesview.db.Database;
import com.rodenapps.appsalesview.function.AppleCsvParser;
import com.rodenapps.appsalesview.function.SalesReportDownloader;
import com.rodenapps.appsalesview.model.Item;
import com.rodenapps.appsalesview.model.ItemFilter;

public class ApplicationFrame extends JFrame implements FormObserver {
	TablePanel table;
	PlotPanel plot;
	JTabbedPane tabPane;
	FormPanel formPanel;
	SummaryPanel summaryPanel;
	
	Preferences prefs;
	static Database db = Database.getInstance();
	
	ItemFilter currentFilter;

	public static void importSalesData(File file) throws Exception {
		if (file != null) {
			AppleCsvParser parser = new AppleCsvParser(file);
			Collection<Item> items = parser.getItems();
			db.insertMultipleItems(items);
		}
	}

	public static void resetDatabase() throws SQLException {
		db.dropAllTables();
		db.createTables();		
	}
	
	private File getDataDir() {
		String dir = prefs.get("dir", null);
		return dir == null ? null : new File(dir);
	}
	
	private void setDataDir(File file) throws IOException {
		prefs.put("dir", file.getCanonicalPath());
	}
	
	public ApplicationFrame() throws Exception {
		super("AppSalesView");
		
		prefs = Preferences.userNodeForPackage(this.getClass());
		System.err.println("Importing sales data...");
		importSalesData(getDataDir());
		System.err.println("Done.");
		
		this.setSize(1000, 600);
		this.setLayout(new BorderLayout());
				
		
		plot = new PlotPanel();
		table = new TablePanel();
		summaryPanel = new SummaryPanel();
		
		tabPane = new JTabbedPane();
		tabPane.addTab("Chart", new JScrollPane(plot));
		tabPane.addTab("Table", table);
		
		formPanel = new FormPanel();
		formPanel.addObserver(this);
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split.add(new JScrollPane(formPanel), 0);
		split.add(tabPane, 1);
		split.setDividerLocation(0.4);
		this.add(split, BorderLayout.CENTER);
		this.add(summaryPanel, BorderLayout.SOUTH);
		
		currentFilter = formPanel.getFilter();
				
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}
	
	private static List<Date> datesInInterval(Date date1, Date date2) {
		List<Date> dates = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date1);
		while (cal.getTime().before(date2)) {
			dates.add(cal.getTime());
			cal.add(Calendar.DAY_OF_MONTH, 1);
		}
		dates.add(cal.getTime());
		
		return dates;
	}
	
	private static void addMissingDates(List<Item> items) {
		if (items.size() <= 1)
			return;
		
		Date minDate = new Date(Long.MAX_VALUE);
		Date maxDate = new Date(Long.MIN_VALUE);
		List<Date> dates = new ArrayList<Date>(); 
		for (Item item : items) {
			Date date = item.getDate();
			dates.add(date);
			if (date.before(minDate))
				minDate = date;
			if (date.after(maxDate))
				maxDate = date;
		}
		
		List<Date> missingDates = datesInInterval(minDate, maxDate);
		missingDates.removeAll(dates);
		for (Date date : missingDates) {
			Item item = new Item();
			item.setDate(date);
			item.setUnits(0);
			items.add(item);
		}
		
		Collections.sort(items);
	}

	private void updateForm() {
		try {
			formPanel.updateForm();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateData() {
		try {
			List<Item> items = Database.getInstance().select(currentFilter);
			addMissingDates(items);
			table.setItems(items);
			plot.setItems(items);
			summaryPanel.setItems(items);
			//formPanel.updateForm(); // XXX
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public void filterChanged(ItemFilter filter) {
		currentFilter = filter;
		updateData();
	}
	
	public static void main(String[] args) throws Throwable {
		ApplicationFrame frame = new ApplicationFrame();
//		ApplicationFrame.importSalesData(new File("/tmp/reports"));
	}

	public void folderChanged(File folder) {
		try {
			setDataDir(folder);
			importSalesData(folder);
			updateData();
			updateForm();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void downloadReports(String username, String password) {
		
		try {
			Database db = Database.getInstance();
			
			SalesReportDownloader downloader = new SalesReportDownloader();		
			
			System.out.println("Fetching report days...");
			String[] daysAvailable = downloader.getAvailableDays(username, password);
			System.out.println("" + daysAvailable.length + " days are available.");
			Object[] daysImported = db.querySingleColumn("SELECT DISTINCT date FROM sales");
			
			DateFormat salesDateFormat = downloader.getDateFormat();
			DateFormat dbDateFormat = Database.getDateFormat();
			for (int i = 0; i < daysImported.length; i++) {
				daysImported[i] = salesDateFormat.format(dbDateFormat.parse((String)daysImported[i]));
				System.out.println(daysImported[i]);
			}
			
			List<String> daysToDownload = new ArrayList<String>(Arrays.asList(daysAvailable));
			daysToDownload.removeAll(Arrays.asList(daysImported));
			System.out.println("" + daysToDownload.size() + " new reports.");
			
			for (String day : daysToDownload) {
				System.out.println("Fetching report for day " + day + "...");
				String x[] = downloader.getDailyReport(day);
				String filename = x[0];
				String contents = x[1];

				filename = getDataDir().getAbsolutePath() + File.separator + filename;
				if (!new File(filename).exists()) {
					Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), 
							   Charset.forName("UTF-8")));
					out.write(contents);
					out.close();
					importSalesData(new File(filename));
				}
			}
			
			JOptionPane.showMessageDialog(this, "Done. " + daysToDownload.size() + " report(s) downloaded.");
			
			updateData();
			updateForm();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error");
			e.printStackTrace();
		}
	}
}
