package RunlistAutomation.Version1;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.sforce.soap.partner.sobject.*;
import com.opencsv.*;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

public class App {
    static final String USERNAME = "gottrappedinthematrix@gmail.com";
    static final String PASSWORD = "Prince0105198@SiqkdRH13LRcRU3MgqKSTOwX";
    static PartnerConnection connection;
    
    public static void main(String[] args) {

        ConnectorConfig config = new ConnectorConfig();
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);

        try {

            connection = Connector.newConnection(config);

            // display some current settings
            System.out.println("Auth EndPoint: "+config.getAuthEndpoint());
            System.out.println("Service EndPoint: "+config.getServiceEndpoint());
            System.out.println("Username: "+config.getUsername());
            System.out.println("SessionId: "+config.getSessionId());
            
            //read records
            QueryResult stepQueryResult = connection.query("SELECT Id, ObjectName__c FROM RunlistStep__c WHERE ParentRunlist__r.IsCurrent__c = TRUE");
            SObject[] steps = stepQueryResult.getRecords();
            QueryResult attachmentQueryResult = connection.query("SELECT Body FROM Attachment WHERE ParentId = '" + steps[0].getId() + "'");
        	SObject[] attachments = attachmentQueryResult.getRecords();
            String home = System.getProperty("user.home");
        	System.out.println(home);
        	try {
	            
        		byte[] decodedString = Base64.getDecoder().decode(((String) attachments[0].getField("Body")).getBytes(StandardCharsets.UTF_8));
        		CSVReader csvReader = new CSVReader(new InputStreamReader(new ByteArrayInputStream(decodedString)));
	            List<String[]> allData = csvReader.readAll(); 
	            
	            // load Data 
	            SObject czc = new SObject((String) steps[0].getField("ObjectName__c"));
	            ArrayList<SObject> czcList = new ArrayList<SObject>();
	            
	            Integer i = 0;
	            Integer fieldCount = 0;
	            ArrayList<String> fieldNames = new ArrayList<String>();// = new List<String>();
	            for (String[] row : allData) { 
	            	czc = new SObject("CityZipCodes__c");
	            	if(i == 0) {
		                for (String cell : row) { 
		                    fieldNames.add(cell.toString());
		                } 
	            	} else {
	            		fieldCount = 0;
	            		for (String cell : row) { 
	            			czc.setSObjectField(fieldNames.get(fieldCount), cell.toString());
	            			fieldCount++;
	            		}
		            	czcList.add(czc);
	            	} 
	            	i++;
	            } 
	            //insert records
	            SaveResult[] saveres = new SaveResult[] {};
	            saveres = connection.create((SObject [])czcList.toArray(new SObject[0]));
	            System.out.println("Save Result: " + saveres[1].isSuccess());
        	}catch(Exception e) {
        		System.out.println("failed: " + e.getMessage() + " line# " + e.getStackTrace().toString());
        	}
        } catch (ConnectionException e1) {
            e1.printStackTrace();
        } 
    }
    
}