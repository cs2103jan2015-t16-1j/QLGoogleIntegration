import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;


public class GoogleCalConn {
    
    private static final String APPLICATION_NAME = "Quicklyst";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    private Credential _credential;

    private com.google.api.services.calendar.Calendar getService() 
            throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        return new com.google.api.services.calendar.Calendar.Builder(
               httpTransport, JSON_FACTORY, _credential).setApplicationName(APPLICATION_NAME).build();
    }
    
    public GoogleCalConn(Credential credential) {
        _credential = credential;
    }
    
    public Events getEvents(String calendarId) 
            throws Exception {
        return getService().events().list(calendarId).execute();
    }
    
    public CalendarList getCalendars() 
            throws Exception {
        return getService().calendarList().list().execute();
    }
    
    public Calendar getCalendarByID(String calendarId) 
            throws Exception {
        return getService().calendars().get(calendarId).execute();
    }
    
    public Event getEventByID(String calendarId, String eventId)
            throws Exception {
        return getService().events().get(calendarId, eventId).execute();
    }
    
    public Calendar createCalendar(Calendar calendar) 
            throws Exception {
        return getService().calendars().insert(calendar).execute();
    }
    
    public Event createEvent(String calendarId, Event event) 
            throws Exception {
        return getService().events().insert(calendarId, event).execute();
    }
    
    public Calendar updateCalendar(String calendarId, Calendar newCalendar) 
            throws Exception {
        return getService().calendars().update(calendarId, newCalendar).execute();
    }
    
    public Event updateEvent(String calendarId, String eventId, Event newEvent) 
            throws Exception {
        return getService().events().update(calendarId, eventId, newEvent).execute();
    }
    
    public void deleteCalendar(String calendarId) 
            throws Exception {
        getService().calendars().delete(calendarId).execute();
    }
    
    public void deleteEvent(String calendarId, String eventId) 
            throws Exception {
        getService().events().delete(calendarId, eventId).execute();
    }
}