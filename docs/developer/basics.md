# Basics
This document contains the basics

# Modules

### IVV Core
Module contains the common utilities, DTOs, etc 

### IVV Parser
Module to parse data sheets (persona, scenario, etc) and convert them to data objects

### IVV Test data generator
Module to mutate/ randomize persona data to avoid interference between the different scenarios.

### IVV Orchestrator
It's a module which contains code to run scenarios, returning results, logs, errors.

### IVV Mutators
It's a module which contains code to change scenario data during runtime

### IVV Preregistration
Contains Rest API-based step classes related to MOSIP's pre-registration module

### IVV Registration
Contains the Java Servie-based step classes related to MOSIP's registration module

### IVV Registration Processor
Contains Rest API-based step classes related to MOSIP's registration-processor module

### IVV Kernel
Contains Rest API-based step classes related to MOSIP's kernel module

### IVV IDA
Contains Rest API-based step classes related to MOSIP's id-authentication module


# Terminalogy

### DSL
Domain specific language (DSL) is a computer language specialized to a particular application domain. We have developed a simple DSL to test MOSIP's specific functionalities. We maintain a list of DSL [here](./../DSL.md)

### Persona
A persona is the one who want to use MOSIP.

We have multiple Personas to test various cases:
* Male adult
* Female adult
* Head of family (HOF), contains adult male/female & at least one child 

A person may contain a single/ multiple persons, like in case of HOF, we have multiple persons.


### Registration user
person who operates at registration center.

### Partner
person or organisation which uses MOSIP's for authentication, eKYC, etc

### step class
It a Java class that implements the functionality of the associated DSL

### Scenario data
It contains the persona data, registration user data, partner data

### Configs
Configuration related properties

### Configs
Global constants related properties

## Store
It contains scenario data, configs, globals

### Scenario
A scenario contains store, list of steps.



