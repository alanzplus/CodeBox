#!/bin/bash
mvn package && clear && java -jar target/commons-1.0.0-SNAPSHOT-jar-with-dependencies.jar
