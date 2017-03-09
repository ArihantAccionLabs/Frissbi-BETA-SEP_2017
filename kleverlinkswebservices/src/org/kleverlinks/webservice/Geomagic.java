package org.kleverlinks.webservice;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.service.dto.UserDTO;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import javafx.geometry.Point2D;

public class Geomagic {


	public static String calculateMidPoint() {
		  String[] locations = new String[]{ "Kothapet" ,"Manikonda", "Secunderabad","Mehdipatnam","Miyapur","Khairatabad","Himayathnagar","Begumpet","Ameerpet","Madhapur","Kukatapally"};
		String midpointLocation = "";
		Double midLatitude =0.0;
		Double midLongitude=0.0;
		ArrayList<Point2D> Point2Ds = new ArrayList<Point2D>();
		for (int i = 0; i < locations.length; i++) {
			String url = "https://maps.googleapis.com/maps/api/geocode/json?address="+ locations[i]+ "&region=es&key="+Constants.GCM_APIKEY;
			ClientConfig config = new DefaultClientConfig();
			Client client = Client.create(config);
			WebResource service = client.resource(url);
			
			try {
				JSONObject jsonObject = new JSONObject(
						getOutputAsString(service));
				JSONArray results = (JSONArray) jsonObject.get("results");
				JSONObject resultsObject = (JSONObject) results.get(0);
				JSONObject geometry = (JSONObject) resultsObject
						.get("geometry");
				JSONObject location = geometry.getJSONObject("location");
				Double latitude = (Double) location.get("lat");
				Double longitude = (Double) location.get("lng");
				Point2D e = new Point2D(latitude, longitude);
		        Point2Ds.add( e);
				System.out.println("latitude value of location "+locations[i] + " is: "+latitude+"\n" );
				System.out.println("longitude value of location "+locations[i] + " is: "+longitude+"\n" );
				//midLatitude+=latitude;
				//midLongitude+=longitude;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//midLatitude/=no_locations;
		//midLongitude/=no_locations;
		
		//System.out.println("Middle latitude value of all locations is: "+midLatitude +"\n");
		//System.out.println("Middle longitude value of all locations is: "+midLongitude+"\n" );
		
		QuickHull qh = new QuickHull();
        ArrayList<Point2D> p = qh.quickHull(Point2Ds);
        System.out.println("The Points in the Convex hull using Quick Hull are: ");
        	for (int i = 0; i < p.size(); i++)
        		System.out.println("(" + p.get(i).getX() + ", " + p.get(i).getY() + ")\n");
		Point2D centroid = qh.findCentroid(Point2Ds);
		System.out.println("Middle latitude value of all locations is: "+centroid.getX() +"\n");
		System.out.println("Middle longitude value of all locations is: "+centroid.getY()+"\n" );
		midLatitude = centroid.getX();
		midLongitude = centroid.getY();
		
		//Doing reverse geocoding
		String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+midLatitude+","+midLongitude+"&key="+Constants.GCM_APIKEY;;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);
		
		try {
			JSONObject jsonObject = new JSONObject(getOutputAsString(service));
			JSONArray results = (JSONArray) jsonObject.get("results");
			JSONObject resultsObject = (JSONObject) results.get(0);
			String  formattedAddress = (String) resultsObject.get("formatted_address");
			System.out.println("Mid point location address is: "+formattedAddress );
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return midpointLocation;
	}


	public static JSONObject calculateMidPointLatLng(List<UserDTO> userDTOList) {
		Double midLatitude =0.0;
		Double midLongitude=0.0;
		JSONObject finalJson = new JSONObject();
		ArrayList<Point2D> Point2Ds = new ArrayList<Point2D>();
		for (UserDTO  userDTO : userDTOList) {
	
			try {
				Double latitude =  Double.parseDouble(userDTO.getLatitude());
				Double longitude = Double.parseDouble(userDTO.getLongitude());
				Point2D point2d = new Point2D(latitude, longitude);
		        Point2Ds.add(point2d);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		QuickHull qh = new QuickHull();
        ArrayList<Point2D> p = qh.quickHull(Point2Ds);
        System.out.println("The Points in the Convex hull using Quick Hull are: ");
        	for (int i = 0; i < p.size(); i++)
        		System.out.println("(" + p.get(i).getX() + ", " + p.get(i).getY() + ")\n");
		Point2D centroid = qh.findCentroid(Point2Ds);
		System.out.println("Middle latitude value of all locations is: "+centroid.getX() +"\n");
		System.out.println("Middle longitude value of all locations is: "+centroid.getY()+"\n" );
		midLatitude = centroid.getX();
		midLongitude = centroid.getY();
		
		String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+midLatitude+","+midLongitude+"&key="+Constants.GCM_APIKEY;;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);
		
		try {
			JSONObject jsonObject = new JSONObject(getOutputAsString(service));
			JSONArray results = (JSONArray) jsonObject.get("results");
			JSONObject resultsObject = (JSONObject) results.get(0);
			String  formattedAddress = (String) resultsObject.get("formatted_address");
			
			finalJson.put("lat", midLatitude);
			finalJson.put("lng", midLongitude);
			finalJson.put("address", formattedAddress);
			
			System.out.println("Mid point location address is: "+formattedAddress );
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return finalJson;
	}

	private static String getOutputAsString(WebResource service) {
		return service.accept(MediaType.TEXT_PLAIN).get(String.class);
	}
}
