#!/bin/bash

echo "### CREATE QUEUES ###"

queues="pn-workflow-manager-action-queue pn-workflow-manager-digital-event-queue pn-workflow-manager-analog-event-queue pn-workflow-manager-io-event-queue"

for qn in  $( echo $queues | tr " " "\n" ) ; do

    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn
done

echo "Initialization terminated"