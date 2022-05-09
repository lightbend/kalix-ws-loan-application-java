package io.kx.loanproc.domain;

import com.google.protobuf.Empty;
import com.google.protobuf.util.Timestamps;
import io.kx.loanapp.domain.LoanAppDomain;
import io.kx.loanproc.api.LoanProcApi;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity.Effect;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the Event Sourced Entity Service described in your io/kx/loanproc/api/loan_proc_api.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanProcEntity extends AbstractLoanProcEntity {

  private static Logger log = LoggerFactory.getLogger(LoanProcEntity.class);
  public static final String ERROR_NOT_FOUND = "Not found";
  public static final String ERROR_WRONG_STATUS = "Wrong status";

  @SuppressWarnings("unused")
  private final String entityId;

  public LoanProcEntity(EventSourcedEntityContext context) {
    this.entityId = context.entityId();
  }

  @Override
  public LoanProcDomain.LoanProcDomainState emptyState() {
    return LoanProcDomain.LoanProcDomainState.getDefaultInstance();
  }

  @Override
  public Effect<Empty> process(LoanProcDomain.LoanProcDomainState currentState, LoanProcApi.ProcessCommand processCommand) {
    log.info("process: {}; loanAppId:{}",entityId,processCommand.getLoanAppId());
    if(currentState.equals(LoanProcDomain.LoanProcDomainState.getDefaultInstance())) {
      LoanProcDomain.ReadyForReview event = LoanProcDomain.ReadyForReview.newBuilder()
              .setLoanAppId(processCommand.getLoanAppId())
              .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis()))
              .build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());
    }else if(currentState.getStatus() == LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public Effect<LoanProcApi.LoanProcState> get(LoanProcDomain.LoanProcDomainState currentState, LoanProcApi.GetCommand getCommand) {
    if(currentState.equals(LoanAppDomain.LoanAppDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    return effects().reply(map(currentState));
  }

  private LoanProcApi.LoanProcState map(LoanProcDomain.LoanProcDomainState state){
    return  LoanProcApi.LoanProcState.newBuilder()
            .setReviewerId(state.getReviewerId())
            .setDeclineReason(state.getDeclineReason())
            .setLastUpdateTimestamp(state.getLastUpdateTimestamp())
            .setStatus(map(state.getStatus()))
            .setDeclineReason(state.getDeclineReason())
            .build();
  }
  private LoanProcApi.LoanProcStatus map(LoanProcDomain.LoanProcDomainStatus status){
    return LoanProcApi.LoanProcStatus.forNumber(status.getNumber());
  }

  @Override
  public Effect<Empty> approve(LoanProcDomain.LoanProcDomainState currentState, LoanProcApi.ApproveCommand approveCommand) {
    log.info("approve: {}; loanAppId:{}",entityId,approveCommand.getLoanAppId());
    if(currentState.equals(LoanProcDomain.LoanProcDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    if(currentState.getStatus() == LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW){
      LoanProcDomain.Approved event = LoanProcDomain.Approved.newBuilder()
              .setLoanAppId(approveCommand.getLoanAppId())
              .setReviewerId(approveCommand.getReviewerId())
              .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis())).build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());

    }else if(currentState.getStatus() == LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public Effect<Empty> decline(LoanProcDomain.LoanProcDomainState currentState, LoanProcApi.DeclineCommand declineCommand) {
    if(currentState.equals(LoanProcDomain.LoanProcDomainState.getDefaultInstance()))
      return effects().error(ERROR_NOT_FOUND);
    if(currentState.getStatus() == LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW){
      LoanProcDomain.Declined event = LoanProcDomain.Declined.newBuilder()
              .setLoanAppId(declineCommand.getLoanAppId())
              .setReviewerId(declineCommand.getReviewerId())
              .setReason(declineCommand.getReason())
              .setEventTimestamp(Timestamps.fromMillis(System.currentTimeMillis())).build();
      return effects().emitEvent(event).thenReply(__ -> Empty.getDefaultInstance());

    }else if(currentState.getStatus() == LoanProcDomain.LoanProcDomainStatus.STATUS_DECLINED)
      return effects().reply(Empty.getDefaultInstance());
    else
      return effects().error(ERROR_WRONG_STATUS);
  }

  @Override
  public LoanProcDomain.LoanProcDomainState readyForReview(LoanProcDomain.LoanProcDomainState currentState, LoanProcDomain.ReadyForReview readyForReview) {
    return LoanProcDomain.LoanProcDomainState.newBuilder()
            .setStatus(LoanProcDomain.LoanProcDomainStatus.STATUS_READY_FOR_REVIEW)
            .setLastUpdateTimestamp(readyForReview.getEventTimestamp())
            .build();
  }
  @Override
  public LoanProcDomain.LoanProcDomainState approved(LoanProcDomain.LoanProcDomainState currentState, LoanProcDomain.Approved approved) {
    return currentState.toBuilder()
            .setReviewerId(approved.getReviewerId())
            .setStatus(LoanProcDomain.LoanProcDomainStatus.STATUS_APPROVED)
            .setLastUpdateTimestamp(approved.getEventTimestamp())
            .build();

  }
  @Override
  public LoanProcDomain.LoanProcDomainState declined(LoanProcDomain.LoanProcDomainState currentState, LoanProcDomain.Declined declined) {
    return currentState.toBuilder()
            .setReviewerId(declined.getReviewerId())
            .setStatus(LoanProcDomain.LoanProcDomainStatus.STATUS_DECLINED)
            .setDeclineReason(declined.getReason())
            .setLastUpdateTimestamp(declined.getEventTimestamp())
            .build();
  }

}
