package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;
import java.util.Objects;

public class Item {
	private String name;
	private float rating;
	private ArrayList<String> tags;
	private String language;
	
	private double orderScore; //Score from comparison, only used for the comparator
	
	private InferenceProcessor ip;
	
	public Item(String name, String language, ArrayList<String> tags, InferenceProcessor ip) {
		try {
			this.name = name;
			this.ip = ip;
			this.setTags(tags);
		}catch(Exception ex) {
			ex.printStackTrace();
			System.out.println("InferenceProcessor failed");
		}
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
	
	@Override
	public boolean equals(Object o) {
		if( o == this)
			return true;
		if( !(o instanceof Item))
			return false;
		Item document = (Item) o;
		return this.getName().equals(document.getName());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName());
	}
}