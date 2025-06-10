# Product Management API

A Spring Boot application that provides REST APIs for managing products and product bundles.

## Project Structure
```
TPSpringBoot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── fr/azry/TPSpringBoot/
│   │   │       ├── controller/
│   │   │       │   └── ProductController.java
│   │   │       ├── model/
│   │   │       │   └── Product.java
│   │   │       ├── repository/
│   │   │       │   └── ProductRepository.java
│   │   │       └── TpSpringBootApplication.java
│   │   └── resources/
│   │       └── application.properties
└── pom.xml
```
## Technical Choices

### Framework and Dependencies
- **Spring Boot 3.5.0**: Modern Java framework for building standalone applications
- **Spring Data JPA**: Simplifies data access layer implementation
- **H2 Database**: In-memory database for development and testing
- **Java 24**: Latest Java version for enhanced performance and features
- **Maven**: Build and dependency management tool

### Architecture
- **REST Architecture**: RESTful API design for HTTP endpoints
- **MVC Pattern**: Separation of concerns with Model-View-Controller pattern
- **Repository Pattern**: Data access abstraction using Spring Data JPA

## Business Rules

### Product Management
1. Each product has:
   - Unique identifier (ID)
   - Name
   - Price
   - Optional list of source products (for bundles)

2. Product Bundles:
   - Can be created from multiple existing products
   - Price is the sum of all included products
   - Name is automatically generated from included products
   - Products can only be part of one bundle
   - Cannot include duplicate products

3. Product Operations:
   - CRUD operations (Create, Read, Update, Delete)
   - Product duplication with automatic name suffix " copy"
   - Bundle creation with validation

## API Endpoints and cURL Examples

### 1. Create a Product
```bash
curl -X POST \
-H "Content-Type: application/json" \
-d '{"name":"Stylo","price":2.5}' \
http://localhost:8080/products
```
### 2. Get All Products
```bash
curl -X GET http://localhost:8080/products
```
### 3. Get Product by ID
```bash
curl -X GET http://localhost:8080/products/1
```
### 4. Update Product
```bash
curl -X PUT \
-H "Content-Type: application/json" \
-d '{"name":"Stylo Premium","price":3.5}' \
http://localhost:8080/products/1
```
### 5. Delete Product
```bash
curl -X DELETE http://localhost:8080/products/1
```
### 6. Duplicate Product
```bash
curl -X POST http://localhost:8080/products/1/duplicate
```
### 7. Create Bundle
```bash
curl -X POST \
-H "Content-Type: application/json" \
-d '[1, 2, 3]' \
http://localhost:8080/products/bundle
```
## Running the Application

1. Clone the repository
2. Ensure you have Java 24 and Maven installed
3. Run the application:
```bash
mvn spring-boot:run
```
## Error Handling
- Products not found return 404 Not Found
- Invalid bundle creation (duplicate products or products already in bundles) returns 400 Bad Request
- Server errors return 500 Internal Server Error

## Database
- Using H2 in-memory database
- Data is not persistent between application restarts
- JPA automatically creates tables based on entity definitions