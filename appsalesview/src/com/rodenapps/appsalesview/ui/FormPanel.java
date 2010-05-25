package com.rodenapps.appsalesview.ui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DateFormatter;

import org.apache.xpath.axes.SelfIteratorNoPredicate;
import org.jfree.ui.action.DowngradeActionMap;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.rodenapps.appsalesview.db.Database;
import com.rodenapps.appsalesview.function.SalesReportDownloader;
import com.rodenapps.appsalesview.model.AppDescription;
import com.rodenapps.appsalesview.model.Item;
import com.rodenapps.appsalesview.model.ItemFilter;
import com.rodenapps.appsalesview.model.ProductType;

public class FormPanel extends JPanel implements ActionListener, DocumentListener, ListSelectionListener {
	JList listAppIds;
	JList listCountries;
	JList listProductTypes;
	JCheckBox chkPaid;
	JButton btnImport;
	JButton btnDownloadReports;
	JFormattedTextField txtBeginDate;
	JFormattedTextField txtEndDate;
	DateFormat dateFormat;
	
	Set<FormObserver> observers = new HashSet<FormObserver>();
	
	public FormPanel() throws SQLException {
		this(null);
	}
	
	public FormPanel(FormObserver fo) throws SQLException {
		if (fo != null)
			addObserver(fo);
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		btnImport = new JButton("Choose Data Folder");
		btnImport.addActionListener(this);
		this.add(btnImport);
		
		btnDownloadReports = new JButton("Download Reports");
		btnDownloadReports.addActionListener(this);
		this.add(btnDownloadReports);

		listAppIds = new JList();
		listAppIds.addListSelectionListener(this);
		this.add(new JScrollPane(listAppIds));
		
		listCountries = new JList();
		listCountries.setLayoutOrientation(JList.VERTICAL_WRAP);
		listCountries.addListSelectionListener(this);
		this.add(new JScrollPane(listCountries));

		listProductTypes = new JList();
		listProductTypes.addListSelectionListener(this);
		this.add(new JScrollPane(listProductTypes));
		
		chkPaid = new JCheckBox("Hide free downloads");
		chkPaid.addActionListener(this);
		this.add(chkPaid);

		txtBeginDate = new JFormattedTextField(new Date(0));
		txtBeginDate.getDocument().addDocumentListener(this);
		this.add(new JLabel("Start Date: "));
		this.add(txtBeginDate);
		
		txtEndDate = new JFormattedTextField(new Date());
		txtEndDate.getDocument().addDocumentListener(this);
		this.add(new JLabel("End Date: "));
		this.add(txtEndDate);
		
		DateFormatter formatter = (DateFormatter)txtBeginDate.getFormatter();
		dateFormat = (DateFormat)formatter.getFormat();		
				
		updateForm();

		listProductTypes.setSelectedIndex(0);
		//listAppIds.setSelectedIndex(0);
		listAppIds.setSelectionInterval(0, listAppIds.getModel().getSize() - 1);
		//listCountries.setSelectedIndex(0);
		listCountries.setSelectionInterval(0, listCountries.getModel().getSize() - 1);
		
		updateForm();
		notifyObservers();
	}

	public void updateForm() throws SQLException {
		// TODO: refactor out call to Database??

		int[] indices;
		
		indices = listAppIds.getSelectedIndices();
		Object[] appIds = Database.getInstance().getAppDescriptions();
		listAppIds.setListData(appIds);
		listAppIds.setSelectedIndices(indices);
		
		indices = listCountries.getSelectedIndices();
		Object[] countries = Database.getInstance().getCountries();
		listCountries.setListData(countries);
		listCountries.setSelectedIndices(indices);
		
		indices = listProductTypes.getSelectedIndices();
		listProductTypes.setListData(Item.getProductTypeList());
		listProductTypes.setSelectedIndices(indices);		
	}


	public ItemFilter getFilter() {
		ItemFilter filter = new ItemFilter();

		filter.countries.clear();
		for (Object o : listCountries.getSelectedValues())
			filter.countries.add((String)o);
		
		filter.appIds.clear();
		for (Object o : listAppIds.getSelectedValues())
			filter.appIds.add(((AppDescription)o).getApp_id());
	
		Date beginDate, endDate;
		try {
			beginDate = dateFormat.parse(txtBeginDate.getText());
		} catch (ParseException e) {
			beginDate = new Date(0);
		}
		try {
			endDate = dateFormat.parse(txtEndDate.getText());
		} catch (ParseException e) {
			endDate = new Date(Long.MAX_VALUE);
		}

		filter.productTypes.clear();
		for (Object o : listProductTypes.getSelectedValues())
			filter.productTypes.add((ProductType)o);
		
		filter.includeFree = !chkPaid.isSelected();
		filter.beginDate = beginDate;
		filter.endDate = endDate;

		return filter;
	}
	
	public void setFilter(ItemFilter filter) {
		
	}
	
	public void notifyObservers() {
		ItemFilter filter = getFilter();
		callObservers(filter);
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnImport) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int ret = fc.showOpenDialog(this);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				for (FormObserver fo : observers) {
					fo.folderChanged(file);
				}
			}
		}
		else if (e.getSource() == btnDownloadReports) {
			String username = JOptionPane.showInputDialog("Username");
			String password = JOptionPane.showInputDialog("Password");
			for (FormObserver o : observers) {
				o.downloadReports(username, password);
			}
		}
		else {
			notifyObservers();
		}
	}
	
	public void addObserver(FormObserver observer) {
		observers.add(observer);
	}
	
	public void removeObserver(FormObserver observer) {
		observers.remove(observer);
	}
	
	private void callObservers(ItemFilter filter) {
		for (FormObserver observer : observers) {
			observer.filterChanged(filter);
		}
	}

	private void changedDate(DocumentEvent e) {
		notifyObservers();
	}
	
	public void changedUpdate(DocumentEvent e) {
		changedDate(e);
	}

	public void insertUpdate(DocumentEvent e) {
		changedDate(e);
	}

	public void removeUpdate(DocumentEvent e) {
		changedDate(e);
	}

	public void valueChanged(ListSelectionEvent e) {
		notifyObservers();
	}

}
