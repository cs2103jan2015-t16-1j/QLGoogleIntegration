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
    
    //QLGoogleIntegration calInstance;
    
    @Before
    public void setup() {
        //calInstance = new QLGoogleIntegration("user1");
    }

    @Test
    public void test() throws Exception {
        List<Task> taskList = new LinkedList<Task>();
        Task t1 = new Task("task 1");
        t1.setDueDate("0404");
        t1.setStartDate("0304");
        taskList.add(t1);
        assertTrue(t1.getGoogleID() == null || t1.getGoogleID().equals(""));
        QLGoogleIntegration.syncTo("user1", "quicklyst", taskList);
        assertTrue(t1.getGoogleID() != null && !t1.getGoogleID().equals(""));
        List<Task> taskList2 = new LinkedList<Task>();
        QLGoogleIntegration.syncFrom("user1", "quicklyst", taskList2);
        
        Task match = null;
        for (Task t : taskList2) {
            if (t.getGoogleID().equals(t1.getGoogleID()))
                match = t;
        }
        assertNotNull(match);
        
        t1.setDueDate("0505");
        QLGoogleIntegration.syncTo("user1", "quicklyst", taskList);
        
        assertEquals(4, match.getDueDate().get(java.util.Calendar.DAY_OF_MONTH));
        assertEquals(java.util.Calendar.APRIL, match.getDueDate().get(java.util.Calendar.MONTH));
        QLGoogleIntegration.syncFrom("user1", "quicklyst", taskList2);
        assertEquals(5, match.getDueDate().get(java.util.Calendar.DAY_OF_MONTH));
        assertEquals(java.util.Calendar.MAY, match.getDueDate().get(java.util.Calendar.MONTH));
        
    }

}
