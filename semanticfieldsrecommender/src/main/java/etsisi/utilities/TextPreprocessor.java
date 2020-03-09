package etsisi.utilities;

import java.util.Map;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Stream;

public class TextPreprocessor {
	public static final String SPANISH = "spanish";
	public static final String ENGLISH = "english";
	
	private String language;
	private Properties config;
	
	public TextPreprocessor(String language, Properties config) {
		this.language = language;
		this.config = config;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	public Properties getConfig() {
		return config;
	}
	
	public void setConfig(Properties config) {
		this.config = config;
	}
	
	public ArrayList<String> preprocessedText(String text){
		String[] wordArray = text.replaceAll("\\p{P}", "").toLowerCase().split("\\s+"); //Deletes punctuation and splits
		ArrayList<String> wordList = new ArrayList<String>(Arrays.asList(wordArray));
		wordList = this.removeStopWords(wordList);
		return wordList;
	}
	
	private ArrayList<String> removeStopWords(ArrayList<String> wordList) {
		try (Stream<String> stream = Files.lines(Paths.get(
				this.config.getProperty(this.language + "stopwords", "")))) {
	        Iterator<String> stopwordIt = stream.iterator();
	        while(stopwordIt.hasNext())
	        	wordList.removeIf(stopwordIt.next()::equals);
	        return wordList;
		}catch(IOException ex) {
			ex.printStackTrace();
			System.out.println("Couldn't remove stopwords");
			return wordList;
		}
	}
	
	public ArrayList<String> extractKeywords(ArrayList<String> wordList, ArrayList<String> databaseTexts,
			int keywordMaxSize){
		LinkedHashMap<String, Double> termsWithScores = new LinkedHashMap<String, Double>();
		for(String term : wordList)
			termsWithScores.put(term, this.tfIdf(wordList, databaseTexts, term));
		ArrayList<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(termsWithScores.entrySet());
		Collections.sort(sortedEntries, 
				(entry0, entry1) -> Double.compare(entry0.getValue(), entry1.getValue()));
		Collections.reverse(sortedEntries);
		ArrayList<String> keywords = new ArrayList<String>();
		Iterator<Map.Entry<String, Double>> entriesIt = sortedEntries.iterator();
		int keywordCount = 0;
		while(entriesIt.hasNext() && keywordCount < keywordMaxSize) {
			keywords.add(entriesIt.next().getKey());
			keywordCount++;
		}
		return keywords;
	}
	
	public Double tfIdf(ArrayList<String> documentWordList, ArrayList<String> databaseTexts, String term) {
		ArrayList<ArrayList<String>> preprocessedTexts = new ArrayList<ArrayList<String>>();
		for(String databaseText : databaseTexts)
			preprocessedTexts.add(new ArrayList<String>(this.preprocessedText(databaseText)));
		Double tf = this.termFrequency(documentWordList, term);
		Double idf = this.inverseDFrequency(preprocessedTexts, term);
		return tf*idf;
	}
	
	private Double termFrequency(ArrayList<String> wordList, String term) {
		Double termCount = 0d;
		for(String word : wordList)
			if(word.equals(term))
				termCount++;
		return termCount/wordList.size();
	}
	
	private Double inverseDFrequency(ArrayList<ArrayList<String>> itemTexts, String term) {
		Double termCount = 0d;
		for(ArrayList<String> wordList : itemTexts)
			for(String word : wordList)
				if(word.equals(term)) {
					termCount++;
					break;
				}
		//Avoids the unlikely case in which the checked term doesn't exist
		if(termCount == 0d)
			return 0d;
		return Math.log10(itemTexts.size()/termCount);
	}
}
