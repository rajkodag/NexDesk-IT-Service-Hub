package com.nexdesk;
import com.nexdesk.db.DatabaseManager;import com.nexdesk.web.ApiServer;
public class NexDeskApplication{
 public static void main(String[]args)throws Exception{DatabaseManager.testConnection();ApiServer s=new ApiServer(8080);s.start();System.out.println("NexDesk running at http://localhost:8080");}
}
