>output.txt (
  java -jar dependency_jars/authentication-partnerdemo-service-0.9.1.jar
  timeout /t 10 && mvn clean test^
  && pause
)
