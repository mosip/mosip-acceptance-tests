package main.java.io.mosip.ivv.base;

import io.restassured.response.Response;

public abstract class BaseHelperData {

	private boolean dataLoaded = false; 
	protected abstract boolean setData(String json);
	public abstract String getData();
	
	public void loadData(String json)
	{
		dataLoaded = setData(json);
	}
	
	public boolean isDataLoaded()
	{
		return dataLoaded;
	}
	public Response api_response = null;
	
}
