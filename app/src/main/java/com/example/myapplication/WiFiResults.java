package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;

import java.io.*;
import java.sql.Time;
import java.text.DateFormat;
import java.util.*;

import static android.text.format.Formatter.formatIpAddress;
import static java.lang.Thread.sleep;

public class WiFiResults extends AppCompatActivity {

    private static final String FILE_NAME="WIFIinfo.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_results);

        File temp=new File(getFilesDir()+"/"+FILE_NAME);
        if(temp.exists()){
            RandomAccessFile raf=null;
            try{
                raf=new RandomAccessFile(temp, "rw");
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
        }



        WifiManager wifiManager=(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if(!wifiManager.isWifiEnabled()){
            Toast.makeText(this, "WiFi is disabled, enabling...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);

        }
        else{
            Toast.makeText(this, "WiFi on, Scanning....", Toast.LENGTH_LONG).show();
        }

        StringBuilder s = new StringBuilder();
        int ip= wifiInfo.getIpAddress();
        int numberOfLevels=5;
        int level=WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        @SuppressWarnings("deprecation")String ipAddress = formatIpAddress(ip);
        s.append("Link name: "+wifiInfo.getSSID()+"\n");
        s.append("Link speed: "+wifiInfo.getLinkSpeed()+"Mbps\n");
        s.append("MAC address: "+wifiInfo.getMacAddress()+"\n");
        s.append("IP Address: "+ipAddress+"\n");
        s.append("BSSID: "+wifiInfo.getBSSID()+"\n");
        s.append("WiFi Strength Level: "+level+"/5\n\n");



        TextView res = (TextView) findViewById(R.id.result);
        res.setText(s.toString());

        StringBuilder rss = new StringBuilder();
        level=0;
        TextView rs = (TextView) findViewById(R.id.rssi);
            for (int i = 0; i < 60; i++) {
                String time = DateFormat.getTimeInstance().format(new Date());
                wifiInfo = wifiManager.getConnectionInfo();
                level = wifiInfo.getRssi();
                rss.append(String.valueOf(level) + " dbm ");
                rss.append(String.valueOf(time)+"\n");


                //rs.setText(rss.toString());
            }

         if(isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
             File file=new File(Environment.getExternalStorageDirectory(),FILE_NAME);
             Log.v("External File", file.toString());

             try{
                 String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
                 FileOutputStream fos= new FileOutputStream(file);
                 fos.write((currentDateTimeString+" \n").getBytes());
                 fos.write(rss.toString().getBytes());
                 fos.close();
                 Toast.makeText(this,"File saved to: "+file.toString(),Toast.LENGTH_LONG).show();
             }catch(FileNotFoundException e){
                 e.printStackTrace();
             }catch(IOException e){
                 e.printStackTrace();
             }
         }

         else{
             Toast.makeText(this, "Cannot perform write operation, Permission denied", Toast.LENGTH_LONG).show();
         }

         /*File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);

         try{
             FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr);
             ArrayList<String> disp = new ArrayList<String>();
             String text;

             while((text=br.readLine())!=null){
                 disp.add(text);
             }
             ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,disp);
             ListView listView = (ListView) findViewById(R.id.display1);
             listView.setAdapter(adapter);
         }catch(FileNotFoundException e){
             e.printStackTrace();
         }catch(IOException e){
             e.printStackTrace();
         }*/




    }

    private boolean isExternalStorageWritable(){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            Log.v("State","Yes");
            return true;
        }
        else{
            return false;
        }
    }

    public boolean checkPermission(String permission){
        int check= ContextCompat.checkSelfPermission(this, permission);
        return (check== PackageManager.PERMISSION_GRANTED);
    }



    //OPEN FILE
    public void display(View view){
        Toast.makeText(this, "Opening file...", Toast.LENGTH_LONG).show();

//        File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);
//
//        Uri selectedUri = Uri.parse(file.getAbsolutePath());
//        Log.v("Sup",String.valueOf(selectedUri));
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//
//        intent.setDataAndType(selectedUri, "text/csv");
//        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
//        {
//            startActivity(intent);
//        }

        Intent intent = new Intent(Intent.ACTION_PICK);
        try{
            startActivityForResult(intent, 1);
        }catch(android.content.ActivityNotFoundException ex){
            Toast.makeText(this, "Please install a file manager...", Toast.LENGTH_SHORT).show();
        }
    }

    public class GenericFileProvider extends FileProvider {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Uri uri = null;
        switch(requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    uri = data.getData();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri,"text/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(intent);
                    Toast.makeText(this,uri.toString(),Toast.LENGTH_LONG).show();//returns file path
                }
                break;
        }
    }

    //REFRESH PAGE
    public void openPage(View view){
        Intent intent = new Intent(this, WiFiResults.class);
        startActivity(intent);

    }

    //MAIN PAGE
    public void goHome(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
