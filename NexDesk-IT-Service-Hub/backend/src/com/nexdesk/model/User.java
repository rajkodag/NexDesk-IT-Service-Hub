package com.nexdesk.model;
public class User {
 private int userId; private String fullName,email,password,role;
 public User(){}
 public User(int id,String name,String email,String password,String role){this.userId=id;this.fullName=name;this.email=email;this.password=password;this.role=role;}
 public int getUserId(){return userId;} public void setUserId(int v){userId=v;}
 public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
 public String getEmail(){return email;} public void setEmail(String v){email=v;}
 public String getPassword(){return password;} public void setPassword(String v){password=v;}
 public String getRole(){return role;} public void setRole(String v){role=v;}
}
