package com.bool.auto;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Api {

    private Context context;
    private String URL = "https://api.tagncar.com/";
    //private String URL = "http://192.168.1.13:8080/";

    private static final String JSONRPC_PARAM_ID = "id";
    private static final String JSONRPC_PARAM_METHOD = "method";
    private static final String JSONRPC_PARAM_PARAMETERS = "params";

    Api(Context context) {
        this.context = context;
    }

    public void sendReply(String message, int conversation_id, final CallBackRequest success, final CallBackRequest error) {
        Log.v("TNC", "sendReply");
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("text", message);
            jsonRequest.put("conversation", conversation_id);
			
            jsonRpc("message.send", jsonRequest, success, error);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendRead(int conversation_id, final CallBackRequest success, final CallBackRequest error) {
        Log.v("TNC", "sendRead");
        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("id", conversation_id);
			
            jsonRpc("conversation.read", jsonRequest, success, error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private String getToken() {
        MySQLite dbHelper = new MySQLite(context);

        return dbHelper.getTokenUser();
    }

    private synchronized void jsonRpc(final String method, final JSONObject jsonRequest, final CallBackRequest success, final CallBackRequest callError) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL + "api.json",
                new JSONObject() {{
                        try {
                            put(JSONRPC_PARAM_ID, UUID.randomUUID().toString());
                            put(JSONRPC_PARAM_METHOD, method);
                            put(JSONRPC_PARAM_PARAMETERS, jsonRequest);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        success.response(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Log.v("TNC", "onErrorResponse: " + error.toString());
                            callError.response(new JSONObject().put("error", error.getMessage()));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap headers = new HashMap();
                headers.put("Content-Type", "application/json");
                headers.put("authorization", getToken());

                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(jsonObjectRequest);
    }

    interface CallBackRequest {
        void response(JSONObject data);
    }
}