package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;
import java.util.Objects;

public class Item {
	private String name;
	private ArrayList<String> tags;
	private String text;
	
	public Item(String name, ArrayList<String> tags) {
		this.name = name;
		this.tags = tags;
	}
	
	public Item(String name, ArrayList<String> tags, String text) {
		this.name = name;
		this.tags = tags;
		this.text = text;
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
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public boolean hasText() {
		return this.text != null;
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