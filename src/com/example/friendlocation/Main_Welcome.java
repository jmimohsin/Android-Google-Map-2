package com.example.friendlocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Main_Welcome extends ActionBarActivity{
	
	TextView tv;
	LocationManager lm;
	String provider, mylocation,name,mobile_number,id;
	Criteria c;
	Geocoder geo;
	List<Address> addresses;
	Intent i;
	JSONParser jsonParser = new JSONParser();
	private static String url = "http://znsoftech.com/googlemap/new/location.php";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_welcome);
		tv=(TextView)findViewById(R.id.textView1);
		Intent i=getIntent();
	    name=i.getStringExtra("name");
		id=i.getStringExtra("id");
	    mobile_number=i.getStringExtra("mobile_number");
		
		tv.setText("Welcome "+name);
		
		geo=new Geocoder(getApplicationContext(), Locale.getDefault());
		
		lm=(LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		c=new Criteria();
		provider=lm.getBestProvider(c, false);
		
		lm.requestSingleUpdate(provider, new LocationListener() {
			
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String arg0) {
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
			
				try {
					int success = json.getInt("success");
					if (success == 1) 
						Toast.makeText(getApplicationContext(), "Updated in welcome page!", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(getApplicationContext(), "Error in welcome page!", Toast.LENGTH_SHORT).show();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
		}, null);
	/*	
		lm.requestLocationUpdates(provider, 0, 0, new LocationListener() {
			
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
			
				try {
					int success = json.getInt("success");
					if (success == 1) 
						Toast.makeText(getApplicationContext(), "Updated in welcome page!", Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(getApplicationContext(), "Error in welcome page!", Toast.LENGTH_SHORT).show();

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				
			}
		});
		*/
		
	}
	
	public void action(View v)
	{
		switch(v.getId())
		{
		case R.id.button1:
			i=new Intent(this, Welcome.class);
			i.putExtra("name",name);
		    i.putExtra("mobile_number",mobile_number);
		    i.putExtra("id", id);
			startActivity(i);
			break;
		case R.id.button2:
			Location l=lm.getLastKnownLocation(provider);
			if(l!=null)
			{
				mylocation="Lattitude: "+l.getLatitude()+" Longitude: "+l.getLongitude();
				
				if(Geocoder.isPresent())
				{
				try {
					Toast.makeText(getApplicationContext(), "In Try!", Toast.LENGTH_SHORT).show();
					addresses = geo.getFromLocation(l.getLatitude(), l.getLongitude(), 1);
					if (addresses != null && addresses.size() > 0) 
					{
						Address address = addresses.get(0);
						String addressText = String.format(
		                        "%s, %s, %s",
		                        // If there's a street address, add it
		                        address.getMaxAddressLineIndex() > 0 ?
		                                address.getAddressLine(0) : "",
		                        // Locality is usually a city
		                        address.getLocality(),
		                        // The country of the address
		                        address.getCountryName());
						mylocation="Lattitude: "+l.getLatitude()+" Longitude: "+l.getLongitude()+"\nAddress: "+addressText;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					Toast.makeText(getApplicationContext(), "In Exception!", Toast.LENGTH_SHORT).show();
				}
			  }
			}
			else
				mylocation="No provider enabled!";
			onCreateDialog(10);
			break;
			
		case R.id.button3:
			i=new Intent(this, Search_Frnd.class);
			i.putExtra("name",name);
		    i.putExtra("mobile_number",mobile_number);
		    i.putExtra("id", id);
			startActivity(i);
			break;
		}
	 }
	
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
	      switch(id)
	      {
	      case 10:
	            Builder builder=new AlertDialog.Builder(this);
	            builder.setMessage(mylocation);
	            builder.setTitle("My Location");
	//button
	      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                 
	                  @Override
	                  public void onClick(DialogInterface dialog, int which)
	                  {
	                        
	                       
	                  }
	            });
	           
	      AlertDialog dialog=builder.create();
	      dialog.show();
	           
	      }
	      return super.onCreateDialog(id);     
	}

}
