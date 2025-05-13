SELECT ta9679.t20_c0 AS ca3033,
       ta5670.t25_c7 AS ca3685,
       sa3082.ca5243 AS ca7571,
       ta9679.t20_c0 AS ca7081,
       ta5670.t25_c7 AS ca5425,
       sa3082.ca2031 AS ca3352,
       ta5670.t25_c1 AS ca1252,
       sa3082.ca2525 AS ca1635,
       ta9257.t3_c3  AS ca9752
FROM (SELECT ta5146.t22_c5 AS ca7751,
             ta2007.t10_c6 AS ca8810,
             ta5146.t22_c8 AS ca9872,
             ta5146.t22_c8 AS ca2486,
             ta8365.t24_c9 AS ca8115,
             ta8365.t24_c9 AS ca3535,
             ta2007.t10_c6 AS ca7389,
             ta8365.t24_c3 AS ca5243,
             ta2007.t10_c1 AS ca6451,
             ta5146.t22_c9 AS ca2872,
             ta8365.t24_c9 AS ca2904,
             ta5146.t22_c4 AS ca8053,
             ta5146.t22_c1 AS ca3575,
             ta8365.t24_c5 AS ca3356,
             ta2007.t10_c1 AS ca2031,
             ta2007.t10_c2 AS ca2525,
             ta5146.t22_c2 AS ca3649,
             ta5146.t22_c5 AS ca3638,
             ta2007.t10_c3 AS ca8341
      FROM t22 AS ta5146
               NATURAL LEFT OUTER JOIN t10 AS ta2007
               CROSS JOIN t24 AS ta8365
      WHERE X'57201164dc84d0ef0f33'
      GROUP BY ca2486, ca3535, ca2904
      ORDER BY TRUE ASC NULLS LAST) AS sa3082
         FULL OUTER JOIN t25 AS ta5670
         NATURAL RIGHT OUTER JOIN t20 AS ta9679
         INNER JOIN t3 AS ta9257 ON nullif(NULL, ta9257.t3_c6)
ORDER BY FALSE DESC, FALSE DESC NULLS FIRST;