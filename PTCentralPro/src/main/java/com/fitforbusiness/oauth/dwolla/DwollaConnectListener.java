package com.fitforbusiness.oauth.dwolla;

public interface DwollaConnectListener {
	
	public abstract void onConnected();
	
	public abstract void onDisconnected();

	public abstract void onError(String error);
	
}