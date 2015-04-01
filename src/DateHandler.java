import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;

public class DateHandler {

	private static final String MESSAGE_INVALID_MONTH = "Invalid month entered. ";
	private static final String MESSAGE_INVALID_DAY = "Invalid day entered. ";
	private static final String MESSAGE_INVALID_DATE_FORMAT = "Invalid date format entered. ";
	private static final String MESSAGE_INVALID_TIME_FORMAT = "Invalid time format entered. ";
	private static final String MESSAGE_INVALID_DATE_TIME_FORMAT = "Invalid date/time format entered. ";

	private static final int OFFSET_CALENDAR_MONTH_FIELD = -1;
	
	private static final String DATE_TIME_FORMAT = "dd/MM/yy HH:mm";
	private static final String DATE_FORMAT = "dd/MM/yy";
	private static final String TIME_FORMAT = "HH:mm";

	private static int decodeYearFromDateInt(int date) {
		return date % 10000;
	}

	private static int decodeMonthFromDateInt(int date) {
		int month = (date % 1000000) / 10000;
		if (month / 10 == 0) {
			month = month % 10;
		}
		return month;
	}

	private static int decodeDayFromDateInt(int date) {
		int day = date / 1000000;
		return day;
	}

	private static int convertToDateInt(String dateString) {
		if (dateString.charAt(0) == '0') {
			dateString = dateString.replaceFirst("0", "");
		}

		if (dateString.equalsIgnoreCase("TDY")
				|| dateString.equalsIgnoreCase("TMR")) {
			Calendar dateTemp = new GregorianCalendar();

			if (dateString.equalsIgnoreCase("TMR")) {
				dateTemp.add(Calendar.DAY_OF_MONTH, 1);
			}

			int day = dateTemp.get(Calendar.DAY_OF_MONTH);
			int month = dateTemp.get(Calendar.MONTH) + 1;
			int year = dateTemp.get(Calendar.YEAR);

			if (month < 10) {
				dateString = String.valueOf(day) + "0" + String.valueOf(month)
						+ String.valueOf(year);
			} else {
				dateString = String.valueOf(day) + String.valueOf(month)
						+ String.valueOf(year);
			}
		}

		int dateInt = formatDateIntToDDMMYYYY(Integer.valueOf(dateString));
		return dateInt;
	}

	private static int formatDateIntToDDMMYYYY(int dateInt) {
		if (String.valueOf(dateInt).length() == 8
				|| String.valueOf(dateInt).length() == 7) {
			return dateInt;
		} else {
			dateInt *= 10000;
			Calendar today = new GregorianCalendar();
			dateInt += today.get(Calendar.YEAR);
			return dateInt;
		}
	}

	public static boolean isValidDateFormat(String dateString,
			StringBuilder feedback) {
		if (!(dateString.length() == 4 || dateString.length() == 8 || dateString
				.length() == 3)) {
			feedback.append(MESSAGE_INVALID_DATE_FORMAT);
			return false;
		}

		if (dateString.length() == 3) {
			if (dateString.equalsIgnoreCase("TDY")
					|| dateString.equalsIgnoreCase("TMR")) {
				return true;
			} else {
				feedback.append(MESSAGE_INVALID_DATE_FORMAT);
				return false;
			}
		}

		try {
			int dateInt = convertToDateInt(dateString);
			int day = decodeDayFromDateInt(dateInt);
			int month = decodeMonthFromDateInt(dateInt);

			if (day > 31) {
				feedback.append(MESSAGE_INVALID_DAY);
				return false;
			}
			if (month > 12) {
				feedback.append(MESSAGE_INVALID_MONTH);
				return false;
			}

		} catch (NumberFormatException e) {
			feedback.append(MESSAGE_INVALID_DATE_FORMAT);
			return false;
		}
		return true;
	}

	public static Calendar convertToDateCalendar(String dateString) {

		int dateInt = convertToDateInt(dateString);
		int dayInt = decodeDayFromDateInt(dateInt);
		int monthInt = decodeMonthFromDateInt(dateInt);
		int yearInt = decodeYearFromDateInt(dateInt);
		return (Calendar) new GregorianCalendar(yearInt, monthInt
				+ OFFSET_CALENDAR_MONTH_FIELD, dayInt);
	}
	


	public static void main(String args[]) {
		//SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy HHmm");
		//SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");
		// SimpleDateFormat format = new SimpleDateFormat("ddMMyy");
		// SimpleDateFormat format = new SimpleDateFormat("ddMM");
		SimpleDateFormat format = new SimpleDateFormat("EEE MM");
		Calendar cal = new GregorianCalendar();
		Scanner sc = new Scanner(System.in);
		while (true) {
			String datetimeStr = sc.nextLine();
			try {
				cal.setTime(format.parse(datetimeStr));
				System.out.println(cal.getTime().toString());
				System.out.println(cal.get(Calendar.DAY_OF_WEEK));
			} catch (NullPointerException e) {
				System.out.println(e.getMessage());
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
		}
	}

}
