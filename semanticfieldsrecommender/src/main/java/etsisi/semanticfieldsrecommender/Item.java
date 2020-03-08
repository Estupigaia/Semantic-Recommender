package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;
import java.util.Objects;

public class Item {
	private String name;
	private ArrayList<String> tags;
	
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
	
	public ArrayList<String> getTags() {
		return tags;
	}
	
	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
	
	
	@Override
	public boolean equals(Object o) {
		if( o == this)
			return true;
		if( !(o instanceof Item))
			return false;
		Item item = (Item) o;
		return this.getName().equals(item.getName());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getName());
	}
}