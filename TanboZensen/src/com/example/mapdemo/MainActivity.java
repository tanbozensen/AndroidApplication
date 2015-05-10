package com.example.mapdemo;

import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import android.location.*;
import android.content.*;
import android.util.Log;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.json.JSONObject;
import org.json.JSONArray;
import android.os.StrictMode;
import java.io.IOException;
import org.apache.http.entity.StringEntity;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends FragmentActivity implements LocationListener, OnClickListener {
	//六甲山：北緯34度46分41秒, 東経135度15分49秒
	private double mLatitude = 34.0d + 46.0d/60 + 41.0d/(60*60);
	private double mLongitude = 135.0d + 15.0d/60 + 49.0d/(60*60);
	private GoogleMap mMap = null;
	private Marker mMarker;
	private String sPhase = "0";
	private String sPhaseJString = "田植え";
	private final String sPhaseTaue = "0";
	private BitmapDescriptor mIcon;
    private static final int GET_MAX_SIZE  = 50;
	LocationManager locman;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bottom_layout).setVisibility(View.INVISIBLE);
        View seekButton = findViewById(R.id.seek_button);
        seekButton.setOnClickListener(this);
        View setPinButton = findViewById(R.id.setpin_button);
        setPinButton.setOnClickListener(this); 
        View getButton = findViewById(R.id.button_get);
        getButton.setOnClickListener(this); 
        View deleteButton = findViewById(R.id.button_del); 
        deleteButton.setOnClickListener(this); 
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(RadioGroup group, int checkedId) {
		    	RadioButton radioButton = (RadioButton)findViewById(checkedId);
		    	boolean checked = radioButton.isChecked();
		        switch (radioButton.getId()) {
		        case R.id.RadioButton1:
		            if (checked) {
		            	sPhase = "0";
		            	sPhaseJString = "田植え";
		            	// mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
		            	mIcon = BitmapDescriptorFactory.fromResource(R.drawable.nae_72x72);
		            }
		            break;
		        case R.id.RadioButtoni2:
		            if (checked) {
		            	sPhase = "1";
		            	sPhaseJString = "稲刈り";
		            	//mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
		            	mIcon = BitmapDescriptorFactory.fromResource(R.drawable.ine_72x72);
		            }
		            break;
		        default:
		            break;
		    }		    }
        });
        // mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
    	mIcon = BitmapDescriptorFactory.fromResource(R.drawable.nae_72x72);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
		
        locman = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map) ).getMap();    
        if (mMap != null) {
        	LatLng location = new LatLng(mLatitude, mLongitude);
        	CameraPosition cameraPos = new CameraPosition.Builder()
        	.target(location).zoom(10.0f)
        	.bearing(0).build();
        	mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

        	// マーカータップ時のイベントハンドラ登録
        	mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
        		@Override
        		public boolean onMarkerClick(Marker marker) {
        	        findViewById(R.id.bottom_layout).setVisibility(View.VISIBLE);
        	        mMarker = marker;
        			return false;
        		}
        	});
        	// 地図タップ時のイベントハンドラ登録
        	mMap.setOnMapClickListener(new OnMapClickListener() {
        		@Override
        		public void onMapClick(LatLng point) {
        	        findViewById(R.id.bottom_layout).setVisibility(View.INVISIBLE);
        		}
        	});        	
        }
    }

    @Override public void onClick(View v) {
    	switch(v.getId()){ 
    		case R.id.seek_button:
    	        Toast.makeText(this, "現在地取得開始・・・", Toast.LENGTH_LONG).show();
    	        if (locman != null){
    	            locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);          
    	        }
    	        break;
    		case R.id.button_del:
    	        findViewById(R.id.bottom_layout).setVisibility(View.INVISIBLE);
    	        String sRemoveId;
    	        sRemoveId = mMarker.getSnippet();

    	        try {    	        	
	    	        String delUrl = "http://tanbozensen.herokuapp.com/api/tanbos/" + sRemoveId;
	    	        HttpDelete deleteMethod = new HttpDelete(delUrl);
	    	        DefaultHttpClient httpDelClient = new DefaultHttpClient();
        			Toast.makeText(getApplicationContext(), "delUrl："+delUrl, Toast.LENGTH_SHORT).show();
            	    Log.d("DelteteButton", "delUrl："+delUrl);

    				deleteMethod.addHeader("Content-type", "application/json");
    				// deleteMethod.setHeader("Content-Type", "text/plain; charset=utf-8");

    				// 要求を出して応答を取得する
    		        String result = httpDelClient.execute(deleteMethod, new ResponseHandler<String>(){
    		            public String handleResponse(HttpResponse response) throws IOException{
    		                switch(response.getStatusLine().getStatusCode()){
    		                case HttpStatus.SC_OK:
    		                    return EntityUtils.toString(response.getEntity(), "UTF-8");
    		                case HttpStatus.SC_NOT_FOUND:
//    		                    return "404";
    		                    return EntityUtils.toString(response.getEntity(), "UTF-8");
    		                default:
//    		                    return "unknown";
    		                    return EntityUtils.toString(response.getEntity(), "UTF-8");
    		                }
    		            }
    		        });
            	    Log.d("DelteteResult", result);

    	    		// マーカーの削除
        			mMarker.remove();
        	        Toast.makeText(this, "田んぼ情報を削除しました\nID:"+sRemoveId, Toast.LENGTH_LONG).show();
    	    	} catch (ClientProtocolException e) {
    	    		e.printStackTrace();
    	    		return;
    	    	} catch (IOException e) {
    	    		e.printStackTrace();
    	    		return;
    	    	}
    	        break;
    		case R.id.button_get:
    	        Toast.makeText(this, "田んぼ情報の取得開始・・・", Toast.LENGTH_LONG).show();
    			HttpClient httpGetClient = new DefaultHttpClient();
    			StringBuilder urlGet = new StringBuilder("http://tanbozensen.herokuapp.com/api/tanbos?year=2015");
            	HttpGet getRequest = new HttpGet(urlGet.toString());
            	HttpResponse httpResponse = null;

            	try {
            	    httpResponse = httpGetClient.execute(getRequest);
            	} catch (Exception e) {
            	    Log.d("JSONSampleActivity", "Error Execute");
            	    return;
            	}            	
            	
            	int status = httpResponse.getStatusLine().getStatusCode();

            	String json = null;
            	if (httpResponse != null && HttpStatus.SC_OK == status) {
            	    try {
            	    	HttpEntity httpEntity = httpResponse.getEntity();
            	    	json = EntityUtils.toString(httpEntity);            	    	
            	    	JSONArray jsonArray = new JSONArray(json);

            	    	mMap.clear(); // マーカー全削除
            	    	int arraySize = jsonArray.length();
            	    	double tmpLatitude;
            	    	double tmpLongitude;
            	    	String sLatitude;
            	    	String sLongitude;
            	    	String done_date;
            	    	String phase;
            	    	String sId;
            	        for(int i = 0; i < arraySize; i++)
            	        { 
            	        	if(i<GET_MAX_SIZE) {
	            	            JSONObject jsonObj = jsonArray.getJSONObject(arraySize-i-1);
	            	            done_date = jsonObj.getString("done_date");
	            	            sLatitude = jsonObj.getString("latitude");
	            	            tmpLatitude = Double.parseDouble(sLatitude);
	            	            sLongitude = jsonObj.getString("longitude");
	            	            tmpLongitude = Double.parseDouble(sLongitude);
	            	            phase = jsonObj.getString("phase");
	            	            sId = jsonObj.getString("id");
	            	    		if (phase.equals(sPhaseTaue)){
	            	    			sPhaseJString = "田植え";
	            	    			mIcon = BitmapDescriptorFactory.fromResource(R.drawable.nae_72x72);
	            	    		}
	            	    		else {
	            	    			sPhaseJString = "稲刈り";
	            	    			mIcon = BitmapDescriptorFactory.fromResource(R.drawable.ine_72x72);	            	    			
	            	    		}
            	            	LatLng location = new LatLng(tmpLatitude, tmpLongitude);
            	            	MarkerOptions options = new MarkerOptions();
            	            	options.position(location);
            	            	options.title(sPhaseJString + ":" + done_date);
            	            	options.icon(mIcon);
            	            	options.snippet(sId);
            	            	options.draggable(true);
            	            	mMarker = mMap.addMarker(options);
            	            	// インフォウィンドウ表示
            	            	mMarker.showInfoWindow();
            	        	}
            	        	else {
            	        		break;
            	        	}
            	        }
            	    	Log.d("JSONSampleActivity", "OK");
            			Toast.makeText(getApplicationContext(), "田んぼ情報の更新を完了しました", Toast.LENGTH_SHORT).show();
            	    } catch (Exception e) {
            	    	e.printStackTrace();
						Log.d("JSONSampleActivity", "Error");
            	    }
            	} else {
            	    Log.d("JSONSampleActivity", "Status" + status);
            	    return;
            	}
            	
            	break;
    		case R.id.setpin_button:
            	// マーカー設定
    			String sNewId = "";
    			
    			CameraPosition cameraPos = mMap.getCameraPosition();
    			mLatitude = cameraPos.target.latitude;
    			mLongitude = cameraPos.target.longitude;

    			String sLatitude = Double.toString(mLatitude);
    			String sLongitude = Double.toString(mLongitude);
    			Date date = new Date();
    			SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
    			String sDate = sdformat.format(date);
//            	Toast.makeText(this, "JSON用文字列\n緯度:" + sLatitude + "\n経度:" + sLongitude + "\n日付:" + sDate, Toast.LENGTH_LONG).show();
    			 
            	String url="http://tanbozensen.herokuapp.com/api/tanbos/";
    			HttpClient httpClient = new DefaultHttpClient();
    			HttpPost post = new HttpPost(url);
    			     			
    			try{
//    				String strJson = "{ \"latitude\": 10.0290141, \"longitude\": 129.0290141, \"phase\": 1, \"rice_type\": 1, \"done_date\": \"2015-04-29\" }";
    				String strJson = "{ \"latitude\": " + sLatitude + ", \"longitude\": " + sLongitude + ", \"phase\": " + sPhase + ", \"rice_type\": 1, \"done_date\": \"" + sDate + "\" }";
    				StringEntity body = new StringEntity(strJson);
    				post.addHeader("Content-type", "application/json");
    				post.setEntity(body);
    		        String result = httpClient.execute(post, new ResponseHandler<String>(){
    		            public String handleResponse(HttpResponse response) throws IOException{
    		                switch(response.getStatusLine().getStatusCode()){
    		                case HttpStatus.SC_OK:
    		                    return EntityUtils.toString(response.getEntity(), "UTF-8");
    		                case HttpStatus.SC_NOT_FOUND:
//    		                    return "404";
    		                    return EntityUtils.toString(response.getEntity(), "UTF-8");
    		                default:
//    		                    return "unknown";
    		                    return EntityUtils.toString(response.getEntity(), "UTF-8");
    		                }
    		            }
    		        });
    			    Toast.makeText(this, "登録を完了しました", Toast.LENGTH_LONG).show();
//    			    Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    			    JSONObject replyJsonObject = new JSONObject(result);
//    			    Toast.makeText(this, "id:" + replyJsonObject.getString("id"), Toast.LENGTH_LONG).show();
    			    sNewId = replyJsonObject.getString("id");
    			    
    			} catch (Exception e) {
    			    e.printStackTrace();
    			    Toast.makeText(this, "登録を失敗しました", Toast.LENGTH_LONG).show();
    			}

    			// マーカー設定
    			if (mMap != null) {
	            	LatLng location = new LatLng(mLatitude, mLongitude);
	            	MarkerOptions options = new MarkerOptions();
	            	options.position(location);
	            	options.title(sPhaseJString + ":" + sDate);
	            	options.icon(mIcon);
	            	options.snippet(sNewId);
	            	mMarker = mMap.addMarker(options);
	            	// インフォウィンドウ表示
	            	mMarker.showInfoWindow();
//	            	Toast.makeText(this, "マーカー設置\n緯度:" + cameraPos.target.latitude + "\n経度:" + cameraPos.target.longitude, Toast.LENGTH_LONG).show();
    			}
            	
    			break;
    	}
    }    
    
    @Override
    protected void onResume(){
        if (locman != null){
            locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);          
        }
        super.onResume();
    }
     
    @Override
    protected void onPause(){
        if (locman != null){
            locman.removeUpdates(this);
        }
        super.onPause();
    }
    
    @Override
    public void onLocationChanged(Location location){
    	LatLng Position;
    	
        Log.v("----------", "----------");
        Log.v("Latitude", String.valueOf(location.getLatitude()));
        Toast.makeText(this, String.valueOf(location.getLatitude()), Toast.LENGTH_LONG).show();
        Log.v("Longitude", String.valueOf(location.getLongitude()));
        Toast.makeText(this, String.valueOf(location.getLongitude()), Toast.LENGTH_LONG).show();
        Log.v("Accuracy", String.valueOf(location.getAccuracy()));
        Log.v("Altitude", String.valueOf(location.getAltitude()));
        Log.v("Time", String.valueOf(location.getTime()));
        Log.v("Speed", String.valueOf(location.getSpeed())); 
        Log.v("Bearing", String.valueOf(location.getBearing()));

        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();

    	//画面位置を取得先に移動する
    	Position = new LatLng(mLatitude, mLongitude);
    	CameraPosition cameraPos = new CameraPosition.Builder()
    	.target(Position).zoom(10.0f)
    	.bearing(0).build();
    	mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
        
    	//listenerの削除
        if (locman != null){
            locman.removeUpdates(this);
        }
    }
     
    @Override
    public void onProviderDisabled(String provider){
     
    }
     
    @Override
    public void onProviderEnabled(String provider){
    }
     
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
        switch(status){
        case LocationProvider.AVAILABLE:
            Log.v("Status","AVAILABLE");
            break;
        case LocationProvider.OUT_OF_SERVICE:
            Log.v("Status","OUT_OF_SERVICE");
            break;
        case  LocationProvider.TEMPORARILY_UNAVAILABLE:
            Log.v("Status","TEMPORARILY_UNAVAILABLE");
            break;
             
        }
    }
}
