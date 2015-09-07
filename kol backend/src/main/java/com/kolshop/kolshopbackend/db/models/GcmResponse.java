package com.kolshop.kolshopbackend.db.models;

import java.util.ArrayList;
import java.util.Map;

public class GcmResponse{

    private String multicast_id;
    private String success;
    private String failure;
    private String canonical_ids;
    private ArrayList<Map<String, String>> results;
    
    public String getMulticast_id() {
		return multicast_id;
	}
	public String getSuccess() {
		return success;
	}
	public String getFailure() {
		return failure;
	}
	public String getCanonical_ids() {
		return canonical_ids;
	}
	public ArrayList<Map<String, String>> getResults() {
		return results;
	}
	public void setMulticast_id(String multicast_id) {
		this.multicast_id = multicast_id;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public void setFailure(String failure) {
		this.failure = failure;
	}
	public void setCanonical_ids(String canonical_ids) {
		this.canonical_ids = canonical_ids;
	}
	public void setResults(ArrayList<Map<String, String>> results) {
		this.results = results;
	}

}
