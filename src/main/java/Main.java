package main.java;

public class Main {
    private static SimpleRecordServer runningInstance;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Graceful shutdown.");
            if (runningInstance != null)
                runningInstance.stopServer();
        }, "ShutdownThread"));

        runningInstance = new SimpleRecordServer();
    }
}