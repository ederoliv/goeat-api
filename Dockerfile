# Etapa 1: Construção do aplicativo usando Maven
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder

# Copie o código fonte para o contêiner
COPY . /app
WORKDIR /app

# Compile e construa o JAR
RUN mvn clean verify -DskipTests

# Etapa 2: Criação da imagem final com a JAR construída
FROM openjdk:17-alpine

# Copie o arquivo JAR do estágio anterior
COPY --from=builder /app/target/*.jar goeat-api.jar

# Exponha a porta que o aplicativo vai rodar
EXPOSE 8080

# Comando para rodar o JAR
CMD ["java", "-jar", "/goeat-api.jar"]
