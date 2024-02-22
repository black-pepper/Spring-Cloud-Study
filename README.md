# Microservice 모니터링
## Micrometer
1. build.gradle 추가
```
implementation 'io.micrometer:micrometer-registry-prometheus'
```
2. application.yml에 metrics, prometheus 추가
```yaml
management:
  endpoints:
    web:
      exposure:
        include: refresh, health, beans, httptrace, busrefresh
```

3. controller에 @Time 추가
```java
    @GetMapping("/health_check")
    @Timed(value="users.status", longTask = true)
    public String status() {...}
```

## Prometheus
1. prometheus 설치 (https://prometheus.io/download/)
2. prometheus.yml 파일 수정
```yaml
scrape_configs:
  - job_name: "prometheus"
    static_configs:
      - targets: ["localhost:9090"]
  #추가
  - job_name: 'user-service'
    scrape_interval: 15s
    metrics_path: '/user-service/actuator/prometheus'
    static_configs:
    - targets: ['localhost:8000']
  - job_name: order-service'
    scrape_interval: 15s
    metrics_path: '/order-service/actuator/prometheus'
    static_configs:
    - targets: ['localhost:8000']
  - job_name: apigateway-service'
    scrape_interval: 15s
    metrics_path: '/actuator/prometheus'
    static_configs:
    - targets: ['localhost:8000']
```
3. prometheus.exe 실행

## Grafana
1. grafana 설치 (https://grafana.com/grafana/download)
2. .\bin\grafana.exe 실행
