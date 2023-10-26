FROM openjdk:19

WORKDIR /app

RUN echo "security.provider.13=org.bouncycastle.jce.provider.BouncyCastleProvider" >> /usr/java/openjdk-19/conf/security/java.security

COPY lib-bft-smart /app/lib-bft-smart

COPY target/hyflexchain-jar-with-dependencies.jar /app/hyflexchain.jar

# ENTRYPOINT ["java", "-jar", "hyflexchain.jar"]