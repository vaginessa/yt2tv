package com.gardockt.tvcast.protocols.dial;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class SsdpDeviceFinder {

	private Runnable onSearchStartedFunction = null;
	private Consumer<String> logFunction = null;
	private Consumer<DialDevice> onDeviceFoundFunction = null;
	private Runnable onSearchStoppedFunction = null;

	private final Integer timeout;

	private final byte[] searchMessage = String.join("\r\n", new String[]{
			"M-SEARCH * HTTP/1.1",
			"HOST: 239.255.255.250:1900",
			"MAN: \"ssdp:discover\"",
			"MX: 10",
			"ST: urn:dial-multiscreen-org:service:dial:1",
			"",
			"",
	}).getBytes(StandardCharsets.UTF_8);
	private final int bufSize = 4096;
	private final Set<DialDevice> devices = new HashSet<>();
	private Thread searchThread = null;

	private final DocumentBuilder documentBuilder;

	public SsdpDeviceFinder(int timeout) {
		if (timeout < 0) {
			throw new IllegalArgumentException("Timeout cannot be negative");
		}

		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}  catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}

		this.timeout = timeout;
	}

	public void start() {
		if (isRunning()) {
			stop();
		}

		devices.clear();

		searchThread = buildSearchThread();
		searchThread.start();
	}

	private Thread buildSearchThread() {
		return new Thread() {
			private DatagramSocket socket = null;

			@Override
			public void run() {
				if (onSearchStartedFunction != null) {
					onSearchStartedFunction.run();
				}

				try {
					socket = new DatagramSocket();
					if (timeout != null) {
						socket.setSoTimeout(timeout);
					}

					sendSearchMessage(socket);

					DatagramPacket packet = new DatagramPacket(new byte[bufSize], bufSize);
					while (!isInterrupted()) {
						socket.receive(packet);
						processDevice(packet);
					}
				} catch (IOException ignored) {
					// SocketException is a result of closing socket while sending/receiving
					// or hitting a timeout
				} finally {
					socket.close();
				}

				if (onSearchStoppedFunction != null) {
					onSearchStoppedFunction.run();
				}
			}

			@Override
			public void interrupt() {
				if (socket != null) {
					socket.close();
				}
			}
		};
	}

	public void stop() {
		if (searchThread == null) {
			return;
		}

		searchThread.interrupt();

		// make sure the thread died to avoid potential race conditions
		try {
			searchThread.join();
		} catch (InterruptedException ignored) {}
	}

	public boolean isRunning() {
		return searchThread != null && searchThread.isAlive();
	}

	public Set<DialDevice> getDevices() {
		return devices;
	}

	private void sendSearchMessage(DatagramSocket socket) throws IOException {
		DatagramPacket packet = new DatagramPacket(searchMessage, searchMessage.length, InetAddress.getByName("239.255.255.250"), 1900);
		socket.send(packet);
	}

	private void processDevice(DatagramPacket packet) {
		// TODO: run in separate thread? (connection may block)
		HttpURLConnection urlConnection = null;

		try {
			// get location
			String locationHeaderPrefix = "LOCATION: ";
			String data = new String(packet.getData());
			log("[processDevice] Received data:\n" + data);
			String locationHeader = Arrays.stream(data.split("\r\n"))
					.filter(s -> s.startsWith(locationHeaderPrefix))
					.findFirst()
					.orElseThrow(() -> new RuntimeException("Invalid location header"));
			String location = locationHeader.substring(locationHeaderPrefix.length());
			log("[processDevice] Location: " + location);

			// connect to location and get friendly name
			URL url = new URL(location);
			urlConnection = (HttpURLConnection) url.openConnection();
			Document document = documentBuilder.parse(urlConnection.getInputStream());

			String friendlyName = document.getElementsByTagName("friendlyName").item(0).getTextContent();
			log("[processDevice] Friendly name: " + friendlyName);
			String appsUrl = urlConnection.getHeaderField("Application-URL");
			log("[processDevice] App URL: " + appsUrl);
			if (friendlyName.isEmpty() || appsUrl.isEmpty()) {
				throw new RuntimeException("Invalid device information");
			}

			DialDevice device = new DialDevice(friendlyName, appsUrl);
			if (!devices.contains(device)) {
				devices.add(device);
				if (onDeviceFoundFunction != null) {
					onDeviceFoundFunction.accept(device);
				}
			}
			log("[processDevice] Device added");
		} catch (Exception e) {
			// return without adding a device
			log("[processDevice] Exception occurred: " + e.getMessage());
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
		}
	}

	private void log(String text) {
		if (logFunction != null) {
			logFunction.accept(text);
		}
	}

	public void setOnSearchStartedFunction(Runnable onSearchStartedFunction) {
		this.onSearchStartedFunction = onSearchStartedFunction;
	}

	public void setLogFunction(Consumer<String> logFunction) {
		this.logFunction = logFunction;
	}

	public void setOnDeviceFoundFunction(Consumer<DialDevice> onDeviceFoundFunction) {
		this.onDeviceFoundFunction = onDeviceFoundFunction;
	}

	public void setOnSearchStoppedFunction(Runnable onSearchStoppedFunction) {
		this.onSearchStoppedFunction = onSearchStoppedFunction;
	}

}
