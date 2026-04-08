package com.exam.monitor;

import org.apache.samza.runtime.LocalApplicationRunner;
import org.apache.samza.config.MapConfig;
import java.util.HashMap;
import java.util.Map;

public class RealTimeRunner {
    public static void main(String[] args) {
// Logging is configured externally; avoid using org.apache.log4j directly here.
try {
            Map<String, String> config = new HashMap<>();
            
            // Fix: Use a variable to ensure app.id and job.id are EXACTLY the same
            String applicationId = "cheating-detection-app-03";

            // Basic Job Setup
            config.put("job.name", "cheating-detector-v2r");
            config.put("app.id", applicationId);
            
            config.put("job.factory.class", "org.apache.samza.job.local.ThreadJobFactory");
            config.put("job.coordinator.factory", "org.apache.samza.zk.ZkJobCoordinatorFactory");
            config.put("job.coordinator.zk.connect", "127.0.0.1:2181");

            // Kafka Connection
            config.put("systems.kafka.samza.factory", "org.apache.samza.system.kafka.KafkaSystemFactory");
            config.put("systems.kafka.producer.bootstrap.servers", "127.0.0.1:9092");
            config.put("systems.kafka.consumer.bootstrap.servers", "127.0.0.1:9092");

            // IMPORTANT: Define the default system for internal Samza operations (Changelogs/Windows)
            // This fixes the "changelog system is not defined" error
            config.put("job.default.system", "kafka");

            // WINDOWS COMPATIBILITY: Force 1 replica to prevent Kafka crashes
            config.put("job.coordinator.replication.factor", "1");
            config.put("systems.kafka.default.stream.replication.factor", "1");
            config.put("stores.default.changelog.replication.factor", "1");
            
            // Read from the very beginning of the topic
            config.put("systems.kafka.default.stream.samza.offset.default", "oldest");
config.put("zk.session.timeout.ms", "60000");
config.put("zk.connection.timeout.ms", "60000");
            LocalApplicationRunner runner = new LocalApplicationRunner(new CheatingDetectionApp(), new MapConfig(config));
            
            System.out.println("--- STARTING SAMZA JOB ---");
            runner.run();
            
            // Wait until the application finishes or is stopped
System.out.println("--- SAMZA JOB IS NOW RUNNING ---");
System.out.println("Keep this terminal open. Send messages to 'exam-events' topic.");

while (true) {
  
        // This loop keeps the main thread alive so the background 
        // Samza threads can actually do their work.
        Thread.sleep(5000); 
    }
            
        } catch (Exception e) {
            System.err.println("Fatal error starting Samza job:");
            e.printStackTrace();
        }
    }
}