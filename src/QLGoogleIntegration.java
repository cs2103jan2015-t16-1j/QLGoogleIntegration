import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.tasks.model.TaskList;

public class QLGoogleIntegration {
    
    private static final String PREFIX_GOOGLEID_TASKS = "t";
    private static final String PREFIX_GOOGLEID_CALENDAR = "c";
    
    private static final int PREFIX_GOOGLEID_LENGTH = 1;
    private String _userId;
    private Credential _cred;
    GoogleCalConn _googleCalendar;
    GoogleTaskConn _googleTasks;
    
    
    public QLGoogleIntegration(String userId) throws Exception {
        _userId = userId;
        _cred = GoogleLogin.getCredential(_userId);
        _googleCalendar = new GoogleCalConn(_cred);
        _googleTasks = new GoogleTaskConn(_cred);
    }
    
    public List<Task> syncFrom(String listName, List<Task> taskList) throws Exception {
        //try {
            
            String calId = getCalendarIdByName(listName, _googleCalendar);
            String taskListId = getTaskListIdByName(listName, _googleTasks);
            
            syncGoogleToTaskList(taskList, _googleCalendar, _googleTasks, calId, taskListId);
            
            return taskList;
        //} catch (Exception e) {
        //    return null;
        //}
    }

    private void syncGoogleToTaskList(List<Task> taskList,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws Exception {
        
        Map<String, Task> calendarTask = new HashMap<String, Task>();
        Map<String, Task> tasksTask = new HashMap<String, Task>();
        
        createMapsForSync(taskList, calendarTask, tasksTask);
        
        syncGoogleCalendarToTaskList(taskList, googleCalendar, calId,
                calendarTask);
        
        syncGoogleTasksToTaskList(taskList, googleTasks, taskListId, tasksTask);
    }

    private void syncGoogleTasksToTaskList(List<Task> taskList,
            GoogleTaskConn googleTasks, String taskListId,
            Map<String, Task> tasksTask) throws Exception {
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
            Map<String, Task> calendarTask) throws Exception {
        if (calId.isEmpty())
            return;
        List<Event> events = googleCalendar.getEvents(calId).getItems();
        for (Event e : events) {
            Task matchingTask = calendarTask.remove(PREFIX_GOOGLEID_CALENDAR + e.getId());
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
    
    public void syncTo(String listName, List<Task> taskList) throws Exception {
        //try {
            String calId = getCalendarIdByName(listName, _googleCalendar);
            String taskListId = getTaskListIdByName(listName, _googleTasks);
            
            calId = createNewCalendarIfNotExist(listName, _googleCalendar, calId);
            
            taskListId = createNewTaskListIfNotExist(listName, _googleTasks,
                    taskListId);
            
            syncTaskListToGoogle(taskList, _googleCalendar, _googleTasks, calId,
                    taskListId);
            
        //} catch (Exception e) {
            //throw e;
        //    throw new Error(e.getMessage());
        //}
    }

    private String createNewTaskListIfNotExist(String listName,
            GoogleTaskConn googleTasks, String taskListId) throws Exception {
        if (taskListId.equals("")) {
            taskListId = googleTasks.createTaskList(new TaskList().setTitle(listName)).getId();
        }
        return taskListId;
    }

    private String createNewCalendarIfNotExist(String listName,
            GoogleCalConn googleCalendar, String calId) throws Exception {
        if (calId.equals("")) {
            calId = googleCalendar.createCalendar(new Calendar().setSummary(listName)).getId();
        }
        return calId;
    }

    private void syncTaskListToGoogle(List<Task> taskList,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws Exception {
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
            GoogleTaskConn googleTasks, String calId, String taskListId, Task t) throws Exception {
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
            GoogleCalConn googleCalendar, String calId) throws Exception {
        String id = t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH);
        Event e = googleCalendar.getEvent(calId, id);
        e = updateGoogleEventWithTask(e, t);
        googleCalendar.updateEvent(calId, id, e);
    }
    
    private void createNewEventToGoogleCalendar(Task t,
            GoogleCalConn googleCalendar, String calId) throws Exception {
        Event e = new Event();
        e = updateGoogleEventWithTask(e, t);
        t.setGoogleID(PREFIX_GOOGLEID_CALENDAR + googleCalendar.createEvent(calId, e).getId());
    }

    private void changeGoogleTaskToGoogleCalendar(Task t,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws Exception {
        googleTasks.deleteTask(taskListId, t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH));
        t.setGoogleID(null);
        createNewEventToGoogleCalendar(t, googleCalendar, calId);
    }

    private void syncTaskToGoogleTasks(GoogleCalConn googleCalendar,
            GoogleTaskConn googleTasks, String calId, String taskListId, Task t) throws Exception {
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
            GoogleTaskConn googleTasks, String taskListId) throws Exception {
        String id = t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH);
        com.google.api.services.tasks.model.Task gt = googleTasks.getTask(taskListId, id);
        gt = updateGoogleTaskWithTask(gt, t);
        googleTasks.updateTask(taskListId, id, gt);
    }

    private void createNewTaskToGoogleTasks(Task t,
            GoogleTaskConn googleTasks, String taskListId) throws Exception {
        com.google.api.services.tasks.model.Task gt = new com.google.api.services.tasks.model.Task();
        gt = updateGoogleTaskWithTask(gt, t);
        t.setGoogleID(PREFIX_GOOGLEID_TASKS + googleTasks.createTask(taskListId, gt).getId());
    }

    private void changeGoogleCalendarToGoogleTasks(Task t,
            GoogleCalConn googleCalendar, GoogleTaskConn googleTasks,
            String calId, String taskListId) throws Exception {
        googleCalendar.deleteEvent(calId, t.getGoogleID().substring(PREFIX_GOOGLEID_LENGTH));
        t.setGoogleID(null);
        createNewTaskToGoogleTasks(t, googleTasks, taskListId);
    }

    private String getCalendarIdByName(String calendarName, GoogleCalConn googleCalendar) throws Exception {
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
    
    private String getTaskListIdByName(String taskListName, GoogleTaskConn googleTasks) throws Exception {
        List<TaskList> taskLists = googleTasks.getTaskLists().getItems();
        String id = "";
        for (TaskList t : taskLists) {
            if (t.getTitle().equals(taskListName)) {
                id = t.getId();
                break;
            }
        }
        return id;
    }
    
    private Task updateTaskWithGoogleEvent(Task t, Event e) {
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("ddMMyyyy HH:mm");
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("ddMMyyyy");
        t.setName(e.getSummary());
        t.setDescription(e.getDescription());
        t.setGoogleID(PREFIX_GOOGLEID_CALENDAR + e.getId());
        if (e.getStart().getDate() != null) {
            t.setStartDate(sdfDateOnly.format(new Date(e.getStart().getDate().getValue())));
        } else if (e.getStart().getDateTime() != null) {
            t.setStartDate(sdfDateTime.format(new Date(e.getStart().getDateTime().getValue())));
        }
        if (e.getEnd().getDate() != null) {
            t.setDueDate(sdfDateOnly.format(new Date(e.getEnd().getDate().getValue())));
        } else if (e.getEnd().getDateTime() != null) {
            t.setDueDate(sdfDateTime.format(new Date(e.getEnd().getDateTime().getValue())));
        }

        return t;
    }
    

    private void updateTaskWithGoogleTask(Task t,
            com.google.api.services.tasks.model.Task gt) {
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("ddMMyyyy HH:mm");
        SimpleDateFormat sdfDateOnly = new SimpleDateFormat("ddMMyyyy");
        t.setName(gt.getTitle());
        t.setDescription(gt.getNotes());
        t.setGoogleID(PREFIX_GOOGLEID_TASKS + gt.getId());
        if (gt.getDue() != null) {
            if (gt.getDue().isDateOnly()) {
                t.setDueDate(sdfDateOnly.format(new Date(gt.getDue().getValue())));
            } else {
                t.setDueDate(sdfDateTime.format(new Date(gt.getDue().getValue())));
            }
        }
    }
    
    private Event updateGoogleEventWithTask(Event e, Task t) {
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
    
    private com.google.api.services.tasks.model.Task updateGoogleTaskWithTask(
            com.google.api.services.tasks.model.Task gt, Task t) {
        gt.setTitle(t.getName());
        gt.setNotes(t.getDescription());
        if (t.getDueDate() != null) {
            if (t.getHasDueTime()) {
                com.google.api.client.util.DateTime dt = new com.google.api.client.util.DateTime(false, t.getDueDate().getTimeInMillis(),  0);
                gt.setDue(dt);
            } else {
                com.google.api.client.util.DateTime dt = new com.google.api.client.util.DateTime(true, t.getDueDate().getTimeInMillis(),  0);
                gt.setDue(dt);
            }
        }
        return gt;
    }
    
    private boolean isCalendarEvent(Task t) {
        return ((t.getStartDate() != null) && (t.getDueDate() != null));
    }
    
}
