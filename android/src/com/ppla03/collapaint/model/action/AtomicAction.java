package com.ppla03.collapaint.model.action;

import java.util.ArrayList;

/**
 * Aksi yang dapat dikirim ke atau diterjemahkan dari server.
 * @author hamba v7
 * 
 */
public abstract class AtomicAction extends UserAction {

	@Override
	public int insertInAtomic(ArrayList<AtomicAction> list) {
		list.add(this);
		return 1;
	}
}
