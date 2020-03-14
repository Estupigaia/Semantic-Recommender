package etsisi.utilities;

import java.util.Map;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;
import java.util.stream.Stream;
import eus.ixa.ixa.pipe.lemma.StatisticalLemmatizer;
import eus.ixa.ixa.pipe.pos.StatisticalTagger;

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
	
	public List<String> preprocessText(String text){
		String[] tokens = text.replaceAll("\\p{P}", "").toLowerCase().split("\\s+"); //Deletes punctuation and splits
		List<String> wordList = this.lemmatizeText(tokens);
		wordList = this.removeStopWords(wordList);
		return wordList;
	}
	
	private List<String> removeStopWords(List<String> wordList) {
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
	
	public List<String> extractKeywords(String text, List<String> databaseTexts,
			int keywordMaxSize){
		List<String> wordList = this.preprocessText(text);
		LinkedHashMap<String, Double> termsWithScores = new LinkedHashMap<String, Double>();
		for(String term : wordList)
			termsWithScores.put(term, this.tfIdf(wordList, databaseTexts, term));
		List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(termsWithScores.entrySet());
		Collections.sort(sortedEntries, 
				(entry0, entry1) -> Double.compare(entry0.getValue(), entry1.getValue()));
		Collections.reverse(sortedEntries);
		List<String> keywords = new ArrayList<String>();
		Iterator<Map.Entry<String, Double>> entriesIt = sortedEntries.iterator();
		int keywordCount = 0;
		while(entriesIt.hasNext() && keywordCount < keywordMaxSize) {
			keywords.add(entriesIt.next().getKey());
			keywordCount++;
		}
		return keywords;
	}
	
	public Double tfIdf(List<String> documentWordList, List<String> databaseTexts, String term) {
		List<ArrayList<String>> preprocessedTexts = new ArrayList<ArrayList<String>>();
		for(String databaseText : databaseTexts)
			preprocessedTexts.add(new ArrayList<String>(this.preprocessText(databaseText)));
		Double tf = this.termFrequency(documentWordList, term);
		Double idf = this.inverseDFrequency(preprocessedTexts, term);
		return tf*idf;
	}
	
	private Double termFrequency(List<String> wordList, String term) {
		Double termCount = 0d;
		for(String word : wordList)
			if(word.equals(term))
				termCount++;
		return termCount/wordList.size();
	}
	
	private Double inverseDFrequency(List<ArrayList<String>> itemTexts, String term) {
		Double termCount = 0d;
		for(List<String> wordList : itemTexts)
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
	
	public List<String> lemmatizeText(String[] tokens){
		List<String> lemmatizedText = new ArrayList<String>();
		String language = this.language == "spanish" ? "es" : "en";
		Properties lemmaProps = new Properties();
		lemmaProps.setProperty("language", language);
		lemmaProps.setProperty("lemmatizerModel", this.getConfig().getProperty(this.language + "lemma"));
		lemmaProps.setProperty("useModelCache", "true");
		StatisticalLemmatizer lemmatizer = new StatisticalLemmatizer(lemmaProps);
		lemmatizedText = new ArrayList<String>(lemmatizer.lemmatize(tokens, tokens));
		return lemmatizedText;
	}
	
	public List<String> tagText(String[] tokens) {
		String language = this.language == "spanish" ? "es" : "en";
		Properties taggerProps = new Properties();
		taggerProps.setProperty("language", language);
		taggerProps.setProperty("model", this.getConfig().getProperty(this.language + "pos"));
		taggerProps.setProperty("useModelCache", "true");
		StatisticalTagger tagger = new StatisticalTagger(taggerProps);
		return  tagger.posAnnotate(tokens);
	}
}
