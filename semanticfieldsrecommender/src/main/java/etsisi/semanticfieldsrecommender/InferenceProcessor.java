package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;

public abstract class InferenceProcessor {
	protected final static String SPANISH = "spanish";
	protected final static String ENGLISH = "english";
	
	protected RecomManager recomManager;
	
	public InferenceProcessor(RecomManager recomManager) {
		this.recomManager = recomManager;
	}
	
	public abstract ArrayList<String> inferTags(ArrayList<String> tags);
	//For item ranking
	public abstract double compareTagSets(ArrayList<String> tagSet0, ArrayList<String> tagSet1);
	public abstract void setLanguage(String language);
	protected abstract String applyInference(String tag, ArrayList<String> databaseTags);
}
