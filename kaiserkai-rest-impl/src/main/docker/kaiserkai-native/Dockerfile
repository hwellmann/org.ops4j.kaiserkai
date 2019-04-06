FROM registry.fedoraproject.org/fedora-minimal
LABEL maintainer="Harald Wellmann <harald.wellmann@gmx.de>"
WORKDIR /work/

ADD maven/native/runner /work/application

EXPOSE 8080
VOLUME /root/kaiserkai

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]


