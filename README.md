# Java Version

java version "1.8.0_191" Java(TM) SE Runtime Environment (build 1.8.0_191-b12) Java HotSpot(TM) 64-Bit Server VM (build 25.191-b12, mixed mode)

Maven Version Apache Maven 3.6.0

AWS java SDK version

1.11.509

Change test configuration.

open configuration.xml file

change the values to your own configuration

Steps,

1.Start ElasticMQ using "java -jar elasticmq-server-0.13.11.jar"

2.CMD to the project folder,then type "mvn clean install -DskipTests"

3.rename run.bat.txt to run.bat double click on run.bat

test report is located at project SQSTest\target\surefire-reports\index.html
