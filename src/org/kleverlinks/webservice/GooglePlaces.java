package org.kleverlinks.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

@Path("GooglePlacesService")
public class GooglePlaces {
	
		// JDBC driver name and database URL
		static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";

	@GET
	@Path("/nearByPlaces/{latitude}/{longitude}/{userId}/{meetingId}/{beginIndex}/{endIndex}")
	@Produces(MediaType.TEXT_PLAIN)
	public String nearByPlaces(@PathParam("latitude") double latitude, @PathParam("longitude") double longitude,
			@PathParam("userId") Long userId,@PathParam("meetingId") Long meetingId,@PathParam("beginIndex") int beginIndex, @PathParam("endIndex") int endIndex ){
		JSONArray jsonArray = new JSONArray();
		String url = "https://maps.googleapis.com/maps/api/place/search/json?location="
				+ latitude
				+ ","
				+ longitude
				+ "&rankby=distance&types=restaurant"
				+ "&sensor=true&key="+Constants.GCM_APIKEY;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);

		try {
			JSONObject jsonObject = new JSONObject(getOutputAsString(service));
			JSONArray results = (JSONArray) jsonObject.get("results");
			for (int i = beginIndex-1; i <endIndex; i++) {
				JSONObject result = new JSONObject();
				JSONObject resultsObject = (JSONObject) results.get(i);
				String name = (String) resultsObject.get("name");
				JSONObject geometry = (JSONObject) resultsObject
						.get("geometry");
				JSONObject location = geometry.getJSONObject("location");
				Double lat = (Double) location.get("lat");
				Double lng = (Double) location.get("lng");
				JSONArray types = (JSONArray) resultsObject.get("types");
				String vicinity = (String) resultsObject.get("vicinity");
				String place_id = (String) resultsObject.get("place_id");
				MeetingDetails meetingDetails = new MeetingDetails();
				String userDetails = meetingDetails.getUserDetailsByMeetingID(meetingId, userId);
				JSONArray array = new JSONArray(userDetails);
				JSONObject user = new JSONObject();
				if (array.length()>0){
				 user = array.getJSONObject(0);
				}
//				UserSettings userSettings = new UserSettings();
//				String preferredLocations = userSettings.getUserPreferredLocations(userId);
//				JSONArray array = new JSONArray(preferredLocations);
//				JSONObject obj =array.getJSONObject(0);
				Double userLatitude = Double.parseDouble(user.getString("olatitude"));
				Double userLongitude = Double.parseDouble(user.getString("oLongitude"));
				String distance = calculateDistance(userLatitude,userLongitude,lat,lng);
				
				
				 url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+place_id+"&key="+Constants.GCM_APIKEY;
					config = new DefaultClientConfig();
					client = Client.create(config);
					service = client.resource(url);
					JSONObject object = new JSONObject(getOutputAsString(service));
					
					JSONObject json = (JSONObject) object.get("result");
					String formatted_address = (String) json.get("formatted_address");
					
				result.put("name",name);
				result.put("lat",lat);
				result.put("lng",lng);
				result.put("distance",distance);
				result.put("formatted_address",formatted_address);				
				jsonArray.put(result);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonArray.toString();
	}
	
	
	
	@GET
	@Path("/nearByPlacesFromMidPoint/{latitude}/{longitude}")
	@Produces(MediaType.TEXT_PLAIN)
	public String nearByPlacesFromMidPoint(@PathParam("latitude") double latitude, @PathParam("longitude") double longitude
			){
		JSONArray jsonArray = new JSONArray();
		String url = "https://maps.googleapis.com/maps/api/place/search/json?location="
				+ latitude
				+ ","
				+ longitude
				+ "&rankby=distance&types=restaurant"
				+ "&sensor=true&key="+Constants.GCM_APIKEY;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);

		try {
			JSONObject jsonObject = new JSONObject(getOutputAsString(service));
			JSONArray results = (JSONArray) jsonObject.get("results");
			for (int i = 0; i <20; i++) {
				JSONObject result = new JSONObject();
				JSONObject resultsObject = (JSONObject) results.get(i);
				String name = (String) resultsObject.get("name");
				JSONObject geometry = (JSONObject) resultsObject
						.get("geometry");
				JSONObject location = geometry.getJSONObject("location");
				Double lat = (Double) location.get("lat");
				Double lng = (Double) location.get("lng");
				String place_id = (String) resultsObject.get("place_id");
				
				
				 url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+place_id+"&key="+Constants.GCM_APIKEY;
					config = new DefaultClientConfig();
					client = Client.create(config);
					service = client.resource(url);
					JSONObject object = new JSONObject(getOutputAsString(service));
					
					JSONObject json = (JSONObject) object.get("result");
					String formatted_address = (String) json.get("formatted_address");
					
				result.put("name",name);
				result.put("lat",lat);
				result.put("lng",lng);
				result.put("formatted_address",formatted_address);				
				jsonArray.put(result);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonArray.toString();
	}
	private static String getOutputAsString(WebResource service) {
		return service.accept(MediaType.TEXT_PLAIN).get(String.class);
	}
	
	public static void main(String args[]) {
		GooglePlaces googlePlaces = new GooglePlaces();
		//googlePlaces.nearByPlacesForMeeting(34, 1, 5);
		//googlePlaces.testNearByPlaces();
		//String distance = googlePlaces.calculateDistance(17.4474117,78.3762304,17.447188,78.378344);
		//System.out.println(distance);
		//String nearByPlaces = googlePlaces.nearByPlacesForMeeting(49, 12, 1, 5);
		//System.out.println(googlePlaces.nearByPlaces(17.444133255555556, 78.43088754444445, 36,119, 1, 5));
		//System.out.println(nearByPlaces);
		//System.out.println(googlePlaces.geoMagic(49));
		double lon11 =-97.116121;
	      double lat11=32.734642;

	      double lon22=-97.111658;
	      double lat22=32.731918;

	    //   calculateDistance(lat11,lon11, lat22, lon22);
	      //distFrom(lat11, lon11, lat22, lon22);
	}
	
	public void testNearByPlaces(){
		double latitude = 17.4474117;
		double longitude = 78.3762304;
		double radius = 10;
		// String url =
		// "https://maps.googleapis.com/maps/api/place/search/json?location="
		// + latitude +","+ longitude
		// +
		// "&radius="+radius+"&sensor=true&key=AIzaSyAsghDD-jnKfHH1hIrSUzTB5U3tRnoySjY";
		String url = "https://maps.googleapis.com/maps/api/place/search/json?location="
				+ latitude
				+ ","
				+ longitude
				+ "&rankby=distance&types=restaurant&food&bar&cafe"
				+ "&sensor=true&key="+Constants.GCM_APIKEY;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);

		try {
			JSONObject jsonObject = new JSONObject(getOutputAsString(service));
			//System.out.println(jsonObject);
			JSONArray results = (JSONArray) jsonObject.get("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject resultsObject = (JSONObject) results.get(i);
				String name = (String) resultsObject.get("name");
				JSONObject geometry = (JSONObject) resultsObject
						.get("geometry");
				JSONObject location = geometry.getJSONObject("location");
				Double lat = (Double) location.get("lat");
				Double lng = (Double) location.get("lng");
				JSONArray types = (JSONArray) resultsObject.get("types");
				String vicinity = (String) resultsObject.get("vicinity");
				String place_id = (String) resultsObject.get("place_id");
				System.out.println("Name : " + name);
				System.out.println("Latitude : " + lat);
				System.out.println("Longitude : " + lng);
				System.out.println("Types : " + types.toString());
				System.out.println("Vicinity : " + vicinity);
				System.out.println("place_id : " + place_id);
				
//				if ( resultsObject.has("rating")){
//					Double rating = (Double) resultsObject.get("rating")*1.0;
//					System.out.println("rating : " + rating+"\n\n");
//				}
				
				 url = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+place_id+"&key="+Constants.GCM_APIKEY;
				config = new DefaultClientConfig();
				client = Client.create(config);
				service = client.resource(url);
				JSONObject object = new JSONObject(getOutputAsString(service));
				
				JSONObject result = (JSONObject) object.get("result");
				String formatted_address = (String) result.get("formatted_address");
				//String formatted_phone_number = (String) result.get("formatted_phone_number");
				//String reviews =  result.get("reviews").toString();
				System.out.println("formatted_address : " + formatted_address);
				//System.out.println("formatted_phone_number : " + formatted_phone_number);
				//System.out.println("reviews : " + reviews);
				// System.out.println("Rating : " +rating);
				System.out.println("object : " + object);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String calculateDistance(double lat1,double long1,
			double lat2,double long2){
//		double lat1 = 17.4474117;
//		double long1 = 78.3762304;
//		double lat2 = 17.447188;
//		double long2 = 78.378344;
//		double radius = 10;
		
		String url = "https://maps.googleapis.com/maps/api/directions/json?origin="
				+ lat1
				+ ","
				+ long1
				+"&destination="
				+ lat2
				+ ","
				+ long2
				+ "&key="+Constants.GCM_APIKEY;
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource service = client.resource(url);

		try {
			JSONObject jsonObject = new JSONObject(getOutputAsString(service));
			JSONArray routes = (JSONArray) jsonObject.get("routes");
			JSONObject route = routes.getJSONObject(0);
			JSONArray legs = (JSONArray)route.get("legs");
			JSONObject leg =  legs.getJSONObject(0);
			JSONObject distance = (JSONObject) leg.get("distance");
			String distanceInKms = distance.getString("text");
			return distanceInKms;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	
}
