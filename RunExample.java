import java.util.concurrent.TimeUnit;

@SuppressWarnings({"java:S106"}) // ignore sonarlit warning on log with System...println
public class RunExample {

    public static final long DEFAULT_SETUP_TIMEOUT = 5L;
    public static final TimeUnit DEFAULT_SETUP_TIMEOUT_UNIT = TimeUnit.SECONDS;
    public static final int DEFAULT_SETUP_NUM_RETRY_TIME_OUT = 3;
    public static final int DEFAULT_SETUP_NUM_RETRY_EXCEPTION = 3;

    
    public static void main(String[] args) {

        var runSupervisedInstance = getStandardSetupRunSupervisedToAllTest();

        // testWithNoErrors(runSupervisedInstance);
        // try{
        //     testWithExecutionException(runSupervisedInstance);
        // }catch(RuntimeException e){
        //     System.out.println("Error thrown on testWithExecutionException()");
        // }
        // try{
        //     testWithTimeOutException(runSupervisedInstance);
        // }catch(RuntimeException e){
        //     System.out.println("Error thrown on testWithTimeOutException()");
        // }

        // try{
        //     testWithTimeOutExceptionPrintOnConsole(runSupervisedInstance);
        // }catch(RuntimeException e){
        //     System.out.println("Error thrown on testWithTimeOutExceptionPrintOnConsole()");
        // }

        
        //TODO: not working....
        testWithHandledExecutionException(runSupervisedInstance);

        testWithHandledTimeOutException(runSupervisedInstance);
      
    }


    private static  RunSupervised<String> getStandardSetupRunSupervisedToAllTest(){
        return new RunSupervised<String>()
            //.logDebugImplementation(System.err::println)    // by default log is disabled
            .setTimeOut(DEFAULT_SETUP_TIMEOUT, DEFAULT_SETUP_TIMEOUT_UNIT)            // by default 14400 seconds ( 4 hours )
            .withNumRetryOnTimeOut(DEFAULT_SETUP_NUM_RETRY_TIME_OUT)                   // by default only once ( 1 )
            .withNumRetryOnEexception(DEFAULT_SETUP_NUM_RETRY_EXCEPTION)    ;            // by default only once  ( 1 )
            //.behaviorOnFinalExceptionTimeOut(RuntimeException::printStackTrace)    // by default will throw RuntimeException
            //.behaviorOnFinalExceptionExecution(RuntimeException::printStackTrace);  // by default will throw RuntimeException
    }



    private static void testWithNoErrors(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithNoErrors() ");
        String output = runSupervisedInstance
            .setRunning(() -> "value from parallel process with no error")
            .run()
                .getResult(); 
        System.out.println("End test: testWithNoErrors() with output = " + output);
    }


    private static void testWithExecutionException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithExecutionException() ");
        String output = runSupervisedInstance
            .setRunning(() -> "value from parallel process with exception" + 1/0 )
            .run()
                .getResult(); 
        System.out.println("End test: testWithExecutionException() with output = " + output);
    }

    private static void testWithTimeOutException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithTimeOutException() ");
        String output = runSupervisedInstance
            .setRunning(() ->{
                    
                TimeUnit.SECONDS.sleep(7); 
                return  "value from parallel process on timeout exception"; 
            })
            .run()
                .getResult(); 
        System.out.println("End test: testWithTimeOutException() with output = " + output);
    }

    private static void testWithHandledTimeOutException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithHandledTimeOutException() ");
        String output = runSupervisedInstance
            .setRunning(() ->{
                    
                TimeUnit.SECONDS.sleep(7); 
                return  "value from parallel process on handled timeout exception"; 
            })
            .run()
                .getResultWithDefaultReturnOnFinalExceptionTimeout("handled default value on timeoutexception");
        System.out.println("End test: testWithHandledTimeOutException() with output = " + output);
    }

    private static void testWithHandledExecutionException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithHandledExecutionException() ");
        String output = runSupervisedInstance
            .setRunning(() ->{
                    
                TimeUnit.SECONDS.sleep(7); 
                return  "value from parallel process on handled execution exception"; 
            })
            .run()
                .getResultWithDefaultReturnOnFinalExceptionExecution("handled default value on exceution timeout");
        System.out.println("End test: testWithHandledExecutionException() with output = " + output);
    }




    private static void testWithTimeOutExceptionPrintOnConsole(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithTimeOutExceptionPrintOnConsole() ");
        String output = runSupervisedInstance
            .setRunning(() ->{
                    
                int increment = 0;
                while(increment <= 1000){
                    TimeUnit.MILLISECONDS.sleep(300); 
                    System.err.println("\t\t\t\tprocessing: " + Thread.currentThread().getId());
                    increment++;
                }
                return  "value from parallel process on timeout exception print on console "; 
            })
            .run()
                .getResult(); 
        System.out.println("End test: testWithTimeOutExceptionPrintOnConsole() with output = " + output);
    }

}
