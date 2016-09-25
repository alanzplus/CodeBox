#/usr/bin/env bash

for fn in $(ls test-*.rkt); do
  racket -t $fn
done
