package com.example.friendlocation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class Frnd_Direction extends ActionBarActivity {
	
	GoogleMap map;
	String name,id,frnd_name,frnd_lat,frnd_longi,frnd_id;
	LatLng my_latlong,frnd_latlong;
	
	JSONParser jsonParser = new JSONParser();
	private static String url = "http://znsoftech.com/googlemap/new/location.php";
	private static String frnd_url = "http://znsoftech.com/googlemap/new/get_frnd_update_loc.php";

	PolylineOptions rectLine;
	ArrayList<LatLng> directionPoint;
	Document doc;
	MarkerOptions mo,frnd_mo;
	
	private int zoom = -1;
	private int animateSpeed = -1;
	private boolean isAnimated = false;
	private double animateDistance = -1;
	private double animateCamera = -1;
	private int step = -1;
	private Polyline animateLine = null;
	private double totalAnimateDistance = 0;
	private boolean cameraLock = false;
	private OnAnimateListener mAnimateListener = null;
	private boolean flatMarker = false;
	private boolean isCameraTilt = false;
	private boolean isCameraZoom = false;
	private ArrayList<LatLng> animatePositionList = null;
	private boolean drawMarker = false;
	private boolean drawLine = false;
	
	
	private LatLng animateMarkerPosition = null;
	private LatLng beginPosition = null;
	private LatLng endPosition = null;
	private Marker animateMarker = null; 	
	
	public final static int SPEED_VERY_FAST = 1;
    public final static int SPEED_FAST = 2;
    public final static int SPEED_NORMAL = 3;
    public final static int SPEED_SLOW = 4;
    public final static int SPEED_VERY_SLOW = 5;
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	@Override
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

		rectLine = new PolylineOptions().width(3).color(Color.RED);
		
		LocationManager lm=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
		Criteria c=new Criteria();
		String provider=lm.getBestProvider(c, false);
		Location location=lm.getLastKnownLocation(provider);
		
		map=((MapFragment)getFragmentManager().findFragmentById(R.id.map)).getMap();
		
		if(map==null && location==null)
			Toast.makeText(getApplicationContext(),"Sorry! unable to create Map.", Toast.LENGTH_SHORT).show();
		else
		{
			map.setMyLocationEnabled(true);
			map.getUiSettings().setZoomControlsEnabled(true);
			map.getUiSettings().setMyLocationButtonEnabled(true);
			
			my_latlong = new LatLng(location.getLatitude(), location.getLongitude());
			mo=new MarkerOptions().position(my_latlong).title(name+", you are here!").snippet(""+my_latlong);
			
			frnd_latlong = new LatLng(Double.parseDouble(frnd_lat), Double.parseDouble(frnd_longi));
			frnd_mo=new MarkerOptions().position(frnd_latlong).title(frnd_name+" is here!").snippet(""+frnd_latlong)
					.icon(BitmapDescriptorFactory.defaultMarker(
	        	    BitmapDescriptorFactory.HUE_GREEN));
			
			map.clear();
			map.addMarker(mo);
			map.addMarker(frnd_mo);
			
			map.moveCamera(CameraUpdateFactory.newLatLng(frnd_latlong));
			map.animateCamera(CameraUpdateFactory.zoomTo(15));

			lm.requestLocationUpdates(provider, 1000, 10, new LocationListener() {
				
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
					Toast.makeText(getApplicationContext(), "Location changed!", Toast.LENGTH_SHORT).show();		
					new Request_Update().execute(location);	
				}
			});
			
		}	
	}
	
	public class Request_Update extends AsyncTask<Location, Void, Location>
	{
		@Override
		protected void onPreExecute()
		{
			Toast.makeText(getApplicationContext(), "onPreExecute()!", Toast.LENGTH_SHORT).show();
		}
		@Override
		protected Location doInBackground(Location... location) {
			// TODO Auto-generated method stub
			
			//update my location on server...............
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("id", id));
			params.add(new BasicNameValuePair("lat", location[0].getLatitude()+""));
			params.add(new BasicNameValuePair("longi", location[0].getLongitude()+""));
			jsonParser.makeHttpRequest(url,"POST", params);

			//Get friend updated location..........
			List<NameValuePair> frnd_params = new ArrayList<NameValuePair>();
			frnd_params.add(new BasicNameValuePair("frnd_id", frnd_id));
			JSONObject frnd_json=jsonParser.makeHttpRequest(frnd_url,"POST", frnd_params);
		
			try {
				int success = frnd_json.getInt("success");
				if (success == 1) 
				{
					frnd_lat=frnd_json.getString("lat");
					frnd_longi=frnd_json.getString("longi");
				}
			} catch (JSONException e) {
		   }
		   /////update path................................
			
			String url = "http://maps.googleapis.com/maps/api/directions/xml?" 
	                + "origin=" + location[0].getLatitude() + "," + location[0].getLongitude()  
	                + "&destination=" + frnd_lat + "," + frnd_longi 
	                + "&sensor=false&units=metric&mode=driving";

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
			//.....................................
			my_latlong = new LatLng(location.getLatitude(), location.getLongitude());
			mo=new MarkerOptions().position(my_latlong).title(name+", you are here!").snippet(""+my_latlong);
			
			frnd_latlong = new LatLng(Double.parseDouble(frnd_lat), Double.parseDouble(frnd_longi));
			frnd_mo=new MarkerOptions().position(frnd_latlong).title(frnd_name+" is here!").snippet(""+frnd_latlong)
					.icon(BitmapDescriptorFactory.defaultMarker(
			        	    BitmapDescriptorFactory.HUE_GREEN));
			
			map.clear();
			if(doc!=null)
			{
				directionPoint=getDirection(doc);
				for(int ii = 0 ; ii < directionPoint.size() ; ii++) {          
					rectLine.add(directionPoint.get(ii));
					}
					
					map.addPolyline(rectLine);
			}
			
			map.addMarker(mo);
			map.addMarker(frnd_mo);
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
                    listGeopoints.add(new LatLng(arr.get(j).latitude
                            , arr.get(j).longitude));
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
      menu.add(0,0,0, "Distance");
      menu.add(0,1,1, "Direction");
      return super.onCreateOptionsMenu(menu);
    }

	public boolean onOptionsItemSelected(MenuItem item)
    {
      switch(item.getItemId())
      { 
      	case 0:
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
      		
			Toast.makeText(getApplicationContext(), dist, Toast.LENGTH_SHORT).show();

      		break;
      	case 1:
      		animateDirection( directionPoint, SPEED_VERY_FAST
    				, true, false, true, true, new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.car)), false, true, new PolylineOptions().width(3).color(Color.GREEN));
      		break;
      }
      return true;
    }
	
	public void animateDirection(ArrayList<LatLng> direction, int speed
			, boolean cameraLock1, boolean isCameraTilt1, boolean isCameraZoom1
			, boolean drawMarker1, MarkerOptions mo, boolean flatMarker1
			, boolean drawLine1, PolylineOptions po) {
		if(direction.size() > 1) {
			
			isAnimated = true;
			animatePositionList = direction;
			animateSpeed = speed;
			drawMarker = drawMarker1;
			drawLine = drawLine1;
			flatMarker = flatMarker1;
			isCameraTilt = isCameraTilt1;
			isCameraZoom = isCameraZoom1;
			step = 0;
			cameraLock = cameraLock1;
				
			setCameraUpdateSpeed(speed);
			
			beginPosition = direction.get(step);
			endPosition = direction.get(step + 1);
			animateMarkerPosition = beginPosition;
			
			if(mAnimateListener != null)
				mAnimateListener.onProgress(step, direction.size());
			
	        if(cameraLock) {
				float bearing = getBearing(beginPosition, endPosition);
	        	CameraPosition.Builder cameraBuilder = new CameraPosition.Builder()
					.target(animateMarkerPosition).bearing(bearing);

	        	if(isCameraTilt) 
	        		cameraBuilder.tilt(90);
	        	else 
	        		cameraBuilder.tilt(map.getCameraPosition().tilt);

	        	if(isCameraZoom) 
	        		cameraBuilder.zoom(zoom);
	        	else 
	        		cameraBuilder.zoom(map.getCameraPosition().zoom);
	        	
	        	CameraPosition cameraPosition = cameraBuilder.build();
	        	map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
	        }
	        
	        if(drawMarker) {
	        	if(mo != null)
	        		animateMarker = map.addMarker(mo.position(beginPosition));
	        	else 
	        		animateMarker = map.addMarker(new MarkerOptions().position(beginPosition));
	        	
	        	if(flatMarker) {
		        	animateMarker.setFlat(true);

		        	float rotation = getBearing(animateMarkerPosition, endPosition) + 180;
		        	animateMarker.setRotation(rotation);
		        }
	        }
	        
	        
	        if(drawLine) {
	        	if(po != null) 
	        		animateLine = map.addPolyline(po.add(beginPosition)
			        		.add(beginPosition).add(endPosition)
			        		.width(dpToPx((int)po.getWidth())));
	        	else 
		        	animateLine = map.addPolyline(new PolylineOptions()
    						.width(dpToPx(5)));
	        }
	        
			new Handler().postDelayed(r, speed);
			if(mAnimateListener != null)
				mAnimateListener.onStart();
		}
	}
	
	private LatLng getNewPosition(LatLng begin, LatLng end) {
		double lat = Math.abs(begin.latitude - end.latitude); 
        double lng = Math.abs(begin.longitude - end.longitude);
        
        double dis = Math.sqrt(Math.pow(lat, 2) + Math.pow(lng, 2));
        if(dis >= animateDistance) {
	        double angle = -1;
	        
	        if(begin.latitude <= end.latitude && begin.longitude <= end.longitude)
	    		angle = Math.toDegrees(Math.atan(lng / lat));
			else if(begin.latitude > end.latitude && begin.longitude <= end.longitude)
	    		angle = (90 - Math.toDegrees(Math.atan(lng / lat))) + 90;
			else if(begin.latitude > end.latitude && begin.longitude > end.longitude)
	    		angle = Math.toDegrees(Math.atan(lng / lat)) + 180;
			else if(begin.latitude <= end.latitude && begin.longitude > end.longitude)
	    		angle = (90 - Math.toDegrees(Math.atan(lng / lat))) + 270;
			
	        double x = Math.cos(Math.toRadians(angle)) * animateDistance;
	        double y = Math.sin(Math.toRadians(angle)) * animateDistance;
	        totalAnimateDistance += animateDistance;
	        double finalLat = begin.latitude + x;
	        double finalLng = begin.longitude + y;
	        
	        return new LatLng(finalLat, finalLng);
        } else {
        	return end;
        }
    }
	
	private Runnable r = new Runnable() {
    	public void run() {
    		
    		animateMarkerPosition = getNewPosition(animateMarkerPosition, endPosition);

	        if(drawMarker)
	        	animateMarker.setPosition(animateMarkerPosition);

	        
	        if(drawLine) {
	        	List<LatLng> points = animateLine.getPoints();
	        	points.add(animateMarkerPosition);
	        	animateLine.setPoints(points);
	        }
    
    		if((animateMarkerPosition.latitude == endPosition.latitude 
    				&& animateMarkerPosition.longitude == endPosition.longitude)) {
    			if(step == animatePositionList.size() - 2) {
    				isAnimated = false;
    				totalAnimateDistance = 0;
    				if(mAnimateListener != null)
    					mAnimateListener.onFinish();
    			} else {
    				step++;
    				beginPosition = animatePositionList.get(step);
    				endPosition = animatePositionList.get(step + 1);
    				animateMarkerPosition = beginPosition;
    				
    		        if(flatMarker && step + 3 < animatePositionList.size() - 1) {
    		        	float rotation = getBearing(animateMarkerPosition, animatePositionList.get(step + 3)) + 180;
    		        	animateMarker.setRotation(rotation);
    		        }
    				
    				if(mAnimateListener != null)
    					mAnimateListener.onProgress(step, animatePositionList.size());
    			}
    		}
    		
    		if(cameraLock && (totalAnimateDistance > animateCamera || !isAnimated)) {
				totalAnimateDistance = 0;
				float bearing = getBearing(beginPosition, endPosition);
	        	CameraPosition.Builder cameraBuilder = new CameraPosition.Builder()
					.target(animateMarkerPosition).bearing(bearing);

	        	if(isCameraTilt) 
	        		cameraBuilder.tilt(90);
	        	else 
	        		cameraBuilder.tilt(map.getCameraPosition().tilt);

	        	if(isCameraZoom) 
	        		cameraBuilder.zoom(zoom);
	        	else 
	        		cameraBuilder.zoom(map.getCameraPosition().zoom);
	        	
	        	CameraPosition cameraPosition = cameraBuilder.build();
	        	map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				
			}
    		
    		if(isAnimated) {
    			new Handler().postDelayed(r, animateSpeed);
    		}
    	}
    };
	
	public interface OnAnimateListener {
	    public void onFinish();
	    public void onStart();
	    public void onProgress(int progress, int total);
	}
	
	private int dpToPx(int dp) {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));       
	    return px;
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
	
	public void setCameraUpdateSpeed(int speed) {		
		if(speed == SPEED_VERY_SLOW) {
			animateDistance = 0.000005;
			animateSpeed = 20;
			animateCamera = 0.0004;
			zoom = 19;
		} else if(speed == SPEED_SLOW) {
			animateDistance = 0.00001;
			animateSpeed = 20;
			animateCamera = 0.0008;
			zoom = 18;
		} else if(speed == SPEED_NORMAL) {
			animateDistance = 0.00005;
			animateSpeed = 20;
			animateCamera = 0.002;
			zoom = 16;
		} else if(speed == SPEED_FAST) {
			animateDistance = 0.0001;
			animateSpeed = 20;
			animateCamera = 0.004;
			zoom = 15;
		} else if(speed == SPEED_VERY_FAST) {
			/*animateDistance = 0.0005;
			animateSpeed = 20;
			animateCamera = 0.004;
			zoom = 13;
			*/
			animateDistance = 0.001;
			animateSpeed = 100;
			animateCamera = 0.04;
			zoom = 15;
		} else {
			animateDistance = 0.00005;
			animateSpeed = 20;
			animateCamera = 0.002;
			zoom = 16;
		}
    }
    
}