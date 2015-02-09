package com.example.friendlocation;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
public class MoveToFriend extends ActionBarActivity // implements TextToSpeech.OnInitListener
{
	
	GoogleMap map;
	String name,id,frnd_name,frnd_lat,frnd_longi,frnd_id;
	LatLng my_latlong,frnd_latlong;
	TextView tv;
	//private TextToSpeech tts;
	ArrayList<LatLng> directionPoint;
	int size_of_latlong,latlong_index=0;
	ArrayList<Polyline> polylines;
	String direction="walking";
	Document doc;
	SensorManager sensor_manager;
	//private int result=0;
	MarkerOptions mo,frnd_mo;
	Marker my_marker,frnd_marker;
	boolean first_time_flag=false, update_flag=false;
	
	@SuppressLint("NewApi") @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		Intent i=getIntent();
	    name=i.getStringExtra("name");
		id=i.getStringExtra("id");
		frnd_lat=i.getStringExtra("frnd_lat");
		frnd_longi=i.getStringExtra("frnd_longi");
		frnd_name=i.getStringExtra("frnd_name");
		frnd_id=i.getStringExtra("frnd_id");
		
		polylines=new ArrayList<Polyline>();
		
		//tts = new TextToSpeech(this, this);
		
		tv=(TextView)findViewById(R.id.textView1);
		tv.setVisibility(View.VISIBLE);
		tv.setText("Wait...");
		frnd_latlong=new LatLng(Double.parseDouble(frnd_lat), Double.parseDouble(frnd_longi));
		
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
					if(update_flag==true)
					{
						my_latlong = new LatLng(location.getLatitude(), location.getLongitude());
						
						if(latlong_index==size_of_latlong)
						{
							//speakOut("you are very close to your friend.");
							return;
						}

						float bearing = getBearing(my_latlong, directionPoint.get(latlong_index));
			        	
						mo=new MarkerOptions().position(my_latlong).title(name+", you are here!").snippet(""+my_latlong)
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).flat(true).rotation(bearing+180);
						my_marker.remove();
						my_marker=map.addMarker(mo);
						//map.moveCamera(CameraUpdateFactory.newLatLng(my_latlong));
					
						CameraPosition.Builder cameraBuilder = new CameraPosition.Builder()
							.target(my_latlong).bearing(bearing);
			        	
			        	cameraBuilder.tilt(map.getCameraPosition().tilt);
			        	cameraBuilder.zoom(map.getCameraPosition().zoom);
			        	CameraPosition cameraPosition = cameraBuilder.build();
			        	
			        	map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

						if(getDistanceinDouble(directionPoint.get(latlong_index),my_latlong)<15.0f)
						{
							polylines.get(latlong_index).remove();
							latlong_index++;
						}
						
						if(getDistanceinDouble(directionPoint.get(latlong_index),my_latlong)>200.0f)
						{
							first_time_flag=false;
							update_flag=false;
							return;
						}
						
						if(latlong_index<size_of_latlong)
						{
							String direction=getDirection(my_latlong,directionPoint.get(latlong_index));
							tv.setText("Go to "+direction+" ("+getDistance(my_latlong,frnd_latlong)+")");
							//speakOut("Go to "+direction);
						}
						
					}
					
					if(first_time_flag==false)
					{
						latlong_index=0;
						my_latlong = new LatLng(location.getLatitude(), location.getLongitude());
						mo=new MarkerOptions().position(my_latlong).title(name+", you are here!").snippet(""+my_latlong)
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).flat(true).rotation(245);
						frnd_mo=new MarkerOptions().position(frnd_latlong).title(frnd_name+" is here!").snippet(""+frnd_latlong)
								.icon(BitmapDescriptorFactory.defaultMarker(
				        	    BitmapDescriptorFactory.HUE_GREEN));
						
						map.clear();
						my_marker=map.addMarker(mo);
						frnd_marker=map.addMarker(frnd_mo);
						
						map.moveCamera(CameraUpdateFactory.newLatLng(my_latlong));
						map.animateCamera(CameraUpdateFactory.zoomTo(16));
						
						new Request_Update().execute(location);
						first_time_flag=true;
						update_flag=true;
					}
				}
				
			private float getBearing(LatLng begin, LatLng end) {
					double lat = Math.abs(begin.latitude - end.latitude); 
			        double lng = Math.abs(begin.longitude - end.longitude);
			    	 if(begin.latitude < end.latitude && begin.longitude < end.longitude)
				    	return (float)(Math.toDegrees(Math.atan(lng / lat)));
					else if(begin.latitude >= end.latitude && begin.longitude < end.longitude)
						return (float)((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
					else if(begin.latitude >= end.latitude && begin.longitude >= end.longitude)
						return  (float)(Math.toDegrees(Math.atan(lng / lat)) + 180);
					else if(begin.latitude < end.latitude && begin.longitude >= end.longitude)
						return (float)((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
			    	 return -1;
			    }
				
			private String getDistance(LatLng my_latlong,LatLng frnd_latlong){
					Location l1=new Location("One");
		      		l1.setLatitude(my_latlong.latitude);
		      		l1.setLongitude(my_latlong.longitude);
		      		     
		      		Location l2=new Location("Two");
		      		l2.setLatitude(frnd_latlong.latitude);
		      		l2.setLongitude(frnd_latlong.longitude);
		      		     
		      		float distance=l1.distanceTo(l2);
		      		String dist=distance+" M";
		      		
		      		if(distance>1000.0f)
		      		{
		      			distance=distance/1000.0f;
		      			dist=distance+" KM";
		      		}
		      		return dist;
				}
				
			private float getDistanceinDouble(LatLng my_latlong,LatLng frnd_latlong){
					Location l1=new Location("One");
		      		l1.setLatitude(my_latlong.latitude);
		      		l1.setLongitude(my_latlong.longitude);
		      		     
		      		Location l2=new Location("Two");
		      		l2.setLatitude(frnd_latlong.latitude);
		      		l2.setLongitude(frnd_latlong.longitude);
		      		     
		      		float distance=l1.distanceTo(l2);

		      		return distance;
				}
				
			private String getDirection(LatLng my_latlong,LatLng frnd_latlong) {
					// TODO Auto-generated method stub
					double my_lat=my_latlong.latitude;
					double my_long=my_latlong.longitude;
					
					double frnd_lat=frnd_latlong.latitude;
					double frnd_long=frnd_latlong.longitude;
					
					double radians=getAtan2((frnd_long-my_long),(frnd_lat-my_lat));
					double compassReading = radians * (180 / Math.PI);

				    String[] coordNames = {"North", "North-East", "East", "South-East", "South", "South-West", "West", "North-West", "North"};
				    int coordIndex = (int) Math.round(compassReading / 45);
				   
				    if (coordIndex < 0) {
				    	coordIndex = coordIndex + 8;
				    };

				    return coordNames[coordIndex]; // returns the coordinate value
				}
				
				private double getAtan2(double longi,double lat) {
			        return Math.atan2(longi, lat);
			    }
			});
		}
	}
/*
	@Override  
	public void onDestroy() {
	  // Don't forget to shutdown!
	  if (tts != null) {
	    tts.stop();
	    tts.shutdown();
	   }
	   super.onDestroy();
	  }
	 
	@Override
	public void onInit(int status) {
	  // TODO Auto-generated method stub
	  if (status == TextToSpeech.SUCCESS) {
		  result = tts.setLanguage(Locale.US);
		  if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
			  Toast.makeText(this, "Missing TTS data", Toast.LENGTH_LONG).show();
		  } 
	    }
	  }
	  
    private void speakOut(String text) {
	  if(result!=tts.setLanguage(Locale.US))
	  {
		 // Toast.makeText(getApplicationContext(), "Enter right Words...... ", Toast.LENGTH_LONG).show();
	  }else{
		  tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	   }
	  }
    */
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
      menu.add(0,0,0, "Normal Mode");
      menu.add(0,1,1, "Hybrid Mode");
      menu.add(0,2,2, "Terrain Mode");
      menu.add(0,3,3, "Satellite Mode");
      menu.add(0,4,4, "With Traffic");
      menu.add(0,5,5, "Without traffic");
      menu.add(0,6,6, "Walking Direction");
      menu.add(0,7,7, "Driving Direction");

      return super.onCreateOptionsMenu(menu);
    }
	
	public boolean onOptionsItemSelected(MenuItem item)
    {
      switch(item.getItemId())
      { 
      	case 0:
      		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
      		break;
      	case 1:
      		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
      		break;
      	case 2:
      		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
      		break;
      	case 3:
      		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
      		break;
      	case 4:
      		map.setTrafficEnabled(true);
      		break;
      	case 5:
      		map.setTrafficEnabled(false);
      		break;
      	case 6:
      		direction="walking";
      		first_time_flag=false;
			update_flag=false;
      		break;
      	case 7:
      		direction="driving";
      		first_time_flag=false;
			update_flag=false;
      		break;
      }
      return true;
    }
	
	public class Request_Update extends AsyncTask<Location, Void, Location>
	{
		@Override
		protected void onPreExecute()
		{
			//Toast.makeText(getApplicationContext(), "onPreExecute()!", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected Location doInBackground(Location... location) {
			// TODO Auto-generated method stub
			
			String url = "http://maps.googleapis.com/maps/api/directions/xml?" 
	                + "origin=" + location[0].getLatitude() + "," + location[0].getLongitude()  
	                + "&destination=" + frnd_lat + "," + frnd_longi 
	                + "&sensor=false&units=metric&mode="+direction;

	        try {
	            HttpClient httpClient = new DefaultHttpClient();
	            HttpContext localContext = new BasicHttpContext();
	            HttpPost httpPost = new HttpPost(url);
	            HttpResponse response = httpClient.execute(httpPost, localContext);
	            InputStream in = response.getEntity().getContent();
	            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	            doc = builder.parse(in);
	        } catch (Exception e) {    
	       }
	        
			return location[0];
		}
		
		@Override
		protected void onPostExecute(Location location)
		{
			if(doc!=null)
			{
				directionPoint=getDirection(doc);
				int ii = 0;
				size_of_latlong=directionPoint.size();
				for( ; ii <size_of_latlong ; ii++) {    
					if(ii==0)
					{
						PolylineOptions rectLine = new PolylineOptions().width(8).color(Color.RED);
						rectLine.add(my_latlong,directionPoint.get(ii));
						Polyline polyline=map.addPolyline(rectLine);
						polylines.add(polyline);
					}
					else
					{
						PolylineOptions rectLine = new PolylineOptions().width(8).color(Color.RED);
						rectLine.add(directionPoint.get(ii-1),directionPoint.get(ii));
						Polyline polyline=map.addPolyline(rectLine);
						polylines.add(polyline);
					}
				}	
				PolylineOptions rectLine = new PolylineOptions().width(8).color(Color.RED);
				rectLine.add(frnd_latlong,directionPoint.get(ii-1));
				Polyline polyline=map.addPolyline(rectLine);
				polylines.add(polyline);
				//map.addPolyline(rectLine);
			}
		}
		
	}
	
	public ArrayList<LatLng> getDirection(Document doc) {
        NodeList nl1, nl2, nl3;
        ArrayList<LatLng> listGeopoints = new ArrayList<LatLng>();
        nl1 = doc.getElementsByTagName("step");
        if (nl1.getLength() > 0) {
            for (int i = 0; i < nl1.getLength(); i++) {
                Node node1 = nl1.item(i);
                nl2 = node1.getChildNodes();

                Node locationNode = nl2.item(getNodeIndex(nl2, "start_location"));
                nl3 = locationNode.getChildNodes();
                Node latNode = nl3.item(getNodeIndex(nl3, "lat"));
                double lat = Double.parseDouble(latNode.getTextContent());
                Node lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                double lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));

                locationNode = nl2.item(getNodeIndex(nl2, "polyline"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "points"));
                ArrayList<LatLng> arr = decodePoly(latNode.getTextContent());
                for(int j = 0 ; j < arr.size() ; j++) {
                    listGeopoints.add(new LatLng(arr.get(j).latitude, arr.get(j).longitude));
                }

                locationNode = nl2.item(getNodeIndex(nl2, "end_location"));
                nl3 = locationNode.getChildNodes();
                latNode = nl3.item(getNodeIndex(nl3, "lat"));
                lat = Double.parseDouble(latNode.getTextContent());
                lngNode = nl3.item(getNodeIndex(nl3, "lng"));
                lng = Double.parseDouble(lngNode.getTextContent());
                listGeopoints.add(new LatLng(lat, lng));
            }
        }
        
        return listGeopoints;
    }
	
	private int getNodeIndex(NodeList nl, String nodename) {
        for(int i = 0 ; i < nl.getLength() ; i++) {
            if(nl.item(i).getNodeName().equals(nodename))
                return i;
        }
        return -1;
    }
	
	private ArrayList<LatLng> decodePoly(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
   
            LatLng position = new LatLng((double)lat / 1E5, (double)lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
}