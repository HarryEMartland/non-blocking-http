FROM java:8-alpine
ENV JAVA_OPTS="-Dserver.tomcat.max-threads=2000 "
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]

ADD target/non-blocking-http-*.jar app.jar