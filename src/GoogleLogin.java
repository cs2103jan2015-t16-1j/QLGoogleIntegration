import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.tasks.TasksScopes;


public class GoogleLogin {
    
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    private static final String CLIENT_ID = "657661987639-mula0sgv7qbs35pe24eq7vg2jj1fqvl8.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "Lk2nEQ38cw0of3vSLIjeY6VT";

    public static Credential getCredential(String user) throws Exception {
        
        
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        //File f = new File("googlecred");
        
        Collection<String> scopes = new HashSet<String>();
        scopes.add(CalendarScopes.CALENDAR);
        scopes.add(TasksScopes.TASKS);
        
        DataStoreFactory dataStoreFactory = new MemoryDataStoreFactory(); //new FileDataStoreFactory(f);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, CLIENT_ID, CLIENT_SECRET,
                scopes).setDataStoreFactory(dataStoreFactory)
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize(user);
    }

}
