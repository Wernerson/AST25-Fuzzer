SELECT sa2260.ca9829 AS ca3499, sa2260.ca9829 AS ca3247
FROM (SELECT sa6829.ca7416 AS ca9829, sa6829.ca1044 AS ca571, sa6829.ca7416 AS ca8549, sa6829.ca3208 AS ca4079
      FROM (SELECT sa6610.ca5051 AS ca3809,
                   ta2455.t4_c5  AS ca1044,
                   ta5389.t6_c0  AS ca7416,
                   ta5389.t6_c0  AS ca9136,
                   ta2455.t4_c2  AS ca3208
            FROM t4 AS ta2455,
                 (SELECT ta8069.t4_c3 AS ca1661, ta7226.t5_c0 AS ca5051
                  FROM t5 AS ta7226
                           INNER JOIN t4 AS ta8069 LIMIT 100) AS sa6610,
                 t6 AS ta5389
            WHERE +ta5389.t6_c1 NOTNULL LIMIT 100) AS sa6829 LIMIT 100) AS sa2260
         CROSS JOIN t1 AS ta7188 LIMIT 100;