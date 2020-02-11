package com.iksgmbh.sysnat.common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SysNatDateUtil
{
	final private static DateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy"); 
	final private static DateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm"); 

	public static String formatDuration(long millis)
	{
	     long hours = TimeUnit.MILLISECONDS.toHours(millis);
	     millis -= TimeUnit.HOURS.toMillis(hours);
	     String hh = "" + hours;
	     if (hh.length() == 1) hh = "0" + hours;
	     
	     long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
	     millis -= TimeUnit.MINUTES.toMillis(minutes);
	     String mm = "" + minutes;
	     if (mm.length() == 1) mm = "0" + minutes;

	     long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
	     String ss = "" + seconds;
	     if (ss.length() == 1) ss = "0" + seconds;

	     return hh + ":" + mm + ":" + ss;
	}

	public static String getNowDateString() {
		return DATE_FORMATTER.format(new Date());
	}

	public static String getNowTimeString() {
		return TIME_FORMATTER.format(new Date());
	}

}
