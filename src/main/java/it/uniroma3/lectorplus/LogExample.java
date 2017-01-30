package it.uniroma3.lectorplus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogExample {

    private static final Logger LOG = LoggerFactory.getLogger(LogExample.class);

    public static void main(String... args){
        LOG.trace("This will be printed on trace");
        LOG.debug("This will be printed on debug");
        LOG.info("This will be printed on info");
        LOG.warn("This will be printed on warn");
        LOG.error("This will be printed on error");
    }
}