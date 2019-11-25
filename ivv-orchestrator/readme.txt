mvn assembly:assembly -Dmaven.test.skip=true  -DdescriptorId=jar-with-dependencies
-Dfile.encoding=UTF-8 -Dspring.profiles.active=qa
connect 'jdbc:derby:reg;bootPassword=mosip12345';

update reg.global_param set val='eng' where name='mosip.primary-language';

# for id auth utility #
mvn clean package spring-boot:repackage -Dmaven.test.skip=true
java -jar dependency_jars/authentication-partnerdemo-service-0.9.1.jar