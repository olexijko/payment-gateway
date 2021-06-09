### To start the application:

* Install OpenJDK(HotSpot) 11 LTS or later (https://adoptopenjdk.net/).
* Install Postgresql 9.2 or later. Start db server on port 5432 and create **payment_gateway** database.
* Set correct db username/password in **src/main/resources/application.properties** configuration file.
* Run **mvn clean spring-boot:run** in project root folder to start the application.
* Alternatively you could create and start a new Application Run/Debug Configuration in your IDE. Main class - 
* **com.olexijko.paymentgw.PaymentGatewayApplication**
* Go to http://localhost:8080/api-docs to open Swagger documentation about available API endpoints.
* Run **mvn clean test** in project root folder to execute unit tests
