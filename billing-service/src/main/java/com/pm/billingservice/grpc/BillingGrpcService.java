package com.pm.billingservice.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import java.util.UUID;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

  private static final String STATUS_ACTIVE = "ACTIVE";

  private static final Logger log = LoggerFactory.getLogger(
      BillingGrpcService.class);

  @Override
  public void createBillingAccount(BillingRequest billingRequest,
      StreamObserver<BillingResponse> responseObserver) {

      // Only the patient id is logged: name/email are personal health data
      log.info("createBillingAccount request received for patient {}",
          billingRequest.getPatientId());

      // Business logic - e.g save to database, perform calculates etc

      BillingResponse response = BillingResponse.newBuilder()
          .setAccountId(UUID.randomUUID().toString())
          .setStatus(STATUS_ACTIVE)
          .build();

      responseObserver.onNext(response);
      responseObserver.onCompleted();
  }
}
