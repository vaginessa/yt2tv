package com.gardockt.tvcast.protocols.dial;

import androidx.annotation.NonNull;

import java.util.Objects;

public class DialDevice {

	private String friendlyName;
	private String appsUrl;

	public DialDevice(String friendlyName, String appsUrl) {
		this.friendlyName = friendlyName;
		this.appsUrl = appsUrl;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getAppsUrl() {
		return appsUrl;
	}

	public void setAppsUrl(String appsUrl) {
		this.appsUrl = appsUrl;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DialDevice)) {
			return false;
		}

		DialDevice d = (DialDevice) o;
		return Objects.equals(friendlyName, d.friendlyName) && Objects.equals(appsUrl, d.appsUrl);
	}

	@Override
	public int hashCode() {
		return Objects.hash(friendlyName, appsUrl);
	}

	@NonNull
	@Override
	public String toString() {
		if (friendlyName == null) {
			return "";
		}

		return friendlyName;
	}
}
