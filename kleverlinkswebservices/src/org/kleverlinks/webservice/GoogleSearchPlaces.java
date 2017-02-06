package org.kleverlinks.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

public class GoogleSearchPlaces {
	
	
	
	public static void getGoogleSearchPlaces(Double latitude , Double longitude , String type) throws IOException, JSONException {
		
		int radious = 1000;//in meter
		String keyword = type;
        String PLACES_SEARCH_URL =  "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius="+radious+"&type="+type+"&keyword="+keyword+"&key=AIzaSyAgM-mwjfosilNEdn5aCe4FJ9aDOp_U7JM";
		System.out.println("PLACES_SEARCH_URL======="+PLACES_SEARCH_URL);
        JSONObject json = readJsonFromUrl(PLACES_SEARCH_URL);
		System.out.println(json.toString());
		
		System.out.println("===="+json.getJSONArray("results"));
	}
	
	private static String readAll(Reader rd) throws IOException {
	    StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	      sb.append((char) cp);
	    }
	    return sb.toString();
	  }

	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
	    InputStream is = new URL(url).openStream();
	    try {
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
	      String jsonText = readAll(rd);
	      JSONObject json = new JSONObject(jsonText);
	      return json;
	    } finally {
	      is.close();
	    }
	  }
}





	/* 
	private static final boolean PRINT_AS_STRING = false;


	 public static void performSearch() throws Exception {
	 try {
	   System.out.println("Perform Search ....");
	   System.out.println("-------------------");
	   double latitude = 17.430702666666665;
	   double longitude =78.44064721111111;
	   HttpTransport transport = new ApacheHttpTransport();
	   HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
	   HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
	   request.url.put("key", Constants.GOOGLE_MAP_KEY);
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
//	   }//https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=500&type=restaurant&keyword=cruise&key=YOUR_API_KEY
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
class PlacesList {

	 @Key
	 public String status;

	 @Key
	 public List<Place> results;

	}*/