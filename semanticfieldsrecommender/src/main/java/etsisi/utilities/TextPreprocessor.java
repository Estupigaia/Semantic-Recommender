package etsisi.utilities;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
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
	
	public List<String> preprocessedText(String text){
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
	
	//TODO perform TF-IDF and return keywords with weighting
	private Map<String, Double> tfIdf(ArrayList<String> wordList, ArrayList<String> databaseTexts){
		return new LinkedHashMap<String, Double>();
	}
}
