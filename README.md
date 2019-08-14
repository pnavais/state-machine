# state-machine
Generic State Machine implementation for Java 8+
<p align="center">
    <a href="https://travis-ci.org/pnavais/state-machine">
        <img src="https://img.shields.io/travis/pnavais/state-machine.svg"
             alt="Build Status"/>
    </a>
    <a href="https://coveralls.io/github/pnavais/state-machine?branch=master">
        <img src="https://img.shields.io/coveralls/pnavais/state-machine.svg"
             alt="Coverage"/>
    </a>
     <a href="LICENSE">
       <img src="https://img.shields.io/github/license/pnavais/state-machine.svg"
            alt="License"/>         
    </a>
    <a href="https://sonarcloud.io/dashboard/index/org.payball:state-machine">
        <img src="https://sonarcloud.io/api/project_badges/measure?project=org.payball:state-machine&metric=alert_status"
             alt="Quality Gate"/>
    </a>
</p>

## Basic usage

```java
StateMachine stateMachine = StateMachine.newBuilder()
                .from("A").to("B").on("1")
                .from("B").to("C").on("2")                
                .build()
 ```
 
Creates a new State Machine as per the following diagram : 

![alt text](simple_graph.png "Logo Title Text 1")

 
 ## Advanced usage
 ### Self loops
