package io.kx;

import io.kx.loanapp.api.LoanAppApi;
import io.kx.loanapp.api.LoanAppService;
import io.kx.loanproc.api.LoanProcApi;
import io.kx.loanproc.api.LoanProcService;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SystemIntegrationTest {

    /**
     * The test kit starts both the service container and the Kalix proxy.
     */
    @ClassRule
    public static final KalixTestKitResource testKit =
            new KalixTestKitResource(Main.createKalix());

    /**
     * Use the generated gRPC client to call the service through the Akka Serverless proxy.
     */
    private final LoanAppService loanAppClient;
    private final LoanProcService loanProcClient;

    public SystemIntegrationTest() {
        loanAppClient = testKit.getGrpcClient(LoanAppService.class);
        loanProcClient = testKit.getGrpcClient(LoanProcService.class);
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
    private LoanAppApi.SubmitCommand create(String loanAppId){
        return create(loanAppId,1000,500,24);
    }

    private void assertLoanAppGet(String loanAppId, LoanAppApi.LoanAppStatus status) throws Exception{
        LoanAppApi.LoanAppState loanApp = loanAppClient.get(LoanAppApi.GetCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5,SECONDS);
        assertNotNull(loanApp);
        assertEquals(status,loanApp.getStatus());
    }
    private void assertLoanProcGet(String loanAppId, LoanProcApi.LoanProcStatus status) throws Exception{
        LoanProcApi.LoanProcState loanProc = loanProcClient.get(LoanProcApi.GetCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5,SECONDS);
        assertNotNull(loanProc);
        assertEquals(status,loanProc.getStatus());
    }
    @Test
    public void submitSuccess() throws Exception {

        String loanAppId = UUID.randomUUID().toString();
        loanAppClient.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS); //note use get for every call to get sequential deterministic results
        assertLoanAppGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
        Thread.sleep(10000);
        assertLoanProcGet(loanAppId,LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
    }


    @Test
    public void approveSuccess() throws Exception {
        String loanAppId = UUID.randomUUID().toString();
        loanAppClient.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS); //note use get for every call to get sequential deterministic results
        assertLoanAppGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
        Thread.sleep(10000);
        assertLoanProcGet(loanAppId,LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
        loanProcClient.approve(LoanProcApi.ApproveCommand.newBuilder().setLoanAppId(loanAppId).build()).toCompletableFuture().get(5, SECONDS);
        Thread.sleep(10000);
        assertLoanAppGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_APPROVED);
    }

    @Test
    public void declineSuccess() throws Exception {
        String loanAppId = UUID.randomUUID().toString();
        String reviewerId = UUID.randomUUID().toString();
        String reason = "reason";
        loanAppClient.submit(create(loanAppId)).toCompletableFuture().get(5, SECONDS); //note use get for every call to get sequential deterministic results
        assertLoanAppGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_IN_REVIEW);
        Thread.sleep(10000);
        assertLoanProcGet(loanAppId,LoanProcApi.LoanProcStatus.STATUS_READY_FOR_REVIEW);
        loanProcClient.decline(LoanProcApi.DeclineCommand.newBuilder().setLoanAppId(loanAppId).setReason(reason).setReviewerId(reviewerId).build()).toCompletableFuture().get(5, SECONDS);
        Thread.sleep(10000);
        assertLoanAppGet(loanAppId, LoanAppApi.LoanAppStatus.STATUS_DECLINED);
    }
}
