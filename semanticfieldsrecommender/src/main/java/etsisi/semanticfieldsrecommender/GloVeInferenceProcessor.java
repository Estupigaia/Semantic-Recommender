package etsisi.semanticfieldsrecommender;

import java.io.IOException;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.jungblut.distance.CosineDistance;
import de.jungblut.glove.GloveRandomAccessReader;
import de.jungblut.glove.impl.GloveBinaryRandomAccessReader;
import de.jungblut.math.DoubleVector;

public class GloVeInferenceProcessor extends InferenceProcessor{
	
	private GloveRandomAccessReader db;
	
	public GloVeInferenceProcessor(String modelPath) throws IOException{
		try {
			this.db = new GloveBinaryRandomAccessReader(Paths.get(modelPath));
		}catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public List<String> inferTags(List<String> tags, List<String> databaseTags){
		List<String> inferredTags = new ArrayList<String>();
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
	
	//Method that gives a similarity value between 0 and 1, more is better
	@Override
	public double compareTagSets(List<String> tagSet0, List<String> tagSet1) { 
		List<Double> similarityVector = new ArrayList<Double>();
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
		return  avg.doubleValue();
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
	protected String applyInference(String tag, List<String> databaseTags) {
		String tagNorm = tag.toLowerCase().trim();
		try {
			return this.findSemanticField(tagNorm, databaseTags);
		}catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//Finds the closest semantic field, if any
	private String findSemanticField (String tagNorm, List<String> databaseTags) throws IOException {
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
