package com.example.daylightquint1cd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;




import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class DayLight_MainActivity extends Activity implements LocationListener {

	private LocationManager lM;
	private int miliInHour = 36000000;
	Double latD, lonD;
	String latS, lonS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_day_light__main);

		lM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location loc = lM.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		fireANT(loc); // gets coorinates from last know location
		lM.requestLocationUpdates(LocationManager.GPS_PROVIDER, miliInHour,
				1000, this); // keeps requesting every hour

	}

	/* 
	 * when this method is called a new Asynctask is created and executes the
	 * openWeatherMap API based off of location coordinates
	 */

	private void fireANT(Location loc) {
		latD = loc.getLatitude();
		lonD = loc.getLongitude();
		latS = Double.toString(latD);
		lonS = Double.toString(lonD);

		ANT ant = new ANT();
		ant.execute("http://api.openweathermap.org/data/2.5/weather?lat="
				+ latS + "&lon=" + lonS);

	}

	@Override
	public void onLocationChanged(Location location) {

		String str = "Latitude: " + location.getLatitude() + " Longitude: "
				+ location.getLongitude();
		Toast.makeText(getBaseContext(), str, Toast.LENGTH_LONG).show();
		fireANT(location);

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}
	
	@Override
	public void onProviderDisabled(String provider) {
		
	}

	private class ANT extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... params) {
			return requestWebServiceJSON(params[0]);
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			super.onPostExecute(result);
		
			try {

				TextView sunriseTV = (TextView) findViewById(R.id.sunriseTV);
				TextView sunSetTV = (TextView) findViewById(R.id.sunSetTV);
				TextView hourLeft = (TextView) findViewById(R.id.hoursLeft);
				TextView hourLeftTV = (TextView) findViewById(R.id.hoursLeftTV);
				TextView totalHours = (TextView) findViewById(R.id.totalHours);
				TextView totalHoursTV = (TextView) findViewById(R.id.totalHoursTV);
				SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
				seekBar.setEnabled(false);

				String sunset = "", sunrise = "";
				result = result.getJSONObject("sys");
				sunrise = result.getString("sunrise");
				sunset = result.getString("sunset");

				double sunsetTS = Double.parseDouble(sunset);
				double sunriseTS = Double.parseDouble(sunrise);

				DateFormat dF = new SimpleDateFormat("hh:mm:ss aa");
				java.util.Date ssD = new java.util.Date((long) sunsetTS * 1000);
				java.util.Date srD = new java.util.Date((long) sunriseTS * 1000);
				Date nD = new Date();
				dF.format(nD);

				Calendar sunsetCal = new GregorianCalendar();
				sunsetCal.setTime(ssD);
				Calendar sunriseCal = new GregorianCalendar();
				sunriseCal.setTime(srD);
				Calendar nowCal = new GregorianCalendar();
				nowCal.setTime(nD);

				int sunSetHour = sunsetCal.get(Calendar.HOUR_OF_DAY);
				int sunriseHour = sunriseCal.get(Calendar.HOUR_OF_DAY);
				int nowHour = nowCal.get(Calendar.HOUR_OF_DAY);
				int hLeft = sunSetHour - nowHour;
				int totalDL = sunSetHour - sunriseHour;

				// String ssHS = Integer.toString(sunSetHour);

				String hlS = Integer.toString(hLeft);
				String totalDLS = Integer.toString(totalDL);

				String sRise = dF.format(srD);
				String sSet = dF.format(ssD);

				/*
				 * Condition for daylight hours, changes seekbar icon and sets
				 * seekbar max based off total hours of sunlight and sets
				 * progress to hours left till sunset
				 */
				if (nowHour < sunSetHour && nowHour > sunriseHour) {
					seekBar.setMax(totalDL);
					Drawable myThumb = getResources().getDrawable(
							R.drawable.ic_launcher);
					myThumb.setBounds(new Rect(0, 0, myThumb
							.getIntrinsicWidth(), myThumb.getIntrinsicHeight()));
					seekBar.setThumb(myThumb);
					seekBar.setProgress(hLeft);
					hourLeft.setText("Hours left till sunset:");
					sunriseTV.setText(sRise);
					sunSetTV.setText(sSet);
					hourLeftTV.setText(hlS);
					totalHours.setText("Total Hours of Sunlight: ");
					totalHoursTV.setText(totalDLS);

					/*
					 * Condition for moonlight hours, changes seekbar icon and
					 * sets seekbar max based off total hours of moonlight and
					 * sets progress to hours left till sunrise
					 */
				}
				if (nowHour > sunSetHour && nowHour < sunriseHour) {
					Drawable myThumb = getResources().getDrawable(
							R.drawable.moon);
					myThumb.setBounds(new Rect(0, 0, myThumb
							.getIntrinsicWidth(), myThumb.getIntrinsicHeight()));
					seekBar.setThumb(myThumb);

					int hoursML = (24 - sunSetHour) + sunriseHour;
					seekBar.setMax(hoursML);
					int tilsunrise = (24 - nowHour) + sunriseHour;
					seekBar.setProgress(tilsunrise);
					String tilSRS = Integer.toString(tilsunrise);
					String totalML = Integer.toString(hoursML);
					hourLeft.setText("Hours left till sunrise:");
					sunriseTV.setText(sRise);
					sunSetTV.setText(sSet);
					hourLeftTV.setText(tilSRS);
					totalHours.setText("Total Hours of MoonLight: ");
					totalHoursTV.setText(totalML);

				}

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

//	 Modeled after
//	 https://dylansegna.wordpress.com/2013/09/19/using-http-requests-to-get-json-objects-in-android/
	public static JSONObject requestWebServiceJSON(String stringRemoteURL) {
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(stringRemoteURL);
		StringBuilder stringBuilder = new StringBuilder();
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}
			} else {
				Log.e(DayLight_MainActivity.class.toString(),
						"Failed at JSON download.");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			return new JSONObject(stringBuilder.toString());
		} catch (JSONException e) {
			Log.e(DayLight_MainActivity.class.toString(),
					"Failed at JSON object.");
		}
		return null;
	}

	


	
}