FROM ubuntu:24.04
RUN apt-get update
RUN apt-get install -y libimage-exiftool-perl
RUN apt-get install -y openjdk-25-jdk
WORKDIR /app
COPY target/classes/ /app
RUN mkdir /app/config
RUN mkdir /app/filedir
CMD java nl.schoepping.Main --config config/config.yml --timeline config/start.yml --directory filedir --log config/log4j.properties