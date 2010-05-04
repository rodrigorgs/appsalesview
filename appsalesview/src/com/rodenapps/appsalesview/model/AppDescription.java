package com.rodenapps.appsalesview.model;

public class AppDescription {
	int app_id;
	String title;
	
	public AppDescription(int appId, String title) {
		super();
		app_id = appId;
		this.title = title;
	}
	public int getApp_id() {
		return app_id;
	}
	public void setApp_id(int appId) {
		app_id = appId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String toString() {
		return title;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AppDescription) {
			AppDescription other = (AppDescription)obj;
			return app_id == other.app_id;
		}
		else
			return false;
	}
}
