FROM openjdk:17
VOLUME /tmp
COPY target/springboot-reactjs-backend springboot-reactjs-backend
ENTRYPOINT ["java","-jar","/springboot-reactjs-backend","--spring.profiles.active=prod"]