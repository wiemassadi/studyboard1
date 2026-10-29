# Base image: Tomcat 10.1 with Java 17 (Tomcat 10 uses the "jakarta" API)
FROM tomcat:10.1-jdk17

# Remove Tomcat's sample application so ours is served at the root (/)
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy the WAR built by Maven; ROOT.war = the default application
COPY target/studyboard.war /usr/local/tomcat/webapps/ROOT.war

# Documentation: the application listens on port 8080
EXPOSE 8080
