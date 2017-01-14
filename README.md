# vxapp

[![Build Status](https://travis-ci.org/kuffel/vxapp.svg?branch=master)](https://travis-ci.org/kuffel/vxapp)

Vert.x based applicaton template.

## Getting started

This project uses gradle, so you can open it in your favorite IDE (IntelliJ, Eclipse or Netbeans).

    gradle tasks => List all available tasks    
    gradle run => Runs this project as a JVM application
    gradle runShadow => Runs this project as a JVM application using the shadow jar
    gradle shadowJar => Create a combined JAR of project and runtime depend
    gradle build => Assembles and tests this project.
    gradle javadoc => Generates Javadoc API documentation for the main source code.
    gradle idea => Generates IDEA project files (IML, IPR, IWS)
    gradle check => Runs all checks.
    gradle test => Runs the unit tests.
    
### Requirements

For convinience you may use docker containers to create your development and your production enviroment. You will need the following containers:

##### Mongo DB

To start this application you need an mongodb instance. The default settings use a local mongo db. To start an instance using docker you can use the following commands.

	$ docker pull mongo:3.4
	$ docker run -d --name mongo_db --restart="always" -p 27017:27017 -v /dockervolumes/mongo_db:/data/db mongo:3.4
	$ docker stop mongo_db && docker rm mongo_db   
    
An useful tool to view data in an mongo db instance is mongo express:

	$ docker pull mongo-express:0.32
	$ docker run -d --name mongo_express --restart="always" -p 8081:8081 --link mongo_db:mongo_db -e ME_CONFIG_MONGODB_SERVER="mongo_db" mongo-express:0.32
	$ docker stop mongo_express && docker rm mongo_express

##### Redis

	$ docker pull redis:3.2
	$ docker run -d --restart="always" --name redisdemo -p 6379:6379 redis:3.2
	$ docker stop redisdemo && docker rm redisdemo


##### Elasticsearch

	$ docker pull elasticsearch:5.1
	$ docker run -d --restart="always" --name search -p 9200:9200 -p 9300:9300 -e "http.host=0.0.0.0" elasticsearch:5.1 -Enode.name=mynode -Ecluster.name=myclustername
	$ docker stop search && docker rm search
	
##### Rabbit MQ	

	$ docker pull rabbitmq:3.6.6-management
	$ docker run -d --restart="always" --name myrabbit --hostname my-rabbit -p 8080:15672 -p 5672:5672 -e RABBITMQ_DEFAULT_USER=myadmin -e RABBITMQ_DEFAULT_PASS=password rabbitmq:3.6.6-management
	$ docker stop myrabbit && docker rm myrabbit


## Project structure