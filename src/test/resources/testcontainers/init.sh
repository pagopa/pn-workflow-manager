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

echo "### CREATE TABLES ###"

aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    dynamodb create-table \
    --table-name pn-CampaignStatistics  \
    --attribute-definitions \
        AttributeName=campaignId,AttributeType=S \
    --key-schema \
        AttributeName=campaignId,KeyType=HASH \
    --provisioned-throughput \
        ReadCapacityUnits=10,WriteCapacityUnits=5

echo ".*Initialization terminated.*"