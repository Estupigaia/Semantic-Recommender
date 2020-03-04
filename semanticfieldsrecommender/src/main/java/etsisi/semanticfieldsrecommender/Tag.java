package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;
import java.util.Iterator;

public class Tag {
	private String name;
	
	public Tag(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static ArrayList<Tag> stringsToTags(ArrayList<String> stringTags){
		ArrayList<Tag> tags = new ArrayList<Tag>();
		Iterator<String> tagIt = stringTags.iterator();
		while(tagIt.hasNext())
			tags.add(new Tag(tagIt.next()));
		return tags;
	}
}