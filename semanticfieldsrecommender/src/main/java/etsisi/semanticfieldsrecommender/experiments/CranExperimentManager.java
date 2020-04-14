package etsisi.semanticfieldsrecommender.experiments;

import etsisi.semanticfieldsrecommender.InferenceProcessor;
import etsisi.semanticfieldsrecommender.Item;
import etsisi.utilities.MongoConnection;
import etsisi.utilities.RecomManager;
import etsisi.utilities.TextPreprocessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CranExperimentManager {
	private RecomManager recomManager;
	private TextPreprocessor processor;
	private Map<Integer, List<Integer>> cranRelations;
	private List<CranDocument> cranDocuments;
	private List<CranQuery> cranQueries;
	private Integer keywordSize;
	private InferenceProcessor ip;
	
	public CranExperimentManager(
			MongoConnection mongo,TextPreprocessor processor, Map<Integer, List<Integer>> cranRelations,
			List<CranDocument> cranDocuments, List<CranQuery> cranQueries, Integer keywordSize,
			InferenceProcessor ip) {
		this.recomManager = new RecomManager(mongo);
		this.processor = processor;
		this.cranRelations = cranRelations;
		this.cranDocuments = cranDocuments;
		this.cranQueries = cranQueries;
		this.keywordSize = keywordSize;
		this.ip = ip;
	}
	
	private void insertDocuments() {
		List<String> documentTexts = new ArrayList<String>();
		for(CranDocument document : cranDocuments)
			documentTexts.add(document.getText());
		for(CranDocument document : cranDocuments) {
			List<String> tags = processor.extractKeywords(
					document.getText(), documentTexts, keywordSize);
			Item itemizedDocument = new Item(document.getIdentifier().toString(),
					tags, "#" + document.getIdentifier() + "#" + document.getText());
			recomManager.insertItem(itemizedDocument, ip);
		}
	}
	
	private List<Integer> getRecommendedItems(CranQuery query, boolean simpleComparator) {
		List<Integer> recommendedItems = new ArrayList<Integer>();
		List<String> queryTags = processor.extractKeywords(
				query.getText(), recomManager.getDatabaseTexts(), keywordSize);
		Item itemizedQuery = new Item(query.getIdentifier().toString(),
				queryTags, query.getText());
		List<Item> recommendedItemizedDocs = recomManager.predictRecommendations(
				itemizedQuery, ip, simpleComparator);
		for(Item itemizedDoc : recommendedItemizedDocs) {
			String text = itemizedDoc.getText();
			String[] splitText = text.split("#");
			Integer docId = Integer.parseInt(splitText[1]);
			recommendedItems.add(docId);
		}
		return recommendedItems;
	}
	
	private Float getPrecision(List<Integer> queryResults, List<Integer> relations) {
		Integer relevantsIncluded = 0;
		//Only a subset the size of the relations is extracted. Otherwise precision would be severely affected
		queryResults = queryResults.subList(0, relations.size());
		for(Integer queryResult : queryResults) {
			if(relations.contains(queryResult))
				relevantsIncluded++;
		}
		Float precision = relevantsIncluded/(float)queryResults.size();
		return precision;
	}
	
	private Float getRecall(List<Integer> queryResults, List<Integer> relations) {
		Integer relevantsIncluded = 0;
		//Only a subset the size of the relations is extracted. Otherwise recall would be severely affected
		queryResults = queryResults.subList(0, relations.size());
		for(Integer queryResult : queryResults) {
			if(relations.contains(queryResult))
				relevantsIncluded++;
		}
		Float recall = relevantsIncluded/(float)relations.size();
		return recall;
	}
	
	

}
