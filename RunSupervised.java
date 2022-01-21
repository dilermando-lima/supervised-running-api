
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RunSupervised<T>{

    public static class TimeOutProcessException extends RuntimeException{
        public TimeOutProcessException(TimeOutProcessException e){ super(e); }
        public TimeOutProcessException(TimeoutException e){ super(e); }
    }
    public static class ExecutionProcessException extends RuntimeException{
        public ExecutionProcessException(ExecutionProcessException e){ super(e); }
        public ExecutionProcessException(ExecutionException e){ super(e); }
    }
    public static class NotSupervidedException extends RuntimeException{
        public NotSupervidedException(NotSupervidedException e){ super(e); }
        public NotSupervidedException(InterruptedException e){ super(e); }
    }
    
    public class Result{

        public T getResult(){
            return  getResult(null, null);
        }

        public T getResultWithDefaultReturn(T defaultReturnOnFinalException){
            return  getResult(defaultReturnOnFinalException, defaultReturnOnFinalException);
        }

        public T getResultWithDefaultReturnOnFinalExceptionTimeout(T defaultReturnOnFinalTimeoutException){
            return  getResult(null, defaultReturnOnFinalTimeoutException);
        }

        public T getResultWithDefaultReturnOnFinalExceptionExecution(T defaultReturnOnFinalExecutionException){
            return  getResult(defaultReturnOnFinalExecutionException, null);
        }
    
        private T getResult( T defaultValueOnFinalExecutionException, T defaultValueOnFinalTimeOutException ){

            if( exceptionOnFinalExecution != null){
                handleOnFinalExecuction.accept(exceptionOnFinalExecution);
                return defaultValueOnFinalExecutionException;
            }

            
            if( exceptionOnFinalTimeOut != null){
                handleOnFinalTimeOut.accept(exceptionOnFinalTimeOut);
                return defaultValueOnFinalTimeOutException;
            }

            return result;
        }
    }

    private Callable<T> callable;
    private ExecutionProcessException exceptionOnFinalExecution;
    private TimeOutProcessException exceptionOnFinalTimeOut;
    private Consumer<ExecutionProcessException> handleOnFinalExecuction;
    private Consumer<TimeOutProcessException> handleOnFinalTimeOut;
    private int numRetryOnTimeOut;
    private int numRetryOnException;
    private long timeOut;
    private TimeUnit timeOutUnit;
    private ExecutorService executor;
    private int retryOnExceptionTimeOutCurrentValue;
    private int retryOnExceptionExecutionCurrentValue;
    private T result;
    private Consumer<String> logDebugImplementation;


    public RunSupervised(){
        setUpInitRunning();
    }

    private void logDebug(String message,Object... replacement){
        if( this.logDebugImplementation != null )
            this.logDebugImplementation.accept(String.format(message, replacement));
    }

    public RunSupervised<T> logDebugImplementation(Consumer<String> logDebugImplementation){
        this.logDebugImplementation = logDebugImplementation;
        return this;
    }

    public RunSupervised<T> behaviorOnFinalExceptionExecution(Consumer<ExecutionProcessException> handleOnFinalExecuction){
        this.handleOnFinalExecuction = handleOnFinalExecuction;
        return this;
    }

    public RunSupervised<T> behaviorOnFinalExceptionTimeOut(Consumer<TimeOutProcessException> handleOnFinalTimeOut){
        this.handleOnFinalTimeOut = handleOnFinalTimeOut;
        return this;
    }

    public RunSupervised<T> setTimeOut(long timeOut, TimeUnit timeUnit){
        this.timeOut = timeOut;
        this.timeOutUnit = timeUnit;
        return this;
    }
    

    public RunSupervised<T> withNumRetryOnEexception(int numRetryOnException){
        this.numRetryOnException = numRetryOnException;
        return this;
    }

    public RunSupervised<T> setRunning(Callable<T> callable){
        this.callable = callable;
        return this;
    }

    public RunSupervised<T> withNumRetryOnTimeOut(int numRetryOnTimeOut){
        this.numRetryOnTimeOut = numRetryOnTimeOut;
        return this;
    }



    private void setUpInitRunning(){
        this.executor = null;
        this.numRetryOnTimeOut = 1;
        this.numRetryOnException = 1;
        this.timeOut = 14400; // 4 hours
        this.timeOutUnit = TimeUnit.SECONDS;
        this.handleOnFinalExecuction = exception -> { throw new ExecutionProcessException(exception); };
        this.handleOnFinalTimeOut = exception -> { throw new TimeOutProcessException(exception); };
        this.logDebugImplementation = null;
    }

    private void setUpEachCallingRunning(){

        this.retryOnExceptionTimeOutCurrentValue = 1;
        this.retryOnExceptionExecutionCurrentValue= 1;
        this.result = null;
    }


    private void setUpEachCallingExecute(){
        if(  this.executor != null ){
            this.executor.shutdownNow();
        }
        this.executor = Executors.newSingleThreadExecutor();
    }

    public Result run(){

        setUpEachCallingRunning();

        try{
            logDebug("Starting managed parallel single thread");
            this.result = execute();
        }catch(TimeOutProcessException execptionTimeoutException){
            logDebug("Ending process WITH TimeoutException final error");
            this.exceptionOnFinalTimeOut =  new TimeOutProcessException(execptionTimeoutException);
        }catch(ExecutionProcessException execptionExecutionException){
            logDebug("Ending process WITH ExecutionException final error");
            this.exceptionOnFinalExecution = new ExecutionProcessException(execptionExecutionException);
        }  
        finally{
            this.executor.shutdown();
        }
        
        return new Result();
    }

    private T execute() throws TimeOutProcessException, ExecutionProcessException {

        logDebug("\tRunning %s of %s on timeout_exception_retrying", retryOnExceptionTimeOutCurrentValue, numRetryOnTimeOut);
        logDebug("\tRunning %s of %s on execution_exception_retrying", retryOnExceptionExecutionCurrentValue, numRetryOnException);

        setUpEachCallingExecute();

        Future<T> feature = null;
        try {

            feature = executor.submit(callable);
            logDebug("\tWaiting running");
            return feature.get(timeOut + 1, timeOutUnit);
            
        } catch (TimeoutException exceptionTimeOut) {
            logDebug("Error TimeOutException %s of %s",  retryOnExceptionTimeOutCurrentValue, numRetryOnTimeOut);
            checkRetryOnExceptionTimeOut(exceptionTimeOut);
            feature.cancel(true);
            execute();
            
        } catch (ExecutionException exceptionExecution) {
            logDebug("Error ExecutionException %s of %s", retryOnExceptionExecutionCurrentValue, numRetryOnException);
            checkRetryOnExceptionExecution(exceptionExecution);
            feature.cancel(true);
            execute();
        } catch (InterruptedException e) {
            feature.cancel(true);
            Thread.currentThread().interrupt();
            throw new NotSupervidedException(e);
        } 

        return null;
    } 

    private void checkRetryOnExceptionTimeOut(TimeoutException exceptionTimeOut){
        if( retryOnExceptionTimeOutCurrentValue == numRetryOnTimeOut  ){
            executor.shutdownNow();
            throw new TimeOutProcessException(exceptionTimeOut);
        }else{
            retryOnExceptionTimeOutCurrentValue++;
        }
    }

    private void checkRetryOnExceptionExecution(ExecutionException exceptionExecution){
        if( retryOnExceptionExecutionCurrentValue == numRetryOnException  ){
            executor.shutdownNow();
            throw new ExecutionProcessException(exceptionExecution);
        }else{
            retryOnExceptionExecutionCurrentValue++;
        }
    }

}
    