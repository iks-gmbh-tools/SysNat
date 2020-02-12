<style>

body {
  background: linear-gradient(90deg, white, lightyellow);
}

h1 {
  background: lightgray;
}

p.testphase {
  color: blue;
}

/* 5 title lines */

table:nth-of-type(1) tr:nth-of-type(1){
  background: #0d73ff;
  font-size: 40px;
  border: solid 1px #0d73ff;
}

table:nth-of-type(1) tr:nth-of-type(2){
  background: #0d73ff;
  font-size: 28px;
  border: solid 1px #0d73ff;
}

table:nth-of-type(1) tr:nth-of-type(3){
  background: #0d73ff;
  font-size: 40px;
  border: solid 1px #0d73ff;
}

table:nth-of-type(1) tr:nth-of-type(4){
  background: #0d73ff;
  font-size: 20px;
  border: solid 1px #0d73ff;
}

table:nth-of-type(1) tr:nth-of-type(5){
  background: #0d73ff;
  font-size: 20px;
  border: solid 1px #0d73ff;
}

/* data tables */
table {
    table-layout:fixed;
    width:100%;
    border: solid 2px black;	 /* Tabellenrahmen */
    border-collapse: collapse;
}

table tr:nth-of-type(odd) {
  background: lightgrey;
}
table tr:nth-of-type(even) {
  background: #efefef;
}


</style>
|   |
|:-:|
|**System Beschreibung**|
|für|
|***HelloWorldSpringBoot***|
|mit Hilfe von *Pandoc* |
|erzeugt am 11.02.2020 um 13:28|

# Introduction

This "application" represents actually a Hompage. As such it needs no login and is used to demonstrate SysNat testing a test application without login. In addition, it is used to demonstrate the download of a PDF file and its easy content validation with SysNat's *DocVal*-Framework.


# GreetingWithTableTestData








  TestData:  
  Datasets in columns  
  
  |   |   |  
  |---|---|  
  |Name|Susi|  
  |Greeting|Hi|  
  |GreetingResult|Hi Susi!|  



<p class="testphase">**Arrange**</p>  "EnterGreetingData".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "::GreetingResult"?  



# GreetingWithTableTestParameter








  Test-Parameter:  
  Datasets in columns  
  
  |   |   |  
  |---|---|  
  |Name|Susi|Bob|  
  |Greeting|Hi|Hello|  
  |GreetingResult|Hi Susi!|Hello Bob!|  



<p class="testphase">**Arrange**</p>  "EnterGreetingData".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "::GreetingResult"?  



# ScriptBasedGreeting









<p class="testphase">**Arrange**</p>  Execute with  "Name" = "John"  "Greeting" = "Hi"  the script "EnterGreetingData".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "Hi John!"?  



# SimpleGreeting









<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "Hello World!"?  



# TestDataBasedGreeting1








  TestData: SingleGreetingData  



<p class="testphase">**Arrange**</p>  "EnterGreetingData".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "Hey Elisabeth!"?  



# TestDataBasedGreeting2








  TestData: HelloWorldTestData  



<p class="testphase">**Arrange**</p>  Select "HelloWorldTestData_GreetingValidationData1::Greeting" in selection field "Greeting".  Enter "HelloWorldTestData_GreetingValidationData1::Name" in text field "Name".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "HelloWorldTestData_GreetingValidationData1::GreetingResult"?  



# TestDataBasedGreeting3








  TestData: MultipleGreetingData_1  
  TestData: MultipleValidationData_1  



<p class="testphase">**Arrange**</p>  Select "MultipleGreetingData_1::Greeting" in selection field "Greeting".  Enter "MultipleGreetingData_1::Name" in text field "Name".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "MultipleValidationData_1::GreetingResult"?  



# TestDataBasedGreeting4








  TestData: MultipleGreetingData  



<p class="testphase">**Arrange**</p>  Select "MultipleGreetingData_1::Greeting" in selection field "Greeting".  Enter "MultipleGreetingData_1::Name" in text field "Name".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "Hi Tim!"?  

***

<p class="testphase">**Arrange**</p>  Click menu item "Form Page".  Select "MultipleGreetingData_2::Greeting" in selection field "Greeting".  Enter "MultipleGreetingData_2::Name" in text field "Name".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "Hello Rose!"?  

***

<p class="testphase">**Arrange**</p>  Click menu item "Form Page".  Select "MultipleGreetingData_3::Greeting" in selection field "Greeting".  Enter "MultipleGreetingData_3::Name" in text field "Name".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "Hi Abdul!"?  



# TestDataBasedGreeting5








  TestData: HelloWorldTestData_GreetingValidationData1  



<p class="testphase">**Arrange**</p>  "EnterGreetingData".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "::GreetingResult"?  



# TestParameterBasedGreeting








  Test-Parameter: HelloWorldTestData  



<p class="testphase">**Arrange**</p>  "EnterGreetingData".  



<p class="testphase">**Act**</p>  Click button "Greet".  



<p class="testphase">**Assert**</p>  Is the displayed text "GreetingResult" equal to "HelloWorldTestData::GreetingResult"?  



# FailedLogin









<p class="testphase">**Arrange**</p>  Is page "Form Page" visible?  Click menu item "Logout".  Is page "Login Page" visible?  



<p class="testphase">**Act**</p>  Login with "Peter", "wrongPW".  



<p class="testphase">**Assert**</p>  Is page "Error Page" visible?  Is the displayed text "ErrorMessage" equal to "Invalid User Login Data."?  



# Relogin









<p class="testphase">**Arrange**</p>  Is page "Form Page" visible?  Click menu item "Logout".  Is page "Login Page" visible?  



<p class="testphase">**Act**</p>  Relogin.  



<p class="testphase">**Assert**</p>  Is page "Form Page" visible?  



# Smoketest









Is page "Form Page" visible?