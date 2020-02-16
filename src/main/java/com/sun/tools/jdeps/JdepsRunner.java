package com.sun.tools.jdeps;

public class JdepsRunner {

    private JdepsRunner() {
        // ...
    }

    public static void run(String in, String out) {
        JdepsTask task = new JdepsTask();
        String[] arguments = { "-verbose", "-dotoutput=" + out, in };
        task.run(arguments);
    }
}
