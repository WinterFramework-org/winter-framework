## 🧩 개요
❄️ Winter Framework

Spring Framework의 핵심 아키텍처(DI, MVC)를 **기반으로 직접 구현한** 경량 웹 프레임워크입니다.

구성은 Core Container(DI/IoC), Web MVC, Data Access(MyBatis) 3개 레이어로 나뉩니다.

<p style="text-align: center;">
    <img src="https://froggy.kro.kr/assets/img/refs/winter-framework/winter-framework-structure.jpg" width="600" />
    <br />
    <sub>레이어 구성</sub>
</p>


### 1. Core Container (DI / IoC)

* **Singleton Registry**: 애플리케이션 내의 관리 대상 객체를 Map 기반의 레지스트리에 캐싱하여, 애플리케이션 전역에서 객체의 싱글톤 보장 및 효율적인 메모리 관리
* **Component Scan**: Reflection API를 활용하여 애플리케이션 패키지, 프레임워크 코어, 외부 AutoConfiguration을 탐색하고 컨테이너에 객체로 등록
* **DI/IoC**: 생성자를 통한 주입(DI)으로 객체 간 연결을 구성, 객체 생명주기 관리의 위임(IoC)하여 객체 간 결합도를 최소화
* **Property Injection**: 외부 설정(`.properties`)을 `Environment` 객체로 추상화하여 관리하며, `@Value`를 통해 필요한 설정값을 필드에 동적으로 바인딩
* **Post-Processor**: 컨테이너 초기화 과정에서 관리되는 객체에 대한 후처리 로직을 수행하며, 프레임워크 기능을 제어할 수 있는 핵심 확장 포인트(Hook)를 제공


### 2. Web MVC
- **DispatcherServlet (Front Controller)**: 모든 HTTP 요청의 단일 진입점, 전체적인 요청 처리 흐름을 제어·위임
- **Handler Mapping**: `@RequestMapping` 요청 URL·HTTP Method에 대응하는 핸들러 메서드를 찾아주는 라우팅 테이블 역할
- **Handler Adapter**: 핸들러 타입과 무관하게 동일한 요청 처리 흐름을 보장하는 어댑터, 핸들러별 실행 로직을 전략 패턴으로 분리해 코어 수정 없이(OCP 준수) 새로운 핸들러 형태 확장을 지원함
* **Argument Resolver**: 전략 패턴을 활용하여 HTTP 요청의 Body, Header, Parameter 등을 분석하고 핸들러 파라미터에 자동 바인딩
- **ReturnValue Handler**: 반환 타입에 따라 View 렌더링(ModelAndView) 또는 `@ResponseBody` 기반 응답(JSON/문자열)

### 3. Embedded Server & Infrastructure
- **Embedded Jetty**: 별도 WAS 없이 `main()` 실행만으로 Jetty 서버를 초기화·구동
- **Configuration 기반 구동**: `.properties` 설정을 로드해 포트/SSL 등 서버 구동 옵션을 외부 설정 관리

---
## 🌐 Web MVC 요청 처리 흐름

Winter MVC는 DispatcherServlet을 단일 진입점(Front Controller) 으로 사용합니다.
모든 HTTP 요청은 먼저 DispatcherServlet을 통해 유입됩니다.

이후 HandlerMapping이 요청 정보(URL, HTTP Method 등)에 맞는 Handler를 탐색하고,
HandlerAdapter가 Handler 타입과 무관하게 동일한 실행 흐름으로 호출을 통합합니다.

이 과정에서 ArgumentResolver는 요청 값을 메서드 인자로 바인딩하고,
ReturnValueHandler는 메서드 반환값을 HTTP 응답 형태로 변환합니다.

<p style="text-align: center;">
    <img src="https://froggy.kro.kr/assets/img/refs/winter-framework/web-mvc-request-flow.jpg" width="600" />
    <br />
    <sub>Web MVC 요청 처리 흐름</sub>
</p>

---

## 🚀 시작하기

### 실행 환경 구성
1. JDK 1.8
2. MAVEN 환경
3. Winter Framework 모듈을 프로젝트에 포함을 하여 실행 준비를 구성합니다.

### 1) Controller 작성

@Controller로 컴포넌트를 등록하고, @RequestMapping으로 URL을 매핑합니다.  
필요한 요청 데이터는 @RequestParam 등으로 바인딩할 수 있고, @ResponseBody는 응답 본문으로 반환합니다.


```java
@Controller
public class HelloController {

    @RequestMapping("/api/hello")
    @ResponseBody
    public String hello(@RequestParam(value = "name", defaultValue = "jackson") String name) {
        // Query Parameter 바인딩 및 JSON 응답
        return "Hello, " + name;
    }
}
```

### 2) Application 실행 (엔트리 포인트)

`main()`에서 프레임워크 런처만 호출하면 내장 서버가 구동됩니다.

```Java

public class ExampleApp {
   public static void main(String[] args) {
      WinterApplication.run(ExampleApp.class, args);
   }
}
```
### 3) 요청 결과

```Bash

$ curl "http://localhost:8080/api/hello?name=Winter"
> Hello, Winter
````

### 4) 설정 (선택)

```properties
server.port=8080

# SSL 사용 시 (옵션)
# server.ssl.enabled=true
# server.ssl.key-store=classpath:keystore.p12
# server.ssl.key-store-type=PKCS12
# server.ssl.key-store-password=changeit
```

### 📁 폴더 구조
```
com.winter.framework
├── boot/                                   # 부트스트랩 / 내장 서버 구동
│   ├── WinterApplication.java              # 프레임워크 진입점(컨테이너 초기화 + 서버 시작)
│   └── web/
│       ├── server/                         # WebServer 추상화
│       └── embedded/jetty/                 # Embedded Jetty 구현
│
├── context/                                # ApplicationContext(컨테이너 외부 API)
│   ├── ApplicationContext.java             # 빈 조회/컨테이너 접근 인터페이스
│   └── annotation/
│       └── ConfigurationClassPostProcessor.java  # @Configuration / @Bean 처리
│
├── beans/                                  # DI 컨테이너 코어
│   ├── factory/
│   │   └── support/
│   │       ├── BeanFactory.java            # 빈 생성 및 의존성 주입 핵심 로직
│   │       └── SingletonBeanRegistry.java  # 싱글톤 레지스트리(Map 캐시)
│   └── definition/
│       └── BeanDefinition.java             # 빈 메타데이터(정의 정보)
│
└── web/                                    # Web MVC 코어
    ├── DispatcherServlet.java              # Front Controller (프레임워크의 최조 진입점)
    ├── servlet/handler/
    │   └── RequestMappingHandlerMapping.java   # 핸들러 라우팅 (URL/Method → Handler)
    ├── servlet/
    │   ├── HandlerAdapter.java                 # Handler 실행 추상화
    │   └── mvc/method/annotation/
    │       └── DefaultControllerHandlerAdapter.java  # 핸들러 실행 어댑터 구현체
    │
    ├── method/
    │   ├── HandlerMethod.java               # 각 핸들러의 실행 단위의 메소드 모델
    │   └── RequestMappingInfo.java          # 요청 매핑 조건(URL/Method) 정의 모델
    └── method/support/
        ├── HandlerMethodArgumentResolver.java        # ArgumentResolver(요청값 → 인자 바인딩)
        └── HandlerMethodReturnValueHandler.java      # ReturnValueHandler(리턴값 → 응답 변환)

```