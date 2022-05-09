package io.kx.loanapp.domain;

import com.google.protobuf.Empty;
import io.kx.loanapp.api.LoanAppApi;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.javasdk.testkit.EventSourcedResult;
import org.junit.Ignore;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanAppEntityTest {

  @Test
  public void submitTest() {
    String loanAppId = UUID.randomUUID().toString();
    LoanAppEntityTestKit testKit = LoanAppEntityTestKit.of(loanAppId,LoanAppEntity::new);
    submitLoan(testKit,loanAppId);
  }

  private void submitLoan(LoanAppEntityTestKit testKit,String loanAppId){
    EventSourcedResult<Empty> result = testKit.submit(create(loanAppId));
    LoanAppDomain.Submitted event = result.getNextEventOfType(LoanAppDomain.Submitted.class);
    assertEquals(event.getLoanAppId(),loanAppId);
    assertGet(testKit,loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
  }
  private LoanAppApi.SubmitCommand create(String loanAppId){
    return create(loanAppId,1000,500,24);
  }
  private LoanAppApi.SubmitCommand create(String loanAppId, long monthlyIncomeCents, long loanAmountCents, int loanDurationMonths){
    return LoanAppApi.SubmitCommand.newBuilder()
            .setLoanAppId(loanAppId)
            .setClientId(UUID.randomUUID().toString())
            .setClientMonthlyIncomeCents(monthlyIncomeCents)
            .setLoanAmountCents(loanAmountCents)
            .setLoanDurationMonths(loanDurationMonths)
            .build();
  }
  private void assertGet(LoanAppEntityTestKit testKit,String loanAppId, LoanAppApi.LoanAppStatus status){
    EventSourcedResult<LoanAppApi.LoanAppState> getResult = testKit.get(LoanAppApi.GetCommand.newBuilder().setLoanAppId(loanAppId).build());
    assertFalse(getResult.didEmitEvents());
    assertEquals(status,getResult.getReply().getStatus());
  }
  @Test
  public void approveReviewTest() {
    String loanAppId = UUID.randomUUID().toString();
    LoanAppEntityTestKit testKit = LoanAppEntityTestKit.of(loanAppId,LoanAppEntity::new);

    submitLoan(testKit,loanAppId);
    EventSourcedResult<Empty> result = testKit.approve(LoanAppApi.ApproveCommand.newBuilder().setLoanAppId(loanAppId).build());
    LoanAppDomain.Approved event = result.getNextEventOfType(LoanAppDomain.Approved.class);
    assertEquals(event.getLoanAppId(),loanAppId);
    assertGet(testKit,loanAppId, LoanAppApi.LoanAppStatus.STATUS_APPROVED);
  }


  @Test
  public void declineReviewTest() {
    LoanAppEntityTestKit testKit = LoanAppEntityTestKit.of(LoanAppEntity::new);
    String loanAppId = UUID.randomUUID().toString();
    submitLoan(testKit,loanAppId);
    EventSourcedResult<Empty> result = testKit.decline(LoanAppApi.DeclineCommand.newBuilder().setLoanAppId(loanAppId).setReason("some reason").build());
    LoanAppDomain.Declined event = result.getNextEventOfType(LoanAppDomain.Declined.class);
    assertEquals(event.getLoanAppId(),loanAppId);
    assertGet(testKit,loanAppId, LoanAppApi.LoanAppStatus.STATUS_DECLINED);
  }

}
