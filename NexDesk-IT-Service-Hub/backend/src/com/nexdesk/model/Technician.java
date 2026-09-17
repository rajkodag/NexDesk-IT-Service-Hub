package com.nexdesk.model;
import java.sql.Timestamp;
public class Technician {
 private int technicianId; private String fullName,email,specialization,status; private Timestamp createdAt;
 public int getTechnicianId(){return technicianId;} public void setTechnicianId(int v){technicianId=v;}
 public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getSpecialization(){return specialization;} public void setSpecialization(String v){specialization=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public Timestamp getCreatedAt(){return createdAt;} public void setCreatedAt(Timestamp v){createdAt=v;}
}
