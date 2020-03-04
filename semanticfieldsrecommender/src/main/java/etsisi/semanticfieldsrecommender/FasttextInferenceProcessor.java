package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;
import java.io.File;
import java.util.Iterator;

import com.mayabot.blas.Vector;
import com.mayabot.mynlp.fasttext.FastText;

public class FasttextInferenceProcessor extends InferenceProcessor {
	
	public static final File ROMANIAN = new File("fasttext/cc.ro.300.bin");
	public static final File GERMAN = new File("fasttext/cc.de.300.bin");
	public static final File ENGLISH = new File("fasttext/cc.en.300.bin");
	public static final File SPANISH = new File("fasttext/cc.es.300.bin");
	
	private FastText fastText;
	
	public FasttextInferenceProcessor(File language) throws Exception{
		super();
		this.fastText = FastText.loadFasttextBinModel(language);
	}
	
	
	@Override
	public ArrayList<String> inferTags(ArrayList<Tag> tags){
		ArrayList<String> inferredInterests = new ArrayList<String>();
		ArrayList<Tag> databaseTags = new ArrayList<Tag>(); //TODO Retrieve tags from database
		Iterator<Tag> tagIt = tags.iterator();
		while(tagIt.hasNext()) {
			Tag currentTag = tagIt.next();
			String inferredInterest = this.applyInference(currentTag, databaseTags);
			if(inferredInterest != null && !inferredInterests.contains(inferredInterest)) {
				inferredInterests.add(inferredInterest);
				databaseTags.add(new Tag(inferredInterest));
			}
		}
		return inferredInterests;
	}
	
	//TODO Replace with a simple comparison of the words
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
				case InferenceProcessor.ROMANIAN:
					this.fastText = FastText.loadFasttextBinModel(FasttextInferenceProcessor.ROMANIAN);
					break;
				case InferenceProcessor.GERMAN:
					this.fastText = FastText.loadFasttextBinModel(FasttextInferenceProcessor.GERMAN);
					break;
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
	
	private String applyInference(Tag interest, ArrayList<Tag> ontologyTags) {
		String interestNorm = interest.getName().toLowerCase().trim();
		String inferredInterest = interestNorm;
		Vector interestVector = this.fastText.getWordVector(interestNorm);
		Iterator<Tag> ontologyTagIt = ontologyTags.iterator();
		boolean foundSemanticField = false;
		while(ontologyTagIt.hasNext() && !foundSemanticField) {
			Tag currentTag = ontologyTagIt.next();
			String currentTagString = currentTag.getName().toLowerCase().trim();
			Vector currentTagVector = this.fastText.getWordVector(currentTagString);
			double dist = (double) Vector.Companion.cosine(currentTagVector, interestVector);
			if(dist < 0.5D) {
				inferredInterest = currentTagString;
				foundSemanticField = true;
			}
		}
		return inferredInterest;
	}
	
}
