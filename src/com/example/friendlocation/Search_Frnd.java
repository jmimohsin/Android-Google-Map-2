package com.example.friendlocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class Search_Frnd extends ActionBarActivity implements OnItemClickListener {
	
	Button bt;
	EditText tv;
	ListView lv;
	private ProgressDialog pDialog;
	String frnd_name,frnd_location, frnd_lat, frnd_longi,my_name,my_id,frnd_id;
	int flag=0;
	Geocoder geo;
	List<Address> addresses;
	JSONParser jsonParser = new JSONParser();
	private static String url = "http://znsoftech.com/googlemap/new/search_frnd.php";
	private static final String TAG_SUCCESS = "success"; 
	private static final String TAG_ID = "id";
	private static final String TAG_NAME = "name";
	private static final String TAG_LAT = "lat";
	private static final String TAG_LONGI = "longi";
	private static final String TAG_DATE_TIME = "date_time";
	private static final String TAG_FRND_LIST = "frnd_list";
	ArrayList<HashMap<String, String>> all_list;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads().detectDiskWrites().detectNetwork()
		.penaltyLog().build());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_frnd);
		
		Intent i=getIntent();
		my_name=i.getStringExtra("name");
		my_id=	i.getStringExtra("id");	
		
		tv=(EditText)findViewById(R.id.editText1);
		bt=(Button)findViewById(R.id.button1);
		lv=(ListView)findViewById(R.id.listView1);
		all_list=new ArrayList<HashMap<String,String>>();
		geo=new Geocoder(getApplicationContext(), Locale.getDefault());
		
		bt.setOnClickListener(new View.OnClickListener() 
	        {			
			@Override
			public void onClick(View view) {
				
			if(tv.length()<2)
			{				
				Toast.makeText(Search_Frnd.this,"Enter correct name!", Toast.LENGTH_LONG).show();
				return;
			}
			//check connectivity		
			if(!isOnline(Search_Frnd.this))
			{					
				Toast.makeText(Search_Frnd.this,"No network connection!", Toast.LENGTH_LONG).show();
				return;	
			}
			all_list.clear();
			lv.setAdapter(null);
			new search_frnd_query().execute();
		  }
	   
			//code to check online details
			private boolean isOnline(Context mContext) {
			ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting())
		   	  {
				return true;
	     		}
			    return false;
	       	}
	      //Close code that check online details		
		  });
		
	}
	
	class search_frnd_query extends AsyncTask<String, String, String> {

		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(Search_Frnd.this);
			pDialog.setMessage("Searching Friends...");
			pDialog.setIndeterminate(false);
			//pDialog.setCancelable(true);
			pDialog.show();
		}
		@Override
		protected String doInBackground(String... arg0) {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			String frnd_name=tv.getText().toString();
			params.add(new BasicNameValuePair("frnd_name", frnd_name));
			
			JSONObject json = jsonParser.makeHttpRequest(url,"POST", params);
			Log.d("Create Response", json.toString());
			
			try {
				int success = json.getInt(TAG_SUCCESS);
				if (success == 1) 
				 {
				  flag=0;	
				  JSONArray frnd_list=json.getJSONArray(TAG_FRND_LIST);
				  for(int i=0; i<frnd_list.length(); i++)
				  {
					  JSONObject jo=frnd_list.getJSONObject(i);
					  String id=jo.getString(TAG_ID);
					  String name=jo.getString(TAG_NAME);
					  String lat=jo.getString(TAG_LAT);
					  String longi=jo.getString(TAG_LONGI);
					  String date_time=jo.getString(TAG_DATE_TIME);
					  HashMap<String, String> map=new HashMap<String, String>();
					  
					  map.put(TAG_ID, id);
					  map.put(TAG_NAME, name);
					  map.put(TAG_LAT, lat);
					  map.put(TAG_LONGI, longi);
					  map.put(TAG_DATE_TIME, date_time);
					  
					  all_list.add(map);
					  
				  }
				 }
				 else
				 {
					// failed to get friend list
					flag=1;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		protected void onPostExecute(String file_url) {
			pDialog.dismiss();
			if(flag==1)
				Toast.makeText(Search_Frnd.this,"Enter Correct informations!", Toast.LENGTH_LONG).show();
			else
			{
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						ListAdapter adapter=new SimpleAdapter(Search_Frnd.this, all_list, R.layout.name_list, new String[]{TAG_NAME, TAG_ID, TAG_LAT, TAG_LONGI, TAG_DATE_TIME}, new int[]{R.id.name, R.id.id, R.id.lat, R.id.longi, R.id.date_time});
						lv.setAdapter(adapter);
						lv.setOnItemClickListener(Search_Frnd.this);
					}
				});
			}
		}
		
	  }

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		String name=((TextView)view.findViewById(R.id.name)).getText().toString();
		String lat=((TextView)view.findViewById(R.id.lat)).getText().toString();
		String date_time=((TextView)view.findViewById(R.id.date_time)).getText().toString();
		String longi=((TextView)view.findViewById(R.id.longi)).getText().toString();
		String frndid=((TextView)view.findViewById(R.id.id)).getText().toString();
		frnd_lat=lat;
		frnd_id=frndid;
		frnd_longi=longi;
		frnd_location="Name: "+name+"\nDate Time: "+date_time;
		frnd_name=name;
		if(lat!=null && lat.length()>4)
		{
			
			if(Geocoder.isPresent())
			{
			try {
				addresses = geo.getFromLocation(Double.parseDouble(lat), Double.parseDouble(longi), 1);
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
					frnd_location="Name: "+name+"\nLattitude: "+lat+"\nLongitude: "+longi+"\nAddress: "+addressText+"\nLast Login: "+date_time;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
		  }
			
		}
		
		onCreateDialog(10);

	}
	
	@SuppressWarnings("deprecation")
	protected Dialog onCreateDialog(int id)
	{
	      switch(id)
	      {
	      case 10:
	            Builder builder=new AlertDialog.Builder(this);
	            builder.setMessage(frnd_location);
	            builder.setTitle("About "+frnd_name);
	//button
	      builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                 
	                  @Override
	                  public void onClick(DialogInterface dialog, int which)
	                  {
	                        
	                       
	                  }
	            });
	      
	      builder.setNegativeButton("Map Direction", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(frnd_lat!=null && frnd_lat.length()>4)
				{
					Intent i=new Intent(Search_Frnd.this, MoveToFriend.class);
					
					i.putExtra("frnd_lat", frnd_lat);
					i.putExtra("frnd_longi",frnd_longi);
					i.putExtra("frnd_name", frnd_name);
					i.putExtra("frnd_id", frnd_id);
					i.putExtra("name", my_name);
					i.putExtra("id", my_id);
					startActivity(i);
				}
				else
					Toast.makeText(getApplicationContext(), "Location is not available to show on Map!", Toast.LENGTH_SHORT).show();
			
			}
		});
	           
	      AlertDialog dialog=builder.create();
	      dialog.show();
	           
	      }
	      return super.onCreateDialog(id);     
	}


}
