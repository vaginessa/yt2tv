package com.gardockt.tvcast.protocols.dial;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DialRequestExecutor {

	private String getAppUrl(DialDevice device, String appName) {
		return device.getAppsUrl() + appName;
	}

	public int executeRequest(DialDevice device, String appName, String requestBody) throws IOException {
		HttpURLConnection connection = null;
		try {
			String appUrl = getAppUrl(device, appName);
			URL url = new URL(appUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

			try (OutputStream outputStream = connection.getOutputStream()) {
				outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
			}

			return connection.getResponseCode();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public int playYoutubeVideo(DialDevice device, String videoId) throws IOException {
		return executeRequest(device, "YouTube", "v=" + videoId);
	}

}
