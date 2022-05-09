package io.kx.loanproc.api;

import com.google.protobuf.Empty;
import io.kx.Main;
import io.kx.loanproc.domain.LoanProcDomain;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class LoanProcEntityIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
    new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final LoanProcService client;

  public LoanProcEntityIntegrationTest() {
    client = testKit.getGrpcClient(LoanProcService.class);
  }

  private LoanProcApi.ProcessCommand create(String loanAppId){
    return create(loanAppId,1000,500,24);
  }
  private LoanProcApi.ProcessCommand create(String loanAppId, long monthlyIncomeCents, long loanAmountCents, int loanDurationMonths){
    return LoanProcApi.ProcessCommand.newBuilder()
            .setLoanAppId(loanAppId)
            .setClientMonthlyIncomeCents(monthlyIncomeCents)
            .setLoanAmountCents(loanAmountCents)
            .setLoanDurationMonths(loanDurationMonths)
            .build();
  }

  private void assertGet(String loanAppId, LoanProcApi.LoanProcStatus status) throws Exception{
    LoanProcApi.LoanProcState loanProc = client.get(LoanProcApi.GetCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5,SECONDS);
    assertNotNull(loanProc);
    assertEquals(status,loanProc.getStatus());
  }
  @Test
  public void processSuccess() throws Exception {

    String loanAppId = UUID.randomUUID().toString();
    client.process(create(loanAppId)).toCompletableFuture().get(5, SECONDS); //note use get for every call to get sequential deterministic results
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
  }

  @Test
  public void submitOnAlreadySubmittedEntity() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    client.process(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
    client.process(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
  }

  @Test
  public void approveSuccess() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    String reviewerId = UUID.randomUUID().toString();
    client.process(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
    client.approve(LoanProcApi.ApproveCommand.newBuilder().setLoanAppId(loanAppId).setReviewerId(reviewerId).build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_APPROVED);
  }

  @Test
  public void declineReviewSuccess() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    String reviewerId = UUID.randomUUID().toString();
    client.process(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
    client.decline(LoanProcApi.DeclineCommand.newBuilder().setLoanAppId(loanAppId).setReviewerId(reviewerId).setReason("reason").build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_DECLINED);
  }
}
