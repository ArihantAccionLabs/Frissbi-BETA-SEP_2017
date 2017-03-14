package org.mongo.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.kleverlinks.webservice.Constants;
import org.kleverlinks.webservice.DataSourceConnection;
import org.util.Utility;
import org.util.service.ServiceUtility;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class MongoDBJDBC {
	
	public DBCollection getMongoCollection() {
		DBCollection collection = null;
		try{
		MongoClient mongoClient = new MongoClient(Constants.MONGO_DB_IP, Constants.MONGO_DB_PORT);
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

	public String getUriImage(String imageId){
		
		if (Utility.checkValidString(imageId)) {
			DBObject dbObj = getFile(imageId);
			if (dbObj != null) {
				return new JSONObject(dbObj.toString()).getString("documentBytes");
			}
		}
		return null;
	}
	public String insertCoverImageToMongoDb(JSONObject imageJson) {

		String mongoFileId = null;
		try {
			JSONObject profilejson = ServiceUtility.getUserImageId(imageJson.getLong("userId"));
			System.out.println("profilejson  :   "+profilejson.toString());
			
			if (profilejson != null && profilejson.has("coverImageID")) {
				mongoFileId = updateFile(imageJson.getString("file"),profilejson.getString("coverImageID"));
			} else {
				mongoFileId = insertFile(imageJson.getString("file"));
			}
			System.out.println("fileId  :  " + mongoFileId);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return mongoFileId;
	}
	
	public String insertProfileImageToMongoDb(JSONObject imageJson) {

		String mongoFileId = null;
		try {
			JSONObject profilejson = ServiceUtility.getUserImageId(imageJson.getLong("userId"));
			if (profilejson != null && profilejson.has("profileImageId")) {
				mongoFileId = updateFile(imageJson.getString("file"),profilejson.getString("profileImageId"));
			} else {
				mongoFileId = insertFile(imageJson.getString("file"));
			}
			System.out.println("fileId  :  " + mongoFileId);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return mongoFileId;
	}
	
	public Boolean updateCoverImage(JSONObject imageJson){

		Connection conn = null;
		CallableStatement callableStatement = null;
		Boolean isInserted = false;
		try {
			
				conn = DataSourceConnection.getDBConnection();
				String insertStoreProc = "{call usp_UpdateUserCoverProfile(?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setLong(1, imageJson.getLong("userId"));
				callableStatement.setString(2, imageJson.getString("mongoFileId"));
				callableStatement.registerOutParameter(3, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				
				int isError = callableStatement.getInt(3);
				System.out.println(isError+"  value  :" +value);
				if(value != 0){
					isInserted = true;
				}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		} 
		return isInserted;
	}
	public Boolean updateProfileImage(JSONObject imageJson){

		Connection conn = null;
		CallableStatement callableStatement = null;
		Boolean isInserted = false;
		System.out.println("imageJson  :  "+imageJson.toString());
		try {
			
				conn = DataSourceConnection.getDBConnection();
				String insertStoreProc = "{call usp_UpdateUserProfile(?,?,?)}";
				callableStatement = conn.prepareCall(insertStoreProc);
				callableStatement.setLong(1, imageJson.getLong("userId"));
				callableStatement.setString(2, imageJson.getString("mongoFileId"));
				callableStatement.registerOutParameter(3, Types.INTEGER);
				int value = callableStatement.executeUpdate();
				
				int isError = callableStatement.getInt(3);
				System.out.println(isError+"  value  :" +value);
				if(value != 0){
					isInserted = true;
				}
		} catch (SQLException se) {
			se.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ServiceUtility.closeConnection(conn);
			ServiceUtility.closeCallableSatetment(callableStatement);
		} 
		return isInserted;
	}
}
