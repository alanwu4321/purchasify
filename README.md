# Purchasify 💵

## Prelude

Companies often struggle with finding the sweet spot to purchase the right amount products for the inventory. Most managers rely heavily on their "experience" and "intuition" which often causes products to be over or under purchased. Over purhcasing would lead to extra expenses in storage wheras under purchasing would negatively impact the product availability, service level, and custom satifcation. 

To tackle this problem and help companies better strategize their purchasing plan, Purchasify levrages historical stock level, demand, warehouse availability, and current custom order to forecast demand and further recommend the right and scientifc amount to purchase.

## Solution

### Tech Stack

#### Frontend - ReactJS
> Dynamic and responsive single-page web app that supports multiple languages
#### Backend - Java, Spring Boot
> RESTful API that offers multi-threaded and asychornous model calulation and Excel export
#### Storage - PostgreSQL, Redis
> Provides caching to optimize response time and reduce server load
#### DevOps - AWS S3, 53 Router, CloudFront CDN, VM, Datadog
> High availability and stability through AWS and active monitoring


### Forecast Algorithm

The underlying Algorithm involves Double Exponential Smoothing on demand and error and Trigg's tracking signal. 

Double exponential smoothing is given by the formulas

![trig](http://asset.alan-wu.com/purchasify/s=x.svg)

And for t > 1 by

![trig](http://asset.alan-wu.com/purchasify/stbt.svg)

where α is the data smoothing factor, 0 < α < 1, and β is the trend smoothing factor, 0 < β < 1.

To forecast beyond xt

![trig](http://asset.alan-wu.com/purchasify/ftm.svg)

To combat the fluctating error, Purchasify also employs Tracking signal which was developed by Trigg.

![trig](http://asset.alan-wu.com/purchasify/ts.svg)

In this model, et is the observed error in period t and |et| is the absolute value of the observed error. The smoothed values of the error and the absolute error are given by:


![trig](http://asset.alan-wu.com/purchasify/et.svg)


![trig](http://asset.alan-wu.com/purchasify/mt.svg)

Then the tracking signal is the ratio:

![trig](http://asset.alan-wu.com/purchasify/tt.svg)


## App Demo


### Search Form - Search by Category, Supplier, Products, Adjust Service Level, Warehouse filter, and Cache control


![GitHub Logo](http://asset.alan-wu.com/purchasify/drawer.png)

### Inventory Tab - Current status of inventory and order status

![GitHub Logo](http://asset.alan-wu.com/purchasify/stock.png)


### Forecast Model Tab - Calculation and Forecast 

![GitHub Logo](http://asset.alan-wu.com/purchasify/model-chart.png)



## Production

- `cd \Users\User\Desktop\labworld2.0\target`
- `git pull`
- `java -jar v1-0.0.1-SNAPSHOT.jar`

> `mkdir ./target/asset` to create a dir that holds Excel export for the first time


## Resources

- [Datadog](https://p.datadoghq.com/sb/qr29vfneoldizrbj-5344d58e735181330ced72e50c841107)
- [Java 9 JRE Runtime](https://www.oracle.com/java/technologies/javase/javase9-archive-downloads.html)
- [Java 8 Runtime](https://www.java.com/zh_TW/download/win10.jsp)
- [Install Redis on Windows](https://github.com/microsoftarchive/redis/releases)
- [Exponential Smoothing](https://en.wikipedia.org/wiki/Exponential_smoothing#:~:text=Double%20exponential%20smoothing,-Simple%20exponential%20smoothing&text=This%20nomenclature%20is%20similar%20to,exhibiting%20some%20form%20of%20trend.)
- [Trigg's Signal](https://en.wikipedia.org/wiki/Tracking_signal)
