package org.kleverlinks.webservice.gcm;

import java.io.IOException;

import org.json.JSONObject;
import org.kleverlinks.webservice.AuthenticateUser;
import org.kleverlinks.webservice.Constants;

public class SenderTest {

	public static void main(String args[]){
		Sender sender = new Sender(Constants.GCM_APIKEY);
		Message message = new Message.Builder()
		     .timeToLive(3)
		     .delayWhileIdle(true)
		     .dryRun(true)
		     .addData("message", "Anvesh has sent you meeting request")
//		     .addData("test1", "test1")
//		     .addData("test2", "test2")
//		     .addData("test3", "test3")
//		     .addData("test4", "test4")
		     .build();

		try {
			AuthenticateUser authenticateUser = new AuthenticateUser();
			JSONObject jsonObject = new JSONObject ( authenticateUser.getGCMDeviceRegistrationId(93l));
			String deviceRegistrationId = jsonObject.getString("DeviceRegistrationID");
			Result result = sender.send(message, deviceRegistrationId, 1);
			System.out.println(result);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
