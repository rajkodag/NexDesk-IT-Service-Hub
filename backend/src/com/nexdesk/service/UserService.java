package com.nexdesk.service;
import com.nexdesk.db.DatabaseManager;
import com.nexdesk.model.User;
import java.sql.*;
public class UserService {
 public User login(String email,String password){
  if(email==null||email.isBlank()||password==null||password.isBlank())return null;
  String sql="SELECT USER_ID,FULL_NAME,EMAIL,USER_PASSWORD,USER_ROLE FROM NX_USERS WHERE LOWER(EMAIL)=LOWER(?) AND USER_PASSWORD=?";
  try(Connection c=DatabaseManager.getConnection();PreparedStatement p=c.prepareStatement(sql)){
   p.setString(1,email.trim());p.setString(2,password);
   try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
  }catch(SQLException e){throw new RuntimeException("Login failed: "+e.getMessage(),e);}
 }
 public User signup(String name,String email,String password){
  if(name==null||name.isBlank())throw new IllegalArgumentException("Name is required.");
  if(email==null||email.isBlank())throw new IllegalArgumentException("Email is required.");
  if(password==null||password.length()<4)throw new IllegalArgumentException("Password must contain at least 4 characters.");
  try(Connection c=DatabaseManager.getConnection()){
   try(PreparedStatement q=c.prepareStatement("SELECT USER_ID FROM NX_USERS WHERE LOWER(EMAIL)=LOWER(?)")){
    q.setString(1,email.trim());try(ResultSet r=q.executeQuery()){if(r.next())throw new IllegalArgumentException("An account with this email already exists.");}
   }
   try(PreparedStatement p=c.prepareStatement("INSERT INTO NX_USERS(USER_ID,FULL_NAME,EMAIL,USER_PASSWORD,USER_ROLE) VALUES(NX_USER_SEQ.NEXTVAL,?,?,?,'USER')")){
    p.setString(1,name.trim());p.setString(2,email.trim().toLowerCase());p.setString(3,password);p.executeUpdate();
   }
   try(PreparedStatement q=c.prepareStatement("SELECT USER_ID,FULL_NAME,EMAIL,USER_PASSWORD,USER_ROLE FROM NX_USERS WHERE LOWER(EMAIL)=LOWER(?)")){
    q.setString(1,email.trim());try(ResultSet r=q.executeQuery()){if(r.next())return map(r);}
   }
   throw new RuntimeException("Account created but could not be loaded.");
  }catch(SQLException e){throw new RuntimeException("Signup failed: "+e.getMessage(),e);}
 }
 private User map(ResultSet r)throws SQLException{return new User(r.getInt("USER_ID"),r.getString("FULL_NAME"),r.getString("EMAIL"),r.getString("USER_PASSWORD"),r.getString("USER_ROLE"));}
}
