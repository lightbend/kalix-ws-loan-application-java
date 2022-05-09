package io.kx.loanproc.view;

import com.google.protobuf.Any;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.domain.LoanProcDomain;
import kalix.javasdk.view.View;
import kalix.javasdk.view.ViewContext;

// This class was initially generated based on the .proto definition by Kalix tooling.
// This is the implementation for the View Service described in your io/kx/loanproc/view/loan_proc_by_status_view.proto file.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanProcByStatusView extends AbstractLoanProcByStatusView {

  public LoanProcByStatusView(ViewContext context) {}

  @Override
  public LoanProcByStatusModel.LoanProcViewState emptyState() {
    return LoanProcByStatusModel.LoanProcViewState.getDefaultInstance();
  }

  @Override
  public View.UpdateEffect<LoanProcByStatusModel.LoanProcViewState> onSubmitted(
          LoanProcByStatusModel.LoanProcViewState state, LoanProcDomain.ReadyForReview readyForReview) {
    LoanProcByStatusModel.LoanProcViewState newState = LoanProcByStatusModel.LoanProcViewState.newBuilder()
            .setLoanAppId(readyForReview.getLoanAppId())
            .setStatus(LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW)
            .setStatusId(LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW.getNumber())
            .setLastUpdateTimestamp(readyForReview.getEventTimestamp())
            .build();
    return effects().updateState(newState);
  }
  @Override
  public View.UpdateEffect<LoanProcByStatusModel.LoanProcViewState> onApproved(
          LoanProcByStatusModel.LoanProcViewState state, LoanProcDomain.Approved approved) {
    LoanProcByStatusModel.LoanProcViewState newState = state.toBuilder()
            .setStatus(LoanProcApi.LoanProcStatus.STATUS_APPROVED)
            .setStatusId(LoanProcApi.LoanProcStatus.STATUS_APPROVED.getNumber())
            .build();
    return effects().updateState(newState);
  }
  @Override
  public View.UpdateEffect<LoanProcByStatusModel.LoanProcViewState> onDeclined(
          LoanProcByStatusModel.LoanProcViewState state, LoanProcDomain.Declined declined) {
    LoanProcByStatusModel.LoanProcViewState newState = state.toBuilder()
            .setStatus(LoanProcApi.LoanProcStatus.STATUS_DECLINED)
            .setStatusId(LoanProcApi.LoanProcStatus.STATUS_DECLINED.getNumber())
            .build();
    return effects().updateState(newState);
  }
  @Override
  public View.UpdateEffect<LoanProcByStatusModel.LoanProcViewState> ignoreOtherEvents(
          LoanProcByStatusModel.LoanProcViewState state, Any any) {
    return effects().ignore();
  }
}

