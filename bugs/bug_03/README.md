## Expected

Empty result set.

## Actual

Some results from `t22`.

## Version

Works as expected in: 
- 3.39.4 2022-09-29 15:55:41 a29f9949895322123f7c38fbe94c649a9d6e6c9cd0c3b41c96d694552f26b309 (compiled from source)
- 3.44.4 2025-02-19 00:18:53 f1e31fd9961ac82535a5d0702b127d84de8ca21d4df1c51c73e078ea0ad4afa8 (64-bit)

Bug found in:
- 3.39.4 2022-09-29 15:55:41 a29f9949895322123f7c38fbe94c649a9d6e6c9cd0c3b41c96d694552f26alt1 (buggy version)


## Why?

Join on `t3`, which is empty and thus the `ON` should never be true, somehow returns elements.

From the docs:

> If there is an ON clause then the ON expression is evaluated for each row of the cartesian product as a boolean expression. Only rows for which the expression evaluates to true are included from the dataset.

```sqlite
SELECT *
FROM t22
    RIGHT OUTER JOIN t20
         JOIN t3 ON t3_c6;
```