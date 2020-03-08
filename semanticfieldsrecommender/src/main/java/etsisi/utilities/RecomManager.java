package etsisi.utilities;

import java.util.ArrayList;

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
			item.setTags(ip.inferTags(item.getTags()));
		}
		return databaseItems;
	}
	
	public void insertTag(String tag) {
		JSONObject parsedTag = new JSONObject();
		parsedTag.put("name", tag);
		parsedTag = this.setObjectId(parsedTag);
		if(!mongo.checkExistenceOneKey("tags", "name", tag)) { //Avoid duplicates
			mongo.insertDocument("tags", parsedTag);
		}
	}
	
	public void insertItem(Item item, InferenceProcessor ip) {
		item.setTags(ip.inferTags(item.getTags()));
		JSONObject parsedItem = new JSONObject();
		parsedItem.put("name", item.getName());
		parsedItem.put("tags", item.getTags());
		parsedItem = this.setObjectId(parsedItem);
		mongo.insertDocument("items", parsedItem);
		for(String tag : item.getTags())
			this.insertTag(tag);
	}
	
	public ArrayList<Item> predictRecommendations(Item item){
		ArrayList<Item> recommendations = new ArrayList<Item>();
		ArrayList<Bson> filters = new ArrayList<Bson>();
		for(String tag : item.getTags())
			filters.add(eq("tags",tag));
		ArrayList<JSONObject> similarItemsJsons = mongo.getDocumentWithFilter("items", or(filters));
		for(JSONObject itemJson : similarItemsJsons)
			recommendations.add(this.parseJsonItem(itemJson));
		return recommendations;
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
		if(tags != null) {
			for(Object tagObject : tagsJson)
				tags.add((String) tagObject);
		}
		return new Item(itemJson.optString("name", ""), tags);
	}
}
