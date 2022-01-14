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
	c. addProduct - add the definition of a new produc
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
1. Ordering - which will be used to perform the sales and create the sales report
2. Warehouse - which will be used to handle the information of the products and available quantities.

Since we expect a big load on our e-commerce system, we will nee to have multiple instances of each microservice.

In order to do the load-balancing between these instances we will implement a Gateway.

To further reduce the load on the microservices, we will implement a Cache which will store for a short period of time the responses for the "GET" requests.

Here is a diagram depicting the high-level interaction between the components of our e-commerce syste:
![alt text](img/sysInteraction.png "E-Commerce components interaction")
