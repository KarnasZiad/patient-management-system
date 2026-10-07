package com.pm.patientservice.kafka;

import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaProducer {

  public static final String PATIENT_CREATED = "PATIENT_CREATED";

  private static final Logger log = LoggerFactory.getLogger(
      KafkaProducer.class);
  private final KafkaTemplate<String, byte[]> kafkaTemplate;
  private final String topic;

  public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate,
      @Value("${kafka.topic.patient}") String topic) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = topic;
  }

  public void sendEvent(Patient patient, String eventType) {
    String patientId = patient.getId().toString();

    PatientEvent event = PatientEvent.newBuilder()
        .setPatientId(patientId)
        .setName(patient.getName())
        .setEmail(patient.getEmail())
        .setEventType(eventType)
        .build();

    // Keyed by patientId so all events of a patient land on the same partition
    // (ordering guaranteed). send() is asynchronous: failures surface in the callback.
    kafkaTemplate.send(topic, patientId, event.toByteArray())
        .whenComplete((result, ex) -> {
          if (ex != null) {
            log.error("Failed to send {} event for patient {}", eventType,
                patientId, ex);
          } else {
            log.debug("Sent {} event for patient {}", eventType, patientId);
          }
        });
  }
}
