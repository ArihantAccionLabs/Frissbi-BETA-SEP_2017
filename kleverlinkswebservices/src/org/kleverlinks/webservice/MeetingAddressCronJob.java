package org.kleverlinks.webservice;


import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.util.service.TrackMeetingTime;

public class MeetingAddressCronJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0) {
	
      try{
    	  TrackMeetingTime.getMeetingListBetweenTime();
    	  System.out.println("TrackingMeetingAddress====   : "+ new Date());
    	  
      } catch (Exception e) {
	   e.printStackTrace();
	}
	}
}
