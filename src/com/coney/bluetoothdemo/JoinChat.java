package com.coney.bluetoothdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.R.integer;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.coney.bluetoothdemo.matadata.BluetoothData;
import com.coney.bluetoothdemo.util.Util;

/**
 * @ClassName: JoinChat
 * @Description: create client
 * @author coney Geng
 * @date 2014年9月19日 下午5:28:05
 * 
 */
public class JoinChat extends Activity {

	Context context;
	ConnectThread connectThread;
	ListView listView;
	ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	ArrayList<String> devicesNames = new ArrayList<String>();
	String username = null;
	String myName = "Unnamed";

	TextView mainChat;
	Button button;
	EditText input;
	int count = 0;

	ConnectedThread connectedThread;
	BluetoothSocket serverSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join);

		context = this;

		username = getIntent().getStringExtra("un");
		if (username != null) {
			myName = username;
		} else {
			myName = BluetoothAdapter.getDefaultAdapter().getName();
		}

		listView = (ListView) findViewById(R.id.listView1);

		getPairedDevices();

	}

	@Override
	protected void onDestroy() {
		String msg = myName + " has left the chat";
		if (connectedThread != null) {
			connectedThread.write(msg.getBytes());
			connectedThread.cancel();
		}

		super.onDestroy();
	}

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		public ConnectThread(BluetoothDevice device) {
			// Use a temporary object that is later assigned to mmSocket,
			// because mmSocket is final
			BluetoothSocket tmp = null;
			mmDevice = device;

			myName = mBluetoothAdapter.getName();
			if (username != "") {
				myName = username;
			}

			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createRfcommSocketToServiceRecord(UUID
						.fromString(BluetoothData.uuid));
			} catch (IOException e) {
				System.out
						.println("BTCHAT: Could not create rf comm socket connection");
				e.printStackTrace();
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				connectException.printStackTrace();
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}

			// Do work to manage the connection (in a separate thread)

			manageConnectedSocket(mmSocket);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private void getPairedDevices() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		final Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		// If there are paired devices

		if (pairedDevices.size() > 0) {

			// Loop through paired devices
			ArrayList<String> names = new ArrayList<String>();
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a
				// ListView
				names.add(device.getName() + "\n" + device.getAddress());
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
					android.R.layout.simple_list_item_1, names);
			listView.setAdapter(adapter);

			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					BluetoothDevice dev = (BluetoothDevice) pairedDevices
							.toArray()[arg2];
					connectTo(dev);
				}
			});
		}
	}

	private void connectTo(BluetoothDevice device) {
		ConnectThread ct = new ConnectThread(device);
		ct.run();
	}

	private void manageConnectedSocket(BluetoothSocket socket) {

		serverSocket = socket;
		System.out.println("BTCHAT: socket connected and managed woo");

		setContentView(R.layout.activity_chat);
		mainChat = (TextView) findViewById(R.id.textView1);
		button = (Button) findViewById(R.id.button1);
		input = (EditText) findViewById(R.id.editText1);

		connectedThread = new ConnectedThread(serverSocket);
		connectedThread.start();

		String joinMsg = myName + " has joined the chat";
		connectedThread.write(joinMsg.getBytes());

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String msg = myName + ": " + input.getText().toString();
				input.setText("");
				mainChat.setText(mainChat.getText().toString() + "\n" + msg);
				connectedThread.write(msg.getBytes());
			}
		});

	}

	private class ConnectedThread extends Thread {

		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run() {
			final byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					// mHandler.obtainMessage(MESSAGE_READ, bytes, -1,
					// buffer).sendToTarget();

					System.out.println("BTCHAT: BTCHAT: reading " + bytes
							+ " bytes");

					final byte[] bufferCopy = buffer.clone();

					Runnable r = new Runnable() {
						public void run() {
							addToChat(bufferCopy);
						}
					};

					runOnUiThread(r);

					clearBuffer(buffer, 1024);

				} catch (IOException e) {
					e.printStackTrace();
					break;
				}
			}
		}

		/* Call this from the main activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void clearBuffer(byte[] buffer, int n) {
		for (int i = 0; i < n; i++) {
			buffer[i] = 0;
		}
	}

	private void addToChat(byte[] buffer) {
		// System.out.println("BTCHAT: Adding "+buffer+" to chat");
		String str = "";
		try {
			str = new String(buffer, "UTF-8");
			String[] strArray = str.split("/");
			if (Integer.parseInt(strArray[0]) == 1) {
				mainChat.setText("");
				count = 1;
			} else {
				count += 1;
			}
			Util util = new Util();
			mainChat.setText(mainChat.getText().toString()
					+ "\n"
					+ "lost:"
					+ util.getCount(count, Integer.parseInt(strArray[0]))
					+ "   delay:"
					+ util.getDelayTime(System.currentTimeMillis(),
							Long.parseLong(strArray[1])));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Spannable parseMarkup(String msg) {

		boolean inBold = false;
		int boldStart = -1;
		int boldEnd = -1;

		for (int i = 0; i < msg.length(); i++) {
			char c = msg.charAt(i);
			if (c == '*') {
				if (inBold) {
					boldEnd = i;
					inBold = false;
				} else {
					boldStart = i;
					inBold = true;
				}
			}
		}

		return null;
	}

}
