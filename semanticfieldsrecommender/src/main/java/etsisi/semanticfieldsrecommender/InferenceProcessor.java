package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;

public abstract class InferenceProcessor {
	protected final static String SPANISH = "spanish";
	protected final static String ENGLISH = "english";
	
	public abstract ArrayList<String> inferTags(ArrayList<String> tags, ArrayList<String> databaseTags);
	//For item ranking
	public abstract double compareTagSets(ArrayList<String> tagSet0, ArrayList<String> tagSet1);
	public abstract void setLanguage(String language);
	protected abstract String applyInference(String tag, ArrayList<String> databaseTags);
	
	public double altCompareTagSets(ArrayList<String> tagSet0, ArrayList<String> tagSet1) {
		double simCount = 0;
		for(String tag : tagSet0) {
			if(tagSet1.contains(tag))
				simCount++;
		}
		for(String tag : tagSet1) {
			if(tagSet0.contains(tag))
				simCount++;
		}
		if(tagSet0.size() != 0 || tagSet1.size() != 0) {
			System.out.println("What");
			return simCount/(tagSet0.size() + tagSet1.size());
		}
		return simCount;
	}
}
