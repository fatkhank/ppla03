package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import com.ppla03.collapaint.model.CanvasModel;
import com.ppla03.collapaint.model.UserModel;

public interface ManageParticipantListener {
	void onParticipantFetched(CanvasModel canvas, UserModel owner,
			ArrayList<UserModel> participants);
	
	void onParticipationFetchedFailed(CanvasModel model, int status);
}
