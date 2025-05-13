## Expected

Empty result set.

## Actual

One single empty row.

## Version

Works as expected in: 
- 3.39.4 2022-09-29 15:55:41 a29f9949895322123f7c38fbe94c649a9d6e6c9cd0c3b41c96d694552f26b309 (compiled from source)
- 3.44.4 2025-02-19 00:18:53 f1e31fd9961ac82535a5d0702b127d84de8ca21d4df1c51c73e078ea0ad4afa8 (64-bit)

Bug found in:
- 3.39.4 2022-09-29 15:55:41 a29f9949895322123f7c38fbe94c649a9d6e6c9cd0c3b41c96d694552f26alt1 (buggy version)


## Why?

Possibly related to another but bug could not reproduce it exactly the same.

Somehow even though `t3` is empty and the join should thus return no results, a single row is returned. 

```sqlite
SELECT t3.t3_c3
FROM t12
    FUll JOIN t23
         INNER JOIN t3;
```