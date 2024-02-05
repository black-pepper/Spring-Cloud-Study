# 애플리케이션 배포 - Docker Container
## Docker Command
로그 확인
```
docker logs {container name}
```
사용하지 않는 컨테이너, 사용 불가 이미지 삭제
```
docker system prune
```

## Create Bridge Network

네트워크 생성
```
docker network create --gateway 172.18.0.1 --subnet 172.18.0.0/16 ecommerce-network
```
네트워크 조회
```
docker network ls
```
네트워크 상세 조회
``` 
docker network inspect ecommerce-network
```

## RabbitMQ
RabbitMQ 실행
```
docker run -d --name rabbitmq --network ecommerce-network -p 15672:15672 -p 5672:5672 -p 15671:15671 -p 5671:5671 -p 4369:4369 -e RABBITMQ_DEFAULT_USER=guest -e RABBITMQ_DEFAULT_PASS=guest rabbitmq:management
```

## Config Server
1. gradle 버전 수정
```
//version = '0.0.1-SNAPSHOT'
version = '1.0'
```
2. gradle clean, gradle assemble
3. Dockerfile 생성
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/config-service-1.0.jar ConfigServer.jar
ENTRYPOINT ["java","-jar","ConfigServer.jar"]
```
4. docker image 생성
```
docker build -t peppercode01/config-service:1.0 .
```
5. docker image 실행
```
run -d -p 8888:8888 --network ecommerce-network -e "spring.rabbitmq.host=rabbitmq" -e "spring.profiles.active=default" --name config-service peppercode01/config-service:1.0
```

## Discovery Service
1. gradle 버전 수정 (1.0)
2. gradle clean, gradle assemble
3. Dockerfile 생성
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/discoveryservice-1.0.jar DiscoveryService.jar
ENTRYPOINT ["java", "-jar", "DiscoveryService.jar"]
```
4. docker image 생성
```
docker build --tag peppercode01/discovery-service:1.0 .
```
5. docker image 실행
```
docker run -d -p 8761:8761 --network ecommerce-network -e "spring.cloud.config.uri=http://config-service:8888" --name discovery-service peppercode01/discovery-service:1.0
```

## Apigateway Service
1. gradle 버전 수정 (1.0)
2. gradle clean, gradle assemble
3. Dockerfile 생성
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/apigateway-service-1.0.jar ApigatewayService.jar
ENTRYPOINT ["java", "-jar", "ApigatewayService.jar"]
```
4. docker image 생성
```
docker build -t peppercode01/apigateway-service:1.0 .
```
5. docker image 실행
```
docker run -d -p 8000:8000 --network ecommerce-network -e "spring.cloud.config.uri=http://config-service:8888" -e "spring.rabbitmq.host=rabbitmq" -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" --name apigateway-service peppercode01/apigateway-service:1.0
```

## MariaDB
1. /mariadb/data 파일을 /mysql_data/data로 복사 후 Dockerfile 생성
```
FROM mariadb
ENV MYSQL_ROOT_PASSWORD test1357
ENV MYSQL_DATABASE mydb
COPY ./mysql_data/data /var/lib/mysql
EXPOSE 3306
CMD ["--user=root"]
```
2. docker image 생성
```
docker build -t peppercode01/my_mariadb-f Dockerfile_mariadb .
```
3. docker image 실행
```
docker run -d -p 3306:3306 --network ecommerce-network --name mariadb peppercode01/my_mariadb
```
MariaDB 실행
```
docker run -d -p 3306:3306  --network ecommerce-network --name mariadb peppercode01/my-mariadb:1.0
```
docker mariadb 접속
```
docker exec -it mariadb /bin/bash
mariadb-uroot -p
```
접근 허용 설정
```
grant all privileges on *.* to 'root'@'%' identified by 'test1357';
flush privileges;
```
종료
```
exit
```

## Kafka
1. Kafka Docker 저장소 클론
```
git clone https://github.com/wurstmeister/kafka-docker
```
2. docker-compose-single-broker.yml 수정
```yml
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
    networks:
      my-network:
        ipv4_address: 172.18.0.100
  kafka:
    # build: .
    image: wurstmeister/kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 172.18.0.101
      KAFKA_CREATE_TOPICS: "test:1:1"
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    depends_on:
      - zookeeper
    networks:
      my-network:
        ipv4_address: 172.18.0.101

networks:
  my-network:
    name: ecommerce-network
    external: true
```
3. Kafka 실행
```
docker-compose -f docker-compose-single-broker.yml up -d
```

## Zipkin
Zipkin 실행
```
docker run -d -p 9411:9411 --network ecommerce-network --name zipkin openzipkin/zipkin
```

## Monitoring
### Docker에서 파일 수정
1. Prometheus 실행
```
docker run -d -p 9090:9090 --network ecommerce-network --name prometheus
```
2. Files - etc/prometheus/prometheus.yml 파일 수정 후 재실행
```yml
...
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: "prometheus"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["host.docker.internal:9090"]

  #추가
  - job_name: 'user-service'
    scrape_interval: 15s
    metrics_path: '/user-service/actuator/prometheus'
    static_configs:
    - targets: ['host.docker.internal:8000']
  - job_name: 'order-service'
    scrape_interval: 15s
    metrics_path: '/order-service/actuator/prometheus'
    static_configs:
    - targets: ['host.docker.internal:8000']
  - job_name: 'apigateway-service'
    scrape_interval: 15s
    metrics_path: '/actuator/prometheus'
    static_configs:
    - targets: ['host.docker.internal:8000']
```
### 로컬 파일 마운트
1. prometheus.yml 파일 수정
```yml
...
scrape_configs:
  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
  - job_name: "prometheus"

    # metrics_path defaults to '/metrics'
    # scheme defaults to 'http'.

    static_configs:
      - targets: ["prometheus:9090"]

  #추가
  - job_name: 'user-service'
    scrape_interval: 15s
    metrics_path: '/user-service/actuator/prometheus'
    static_configs:
    - targets: ['apigateway-service:8000']
  - job_name: 'order-service'
    scrape_interval: 15s
    metrics_path: '/order-service/actuator/prometheus'
    static_configs:
    - targets: ['apigateway-service:8000']
  - job_name: 'apigateway-service'
    scrape_interval: 15s
    metrics_path: '/actuator/prometheus'
    static_configs:
    - targets: ['apigateway-service:8000']
```
2. Prometheus 실행
```
docker run  -d -p 9090:9090 --network ecommerce-network --name prometheus -v 파일위치/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus
```
Grafana 실행
```
docker run -d -p 3000:3000 --network ecommerce-network --name grafana grafana/grafana 
```

## User Microservice
1. gradle 버전 수정 (1.0)
2. gradle clean, gradle assemble
3. Dockerfile 생성
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/user-service-1.0.jar UserService.jar
ENTRYPOINT ["java", "-jar", "UserService.jar"]
```
4. docker image 생성
```
docker build -t peppercode01/user-service:1.0 .
```
5. docker image 실행
```
docker run -d --network ecommerce-network  --name user-service -e "spring.cloud.config.uri=http://config-service:8888" -e "spring.rabbitmq.host=rabbitmq" -e "spring.zipkin.base-url=http://zipkin:9411" -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" -e "logging.file=/api-logs/users-ws.log" peppercode01/user-service
```

## Order Microservice
1. gradle 버전 수정 (1.0)
2. KafkaProducerConfig.producerFactory의 kafka 주소 변경
```java
//props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.0.101:9092");
```
3. gradle clean, gradle assemble
4. Dockerfile 생성
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/order-service-1.0.jar OrderService.jar
ENTRYPOINT ["java", "-jar", "OrderService.jar"]
```
5. docker image 생성
```
docker build -t peppercode01/order-service:1.0 .
```
6. docker image 실행
```
docker run -d --network ecommerce-network  --name order-service -e "spring.zipkin.base-url=http://zipkin:9411" -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/"  -e "spring.datasource.url=jdbc:mariadb://mariadb:3307/mydb" -e "logging.file=/api-logs/orders-ws.log" peppercode01/order-service
```

## Catalog Microservice
1. gradle 버전 수정 (1.0)
2. KafkaConsumerConfig.producerFactory의 kafka 주소 변경
```java
//props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.0.101:9092");
```
3. gradle clean, gradle assemble
4. Dockerfile 생성
```
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY build/libs/catalog-service-1.0.jar CatalogService.jar
ENTRYPOINT ["java", "-jar", "CatalogService.jar"]
```
5. docker image 생성
```
docker build -t peppercode01/catalog-service:1.0 .
```
6. docker image 실행
```
docker run -d --network ecommerce-network --name catalog-service -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" -e "logging.file=/api-logs/catalogs-ws.log" peppercode01/catalog-service
```
