FROM openjdk:8
COPY ./* /app/
WORKDIR /app/
RUN javac ./*.java