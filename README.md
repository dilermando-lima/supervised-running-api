# Supervised Running API

## About
This project is a simple API solution to execute any piece of code in a parallel thread and manage number of retrying and number of error on running.

  * [Simple  running](#simple--running)
  * [Set up and Running code](#set-up-and-running-code)
    + [How does this work?](#how-does-this-work-)
    + [Step 1: Instance running](#step-1--instance-running)
    + [Step 2: Set up supervising](#step-2--set-up-supervising)
    + [Step 3: Set implementation running](#step-3--set-implementation-running)
    + [Step 4: Running](#step-4--running)
    + [Step 5: Get result](#step-5--get-result)
  * [Documentation API](#documentation-api)
  * [Showing more code :)](#showing-more-code---)
    + [Running code setting up retries on **timeout**](#running-code-setting-up-retries-on---timeout--)
    + [Running code setting up retries on any **exception running**](#running-code-setting-up-retries-on-any---exception-running--)
    + [Running with **log** implementation](#running-with---log---implementation)
    + [Running code handling timeout e execution exception](#running-code-handling-timeout-e-execution-exception)
    + [Running code setting up delay on retries](#running-code-setting-up-delay-on-retries)


## Simple  running
Running a piece of code and retrieve result
```java
  String result = new RunSupervised<String>()
          // set up running implementation
          .setRunning(() -> "value from parallel process" )
          // running in a parallel managed single thread
          .run() 
          .getResult();

```

## Set up and Running code

### How does this work?
How to manage a running a piece of code on safe timeout retries on it and handling all exception throwing?

All exceptions, retries, logs, and running will be managed by API.

There are 2 retrying flows can be set up on API
  - retry on timeout managing
  - retry on running exception 

All running will be executed in a single supervised that thread will manage retries and result.

Let's see the steps of this API:

### Step 1: Instance running
```java
  new RunSupervised<String>()
```
### Step 2: Set up supervising
```java
  .logDebugImplementation(System.err::println) // log is disabled by default
  .setTimeOut(2, TimeUnit.SECONDS)    // 14400 seconds ( 4 hours ) by default
  .withMaxRetryOnTimeOut(3)           // only once ( 1 ) by default
  .withMaxRetryOnException(3)         // only once  ( 1 ) by default
  .setDelayOnEachExecutionExceptionRetry()  // there is no delay by default 
  .setDelayOnEachTimeOutExceptionRetry()    // there is no delay by default 
```

### Step 3: Set implementation running
```java
// setRunning() receive a Callable<T> as argument returning same type to RunSupervised<T>
.setRunning(() -> {  
      return allMyRunninCodeReturnTypeOfRunSupervised();
  })
```
### Step 4: Running
```java
new RunSupervised<String>()
  .setTimeOut(...)
  .withMaxRetryOnTimeOut(...)
  // All running will be executed in a single supervised thread that will manage retries, exceptions and result
  .run()
```
### Step 5: Get result
```java
  // only get resut from running and let errors be thrown
  .run().getResult(); 

  // OR

  // get resut and handle throwing final timeout and final exception
  .run()
  .getResultHandlingThrows(
    (exception) -> { exception.printStackTrace(); return null; },// handleFinalExecutionException
    (exception) -> { exception.printStackTrace(); return null; } // handleFinalTimeoutException
  ); 
```

## Documentation API

All implementation has been placed in `RunSupervised<T>.java` and doesn't require any external libs.

Method                         | Description                         | Comments
---                            | ---                                 | ---
**.setRunning(...)**           | Implementation running              | This running will be executed only on calling `.run()`
**.run()**                     | Run Implemented process in `.setRunning(...)` process | The process will be running in a parallel single thread
**.getResult()**               | Retrieve output result from running implementation | Final failed timeout and final exception will be thrown
.logDebugImplementation(...)   | Implementation about how to handle log message | If not implemented, log is disabled by default
.setTimeOut(...)               | Set up timeout to retry running     | If not set up, will be retried on 14400 seconds ( 4 hours ) by default
.withMaxRetryOnTimeOut(...)    | Set up number of retries on timeout | If not set up will be retried only once ( 1 ) by default
.withMaxRetryOnException(...)  | Set up number of retries on exception cathing in running | If not set up, will be retried only once ( 1 ) by default
.getResultHandlingThrows(...)  | Implementation for final failed timeout and final exception running retry |  
.setDelayOnEachTimeOutExceptionRetry(...)  | Set up delay on each retry comes from timeout exception | There is no delay by default
.setDelayOnEachExecutionExceptionRetry(...)  |  Set up delay on each retry comes from execution exception | There is no delay by default

## Showing more code :)

### Running code setting up retries on **timeout**
Running a piece of code **forcing timeout** and supervising retries
```java
  String result = new RunSupervised<String>()
          .withMaxRetryOnTimeOut(3)   // number of retries if timeout
          .setTimeOut(5, TimeUnit.SECONDS) // timeout less than running implementation
          .setRunning(()->{
              TimeUnit.SECONDS.sleep(6);  // force exception for timeout
              return "value from parallel process"; 
          })
          .run()
          .getResult();
```

### Running code setting up retries on any **exception running**
Running a piece of code **forcing exception** and supervising retries
```java
  String result = new RunSupervised<String>()
          .withMaxRetryOnException(3)  // number of retries if throws any error
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

### Running code handling timeout e execution exception
Running a piece of code and **override behavior** when throws error in **timeout exception**
```java

  // implement handling exceptions
  Function<RunSupervised.TimeOutProcessException,String> handleFinalTimeoutException = 
      (exception) -> { exception.printStackTrace(); return null; };

  Function<RunSupervised.ExecutionProcessException,String> handleFinalExecutionException = 
      (exception) -> { exception.printStackTrace(); return ""; };

  // get result on exceptions handling
  String result = new RunSupervised<String>()                       
          .setRunning(...)
          .run()
          .getResultHandlingThrows(
              handleFinalExecutionException,
              handleFinalTimeoutException
          );
```

### Running code setting up delay on retries
Running a piece of code setting up **delay** on each retry
```java
  String result = new RunSupervised<String>()
          .withMaxRetryOnException(3)
          .withMaxRetryOnTimeOut(3)

          // delay on each execution exception retry
          .setDelayOnEachExecutionExceptionRetry(10, TimeUnit.SECONDS)
          // delay on each timeout exception retry
          .setDelayOnEachTimeOutExceptionRetry(10, TimeUnit.SECONDS)

          .setRunning(...)
          .run()
          .getResult();
```
