package com.example.friendlocation;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	
	Button login,signin;
	private EditText mobile_number,password;
	private ProgressDialog pDialog;
	int flag=0;
	SharedPreferences sp;
	JSONParser jsonParser = new JSONParser();
	private static String url = "http://znsoftech.com/googlemap/new/login.php";
	private static final String TAG_SUCCESS = "success"; 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads().detectDiskWrites().detectNetwork()
		.penaltyLog().build());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		sp=getSharedPreferences("location", Activity.MODE_PRIVATE);
		
	//Go To Signin.java	
		signin=(Button)findViewById(R.id.signin);	
        signin.setOnClickListener(new View.OnClickListener() 
        {		
			@Override
			public void onClick(View view) {
				Intent i = new Intent(getApplicationContext(), Signin.class);
				startActivity(i);
				
			}
		}); 
    // Close Signin.java
        
     //Get all data and log in 
    	login=(Button)findViewById(R.id.login);	
    	mobile_number=(EditText)findViewById(R.id.mobile_number);
    	password=(EditText)findViewById(R.id.password);
    	
        login.setOnClickListener(new View.OnClickListener() 
        {			
			@Override
			public void onClick(View view) {
				
		//Check all fields		
				if(mobile_number.length()<10)
				{
					Toast.makeText(MainActivity.this,"Enter correct mobile number!", Toast.LENGTH_LONG).show();
					return;
				}
				 if(password.length()<4)
				{				
					Toast.makeText(MainActivity.this,"Enter correct password!", Toast.LENGTH_LONG).show();
					return;
				}
		//check connectivity		
				 if(!isOnline(MainActivity.this))
				{					
					Toast.makeText(MainActivity.this,"No network connection!", Toast.LENGTH_LONG).show();
					return;	
				}
		
		//from login.java		
					new loginAccess().execute();
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
        //Close log in 
    }


class loginAccess extends AsyncTask<String, String, String> {

	protected void onPreExecute() {
		super.onPreExecute();
		pDialog = new ProgressDialog(MainActivity.this);
		pDialog.setMessage("Login...");
		pDialog.setIndeterminate(false);
		//pDialog.setCancelable(true);
		pDialog.show();
	}
	@Override
	protected String doInBackground(String... arg0) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		String number=mobile_number.getText().toString();
		String pwd=password.getText().toString();
		params.add(new BasicNameValuePair("mobile_number", number));
		params.add(new BasicNameValuePair("password", pwd));
		JSONObject json = jsonParser.makeHttpRequest(url,"POST", params);
		Log.d("Create Response", json.toString());
		
		try {
			int success = json.getInt(TAG_SUCCESS);
			if (success == 1) 
			 {
			  flag=0;	
			  int id=json.getInt("id");
			  String name=json.getString("name");
			  
			  Intent i = new Intent(getApplicationContext(),Main_Welcome.class);
			  i.putExtra("name",name);
			  i.putExtra("mobile_number",number);
			  i.putExtra("id", id+"");
			  sp.edit().putString("id", id+"").commit();
			  startActivity(i);
			  finish();
			 }
			 else
			 {
				// failed to login
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
			Toast.makeText(MainActivity.this,"Enter Correct informations!", Toast.LENGTH_LONG).show();
		
	}
	
  }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

}

