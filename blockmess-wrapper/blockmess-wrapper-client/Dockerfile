FROM openjdk:19

WORKDIR /app

RUN echo "security.provider.13=org.bouncycastle.jce.provider.BouncyCastleProvider" >> /usr/java/openjdk-19/conf/security/java.security

COPY target/Blockmess-Wrapper-Client-1.0-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

ENTRYPOINT ["java", "-cp", "app.jar"]
