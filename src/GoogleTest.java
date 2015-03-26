import static org.junit.Assert.*;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;


public class GoogleTest {
    
    QLGoogleIntegration calInstance;
    
    @Before
    public void setup() {
        calInstance = new QLGoogleIntegration("user1");
    }

    @Test
    public void test() throws Exception {
        List<Task> taskList = new LinkedList<Task>();
        calInstance.syncFrom(taskList, "quicklyst");
        System.out.println(taskList);
    }

}
