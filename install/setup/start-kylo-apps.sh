#!/bin/bash
CMD=${1:-start}
service kylo-ui $CMD
service kylo-services $CMD
service kylo-spark-shell start
