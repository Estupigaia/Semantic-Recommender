package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;
import java.util.Objects;

public class Item {
	private String name;
	private float rating;
	private ArrayList<String> tags;
	
	private double orderScore; //Score from comparison, only used for the comparator
	
	public Item(String name, ArrayList<String> tags) {
		this.name = name;
		this.setTags(tags);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
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
		this.tags = tags;
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