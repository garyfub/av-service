# Performance notes

Based on PC3.

## Solr

Documents are Message info documents.

### Inserting documents

#### 1. million
 * In collection: 10 602 ms
 * In Solr: 59 769 ms
 * After commit: 65 834 ms

#### 201. million
 * In collection: 10 410 ms
 * In Solr: 55 889 ms
 * After commit: 273 645 ms
 
#### 251. million
 * In collection: 10 375 ms
 * In Solr: 49 748 ms
 * After commit: 346 980 ms

### Sorting queries

first hits

**created** field - ASC and then DESC

 x | 100 M | 200 M | 250 M
 --- | --- | --- | ---
 ASC | 2348 ms | 5797 ms | 6795 ms
 DESC | 1643 ms | 3549 ms | 4354 ms

### Commits

Insert document and commit after.

With **empty** collection.

 x  | 10 | 100 | 1000 | 10 000 
--- | --- | --- | --- | ---
no  | 67 ms | 396 ms | 3 s | 21.4 s 
soft| 149 ms | 879 ms | 7.9 s | 1 m 6 s 
hard| 4.2 s | 29.8 s | 4 m 46 s | x 

With **100 million** documents (25 GB).

 x  | 10 | 100 | 1000 | 10 000 
--- | --- | --- | --- | ---
no  | 61 ms | 547 ms | 3.7 s | 21.9 s 
soft| x | 1.6 s | 11.3 s | 11 m 10 s 
hard| 8.5 s | 58.4 s | 9 m 42 s | x 

With **200 million** documents (46 GB).

 x  | 10 | 100 | 1000 | 10 000 
--- | --- | --- | --- | ---
no  | 76 ms | 532 ms | 3.7 s | 26.3 s 
soft| 4.9 s | 31.6 s | 3 m 38 s | 13 m 12 s 
hard| 9.2 s | 1 m 4 s | 10 m 43 s | x

With **250 million** documents (58 GB).

 x  | 10 | 100 | 1000 | 10 000 
--- | --- | --- | --- | ---
no  | 48 ms | 312 ms | 2.4 s | 19 s 
soft| 4.3 s | 29.5 s | 3 m 35 s | 26 m 2 s 
hard| 7.8 s | 1 m 17 s | x | x 