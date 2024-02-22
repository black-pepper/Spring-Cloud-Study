# 장애 처리와 Microservice 분산 추적
## Users Microservice에 CircuitBreaker 적용
1. build.gradle 의존성 추가
```
implementation 'org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j'
```
2. userservice/config/Resilience4JConfig.java 추가
```java
@Configuration
public class Resilience4JConfig {
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4)
                .waitDurationInOpenState(Duration.ofMillis(1000))
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(2)
                .build();

        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4))
                .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build()
        );
    }
}
```
3. UserServiceImple.java 코드 수정
```java
...
private final OrderServiceClient orderServiceClient;
private final CircuitBreakerFactory circuitBreakerFactory;

...
        /* ErrorDecoder */
//        List<ResponseOrder> orderList = orderServiceClient.getOrders(userId);

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        List<ResponseOrder> orderList = circuitBreaker.run(() -> orderServiceClient.getOrders(userId),
                throwable -> new ArrayList<>());
...
```
## Microservice의 분산 추적
1. zipkin 실행 (https://zipkin.io/pages/quickstart.html)
2.  build.gradle 의존성 추가
```
implementation 'org.springframework.boot:spring-boot-starter-actuator'
implementation 'io.micrometer:micrometer-tracing-bridge-brave'
implementation 'io.zipkin.reporter2:zipkin-reporter-brave'
```
3. application.yml 코드 추가
```yaml
  tracing:
    sampling:
      probability: 1.0
    zipkin:
      tracing:
        endpoint: "http://localhost:9411"
```
