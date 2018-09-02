FROM jboss/wildfly:14.0.0.Final
LABEL maintainer="Harald Wellmann <harald.wellmann@gmx.de>"

# deploy the WAR from the Maven build
ADD maven/kaiserkai-web.war /opt/jboss/wildfly/standalone/deployments/kaiserkai-web.war

VOLUME /opt/jboss/wildfly/kaiserkai


