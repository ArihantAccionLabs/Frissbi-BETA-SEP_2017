package org.kleverlinks.webservice;

import javax.ws.rs.core.MediaType;  
import com.sun.jersey.api.client.Client;  
import com.sun.jersey.api.client.ClientResponse;  
import com.sun.jersey.api.client.WebResource;  
import com.sun.jersey.api.client.config.ClientConfig;  
import com.sun.jersey.api.client.config.DefaultClientConfig;  
  
public class ConversionServiceClient {  
    static final String REST_URI = "http://localhost:8080/kleverlinkswebservices";  
    static final String USER_AUTHENTICATION = "/AuthenticateUserService/userAuthentication/";  
  
    public static void main(String[] args) {  
  
        ClientConfig config = new DefaultClientConfig();  
        Client client = Client.create(config);  
        WebResource service = client.resource(REST_URI);
        
        String username = "dharma_kolla";
        String password = "teja123";
        String ipAddress = "10.125.93.4";
  
        WebResource addService = service.path("rest").path(USER_AUTHENTICATION+username+"/"+password+"/"+ipAddress );  
        System.out.println("User Authentication Response: " + getResponse(addService));  
        System.out.println("User Authentication output: " + getOutputAsString(addService));  
        System.out.println("---------------------------------------------------");  
  
    }  
  
    private static String getResponse(WebResource service) {  
        return service.accept(MediaType.TEXT_XML).get(ClientResponse.class).toString();  
    }  
  
    private static String getOutputAsString(WebResource service) {  
        return service.accept(MediaType.TEXT_PLAIN ).get(String.class);  
    }  
}  
