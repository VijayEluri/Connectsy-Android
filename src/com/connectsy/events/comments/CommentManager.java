package com.connectsy.events.comments;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.connectsy.data.ApiRequest;
import com.connectsy.data.ApiRequest.Method;
import com.connectsy.data.DataManager;
import com.connectsy.events.EventManager;

public class CommentManager extends DataManager {
	@SuppressWarnings("unused")
	private final static String TAG = "CommentManager";
	
	/**
	 * Holds comment data
	 */
	public static class Comment {
		String id;
		String event;
		String username;
		String nonce;
		String comment;
		int timestamp;
		
		public Comment(String json) throws JSONException {
			//Log.d(TAG, "event json: "+json);
			JSONObject obj = new JSONObject(json);
			//id = obj.getString("id");
			//event = obj.getString("event");
			username = obj.getString("user");
			nonce = obj.getString("nonce");
			timestamp = (int)obj.getLong("timestamp");
			comment = obj.getString("comment");
		}
		
		public String getId() {
			return id;
		}
		public String getEvent() {
			return event;
		}
		public String getUsername() {
			return username;
		}
		public String getNonce() {
			return nonce;
		}
		public String getComment() {
			return comment;
		}
		public int getTimestamp() {
			return timestamp;
		}
		
		public static ArrayList<Comment> deserializeList(String json) throws JSONException{
			ArrayList<Comment> comments = new ArrayList<Comment>();
			JSONArray jsonComments = new JSONArray(json);
			for (int i=0; i<jsonComments.length(); i++) {
				Comment c = new Comment(jsonComments.getString(i));
				comments.add(c);
			}
			return comments;
		}
	}
	//////////////////////////////////////////////
	
	//ApiRequest identifiers
	final static int GET_COMMENTS = 0;
	private static final int NEW_COMMENT = 1;
	
	EventManager.Event event;
	int lastTimestamp = 0;
	
	public CommentManager(Context c, DataUpdateListener l, EventManager.Event event) {
		super(c, l);
		this.event = event;
	}
	
	/**
	 * Fetches new comments in the background
	 */
	public void refreshComments(int returnCode) {
		this.returnCode = returnCode;
		String path = String.format("/events/%s/comments/", event.ID);
		ApiRequest request = new ApiRequest(this, this.context, 
				ApiRequest.Method.GET, path, true, 0);
		request.execute();
	}
	
	/**
	 * Fetches all cached comments
	 * @return array of comments
	 */
	public ArrayList<Comment> getComments() {
		ApiRequest request = new ApiRequest(this, context, Method.GET,
				String.format("/events/%s/comments/", event.ID), true, 0);
		try {
			String jsonComments = request.getCached();
			if (jsonComments != null){
				String comments = new JSONObject(jsonComments).getString("comments");
				return Comment.deserializeList(comments);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new ArrayList<Comment>();
	}

	public void createComment(String comment, int returnCode) {
		this.returnCode = returnCode;
		ApiRequest request = new ApiRequest(this, context, Method.POST, 
				String.format("/events/%s/comments/", event.ID), true, NEW_COMMENT);
		JSONObject body = new JSONObject();
		try {
			body.put("comment", comment);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		request.setBodyString(body.toString());
		request.execute();
	}
	
	@Override
	public void onApiRequestFinish(int status, String response, int code) {
		listener.onDataUpdate(returnCode, response);
	}
}
