package com.coney.bluetoothdemo;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	private static final int REQUEST_BT = 1000;
	Context context;
	Button create,join;
	EditText un;
	boolean btEnabled = false;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        
        context = this;
        create = (Button) findViewById(R.id.createButton);
        join = (Button) findViewById(R.id.joinButton);
        un = (EditText) findViewById(R.id.editText1);
        
        enableBt();
        
        create.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				if (btEnabled)
				{
					Intent i = new Intent(context, hostChat.class);
					i.putExtra("un", un.getText().toString());
					startActivity(i);
				}else{
					enableBt();
				}
				
				
			}
		});
        
        join.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if (btEnabled)
				{
					Intent i = new Intent(context, joinChat.class);
					i.putExtra("un", un.getText().toString());
					startActivity(i);
				}else{
					enableBt();
				}
			
			}
		});
        
    }


    private void enableBt()
	{
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
		    // Device does not support Bluetooth
			return;
		}
		
		
		if (!mBluetoothAdapter.isEnabled()) {
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_BT);
		}else{
			btEnabled = true;
		}
	}
	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQUEST_BT)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				btEnabled = true;
			}
		}
	
	}
}
