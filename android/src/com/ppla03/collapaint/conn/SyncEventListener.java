package com.ppla03.collapaint.conn;

import java.util.ArrayList;

import com.ppla03.collapaint.model.action.UserAction;

public interface SyncEventListener {
	void onActionUpdated(int lastActionNumber,
			ArrayList<UserAction> replyActions);
	
	
}
