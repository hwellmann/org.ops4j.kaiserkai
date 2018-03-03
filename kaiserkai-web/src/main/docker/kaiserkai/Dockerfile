FROM jboss/wildfly:12.0.0.Final
LABEL maintainer="Harald Wellmann <harald.wellmann@gmx.de>"

# deploy the WAR from the Maven build
ADD maven/war/kaiserkai-web.war /opt/jboss/wildfly/standalone/deployments/kaiserkai-web.war

VOLUME /opt/jboss/wildfly/kaiserkai


