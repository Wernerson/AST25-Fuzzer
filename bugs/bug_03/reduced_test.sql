SELECT ta2255.t7_c8 AS ca6074, ta2255.t7_c5 AS ca7430, sa2886.ca7705 AS ca2608
FROM t7 AS ta2255
         LEFT JOIN (SELECT sa8967.ca3863 AS ca7705, ta7706.t6_c0 AS ca1090, sa4696.ca9198 AS ca8852
                    FROM t6 AS ta7706,
                         (SELECT sa5970.ca1349 AS ca4275,
                                 sa5970.ca5530 AS ca9198,
                                 sa5970.ca5530 AS ca5098,
                                 ta1029.t8_c0  AS ca9454
                          FROM (SELECT sa9361.ca4623 AS ca1349,
                                       sa9361.ca4623 AS ca5530,
                                       sa9361.ca4723 AS ca795,
                                       sa9361.ca4623 AS ca7365
                                FROM (SELECT ta5132.t10_c7 AS ca6018,
                                             ta3179.t4_c4  AS ca9930,
                                             ta5132.t10_c5 AS ca7610,
                                             ta5132.t10_c8 AS ca4623,
                                             ta3179.t4_c1  AS ca1256,
                                             ta5132.t10_c7 AS ca4723
                                      FROM t4 AS ta3179
                                               INNER JOIN t10 AS ta5132
                                      WHERE (ifnull(ta3179.t4_c6, (-1 <= ta3179.t4_c1 NOTNULL NOTNULL NOT NULL)) IS NOT ta5132.t10_c7) LIMIT 100) AS sa9361
                                WHERE (length((sa9361.ca9930 != changes() ISNULL)) * sa9361.ca1256) LIMIT 100) AS sa5970,
                               t8 AS ta1029 LIMIT 100) AS sa4696,
                         (SELECT ta1409.t5_c0 AS ca7725 FROM t5 AS ta1409 LIMIT 100) AS sa9741,
                         (SELECT sa1950.ca8657 AS ca7679,
                                 sa1950.ca8657 AS ca8821,
                                 sa1950.ca9716 AS ca9845,
                                 sa1950.ca1197 AS ca3863
                          FROM (SELECT ta9140.t8_c0 AS ca9716, ta9416.t4_c1 AS ca8657, ta9416.t4_c1 AS ca1197
                                FROM t4 AS ta9416
                                         LEFT JOIN t8 AS ta9140 LIMIT 100) AS sa1950
                          WHERE hex(sa1950.ca9716) LIMIT 100) AS sa8967 LIMIT 100) AS sa2886
WHERE (abs(ta2255.t7_c7 ISNULL) / ~ ((ta2255.t7_c0 >= ifnull(((~ta2255.t7_c3 & 10) IS NOT sa2886.ca8852), ifnull('kzwarmypd', sa2886.ca8852 ISNULL) NOTNULL)) * ('ntb' <= (+-0.016198932370454333 * sa2886.ca7705 NOTNULL))) NOTNULL NOTNULL) LIMIT 100;