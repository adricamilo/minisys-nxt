#!/bin/bash
  
sudo su postgres -c "psql -U postgres -d postgres -f create-schema.sql"
if [ $? == 0 ]; then
    echo "Create schema succeeeded"
else
    echo "Create schema failed"
fi
