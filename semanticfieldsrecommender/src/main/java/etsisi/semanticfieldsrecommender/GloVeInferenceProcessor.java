package etsisi.semanticfieldsrecommender;

import java.io.IOException;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Iterator;

import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.math.DoubleVector;
import etsisi.utilities.RecomManager;

public class GloVeInferenceProcessor extends InferenceProcessor{
	
	private GloveRandomAccessReader db;
	
	public GloVeInferenceProcessor(String modelPath, RecomManager recomManager) throws IOException{
		super(recomManager);
		try {
			this.db = new GloveBinaryRandomAccessReader(Paths.get(modelPath));
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public ArrayList<String> inferTags(ArrayList<String> tags){
		ArrayList<String> inferredTags = new ArrayList<String>();
		ArrayList<String> databaseTags = this.recomManager.getDatabaseTags(); //TODO Retrieve tags from database
		Iterator<String> tagIt = tags.iterator();
		while(tagIt.hasNext()) {
			String currentTag = tagIt.next();
			String inferredTag = this.applyInference(currentTag, databaseTags);
			if(inferredTag != null && !inferredTags.contains(inferredTag)) {
				inferredTags.add(inferredTag);
				databaseTags.add(inferredTag);
			}
		}
		return inferredTags;
	}
	
	@Override
	public double compareTagSets(ArrayList<String> tagSet0, ArrayList<String> tagSet1) { //Method that gives a similarity value between 0 and 1, more is better
		ArrayList<Double> similarityVector = new ArrayList<Double>();
		CosineDistance cos = new CosineDistance();
		try {
			if(tagSet0 != null && tagSet1 != null) {
				for(String tag0 : tagSet0) {
					if(db.contains(tag0)) {
						DoubleVector tag0Vec = db.get(tag0);
						for(String tag1 : tagSet1) {
							if(db.contains(tag1)) {
								DoubleVector tag1Vec = db.get(tag1);
								Double similarity = cos.measureDistance(tag0Vec, tag1Vec);
								similarityVector.add(similarity);
							}
							else
								similarityVector.add(tag0.equals(tag1)? 0d : 1d);
						}
					}
					else {
						for(String tag1: tagSet1)
							similarityVector.add(tag0.equals(tag1)? 0d : 1d);
					}
				}
			}
		}catch(IOException ex) {
			ex.printStackTrace();
		}
		Double avg = 1d;
		for(Double similarity : similarityVector) 
			avg += similarity;
		if(similarityVector.size() > 0)
			avg = avg / similarityVector.size();
		return 1 - avg.doubleValue();
	}
	
	@Override
	public void setLanguage(String modelPath) {
		try {
			this.db = new GloveBinaryRandomAccessReader(Paths.get(modelPath));
		}catch(IOException ex) {
			ex.printStackTrace();
			System.out.println("Can't find model path provided");
		}
	}
	
	@Override
	protected String applyInference(String tag, ArrayList<String> databaseTags) {
		String tagNorm = tag.toLowerCase().trim();
		try {
			return this.findSemanticField(tagNorm, databaseTags);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Finds the closest semantic field, if any
	private String findSemanticField (String tagNorm, ArrayList<String> databaseTags) throws IOException {
		String inferredTag = tagNorm;
		if(db.contains(tagNorm)) {
			DoubleVector tagVector = db.get(tagNorm);
			Iterator<String> databaseTagIt = databaseTags.iterator();
			CosineDistance cos = new CosineDistance();
			Double smallestDistance = Double.MAX_VALUE; 
			while(databaseTagIt.hasNext() ) {
				String currentTag = databaseTagIt.next();
				String currentTagString = currentTag.toLowerCase().trim();
				if (db.contains(currentTagString)) {
					DoubleVector currentTagVector = db.get(currentTagString);
					double dist = cos.measureDistance(currentTagVector, tagVector);
					if( dist < 0.5d && dist < smallestDistance) {
						inferredTag = currentTagString;
						smallestDistance = dist;
					}
				}
			}
		}
		return inferredTag;
	}
	
}
