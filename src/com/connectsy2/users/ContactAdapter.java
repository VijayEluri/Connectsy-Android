package com.connectsy2.users;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.connectsy2.R;
import com.connectsy2.users.ContactCursor.Contact;

public class ContactAdapter extends BaseAdapter {

	@SuppressWarnings("unused")
	private final static String TAG = "ContactAdapter";
	private ContactCursor contactsCursor;
	private Activity activity;
	private HashMap<String, Pair<Boolean, Contact>> contactsSelected = 
		new HashMap<String, Pair<Boolean, Contact>>();

	public ContactAdapter(Activity activity){
		this.activity = activity;
		this.contactsCursor = new ContactCursor(activity);
	}

	public void setSelectedContacts(ArrayList<Contact> contacts) {
		for (Contact c: contacts)
			contactsSelected.put(c.number, new Pair<Boolean, Contact>(true, c));
	}

	public ArrayList<Contact> getSelectedContacts() {
		ArrayList<Contact> selContacts = new ArrayList<Contact>();
		for (Pair<Boolean, Contact> pair: contactsSelected.values())
			if (pair.first) selContacts.add(pair.second);
		return selContacts;
	}
	
	public int getCount() {
		return contactsCursor.getCount();
	}

	public Contact getItem(int position) {
		return contactsCursor.getAt(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final Contact contact = getItem(position);
		View view;
		if (convertView != null){
			view = convertView;
		}else{
			view = LayoutInflater.from(activity).inflate(
					R.layout.user_list_item, parent, false);
		}
		
		CheckBox sel = (CheckBox)view.findViewById(R.id.user_list_item_select);
		sel.setVisibility(CheckBox.VISIBLE);
		sel.setOnCheckedChangeListener(new OnCheckedChangeListener(){
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
		    	contactsSelected.put(contact.number, 
		    			new Pair<Boolean, Contact>(isChecked, contact));
		    }
		});
		if (contactsSelected.containsKey(contact.number))
			sel.setChecked(contactsSelected.get(contact.number).first);
		else
			sel.setChecked(false);
		
        TextView name = (TextView)view.findViewById(R.id.user_list_item_username);
        name.setText(contact.displayName);
        name.setTextColor(activity.getResources().getColor(R.color.text_grey));
        
        if (contact.starred)
        	view.findViewById(R.id.user_list_item_star)
        			.setVisibility(View.VISIBLE);
        else
        	view.findViewById(R.id.user_list_item_star)
					.setVisibility(View.GONE);
    	
        TextView number = (TextView)view.findViewById(R.id.user_list_item_detail);
        number.setText(contact.displayType +": "+ contact.displayNumber);
        number.setVisibility(TextView.VISIBLE);
        
        ImageView avatar = (ImageView)view.findViewById(R.id.user_list_item_avatar);
        ContentResolver cr = activity.getContentResolver();
        Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, 
        		contact.lookupKey);
        Uri uri = Contacts.lookupContact(cr, lookupUri);
        InputStream input = null;
        if (uri != null)
        	input= Contacts.openContactPhotoInputStream(cr, uri);
        if (input != null) 
             avatar.setImageBitmap(BitmapFactory.decodeStream(input));
        else
        	avatar.setImageResource(R.drawable.avatar_default);

        return view;
	}

}
