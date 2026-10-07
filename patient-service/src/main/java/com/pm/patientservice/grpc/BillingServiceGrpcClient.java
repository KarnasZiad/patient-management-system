package com.pm.patientservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc;
import com.pm.patientservice.exception.BillingServiceUnavailableException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {

  private static final Logger log = LoggerFactory.getLogger(
      BillingServiceGrpcClient.class);
  private final ManagedChannel channel;
  private final BillingServiceGrpc.BillingServiceBlockingStub blockingStub;
  private final Duration deadline;

  public BillingServiceGrpcClient(
      @Value("${billing.service.address}") String serverAddress,
      @Value("${billing.service.grpc.port}") int serverPort,
      @Value("${billing.service.grpc.deadline}") Duration deadline) {

    log.info("Connecting to Billing Service GRPC service at {}:{}",
        serverAddress, serverPort);

    this.channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
        .usePlaintext().build();
    this.blockingStub = BillingServiceGrpc.newBlockingStub(channel);
    this.deadline = deadline;
  }

  public BillingResponse createBillingAccount(String patientId, String name,
      String email) {

    BillingRequest request = BillingRequest.newBuilder().setPatientId(patientId)
        .setName(name).setEmail(email).build();

    try {
      // A fresh deadline per call: without it a hung billing service blocks forever
      BillingResponse response = blockingStub
          .withDeadlineAfter(deadline.toMillis(), TimeUnit.MILLISECONDS)
          .createBillingAccount(request);
      log.info("Billing account {} created for patient {}",
          response.getAccountId(), patientId);
      return response;
    } catch (StatusRuntimeException e) {
      log.error("Billing service call failed for patient {}: {}", patientId,
          e.getStatus());
      throw new BillingServiceUnavailableException(
          "Billing service unavailable", e);
    }
  }

  @PreDestroy
  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }
}
