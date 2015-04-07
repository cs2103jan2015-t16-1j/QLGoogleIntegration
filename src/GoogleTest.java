import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;



public class GoogleTest {
    
    QLGoogleIntegration gInstance;
    
    @Before
    public void setup() throws Exception {
        gInstance = new QLGoogleIntegration();
    }

    @Test
    public void testCalendar() throws Exception {
        List<Task> taskList = new LinkedList<Task>();
        Task t1 = new Task("task 1");
        t1.setDueDate("0404");
        t1.setStartDate("0304");
        Task t2 = new Task("task 2");
        t2.setDueDate("0404");
        Task t3 = new Task("task 3");
        taskList.add(t1);
        taskList.add(t2);
        taskList.add(t3);
        assertTrue(t1.getGoogleID() == null || t1.getGoogleID().equals(""));
        gInstance.syncTo(taskList);
        assertTrue(t1.getGoogleID() != null && !t1.getGoogleID().equals(""));
        List<Task> taskList2 = new LinkedList<Task>();
        gInstance.syncFrom(taskList2);
        
        for (Task tt : taskList) {
            Task match = null;
            for (Task t : taskList2) {
                if (t.getGoogleID() != null && t.getGoogleID().equals(tt.getGoogleID())) {
                    match = t;
                }
            }
            assertNotNull(match);
            
            if (tt.getDueDate() != null) {
                assertEquals(tt.getDueDate().get(Calendar.DAY_OF_MONTH), match.getDueDate().get(Calendar.DAY_OF_MONTH));
                assertEquals(tt.getDueDate().get(Calendar.MONTH), match.getDueDate().get(Calendar.MONTH));
            }
            tt.setDueDate("0705");
            gInstance.syncTo(taskList);
            gInstance.syncFrom(taskList2);
            assertEquals(7, match.getDueDate().get(Calendar.DAY_OF_MONTH));
            assertEquals(Calendar.MAY, match.getDueDate().get(Calendar.MONTH));
        }

        
    }

}
