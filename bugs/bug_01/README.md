## Expected

| t5_c1 |
|-------|
| 3     |

A single entry with 3.

## Actual

| t5_c1 |
|-------|

No result.

## Version

Works as expected in: 
- 3.39.4 
- 3.48.0

Bug found in:
- 3.26.0

## Why?

The bug is in `WHERE` and `JOIN`, as `'' >= (t5_c1 AND 'x')`
correctly evaluates to `1` if used in the `SELECT` clause.

```sqlite
CREATE TABLE t5 (t5_c1);
CREATE TABLE t8 (t8_c2, t8_c3);
INSERT INTO t8 VALUES (3, NULL);

-- buggy query
SELECT t8_c2 FROM t8 LEFT JOIN t5
WHERE '' >= (t5_c1 AND 'x');

-- some eval for comparison
SELECT ('' >= (t5_c1 AND 'x')) FROM t8 LEFT JOIN t5 -- returns 1;
```