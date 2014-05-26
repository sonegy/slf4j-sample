#SLF4J 로깅 처리

세상에는 여러가지 Logger들이 존재합니다.

- commons logging
	- http://commons.apache.org/proper/commons-logging/
- log4j
	- http://logging.apache.org/log4j/2.x/
- java util logging
	- http://docs.oracle.com/javase/6/docs/api/java/util/logging/package-summary.html
- logback
	- http://logback.qos.ch/

무엇을 쓸지는 크게 상관없지만 어플리케이션을 개발할때는 한가지 선택 해야하고, 중요한 것은 의존된 라이브러리가 쓰고 있는 Logger도 잘 확인해야 합니다. 섞어 쓸 수도 있지만, logging을 제어하려면 하나로 통일 되야 좋습니다. 여기서 내가 쓰고 싶은 Logger로 통일하고 싶다면 방법은 SLF4J를 쓰면 되는 것 입니다.

SLF4J는 로깅 Facade입니다. 로깅에 대한 추상 레이어를 제공하는것이고 java로 따지면 interface덩어리 라고 보시면 됩니다. artifact이름도 api라고 부릅니다.

pom.xml에 아래 의존을 추가합니다.
```
	<dependency>
    	<groupId>org.slf4j</groupId>
    	<artifactId>slf4j-api</artifactId>
    	<version>1.7.7</version>
	</dependency>
```
하지만 실행을 한다면.
```
	import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    public class Sample {
    	final Logger logger = LoggerFactory.getLogger(Sample.class);
        public void run() {
        	logger.debug("debug");
	        logger.info("info");
        }
    }
```
```
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".SLF4J: Defaulting to no-operation (NOP) logger implementationSLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
```
위와 같은 구현체가 없다는 에러를 내보냅니다. 간단하게 slf4j-simple을 추가 할 수도 있지만, 현실적으로 이것을 쓸이유는 없으니(기능이 너무 단순함). 바로 slf4j native 구현체인 ==logback== 을 사용하도록 하겠습니다.
```
	<dependency>
    	<groupId>ch.qos.logback</groupId>
        <artifactId>logback-classic</artifactId>
        <version>1.1.2</version>
  	</dependency>
```
## logback 사용

logback은 slf4j-api 1.7.6에 의존합니다. slf4j특성상 다양항 로깅 구현체를 쓸수 있는 형태라 대부분 slf4j-api와 logback 을 동시에 의존성 추가합니다.
```
    <dependencies>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.7.7</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.1.2</version>
            <exclusions>
                <exclusion>
                    <groupId>org.slf4j</groupId>
                    <artifactId>slf4j-api</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    </dependencies>
```
우리가 작성하고 있는 어플리케이션 레이어에서는 SLF4J를 사용해서 logging처리를 하면 실제 로그가 출력하는 행위는 logback이 하게 됩니다.

이제 흔하디 흔한 spring 한번 추가해보면.
```
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>3.2.4.RELEASE</version>
    </dependency>
```
하지만 spring-context는 jarkarta commons logging을 사용하고 있습니다. 실제 이렇게 두면 commons-logging이 의존성에 추가 됩니다. 여기서는 logback을 쓰려고 하니 관련라이브러리는 제거합니다.
```
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>3.2.4.RELEASE</version>
        <exclusions>
            <exclusion>
                <groupId>commons-logging</groupId>
                <artifactId>commons-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
```

그러나 아래 Spring관련 코드가 실행을 하면 문제가 발생합니다.
```
    StaticApplicationContext context = new StaticApplicationContext();
    context.registerSingleton("test", Sample.class);
    context.getBean("test", Sample.class);
```
결과
```
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/logging/LogFactory
     at org.springframework.context.support.AbstractApplicationContext.<init>(AbstractApplicationContext.java:164)
```

위와 같은 NoClassDeFoundError을 발생하게 됩니다. 실제 commons-logging.jar가 제거 되었으니 당연합니다. 이때 SLF4J는 Bridging legacy logging APIs를 제공해서 해결합니다.

## Bridging legacy logging APIs
```
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>jcl-over-slf4j</artifactId>
        <version>1.7.7</version>
    </dependency>
```
xxx-over-slf4j는 SLF4J가 지원하는 로깅을 대신 구현해주는 이름들입니다. 결과적으로 자신이 고칠수 없는 소스를 그대로 두고 SLF4J를 사용하는것처럼 바꿀수 있는 방법입니다. 쉽게 말해 각각 로깅 구현체를 SLF4J가 package이름으로 구현을 해놓은 것입니다. 참고로 jcl은 jarkarta commons logging의 약자이고, jul은 java util logging의 약자입니다.

간단하 logback.xml 설정을 해봅시다.
```
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type
             ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    <logger name="sonegy.slf4j.sample" level="INFO"></logger>
    <logger name="org.springframework.context" level="DEBUG"></logger>
    <root level="debug">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```
다시 실행하면 spring context에 debug 로그가 출력됨이 확인됨니다.
## Support for parameterized log messages
기존 Logger에서는 로그를 기록할때 완성된 문장을 써넣게 됩니다. 하지만 Logging은 전략에 따른 기록 형태이고 live상태에서는 debug를 지우고 info상태에서만 남기는 경우가 많습니다.
```
logger.debug("log test " + String.valueOf(1) + "count.");
```
이런경우라면 debug가 OFF상태 더라도 항상 String연산을 하게 되어 필요없는 낭비가 발생합니다.
그래서 아래와 같은 코드가 주를 이루었습니다.
```
if (logger.isdebugenabled()) {
	logger.debug("log test " + String.valueOf(1) + "count.");
}
```
조건절 로 연산을 막는 구조이지만 코드의 흐름을 방해하게 됩니다.

여기서 SLF4J는 String parameter arguments 를 제공합니다.
```
logger.debug("log test {} count", 1);
```
이렇게 되면 실제 String format연산을 debug가 실행하기전으로 미룰수 있습니다.

관련소스는 https://github.com/sonegy/slf4j-sample/tree/master 에 등록 되었습니다.

다음에는 logback 관련 포스팅을 해볼까 하네요.
