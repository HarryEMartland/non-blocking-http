FROM java:8-alpine
ENV JAVA_OPTS="-Dserver.tomcat.max-threads=2000 -XX:+UseG1GC -Xmx1g"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -server -jar /app.jar" ]

ADD target/non-blocking-http-*.jar app.jar