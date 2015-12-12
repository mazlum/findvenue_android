package com.app.konumbul.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

public class Login extends Activity{

    EditText editTextUserName, editTextPassword;
    TextView registerText;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ((TextView)findViewById(R.id.txtUserNameErrors)).setText("");
        ((TextView)findViewById(R.id.txtPasswordErrors)).setText("");
        btnLogin = (Button)findViewById(R.id.btnSignin);
        registerText = (TextView) findViewById(R.id.registerTextView);
        editTextUserName = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.txtUserNameErrors)).setText("");
                ((TextView)findViewById(R.id.txtPasswordErrros)).setText("");
                String username, password;
                username = editTextUserName.getText().toString();
                password = editTextPassword.getText().toString();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                PostData postData = new PostData(jsonObject, getApplicationContext());
                postData.execute();
            }
        });

        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(Login.this, Register.class);
                startActivity(registerIntent);
            }
        });
    }
    private class PostData extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        JSONObject jsonData;
        Context context;
        public PostData(JSONObject jsonData, Context context){
            this.jsonData = jsonData;
            this.context=context;
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(Global.webServerUrl + "/login/");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream());
                out.write(jsonData.toString());
                out.flush();

                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

            }catch( Exception e) {
                e.printStackTrace();
            }
            finally {
                urlConnection.disconnect();
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("konumBul", result);

            try {
                JSONObject resultObject = new JSONObject(result);
                if(resultObject.getString("status").equals("0")) {
                    if(resultObject.has("fail")){
                        Toast.makeText(getApplicationContext(), resultObject.getString("fail"), Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONObject errors = resultObject.getJSONObject("errors");
                    Iterator<?> keys = errors.keys();
                    while (keys.hasNext()){
                        String key = (String)keys.next();
                        if ( errors.get(key) instanceof JSONArray) {
                            JSONArray errorsArray = ((JSONArray) errors.get(key));
                            String errorsUsername="", errorsPassword="", allError="";
                            for (int i=0; i<errorsArray.length();i++){
                                if(key.toString().equals("username")){
                                    errorsUsername += errorsArray.get(i).toString();
                                }else if(key.toString().equals("password")){
                                    errorsPassword += errorsArray.get(i).toString();
                                }else if(key.toString().equals("__all__")){
                                    allError += errorsArray.get(i).toString();
                                }
                            }
                            if(key.toString().equals("username")){
                                ((TextView)findViewById(R.id.txtUserNameErrors)).setText(errorsUsername);
                            }else if(key.toString().equals("password")){
                                ((TextView)findViewById(R.id.txtPasswordErrors)).setText(errorsPassword);
                            }else if(key.toString().equals("__all__")){
                                Toast.makeText(getApplicationContext(), allError, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Başarı ile giriş yaptınız. Ana sayfaya yönlendiriliyorsunuz.", Toast.LENGTH_LONG).show();

                    SharedPreferences sharedpreferences = getSharedPreferences("registiration", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("token", resultObject.get("key").toString());
                    editor.commit();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent loginIntent = new Intent(Login.this, Venues.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(loginIntent);
                            finish();
                        }
                    }, 2000);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
