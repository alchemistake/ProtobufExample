## This is taken
FROM maven:3.6-jdk-12-alpine

ARG PROJECT_PLACE=simpleRecordStore
ARG PROTOBUF_VERSION=3.6.1

WORKDIR /simpleRecordStore

RUN apk -U --no-cache add protobuf

COPY / ./
RUN mvn clean compile assembly:single

# Note the missing brackets and quotes, so that the command gets the default /bin/sh -c prefix
# So this workaround makes the graceful exit work
ENTRYPOINT exec java -jar ./target/SimpleRecordStore-1.0.0-demo-jar-with-dependencies.jar
