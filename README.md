### 🧩 개요
Spring Framework의 핵심 개념들을 직접 구현하며  
**내부 동작 방식에 대한 깊이 있는 이해**를 목적으로 개발한 경량 웹 프레임워크입니다.  
Servlet 기반의 DispatcherServlet, Bean DI, Annotation 기반 Request Mapping, Component Scan 등을 포함하고 있습니다.

---

### 🚀 주요 기능 구현

| 기능 구성 요소               | 구현 설명                                                    |
| ---------------------------- | ------------------------------------------------------------ |
| **DI 컨테이너**              | `@Component`, `@Autowired` 기반의 의존성 주입 구현. `BeanDefinition`, `BeanFactory`, `ApplicationContext` 설계 |
| **Front Controller**         | `DispatcherServlet`이 모든 요청을 받아 적절한 Handler로 위임 |
| **RequestMapping 처리기**    | `@RequestMapping`, `@RequestParam`, `@PathVariable` 기반 메서드 매핑 로직 설계 |
| **Handler Adapter 구조**     | 다양한 리턴 타입 지원을 위한 `HandlerMethodReturnValueHandler` 체계 구축 |
| **Embedded Web Server**      | Jetty 기반 WAS 내장, JSP 처리기 연동                         |
| **패키지 스캐닝 및 빈 등록** | 클래스 경로 탐색 후, `@Component`가 붙은 클래스만 Bean으로 등록 |

---

### 📁 코드 구조
```
winterframework/
├── beans/
│   └── BeanFactory, BeanDefinition, Autowired 등
├── context/
│   └── ApplicationContext
├── web/
│   ├── DispatcherServlet
│   ├── annotation/
│   │   └── RequestMapping, RequestParam 등
│   └── servlet/
│       └── handler/RequestMappingHandlerMapping
├── boot/
│   └── WinterApplication (실행 진입점)
```
