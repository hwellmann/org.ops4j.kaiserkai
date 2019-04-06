FROM openjdk:8u201-jre-alpine
LABEL maintainer="Harald Wellmann <harald.wellmann@gmx.de>"
WORKDIR /work

# deploy the WAR from the Maven build
ADD maven/jar/ /work/

EXPOSE 8080
VOLUME /root/kaiserkai

CMD [ "java", "-Dquarkus.http.host=0.0.0.0", "-jar", "runner.jar" ]


