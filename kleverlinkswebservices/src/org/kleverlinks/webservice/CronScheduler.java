package org.kleverlinks.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
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

			JobDetail jobDetail = JobBuilder.newJob(CronJob.class).build();
			Trigger t1 = TriggerBuilder.newTrigger().withIdentity("SimpleTrigger").startNow().build();
			//Trigger t1 = TriggerBuilder.newTrigger().withIdentity("CronTrigger").withSchedule(CronScheduleBuilder.cronSchedule("0 0/1 * 1/1 * ? *")).build();
			//Trigger t1 = TriggerBuilder.newTrigger().withIdentity("CronTrigger").withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(05).repeatForever()).build();
			Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			System.out.println("mail is going to send");
			MyEmailer.SendMail("sunil@thrymr.net", "testing mail", "hello man");;
			System.out.println("mail sent successfully");
			scheduler.scheduleJob(jobDetail, t1);
	}
}
