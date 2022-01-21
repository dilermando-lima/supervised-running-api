import java.util.concurrent.TimeUnit;

@SuppressWarnings({"java:S106","java:S1854","java:S125"})
public class RunExample {
    
    public static void main(String[] args) {

        System.out.println(" ====== Start");
        String resultRetrievedOnProcess;

        var runSupervisedProcess = new RunSupervisedProcess<String>()
                    .logDebugImplementation(System.err::println)// by default log is disabled
                    .setTimeOut(5, TimeUnit.SECONDS)            // by default 14400 seconds ( 4 hours )
                    .withNumRetryOnTimeOut(3)                   // by default only once ( 1 )
                    .withNumRetryOnEexception(6)                // by default only once  ( 1 )
                    .behaviorOnFinalExceptionTimeOut(RuntimeException::printStackTrace)      // by default will throw RuntimeException
                    .behaviorOnFinalExceptionExecution(RuntimeException::printStackTrace);  // by default will throw RuntimeException
        
        System.out.println(" ====== example with no errors ");

        resultRetrievedOnProcess =
            runSupervisedProcess
                .setRunning(() -> "value from paralel process with no error")
                .run()
                    .getResult(); 
                    
        System.out.println(" ====== example with ExecutionException ");        
               
        resultRetrievedOnProcess =
            runSupervisedProcess
                .setRunning(() -> "value from paralel process" + 1/0 )
                .run()
                    //.getResultWithDefaultReturnOnFinalExceptionExecution("Final result on error execution");
                    .getResult(); 

        System.out.println(" ====== example with TimeOutException ");  
    
        resultRetrievedOnProcess =
            runSupervisedProcess
            .setRunning(() ->{
                TimeUnit.SECONDS.sleep(6); 
                return  "value from paralel process"; 
            })
            .run()
                //.getResultWithDefaultReturnOnFinalExceptionTimeout("Final result on error execution");
                .getResult();
             
        System.out.println(" ====== Final result = " + resultRetrievedOnProcess);

    }

}
