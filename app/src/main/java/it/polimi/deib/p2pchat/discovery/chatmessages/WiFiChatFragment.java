
package it.polimi.deib.p2pchat.discovery.chatmessages;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.deib.p2pchat.R;
import it.polimi.deib.p2pchat.discovery.DestinationDeviceTabList;
import it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue;
import it.polimi.deib.p2pchat.discovery.socketmanagers.ChatManager;
import it.polimi.deib.p2pchat.discovery.services.ServiceList;
import it.polimi.deib.p2pchat.discovery.services.WiFiP2pService;
import lombok.Getter;
import lombok.Setter;


public class WiFiChatFragment extends Fragment {


    @Getter private final List<String> items = new ArrayList<>();

    private TextView chatLine;

    private WiFiChatMessageListAdapter adapter = null;

    private static final String TAG = "WiFiChatFragment";

    //Seek bar object
    private SeekBar brightbar;
    private TextView receviedMessage;
    final BluetoothAdapter bluetoothAdaper = BluetoothAdapter.getDefaultAdapter( );

    //Variable to store brightness value
    private int brightness;

    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;

    //Window object, that will store a reference to the current window
    private Window window;

    private  AudioManager mobilemode;

    ToggleButton ringer;
    ToggleButton bluetooth;

    TextView txtPerc;
    /** Called when the activity is first created. */

    @Getter @Setter private Integer tabNumber;
    @Getter @Setter private static boolean firstStartSendAddress;
    @Getter @Setter private boolean grayScale = true;


    @Getter @Setter private ChatManager chatManager;


    /**
     * Callback interface to call methods reconnectToService in {@link it.polimi.deib.p2pchat.discovery.MainActivity}.
     * MainActivity implements this interface.
     */
    public interface AutomaticReconnectionListener {
        public void reconnectToService(WiFiP2pService wifiP2pService);
    }

    /**
     * Method to obtain a new Fragment's instance.
     * @return This Fragment instance.
     */
    public static WiFiChatFragment newInstance() {
        return new WiFiChatFragment();
    }

    /**
     * Default Fragment constructor.
     */
    public WiFiChatFragment() {}



    /**
     * Method that combines all the messages inside the
     * {@link it.polimi.deib.p2pchat.discovery.chatmessages.waitingtosend.WaitingToSendQueue}
     * in one String and pass this one to the {@link it.polimi.deib.p2pchat.discovery.socketmanagers.ChatManager}
     * to send the message to other devices.
     */
    public void sendForcedWaitingToSendQueue() {

        Log.d(TAG, "sendForcedWaitingToSendQueue() called");

        String combineMessages = "";
        List<String> listCopy = WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber);
        for (String message : listCopy) {
            if(!message.equals("") && !message.equals("\n")  ) {
                combineMessages = combineMessages + "\n" + message;
            }
        }
        combineMessages = combineMessages + "\n";

        Log.d(TAG, "Queued message to send: " + combineMessages);

        if (chatManager != null) {
            if (!chatManager.isDisable()) {
                chatManager.write((combineMessages).getBytes());
                WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).clear();
            } else {
                Log.d(TAG, "Chatmanager disabled, impossible to send the queued combined message");
            }

        }
    }


    /**
     * Method to add a message to the Fragment's listView and notifies this update to
     * {@link it.polimi.deib.p2pchat.discovery.chatmessages.WiFiChatMessageListAdapter}.
     * @param readMessage String that represents the message to add.
     */
    public void pushMessage(String readMessage) {
        items.add(readMessage);
        adapter.notifyDataSetChanged();
    }

    /**
     * Method that updates the {@link it.polimi.deib.p2pchat.discovery.chatmessages.WiFiChatMessageListAdapter}.
     */
    public void updateChatMessageListAdapter() {
        if(adapter!=null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Method that add the text in the chatLine EditText to the WaitingToSendQueue and try to reconnect
     * to the service associated to the device of this tab, with index tabNumber.
     */
    private void addToWaitingToSendQueueAndTryReconnect(String data) {
        //add message to the waiting to send queue
        WaitingToSendQueue.getInstance().getWaitingToSendItemsList(tabNumber).add(data);

        //try to reconnect
        WifiP2pDevice device = DestinationDeviceTabList.getInstance().getDevice(tabNumber - 1);
        if(device!=null) {
            WiFiP2pService service = ServiceList.getInstance().getServiceByDevice(device);
            Log.d(TAG, "device address: " + device.deviceAddress + ", service: " + service);

            //call reconnectToService in MainActivity
            ((AutomaticReconnectionListener) getActivity()).reconnectToService(service);

        } else {
            Log.d(TAG,"addToWaitingToSendQueueAndTryReconnect device == null, i can't do anything");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chatmessage_list, container, false);

        viewReceviedMessage(view);

        handleButton(view);

        handleBluetoothBtn(view);


//        CheckBox    test= (CheckBox)view.findViewById(R.id.checkBox2);
//
//        test.setOnClickListener(
//                new View.OnClickListener(){
//
//                    @Override
//                    public void onClick(View v) {
//                        if (chatManager != null) {
//                            if (!chatManager.isDisable()) {
//                                Log.d(TAG, "chatmanager state: enable");
//
//                                //send message to the ChatManager's outputStream.
//                                chatManager.write("Turn On/Off".getBytes());
//                            } else {
//                                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);
//
//                                addToWaitingToSendQueueAndTryReconnect();
//                            }
//                        } else {
//                            Log.d(TAG, "chatmanager is null");
//                        }
//                    }
//                }
//        );
//
//        SeekBar seekBar= (SeekBar)view.findViewById(R.id.seekBar);
//        seekBar.setOnSeekBarChangeListener(
//                new SeekBar.OnSeekBarChangeListener(){
//
//                    @Override
//                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        if (chatManager != null) {
//                            if (!chatManager.isDisable()) {
//                                Log.d(TAG, "chatmanager state: enable");
//
//                                //send message to the ChatManager's outputStream.
//                                chatManager.write((Integer.toString(progress)).getBytes());
//                            } else {
//                                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);
//
//                                addToWaitingToSendQueueAndTryReconnect();
//                            }
//
//                        } else {
//                            Log.d(TAG, "chatmanager is null");
//                        }
//                    }
//
//                    @Override
//                    public void onStartTrackingTouch(SeekBar seekBar) {
//
//                    }
//
//                    @Override
//                    public void onStopTrackingTouch(SeekBar seekBar) {
//
//                    }
//                }
//        );
//
//        view.findViewById(R.id.swithOff).setOnClickListener(
//                new View.OnClickListener(){
//
//                    @Override
//                    public void onClick(View v) {
//                        if (chatManager != null) {
//                            if (!chatManager.isDisable()) {
//                                Log.d(TAG, "chatmanager state: enable");
//
//                                //send message to the ChatManager's outputStream.
//                                chatManager.write("Turn On/Off".getBytes());
//                            } else {
//                                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);
//
//                                addToWaitingToSendQueueAndTryReconnect();
//                            }
//                        } else {
//                            Log.d(TAG, "chatmanager is null");
//                        }
//                    }
//                }
//        );

        handleScreenBrigthness(view);

        return view;
    }


    public void viewReceviedMessage(View view){

        receviedMessage = (TextView) view.findViewById(R.id.recevedMessage);
        receviedMessage.setText("message");

    }

    public void setToggleButton(String button) {
        ringer.toggle();
    }

    public void handleButton(View view){
        ringer = (ToggleButton) view.findViewById(R.id.toggleButton);

//        mobilemode= (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        ringer.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                String data="SP - "+ringer.getText().toString();
                SendMessage(data);


            }


        });
    }

    public void handleBluetoothBtn(View view){
        bluetooth = (ToggleButton) view.findViewById(R.id.bluetoothButton);

        bluetooth.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                if( !bluetoothAdaper.isEnabled( ) )
                {
                    bluetoothAdaper.enable( );
                }else{
                    bluetoothAdaper.disable();
                }



            }
        });
    }

    public void setBluetooth(String blue){
        bluetoothAdaper.enable();
    }

    public void setMessage(String message){
        receviedMessage.setText(message);
    }

    public void handleScreenBrigthness(View view){

        //Instantiate seekbar object
        brightbar = (SeekBar) view.findViewById(R.id.brightbar);

        txtPerc = (TextView) view.findViewById(R.id.txtPercentage);

        //Get the content resolver
        cResolver = getActivity().getContentResolver();

        //Get the current window
        window = getActivity().getWindow();

        //Set the seekbar range between 0 and 255
        brightbar.setMax(255);

        //Set the seek bar progress to 1
        brightbar.setKeyProgressIncrement(1);

        try {
            //Get the current system brightness
            brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            // TODO: handle exception
            //Throw an error case it couldn't be retrieved
            Log.e("Error", "cannot access system brightness.");
            e.printStackTrace();
        }

        //Set the progress of the seek bar based on the system's brightness
        brightbar.setProgress(brightness);


        //Register OnSeekBarChangeListener, so it can actually change values
        brightbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

                //Set the system brightness using the brightness variable value
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
                //Get the current window attributes
                WindowManager.LayoutParams layoutpars = window.getAttributes();

                //Set the brightness of this window
                layoutpars.screenBrightness = brightness / (float)255;
                //Apply attribute changes to this window
                window.setAttributes(layoutpars);

                String data="SB - "+Integer.toString(brightness);
                SendMessage(data);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

                //Nothing handled here
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                //Set the minimal brightness level
                //if seek bar is 20 or any value below
                if(progress<=1)
                {
                    //Set the brightness to 20
                    brightness=10;
                }
                else //brightness is greater than 20
                {
                    //Set brightness variable based on the progress bar
                    brightness = progress;
                }

                //Calculate the brightness percentage
                float perc = (brightness /(float)255)*100;


                //Set the brightness percentage
                txtPerc.setText((int)perc + "%");


            }
        });
    }

    public void changeBrigthness(String data){
        int value=0;
        try {
            value = Integer.parseInt(data);
        } catch(NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
            value=0;
        }

        //Set the system brightness using the brightness variable value
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, value);
        //Get the current window attributes
        WindowManager.LayoutParams layoutpars = window.getAttributes();

        //Set the brightness of this window
        layoutpars.screenBrightness = value / (float)255;
        //Apply attribute changes to this window
        window.setAttributes(layoutpars);

        //Calculate the brightness percentage
        float perc = (value /(float)255)*100;

        brightbar.setProgress(value);

        //Set the brightness percentage
        txtPerc.setText((int)perc + "%");


    }


    private  boolean SendMessage(String data){
        if (chatManager != null) {
            if (!chatManager.isDisable()) {
                Log.d(TAG, "chatmanager state: enable");

                //send message to the ChatManager's outputStream.
                chatManager.write(data.getBytes());
                return true;
            } else {
                Log.d(TAG, "chatmanager disabled, trying to send a message with tabNum= " + tabNumber);

                addToWaitingToSendQueueAndTryReconnect(data);
                return false;
            }
        } else {
            Log.d(TAG, "chatmanager is null");
            return false;
        }
    }
}
