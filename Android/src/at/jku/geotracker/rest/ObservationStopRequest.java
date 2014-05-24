package at.jku.geotracker.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import at.jku.geotracker.application.Globals;
import at.jku.geotracker.fragment.UserListFragment;
import at.jku.geotracker.rest.model.ResponseObjectFragment;
import at.jku.geotracker.rest.model.ObservationModel;

public class ObservationStopRequest extends
		AsyncTask<ObservationModel, ResponseObjectFragment, ResponseObjectFragment> {

	@Override
	protected ResponseObjectFragment doInBackground(ObservationModel... params) {
		HttpClient httpclient = HTTPSClient.getNewHttpClient();
		HttpPut put = new HttpPut(Globals.restUrl + "/observation/stop");
		put.setHeader("content-type", "application/json; charset=UTF-8");
		put.setHeader("Accept", "application/json");
		JSONObject dato = new JSONObject();
		try {
			dato.put("username", Globals.username);
			dato.put("password", Globals.password);
			dato.put("isObserver", true);
			dato.put("secondUsername", params[0].getObserved());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		StringEntity entity = null;
		try {
			entity = new StringEntity(dato.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		put.setEntity(entity);

		HttpResponse resp = null;
		int statusCode = 500;
		try {
			resp = httpclient.execute(put);
			statusCode = resp.getStatusLine().getStatusCode();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String respStr = null;
		try {
			respStr = EntityUtils.toString(resp.getEntity());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseObjectFragment(respStr, params[0].getUserListFragment(),
				statusCode);
	}

	@Override
	protected void onPostExecute(ResponseObjectFragment result) {
		super.onPostExecute(result);
		((UserListFragment) result.getFragment()).requestFinishedObservation(result);
	}
}
