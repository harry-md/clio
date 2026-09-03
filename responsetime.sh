#!/usr/bin/bash

for i in {1..5}; do
    curl -sS -o /dev/null \
        -w 'dns=%{time_namelookup}s tcp=%{time_connect}s tls=%{time_appconnect}s ttfb=%{time_starttransfer}s total=%{time_total}s\n' \
        'https://clio-backend-fe5e9b73de99.herokuapp.com/api/books'
done
