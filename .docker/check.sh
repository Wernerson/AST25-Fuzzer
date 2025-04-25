#!/usr/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

/usr/bin/sqlite3-3.26.0 test.db < reduced_test.sql 1> test_out.txt
/usr/bin/sqlite3-3.39.4 test.db < reduced_test.sql 1> orcl_out.txt

diff test_out.txt orcl_out.txt

if [ $? -eq 0 ]; then
  echo -e "${RED}No diff${NC}"
else
  echo -e "${GREEN}Diff!${NC}"
fi