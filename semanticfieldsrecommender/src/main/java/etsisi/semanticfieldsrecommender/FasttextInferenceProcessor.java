package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;
import java.io.File;
import java.util.Iterator;
import etsisi.utilities.MongoConnection;

import com.mayabot.blas.Vector;
import com.mayabot.mynlp.fasttext.FastText;

public class FasttextInferenceProcessor extends InferenceProcessor {
	
	public static final File ENGLISH = new File("fasttext/cc.en.300.bin");
	public static final File SPANISH = new File("fasttext/cc.es.300.bin");
	
	private FastText fastText;
	
	public FasttextInferenceProcessor(File language, MongoConnection mongo) throws Exception{
		super(mongo);
		this.fastText = FastText.loadFasttextBinModel(language);
	}
	
	
	@Override
	public ArrayList<String> inferTags(ArrayList<String> tags){
		ArrayList<String> inferredTags = new ArrayList<String>();
		ArrayList<String> databaseTags = new ArrayList<String>(); //TODO Retrieve tags from database
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
	public double compareTagSets(ArrayList<String> tagSet0, ArrayList<String> tagSet1) {
		ArrayList<Double> similarityVector = new ArrayList<Double>();
		for(String tag0 : tagSet0) {
			Vector tag0Vec = this.fastText.getWordVector(tag0);
			for(String tag1 : tagSet1) {
				Vector tag1Vec = this.fastText.getWordVector(tag1);
				Double similarity = (double) Vector.Companion.cosine(tag0Vec, tag1Vec);
				similarityVector.add(similarity);
			}
		}
		Double avg = 1d;
		for(Double similarity : similarityVector) 
			avg += similarity;
		if(similarityVector.size() > 0)
			avg = avg / similarityVector.size();
		return 1 - avg.doubleValue();
	}
	
	@Override
	public void setLanguage(String language) {
		language = language.toLowerCase().trim();
		try {
			switch(language) {
				case InferenceProcessor.SPANISH:
					this.fastText = FastText.loadFasttextBinModel(FasttextInferenceProcessor.SPANISH);
					break;
				default:
					this.fastText = FastText.loadFasttextBinModel(FasttextInferenceProcessor.ENGLISH); // Defaults to English
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private String applyInference(String tag, ArrayList<String> databaseTags) {
		String tagNorm = tag.toLowerCase().trim();
		String inferredTag = tagNorm;;
		Vector interestVector = this.fastText.getWordVector(tagNorm);
		Iterator<String> databaseTagIt = databaseTags.iterator();
		boolean foundSemanticField = false;
		while(databaseTagIt.hasNext() && !foundSemanticField) {
			String currentTag = databaseTagIt.next();
			String currentTagString = currentTag.toLowerCase().trim();
			Vector currentTagVector = this.fastText.getWordVector(currentTagString);
			double dist = (double) Vector.Companion.cosine(currentTagVector, interestVector);
			if(dist < 0.5D) {
				inferredTag = currentTagString;
				foundSemanticField = true;
			}
		}
		return inferredTag;
	}
	
}
