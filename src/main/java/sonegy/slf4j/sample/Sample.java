package sonegy.slf4j.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.StaticApplicationContext;

/**
 * @author: sonegy@sk.com
 */
public class Sample {
    private static final Logger logger = LoggerFactory.getLogger(Sample.class);

    public static void run() {
        // logback
        logger.debug("debug");
        logger.info("info");

        // common logging
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerSingleton("test", Sample.class);
        context.getBean("test", Sample.class);
    }

    public static void main(String[] args) {
        run();
    }
}
