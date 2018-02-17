#!/bin/sh

/opt/jboss/wildfly/bin/jboss-cli.sh -c \
  --commands='/subsystem=undertow/server=default-server/http-listener=default:write-attribute(name=max-post-size, value=99000000), :reload'
