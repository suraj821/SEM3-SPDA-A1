package com.exam.monitor;

import org.apache.samza.application.StreamApplication;
import org.apache.samza.application.descriptors.StreamApplicationDescriptor;
import org.apache.samza.operators.KV;
import org.apache.samza.operators.windows.Windows;
import org.apache.samza.serializers.StringSerde;
import org.apache.samza.system.kafka.descriptors.KafkaInputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaOutputDescriptor;
import org.apache.samza.system.kafka.descriptors.KafkaSystemDescriptor;
import java.time.Duration;

public class CheatingDetectionApp implements StreamApplication {
    @Override
    public void describe(StreamApplicationDescriptor appDescriptor) {
        // 1. Define the Kafka System
        // 1. Define your system descriptor
    KafkaSystemDescriptor kafkaSystemDescriptor = new KafkaSystemDescriptor("kafka");
    
 /*  // 2. Set this system as the default for the whole application
    appDescriptor.withDefaultSystem(kafkaSystemDescriptor);

        // 2. Define Input and Output Topics
        KafkaInputDescriptor<String> inputDescriptor = 
            kafkaSystemDescriptor.getInputDescriptor("exam-events", new StringSerde());
        KafkaOutputDescriptor<String> outputDescriptor = 
            kafkaSystemDescriptor.getOutputDescriptor("exam-alerts", new StringSerde());
*/
// 2. Define the Serde explicitly
StringSerde stringSerde = new StringSerde();

// 3. Attach the Serde to the descriptors
KafkaInputDescriptor<String> inputDescriptor = 
    kafkaSystemDescriptor.getInputDescriptor("exam-events", stringSerde);
    
KafkaOutputDescriptor<String> outputDescriptor = 
    kafkaSystemDescriptor.getOutputDescriptor("exam-alerts", stringSerde);
        // 3. The Pipeline
        appDescriptor.getInputStream(inputDescriptor)
            .map(event -> {
                System.out.println("DEBUG: Samza received raw event: " + event);
                return event;
            })
            // Window: Group by Student ID, wait 10 seconds, count events
            .window(Windows.keyedTumblingWindow(
                event -> event.split(":")[0], // Key: Student ID
                Duration.ofSeconds(10), 
                new StringSerde(), 
                new StringSerde()
            ), "window-1")
            .filter(windowPane -> {
                int count = windowPane.getMessage().size();
                return count >= 3; // Trigger alert if 3+ violations
            })
            .map(windowPane -> {
                String studentId = windowPane.getKey().getPaneId();
                String alert = "ALERT: Potential Cheating by " + studentId;
                System.out.println("DEBUG: Sending to Kafka -> " + alert);
                return alert;
            })
            .sendTo(appDescriptor.getOutputStream(outputDescriptor));
    }
}