FROM openjdk:8u151-jdk
LABEL maintainer="Harald Wellmann <harald.wellmann@gmx.de>"

EXPOSE 8080
VOLUME /opt/jboss/wildfly/kaiserkai


ADD maven/war/kaiserkai-hollow-swarm.jar hollowswarm.jar
ADD maven/war/kaiserkai-web.war kaiserkai.war

ENTRYPOINT [ "java",  "-Djava.security.egd=file:/dev/./urandom", "-Djava.net.preferIPv4Stack=true", "-jar", "hollowswarm.jar", "kaiserkai.war" ]



