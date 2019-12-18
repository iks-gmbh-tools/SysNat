package com.iksgmbh.sysnat.common.utils;

import java.util.concurrent.TimeUnit;

public class SysNatDateUtil
{
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

}
