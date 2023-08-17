# Whitelist Access Plugin

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

-------

## What does it do

whitelist-access is a simple library that allows you to whitelist access to your application.

To limit access to a specific part of an application to only a select group of users, you can use a whitelist-based approach. In this approach, you create a list of allowed users and check if the incoming request's user is in the whitelist. If the user is not in the whitelist, the request is denied.

I can implement Aspect-Oriented Programming (AOP) using the Spring AOP framework or AspectJ in Java. AOP is a programming paradigm that aims to increase modularity by allowing the separation of cross-cutting concerns.

## Quick Start

Add the following dependency to your pom.xml

```xml
<dependency>
    <groupId>cn.huazai.tool</groupId>
    <artifactId>whitelist-access</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
import cn.huazai.tool.whitelist.access.annotation.WhitelistAccess;

@Service
public class Business {

    @WhitelistAccess(businessKey = "exampleBusinessKey", checkValue = "#reqDTO.userId")
    public void business(BizReDTO reqDTO) {
        // whitelist can access
    }
}
```