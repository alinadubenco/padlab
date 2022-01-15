# Lab 1: Web Proxy
This branch contains laboratory 1.
For other laboratories, please see the corresponding branch:
1. Lab1 -> branch "lab1"
2. Lab2 -> branch "lab2"

## System funnctionality
This is the implementation of a simple an e-commerce system which will handle the ordering and warehouse operations.

There are 2 types of possible users of the system, each having a list of operations that they can perform:

1. Manager - This is the manager (or employee) of the e-commerce. He can perform the following operations:
	a. searchProducts - search for the existing products
	b. getProduct - get the complete information about one product
	c. addProduct - add the definition of a new product
	d. updateProduct - update the definition of an existing product
	e. supplyProducts - supply products to the warehouse (add quantities of products)
	f. getSalesReport - get information about what quantities of which products have been sold, for which price and the total sales amount	
2. Customer - This is the customer of the e-commerce. He can perform the following operations:
	a. searchProducts - search for the existing products
	b. getProduct - get the complete information about one product
	c. placeOrder - place a purcase order
	d. searchOrders - search the orders placed earlier
	
## System architecture
Our e-commerce system will consyst of 2 microservices: 
1. Warehouse - which will be used to handle the information of the products and available quantities.
2. Ordering - which will be used to perform the sales and create the sales report

Since we expect a big load on our e-commerce system, we will nee to have multiple instances of each microservice.

In order to do the load-balancing between these instances we will implement a Gateway.

To further reduce the load on the microservices, we will implement a Cache which will store for a short period of time the responses for the "GET" requests.

Here is a diagram depicting the high-level interaction between the components of our e-commerce system:
![alt text](img/sysInteraction.png "E-Commerce components interaction")

Here is the sequence diagram depicting the basic flow of a shopping session.
![alt text](img/sdShopping.png "Shopping sequence diagram")

### Warehouse microservice
This microservice will handle all the warehouse-related operations.

#### The following technologies will be used:
1. Programming language: Java11
2. Frameworks: Spring Boot, Spring Web, JPA
3. Database: MySql
4. Protocol: HTTP, REST APIs

#### The REST APIs
The Path of all the REST APIs of this microservice will start with /warehouse
This microservice will expose the following REST APIs:

1. searchProducts: search for the existing products
	- Method: GET  
	- Path: /warehouse/products/  
	- Parameters:  

|Name           |Location         | Type             | Description                                       |
|---------------|-----------------|------------------|---------------------------------------------------|
| category      | query           | URL-encoded text | The API will return only the products from the specified category                                        |
| text          | query           | URL-encoded text | The API will return only the products that have the specified text either in name or in description      |
| priceMin      | query           | number           | The API will return only the products whose price is higher than or equal to the specified value         |
| priceMax      | query           | number           | The API will return only the products whose price is lower that or equal to the specified value          |

	- Successful result:
		* Status Code: 200
		* Content Type: application/json
		* Body: JSON object containing a list of products matching the search criteria. Each product in the list will contain the following properties: 
		id, name, category, price, quantity

2. getProduct: get the complete information about one product
	- Method: GET  
	- Path: /warehouse/products/  
	- Parameters:  

|Name           |Location         | Type             | Description                                       |
|---------------|-----------------|------------------|---------------------------------------------------|
| id            | query           | number           | The ID of the product to be retrieved             |

	- Successful result:
		* Status Code: 200
		* Content Type: application/json
		* Body: JSON object containing the following properties: id, name, category, price, quantitydescription, characteristics, version

3. addProduct: add the definition of a new product
	- Method: POST  
	- Path: /warehouse/products/  
	- Body: JSON object containing all the information about a product definition: name, category, price, quantitydescription, characteristics    
	- Successful result:
		* Status Code: 200
		* Content Type: application/json
		* Body: JSON object containing the following properties: id, name, category, price, quantitydescription, characteristics, version

4. updateProduct: update the definition of an existing product
	- Method: PUT  
	- Path: /warehouse/products/  
	- Body: JSON object containing all the information about a product definition: name, category, price, quantitydescription, characteristics    
	- Successful result:
		* Status Code: 200
		* Content Type: application/json
		* Body: JSON object containing the following properties: id, name, category, price, quantitydescription, characteristics, version

5. supplyProducts - supply products to the warehouse (add quantities of products)
	- Method: POST  
	- Path: /warehouse/supply/  
	- Body: JSON object containing all the information about the supply: date-time, list of product IDs and quantities.    
	- Successful result:
		* Status Code: 200
		* Content Type: application/json

6. reserveProducts - reserve quantities of products
	- Method: POST  
	- Path: /warehouse/reservation/  
	- Body: JSON object containing all the information about the reservation: date-time, list of product IDs and quantities.    
	- Successful result:
		* Status Code: 200
		* Content Type: application/json
		* Body: JSON object containing a single property: reservationId

6. confirmReservation - confirm reserved quantities of products
	- Method: PUT  
	- Path: /warehouse/reservation/confirm  
	- Body: JSON object containing a single property: reservationId    
	- Successful result:
		* Status Code: 200
		* Content Type: application/json

7. cancelReservation - calcel reserved quantities of products
	- Method: PUT  
	- Path: /warehouse/reservation/confirm  
	- Body: JSON object containing a single property: reservationId    
	- Successful result:
		* Status Code: 200

8. consolidateQuantities - recalculate quantities for all the operations that happened before the specified date.
	- Method: PUT  
	- Path: /warehouse/consolidate  
|Name           |Location         | Type             | Description                                       |
|---------------|-----------------|------------------|---------------------------------------------------|
| date          | query           | date             | The date for consolidation                        |
	- Successful result:
		* Status Code: 200


### Ordering microservice
This microservice will handle all the operations related to odering of products.

#### The following technologies will be used:
1. Programming language: Java11
2. Frameworks: Spring Boot, Spring Web, JPA
3. Database: MySql
4. Protocol: HTTP, REST APIs

#### The REST APIs
The Path of all the REST APIs of this microservice will start with /ordering
This microservice will expose the following REST APIs:

1. placeOrder: place a purcase order
	- Method: POST  
	- Path: /ordering/order/  
	- Body: a JSON object containing all the information about the purchase order: customer info, shipping address, list of product informations (product ID, quantity , price).  

	- Successful result:
		* Status Code: 200
		* Content Type: application/json
		* Body: JSON object containing all the information about the purchase order: order ID, order date-time, customer info, shipping address, list of product informations (product ID, quantity , price).

