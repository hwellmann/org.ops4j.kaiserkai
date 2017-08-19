FROM piegsaj/wildfly:10.1.0.Final
LABEL maintainer="Harald Wellmann <harald.wellmann@gmx.de>"

# deploy the WAR from the Maven build
ADD maven/war/kaiserkai-web.war /opt/wildfly/standalone/deployments/kaiserkai-web.war

VOLUME /opt/wildfly/kaiserkai


