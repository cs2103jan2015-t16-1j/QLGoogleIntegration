import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

public class QLGoogleIntegration {
    //private String _userId;
    
    //public QLGoogleIntegration(String userId) {
    //    _userId = userId;
    //}
    
    public static List<Task> syncFrom(String userId, String calendarName, List<Task> taskList) {
        try {
            Credential cred = GoogleLogin.getCredential(userId);
            GoogleCalConn googleCalendar = new GoogleCalConn(cred);
            List<CalendarListEntry> calendars = googleCalendar.getCalendars().getItems();
            String calId = getCalendarIdByName(calendarName, calendars);
            List<Event> events = googleCalendar.getEvents(calId).getItems();
            for (Event e : events) {
                Task foundTask = null;
                for (Task t : taskList) {
                    if (t.getGoogleID().equals(e.getId())) {
                        foundTask = t;
                    }
                }
                if (foundTask == null) {
                    foundTask = new Task("");
                    taskList.add(foundTask);
                }
                updateTaskWithEvent(foundTask, e);
            }
            return taskList;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static void syncTo(String userId, String calendarName, List<Task> taskList) throws Exception {
    	try {
            Credential cred = GoogleLogin.getCredential(userId);
            GoogleCalConn googleCalendar = new GoogleCalConn(cred);
            List<CalendarListEntry> calendars = googleCalendar.getCalendars().getItems();
            String calId = getCalendarIdByName(calendarName, calendars);
            if (calId.equals("")) {
            	calId = googleCalendar.createCalendar(new Calendar().setSummary(calendarName)).getId();
            }
            for (Task t : taskList) {
                if (isCalendarEvent(t)) {
                	if ((t.getGoogleID() != null) && (!t.getGoogleID().equals(""))) {
                		Event e = googleCalendar.getEvent(calId, t.getGoogleID());
                		e = updateEventWithTask(e, t);
                		googleCalendar.updateEvent(calId, t.getGoogleID(), e);
                	} else {
                		Event e = new Event();
                		e = updateEventWithTask(e, t);
                		t.setGoogleID(googleCalendar.createEvent(calId, e).getId());
                	}
                }
            }
        } catch (Exception e) {
            //throw e;
            throw new Error(e.getMessage());
        }
    }

    private static String getCalendarIdByName(String calendarName, List<CalendarListEntry> calendars) {
        String calId = "";
        for (CalendarListEntry e : calendars) {
            if (e.getSummary().equals(calendarName)) {
                calId = e.getId();
                break;
            }
        }
        return calId;
    }
    
    private static Task updateTaskWithEvent(Task t, Event e) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        t.setName(e.getSummary());
        t.setGoogleID(e.getId());
        t.setDescription(e.getDescription());
        if (e.getStart().getDate() != null) {
            t.setStartDate(sdf.format(new Date(e.getStart().getDate().getValue())));
        } else if (e.getStart().getDateTime() != null) {
            t.setStartDate(sdf.format(new Date(e.getStart().getDateTime().getValue())));
        }
        if (e.getEnd().getDate() != null) {
            t.setDueDate(sdf.format(new Date(e.getEnd().getDate().getValue())));
        } else if (e.getEnd().getDateTime() != null) {
            t.setDueDate(sdf.format(new Date(e.getEnd().getDateTime().getValue())));
        }

        return t;
    }
    
    private static Event updateEventWithTask(Event e, Task t) {
    	e.setSummary(t.getName());
    	e.setDescription(t.getDescription());
		if (t.getHasStartTime()) {
		    com.google.api.client.util.DateTime dt = new com.google.api.client.util.DateTime(false, t.getStartDate().getTimeInMillis(),  0);
			e.setStart(new EventDateTime().setDateTime(dt));
		} else {
		    com.google.api.client.util.DateTime dt = new com.google.api.client.util.DateTime(true, t.getStartDate().getTimeInMillis(),  0);
			e.setStart(new EventDateTime().setDate(dt));
		}
		if (t.getHasDueTime()) {
		    com.google.api.client.util.DateTime dt = new com.google.api.client.util.DateTime(false, t.getDueDate().getTimeInMillis(),  0);
			e.setEnd(new EventDateTime().setDateTime(dt));
		} else {
		    com.google.api.client.util.DateTime dt = new com.google.api.client.util.DateTime(true, t.getDueDate().getTimeInMillis(),  0);
			e.setEnd(new EventDateTime().setDate(dt));
		}
    	return e;
    }
    
    private static boolean isCalendarEvent(Task t) {
        return ((t.getStartDate() != null) && (t.getDueDate() != null));
    }
    
}
