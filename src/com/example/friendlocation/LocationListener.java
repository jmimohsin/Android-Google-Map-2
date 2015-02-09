package com.example.friendlocation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class LocationListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent==null || intent.getAction()==null)
			return;
		
		if(isOnline(context))
		{
			intent=new Intent(context, UpdateLocationService.class);
			intent.putExtra("Started", "YES");
			context.startService(intent);
			Toast.makeText(context, "online & Started", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private boolean isOnline(Context mContext) {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting())
	   	  {
			return true;
	   	  }
		 return false;
       	}

}
