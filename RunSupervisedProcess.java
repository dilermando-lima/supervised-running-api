
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class RunSupervisedProcess<T>{

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
    private long numRetryOnTimeOut;
    private long numRetryOnException;
    private long timeOut;
    private TimeUnit timeOutUnit;
    private ExecutorService executor;
    private long retryOnExceptionTimeOutCurrentValue;
    private long retryOnExceptionExecutionCurrentValue;
    private T result;
    private Consumer<String> logDebugImplementation;


    public RunSupervisedProcess(){
        setUpInitProcess();
    }

    private void logDebug(String message,Object... replacement){
        if( this.logDebugImplementation != null )
            this.logDebugImplementation.accept(String.format(message, replacement));
    }

    public RunSupervisedProcess<T> logDebugImplementation(Consumer<String> logDebugImplementation){
        this.logDebugImplementation = logDebugImplementation;
        return this;
    }

    public RunSupervisedProcess<T> behaviorOnFinalExceptionExecution(Consumer<ExecutionProcessException> handleOnFinalExecuction){
        this.handleOnFinalExecuction = handleOnFinalExecuction;
        return this;
    }

    public RunSupervisedProcess<T> behaviorOnFinalExceptionTimeOut(Consumer<TimeOutProcessException> handleOnFinalTimeOut){
        this.handleOnFinalTimeOut = handleOnFinalTimeOut;
        return this;
    }

    public RunSupervisedProcess<T> setTimeOut(int timeOut, TimeUnit timeUnit){
        this.timeOut = timeOut;
        this.timeOutUnit = timeUnit;
        return this;
    }
    

    public RunSupervisedProcess<T> withNumRetryOnEexception(int numRetryOnEexception){
        this.numRetryOnException = numRetryOnEexception;
        return this;
    }

    public RunSupervisedProcess<T> setRunning(Callable<T> callable){
        this.callable = callable;
        return this;
    }

    public RunSupervisedProcess<T> withNumRetryOnTimeOut(int numRetryOnTimeOut){
        this.numRetryOnTimeOut = numRetryOnTimeOut;
        return this;
    }



    private void setUpInitProcess(){
        this.retryOnExceptionTimeOutCurrentValue = 1;
        this.retryOnExceptionExecutionCurrentValue= 1;
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.result = null;
        this.handleOnFinalExecuction = exception -> { throw new ExecutionProcessException(exception); };
        this.handleOnFinalTimeOut = exception -> { throw new TimeOutProcessException(exception); };
        this.logDebugImplementation = null;
    }


    public Result run(){

        try{
            logDebug("Starting managed paralel single thread");
            this.result = execute();
            logDebug("Ending process WITH NO ERRORS");
        }catch(TimeoutException execptionTimeoutException){
            logDebug("Ending process WITH TimeoutException final error");
            this.exceptionOnFinalTimeOut =  new TimeOutProcessException(execptionTimeoutException);
        }catch(ExecutionException execptionExecutionException){
            logDebug("Ending process WITH ExecutionException final error");
            this.exceptionOnFinalExecution = new ExecutionProcessException(execptionExecutionException);
        }  
        finally{
            this.executor.shutdown();
        }
        
        return new Result();
    }

    private T execute() throws TimeoutException, ExecutionException {

        logDebug("Running %s of %s on timeout_exception_retrying", retryOnExceptionTimeOutCurrentValue, numRetryOnTimeOut);
        logDebug("Running %s of %s on execution_exception_retrying", retryOnExceptionExecutionCurrentValue, numRetryOnException);

        Future<T> feature = null;
        try {

            feature = executor.submit(callable);

            return feature.get(timeOut + 1, timeOutUnit);

        } catch (TimeoutException exceptionTimeOut) {
            logDebug("TimeOutException %s of %s",  retryOnExceptionTimeOutCurrentValue, numRetryOnTimeOut);
            checkRetryOnExceptionTimeOut(exceptionTimeOut);
            feature.cancel(true);
            execute();
            
        } catch (ExecutionException exceptionExecution) {
            logDebug("ExecutionException %s of %s", retryOnExceptionExecutionCurrentValue, numRetryOnException);
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
    