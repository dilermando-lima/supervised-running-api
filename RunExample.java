import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@SuppressWarnings({"java:S106"}) // ignore sonarlit warning on log with System...println
public class RunExample {

    public static final long DEFAULT_SETUP_TIMEOUT = 5L;
    public static final TimeUnit DEFAULT_SETUP_TIMEOUT_UNIT = TimeUnit.SECONDS;
    public static final int DEFAULT_SETUP_NUM_RETRY_TIME_OUT = 3;
    public static final int DEFAULT_SETUP_NUM_RETRY_EXCEPTION = 3;

    
    public static void main(String[] args) {

        var runSupervisedInstance = getStandardSetupRunSupervisedToAllTest();
        
        runSupervisedInstance.logDebugImplementation(System.err::println);    // by default log is disabled

        testWithNoErrors(runSupervisedInstance);
        try{
            testWithExecutionException(runSupervisedInstance);
        }catch(RuntimeException e){
            System.out.println("Error thrown on testWithExecutionException()");
        }
        try{
            testWithTimeOutException(runSupervisedInstance);
        }catch(RuntimeException e){
            System.out.println("Error thrown on testWithTimeOutException()");
        }

        try{
            testWithTimeOutExceptionPrintOnConsole(runSupervisedInstance);
        }catch(RuntimeException e){
            System.out.println("Error thrown on testWithTimeOutExceptionPrintOnConsole()");
        }
        /* 
        // NEVER DO THIS becaouse sub thread will not be killed on timeout retring
        try{
            testWithTimeOutExceptionPrintOnConsoleWithSubThreadRunning(runSupervisedInstance);
        }catch(RuntimeException e){
            System.out.println("Error thrown on testWithTimeOutExceptionPrintOnConsoleWithSubThreadRunning()");
        }
        */


        testHandlingExecutionException(runSupervisedInstance);
        testHandlingTimeOutException(runSupervisedInstance);
        
        try{
            testWithDelayTimeOutException(runSupervisedInstance);
        }catch(RuntimeException e){
            System.out.println("Error thrown on testWithDelayExcecutionException()");
        }

        try{
            testWithDelayExcecutionException(runSupervisedInstance);
        }catch(RuntimeException e){
            System.out.println("Error thrown on testWithDelayExcecutionException()");
        }
      
    }

    private static  RunSupervised<String> getStandardSetupRunSupervisedToAllTest(){
        return new RunSupervised<String>()
            .setTimeOut(DEFAULT_SETUP_TIMEOUT, DEFAULT_SETUP_TIMEOUT_UNIT)            // by default 14400 seconds ( 4 hours )
            .withMaxRetryOnTimeOut(DEFAULT_SETUP_NUM_RETRY_TIME_OUT)                   // by default only once ( 1 )
            .withMaxRetryOnException(DEFAULT_SETUP_NUM_RETRY_EXCEPTION);            // by default only once  ( 1 )
    }


    private static void testWithNoErrors(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithNoErrors() ");
        String output = runSupervisedInstance
            .setRunning(() -> "value testWithNoErrors()")
            .run()
                .getResult(); 
        System.out.println("End test: testWithNoErrors() with output = " + output);
    }



    private static void testWithExecutionException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithExecutionException() ");
        String output = runSupervisedInstance
            .setRunning(() -> "value testWithExecutionException()" + 1/0 )
            .run()
                .getResult(); 
        System.out.println("End test: testWithExecutionException() with output = " + output);
    }


    private static void testHandlingExecutionException(RunSupervised<String> runSupervisedInstance){

        Function<RunSupervised.TimeOutProcessException,String> handleFinalTimeoutException = (exception) -> { exception.printStackTrace(); return null; };
        Function<RunSupervised.ExecutionProcessException,String> handleFinalExecutionException = (exception) -> { exception.printStackTrace(); return null; };

        System.out.println("Start test: testHandlingExecutionException() ");
        String output = runSupervisedInstance
            .setRunning(() -> "value testHandlingExecutionException()" + 1/0 )
            .run()
                .getResultHandlingThrows(handleFinalExecutionException,handleFinalTimeoutException); 
        System.out.println("End test: testHandlingExecutionException() with output = " + output);
    }

    private static void testHandlingTimeOutException(RunSupervised<String> runSupervisedInstance){

        Function<RunSupervised.TimeOutProcessException,String> handleFinalTimeoutException = (exception) -> { exception.printStackTrace(); return null; };
        Function<RunSupervised.ExecutionProcessException,String> handleFinalExecutionException = (exception) -> { exception.printStackTrace(); return null; };

        System.out.println("Start test: testHandlingTimeOutException() ");
        String output = runSupervisedInstance
            .setRunning(() ->{
                TimeUnit.SECONDS.sleep(7); 
                return  "value testHandlingTimeOutException()"; 
            })
            .run()
                .getResultHandlingThrows(handleFinalExecutionException,handleFinalTimeoutException); 
        System.out.println("End test: testHandlingTimeOutException() with output = " + output);
    }

    private static void testWithTimeOutException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithTimeOutException() ");
        String output = runSupervisedInstance
            .setRunning(() ->{
                    
                TimeUnit.SECONDS.sleep(7); 
                return  "value testWithTimeOutException()"; 
            })
            .run()
                .getResult(); 
        System.out.println("End test: testWithTimeOutException() with output = " + output);
    }


    private static void testWithDelayTimeOutException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithDelayTimeOutException() ");
        String output = runSupervisedInstance
            .setDelayOnEachTimeOutExceptionRetry(10, TimeUnit.SECONDS)
            .setRunning(() ->{
                    
                TimeUnit.SECONDS.sleep(7); 
                return  "value testWithDelayTimeOutException()"; 
            })
            .run()
                .getResult(); 
        System.out.println("End test: testWithDelayTimeOutException() with output = " + output);
    }


    private static void testWithDelayExcecutionException(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithTimeOutException() ");
        String output = runSupervisedInstance
            .setDelayOnEachExecutionExceptionRetry(10, TimeUnit.SECONDS)
            .setRunning(() ->{
                    
                TimeUnit.SECONDS.sleep(7); 
                return  "value testWithDelayExcecutionException()"; 
            })
            .run()
                .getResult(); 
        System.out.println("End test: testWithTimeOutException() with output = " + output);
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
                return  "value testWithTimeOutExceptionPrintOnConsole()"; 
            })
            .run()
                .getResult(); 
        System.out.println("End test: testWithTimeOutExceptionPrintOnConsole() with output = " + output);
    }

    /* ==== NEVER DO THIS ============== */
    /*
    private static void testWithTimeOutExceptionPrintOnConsoleWithSubThreadRunning(RunSupervised<String> runSupervisedInstance){
        System.out.println("Start test: testWithTimeOutExceptionPrintOnConsoleWithSubThreadRunning() ");
        String output = runSupervisedInstance
            .setRunning(() ->{

                new Thread(() ->{
                          
                    int increment = 0;
                    while(increment <= 1000){
                        try {
                            TimeUnit.MILLISECONDS.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        } 
                        System.err.println("\t\t\t\tprocessing: in sub-thread" + Thread.currentThread().getId());
                        increment++;
                    }

                }).start();

                TimeUnit.SECONDS.sleep(10);

                return  "value testWithTimeOutExceptionPrintOnConsoleWithSubThreadRunning()"; 
            })
            .run()
                .getResult(); 
        System.out.println("End test: testWithTimeOutExceptionPrintOnConsoleWithSubThreadRunning() with output = " + output);
    }
    */

}
