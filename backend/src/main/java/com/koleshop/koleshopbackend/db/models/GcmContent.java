package com.koleshop.koleshopbackend.db.models;

import java.util.List;


import com.google.gson.JsonObject;

public class GcmContent {
	
	List<String> registration_ids;
	String collapse_key;
	JsonObject data;
	String delay_while_idle;
	String time_to_live;
	
	public List<String> getRegistration_ids() {
		return registration_ids;
	}
	public String getCollapse_key() {
		return collapse_key;
	}
	public JsonObject getData() {
		return data;
	}
	public String getDelay_while_idle() {
		return delay_while_idle;
	}
	public String getTime_to_live() {
		return time_to_live;
	}
	public void setRegistration_ids(List<String> registration_ids) {
		this.registration_ids = registration_ids;
	}
	public void setCollapse_key(String collapse_key) {
		this.collapse_key = collapse_key;
	}
	public void setData(JsonObject data) {
		this.data = data;
	}
	public void setDelay_while_idle(String delay_while_idle) {
		this.delay_while_idle = delay_while_idle;
	}
	public void setTime_to_live(String time_to_live) {
		this.time_to_live = time_to_live;
	}
	
	public GcmContent data(JsonObject data)
	{
		this.data = data;
		return this;
	}
	
	public GcmContent collapse_key(String collapse_key)
	{
		this.collapse_key = collapse_key;
		return this;
	}
	
	public GcmContent registration_ids(List<String> registration_ids)
	{
		this.registration_ids = registration_ids;
		return this;
	}
	
	public GcmContent delay_while_idle(String delay_while_idle)
	{
		this.delay_while_idle = delay_while_idle;
		return this;
	}
	
	public GcmContent time_to_live(String time_to_live)
	{
		this.time_to_live = time_to_live;
		return this;
	}
	
    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("registration_ids", registration_ids.toString());
        jsonObject.addProperty("data", data.toString());
        if(collapse_key!=null && !collapse_key.isEmpty())
        {
        	jsonObject.addProperty("collapse_key", collapse_key);
        }
        if(delay_while_idle!=null && !delay_while_idle.isEmpty())
        {
        	jsonObject.addProperty("delay_while_idle", delay_while_idle);
        }
        if(time_to_live!=null && !time_to_live.isEmpty())
        {
        	jsonObject.addProperty("time_to_live", time_to_live);
        }
        return jsonObject.toString();
    }

}
