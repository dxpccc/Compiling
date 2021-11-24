FROM openjdk:8
COPY ./* /app/
WORKDIR /app/
RUN mkdir output
RUN javac -d ./output ./*.java
WORKDIR /app/output