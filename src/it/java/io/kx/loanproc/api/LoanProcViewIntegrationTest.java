package io.kx.loanproc.api;

import io.kx.Main;
import io.kx.loanproc.view.LoanProcByStatus;
import io.kx.loanproc.view.LoanProcByStatusModel;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

// Example of an integration test calling our service via the Kalix proxy
// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
public class LoanProcViewIntegrationTest {

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
  private final LoanProcByStatus view;

  public LoanProcViewIntegrationTest() {
    client = testKit.getGrpcClient(LoanProcService.class);
    view = testKit.getGrpcClient(LoanProcByStatus.class);
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
  public void viewTest() throws Exception {
    String loanAppId = UUID.randomUUID().toString();
    String reviewerId = UUID.randomUUID().toString();
    client.process(create(loanAppId)).toCompletableFuture().get(5, SECONDS);
    client.approve(LoanProcApi.ApproveCommand.newBuilder().setLoanAppId(loanAppId).setReviewerId(reviewerId).build()).toCompletableFuture().get(5, SECONDS);
    assertGet(loanAppId, LoanProcApi.LoanProcStatus.STATUS_APPROVED);
    assertView(LoanProcApi.LoanProcStatus.STATUS_APPROVED,1);

    String loanAppId2 = UUID.randomUUID().toString();
    client.process(create(loanAppId2)).toCompletableFuture().get(5, SECONDS);

    String loanAppId3 = UUID.randomUUID().toString();
    client.process(create(loanAppId3)).toCompletableFuture().get(5, SECONDS);

    assertView(LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW,2);
    assertView(LoanProcApi.LoanProcStatus.STATUS_APPROVED,1);

  }

  private void assertView(LoanProcApi.LoanProcStatus status, int expectedResults) throws Exception{
    Thread.sleep(10000);//needed for the view eventual consistent update
    LoanProcByStatusModel.GetLoanProcByStatusResponse response =
            view.getLoanAppsByStatus(LoanProcByStatusModel.GetLoanProcByStatusRequest.newBuilder()
                            .setStatusId(status.getNumber()).build())
                    .toCompletableFuture().get(5,SECONDS);
    assertNotNull(response);
    assertEquals(expectedResults,response.getResultsCount());
    assertFalse(response.getResultsList().stream().filter(l->l.getStatus()!=status).findAny().isPresent());
  }
}
