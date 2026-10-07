package com.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer {

  private static final Logger log = LoggerFactory.getLogger(
      KafkaConsumer.class);

  // groupId comes from spring.kafka.consumer.group-id
  @KafkaListener(topics = "${kafka.topic.patient}")
  public void consumeEvent(byte[] event) {
    try {
      PatientEvent patientEvent = PatientEvent.parseFrom(event);
      // ... perform any business related to analytics here

      // Only identifiers are logged: name/email are personal health data
      log.info("Received Patient Event: [PatientId={}, EventType={}]",
          patientEvent.getPatientId(),
          patientEvent.getEventType());
    } catch (InvalidProtocolBufferException e) {
      log.error("Error deserializing event {}", e.getMessage());
    }
  }
}
