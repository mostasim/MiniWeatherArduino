package tech.mbsoft.miniweatherarduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import me.aflak.bluetooth.Bluetooth;
import tech.mbsoft.miniweatherarduino.data.model.Data;

import static android.Manifest.permission.BLUETOOTH;
import static android.Manifest.permission.BLUETOOTH_ADMIN;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,Bluetooth.CommunicationCallback{

    /*
    * Press Alt-0176 to degree icon "°"
    * */
    private static final String TAG = "MainActivity-Mostasim";
    private static final int PERMISSION_REQUEST_CODE = 200;

    private boolean IS_FOUND_DEVICE_HC_06=false;
    private ConstraintLayout constraintLayout;
    private Bluetooth bt;
    private List<BluetoothDevice> paired;
    private boolean registered=false;

    private TextView logText,tvTemperature,tvSoilTemperature,tvDate,tvPressure,tvAltitude,tvHumadity,tvMoisture,tvGas,tvAtmosphere,tvUvindex,tvLoadingText;
    private View itemAltitde,itemHumadity,itemMoisture,itemGas,itemAtmosphre,itemUvindex;
    private Group groupLoading,groupMainUI;
    private TextClock tvTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered=true;

        bt = new Bluetooth(this);

        if (checkPermission())
        {
            if (checkIsBluetoothOn())
            {
                addDevicesToList();
            }else
            {
                bt.enableBluetooth();
                Snackbar.make(constraintLayout, "Enabling bluetooth...", Snackbar.LENGTH_SHORT).show();
            }
        }else
        {
            requestPermission();
        }

    }

    private boolean checkIsBluetoothOn() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            //device doesn't support bluetooth
            return false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                return false; //not enabled
            }else
            {
                return true; //enabled
            }
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), BLUETOOTH_ADMIN);

        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{BLUETOOTH, BLUETOOTH_ADMIN}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean bluetoothAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean bluetoothAdminAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (bluetoothAccepted && bluetoothAdminAccepted)
                        Snackbar.make(constraintLayout, "Permission Granted, Now you can access bluetooth.", Snackbar.LENGTH_LONG).show();
                    else {

                        Snackbar.make(constraintLayout, "Permission Denied, You cannot access bluetooth.", Snackbar.LENGTH_LONG).show();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(BLUETOOTH)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{BLUETOOTH, BLUETOOTH_ADMIN},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    private void initUI() {

        constraintLayout =findViewById(R.id.parentView);
        groupLoading=findViewById(R.id.group_loading);
        groupMainUI=findViewById(R.id.mainUI);
        groupMainUI.setVisibility(View.GONE);

        logText =findViewById(R.id.logText);
        tvTemperature=findViewById(R.id.tvTemperature);
        tvSoilTemperature=findViewById(R.id.tvSoilTemperature);
        tvTime=findViewById(R.id.tvTime);

        tvDate=findViewById(R.id.tvDate);
        SimpleDateFormat sdfDate = new SimpleDateFormat("EEE dd MMM ,yyyy");
        String currentDate = sdfDate.format(new Date());
        tvDate.setText(currentDate);
        tvPressure=findViewById(R.id.tvPressure);
        tvLoadingText=findViewById(R.id.tvLoadingText);

        itemAltitde=findViewById(R.id.itemAltitude);
        ImageView ivAltitude=itemAltitde.findViewById(R.id.ivItemIcon);
        ivAltitude.setImageDrawable(getResources().getDrawable(R.drawable.altitude));
        TextView tvAltitudeLabel=itemAltitde.findViewById(R.id.tvItemLabel);
        tvAltitudeLabel.setText("Altitude");
        tvAltitude=itemAltitde.findViewById(R.id.tvItemValue);


        itemHumadity=findViewById(R.id.itemHumadity);
        ImageView ivHumadity=itemHumadity.findViewById(R.id.ivItemIcon);
        ivHumadity.setImageDrawable(getResources().getDrawable(R.drawable.humadity));
        TextView tvHumadityLabel=itemHumadity.findViewById(R.id.tvItemLabel);
        tvHumadityLabel.setText("Humadity");
        tvHumadity=itemHumadity.findViewById(R.id.tvItemValue);

        itemMoisture=findViewById(R.id.itemMoisture);
        ImageView ivMoisture=itemMoisture.findViewById(R.id.ivItemIcon);
        ivMoisture.setImageDrawable(getResources().getDrawable(R.drawable.moisture));
        TextView tvMoistureLabel=itemMoisture.findViewById(R.id.tvItemLabel);
        tvMoistureLabel.setText("Moisture");
        tvMoisture=itemMoisture.findViewById(R.id.tvItemValue);

        itemGas=findViewById(R.id.itemGas);
        ImageView ivGas=itemGas.findViewById(R.id.ivItemIcon);
        ivGas.setImageDrawable(getResources().getDrawable(R.drawable.gas));
        TextView tvGasLabel=itemGas.findViewById(R.id.tvItemLabel);
        tvGasLabel.setText("Gas");
        tvGas=itemGas.findViewById(R.id.tvItemValue);

        itemAtmosphre=findViewById(R.id.itemAtmosphere);
        ImageView ivAtmosphere=itemAtmosphre.findViewById(R.id.ivItemIcon);
        ivAtmosphere.setImageDrawable(getResources().getDrawable(R.drawable.atmosphere));
        TextView tvAtmosphereLabel=itemAtmosphre.findViewById(R.id.tvItemLabel);
        tvAtmosphereLabel.setText("Atmosphere");
        tvAtmosphere=itemAtmosphre.findViewById(R.id.tvItemValue);

        itemUvindex=findViewById(R.id.itemUvindex);
        ImageView ivUvindex=itemUvindex.findViewById(R.id.ivItemIcon);
        ivUvindex.setImageDrawable(getResources().getDrawable(R.drawable.uv_index));
        TextView tvUvindexLabel=itemUvindex.findViewById(R.id.tvItemLabel);
        tvUvindexLabel.setText("UvIndex");
        tvUvindex=itemUvindex.findViewById(R.id.tvItemValue);



    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

        }
    }
    private void addDevicesToList(){
        paired = bt.getPairedDevices();

        List<String> names = new ArrayList<>();
        for (BluetoothDevice d : paired){
            names.add(d.getName());
            if (d.getName().equals("HC-06"))
            {
                IS_FOUND_DEVICE_HC_06=true;
                Toast.makeText(this, "Paired with HC-06", Toast.LENGTH_SHORT).show();
                bt.setCommunicationCallback(this);
                Display("Connecting...");
                bt.connectToDevice(d);
            }
        }
        if (!IS_FOUND_DEVICE_HC_06)
            Snackbar.make(constraintLayout, "Please first paired with HC-06 Bluetooth device", Snackbar.LENGTH_LONG).show();

    }
    public void Display(final String s){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.append(s + "\n");

            }
        });
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(MainActivity.this, "Turn on bluetooth", Toast.LENGTH_LONG).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(constraintLayout, "Bluetooth is turned on", Snackbar.LENGTH_LONG).show();
                                addDevicesToList();
                            }
                        });
                        break;
                }
            }
        }
    };
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "convertStreamToString: "+e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Display("Connected to "+device.getName()+" - "+device.getAddress());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupLoading.setVisibility(View.GONE);
                groupMainUI.setVisibility(View.VISIBLE);
            }
        });


    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display("Disconnected!");
        Display("Connecting again...");
        bt.connectToDevice(device);
    }

    @Override
    public void onMessage(String message) {
        //dump message :: {"soil_temperature":"32.00","Temperature":"30.00","pressure":"100913","Altitude":"0.25","Humadity":"42.00","Moisture":"37","Gas":"37","Atmosphere":"0.9959","uv_index":"0"}
        Gson gson = new Gson();
        final Data data= gson.fromJson(message, Data.class);
        Log.d(TAG, "onMessage: Gson " + data.toString());
        Display(message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvTemperature.setText(data.getTemperature());
                tvSoilTemperature.setText(data.getSoilTemperature());
                tvPressure.setText(data.getPressure());
                tvAltitude.setText(data.getAltitude());
                tvHumadity.setText(data.getHumadity());
                tvMoisture.setText(data.getMoisture());
                tvGas.setText(data.getGas());
                tvAtmosphere.setText(data.getAtmosphere());
                tvUvindex.setText(data.getUvIndex());
            }
        });

    }

    @Override
    public void onError(String message) {
        Display("Error: "+message);
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        Display("Error: "+message);
        Display("Trying again in 3 sec.");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvLoadingText.setText("Reconnecting...");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bt.connectToDevice(device);
                    }
                }, 2000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        try{
            if (mReceiver!=null)
                unregisterReceiver(mReceiver);
        }catch (Exception e)
        {

        }
        super.onDestroy();
    }
}
