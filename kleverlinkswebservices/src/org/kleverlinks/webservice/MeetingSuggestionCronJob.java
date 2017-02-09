package org.kleverlinks.webservice;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.util.service.FreeTimeTracker;

public class MeetingSuggestionCronJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0)  {
		
		try{
			System.out.println("MeetingSuggestionCronJob===========================running at " + new Date());
			FreeTimeTracker.getUserFreeTime();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

}
