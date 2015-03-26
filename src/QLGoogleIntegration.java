import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;

public class QLGoogleIntegration {
    private String _userId;
    
    public QLGoogleIntegration(String userId) {
        _userId = userId;
    }
    
    public List<Task> syncFrom(List<Task> taskList, String calendarName) {
        try {
            Credential cred = GoogleLogin.getCredential(_userId);
            GoogleCalConn googleCalendar = new GoogleCalConn(cred);
            String calId = "";
            List<CalendarListEntry> calendars = googleCalendar.getCalendars().getItems();
            for (CalendarListEntry e : calendars) {
                if (e.getSummary().equals(calendarName)) {
                    calId = e.getId();
                    break;
                }
            }
            List<Event> events = googleCalendar.getEvents(calId).getItems();
            for (Event e : events) {
                Task foundTask = null;
                for (Task t : taskList) {
                    if (t.getGoogleIdentifier().equals(e.getId())) {
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
    
    private Task updateTaskWithEvent(Task t, Event e) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        t.setName(e.getSummary());
        t.setGoogleIdentifier(e.getId());
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
}
