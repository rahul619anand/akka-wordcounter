# akka-wordcounter

A wordcounter application built in akka.

### Working

This wordcounter application is a very basic model to calculate the word count of files.
It reads a directory and calculates the word count of each file in the directory.

The main process involves the following :

    1. Application starts up by seting up the Actor sytem with the actors and sending a scan message to FileScannerActor.
    2. FileScannerActor gets all the files present in the defined directory (i.e. src/main/resources/log) and sends to FileParserActor.
    3. The FileParserActor parses each file and sends stream of all LINES in the file along with a START_OF_FILE and END_OF_FILE events to AggregatorActor.
    4. The AggregatorActor aggregates the wordcount of each file and prints to console when it receives END_OF_FILE event.
    
### Configuration:
   
1. The application can be run in two ways:

      a. Single Mode - Runs a single SCAN across log directory
      
             // default   
             execution-mode = scheduler 
            
      b. Scheduled Mode - Runs every 30 seconds (configurable)       
            
             execution-mode = single 
            

2. The directory to be scanned should be present in the resources classpath. The app supports recursive scans, hence would calculate word count of each file in a directory. 
            
            // default directory, present in src/main/resources
            log-directory = log  
            
Note : Configurations regarding jmx, actor mailbox , dispatchers ,log levels and debugging options can be tuned as and when needed in application.conf             
            
        
### Dependencies Used 
        
1. JUnit 5 and Akka TestKit :  Test Cases for Actors

2. Logback : Logging Framework

Note: TypeSafe Config (Akka Framework dependency) is overriden (application.conf overrides reference.conf of akka) and referred (i.e. loaded by Akka).
        

### Consistency 

Consistency and sequencing of message events ( START_OF_FILE , line, END_OF_FILE ) can be guaranteed by use of Atomic Counter and having actor mailbox with underlying FIFO implementation. 

### Scalability

The above model showcases medium scalability.

A separate dispatcher (i.e. custom-blocking-io-dispatcher in application.conf) for FileParserActor (that parses lines of individual files) is used to not starve the AggregatorActor.
AggregatorActor has a blocking mailbox (i.e. custom-bounded-mailbox in application.conf) to prevent itself from being overwhelmed by messages from FileParserActor. However this can be debatable based on the available memory and system constraints.

The best can be achieved by this model by tuning these configs according to the memory and system constraints.    


### Possibilities of improvement

Using Client Server Model / Akka Streams would much better suit the use case, however sequencing of message events ( START_OF_FILE , line, END_OF_FILE ) should be carefully dealt with. 


### Logs 

All application specific logs will be printed on console as well as in application.log.
Default Log level is INFO 

### Build , Test and Run

To create an executable jar : build/libs/wordcounter-1.0.jar 

    gradle createExecutableJar   
    
To run tests: Test cases are covered for actor classes
    
    gradle test
    
Running the application:
    
    java -jar wordcounter-1.0.jar 
    

### Sample Output 
    
Scanning default :  "src/main/resources/log" containing 10 random sample logs 

        2017-07-03 19:22:05,253 INFO  example.akka.wordcounter.Main - Creating Actor System ...
        2017-07-03 19:22:06,274 INFO  akka.event.slf4j.Slf4jLogger - Slf4jLogger started
        2017-07-03 19:22:06,745 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/sample8.log , Word Count: 46
        2017-07-03 19:22:06,747 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/sample9.log , Word Count: 21
        2017-07-03 19:22:06,835 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/morelogs/sample1.log , Word Count: 34473
        2017-07-03 19:22:06,839 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/sample5.log , Word Count: 25174
        2017-07-03 19:22:06,847 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/morelogs/sample2.log , Word Count: 32461
        2017-07-03 19:22:06,895 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/morelogs/sample3.log , Word Count: 32852
        2017-07-03 19:22:06,906 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/morelogs/sample7.log , Word Count: 34473
        2017-07-03 19:22:06,972 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/sample10.log , Word Count: 34473
        2017-07-03 19:22:06,992 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/sample4.log , Word Count: 34314
        2017-07-03 19:22:06,999 INFO  e.a.w.actors.AggregatorActor - /Users/ranand/github/akka/build/resources/main/log/morelogs/sample6.log , Word Count: 34473
        
        
### Monitoring  
      
      ![cpu samples snapshot -](https://github.com/rahul619anand/akka-wordcounter/blob/master/Cpu_Sample.png)
      ![thread state snapshot -](https://github.com/rahul619anand/akka-wordcounter/blob/master/Thread_Monitor.png)