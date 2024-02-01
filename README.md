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
docker network create ecommerce-network
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
docker image 생성
```
docker build -t peppercode01/config-service:1.0 .
```
docker image 실행
```
run -d -p 8888:8888 --network ecommerce-network -e "spring.rabbitmq.host=rabbitmq" -e "spring.profiles.active=default" --name config-service peppercode01/config-service:1.0
```

## Discovery Service
docker image 생성
```
docker build --tag peppercode01/discovery-service:1.0 .
```
docker image 실행
```
docker run -d -p 8761:8761 --network ecommerce-network -e "spring.cloud.config.uri=http://config-service:8888" --name discovery-service peppercode01/discovery-service:1.0
```

## Apigateway Service
docker image 생성
```
docker build -t peppercode01/apigateway-service:1.0 .
```
docker image 실행
```
docker run -d -p 8000:8000 --network ecommerce-network -e "spring.cloud.config.uri=http://config-service:8888" -e "spring.rabbitmq.host=rabbitmq" -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" --name apigateway-service peppercode01/apigateway-service:1.0
```

## MariaDB
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
Kafka 실행
```
docker-compose -f docker-compose-single-broker.yml up -d
```

## Monitoring
Prometheus 실행
```
docker run -d -p 9090:9090 --network ecommerce-network --name prometheus
```
Files - etc/prometheus/prometheus.yml 파일 수정
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
Grafana 실행
```
docker run -d -p 3000:3000 --network ecommerce-network --name grafana grafana/grafana 
```

## User Microservice
docker image 생성
```
docker build -t peppercode01/user-service:1.0 .
```
docker image 실행
```
docker run -d --network ecommerce-network  --name user-service -e "spring.cloud.config.uri=http://config-service:8888" -e "spring.rabbitmq.host=rabbitmq" -e "spring.zipkin.base-url=http://zipkin:9411" -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" -e "logging.file=/api-logs/users-ws.log" peppercode01/user-service
```

## Order Microservice
docker image 생성
```
docker build -t peppercode01/order-service:1.0 .
```
docker image 실행
```
docker run -d --network ecommerce-network  --name order-service -e "spring.zipkin.base-url=http://zipkin:9411" -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/"  -e "spring.datasource.url=jdbc:mariadb://mariadb:3307/mydb" -e "logging.file=/api-logs/orders-ws.log" peppercode01/order-service
```

## Catalog Microservice
docker image 생성
```
docker build -t peppercode01/catalog-service:1.0 .
```
docker image 실행
```
docker run -d --network ecommerce-network --name catalog-service -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" -e "logging.file=/api-logs/catalogs-ws.log" peppercode01/catalog-service
```
