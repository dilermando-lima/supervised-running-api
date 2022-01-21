# Supervised Running API

## About
This project is a simple API solution to execute any piece of code in a parallel thread and manage number of retrying and number of error on running.

  * [Simple Explanation](#simple-explanation)
    + [How does this work?](#how-does-this-work-)
    + [Type of retries](#type-of-retries)
    + [Execution](#execution)
  * [Documentation API](#documentation-api)
  * [Showing code :)](#showing-code---)
    + [Simple  running](#simple--running)
    + [Running with retrying on **timeout** set up](#running-with-retrying-on---timeout---set-up)
    + [Running with retrying on any **exception running**](#running-with-retrying-on-any---exception-running--)
    + [Running with **log** implementation](#running-with---log---implementation)
    + [Running with throws **timeout exception** handled](#running-with-throws---timeout-exception---handled)
    + [Running with throws **execution exception** handled](#running-with-throws---execution-exception---handled)

## Simple Explanation

### How does this work?
How to manage a running a piece of code, managing safe retries and timeout on it?
Let's see the abstract steps of this API:

  - 1 Instance running
  - 2 Set up supervising ( timeout, log, behavior on exception )
  - 3 Set implementation running ( used functional interface )
  - 4 Running
  - 5 Get result ( handle results from running output )

All exceptions, retries, logs, and running will be managed by API.

### Type of retries
There are 2 retrying flows can be set up on API
  - retry on timeout managing
  - retry on running exception 

### Execution
All running will be executed in a single supervised thread will be manage retries and result.

## Documentation API

All implementation has been placed in `RunSupervised<T>.java` and doesn't require any external libs.

Method                                   | Description        | Comments
---                                      | ---                    | ---
.logDebugImplementation(...)             | Implementation about how to handle log message | If not implemented, log is disabled by default
.setTimeOut(...)                         | Set up timeout to retry running | If not set up, will be retried on 14400 seconds ( 4 hours ) by default
.withNumRetryOnTimeOut(...)              | Set up number of retries on timeout | If not set up will be retried only once ( 1 ) by default
.withNumRetryOnEexception(...)           | Set up number of retries on exception cathing in running | If not set up, will be retried only once ( 1 ) by default
.behaviorOnFinalExceptionTimeOut(...)    | Implementation about how to handle final failed timeout retry | If not implemented, will be thrown exception by default
.behaviorOnFinalExceptionExecution(...)  | Implementation about how to handle final exception running retry | If not implemented, will be thrown exception by default
**.setRunning(...)**                     | Implementation running  | This running will be executed only on calling `.run()`
**.run()**                               | Run Implemented process in `.setRunning(...)` process | The process will be running in a parallel single thread
**.getResult()**                         | Retrieve output result from running implementation | 
.getResultWithDefaultReturnOnFinalExceptionExecution(...) | Default value if reach all retries on execution exception | this works only if has been set up behavior in `.behaviorOnFinalExceptionExecution(...) ` to not throw exception ( by default )
.getResultWithDefaultReturnOnFinalExceptionTimeout(...) | Default value if reach all retries on timeout exception | this works only if has been set up behavior in `.behaviorOnFinalExceptionTimeOut(...) ` to not throw exception ( by default )

## Showing code :)

### Simple  running
Running a piece of code and retrieve result
```java
    String result = new RunSupervised<String>()
            .setRunning(() -> "value from parallel process" ) // set up running implementation
            .run() // running in a parallel managed single thread
            .getResult();

    System.out.println(result);

```


### Running with retrying on **timeout** set up
Running a piece of code **forcing timeout** and supervising retries
```java
    String result = new RunSupervised<String>()
            .withNumRetryOnTimeOut(3)   // number of retries if timeout
            .setTimeOut(5, TimeUnit.SECONDS) // timeout less than running
            .setRunning(()->{
                TimeUnit.SECONDS.sleep(6);  // force exception for timeout
                return "value from parallel process"; 
            })
            .run()
            .getResult();

```

### Running with retrying on any **exception running**
Running a piece of code **forcing exception** and supervising retries
```java
    String result = new RunSupervised<String>()
            .withNumRetryOnEexception(3)  // number of retries if throws any error
            .setRunning(() -> "value from parallel process" + 1/0 ) // force exception
            .run()
            .getResult();
```

### Running with **log** implementation
Running a piece of code and implement **debud log**
```java
    String result = new RunSupervised<String>()
            .logDebugImplementation(System.err::println) // by default log is disable
            .setRunning(() -> "value from parallel process" )
            .run()
            .getResult();
```

### Running with throws **timeout exception** handled
Running a piece of code and **override behavior** when throws error in **timeout exception**
```java
    String result = new RunSupervised<String>()
            .setTimeOut(...)           
            .withNumRetryOnTimeOut(...)                           
            .setRunning(...)
            .run()
            .behaviorOnFinalExceptionTimeOut(RuntimeException::printStackTrace)     // by default will throw RuntimeException
            .getResultWithDefaultReturnOnFinalExceptionTimeout("Final result on error execution") // get result with default value if reach all retries execution exception
```


### Running with throws **execution exception** handled
Running a piece of code and **override behavior** when throws error in **execution exception**
```java
    String result = new RunSupervised<String>()
            .setTimeOut(...)           
            .withNumRetryOnEexception(...)                           
            .setRunning(...)
            .run()
            .behaviorOnFinalExceptionExecution(RuntimeException::printStackTrace)     // by default will throw RuntimeException
            .getResultWithDefaultReturnOnFinalExceptionExecution("Final result on error execution") // get result with default value if reach all retries execution exception
```

