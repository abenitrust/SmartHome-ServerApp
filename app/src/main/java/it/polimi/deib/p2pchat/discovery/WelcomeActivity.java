package it.polimi.deib.p2pchat.discovery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import it.polimi.deib.p2pchat.R;

public class WelcomeActivity extends AppCompatActivity {

    Switch aSwitch;
    WifiManager wifiManager;

    ListView scanresult_lv;
    List<ScanResult> wifiList;

    ArrayAdapter<String> adapter;
    ArrayList<String> wifiResults;
    MyBroadCastReceiver myBroadCastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
        //this is for passing the username for the welcome note from the login page
        String username = getIntent().getStringExtra("username").toString();
        //Then this will set the text by the username
        TextView user = (TextView) findViewById(R.id.username);
        user.setText(username);
        //Scan Result List View
        scanresult_lv = (ListView)findViewById(R.id.scanresult_list);

        wifiResults=new ArrayList<String>();
        adapter=new ArrayAdapter<String>(WelcomeActivity.this, R.layout.list_item,R.id.txt,wifiResults);
        scanresult_lv.setAdapter(adapter);

        //The switch used to turn the wifi on and off
        aSwitch = (Switch)findViewById(R.id.wifi_switch);
        //creating wifi instance
        wifiManager = (WifiManager)getSystemService(WIFI_SERVICE);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //to switch ON wifi
                if(isChecked && !wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(true);
                    wifiManager.startScan();
                }

                //to Switch OFF wifi
                else if (!isChecked && wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                }
            }
        });

        myBroadCastReceiver = new MyBroadCastReceiver();
        //Register the broadcast receiver
        registerReceiver(myBroadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        registerReceiver(new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                results = wifiManager.getScanResults();
//                size = results.size();
//            }
//        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the wifi receiver
        unregisterReceiver(myBroadCastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register wifi receiver to get the results
        registerReceiver(myBroadCastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    class MyBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
                       //StringBuilder stringBuilder = new StringBuilder();
//            List<ScanResult> list = wifiManager.getScanResults();
//            for (ScanResult scanResult : list){
//                //filtering the scanResult by SSID
//                //stringBuilder.append(scanResult.SSID+"\n");
//            }
//            //scanresult.setText(stringBuilder);

            wifiResults.clear();
            wifiList = wifiManager.getScanResults();
            for(int i = 0; i < wifiList.size(); i++){
                wifiResults.add(wifiList.get(i).SSID.toString()+"\n"+wifiList.get(i).frequency);
            }
            adapter.notifyDataSetChanged();
        }
    }
}
