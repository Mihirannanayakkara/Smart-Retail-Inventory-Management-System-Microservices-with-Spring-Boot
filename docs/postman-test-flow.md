# Postman Test Flow — Smart Retail System

Follow these steps in order to test the full system flow.

## Prerequisites
- All 6 services running
- RabbitMQ running

---

## Step 1: Register a User

**POST** `http://localhost:8080/api/users/register`

```json
{
  "name": "Test Staff",
  "email": "teststaff@smartretail.com",
  "password": "password123",
  "role": "STAFF"
}
```

**Expected**: `201 Created` with user details. Check Notification Service — a `USER_REGISTERED` notification should appear.

---

## Step 2: Login

**POST** `http://localhost:8080/api/users/login`

```json
{
  "email": "teststaff@smartretail.com",
  "password": "password123"
}
```

**Expected**: `200 OK` with JWT token, email, and role.

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "teststaff@smartretail.com",
  "role": "STAFF",
  "message": "Login successful"
}
```

---

## Step 3: Get All Products

**GET** `http://localhost:8080/api/products`

**Expected**: `200 OK` with 5 seeded products.

---

## Step 4: Create a Product

**POST** `http://localhost:8080/api/products`

```json
{
  "name": "Bluetooth Keyboard",
  "sku": "ELEC-003",
  "category": "ELECTRONICS",
  "price": 59.99,
  "quantity": 100,
  "lowStockThreshold": 15,
  "description": "Wireless Bluetooth keyboard with backlight"
}
```

**Expected**: `201 Created`

---

## Step 5: Create an Order

**POST** `http://localhost:8080/api/orders`

```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 3
    }
  ]
}
```

**Expected**: `201 Created` with order details, calculated totals. Check:
- Product stock reduced
- `ORDER_CREATED` notification created

---

## Step 6: Verify Stock Decreased

**GET** `http://localhost:8080/api/products/1`

**Expected**: Quantity should be `148` (was 150, ordered 2).

---

## Step 7: Cancel the Order

**PUT** `http://localhost:8080/api/orders/1/cancel`

**Expected**: `200 OK` with status `CANCELLED`. Stock is restored. `ORDER_CANCELLED` notification created.

---

## Step 8: Check Low Stock Products

**GET** `http://localhost:8080/api/products/low-stock`

**Expected**: Products where `quantity <= lowStockThreshold`.

---

## Step 9: Create a Supplier

**POST** `http://localhost:8080/api/suppliers`

```json
{
  "name": "New Supplier",
  "company": "SupplyChain Corp",
  "email": "newsupplier@supply.com",
  "phone": "555-9999",
  "address": "100 Supply Road"
}
```

**Expected**: `201 Created`

---

## Step 10: Create a Restock Request

**POST** `http://localhost:8080/api/restocks`

```json
{
  "productId": 1,
  "supplierId": 1,
  "quantity": 200,
  "expectedDate": "2026-04-15",
  "createdBy": "Admin User",
  "notes": "Urgent restock for wireless mouse"
}
```

**Expected**: `201 Created` with status `PENDING`. `RESTOCK_CREATED` notification created.

---

## Step 11: Approve and Deliver Restock

**PUT** `http://localhost:8080/api/restocks/1/approve`

**Expected**: Status changes to `APPROVED`.

**PUT** `http://localhost:8080/api/restocks/1/mark-delivered`

**Expected**: Status changes to `DELIVERED`. Product stock increased by 200. `RESTOCK_COMPLETED` notification created.

---

## Step 12: Verify All Notifications

**GET** `http://localhost:8080/api/notifications`

**Expected**: All generated notifications listed (USER_REGISTERED, ORDER_CREATED, ORDER_CANCELLED, RESTOCK_CREATED, RESTOCK_COMPLETED).

---

## Step 13: Search & Filter

- **Search products**: `GET http://localhost:8080/api/products/search?name=mouse`
- **Filter by category**: `GET http://localhost:8080/api/products/category/ELECTRONICS`
- **Orders by user**: `GET http://localhost:8080/api/orders/user/1`
- **Orders by status**: `GET http://localhost:8080/api/orders/status/CANCELLED`
- **Restocks by status**: `GET http://localhost:8080/api/restocks/status/DELIVERED`

---

## Step 14: User Management

- **Get all users**: `GET http://localhost:8080/api/users`
- **Get user by ID**: `GET http://localhost:8080/api/users/1`
- **Deactivate user**: `PUT http://localhost:8080/api/users/3/deactivate`
- **Activate user**: `PUT http://localhost:8080/api/users/3/activate`
- **Delete user**: `DELETE http://localhost:8080/api/users/3`
