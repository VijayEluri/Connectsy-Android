package com.connectsy.categories;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.connectsy.R;
import com.connectsy.categories.CategoryManager.Category;

public class CategoryAdapter extends ArrayAdapter<Category> {
	private final String TAG = "CategoryAdapter";
	
	public CategoryAdapter(Context context, int textViewResourceId,
			ArrayList<Category> categories) {
		super(context, textViewResourceId, categories);
	}
 
	@Override
	public View getView (int position, View convertView, ViewGroup parent) {
		final Context context = getContext();
		final Category cat = getItem(position);
		Log.d(TAG, "inflating row");
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.category_list_item, parent, false);
		TextView name = (TextView)view.findViewById(R.id.category_list_item_name);
        name.setText(cat.name);
		return view;
	}

}