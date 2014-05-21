package sonegy.slf4j.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: sonegy@sk.com
 */
public class Sample {
    final Logger logger = LoggerFactory.getLogger(Sample.class);

    public void run() {
        logger.debug("debug");
        logger.info("info");
    }

    public static void main(String[] args) {
        new Sample().run();
    }
}
