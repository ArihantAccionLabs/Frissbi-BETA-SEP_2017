package org.kleverlinks.webservice;

import java.util.Date;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CronJob implements org.quartz.Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("hello cron executing==========="+new Date());
		
	}
}

