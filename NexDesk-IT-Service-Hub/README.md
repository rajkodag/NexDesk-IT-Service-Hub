# NexDesk IT Service Hub

Java + Oracle + HTML/CSS/JavaScript IT service desk prototype.

## Requirements
- JDK 21+
- Oracle XE/XEPDB1
- ojdbc11.jar in backend/lib

## Run
1. Put ojdbc11.jar in backend/lib.
2. Create the database using database/nexdesk.sql.
3. Set DB_PASSWORD if needed.
4. Run backend/run.bat.
5. Open http://localhost:8080

Demo accounts created by SQL:
admin@nexdesk.com / admin123
demo@nexdesk.com / demo123

For public deployment, use secrets/environment variables and change demo credentials.
