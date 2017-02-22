package org.mongo.dao;

import java.util.Date;

import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoDBJDBC {
	
	public DBCollection getMongoCollection() {
		DBCollection collection = null;
		try{
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		DB db = mongoClient.getDB("FrissDB");
		collection = db.getCollection("User_Images");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return collection;
	}
	
	public String insertFile(String documentBytes){
		DBCollection collection = null;
		try{
			collection = getMongoCollection();
			BasicDBObject document = new BasicDBObject();
			document.put("documentBytes", documentBytes);
			document.put("createdDate", new Date());
			collection.insert(document);
			ObjectId objectId = document.getObjectId("_id");
			//System.out.println("objectId   " + objectId);
			
			return objectId+"";
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			collection.getDB().getMongo().close();
		}
		return null;
	}
	
	public String updateFile(String documentBytes , String id){
		DBCollection collection = null;
		try {
			System.out.println("id  :  "+id+" :  "+documentBytes);
	    DBObject dbObj = getFile(id);
	    collection  = getMongoCollection();
		 if(dbObj != null){
			
			
			DBObject document = new BasicDBObject();
			document.put("createdDate", new Date());
			document.put("documentBytes", documentBytes);
			
			BasicDBObject updateObj = new BasicDBObject();
			updateObj.put("$set", document);
			collection.update(dbObj , updateObj);
			updateObj.getObjectId("_id");
			System.out.println((collection.getCount() == 1)+"  collection.update(document, updateObj)"+ collection.getCount());
			if(collection.getCount() == 1){
				
			}
			return id;
		}
		}catch (Exception e) {
		 e.printStackTrace();
		}finally {
			collection.getDB().getMongo().close();
		}
		return null;
}
	
	public DBObject getFile(String id){
		DBCollection collection = null;
		try {
			collection  = getMongoCollection();
		BasicDBObject query = new BasicDBObject();
		query.put("_id", new ObjectId(id));
		DBObject dbObj = collection.findOne(query);
		
		if(dbObj != null){
		 return dbObj;
		}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			collection.getDB().getMongo().close();
		}
		return null;
	}

	public void deleteFile(String id) {
		DBCollection collection = null;
		try {
			collection  = getMongoCollection();
			DBObject myDoc = getFile(id);
			collection.remove(myDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			collection.getDB().getMongo().close();
		}
	}

}
