import java.io.IOException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

public class GoogleCalConn {

    private static final String APPLICATION_NAME = "Quicklyst";
    private static final JsonFactory JSON_FACTORY = GsonFactory
            .getDefaultInstance();

    private Credential _credential;
    private HttpTransport _httpTransport;

    private com.google.api.services.calendar.Calendar getService() {

        return new com.google.api.services.calendar.Calendar.Builder(
                _httpTransport, JSON_FACTORY, _credential).setApplicationName(
                APPLICATION_NAME).build();
    }

    public GoogleCalConn(Credential credential, HttpTransport httpTransport) {
        _credential = credential;
        _httpTransport = httpTransport;
    }

    public Events getEvents(String calendarId) throws IOException {
        return getService().events().list(calendarId).execute();
    }

    public CalendarList getCalendars() throws IOException {
        return getService().calendarList().list().execute();
    }

    public Calendar getCalendar(String calendarId) throws IOException {
        return getService().calendars().get(calendarId).execute();
    }

    public Event getEvent(String calendarId, String eventId) throws IOException {
        return getService().events().get(calendarId, eventId).execute();
    }

    public Calendar createCalendar(Calendar calendar) throws IOException {
        return getService().calendars().insert(calendar).execute();
    }

    public Event createEvent(String calendarId, Event event) throws IOException {
        return getService().events().insert(calendarId, event).execute();
    }

    public Calendar updateCalendar(String calendarId, Calendar newCalendar)
            throws IOException {
        return getService().calendars().update(calendarId, newCalendar)
                .execute();
    }

    public Event updateEvent(String calendarId, String eventId, Event newEvent)
            throws IOException {
        return getService().events().update(calendarId, eventId, newEvent)
                .execute();
    }

    public void deleteCalendar(String calendarId) throws IOException {
        getService().calendars().delete(calendarId).execute();
    }

    public void deleteEvent(String calendarId, String eventId)
            throws IOException {
        getService().events().delete(calendarId, eventId).execute();
    }
}