package com.coney.bluetoothdemo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.R.integer;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @ClassName: HostChat
 * @Description: create host
 * @author coney Geng
 * @date 2014年9月19日 下午5:27:40
 * 
 */
public class HostChat extends Activity {

	AcceptThread acceptor;
	ArrayList<BluetoothSocket> sockets = new ArrayList<BluetoothSocket>();
	ArrayList<ConnectedThread> connections = new ArrayList<ConnectedThread>();
	Context context;
	TextView mainChat;
	Button button;
	Button testButton;
	EditText input;
	EditText testInput;

	String username = null;
	String myName = "Unnamed";
	int count = 0;
	String message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		context = this;
		mainChat = (TextView) findViewById(R.id.textView1);
		button = (Button) findViewById(R.id.button1);
		testButton = (Button) findViewById(R.id.intervalButton);
		input = (EditText) findViewById(R.id.editText1);
		testInput = (EditText) findViewById(R.id.intervalTime);
		acceptor = new AcceptThread();
		acceptor.start();

		username = getIntent().getStringExtra("un");
		if (username != null) {
			myName = username;
		} else {
			myName = BluetoothAdapter.getDefaultAdapter().getName();
		}

		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String msg = myName + ": " + input.getText().toString();
				input.setText("");
				// mainChat.setText(mainChat.getText().toString() + "\n" + msg);
				// mainChat.setText(String.valueOf(System.currentTimeMillis()));
				count += 1;
				message = count + "/" + System.currentTimeMillis() + "/"
						+ input.getText();
				for (ConnectedThread ct : connections) {
					// ct.write(msg.getBytes());
					ct.write(message.getBytes());
				}
			}
		});
		testButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				count = 0;
				int time = Integer.parseInt(testInput.getText().toString());
				timer = new Timer();
				timer.schedule(new RemindTask(), 0, // initial delay
						time); // subsequent rate
			}
		});

	}

	private class AcceptThread extends Thread {

		private final BluetoothServerSocket mmServerSocket;
		private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		public AcceptThread() {
			System.out.println("BTCHAT: AcceptThread was called");

			myName = mBluetoothAdapter.getName();
			if (username != "") {
				myName = username;
			}
			// Use a temporary object that is later assigned to mmServerSocket,
			// because mmServerSocket is final
			BluetoothServerSocket tmp = null;
			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
				tmp = mBluetoothAdapter
						.listenUsingRfcommWithServiceRecord(
								"Bluetooth Chat",
								UUID.fromString("f820b940-a4ef-11e3-a5e2-0800200c9a66"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			mmServerSocket = tmp;
		}

		public void run() {
			System.out.println("BTCHAT: running...");
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mmServerSocket.accept();
					System.out.println("BTCHAT: accepted connection!");
				} catch (IOException e) {
					System.out.println("BTCHAT: socket could not accept");
					e.printStackTrace();
					break;
				}
				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a separate thread)
					manageConnectedSocket(socket);
				}
			}
		}

		/** Will cancel the listening socket, and cause the thread to finish */
		public void cancel() {
			try {
				mmServerSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private void manageConnectedSocket(BluetoothSocket socket) {

		System.out.println("BTCHAT: accepted socket! woo");
		sockets.add(socket);
		Log.i("sockets", sockets.size() + "");
		final ConnectedThread ct = new ConnectedThread(socket);
		ct.start();
		connections.add(ct);

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

					System.out.println("BTCHAT: reading " + bytes + " bytes");

					final byte[] bufferCopy = buffer.clone();

					Runnable r = new Runnable() {
						public void run() {
							addToChat(bufferCopy);

						}
					};

					runOnUiThread(r);
					forwardMessage(bufferCopy, this);
					clearBuffer(buffer, 1024);

				} catch (Exception e) {
					System.out.println("BTCHAT: Exception in reading");
					try {
						mmInStream.close();
						mmSocket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
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

	private void forwardMessage(byte[] buffer, ConnectedThread fromConnection) {
		for (ConnectedThread ct : connections) {
			if (ct != fromConnection) {
				ct.write(buffer);
			}
		}
	}

	private void addToChat(byte[] buffer) {
		String str = "";
		try {
			str = new String(buffer, "UTF-8");
			mainChat.setText(mainChat.getText().toString() + "\n" + str);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void clearBuffer(byte[] buffer, int n) {
		for (int i = 0; i < n; i++) {
			buffer[i] = 0;
		}
	}

	@Override
	protected void onDestroy() {
		for (ConnectedThread ct : connections) {
			if (ct != null) {
				ct.cancel();
			}
		}
		acceptor.cancel();
		super.onDestroy();
	}

	/**
	 * @ClassName: RemindTask
	 * @Description: a timer to test bluetooth connection
	 * @author coney Geng
	 * @date 2014年9月19日 下午6:35:28
	 * 
	 */

	Timer timer;

	class RemindTask extends TimerTask {
		int numWarningBeeps = 10;

		public void run() {
			if (numWarningBeeps > 0) {
				// mainChat.setText(mainChat.getText().toString() + "\n" + msg);
				// mainChat.setText(String.valueOf(System.currentTimeMillis()));
				count += 1;
				message = count + "/" + System.currentTimeMillis() + "/";
				Log.i("timeStart", System.currentTimeMillis() + "");
				for (ConnectedThread ct : connections) {
					// ct.write(msg.getBytes());
					ct.write(message.getBytes());
				}
				Log.i("timeEnd", System.currentTimeMillis() + "");
				numWarningBeeps--;
			} else {
				System.out.println("Time's up!");
				timer.cancel(); // Not necessary because we call
				// System.exit
			}
		}
	}
}
