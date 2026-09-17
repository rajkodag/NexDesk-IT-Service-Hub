package com.nexdesk.model;
import java.sql.Timestamp;
public class Ticket {
 private int ticketId,userId; private Integer technicianId; private String title,description,category,priority,status; private Timestamp createdAt,updatedAt;
 public int getTicketId(){return ticketId;} public void setTicketId(int v){ticketId=v;}
 public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
 public Integer getTechnicianId(){return technicianId;} public void setTechnicianId(Integer v){technicianId=v;}
 public String getTitle(){return title;} public void setTitle(String v){title=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public String getCategory(){return category;} public void setCategory(String v){category=v;}
 public String getPriority(){return priority;} public void setPriority(String v){priority=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
 public Timestamp getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Timestamp v){updatedAt=v;}
}
