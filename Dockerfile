FROM openjdk:8-alpine
COPY ./src/* /app/
WORKDIR /app/
RUN javac -d ./output ./*.java
WORKDIR /app/output