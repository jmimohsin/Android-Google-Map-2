package com.example.friendlocation;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

public class UpdateLocationService extends Service {

	String url = "http://znsoftech.com/googlemap/new/location.php";
	JSONParser jsonParser = new JSONParser();
	String id;
	
	 @Override
	 public void onCreate() {
		 	super.onCreate();
			Toast.makeText(getApplicationContext(), "In onCreate()!", Toast.LENGTH_SHORT).show();
	   }
	 
	@SuppressWarnings("deprecation")
	@Override
		public void onStart(Intent intent, int startId){
		 	super.onStart(intent, startId);
			Toast.makeText(getApplicationContext(), "In onStart()!", Toast.LENGTH_SHORT).show();
			getresult();
	 }
	 
	  private void getresult() {
		// TODO Auto-generated method stub
		  SharedPreferences sp=getSharedPreferences("location", Activity.MODE_PRIVATE);
			id=sp.getString("id", null);
		    if(id!=null)
		    {
		    	Toast.makeText(getApplicationContext(), "In id!"+id, Toast.LENGTH_SHORT).show();
		    	 LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		    	 Criteria c=new Criteria();
		    	 String provider=lm.getBestProvider(c, false);
		    	 lm.requestSingleUpdate(provider, new LocationListener() {
					
					@Override
					public void onStatusChanged(String provider, int status, Bundle extras) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderEnabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onProviderDisabled(String provider) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onLocationChanged(Location location) {
						// TODO Auto-generated method stub
						List<NameValuePair> params = new ArrayList<NameValuePair>();
		 				params.add(new BasicNameValuePair("id", id));
		 				params.add(new BasicNameValuePair("lat", location.getLatitude()+""));
		 				params.add(new BasicNameValuePair("longi", location.getLongitude()+""));
		 				JSONObject json=jsonParser.makeHttpRequest(url,"POST", params);
		 				Toast.makeText(getApplicationContext(), "In onLocationChanged()!", Toast.LENGTH_SHORT).show();
		 				try {
		 					int success = json.getInt("success");
		 					if (success == 1) 
		 					{
		 						Toast.makeText(getApplicationContext(), "Updated in Service!", Toast.LENGTH_SHORT).show();
		 						stopSelf();
		 					}
		 					else
		 						Toast.makeText(getApplicationContext(), "Not updated in Service!", Toast.LENGTH_SHORT).show();
		 				} catch (JSONException e) {
		 					// TODO Auto-generated catch block
		 					//e.printStackTrace();
	 						Toast.makeText(getApplicationContext(), "Error in JsonException Service!", Toast.LENGTH_SHORT).show();
	 						stopSelf();
		 				}
					}
				},null);
		    }
	}

	@Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
		  super.onStartCommand(intent, flags, startId);
		if(intent==null || intent.getAction()==null)
    		return Service.START_NOT_STICKY;
		Toast.makeText(getApplicationContext(), "In onStartCommand()!", Toast.LENGTH_SHORT).show();
		getresult();
	    return Service.START_NOT_STICKY;
   }

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	 @Override
	   public void onDestroy() {
	      super.onDestroy();
	      Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
	   }

}
