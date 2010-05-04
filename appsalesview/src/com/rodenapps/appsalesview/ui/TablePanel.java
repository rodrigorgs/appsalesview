package com.rodenapps.appsalesview.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.rodenapps.appsalesview.model.Item;

public class TablePanel extends JPanel implements TableModel, ActionListener {
	private static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	private List<Item> itemList;
	private JTable table;
	private JButton btnExport;
	
	public TablePanel() {
		super();
		
		itemList = new ArrayList<Item>();
		
		this.setLayout(new BorderLayout());
		
		table = new JTable(this);
		this.add(new JScrollPane(table), BorderLayout.CENTER);
		
		TableColumn col = table.getColumnModel().getColumn(4);
		col.setCellRenderer(new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				Component myself = super.getTableCellRendererComponent(table,
						value, isSelected, hasFocus, row, column);
				setHorizontalAlignment(SwingConstants.RIGHT);
				return myself;
			}
		});
		col = table.getColumnModel().getColumn(5);
		col.setCellRenderer(new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				Component myself = super.getTableCellRendererComponent(table,
						value, isSelected, hasFocus, row, column);
				setHorizontalAlignment(SwingConstants.CENTER);
				return myself;
			}
		});
		
		btnExport = new JButton("Export CSV");
		btnExport.addActionListener(this);
		this.add(btnExport, BorderLayout.SOUTH);
	}
	
	public void setItems(List<Item> items) {
		this.itemList.clear();
		this.itemList.addAll(items);
		
		table.tableChanged(new TableModelEvent(this) {
			
		});
	}
	
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	private static String[] columns = {"Date", "AppId", "Country", "Units", "US$", "Type"};
	
	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}
	
	public Class<?> getColumnClass(int column) {
		return String.class;
	}

	public int getRowCount() {
		return itemList.size();
	}

	private static Object coalesce(Object a, Object b) {
		return a == null ? b : a;
	}
	
	public Object getValueAt(int row, int column) {
		Item item = itemList.get(row);
		switch (column) {
		case 0: return "" + item.getDate() != null ? dateFormat.format(item.getDate()) : "";
		case 1: return "" + coalesce(item.getAppId(), "");
		case 2: return "" + coalesce(item.getCountry(), "");
		case 3: return "" + coalesce(item.getUnits(), "");
		case 4: return "" + (item.getIncome() == null ? "" : String.format("%.2f", item.getIncome()));
		case 5: return "" + coalesce(item.getProductType(), "");
		default: return "";
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
	}

	public void actionPerformed(ActionEvent e) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
		int ret = fileChooser.showSaveDialog(this);
		if (ret == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			try {
				exportCsv(file);
				JOptionPane.showMessageDialog(this, "Done!");
			} catch (IOException e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this, "Could not write to file.");
			}
		}
	}
		
	private void exportCsv(File file) throws IOException {
		String sep = "\t";
		
		FileWriter fw = new FileWriter(file);
		
		int nrows = getRowCount();
		int ncols = getColumnCount();
		
		for (int j = 0; j < ncols; j++) {
			fw.write(getColumnName(j));
			if (j < ncols - 1)
				fw.write(sep);
		}
		fw.write("\n");
		
		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				fw.write(getValueAt(i, j).toString());
				if (j < ncols - 1)
					fw.write(sep);
			}
			if (i < nrows - 1)
				fw.write("\n");
		}
		fw.close();
	}
}
