package com.armstrongltd.armstrongtv;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class LoadUrlActivity extends AppCompatActivity {

    private WebView webView;
    private TextView textViewErrorMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webview);
        textViewErrorMessage=(TextView)findViewById(R.id.tv_error_message);
        webView.setVisibility(View.GONE);
        textViewErrorMessage.setVisibility(View.GONE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        //get IP address of the tv and pass to webservice...
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        Log.e("TAG","IP address of tv is   "+ipAddress);

        /*webView.setVisibility(View.VISIBLE);
        webView.loadUrl("https://www.google.com/");*/

         //call webservice to get the URl from server...
         callWebServiceToGetUrl(ipAddress);

    }

    private void callWebServiceToGetUrl( String ipAddress) {

        // Tag used to cancel the request
        String tag_string_req = "request_url";
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching data...");
        pDialog.show();

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.BASE_URL+ipAddress, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                pDialog.dismiss();

                String jsonString=response;
                String jsonFormattedString = jsonString.replaceAll("\\\\", "");
                String removeQuotes= jsonFormattedString.replaceAll("^\"|\"$", "");

                try {

                    if (jsonString.isEmpty() || jsonString.length() == 0 || jsonString == null || jsonString.equalsIgnoreCase("null")) {

                        setErrorMessage();

                    } else {

                        JSONObject jsonObject = new JSONObject(removeQuotes);
                        String urlFromServer = jsonObject.getString("url");
                        Log.e("TAG","My URL IS JSON \n"+urlFromServer);

                        webView.setVisibility(View.VISIBLE);
                        webView.clearCache(true);

                        webView.loadUrl(urlFromServer);

                    }

                } catch (JSONException e) {
                    // JSON error
                    pDialog.dismiss();
                    e.printStackTrace();
                    setErrorMessage();
                    Log.d("TAG", "Error " + e);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();
                setErrorMessage();
                Log.d("TAG", "Error " + error);
            }
        }) {

           /* @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
              //  params.put("email", email);
               // params.put("password", password);

                return params;
            }*/

        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(
                40000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }

    private void setErrorMessage() {

        webView.setVisibility(View.GONE);
        textViewErrorMessage.setVisibility(View.VISIBLE);
        textViewErrorMessage.setText(R.string.error_message);

    }

    private void scheduledTask(String urlFromServer) {

        //Declare the timer
        Timer t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {

                                  @Override
                                  public void run() {
                                      //Called each time when 1000 milliseconds (1 second) (the period parameter)
                                      webView.post(new Runnable() {
                                          @Override
                                          public void run() {

                                              webView.clearCache(true);
                                              webView.loadUrl(urlFromServer);

                                              // webView.loadUrl("http://172.17.224.49/FlipkartNew/DashBoard/Livedashboard1");
                                              // webView.loadUrl("https://www.google.com/");

                                          }
                                      });

                                      Log.d("TAG", "Printed Message");
                                  }
                              },

                //Set how long before to start calling the TimerTask (in milliseconds)
                0,
                //Set the amount of time between each execution (in milliseconds)
                60000);
    }
}