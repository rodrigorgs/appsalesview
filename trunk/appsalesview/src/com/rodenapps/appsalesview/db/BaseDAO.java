package com.rodenapps.appsalesview.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BaseDAO {
	public class DBException extends java.lang.RuntimeException {
		public static final String NENHUM_REGISTRO = "A consulta nao retornou nenhum registro.";
		public static final String MAIS_DE_UM_REGISTRO = "A consulta retornou mais de um registro.";
		
		private Object extraInfo = null;
		
		public DBException(String s) {
			super(s);
		}
		
		public DBException(String s, Object extraInfo) {
			super(s);
			this.extraInfo = extraInfo;
		}

		public Object getExtraInfo() {
			return extraInfo;
		}

		public void setExtraInfo(Object extraInfo) {
			this.extraInfo = extraInfo;
		}
	}

	
	protected Connection connection;
	
	public int executeUpdate(String query) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(query);
		int n = executeUpdate(stmt);
		stmt.close();
		return n;
	}
	
	public int executeUpdate(PreparedStatement stmt) throws SQLException {
		int count = stmt.executeUpdate();
		return count;
	}

	public List queryResults(PreparedStatement stmt) throws SQLException {
		List results = new ArrayList();
		
		ResultSet rs = stmt.executeQuery();
		ResultSetMetaData metadata = rs.getMetaData();
		while (rs.next()) {
			Map row = new HashMap();
			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				String name = metadata.getColumnName(i);
				Object value = rs.getObject(i);
				row.put(name, value);
			}
			results.add(row);
		}
		rs.close();
		stmt.close();
		return results;
	}

	public List queryResults(String query) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(query);
		return queryResults(stmt);
	}

	public Map queryUniqueResult(String query) throws SQLException, DBException {
		PreparedStatement stmt = connection.prepareStatement(query);
		return queryUniqueResult(stmt);
	}

	public Map queryUniqueResult(PreparedStatement stmt) throws SQLException, DBException {
		Map row = new HashMap();
		ResultSet rs = stmt.executeQuery();
		ResultSetMetaData metadata = rs.getMetaData();
		if (rs.next()) {
			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				String name = metadata.getColumnName(i);
				Object value = rs.getObject(i);
//				if (value == null /*&& metadata.getColumnClassName(i).equals("java.lang.String")*/)
//					value = "";
				row.put(name, value);
			}
		} else {
			throw new DBException(DBException.NENHUM_REGISTRO);
		}
		
		if (rs.next())
			throw new DBException(DBException.MAIS_DE_UM_REGISTRO);
		
		rs.close();
		stmt.close();
		return row;
	}
	
	public Object queryValue(String query) throws SQLException, DBException {
		PreparedStatement stmt = connection.prepareStatement(query);
		return queryValue(stmt);
	}

	public Object queryValue(PreparedStatement stmt) throws SQLException,
			DBException {
		ResultSet rs = stmt.executeQuery();
		Object ret;
		
		if (rs.next())
			ret = rs.getObject(1);
		else 
			throw new DBException(DBException.NENHUM_REGISTRO);
		
		if (rs.next())
			throw new DBException(DBException.MAIS_DE_UM_REGISTRO);
		
		rs.close();
		stmt.close();
		return ret;
	}
	
	public Object queryValueOrNull(String query) throws SQLException, DBException {
		PreparedStatement stmt = connection.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();
		Object ret;
		
		if (rs.next())
			ret = rs.getObject(1);
		else 
			return null;
		
//		if (rs.next())
//			throw new SigaException(SigaException.MAIS_DE_UM_REGISTRO);
		
		rs.close();
		stmt.close();
		
		return ret;
	}
	
	public Integer queryInt(String query) throws SQLException, DBException {
		return (Integer)queryValue(query);
	}
	
	public Integer queryIntOrNull(String query) throws SQLException, DBException {
		return (Integer)queryValueOrNull(query);
	}
	
	public Long queryLong(String query) throws SQLException, DBException {
		return (Long)queryValue(query);
	}
	
	public Long queryLongOrNull(String query) throws SQLException, DBException {
		return (Long)queryValueOrNull(query);
	}
	
	public String queryString(String query) throws SQLException, DBException {
		return (String)queryValue(query);
	}
	
	public String queryStringOrNull(String query) throws SQLException, DBException {
		return (String)queryValueOrNull(query);
	}
	
	public Date queryDate(String query) throws SQLException, DBException {
		return (Date)queryValue(query);
	}
	
	public boolean queryResultsEmpty(String query) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(query);
		ResultSet rs = stmt.executeQuery();
		return !rs.next();
	}
	
	public boolean queryResultsNotEmpty(String query) throws SQLException {
		return !queryResultsEmpty(query);
	}
	
	public Object[] querySingleColumn(String query) throws SQLException {
		List results = queryResults(query);
		Object[] ret = new Object[results.size()];
		for (int i = 0; i < results.size(); i++) {
			Collection values = ((Map)results.get(i)).values();
			for (Object o : values)
				ret[i] = o;
		}
		return ret;
	}
	
//	public Integer[] querySingleIntColumn(String query) throws SQLException {
//		return (Integer[])querySingleColumn(query);
//	}
//	
//	public String[] querySingleStringColumn(String query) throws SQLException {
//		return (String[])querySingleColumn(query);
//	}
}
