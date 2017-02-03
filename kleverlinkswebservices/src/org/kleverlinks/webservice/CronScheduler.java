package org.kleverlinks.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

@Path("CronSchedulerService")
public class CronScheduler {
	@GET
	@Path("/runCronScheduler")
	@Produces(MediaType.TEXT_PLAIN)
	public void doSomething() throws Exception {
           System.out.println("doSomething=============================");
          
          /* //Smart reminder before 2 hours from meeting
           JobKey jobKeyA = new JobKey("jobA", "group1");
       	   JobDetail smartMeetingReminderDetail = JobBuilder.newJob(SmartMeetingReminderCronJob.class).withIdentity(jobKeyA).build();
       	   Trigger smartMeetingReminderTrigger = TriggerBuilder.newTrigger().withIdentity("dummyTriggerName1", "group1").withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?")).build();
          */
           
           
       	   //Every day 8;00 AM in the morning cron job will run
       	   JobKey jobKeyBMorning = new JobKey("jobBM", "group2M");
    	   JobDetail meetingSuggestionMorningDetail = JobBuilder.newJob(MeetingSuggestionCronJob.class).withIdentity(jobKeyBMorning).build();
    	   Trigger meetingSuggestionMorningTrigger = TriggerBuilder.newTrigger().withIdentity("dummyTriggerName2", "group2M").withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 1/1 * ? *")).build();
    	   
       	   
    	   //Every day 8:00 PM in the evevning cron job will run
       	   JobKey jobKeyBEvening = new JobKey("jobBE", "group2E");
    	   JobDetail meetingSuggestionEveningDetail = JobBuilder.newJob(MeetingSuggestionCronJob.class).withIdentity(jobKeyBEvening).build();
    	   Trigger meetingSuggestionEveningTrigger = TriggerBuilder.newTrigger().withIdentity("dummyTriggerName3", "group2E").withSchedule(CronScheduleBuilder.cronSchedule("0 0 20 1/1 * ? *")).build();
    	   
    	   //This scheduler will run every minute
    	   JobKey meetingAddressJob = new JobKey("meetingAddressJob", "meetingAddressJob");
    	   JobDetail meetingAddressJobDetail = JobBuilder.newJob(MeetingAddressCronJob.class).withIdentity(meetingAddressJob).build();
    	   Trigger meetingAddressJobTrigger = TriggerBuilder.newTrigger().withIdentity("meetingAddressJobTrigger", "meetingAddressJob").withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *")).build();
    	   
    	   
    		Scheduler scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			//scheduler.scheduleJob(smartMeetingReminderDetail, smartMeetingReminderTrigger);
			scheduler.scheduleJob(meetingSuggestionMorningDetail, meetingSuggestionMorningTrigger);
			scheduler.scheduleJob(meetingSuggestionEveningDetail, meetingSuggestionEveningTrigger);
			scheduler.scheduleJob(meetingAddressJobDetail, meetingAddressJobTrigger);
			
	}
}
