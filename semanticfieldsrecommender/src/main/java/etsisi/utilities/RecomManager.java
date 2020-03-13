package etsisi.utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
	
	public ArrayList<String> getDatabaseTags() {
		ArrayList<String> databaseTags = new ArrayList<String>();
		ArrayList<JSONObject> tagObjects = mongo.getCollection("tags");
		for(JSONObject tagJson : tagObjects)
			databaseTags.add(tagJson.getString("name"));
		return databaseTags;
	}
	
	public ArrayList<Item> getDatabaseItems(InferenceProcessor ip){
		ArrayList<Item> databaseItems = new ArrayList<Item>();
		ArrayList<JSONObject> databaseJsons = mongo.getCollection("items");
		for(JSONObject itemJson : databaseJsons) {
			Item item = this.parseJsonItem(itemJson);
			item.setTags(ip.inferTags(item.getTags(), this.getDatabaseTags()));
		}
		return databaseItems;
	}
	
	public ArrayList<String> getDatabaseTexts(){
		ArrayList<String> databaseTexts = new ArrayList<String>();
		ArrayList<JSONObject> databaseJsons = mongo.getCollection("items");
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
	
	public ArrayList<Item> predictRecommendations(Item item, InferenceProcessor ip){
		ArrayList<Item> recommendations = new ArrayList<Item>();
		ArrayList<Bson> filters = new ArrayList<Bson>();
		for(String tag : item.getTags())
			filters.add(eq("tags",tag));
		ArrayList<JSONObject> similarItemsJsons = mongo.getDocumentWithFilter("items", or(filters));
		for(JSONObject itemJson : similarItemsJsons)
			recommendations.add(this.parseJsonItem(itemJson));
		recommendations.remove(item); //Remove the item for which to predict recommendations, as it may appear
		return this.rankRecommendations(recommendations, item, ip);
	}
	
	private ArrayList<Item> rankRecommendations(ArrayList<Item> recommendations, Item item,
			InferenceProcessor ip){
		LinkedHashMap<Item, Double> itemsWithScores = new LinkedHashMap<Item, Double>();
		for(Item recomItem : recommendations) {
			itemsWithScores.put(recomItem, ip.altCompareTagSets(item.getTags(), recomItem.getTags()));
		}
		ArrayList<Map.Entry<Item, Double>> sortedEntries = new ArrayList<>(itemsWithScores.entrySet());
		Collections.sort(sortedEntries, 
				(entry0, entry1) -> Double.compare(entry0.getValue(), entry1.getValue()));
		Collections.reverse(sortedEntries);
		ArrayList<Item> sortedRecommendations = new ArrayList<Item>();
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
		ArrayList<String> tags = new ArrayList<String>();
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
