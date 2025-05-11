# Discount manager - recruitment task

Algorithm for minimization of total payment for orders with specific payment methods.

## Dependencies

* Java 21
* JUnit 5
* Lombok
* Jackson Databind
* Maven

## Usage
### Build
The project has already built fat-jar but you can also build the project by:
```bash
mvn clean package
```

### Run
```bash
java -jar /home/…/app.jar /home/…/orders.json /home/…/paymentmethods.json
```

### Tests
```bash
mvn test
```

## Problem description

`paymentmethods.json` stores
* id - name od the payment method (string)
* discount - discount in % (integer value)
* limit - payment method balance (two decimal places)

`orders.json` stores
* id - order identificator (string)
* value - cost (two decimal places)
* promotions - list of payment method ids - discount from payment method can be applied to this order (may be empty)

Each order can be paid by:
* one traditional payment method - if the chosen payment method was in the promotions list then the discount is applied. Otherwise the entire amount has to be paid.
* points - the points discount is applied (from `paymentmethods.json`).
* partially point and traditional payment method - no discount from traditional method payment. If at least 10% of order value is covered by points, then a 10% discount is applied.

Points are preferred as a payment method. The total discount has to be maximized.

## Observations

### Basics
1. Points method has the biggest discount. Clients are encouraged to use points with a 10% discount, which is the same (mZysk card) or higher (BosBankrut) than only card use discount.
2. The greater the order value the greater the discount (e.g. 200 * 10% = 20 vs 50 * 10% = 5).
3. Discount 10% for partial payment is universal - try to use a card with lower discount because it is less promising.
4. Sometimes paying the entire order with points for 15% discount can be worse - these points can be distributed between multiple orders and get multiple 10% discounts for paying 1/10 of order value.

### First idea
First of all I noticed that in the delivered example the most expensive ORDER2 (value 200.00) was paid with a BosBankrut card which has the lowest discount (5%). This is not the best solution, because by paying only 10% of order value with points we can get a 10% discount.
I started experimenting by focusing on partial points payments. My first idea was:
1. Sort orders by value ascending - we want to maximize discounts so more expensive orders are more promising.
2. Try to use all points.
3. Try to pay with 10% points and 90% traditional method with the lowest discount - it is better to leave cards with higher discount for future orders. If not possible try other cards the same way.
4. Try to pay with the highest limit and the rest try to pay with points - e.g. order costs 100.00. We have a card with a limit of 60.00 and 80 points. We can combine 60.00 + 40.00 points and apply a 10% discount.
We want to save points for later so it is better to use a card with a higher limit.
6. Try to pay with a promotion card and apply its discount.
7. Try to pay with a regular card - no discount.
8. If it is the last order - use all points (points are preferred even if they don't provide a discount).

Thanks to this method I found a better solution:  
For short: Points(P), MZ(mZysk), BB(BosBankrut)
1. Pay for the ORDER2 with 20P + 180BB -> 10% discount -> 20P + 160BB (80P and 40BB left).
2. Pay for the ORDER3 with 15P + 135MZ -> 10% discount -> 15P + 120MZ (65P and 60MZ left).
3. Pay for the ORDER1 with 40P + 60MZ  -> 10% discount -> 40P + 50MZ  (25P and 10MZ left).
4. Pay for the ORDER4 with 25P + 25BB  -> 10% discount -> 25P + 20BB  (0P  and 20BB left).

Solution:
PUNKTY 100.00  
mZysk 170.00  
BosBankrut 180.00  

Total payment is 450.00 (455.00 in instruction).

### Optimization
For this set of data the first method was quite optimal. But I challenged myself and started to think about situations where starting with the most expensive order and for example spending all points was not acceptable. The biggest difference is the starting point.
I tried to think about a method that can calculate cost in different ways. I decided to implement a Dynamic Programming method.  
* dp[mask] - stores current cost and current state of wallet - limits.
* mask - paid orders binary coded - for example mask 1010 means that order 2 and 4 was paid. It is an easy and time efficient way to remember paid orders without remembering the path that led to this state.
* The algorithm iterates through all possible masks, attempting to pay for the unpaid orders. If the new cost is lower than previous for the same state then the value in dp is updated.
* The order of payment is the same as in the first idea - points have priority.

#### Upsides
* It checks many more combinations than the first method.
* It is easy to save the current state thanks to masks.
* Dynamic programming allows for the exploration of a broader solution space within a reasonable timeframe.

#### Downfalls
* For a big list of orders the memory complexity is high - O($$2^n$$) where n = amount of orders (because of masks).
* Some payment combinations can be bypassed - mainly in the case of payments of at least 10% points because you could choose different cards. The one with the smaller discount is preferred in this implementation.
