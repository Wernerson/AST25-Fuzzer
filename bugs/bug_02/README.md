## Expected

Empty result set.

## Actual

Some results from `t1`.

## Version

Works as expected in: 
- 3.39.4 2022-09-29 15:55:41 a29f9949895322123f7c38fbe94c649a9d6e6c9cd0c3b41c96d694552f26alt1
- 3.48.0 2025-01-14 11:05:00 d2fe6b05f38d9d7cd78c5d252e99ac59f1aea071d669830c1ffe4e8966e84010

Bug found in:
- 3.26.0 2018-12-01 12:34:55 bf8c1b2b7a5960c282e543b9c293686dccff272512d08865f4600fb58238b4f9


## Why?

In v3.26.0, SQLite somehow treats `'9d'` as a `REAL` instead of an `INTEGER` and thus does not perform an integer division. 

```sqlite
SELECT *
FROM t1
WHERE 1 / '9d';
```