FROM java:8-alpine
RUN apk add --update curl && \
    rm -rf /var/cache/apk/*
ENV JAVA_OPTS="-Dserver.tomcat.max-threads=2000 -XX:+UseG1GC -Xmx1g"
HEALTHCHECK --interval=10s --timeout=3s --retries=3 CMD curl -f http://localhost:8080/health || exit 1
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -server -jar /app.jar" ]
ADD target/non-blocking-http-*.jar app.jar