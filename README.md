# 데이터 동기화를 위한 Apache Kafka의 활용
SpringBoot 3.2 | Kafka 3.6 | MariaDB는 11.2.2 | confluent 7.5.2 | confluentinc-kafka-connect-jdbc 10.7.4
## Kafka 서버 기동
### KafKa 설치
1. Kafka 설치 (https://kafka.apache.org/)
2. bin\windows\kafka-run-class.bat 코드 변경
```bat
rem Classpath addition for release
for %%i in ("%BASE_DIR%\libs\*") do (
	call :concat "%%i"
)

rem Classpath addition for core
for %%i in ("%BASE_DIR%\core\build\libs\kafka_%SCALA_BINARY_VERSION%*.jar") do (
	call :concat "%%i"
)
```
ㅤ위 코드를 아래로 변경
```
rem Classpath addition for release
for %%i in ("%BASE_DIR%\libs\*") do (
	call :concat "%%i"
)

rem Classpath addition for LSB style path
if exist %BASE_DIR%\share\java\kafka\* (
	call:concat %BASE_DIR%\share\java\kafka\*
)

rem Classpath addition for core
for %%i in ("%BASE_DIR%\core\build\libs\kafka_%SCALA_BINARY_VERSION%*.jar") do (
	call :concat "%%i"
)
```
3. bin\windows\connect-distributed.bat 코드 변경
```bat
rem Log4j settings
IF ["%KAFKA_LOG4J_OPTS%"] EQU [""] (
	set KAFKA_LOG4J_OPTS=-Dlog4j.configuration=file:%BASE_DIR%/config/connect-log4j.properties
)
```
```bat
rem Log4j settings
IF ["%KAFKA_LOG4J_OPTS%"] EQU [""] (
	set KAFKA_LOG4J_OPTS=-Dlog4j.configuration=file:%BASE_DIR%/etc/kafka/connect-log4j.properties
)
```
4. Zookeper와 KafKa 서버 기동
```
.\bin\windows\zookeeper-server-start.bat config\zookeeper.properties
.\bin\windows\kafka-server-start.bat config\server.properties
```
### Kafka Topic
topic 생성
```
.\bin\windows\kafka-topics --create --topic quickstart-events --bootstrap-server localhost:9092 --partitions 1
```

topic 목록 확인
```
.\bin\windows\kafka-topics --bootstrap-server localhost:9092 --list
```

topic 정보 확인
```
.\bin\windows\kafka-topics --describe --topic quickstart-events --bootstrap-server localhost:9092
```

메시지 생산
```
.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic quickstart-events
```

메시지 소비
```
.\bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic orders --from-beginning
```

## 데이터베이스 연동
### MariaDB 설정
1. MariaDB Zip file 설치 (https://mariadb.org)
2. Database 초기화
```
.\bin\mariadb-install-db.exe --datadir=mariadb폴더\data --service=mariaDB --port=3306 --password=test1357
```
3. Windows Service에서 mariaDB 설치 확인

### Kafka Connect 설치
1. Kafka Connect 설치 (http://packages.confluent.io/archive/7.5/confluent-community-7.5.2.tar.gz)
2. 압축 해제
```
tar xvf confluent-community-7.5.2.tar.gz
```
3. Kafka Connect 실행
```
.\bin\windows\connect-distributed .\etc\kafka\connect-distributed.properties
```
### JDBC Connector 설정
1. JDBC Connector 설치 (https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc)
   
2. .\etc\kafka\connect-distributed.properties 파일 마지막 아래 plugin 정보 추가
```yml
# plugin.path=/usr/share/java
plugin.path={kafka-connect-jdbc 위치}\\lib
```
3. ${USER.HOME}\.m2 폴더에서 mariadb-java-client.jar 파일을 ./share/java/kafka/로 복사

### Kafka Source Connect 테스트
1. Kafka Source Connect 추가 
   
   POST http://localhost:8083/connectors | header "content-Type:application/json"
```
{
	"name" : "my-source-connect",
	"config" : {
		"connector.class" : "io.confluent.connect.jdbc.JdbcSourceConnector",
		"connection.url":"jdbc:mysql://localhost:3306/mydb",
		"connection.user":"root",
		"connection.password":"test1357",
		"mode": "incrementing",
		"incrementing.column.name" : "id",
		"table.whitelist":"mydb.users",
		"topic.prefix" : "my_topic_",
		"tasks.max" : "1"
	}
}
```
2. Kafka Connect 목록 확인
   
	GET http://localhost:8083/connectors
3. Kafka Connect 확인

	GET http://localhost:8083/connectors/my-source-connect/status

### Kafka Sink Connect 테스트
1. KafKa Sink Connect 추가

   POST http://localhost:8083/connectors | header "content-Type:application/json"
```
{
	"name":"my-sink-connect",
	"config":{
		"connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
		"connection.url":"jdbc:mysql://localhost:3306/mydb",
		"connection.user":"root",
		"connection.password":"test1357",
		"auto.create":"true",
		"auto.evolve":"true",
		"delete.enabled":"false",
		"tasks.max":"1",
		"topics":"my_topic_users"
	}
}
```
2. MariaDB에서 데이터 추가
3. KafKa Producer를 이용해서 kafka Topic에 데이터 직접 전송
```
.\bin\windows\kafka-console-producer.bat --broker-list localhost:9092 --topic my_topic_users
```
```
{"schema":{"type":"struct","fields":[{"type":"int32","optional":false,"field":"id"},{"type":"string","optional":true,"field":"user_id"},{"type":"string","optional":true,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"created_at"}],"optional":false,"name":"users"},"payload":{"id":9,"user_id":"my_id","pwd":"my_password","name":"Test user","created_at":1614230080000}}
```