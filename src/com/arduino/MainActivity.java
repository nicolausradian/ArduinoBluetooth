package com.arduino;

import java.util.Set;
import java.util.UUID;

import android.R.integer;
//import android.R.string;
import android.app.Activity;
//import android.app.ActionBar;
//import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts.Data;

//import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
//import android.widget.Toast;
import android.util.Log;

import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.arduino.R.string;

import android.widget.ToggleButton;



public class MainActivity extends Activity {
	
	//set Variable
	Set<BluetoothDevice> pairedDevices;
	ToggleButton OnOff; //,visible, list;
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
	TextView myLabel, statement1, statement2;
	String input_data_arduino;
	private int var_1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		OnOff 		= (ToggleButton)findViewById(R.id.sensor_jarak);
		myLabel 	= (TextView)findViewById(R.id.textView1);
		statement1	= (TextView)findViewById(R.id.textView2);
		statement2	= (TextView)findViewById(R.id.textView3);
        //Button sendButton = (Button)findViewById(R.id.send);
        //Button closeButton = (Button)findViewById(R.id.close);
        //myLabel = (TextView)findViewById(R.id.label);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		OnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked){
					
//		            statement1.setText("ON");
		            try 
	                {
	                    findBT();
	                    openBT();
	                }
	                catch (IOException ex) { }
		            try 
	                {
	                    sendData();
	                }
	                catch (IOException ex) { }
				}
				else{
//					statement2.setText("OFF");

					try 
	                {
	                    closeBT();
	                }
	                catch (IOException ex) { }
				}
			}
		});

	}
	
	
	void findBT()
    {
    	Log.i("findBT","in findBT");
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	Log.i("findBT","bt adapter set");

        if(mBluetoothAdapter == null)
        {
        	Log.i("findBT","null");
            myLabel.setText("No bluetooth adapter available");
        }
        
        if(!mBluetoothAdapter.isEnabled())
        {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    	Log.i("findBT","paired dev: "+pairedDevices.toString());

        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("Bluetooth_Bee_V2")) 
                {
                	Log.i("findBT","found seeed");

                    mmDevice = device;
                    break;
                }
            }
        }
    	Log.i("findBT","could not fine seeed");

        myLabel.setText("Bluetooth Device Found");
    }
    
	void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);   
        Log.i("openBT", "after createRf");

        mmSocket.connect();
        Log.i("openBT", "after connect");
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        
        beginListenForData();
        
        myLabel.setText("Bluetooth Opened");
    }
    
    void beginListenForData()
    {
        final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character
        
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {                
               while(!Thread.currentThread().isInterrupted() && !stopWorker)
               {
                    try 
                    {
                        int bytesAvailable = mmInputStream.available();                        
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            String variable_1;//, variable_2;
                                            
                                        	myLabel.setText(data);
                                            String[] value = data.split("\\s*,\\s*");
//                                            variable_1 = Integer.valueOf(value[0]);
//                                           variable_2 = Integer.parseInt(value[1]);
                                            variable_1 = (value[1]);
                                            
                                            
                                            
                                            
                                            
 //                                           statement2.setText(value[1]);
                                            try {
                                            	
                                            	var_1 = Integer.parseInt(variable_1);
                                            	
                                            	if (var_1 < 400){
                                            		statement1.setText("1");
                                            	}
                                            	else {
                                            		statement1.setText("2");
                                            	}
                                            	
                                            } catch (NumberFormatException nfe) { 
                                            	
                                            }
                                            	
                                            
                                            
                                            
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } 
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                    }
               }
            }
        });

        workerThread.start();
    }
    
    void sendData() throws IOException
    {
        String msg = "1";
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }
    
    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
