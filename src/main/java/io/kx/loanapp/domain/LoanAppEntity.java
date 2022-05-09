package io.kx.loanapp.domain;

import com.google.protobuf.Empty;
import com.google.protobuf.util.Timestamps;
import io.kx.loanapp.api.LoanAppApi;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/kx/loanapp/api/loan_app_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanAppEntity extends AbstractLoanAppEntity {
  private static Logger log = LoggerFactory.getLogger(LoanAppEntity.class);
  @SuppressWarnings("unused")
  private final String entityId;
  public static final String ERROR_NOT_FOUND = "Not found";
  public static final String ERROR_WRONG_STATUS = "Wrong status";

  public LoanAppEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public LoanAppDomain.LoanAppDomainState emptyState() {
    return LoanAppDomain.LoanAppDomainState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> submit(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.SubmitCommand submitCommand) {
    log.info("submit: {}; loanAppId:{}", entityId, submitCommand.getLoanAppId());
    if (currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance())) {
      LoanAppDomain.Submitted event = LoanAppDomain.Submitted.newBuilder()
              .setLoanAppId(submitCommand.getLoanAppId())
              .setClientId(submitCommand.getClientId())
              .setClientMonthlyIncomeCents(submitCommand.getClientMonthlyIncomeCents())
              .setLoanAmountCents(submitCommand.getLoanAmountCents())
              .setLoanDurationMonths(submitCommand.getLoanDurationMonths())
              .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
              .build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());
    } else if (currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW) {
      return effects().reply(Empty.getDefaultInstance());
    } else {
      return effects().error(ERROR_WRONG_STATUS);
    }
  }

  @Override
  public Effect<LoanAppApi.LoanAppState> get(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.GetCommand getCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    return effects().reply(map(currentState));
  }

  private LoanAppApi.LoanAppState map(LoanAppDomain.LoanAppDomainState state){
    return  LoanAppApi.LoanAppState.newBuilder()
            .setClientId(state.getClientId())
            .setClientMonthlyIncomeCents(state.getClientMonthlyIncomeCents())
            .setLoanAmountCents(state.getLoanAmountCents())
            .setLoanDurationMonths(state.getLoanDurationMonths())
            .setLastUpdateTimestamp(state.getLastUpdateTimestamp())
            .setStatus(map(state.getStatus()))
            .setDeclineReason(state.getDeclineReason())
            .build();
  }
  private LoanAppApi.LoanAppStatus map(LoanAppDomain.LoanAppDomainStatus status){
    return LoanAppApi.LoanAppStatus.forNumber(status.getNumber());
  }

  @Override
  public Effect<Empty> approve(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.ApproveCommand approveCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW){
      LoanAppDomain.Approved event = LoanAppDomain.Approved.newBuilder()
              .setLoanAppId(approveCommand.getLoanAppId())
              .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis())).build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());

    }else if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_APPROVED)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public Effect<Empty> decline(LoanAppDomain.LoanAppDomainState currentState, LoanAppApi.DeclineCommand declineCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW){
      LoanAppDomain.Declined event = LoanAppDomain.Declined.newBuilder()
              .setLoanAppId(declineCommand.getLoanAppId())
              .setReason(declineCommand.getReason())
              .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis())).build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());

    }else if(currentState.getStatus() == LoanAppDomain.LoanAppDomainStatus.STATUS_DECLINED)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public LoanAppDomain.LoanAppDomainState submitted(LoanAppDomain.LoanAppDomainState currentState, LoanAppDomain.Submitted submitted) {
    return LoanAppDomain.LoanAppDomainState.newBuilder()
            .setClientId(submitted.getClientId())
            .setClientMonthlyIncomeCents(submitted.getClientMonthlyIncomeCents())
            .setLoanAmountCents(submitted.getLoanAmountCents())
            .setLoanDurationMonths(submitted.getLoanDurationMonths())
            .setLastUpdateTimestamp(submitted.getEventTimestamp())
            .setStatus(LoanAppDomain.LoanAppDomainStatus.STATUS_IN_REVIEW)
            .build();
  }
  @Override
  public LoanAppDomain.LoanAppDomainState approved(LoanAppDomain.LoanAppDomainState currentState, LoanAppDomain.Approved approved) {
    return currentState.toBuilder().setStatus(LoanAppDomain.LoanAppDomainStatus.STATUS_APPROVED).setLastUpdateTimestamp(approved.getEventTimestamp()).build();
  }
  @Override
  public LoanAppDomain.LoanAppDomainState declined(LoanAppDomain.LoanAppDomainState currentState, LoanAppDomain.Declined declined) {
    return currentState.toBuilder().setStatus(LoanAppDomain.LoanAppDomainStatus.STATUS_DECLINED).setDeclineReason(declined.getReason()).setLastUpdateTimestamp(declined.getEventTimestamp()).build();
  }

}
