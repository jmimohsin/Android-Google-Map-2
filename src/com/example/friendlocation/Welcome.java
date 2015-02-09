package com.example.friendlocation;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapFragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Welcome extends ActionBarActivity {
	
	GoogleMap map;
	boolean flag=false;
	String name,mobile_number,id;
	JSONParser jsonParser = new JSONParser();
	private static String url = "http://znsoftech.com/googlemap/new/location.php";
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		Intent i=getIntent();
	    name=i.getStringExtra("name");
		id=i.getStringExtra("id");
	    mobile_number=i.getStringExtra("mobile_number");
		
		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		if(map==null)
			Toast.makeText(getApplicationContext(),"Sorry! unable to create Map.", Toast.LENGTH_SHORT).show();
		else
		{
			map.setMyLocationEnabled(true);
			map.getUiSettings().setZoomControlsEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(true);
			//map.getUiSettings().setCompassEnabled(true);
			
			map.setOnMyLocationChangeListener(new OnMyLocationChangeListener() {
				
				@Override
				public void onMyLocationChange(Location location) {
					// TODO Auto-generated method stub
					LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
					MarkerOptions mo=new MarkerOptions().position(loc).title(name+", you are here!").snippet(""+loc);
					map.clear();
					map.addMarker(mo);
				
					if(flag==false)
					{
					  map.moveCamera(CameraUpdateFactory.newLatLng(loc));
					  map.animateCamera(CameraUpdateFactory.zoomTo(15));
					  flag=true;
					}
					
					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("id", id));
					params.add(new BasicNameValuePair("lat", location.getLatitude()+""));
					params.add(new BasicNameValuePair("longi", location.getLongitude()+""));
					JSONObject json=jsonParser.makeHttpRequest(url,"POST", params);
				
					try {
						int success = json.getInt("success");
						if (success == 1) 
							Toast.makeText(getApplicationContext(), "Updated!!!", Toast.LENGTH_SHORT).show();
						else
							Toast.makeText(getApplicationContext(), "Error!!!", Toast.LENGTH_SHORT).show();

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			});
		}
	}

}
