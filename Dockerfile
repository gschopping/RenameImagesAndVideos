FROM openjdk:25-ea-jdk
WORKDIR /app
COPY target/classes/ /app
RUN mkdir /app/config
RUN mkdir /app/files
CMD java nl.schoepping.renameimages.RenameImages