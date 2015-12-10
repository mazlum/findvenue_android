package com.app.konumbul.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import android.os.Handler;

public class Register extends Activity{

    EditText editTextName, editTextUserName, editTextUserPassword, editTextUserMail, editTextUserPasswordAgain;
    Button registerButton;
    String name, username, password, passwordAgain, mail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editTextName = (EditText)findViewById(R.id.editTextName);
        editTextUserName = (EditText)findViewById(R.id.editTextUsername);
        editTextUserPassword = (EditText)findViewById(R.id.editTextPassword);
        editTextUserMail = (EditText)findViewById(R.id.editTextMail);
        editTextUserPasswordAgain = (EditText)findViewById(R.id.editTextPasswordAgain);

        registerButton = (Button)findViewById(R.id.btnRegister);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((TextView)findViewById(R.id.txtUserNameErrors)).setText("");
                ((TextView)findViewById(R.id.txtPasswordErrros)).setText("");
                ((TextView)findViewById(R.id.txtPasswordAgainErros)).setText("");
                ((TextView)findViewById(R.id.txtNameErrors)).setText("");
                ((TextView)findViewById(R.id.txtMailErrors)).setText("");
                name = editTextName.getText().toString();
                username = editTextUserName.getText().toString();
                password = editTextUserPassword.getText().toString();
                passwordAgain = editTextUserPasswordAgain.getText().toString();
                mail = editTextUserMail.getText().toString();

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("password1", password);
                    jsonObject.put("password2", passwordAgain);
                    jsonObject.put("first_name", name);
                    jsonObject.put("email", mail);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                PostData postData = new PostData(jsonObject);
                postData.execute();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class PostData extends AsyncTask<String, String, String> {

        HttpURLConnection urlConnection;

        JSONObject jsonData;

        public PostData(JSONObject jsonData){
            this.jsonData = jsonData;
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(Global.webServerUrl + "/register/");
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
                            String errorsUsername="", errorsName="", errorsPassword="", errorsPassword2="", errorsMail="";
                            for (int i=0; i<errorsArray.length();i++){
                                if(key.toString().equals("username")){
                                    errorsUsername += errorsArray.get(i).toString();
                                }else if(key.toString().equals("password1")){
                                    errorsPassword += errorsArray.get(i).toString();
                                }else if(key.toString().equals("password2")){
                                    errorsPassword2 += errorsArray.get(i).toString();
                                }else if(key.toString().equals("first_name")){
                                    errorsName += errorsArray.get(i).toString();
                                }else if(key.toString().equals("email")){
                                    errorsMail += errorsArray.get(i).toString();
                                }
                            }
                            if(key.toString().equals("username")){
                                ((TextView)findViewById(R.id.txtUserNameErrors)).setText(errorsUsername);
                            }else if(key.toString().equals("password1")){
                                ((TextView)findViewById(R.id.txtPasswordErrros)).setText(errorsPassword);
                            }else if(key.toString().equals("password2")){
                                ((TextView)findViewById(R.id.txtPasswordAgainErros)).setText(errorsPassword2);
                            }else if(key.toString().equals("first_name")){
                                ((TextView)findViewById(R.id.txtNameErrors)).setText(errorsName);
                            }else if(key.toString().equals("email")){
                                ((TextView)findViewById(R.id.txtMailErrors)).setText(errorsMail);
                            }
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Başarı ile kayıt oldunuz. Giriş ekranına yönlendiriliyorsunuz", Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final Intent loginIntent = new Intent(Register.this, Login.class);
                            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(loginIntent);
                            finish();
                        }
                    }, 5000);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
