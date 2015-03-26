import java.util.Calendar;
import java.util.GregorianCalendar;

public class Task {
	
	private static final int CALENDAR_MONTH_OFFSET = -1;
	private String _name; 
	private String _description;
	private char _priority;
	private Calendar _startDate; 
	private Calendar _dueDate; 
	private boolean _isCompleted;
	private int _isDue;
	private boolean _shouldSync;
	private String _googleIdentifier;
	
	public String toString() {
	    return "[\"" + _name + "\", \"" +
	                   _description + "\", \"" +
	                   _priority + "\", \"" +
	                   _startDate + "\", \"" +
	                   _dueDate + "\", \"" +
	                   _isCompleted + "\", \"" +
	                   _isDue + "\", \"" +
	                   _shouldSync + "\"]";
	                   
	}
	
	/* Constructors */
	public Task(String name) {
		_name = new String(name);
		_googleIdentifier = "";
	}
	
	/* Mutators */
	public void setName(String name) {
		_name = name;
	}
	
	public void setDescription(String description) {
		_description = new String(description);
	}
	
	public void setPriority(char priority) {
		switch (priority) {
		case 'L': 
			_priority = 'L';
			break;
		case 'M': 
			_priority = 'M';
			break;
		case 'H': 
			_priority = 'H';
			break;
		default: 
			break;
		}
		
	}
	
	public void setStartDate (String startDateString) {
		int startDateInt = changeFromDateStringToDateInt(startDateString);
		startDateInt = formatToDDMMYYYY(startDateInt);
		
		int startDay = decodeDayFromDate(startDateInt);
		int startMonth = decodeMonthFromDate(startDateInt);
		int startYear = decodeYearFromDate(startDateInt);
		
		_startDate = new GregorianCalendar(startYear, startMonth + CALENDAR_MONTH_OFFSET, startDay);
	}
	
	public void setDueDate (String dueDateString) {
		int dueDateInt = changeFromDateStringToDateInt(dueDateString);
		dueDateInt = formatToDDMMYYYY(dueDateInt);
		
		int dueDay = decodeDayFromDate(dueDateInt);
		int dueMonth = decodeMonthFromDate(dueDateInt);
		int dueYear = decodeYearFromDate(dueDateInt);
		
		_dueDate = new GregorianCalendar(dueYear, dueMonth + CALENDAR_MONTH_OFFSET, dueDay);
	}

	private int formatToDDMMYYYY(int dateInt) {
		if(dateInt > 3012) {
			return dateInt;
		} 
		else {
			dateInt *= 10000;
			Calendar today = new GregorianCalendar();
			dateInt += today.get(Calendar.YEAR);
			return dateInt;
		}
	}
	
	public void setCompleted() {
		_isCompleted = true;
	}
	
	public void setNotCompleted() {
		_isCompleted = false;
	}
	
	public void updateIsDue() {
		Calendar today = new GregorianCalendar();
		_isDue = _dueDate.compareTo(today);
	}
	
	public void setShouldSync() {
		_shouldSync = true;
	}
	
	public void setShouldNotSync() {
		_shouldSync = false;
	}
	
	public void setGoogleIdentifier(String identifier) {
	    _googleIdentifier = identifier;
	}

	/* Accessors */
	public String getName() {
		return _name;
	}
	
	public String getDescription() {
		return _description;
	}
	
	public char getPriority() {
		return _priority;
	}
	
	public Calendar getStartDate() {
		return _startDate;
	}
	
	public Calendar getDueDate() {
		return _dueDate;
	}
	
	public boolean getIsCompleted() {
		return _isCompleted;
	}
	
	public int getIsDue() {
		return _isDue;
	}
	
	public boolean getShouldSync() {
		return _shouldSync;
	}
	
	public String getGoogleIdentifier() {
        return _googleIdentifier;
    }
	
	/* Static methods */
	public static int decodeYearFromDate(int date) {
		return date%10000;
	}
	
	public static int decodeMonthFromDate(int date) {
		int month = (date%1000000)/10000;
		if(month/10 == 0) {
			month = month%10;
		}
		return month;
	}

	public static int decodeDayFromDate(int date) {
		int day = date/1000000;
		if(day/10 == 0) {
			day = day%10;
		}
		return day;
	}
	
	public static int changeFromDateStringToDateInt(String dueDateString) {
		if(dueDateString.charAt(0) == '0') {
			dueDateString = dueDateString.replaceFirst("0", "");
		}
		
		int dueDateInt = Integer.valueOf(dueDateString);
		return dueDateInt;
	}

}
