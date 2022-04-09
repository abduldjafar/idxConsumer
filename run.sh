mvn clean
mvn package
java -jar target/idxConsumer-1.0-SNAPSHOT-jar-with-dependencies.jar kafka.config test_openapi_push


