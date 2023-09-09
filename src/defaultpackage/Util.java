package defaultpackage;

import java.util.Calendar;

public class Util {

	public static void printMessage(String message) {
		Calendar cal = Calendar.getInstance();
		String datetime = "" + cal.get(Calendar.YEAR); 
		datetime += "-" + (cal.get(Calendar.MONTH)+1);
		datetime += "-" + cal.get(Calendar.DATE);
		datetime += " " + cal.get(Calendar.HOUR_OF_DAY);
		datetime += ":" + cal.get(Calendar.MINUTE);
		datetime += ":" + cal.get(Calendar.SECOND);
		datetime += "." + cal.get(Calendar.MILLISECOND);
		System.out.println(datetime + " - " + message);
	}
}
