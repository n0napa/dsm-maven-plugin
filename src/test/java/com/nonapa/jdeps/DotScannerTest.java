package com.nonapa.jdeps;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

public class DotScannerTest extends TestCase {

    private DotScanner fixure;

    @Override
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        fixure = new DotScanner();
    }

    @Test
    public void testScanWithNullDependencyHandler() {
        try {
            fixure.scan(Paths.get("src/test/resources/jdeps/fake.jar.dot"), null);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
