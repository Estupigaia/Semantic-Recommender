package etsisi.semanticfieldsrecommender;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import org.json.JSONObject;

import etsisi.utilities.MongoConnection;

public class Item {
	private String _id;
	private float rating;
	private ArrayList<String> tags;
	private String language;
	
	private double orderScore; //Score from comparison, only used for the comparator
	
	private MongoConnection mongo;
	private InferenceProcessor ip;
	
	public Item(MongoConnection mongo, File language, ArrayList<String> tags) {
		try {
			this.mongo = mongo;
			this.ip = new FasttextInferenceProcessor(language, mongo);
			this.setTags(tags);
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("Fasttext model failed");
		}
	}
	
	public String get_id() {
		return _id;
	}
	
	public void set_id(String _id) {
		this._id = _id;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
		this.ip.setLanguage(language);
	}

	public float getRating() {
		return rating;
	}
	
	public void setRating(float rating) {
		this.rating = rating;
	}
	
	public ArrayList<String> getTags() {
		return tags;
	}
	
	public void setTags(ArrayList<String> tags) {
		this.tags = this.ip.inferTags(tags);
	}
	
	public double getOrderScore() {
		return this.orderScore;
	}
	
	public void setOrderScore(double orderScore) {
		this.orderScore = orderScore;
	}
	
	//TODO Parse item into Json
	public void insertItem() {
		this.mongo.insertDocument("Items", new JSONObject());
	}
	
	@Override
	public boolean equals(Object o) {
		if( o == this)
			return true;
		if( !(o instanceof Item))
			return false;
		Item document = (Item) o;
		return this.get_id().equals(document.get_id());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.get_id());
	}
}