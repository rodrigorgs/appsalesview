package com.rodenapps.appsalesview.ui;

import java.io.File;

import com.rodenapps.appsalesview.model.ItemFilter;

public interface FormObserver {
	public void filterChanged(ItemFilter filter);
	public void folderChanged(File folder);
	public void downloadReports(String username, String password);
}
