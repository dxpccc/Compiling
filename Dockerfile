FROM openjdk:8-alpine
COPY ./* /app/
WORKDIR /app/
RUN javac ./*.java