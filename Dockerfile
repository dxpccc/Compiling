FROM openjdk:8
COPY ./* /app/
WORKDIR /app/
RUN javac ./src/*.java