package com.learn.bitcoin.trust;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private double p_graph;
    private double p_malicious;
    private double p_txDistribution;
    private double numRounds;
    private boolean []followees;
    private Set<Transaction> pendingTransactionsSet;
    private Set<Candidate> candidateSet;


    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        this.p_graph = p_graph;
        this.p_malicious = p_malicious;
        this.p_txDistribution = p_txDistribution;
        this.numRounds = numRounds;
        this.candidateSet = new HashSet<Candidate>();
        this.pendingTransactionsSet = new HashSet<Transaction>();
    }

    public void setFollowees(boolean[] followees) {
        this.followees = followees;
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        // IMPLEMENT THIS
        this.pendingTransactionsSet.addAll(pendingTransactions);
    }

    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return pendingTransactionsSet;

    }

    public void receiveFromFollowees(Set<Candidate> candidates) {

//        candidateSet.addAll(candidates);
        for(Candidate candidate : candidates){
            pendingTransactionsSet.add(candidate.tx);
        }
    }
}
