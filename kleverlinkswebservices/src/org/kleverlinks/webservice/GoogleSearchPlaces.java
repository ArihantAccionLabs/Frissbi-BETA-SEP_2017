package org.kleverlinks.webservice;

import java.util.List;

import se.walkercrou.places.Place;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.Key;

public class GoogleSearchPlaces {
	
	private static final String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/search/json?";
	 
	private static final boolean PRINT_AS_STRING = false;


	 public void performSearch() throws Exception {
	 try {
	   System.out.println("Perform Search ....");
	   System.out.println("-------------------");
	   double latitude = 17.44374595;
	   double longitude = 78.36257359999999;
	   HttpTransport transport = new ApacheHttpTransport();
	   HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
	   HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
	   request.url.put("key", "AIzaSyCJE9LKLKMqMg8n8CNzpt5xdsS8VXumrhQ");
	   request.url.put("location", latitude + "," + longitude);
	   request.url.put("radius", 500);
	   request.url.put("sensor", "false");
	   
//	   GooglePlaces client = new GooglePlaces("AIzaSyCJE9LKLKMqMg8n8CNzpt5xdsS8VXumrhQ", new DefaultRequestHandler());
//	   java.util.List<se.walkercrou.places.Place> places = client.getNearbyPlaces(latitude, longitude, 5, GooglePlaces.MAXIMUM_RESULTS);
//	   //client.getNearbyPlaces
//	   Iterator<Place> iterator = places.iterator();
//	   while(iterator.hasNext()){
//		   Place place = iterator.next();
//		   System.out.println("place: "+place);
//	   }
//	   System.out.println();
	   
	   if (PRINT_AS_STRING) {
	    System.out.println(request.execute().parseAsString());
	   } else {
	    
	    PlacesList places = request.execute().parseAs(PlacesList.class);
	    System.out.println("STATUS = " + places.status);
	    for (Place place : places.results) {
	     System.out.println(place);
	    }
	   }

	  } catch (HttpResponseException e) {
	   System.err.println(e.response.parseAsString());
	   throw e;
	  }
	 }
	 
	 public static void main(String args[]) throws Exception{
		 GoogleSearchPlaces googleSearchPlaces = new GoogleSearchPlaces();
		 googleSearchPlaces.performSearch();
	 }
	 
	 public static HttpRequestFactory createRequestFactory(final HttpTransport transport){
		  return transport.createRequestFactory(new HttpRequestInitializer(){
		    public void initialize(    HttpRequest request){
		      //GoogleHeaders headers=new GoogleHeaders();
		      //headers.setApplicationName("Google-Places-DemoApp");
		      //request.headers=headers;
		      JsonHttpParser parser=new JsonHttpParser();
		      parser.jsonFactory=new JacksonFactory();
		      request.addParser(parser);
		    }
		  }
		);
	 }
}

class PlacesList {

	 @Key
	 public String status;

	 @Key
	 public List<Place> results;

	}