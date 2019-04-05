package com.learn.bitcoin.trust;

public class Candidate {
	Transaction tx;
	int sender;
	
	public Candidate(Transaction tx, int sender) {
		this.tx = tx;
		this.sender = sender;
	}
}