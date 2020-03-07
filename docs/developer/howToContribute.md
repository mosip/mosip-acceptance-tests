# Where to contribute

<span style="color:red">**</span>
_For better understanding, please go through the [Basics](basics.md) before going through this document_


We are actively seeking contributions in the following parts:
* Writing new step classes to test Rest API-based functionality for MOSIP's pre-registration, registration-processor, id-authentication and kernel apis


# How to contribute

Every Rest API-based step class contains three functions:
* prepare (code for preparing the data for API call)
* call (code for calling the API)
* process (code for analysing the response of API call)

### 1) Enhancing functionality of existing step
Lets start with most simple step class: ivv-preregistration->methods->SendOTP

**SendOTP**

Create a send OTP HTTP request and sends it to server for getting an OTP in the email

Data required:
* email id of person (store.currentPerson.userid)

Check the SendOTP class for prepare, call and process functions. You can add new code to enhance the functionality of step class.

### 2) Adding new functionality (creating new step classes)
* In order to create new step class, you can take reference of existing step classes that implements similar functionality.
* Identify the request parameters and urls from the MOSIP documentation
* write code inside prepare, call and process function

### How to write unit test for step classes
* In order to create a test for existing step class or new step class. You can take reference of existing test of send otp test in test folder of ivv-preregistration module
* Follow the comments in the sendOTPTest class to modify code according to the needs
* In unit tests, we check only the prepare & process functions
* You need to create request and response json templates in the test resources's requests and responses folder
