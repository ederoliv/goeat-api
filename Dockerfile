# Etapa 1: Construção do aplicativo usando Maven
FROM maven:3.9.8-eclipse-temurin-17-alpine AS builder

# Definir variáveis de ambiente para encoding
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8 -Dproject.build.sourceEncoding=UTF-8 -Dproject.reporting.outputEncoding=UTF-8"
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Copie o código fonte para o contêiner
COPY . /app
WORKDIR /app

# Compile e construa o JAR com configurações explícitas de encoding
RUN mvn clean verify -DskipTests \
    -Dfile.encoding=UTF-8 \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8 \
    -Dmaven.compiler.encoding=UTF-8

# Etapa 2: Criação da imagem final com a JAR construída
FROM openjdk:17-alpine

ENV TZ=America/Sao_Paulo

RUN apk add --no-cache tzdata

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Copie o arquivo JAR do estágio anterior
COPY --from=builder /app/target/*.jar goeat-api.jar

# Exponha a porta que o aplicativo vai rodar
EXPOSE 8080

# Comando para rodar o JAR
CMD ["java", "-jar", "/goeat-api.jar"]