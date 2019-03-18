FROM openjdk:11.0.2-jdk-stretch
LABEL maintainer="Harald Wellmann <harald.wellmann@gmx.de>"

EXPOSE 8080
VOLUME /opt/jboss/wildfly/kaiserkai


ADD maven/war/kaiserkai-hollow-thorntail.jar hollow.jar
ADD maven/war/kaiserkai-web.war kaiserkai.war

ENTRYPOINT [ "java",  "-Djava.security.egd=file:/dev/./urandom", "-Djava.net.preferIPv4Stack=true", "-jar", "hollow.jar", "kaiserkai.war" ]



