---
title: "Типобезопасные Data Callback Queries"
description: ""
lead: ""
date: 2022-09-20T16:17:33+03:00
lastmod: 2022-09-20T16:17:33+03:00
draft: false
images: []
weight: 050
toc: true
---

```kotlin
@Serializable
sealed interface Query

@Serializable
@SerialName("smp")
class SampleQuery(
  val text: String
) : Query
```
