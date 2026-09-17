package com.nexdesk.service;
import com.nexdesk.db.DatabaseManager;import java.sql.*;import java.util.*;
public class TechnicianService {
 public List<Map<String,Object>> all(){List<Map<String,Object>> out=new ArrayList<>();String sql="SELECT TECHNICIAN_ID,FULL_NAME,EMAIL,SPECIALIZATION,STATUS,CREATED_AT FROM NX_TECHNICIANS ORDER BY FULL_NAME";try(Connection c=DatabaseManager.getConnection();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){while(r.next()){Map<String,Object> m=new LinkedHashMap<>();m.put("technician_id",r.getInt("TECHNICIAN_ID"));m.put("full_name",r.getString("FULL_NAME"));m.put("email",r.getString("EMAIL"));m.put("specialization",r.getString("SPECIALIZATION"));m.put("status",r.getString("STATUS"));m.put("created_at",r.getTimestamp("CREATED_AT"));out.add(m);}return out;}catch(SQLException e){throw new RuntimeException("Technician query failed: "+e.getMessage(),e);}}
}
