#!/bin/bash

echo "### CREATE QUEUES ###"

queues="pn-workflow-manager-action-queue"

for qn in  $( echo $queues | tr " " "\n" ) ; do

    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn
done

echo "### CREATE EVENT BUS - pn-CoreEventBus ###"
event_bus_name="pn-CoreEventBus"
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
  events create-event-bus --name $event_bus_name
echo "Initialization terminated"
