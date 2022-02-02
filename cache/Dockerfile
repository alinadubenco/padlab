FROM adoptopenjdk/openjdk11:jre-11.0.9.1_1-alpine@sha256:b6ab039066382d39cfc843914ef1fc624aa60e2a16ede433509ccadd6d995b1f
RUN mkdir /app
COPY ./target/cache-0.0.1-SNAPSHOT.jar /app/cache-0.0.1-SNAPSHOT.jar
WORKDIR /usr/src/project
CMD "java" "-cp" "/app/cache-0.0.1-SNAPSHOT.jar" "-Dserver.port=8080" "edu.dubenco.alina.cache.CacheApplication"