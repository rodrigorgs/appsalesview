package com.rodenapps.appsalesview.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemFilter {
	public List<Integer> appIds = new ArrayList<Integer>();
	public Date beginDate = new Date();
	public Date endDate = new Date();
	public List<String> countries = new ArrayList<String>();
	public List<String> currencies = new ArrayList<String>();
	public List<ProductType> productTypes = new ArrayList<ProductType>();

	public boolean separateByAppId = false;
	public boolean separateByCountry = false;
	public boolean separateByCurrency = false;
	public boolean separateByProductType = false;

	public boolean includePaid = true;
	public boolean includeFree = true;
}
