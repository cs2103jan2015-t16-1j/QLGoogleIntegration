import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.tasks.model.TaskList;

public class QLGoogleIntegration {
    
    private static final String PRIMARY_CALENDAR_ID = "primary";
    private static final String PRIMARY_TASKS_ID = "@default";
    
    private static final String PREFIX_GOOGLEID_TASKS = "t";
    private static final String PREFIX_GOOGLEID_CALENDAR = "c";
    private static final String USER_ID = "u";
    
    
    private static final int PREFIX_GOOGLEID_LENGTH = 1;
    
    private String _userId;
    private boolean _shouldRememberLogin;
    
    private Credential _cred;
    private GoogleCalConn _googleCalendar;
    private GoogleTaskConn _googleTasks;
    
    
    public QLGoogleIntegration() {
        _userId = USER_ID;
        _shouldRememberLogin = false;
    }
    
    public QLGoogleIntegration(String userId, boolean shouldRememberLogin) {
        _userId = userId;
        _shouldRememberLogin = shouldRememberLogin;
    }
    
    private void init() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        DataStoreFactory dataStoreFactory;
        if (_shouldRememberLogin) { 
            File f = new File("googlecred");
            dataStoreFactory = new FileDataStoreFactory(f);
        } else {
            dataStoreFactory = new MemoryDataStoreFactory();
        }
            
        
        _cred = GoogleLogin.getCredential(_userId, httpTransport, dataStoreFactory);
        _googleCalendar = new GoogleCalConn(_cred, httpTransport);
        _googleTasks = new GoogleTaskConn(_cred, httpTransport);
    }
    
    private boolean isInitiated() {
        return (_cred != null && _googleCalendar != null && _googleTasks != null);
    }
    
    public List<Task> syncFrom(List<Task> taskList) {
        try {
        
            if (!isInitiated()) {
                    init();
            }
            
            String calId = PRIMARY_CALENDAR_ID;
            String taskListId = PRIMARY_TASKS_ID;
            
            syncGoogleToTaskList(taskList, _googleCalendar, _googleTasks, calId, taskListId);
            
            return taskList;
            
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public void syncTo(List<Task> taskList) {
        try {
        
            if (!isInitiated()) {
                init();
            }
            
            String calId = PRIMARY_CALENDAR_ID;
            String taskListId = PRIMARY_TASKS_ID;
            
            syncTaskListToGoogle(taskList, _googleCalendar, _googleTasks, calId,
                    taskListId);
            
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void syncGoogleToTaskList(List<Task> taskList,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws IOException {
        
        Map<String, Task> calendarTask = new HashMap<String, Task>();
        Map<String, Task> tasksTask = new HashMap<String, Task>();
        
        createMapsForSync(taskList, calendarTask, tasksTask);
        
        syncGoogleCalendarToTaskList(taskList, googleCalendar, calId,
                calendarTask);
        
        syncGoogleTasksToTaskList(taskList, googleTasks, taskListId, tasksTask);
    }

    private void syncGoogleTasksToTaskList(List<Task> taskList,
            GoogleTaskConn googleTasks, String taskListId,
            Map<String, Task> tasksTask) throws IOException {
        if (taskListId.isEmpty())
            return;
        List<com.google.api.services.tasks.model.Task> tasks = googleTasks.getTasks(taskListId).getItems();
        for (com.google.api.services.tasks.model.Task t : tasks) {
            Task matchingTask = tasksTask.remove(PREFIX_GOOGLEID_TASKS + t.getId());
            if (matchingTask == null) {
                matchingTask = new Task("");
                taskList.add(matchingTask);
            }
            updateTaskWithGoogleTask(matchingTask, t);
        }
        
        for (Task t : tasksTask.values()) {
            taskList.remove(t);
        }
    }

    private void syncGoogleCalendarToTaskList(List<Task> taskList,
            GoogleCalConn googleCalendar, String calId,
            Map<String, Task> calendarTask) throws IOException {
        if (calId.isEmpty())
            return;
        List<Event> events = googleCalendar.getEvents(calId).getItems();
        for (Event e : events) {
            Task matchingTask = calendarTask.remove(PREFIX_GOOGLEID_CALENDAR + e.getId());
            if (e.getRecurrence() != null) {
                continue;
            }
            if (matchingTask == null) {
                matchingTask = new Task("");
                taskList.add(matchingTask);
            }
            updateTaskWithGoogleEvent(matchingTask, e);
        }
        
        for (Task t : calendarTask.values()) {
            taskList.remove(t);
        }
        
    }

    private void createMapsForSync(List<Task> taskList,
            Map<String, Task> calendarTask, Map<String, Task> tasksTask) {
        for (Task t: taskList) {
            if ((t.getGoogleID() != null) && (!t.getGoogleID().isEmpty())) {
                if (t.getGoogleID().startsWith(PREFIX_GOOGLEID_CALENDAR)) {
                    calendarTask.put(t.getGoogleID(), t);
                } else {
                    tasksTask.put(t.getGoogleID(), t);
                }
            }
        }
    }
    
    

    private String createNewTaskListIfNotExist(String listName,
            GoogleTaskConn googleTasks, String taskListId) throws IOException {
        if (taskListId.equals("")) {
            taskListId = googleTasks.createTaskList(new TaskList().setTitle(listName)).getId();
        }
        return taskListId;
    }

    private String createNewCalendarIfNotExist(String listName,
            GoogleCalConn googleCalendar, String calId) throws IOException {
        if (calId.equals("")) {
            calId = googleCalendar.createCalendar(new Calendar().setSummary(listName)).getId();
        }
        return calId;
    }

    private void syncTaskListToGoogle(List<Task> taskList,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws IOException {
        for (Task t : taskList) {
            if (isCalendarEvent(t)) {
                syncTaskToGoogleCalendar(googleCalendar, googleTasks, calId,
                        taskListId, t);
            } else {
                syncTaskToGoogleTasks(googleCalendar, googleTasks, calId,
                        taskListId, t);
            }
        }
    }

    private void syncTaskToGoogleCalendar(GoogleCalConn googleCalendar,
            GoogleTaskConn googleTasks, String calId, String taskListId, Task t) throws IOException {
        if ((t.getGoogleID() != null) && (!t.getGoogleID().isEmpty())) {
            if (t.getGoogleID().startsWith(PREFIX_GOOGLEID_CALENDAR)) {
                updateEventToGoogleCalendar(t, googleCalendar, calId);
            } else {
                changeGoogleTaskToGoogleCalendar(t, googleCalendar, googleTasks, 
                                                 calId, taskListId);
            }
        } else {
            createNewEventToGoogleCalendar(t, googleCalendar, calId);
        }
    }
    
    private void updateEventToGoogleCalendar(Task t,
            GoogleCalConn googleCalendar, String calId) throws IOException {
        String id = t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH);
        Event e = googleCalendar.getEvent(calId, id);
        e = updateGoogleEventWithTask(e, t);
        googleCalendar.updateEvent(calId, id, e);
    }
    
    private void createNewEventToGoogleCalendar(Task t,
            GoogleCalConn googleCalendar, String calId) throws IOException {
        Event e = new Event();
        e = updateGoogleEventWithTask(e, t);
        t.setGoogleID(PREFIX_GOOGLEID_CALENDAR + googleCalendar.createEvent(calId, e).getId());
    }

    private void changeGoogleTaskToGoogleCalendar(Task t,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws IOException {
        googleTasks.deleteTask(taskListId, t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH));
        t.setGoogleID(null);
        createNewEventToGoogleCalendar(t, googleCalendar, calId);
    }

    private void syncTaskToGoogleTasks(GoogleCalConn googleCalendar,
            GoogleTaskConn googleTasks, String calId, String taskListId, Task t) throws IOException {
        if ((t.getGoogleID() != null) && (!t.getGoogleID().isEmpty())) {
            if (t.getGoogleID().startsWith(PREFIX_GOOGLEID_TASKS)) {
                updateTaskToGoogleTasks(t, googleTasks, taskListId);
            } else {
                changeGoogleCalendarToGoogleTasks(t, googleCalendar, googleTasks, 
                                                 calId, taskListId);
            }
        } else {
            createNewTaskToGoogleTasks(t, googleTasks, taskListId);
        }
    }
    
    private void updateTaskToGoogleTasks(Task t,
            GoogleTaskConn googleTasks, String taskListId) throws IOException {
        String id = t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH);
        com.google.api.services.tasks.model.Task gt = googleTasks.getTask(taskListId, id);
        gt = updateGoogleTaskWithTask(gt, t);
        googleTasks.updateTask(taskListId, id, gt);
    }

    private void createNewTaskToGoogleTasks(Task t,
            GoogleTaskConn googleTasks, String taskListId) throws IOException {
        com.google.api.services.tasks.model.Task gt = new com.google.api.services.tasks.model.Task();
        gt = updateGoogleTaskWithTask(gt, t);
        t.setGoogleID(PREFIX_GOOGLEID_TASKS + googleTasks.createTask(taskListId, gt).getId());
    }

    private void changeGoogleCalendarToGoogleTasks(Task t,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws IOException {
        googleCalendar.deleteEvent(calId, t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH));
        t.setGoogleID(null);
        createNewTaskToGoogleTasks(t, googleTasks, taskListId);
    }

    private String getCalendarIdByName(String calendarName, GoogleCalConn googleCalendar) throws IOException {
        List<CalendarListEntry> calendars = googleCalendar.getCalendars().getItems();
        String id = "";
        for (CalendarListEntry c : calendars) {
            if (c.getSummary().equals(calendarName)) {
                id = c.getId();
                break;
            }
        }
        return id;
    }
    
    private String getTaskListIdByName(String taskListName, GoogleTaskConn googleTasks) throws IOException {
        List<TaskList> taskLists = googleTasks.getTaskLists().getItems();
        String id = "";
        for (TaskList t : taskLists) {
            System.out.println(t.getTitle());
            if (t.getTitle().equals(taskListName)) {
                id = t.getId();
                break;
            }
        }
        return id;
    }
    
    private Task updateTaskWithGoogleEvent(Task t, Event e) {
        t.setName(e.getSummary());
        t.setDescription(e.getDescription());
        t.setGoogleID(PREFIX_GOOGLEID_CALENDAR + e.getId());
        if (e.getStart().getDateTime() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(e.getStart().getDateTime().getValue());
            t.setStartDate(cal);
            t.setHasDueTime(true);
        } else if (e.getStart().getDate() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(e.getStart().getDate().getValue());
            t.setStartDate(cal);
            t.setHasDueTime(false);
        }
        if (e.getEnd().getDateTime() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(e.getEnd().getDateTime().getValue());
            t.setDueDate(cal);
            t.setHasDueTime(true);
        } else if (e.getEnd().getDate() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(e.getEnd().getDate().getValue());
            t.setDueDate(cal);
            t.setHasDueTime(false);
        }
        return t;
    }
    

    private void updateTaskWithGoogleTask(Task t,
            com.google.api.services.tasks.model.Task gt) {
        t.setName(gt.getTitle());
        t.setDescription(gt.getNotes());
        t.setGoogleID(PREFIX_GOOGLEID_TASKS + gt.getId());
        if (gt.getDue() != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(gt.getDue().getValue());
            t.setDueDate(cal);
            if (gt.getDue().isDateOnly()) {
                t.setHasDueTime(false);
            } else {
                t.setHasDueTime(true);
            }
        }
        if (gt.getCompleted() == null) {
            t.setIsCompleted(false);
        }
        else {
            t.setIsCompleted(true);
        }
    }
    
    private Event updateGoogleEventWithTask(Event e, Task t) {
        e.setSummary(t.getName());
        e.setDescription(t.getDescription());
        if (t.getHasStartTime()) {
            DateTime dt = new DateTime(false, t.getStartDate().getTimeInMillis(),  0);
            e.setStart(new EventDateTime().setDateTime(dt));
        } else {
            DateTime dt = new DateTime(true, t.getStartDate().getTimeInMillis(),  0);
            e.setStart(new EventDateTime().setDate(dt));
        }
        if (t.getHasDueTime()) {
            DateTime dt = new DateTime(false, t.getDueDate().getTimeInMillis(),  0);
            e.setEnd(new EventDateTime().setDateTime(dt));
        } else {
            DateTime dt = new DateTime(true, t.getDueDate().getTimeInMillis(),  0);
            e.setEnd(new EventDateTime().setDate(dt));
        }
        return e;
    }
    
    private com.google.api.services.tasks.model.Task updateGoogleTaskWithTask(
            com.google.api.services.tasks.model.Task gt, Task t) {
        gt.setTitle(t.getName());
        gt.setNotes(t.getDescription());
        if (t.getDueDate() != null) {
            DateTime dt = new DateTime(false, t.getDueDate().getTimeInMillis(),  0);
            gt.setDue(dt);
        }
        if ((t.getIsCompleted()) && (gt.getCompleted() == null)) {
            DateTime dt = new DateTime(new Date());
            gt.setCompleted(dt);
        } else if (!t.getIsCompleted()) {
            gt.setCompleted(null);
        }
        return gt;
    }
    
    private boolean isCalendarEvent(Task t) {
        return ((t.getStartDate() != null) && (t.getDueDate() != null));
    }
    
}
