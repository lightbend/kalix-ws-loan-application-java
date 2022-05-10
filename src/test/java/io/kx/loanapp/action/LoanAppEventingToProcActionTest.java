package io.kx.loanapp.action;

import akka.stream.javadsl.Source;
import com.google.protobuf.Any;
import com.google.protobuf.Empty;
import io.kx.loanapp.action.LoanAppEventingToProcAction;
import io.kx.loanapp.action.LoanAppEventingToProcActionTestKit;
import io.kx.loanapp.domain.LoanAppDomain;
import kalix.javasdk.testkit.ActionResult;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

public class LoanAppEventingToProcActionTest {

  @Test
  @Ignore("to be implemented")
  public void exampleTest() {
    LoanAppEventingToProcActionTestKit service = LoanAppEventingToProcActionTestKit.of(LoanAppEventingToProcAction::new);
    // // use the testkit to execute a command
    // SomeCommand command = SomeCommand.newBuilder()...build();
    // ActionResult<SomeResponse> result = service.someOperation(command);
    // // verify the reply
    // SomeReply reply = result.getReply();
    // assertEquals(expectedReply, reply);
  }

  @Test
  @Ignore("to be implemented")
  public void onSubmittedTest() {
    LoanAppEventingToProcActionTestKit testKit = LoanAppEventingToProcActionTestKit.of(LoanAppEventingToProcAction::new);
    // ActionResult<Empty> result = testKit.onSubmitted(LoanAppDomain.Submitted.newBuilder()...build());
  }

  @Test
  @Ignore("to be implemented")
  public void ignoreOtherEventsTest() {
    LoanAppEventingToProcActionTestKit testKit = LoanAppEventingToProcActionTestKit.of(LoanAppEventingToProcAction::new);
    // ActionResult<Empty> result = testKit.ignoreOtherEvents(Any.newBuilder()...build());
  }

}
