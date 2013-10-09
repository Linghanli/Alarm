package com.ece.alarmmanager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;

public class RetrieveXML extends AsyncTask<String,Void,String>{
	public AsyncResponse delegate=null;

	@Override
	protected String doInBackground(String... arg0) {
		String xml = null;
		try{
		DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(arg0[0]);

        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        xml = EntityUtils.toString(httpEntity);

    } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
    } catch (ClientProtocolException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
     return xml;
	}
	
	 @Override
	   protected void onPostExecute(String result) {
	      delegate.processFinish(result);
	   }

}
