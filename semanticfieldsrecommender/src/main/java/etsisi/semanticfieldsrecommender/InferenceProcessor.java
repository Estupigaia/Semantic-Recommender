package etsisi.semanticfieldsrecommender;

import etsisi.utilities.MongoConnection;
import java.util.ArrayList;

public abstract class InferenceProcessor {
	protected final static String SPANISH = "spanish";
	protected final static String ENGLISH = "english";
	
	protected MongoConnection mongo;
	
	public InferenceProcessor(MongoConnection mongo) {
		this.mongo = mongo;
	}
	
	public abstract ArrayList<String> inferTags(ArrayList<String> tags);
	//For item ranking
	public abstract double compareTagSets(ArrayList<String> tagSet0, ArrayList<String> tagSet1);
	public abstract void setLanguage(String language);
}