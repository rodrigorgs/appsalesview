package com.rodenapps.appsalesview.db;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import com.rodenapps.appsalesview.model.AppDescription;
import com.rodenapps.appsalesview.model.Item;
import com.rodenapps.appsalesview.model.ItemFilter;
import com.rodenapps.appsalesview.model.ProductType;
import com.rodenapps.appsalesview.util.Util;

public class Database extends BaseDAO {
	private static Database instance = null;
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
	public static DateFormat getDateFormat() {
		return dateFormat;
	}

	private Database() {
	    try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:AppSalesView.db");
			createTables();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Database getInstance() {
		if (instance == null)
			instance = new Database();
		return instance;
	}
	
	private void auxCreateTable(String name, boolean temp) throws SQLException {
		String query = " CREATE " + (temp ? "TEMPORARY" : "") + 
		" TABLE IF NOT EXISTS " + name + " (" +
		" app_id LONG REFERENCES app(app_id)," +
		" units INTEGER," +
		" price DECIMAL," +
		" dollars DECIMAL, " +
		" date DATE," +
		" country VARCHAR(2)," +
		" currency DECIMAL," +
		" prod_type INTEGER" +
		")";
		connection.createStatement().executeUpdate(query);		
	}
	
	public void createTables() throws SQLException {
		auxCreateTable("sales", false);
		auxCreateTable("sales_temp", true);
		
		connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS app (" +
				"app_id LONG," +
				"title TEXT)");
		
		connection.createStatement().executeUpdate(
				"CREATE UNIQUE INDEX IF NOT EXISTS idx_app_app_id ON app(app_id)");
		connection.createStatement().executeUpdate(
				"CREATE INDEX IF NOT EXISTS idx_app_title ON app(title)");

	}
	
	private void setStmtValues(PreparedStatement stmt, Item item) throws SQLException {
		stmt.setLong(1, item.getAppId());
		stmt.setInt(2, item.getUnits());
		stmt.setDouble(3, item.getPrice());
		stmt.setString(4, dateFormat.format(item.getDate()));
		stmt.setString(5, item.getCountry());
		stmt.setString(6, item.getCurrency());
		stmt.setInt(7, item.getProductType().getCode());
		stmt.setDouble(8, item.getDollars());
	}
	
	public void insertMultipleItems(Collection<Item> items) throws SQLException {
		insertOrUpdateApps(items);
		
//		connection.setAutoCommit(false);
		
		connection.createStatement().execute("DELETE FROM sales_temp");
		
		String query = " INSERT INTO sales_temp (app_id, units, price, date," +
		" country, currency, prod_type, dollars) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement stmt = connection.prepareStatement(query);
		for (Item item : items) {
			setStmtValues(stmt, item);
			stmt.addBatch();
		}
		
		stmt.executeBatch();
		
		connection.createStatement().execute("INSERT INTO sales " +
				"SELECT * FROM sales_temp EXCEPT SELECT * FROM sales");
		
//		connection.commit();
	}	
		
	public static Item itemFromMap(Map map) {
		Item item = new Item();
		if (map.get("date") == null)
			return null;
		
		item.setTitle((String)map.get("title"));
		item.setUnits((Integer)map.get("units"));
		
		Object o = map.get("price");
		if (o instanceof Double)
			item.setPrice((Double)o);
		else if (o instanceof Integer)
			item.setPrice(0.0 + (Integer)o);

		o = map.get("income");
		if (o instanceof Double)
			item.setIncome((Double)o);
		else if (o instanceof Integer)
			item.setIncome(0.0 + (Integer)o);
		
		o = map.get("dollars");
		if (o instanceof Double)
			item.setDollars((Double)o);
		else if (o instanceof Integer)
			item.setDollars(0.0 + (Integer)o);

		try {
			o = map.get("date");
			if (o != null && o instanceof String)
			item.setDate(dateFormat.parse((String)o));
		} catch (ParseException e) {}
		item.setCountry((String)map.get("country"));
		item.setCurrency((String)map.get("currency"));
		
		o = map.get("app_id");
		if (o instanceof Integer)
		item.setAppId((Integer)o);
		
		o = map.get("prod_type");
		if (o instanceof Integer)
			item.setProductType((Integer)o);
		
		return item;
	}

	private void insertOrUpdateApps(Collection<Item> items) throws SQLException {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		for (Item item : items) {
			map.put(item.getAppId(), item.getTitle());
		}
		
		for (Integer id : map.keySet()) {
			if (queryResultsEmpty("SELECT * FROM app WHERE app_id = " + id)) {
				PreparedStatement stmt = connection.prepareStatement("INSERT INTO app (app_id, title) VALUES (?, ?)");
				stmt.setInt(1, id);
				stmt.setString(2, map.get(id));
				executeUpdate(stmt);
				stmt.close();
			}
			else {
				PreparedStatement stmt = connection.prepareStatement("UPDATE app SET title = ? WHERE app_id = ?");
//				System.out.println(id + " " + map.get(id));
				stmt.setString(1, map.get(id));
				stmt.setInt(2, id);
				executeUpdate(stmt);
				stmt.close();				
			}
		}
	}
		
	public void dumpTable() throws SQLException {
		String query = "SELECT * FROM sales" +
				" ORDER BY date, app_id, country";
		
		List results = queryResults(query);
		for (Object o : results) {
			Map map = (Map)o;
			Item item = itemFromMap(map);
			System.err.println(item);
		}
	}
	
	public static void dumpItems(Collection<Item> items) {
		for (Item item : items) {
			System.err.println(item);
		}
	}

	public void dropAllTables() throws SQLException {
		Statement stmt = connection.createStatement();
		stmt.execute("DROP TABLE IF EXISTS sales");
		stmt.execute("DROP TABLE IF EXISTS sales_temp");
	}
	
	public Object[] getCountries() throws SQLException {
		return querySingleColumn("SELECT DISTINCT country FROM sales ORDER BY country");
	}
	
	public Object[] getAppIds() throws SQLException {
		return querySingleColumn("SELECT DISTINCT app_id FROM sales ORDER BY app_id");
	}
	
	public AppDescription[] getAppDescriptions() throws SQLException {
		List<AppDescription> descriptions = new ArrayList<AppDescription>();
		String query = "SELECT DISTINCT app.app_id, title" +
				" FROM sales" +
				" INNER JOIN app ON app.app_id = sales.app_id" +
				" ORDER BY title";
		
		List results = queryResults(query);
		
		for (Object o : results) {
			Map map = (Map)o;
			AppDescription desc = new AppDescription((Integer)map.get("app_id"), (String)map.get("title"));
			descriptions.add(desc);
		}
	
		return descriptions.toArray(new AppDescription[0]);
	}
	
	private static String sqlDate(Date date) {
		return "'" + dateFormat.format(date) + "'";
	}
	
	private String sqlInClause(List<?> list) {
		StringBuilder s = new StringBuilder();
		s.append("(");
		
		ListIterator<?> iter = list.listIterator();
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o instanceof String)
				s.append("'" + o + "'");
			else if (o instanceof ProductType)
				s.append(((ProductType)o).getCode());
			else
				s.append(o);
			if (iter.hasNext())
				s.append(",");
		}
		
		s.append(")");
		return s.toString();
	}
	
	public List<Item> select(ItemFilter filter) throws SQLException {
		List<Item> list = new ArrayList<Item>();
		
		if (filter.appIds.isEmpty() || filter.countries.isEmpty())
			return list;
		
		String query;
		
		query = "SELECT date, price, currency, ";
		query += filter.currencies.size() != 1 && !filter.separateByCurrency ? "'' AS currency, " : "currency, ";
		query += filter.appIds.size() != 1 && !filter.separateByAppId ? "'MANY' as app_id, " : "app_id, ";
		query += filter.countries.size() != 1 && !filter.separateByCountry ? "'MANY' AS country, " : "country, ";
		query += filter.productTypes.size() != 1 && !filter.separateByProductType ? "'MANY' AS prod_type, " : "prod_type, ";
		query += "   SUM(units) AS units, 0.0 + SUM(units * dollars) AS income " +
				" FROM sales " +
				" WHERE 1=1";
		
		if (filter.appIds.size() > 0)
			query += " AND app_id IN " + sqlInClause(filter.appIds);
		if (filter.countries.size() > 0)
			query += " AND country IN " + sqlInClause(filter.countries);
		if (filter.currencies.size() > 0)
			query += " AND currency IN " + sqlInClause(filter.currencies);
		if (filter.productTypes.size() > 0)
			query += " AND prod_type IN " + sqlInClause(filter.productTypes);
		
		if (!filter.includeFree)
		 	query += " AND price > 0";
		
		if (filter.beginDate != null)
			query += " AND date >= " + sqlDate(filter.beginDate);
		if (filter.endDate != null)
			query += " AND date <= " + sqlDate(filter.endDate);
		
		query += " GROUP BY date";
		if (filter.separateByAppId)
			query += ", app_id ";			
		if (filter.separateByCountry)
			query += ", country ";
		if (filter.separateByCurrency)
			query += ", currency ";
		if (filter.separateByProductType)
			query += ", prod_type ";
		
		query += " ORDER BY date, app_id, country, prod_type";
		
		List results = queryResults(query);
		for (Object o : results) {
			Map map = (Map)o;
			Item item = itemFromMap(map);
			if (item != null)
				list.add(item);
		}

//		System.out.println(filter);
//		System.out.println(query);
		
		return list;
	}
}
