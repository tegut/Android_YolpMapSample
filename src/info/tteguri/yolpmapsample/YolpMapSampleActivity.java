package info.tteguri.yolpmapsample;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import android.widget.ListView;
import android.widget.Toast;

import jp.co.yahoo.android.maps.*;
import jp.co.yahoo.android.maps.MapView.MapTouchListener;

public class YolpMapSampleActivity extends MapActivity {
    /** Called when the activity is first created. */
	
	
	private MapView mapView;
	private ListView listView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = new MapView(this,getResources().getString(R.string.yolp_appid));
        mapView.setBuiltInZoomControls(true);
        mapView.setLongPress(true);
        MapController c = mapView.getMapController();
        c.setCenter(new GeoPoint(35665721, 139731006)); //初期表示の地図を指定
        c.setZoom(3); 				  //初期表示の縮尺を指定
        
        FrameLayout mainLayout = (FrameLayout)findViewById(R.id.mapContainer);
        
        mapView.setMapTouchListener(new MapTouchListener() {
			
			@Override
			public boolean onTouch(MapView arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onPinchOut(MapView arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onPinchIn(MapView arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public boolean onLongPress(MapView arg0, Object arg1, PinOverlay arg2,
					GeoPoint arg3) {
				// TODO Auto-generated method stub
				
				ArrayList<Overlay> overlays = (ArrayList<Overlay>) arg0.getOverlays();
				overlays.clear();
				arg2.clearPoint();
				PinOverlay pinOverlay = new PinOverlay(PinOverlay.PIN_VIOLET);
				  mapView.getOverlays().add(pinOverlay);
				  pinOverlay.addPoint(arg3,null);
				

				StringBuilder url = new StringBuilder("http://search.olp.yahooapis.jp/OpenLocalPlatform/V1/localSearch?output=json");
				url.append("&appid=");
				url.append(getResources().getString(R.string.yolp_appid));
				url.append("&cid=6e6c4795b23a5e45540addb5ff6f0d00");
				url.append("&dist=1&sort=dist");
				url.append("&lat=");
				url.append(""+(float)(arg3.getLatitudeE6()/1000000.f));
				url.append("&lon=");
				url.append(""+(float)(arg3.getLongitudeE6()/1000000.f));
				
				HttpAsyncTask httpClient = new HttpAsyncTask();
				httpClient.execute(url.toString());
				
				return false;
			}
		});
        
        mainLayout.addView(mapView);
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
       
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public  class HttpAsyncTask extends AsyncTask<String, Integer, JSONObject>{

		private ProgressDialog progressDialog;
		
		@Override
		protected void onPostExecute(JSONObject result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			//Log.d("result",result.toString());
			
			 mapView.getOverlays().add(new YDFOverlay(result));
			 
			 ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
			 adapter.clear();
			 try {
				 Toast.makeText(YolpMapSampleActivity.this, 
						 ""+result.getJSONObject("ResultInfo").getString("Count")+"件見つかりました", Toast.LENGTH_SHORT).show();
				JSONArray feature = result.getJSONArray("Feature");
				for(int i =0; i < feature.length();i++){
					JSONObject item = feature.getJSONObject(i);
					adapter.add(item.getString("Name"));
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			
			progressDialog = new ProgressDialog(YolpMapSampleActivity.this);
	        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        progressDialog.setMessage("Send Request...");
	        progressDialog.setCancelable(true);
	        progressDialog.show();
	        
		}

		@Override
		protected JSONObject doInBackground(String... params) {
			// TODO Auto-generated method stub
			synchronized (this) {

				DefaultHttpClient httpClient = new DefaultHttpClient();
				// StringBuilder urlString = new
				// StringBuilder("http://search.olp.yahooapis.jp/OpenLocalPlatform/V1/localSearch?cid=6e6c4795b23a5e45540addb5ff6f0d00&device=mobile");
				HttpGet request = new HttpGet(params[0]);

				try {
					JSONObject result = httpClient.execute(request,
							new ResponseHandler<JSONObject>() {

								@Override
								public JSONObject handleResponse(
										HttpResponse response)
										throws ClientProtocolException,
										IOException {
									// TODO Auto-generated method stub
									switch (response.getStatusLine()
											.getStatusCode()) {
									case HttpStatus.SC_OK:

										try {
											String data = EntityUtils
													.toString(response
															.getEntity(),"UTF-8");
											Log.d("data",data);
											return new JSONObject(data);
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

									case HttpStatus.SC_NOT_FOUND:
										throw new RuntimeException("not found");

									default:
										throw new RuntimeException("error");
									}
								}
							});
					return result;
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		}

	}
}