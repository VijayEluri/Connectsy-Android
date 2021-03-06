package com.connectsy2.events;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.connectsy2.ActionBarHandler;
import com.connectsy2.R;
import com.connectsy2.data.Analytics;
import com.connectsy2.data.DataManager.DataUpdateListener;
import com.connectsy2.events.EventManager.Event;
import com.connectsy2.settings.MainMenu;
import com.connectsy2.users.UserManager;
import com.connectsy2.users.UserSelector;
import com.connectsy2.users.ContactCursor.Contact;
import com.connectsy2.utils.DateUtils;
import com.connectsy2.utils.TimePickerDialog;
import com.connectsy2.utils.TimePickerDialog.OnTimeSetListener;

public class EventNew extends Activity implements OnClickListener, 
		DataUpdateListener, OnTimeSetListener {
	@SuppressWarnings("unused")
	private final String TAG = "NewEvent";
	private ProgressDialog loadingDialog;
    private EventManager eventManager;
    private ArrayList<String> chosenUsers;
    private ArrayList<Contact> chosenContacts;
    private JSONObject eventJSON;
	
    private static final int TIME_DIALOG_ID = 0;
	private static final int SELECT_CONTACTS = 1;
	private static final int SELECT_FRIENDS = 3;
	private static final int CREATE_EVENT = 4;
    
    private Long timestamp;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_new);
        Analytics.pageView(this, this.getClass().getName());

        //set up logo clicks
        new ActionBarHandler(this);
        
        ((EditText) findViewById(R.id.events_new_what))
        		.addTextChangedListener(new TextWatcher(){
        			public void afterTextChanged(Editable s) {}
        			public void beforeTextChanged(CharSequence s, int start, 
        					int count, int after) {}
        			public void onTextChanged(CharSequence s, int start, 
        					int before, int count) {
        				((TextView)findViewById(R.id.ab_char_counter))
        						.setText(Integer.toString(140-s.length()));
        			}
                });
        ((TextView) findViewById(R.id.events_new_where))
        		.addTextChangedListener(new TextWatcher(){
        			public void afterTextChanged(Editable s) {}
        			public void beforeTextChanged(CharSequence s, int start, 
        					int count, int after) {}
        			public void onTextChanged(CharSequence s, int start, 
        					int before, int count) {
        				((TextView)findViewById(R.id.ab_char_counter))
        						.setText(Integer.toString(25-s.length()));
        			}
                });
        
        View publicRadio = findViewById(R.id.events_new_public);
        publicRadio.setOnClickListener(this);
        publicRadio.setSelected(true);
        findViewById(R.id.events_new_private).setOnClickListener(this);
        findViewById(R.id.events_new_friends_selected).setOnClickListener(this);
        findViewById(R.id.events_new_submit).setOnClickListener(this);
        findViewById(R.id.events_new_when).setOnClickListener(this);
        findViewById(R.id.events_new_sms).setOnClickListener(this);
    }

	public void onClick(View v) {
		int id = v.getId();
        if (id == R.id.events_new_when){
        	showDialog(TIME_DIALOG_ID);
	    }else if (id == R.id.events_new_friends_selected){
        	selectFriends();
	    }else if (id == R.id.events_new_sms){
        	selectContacts();
	    }else if (id == R.id.events_new_submit){
        	submitData();
	    }else if (id == R.id.events_new_public){
        	setWho("public");
	    }else if (id == R.id.events_new_private){
        	setWho("private");
	    }
	}
	
	private void selectFriends(){
		Intent i = new Intent(Intent.ACTION_CHOOSER);
		i.setType("vnd.android.cursor.item/vnd.connectsy.user");
		if (chosenUsers != null){
			i.putExtra("com.connectsy2.users", 
					UserSelector.serializeUsers(chosenUsers));
		}
		startActivityForResult(i, SELECT_FRIENDS);
	}
	
	private void selectContacts(){
		Intent i = new Intent(Intent.ACTION_CHOOSER);
		i.setType("vnd.android.cursor.item/vnd.connectsy.contact");
		if (chosenContacts != null)
			i.putExtra("com.connectsy2.contacts", 
					Contact.serializeList(chosenContacts));
		startActivityForResult(i, SELECT_CONTACTS);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		try {
			if (resultCode == RESULT_OK && requestCode == SELECT_FRIENDS){
				Bundle e = data.getExtras();
				chosenUsers = UserSelector.deserializeUsers(
						e.getString("com.connectsy2.users"));
				
				String display = chosenUsers.size()+" follower";
				if (chosenUsers.size() != 1)
					display += "s";
				display += " selected";
				
				((TextView)findViewById(R.id.events_new_friends_selected))
					.setText(display);
			}else if (resultCode == RESULT_OK && requestCode == SELECT_CONTACTS){
				Bundle e = data.getExtras();
				chosenContacts = Contact.deserializeList(
						e.getString("com.connectsy2.contacts"));
				
				String display = chosenContacts.size()+" contact";
				if (chosenContacts.size() != 1)
					display += "s";
				display += " selected";
				
				((TextView)findViewById(R.id.events_new_sms)).setText(display);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == TIME_DIALOG_ID){
        	return new TimePickerDialog(this, this, null);
        }else{
            return null;
        }
    }
    
	public void onTimeSet(Long timestamp) {
		this.timestamp = timestamp;
		((TextView) findViewById(R.id.events_new_when))
				.setText(DateUtils.formatTimestamp(timestamp));
	}
    
    private void setEnabled(View priv, View pub){
		priv.setSelected(true);
		pub.setSelected(false);
	}
    
    private void setWho(String who){
    	View pub = findViewById(R.id.events_new_radio_public);
    	View priv = findViewById(R.id.events_new_radio_private);
    	if (who == "public"){
	        findViewById(R.id.events_new_friends_selected_wrapper)
	        		.setVisibility(View.GONE);
	        setEnabled(pub, priv);
    	}else if (who == "private"){
	        findViewById(R.id.events_new_friends_selected_wrapper)
	        		.setVisibility(View.VISIBLE);
	        setEnabled(priv, pub);
//	        if (chosenUsers == null && chosenContacts == null)
//	        	selectFriends();
    	}

    	final ScrollView scroll = (ScrollView)findViewById(R.id.events_new_scroller);
    	scroll.post(new Runnable() { 
    	    public void run() { 
    	        scroll.fullScroll(ScrollView.FOCUS_DOWN); 
    	    } 
    	});
    }

    private void submitData() {
    	String what = ((TextView) findViewById(R.id.events_new_what))
				.getText().toString();
    	boolean broadcast = findViewById(R.id.events_new_radio_public).isSelected();
    	if (what.equals("")){
    		toast("What are you doing later?");
    		return;
    	}else if (!broadcast 
    			&& ((chosenUsers == null || chosenUsers.size() == 0) 
    			 && (chosenContacts == null || chosenContacts.size() == 0))){
    		toast("Please invite someone");
    		return;
    	}
    	
        eventManager = new EventManager(this, this, null, null);
        Event event = eventManager.new Event();
        event.creator = UserManager.currentUsername(this);
        event.broadcast = broadcast;
        event.what = what;
        
        String where = ((TextView) findViewById(R.id.events_new_where))
				.getText().toString();
        if (!where.equals(""))
        	event.where = where;

        if (timestamp != null)
        	event.when = timestamp;
        
        eventManager.createEvent(event, chosenUsers, chosenContacts, CREATE_EVENT);
        loadingDialog = ProgressDialog.show(this, "", "Creating event...", true);
    }
	
    public boolean onCreateOptionsMenu(Menu menu) {
        return MainMenu.onCreateOptionsMenu(menu);
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
        return MainMenu.onOptionsItemSelected(this, item);
    }

	public void onDataUpdate(int code, String response) {
		try {
			eventJSON = new JSONObject(response);
			loadingDialog.dismiss();
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setType("vnd.android.cursor.item/vnd.connectsy.event");
			i.putExtra("com.connectsy2.events.revision", 
					eventJSON.getString("revision"));
			startActivity(i);
			finish();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void onRemoteError(int httpStatus, String response, int code) {
		if (loadingDialog != null) loadingDialog.dismiss();
		try {
			JSONObject jsonResp = new JSONObject(response);
			if (jsonResp.getString("error").equals("OUT_OF_NUMBERS")){
				String message = "The following users are rock stars "
						+"(apparently) and have hit our SMS limit. \n\n";
				
				JSONArray contacts = jsonResp.getJSONArray("contacts");
				for (int i=0;i<contacts.length();i++)
					message += "- "+contacts.getJSONObject(i).getString("name")+"\n";
				
				message += "\nThey won't recieve your invite. Tell "
						+"them to get the app and friend you.";
				
				final String rev = jsonResp.getString("event_revision");
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(message)
				       .setCancelable(false)
				       .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
								Intent i = new Intent(Intent.ACTION_VIEW);
								i.setType("vnd.android.cursor.item/vnd.connectsy.event");
								i.putExtra("com.connectsy2.events.revision", rev);
								startActivity(i);
								finish();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}else if (jsonResp.getString("error").equals("MISSING_FIELDS")){
				toast("Please fill out "+jsonResp.getString("field_missing"));
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void toast(String message){
		Toast t = Toast.makeText(this, message, 5000);
		t.setGravity(Gravity.TOP, 0, 20);
		t.show();
	}
}
