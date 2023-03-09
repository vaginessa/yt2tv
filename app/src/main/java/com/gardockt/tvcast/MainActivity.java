package com.gardockt.tvcast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gardockt.tvcast.protocols.dial.DialRequestExecutor;
import com.gardockt.tvcast.protocols.dial.DialDevice;
import com.gardockt.tvcast.protocols.dial.SsdpDeviceFinder;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

	private TextView logLabel;
	private EditText videoIdField;
	private ListView foundDevicesListView;
	private ArrayAdapter<DialDevice> foundDevicesListAdapter;
	private ProgressBar refreshDevicesSpinner;
	private Button refreshDevicesButton;

	private final SsdpDeviceFinder deviceFinder = new SsdpDeviceFinder(3000);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		logLabel = findViewById(R.id.logLabel);
		videoIdField = findViewById(R.id.videoIdField);
		foundDevicesListView = findViewById(R.id.foundDevicesListView);
		refreshDevicesSpinner = findViewById(R.id.refreshDevicesSpinner);
		refreshDevicesButton = findViewById(R.id.refreshDevicesButton);

		Intent intent = getIntent();
		String action = intent.getAction();

		if (Intent.ACTION_SEND.equals(action)) {
			String url = intent.getStringExtra(Intent.EXTRA_TEXT);
			try {
				YoutubeVideoIdExtractor.extract(url);
				videoIdField.setText(url);
			} catch (YoutubeVideoIdExtractor.VideoIdNotFoundException e) {
				Toast.makeText(getApplicationContext(), getString(R.string.error_shared_url_not_supported), Toast.LENGTH_SHORT).show();
				finish();
			}
		}

		refreshDevicesButton.setOnClickListener(v -> refreshDevices());

		foundDevicesListAdapter = new ArrayAdapter<>(this, R.layout.component_list_device, new ArrayList<>());
		foundDevicesListView.setAdapter(foundDevicesListAdapter);
		foundDevicesListView.setOnItemClickListener((adapterView, view, i, l) -> {
			DialDevice device = (DialDevice) adapterView.getItemAtPosition(i);
			onDeviceSelected(device);
		});

		deviceFinder.setOnSearchStartedFunction(this::onSearchStarted);
		deviceFinder.setLogFunction((s) -> runOnUiThread(() -> log(s)));
		deviceFinder.setOnDeviceFoundFunction(this::onDeviceFound);
		deviceFinder.setOnSearchStoppedFunction(this::onSearchStopped);

		refreshDevices();
	}

	private void reset() {
		foundDevicesListAdapter.clear();
	}

	private void refreshDevices() {
		reset();

		if (!isWifiConnected()) {
			Toast.makeText(getApplicationContext(), getString(R.string.error_no_connection), Toast.LENGTH_SHORT).show();
			return;
		}

		if (deviceFinder.isRunning()) {
			deviceFinder.stop();
		}

		deviceFinder.start();
	}

	private void onDeviceSelected(DialDevice device) {
		try {
			String userEnteredVideoId = videoIdField.getText().toString();
			String videoId = YoutubeVideoIdExtractor.extract(userEnteredVideoId);
			sendVideo(device, videoId);
		} catch (YoutubeVideoIdExtractor.VideoIdNotFoundException e) {
			Toast.makeText(getApplicationContext(), getString(R.string.error_cannot_extract_video_id), Toast.LENGTH_SHORT).show();
		}
	}

	private void sendVideo(DialDevice device, String videoId) {
		new Thread() {
			@Override
			public void run() {
				try {
					int responseCode = new DialRequestExecutor().playYoutubeVideo(device, videoId);
					String toastMessage;
					if (responseCode / 100 == 2) {
						// HTTP 2XX
						toastMessage = getString(R.string.msg_video_sent);
					} else {
						toastMessage = String.format(getString(R.string.msg_request_failed_http_code), responseCode);
					}
					runOnUiThread(() -> Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show());
				} catch (IOException e) {
					String exceptionDescription = String.format("%s (%s)", e.getClass().getName(), e.getMessage());
					String toastMessage = String.format(getString(R.string.msg_exception_occurred), exceptionDescription);
					runOnUiThread(() -> Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show());
				}
			}
		}.start();
	}

	private void onSearchStarted() {
		runOnUiThread(() -> {
			log("[SSDPDeviceFinder] Search started");
			refreshDevicesSpinner.setVisibility(View.VISIBLE);

			// for some reason starting a new refresh while another is in progress occasionally
			// causes the program to freeze - one day it may be fixed, but I have no idea why does
			// it happen (socket.receive() continuing even after socket gets closed?), so for now
			// I'm just going to disable the button while refresh is in progress
			refreshDevicesButton.setEnabled(false);
		});
	}

	private void onSearchStopped() {
		runOnUiThread(() -> {
			log("[SSDPDeviceFinder] Search stopped");
			refreshDevicesSpinner.setVisibility(View.INVISIBLE);
			refreshDevicesButton.setEnabled(true);
		});
	}

	private void onDeviceFound(DialDevice device) {
		runOnUiThread(() -> foundDevicesListAdapter.add(device));
	}

	private boolean isWifiConnected() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		for (Network network : connMgr.getAllNetworks()) {
			NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
			if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				return true;
			}
		}
		return false;
	}

	public void log(String text) {
		logLabel.append(text + "\n");
	}

}
