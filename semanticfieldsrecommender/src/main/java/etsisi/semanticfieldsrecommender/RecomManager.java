package etsisi.semanticfieldsrecommender;

import java.util.ArrayList;

import org.bson.types.ObjectId;
import org.json.JSONObject;

import etsisi.utilities.MongoConnection;

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
	
	public void insertTag(String tag) {
		JSONObject parsedTag = new JSONObject();
		parsedTag.put("name", tag);
		parsedTag = this.setObjectId(parsedTag);
		if(!mongo.checkExistenceOneKey("tags", "name", tag)) { //Avoid duplicates
			mongo.insertDocument("tags", parsedTag);
		}
	}
	
	public void insertItem(Item item) {
		JSONObject parsedItem = new JSONObject();
		parsedItem.put("name", item.getName());
		parsedItem.put("tags", item.getTags());
		parsedItem = this.setObjectId(parsedItem);
		mongo.insertDocument("items", parsedItem);
		for(String tag : item.getTags())
			this.insertTag(tag);
	}
	
	private JSONObject setObjectId(JSONObject json) {
		JSONObject _id = new JSONObject();
		_id.put("$oid", new ObjectId().toString());
		json.put("_id", _id);
		return json;
	}
}
