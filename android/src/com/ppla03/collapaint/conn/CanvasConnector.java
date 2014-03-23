package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import android.graphics.Bitmap;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;
import com.ppla03.collapaint.model.action.UserAction;
import com.ppla03.collapaint.model.object.ImageObject;

public class CanvasConnector extends ServerConnector {
	private ArrayList<UserAction> actionBuffer;
	
	public void getParticipants(CanvasModel canvas){
		//TODO
	}
	
	public void inviteUser(String username){
		//TODO
	}
	
	public void kickUser(UserModel user){
		//TODO
	}
	
	public void closeCanvas(){
		//TODO
	}
	
	public void updateActions(ArrayList<UserAction> actions){
		//TODO
	}
	
	public void downloadImage(ImageObject image){
		//TODO
	}
	
	public void uploadImage(Bitmap bitmap, String id){
		//TODO
	}
}
