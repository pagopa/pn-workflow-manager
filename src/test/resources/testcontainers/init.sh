#!/bin/bash

echo "### CREATE QUEUES ###"

queues="pn-workflow-manager_action pn-workflow-manager_digital_event pn-workflow-manager_analog_event pn-workflow-manager_io_event pn-safestore_to_workflowmanager pn-workflow_manager_inputs"

for qn in  $( echo $queues | tr " " "\n" ) ; do

    echo creating queue $qn ...

    aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
        sqs create-queue \
        --attributes '{"DelaySeconds":"2"}' \
        --queue-name $qn
done

echo "### CREATE DLQ ###"

echo "Creating pn-CoreEventBus-DLQ..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    sqs create-queue \
    --queue-name pn-CoreEventBus-DLQ

echo "### CREATE EVENT BUS ###"

echo "Creating pn-CoreEventBus..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events create-event-bus \
    --name pn-CoreEventBus

echo "### GET QUEUE ARNs ###"

ACTION_QUEUE_ARN=$(aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/pn-workflow-manager-action-queue \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

ANALOG_QUEUE_ARN=$(aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/pn-workflow-manager-analog-event-queue \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

DIGITAL_QUEUE_ARN=$(aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/pn-workflow-manager-digital-event-queue \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

IO_QUEUE_ARN=$(aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/pn-workflow-manager-io-event-queue \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

SAFESTORE_QUEUE_ARN=$(aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/pn-safestore_to_workflowmanager \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

INPUTS_QUEUE_ARN=$(aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    sqs get-queue-attributes \
    --queue-url http://localstack:4566/000000000000/pn-workflow_manager_inputs \
    --attribute-names QueueArn \
    --query 'Attributes.QueueArn' \
    --output text)

echo "### CREATE EVENT RULES ###"

# Rule 1: ActionManagerEventOutcome -> action queue
echo "Creating ActionManagerEventOutcome rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-rule \
    --name pn-ActionToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --event-pattern '{"detail-type":["ActionManagerEventOutcome"]}'

echo "Adding target to ActionManagerEventOutcome rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-targets \
    --rule pn-ActionToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --targets "Id=1,Arn=$ACTION_QUEUE_ARN,InputPath=$.detail.body"

# Rule 2: PaperChannelOutcomeEvent -> analog event queue
echo "Creating PaperChannelOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-rule \
    --name pn-AnalogToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --event-pattern '{"detail-type":["PaperChannelOutcomeEvent"]}'

echo "Adding target to PaperChannelOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-targets \
    --rule pn-AnalogToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --targets "Id=1,Arn=$ANALOG_QUEUE_ARN,InputPath=$.detail"

# Rule 3: ExternalChannelOutcomeEvent -> digital event queue
echo "Creating ExternalChannelOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-rule \
    --name pn-DigitalToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --event-pattern '{"detail-type":["ExternalChannelOutcomeEvent"]}'

echo "Adding target to ExternalChannelOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-targets \
    --rule pn-DigitalToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --targets "Id=1,Arn=$DIGITAL_QUEUE_ARN,InputPath=$.detail"

# Rule 4: IoConnectorOutcomeEvent -> io event queue
echo "Creating IoConnectorOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-rule \
    --name pn-IoToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --event-pattern '{"detail-type":["IoConnectorOutcomeEvent"]}'

echo "Adding target to IoConnectorOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-targets \
    --rule pn-IoToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --targets "Id=1,Arn=$IO_QUEUE_ARN,InputPath=$.detail"

# Rule 5: SafeStorageOutcomeEvent -> safestore to workflowmanager queue
echo "Creating SafeStorageOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-rule \
    --name pn-SafeStorageToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --event-pattern '{"detail-type":["SafeStorageOutcomeEvent"],"detail":{"documentType":["PN_COMMUNICATIONS_COVERPAGE"]}}'

echo "Adding target to SafeStorageOutcomeEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-targets \
    --rule pn-SafeStorageToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --targets "Id=1,Arn=$SAFESTORE_QUEUE_ARN,InputPath=$.detail"

# Rule 6: InformalNotificationViewedEvent -> workflow manager inputs queue
echo "Creating InformalNotificationViewedEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-rule \
    --name pn-InformalToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --event-pattern '{"detail-type":["InformalNotificationViewedEvent"]}'

echo "Adding target to InformalNotificationViewedEvent rule..."
aws --profile default --region us-east-1 --endpoint-url=http://localstack:4566 \
    events put-targets \
    --rule pn-InformalToWorkflowManager \
    --event-bus-name pn-CoreEventBus \
    --targets "Id=1,Arn=$INPUTS_QUEUE_ARN,InputPath=$.detail"

echo "Initialization terminated"
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