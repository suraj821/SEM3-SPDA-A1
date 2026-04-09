
# Real-Time Exam Monitoring Pipeline (Kafka + Samza)
This guide explains how to set up and run a real-time exam monitoring pipeline using Apache Kafka and Apache Samza.
---
## Prerequisites

* Java (JDK 8 or higher)
* Maven installed and configured
* Windows OS (commands provided for PowerShell)
---
## Setup & Execution Steps
### 1. Download Kafka
Download **Kafka version 3.7.0** (Scala 2.13):
```
kafka_2.13-3.7.0
```
---
### 2. Extract Kafka
Extract the downloaded folder to your preferred drive:
```
C:\kafka_2.13-3.7.0
```
or
```
D:\kafka_2.13-3.7.0
```
---
### 3. Navigate to Kafka Directory
Open Command Prompt or PowerShell and navigate:
```
cd C:\kafka_2.13-3.7.0
```
---

### 4. Start Zookeeper
Run the following command:
```
bin\zookeeper-server-start.sh config\zookeeper.properties
```
---

### 5. Start Kafka Broker
Open a new terminal and run:
```
bin\kafka-server-start.sh config\server.properties
```
---

### 6. Create Kafka Topics
Run in PowerShell:
```
.\bin\windows\kafka-topics.bat --create --topic exam-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

.\bin\windows\kafka-topics.bat --create --topic exam-alerts --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```
---

### 7. Start Kafka Consumer
Open a new PowerShell window and run:
```
.\bin\windows\kafka-console-consumer.bat --topic exam-alerts --from-beginning --bootstrap-server localhost:9092
```
This will display processed messages from the Samza pipeline.
---

### 8. Run Apache Samza Job
In your project directory, run:
```
mvn --% compile exec:java -Dexec.mainClass=com.exam.monitor.RealTimeRunner
```
Keep this running to process incoming events.
---

### 9. Start Kafka Producer
Open another PowerShell window:
```
.\bin\windows\kafka-console-producer.bat --topic exam-events --bootstrap-server localhost:9092
```
Send sample messages:

```
student_001:VIOLATION_LOOK_AWAY
student_001:VIOLATION_TAB_SWITCH
student_001:VIOLATION_LOOK_AWAY
student_001:VIOLATION_LOOK_AWAY
```

---

## 🛠️ Troubleshooting

* Ensure Zookeeper is running before Kafka
* Verify port `9092` is not blocked
* Check Java and Maven setup using:

  ```
  java -version
  mvn -version
  ```
* If scripts fail, try running PowerShell as Administrator
---

## 📂 Project Structure (Example)
```
project-root/
│
├── src/
├── pom.xml
└── README.md
```
---

## Summary
This setup enables:
* Real-time event ingestion (Kafka)
* Stream processing (Samza)
* Alert generation for exam violations

---
## 📎 Notes

* Keep all services running in separate terminals
* Use multiple windows for better monitoring
* Modify topics or partitions based on scaling needs

---



# FOR MAC Users and also using docker-compose file to install kafka and zookeeper

# **STEP 0 — What you need installed**

Run these one by one in Terminal:

```
java -version
mvn -version
docker --version
docker compose version
kubectl version --client
kind version
```

### You should have:

- **Java 8 or 11** (usually safest for Samza projects)
- **Maven**
- **Docker Desktop**
- **kubectl**
- **kind** (optional if using Kubernetes part)

# **STEP 1 — Open the project folder**

```
cd ~/Downloads
git clone https://github.com/suraj821/SEM3-SPDA-A1.git
cd SEM3-SPDA-A1
```

If you already downloaded it:

```
cd /path/to/SEM3-SPDA-A1
```

# **STEP 2 — Check project structure**

Run:

```
ls
```

You should see files/folders like:

```
docker-compose.yml
pom.xml
src/
README.md
```

If yes, good.

# **STEP 3 — Start Kafka + Zookeeper**

This is the most important step.

Run:

```
docker compose up -d
```

Then verify:

```
docker ps
```

You should see containers like:

- `zookeeper`
- `kafka`

If they are running → good.

# **STEP 4 — Check Kafka is healthy**

Run:

```
docker logs kafka --tail50
```

or if the container name is different:

```
docker ps
```

Look for the Kafka container name, then:

```
docker logs <your-kafka-container-name>--tail50
```

You want to see Kafka started successfully.

# **STEP 5 — Create Kafka topics**

Your project uses these topics:

- **`exam-events`** → input
- **`exam-alerts`** → output

Run this inside the Kafka container.

```
docker exec -it kafka  kafka-topics --create --topic exam-events --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1

docker exec -it kafka kafka-topics --create --topic exam-alerts --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

List topics to confirm:

```
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

You should see:

```
exam-events
exam-alerts
```

# **STEP 6 — IMPORTANT: Fix Mac localhost config**

This is the part that usually breaks on Mac.

Your Java Samza app should use **localhost**, not Docker internal hostnames like `kafka:29092`, unless you're running the app inside Docker.

## Open your Java runner file

Likely here:

```
src/main/java/com/exam/monitor/RealTimeRunner.java
```

Open it in VS Code / Cursor / IntelliJ and check for lines like:

```
config.put("systems.kafka.producer.bootstrap.servers","kafka:29092");
config.put("systems.kafka.consumer.bootstrap.servers","kafka:29092");
config.put("job.coordinator.zk.connect","zookeeper:2181");
```

### Change them to:

```
config.put("systems.kafka.producer.bootstrap.servers","localhost:9092");
config.put("systems.kafka.consumer.bootstrap.servers","localhost:9092");
config.put("job.coordinator.zk.connect","localhost:2181");
```

### Why?

Because:

- `kafka:29092` works **inside Docker**
- `localhost:9092` works **from your Mac terminal / Java app**

This is usually the **main fix**.

# **STEP 7 — Build the project**

Run:

cd to parent directory SEM3-SPDA-A1

In my case: /Users/macminii7/OBSIDIAN/BITS-PILANI/StreamProcessing-Assignment2/SEM3-SPDA-A1

```
cd /Users/macminii7/OBSIDIAN/BITS-PILANI/StreamProcessing-Assignment2/SEM3-SPDA-A1
```

```
mvn clean compile
```

If successful, Maven should finish without errors.

# **STEP 8 — Start the Kafka output consumer first**

Before running Samza, open a **new Terminal tab** and listen for alerts.

```
docker exec -it kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic exam-alerts --from-beginning
```

Keep this terminal open.

👉 This will show **alerts produced by Samza**.

# **STEP 9 — Start the Samza job**

Go back to your main terminal (project folder) and run:

```
mvn exec:java -Dexec.mainClass=com.exam.monitor.RealTimeRunner
```

This should start:

- Kafka consumer in Samza
- Real-time processing
- Alert generation

If it starts successfully, your Samza pipeline is now live.
