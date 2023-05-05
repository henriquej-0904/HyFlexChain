FROM openjdk:19

WORKDIR /app

RUN echo "security.provider.13=org.bouncycastle.jce.provider.BouncyCastleProvider" >> /usr/java/openjdk-19/conf/security/java.security

COPY target/Blockmess-Simple-Crypto-Node-1.0-jar-with-dependencies.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]