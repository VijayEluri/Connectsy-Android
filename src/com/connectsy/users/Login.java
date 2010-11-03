package com.connectsy.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.ApiRequestListener;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager.DataUpdateListener;
import com.connectsy.data.DataManager;

public class Login extends Activity implements OnClickListener, 
		ApiRequestListener, DataUpdateListener {
	private ProgressDialog loadingDialog;
	static final int ACTIVITY_REGISTER = 0;
	private static final String TAG = "Login";
	private String username;
	private String password;
	private int LOGIN = 0;
	private int FETCH = 1;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.auth_login);

        Button signin = (Button)findViewById(R.id.auth_login_button);
        signin.setOnClickListener(this);
        
        TextView signup = (TextView)findViewById(R.id.auth_login_reg);
        signup.setOnClickListener(this);
    }
    
    public void onClick(View v){
    	if (v.getId() == R.id.auth_login_button){
	    	EditText usernameText = (EditText)findViewById(R.id.auth_login_username);
	    	EditText passwordText = (EditText)findViewById(R.id.auth_login_password);
	    	username = usernameText.getText().toString();
	    	password = passwordText.getText().toString();
	        doLogin();
	        
	        loadingDialog = ProgressDialog.show(this, "", "Signing in...", true);
    	}else if (v.getId() == R.id.auth_login_reg){
    		startActivityForResult(new Intent(this, Register.class), ACTIVITY_REGISTER);
    	}
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == ACTIVITY_REGISTER) {
            if (resultCode == RESULT_OK) {
            	Bundle data = intent.getExtras();
    	        username = data.getString("username");
    	        password = data.getString("password");
    	        doLogin();
            }
        }
    }
    private void doLogin(){
		ApiRequest r = new ApiRequest(this, this, Method.GET, "/token/", false, LOGIN );
		r.addGetArg("password", password);
		r.addGetArg("username", username);
		r.execute();
    }
    
	public void onApiRequestFinish(int status, String strResponse, int code){
		if (code == LOGIN){
			SharedPreferences data = DataManager.getCache(this);
	        SharedPreferences.Editor dataEditor = data.edit(); 
	        dataEditor.putString("token", strResponse);
	        dataEditor.putString("username", username);
	        dataEditor.commit();
	        
	        new UserManager(this, this, username).refreshUser(FETCH);
		}
	}

	public void onApiRequestError(int status, String response, int code) {
		if (loadingDialog != null) loadingDialog.dismiss();
		if (status == 404){
			AlertDialog.Builder alert = new AlertDialog.Builder(this); 
			alert.setMessage("Incorrect username or password, please try again."); 
			alert.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				    dialog.dismiss();
				}
			});
			alert.show();
		}else{
			Log.e(TAG, "an API error occured with httpStatus: "+
					Integer.toString(status));
		}
	}

	public void onDataUpdate(int code, String response) {
		if (loadingDialog != null) loadingDialog.dismiss();
		if (code == FETCH){
			SharedPreferences data = DataManager.getCache(this);
	        SharedPreferences.Editor dataEditor = data.edit(); 
	        dataEditor.putString("user", 
	        		new UserManager(this, this, username).getUser().serialize());
	        dataEditor.commit();
	        
	        //start the notification service
	        Intent i = new Intent();
			i.setAction("com.connectsy.START_NOTIFICATIONS");
			startService(i);

	        setResult(RESULT_OK);
	        this.finish();
		}
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		if (loadingDialog != null) loadingDialog.dismiss();
		// TODO Auto-generated method stub
	}
}