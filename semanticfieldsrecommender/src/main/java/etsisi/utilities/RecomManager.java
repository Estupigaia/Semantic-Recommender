package etsisi.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;

import etsisi.semanticfieldsrecommender.InferenceProcessor;
import etsisi.semanticfieldsrecommender.Item;

import static com.mongodb.client.model.Filters.*;

public class RecomManager {

	private MongoConnection mongo;
	
	public RecomManager(MongoConnection mongo) {
		this.mongo = mongo;
	}

	public MongoConnection getMongo() {
		return mongo;
	}

	public void setMongo(MongoConnection mongo) {
		this.mongo = mongo;
	}

	public List<String> getDatabaseTags() {
		List<String> databaseTags = new ArrayList<String>();
		List<JSONObject> tagObjects = mongo.getCollection("tags");
		for(JSONObject tagJson : tagObjects)
			databaseTags.add(tagJson.getString("name"));
		return databaseTags;
	}
	
	public List<Item> getDatabaseItems(InferenceProcessor ip){
		List<Item> databaseItems = new ArrayList<Item>();
		List<JSONObject> databaseJsons = mongo.getCollection("items");
		for(JSONObject itemJson : databaseJsons) {
			Item item = this.parseJsonItem(itemJson);
			item.setTags(ip.inferTags(item.getTags(), this.getDatabaseTags()));
		}
		return databaseItems;
	}
	
	public List<String> getDatabaseTexts(){
		List<String> databaseTexts = new ArrayList<String>();
		List<JSONObject> databaseJsons = mongo.getCollection("items");
		for(JSONObject itemJson : databaseJsons) {
			String text = itemJson.optString("text", "");
			if(!text.equals(""))
				databaseTexts.add(text);
		}
		return databaseTexts;
	}
	
	public void insertTag(String tag) {
		JSONObject parsedTag = new JSONObject();
		parsedTag.put("name", tag);
		parsedTag = this.setObjectId(parsedTag);
		if(!mongo.checkExistenceFilter("tags", eq("name", tag))) { //Avoid duplicates
			mongo.insertDocument("tags", parsedTag);
		}
	}
	
	public void insertItem(Item item, InferenceProcessor ip) {
		item.setTags(ip.inferTags(item.getTags(), this.getDatabaseTags()));
		JSONObject parsedItem = new JSONObject();
		parsedItem.put("name", item.getName());
		parsedItem.put("tags", item.getTags());
		parsedItem.putOpt("text", item.getText());
		parsedItem = this.setObjectId(parsedItem);
		mongo.insertDocument("items", parsedItem);
		for(String tag : item.getTags())
			this.insertTag(tag);
	}
	
	public List<Item> predictRecommendations(Item item, InferenceProcessor ip, boolean simpleComparator){
		List<Item> recommendations = new ArrayList<Item>();
		List<Bson> filters = new ArrayList<Bson>();
		for(String tag : item.getTags())
			filters.add(eq("tags",tag));
		List<JSONObject> similarItemsJsons = mongo.getDocumentWithFilter("items", or(filters));
		for(JSONObject itemJson : similarItemsJsons)
			recommendations.add(this.parseJsonItem(itemJson));
		recommendations.remove(item); //Remove the item for which to predict recommendations, as it may appear
		return this.rankRecommendations(recommendations, item, ip, simpleComparator);
	}
	
	private List<Item> rankRecommendations(List<Item> recommendations, Item item,
			InferenceProcessor ip, boolean simpleComparator){
		LinkedHashMap<Item, Double> itemsWithScores = new LinkedHashMap<Item, Double>();
		if(simpleComparator)
			for(Item recomItem : recommendations)
				itemsWithScores.put(recomItem, ip.altCompareTagSets(item.getTags(), recomItem.getTags()));
		else
			for(Item recomItem : recommendations)
				itemsWithScores.put(recomItem, ip.compareTagSets(item.getTags(), recomItem.getTags()));
		List<Map.Entry<Item, Double>> sortedEntries = new ArrayList<>(itemsWithScores.entrySet());
		Collections.sort(sortedEntries, 
				(entry0, entry1) -> Double.compare(entry0.getValue(), entry1.getValue()));
		Collections.reverse(sortedEntries);
		List<Item> sortedRecommendations = new ArrayList<Item>();
		for(Entry<Item, Double> entry : sortedEntries)
			sortedRecommendations.add(entry.getKey());
		return sortedRecommendations;
	}
	
	private JSONObject setObjectId(JSONObject json) {
		JSONObject _id = new JSONObject();
		_id.put("$oid", new ObjectId().toString());
		json.put("_id", _id);
		return json;
	}
	
	private Item parseJsonItem(JSONObject itemJson) {
		JSONArray tagsJson = itemJson.optJSONArray("tags");
		List<String> tags = new ArrayList<String>();
		if(tagsJson != null) {
			for(Object tagObject : tagsJson)
				tags.add((String) tagObject);
		}
		String text = itemJson.optString("text", "");
		if(text.equals(""))
			return new Item(itemJson.optString("name", ""), tags);
		return new Item(itemJson.optString("name", ""), tags, text);
	}
}
