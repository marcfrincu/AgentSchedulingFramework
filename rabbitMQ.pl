 #!/usr/bin/perl

#install Net::RabbitMQ perl module from http://search.cpan.org/~jesus/Net-RabbitMQ-0.2.0/

use Net::RabbitMQ;

my $mq = Net::RabbitMQ->new();

#obtain connection to RabbitMQ server
$mq->connect("localhost", { user => "guest", password => "guest" });

##################################
#PART 1: ping the healing modules#
##################################
# needed for the healing agent. if pings are not received it considers the module lost and needs to start another one

#just pick a channel
$channelPingNo = 1;

#set the name of the exchange used for pinging it should always be "asf.registration"
$exchangePing = "asf.registration";

#set the routing key. it should always be "routing_key_vo1"
$routingKeyPing = "routing_key_vo1";

# set the message to be sent in JSON:
#  uuid the agent UUID as defined by the deploy.json
#  agentParentId usually -1 since we do not have a hierarchy of agents
#  moduleType=SCHEDULING always since Schlouder is a scheduler
#  status=RUNNING always
#  lastPing= the time in milliseconds since the last ping. In java we have System.currentTimeMillis()
$messagePing = "{\"uuid\"=\"26853a29-0073-4ab8-b3ad-5aacf442d0f5\", \"agentParentId\"=\"-1\", \"workflowId\"=\"-1\", \"moduleType\"=\"SCHEDULING\", \"status\"=\"RUNNING\", \"lastPing\"=\"1360231802011\"}";

############################################################
# uncomment for testing the functionality as standalone
# $mq->queue_declare($channelPingNo, "testpingreader");
# $mq->queue_bind($channelPingNo, "testpingreader", $exchangePing, $routingKeyPing);
############################################################

# open the ping channel
$mq->channel_open($channelPingNo);
#send out the message
$mq->publish($channelPingNo, $routingKeyPing, $messagePing, {exchange => $exchangePing});

############################################################
# uncomment for testing the functionality as standalone
# $result = $mq->get($channelPingNo, "testpingreader");
# print $result->{"body"};
############################################################

#!!! we do not need to set up any exchanges since this is done by the ASF platform
#$mq->exchange_declare($channelPingNo, $exchangePing);

###################################
#PART 2: send/receive bid requests#
###################################
# sends bid requests and awaits for answers or other bids from other schedulers
# it occurs when there are no more VMs available on current cloud and an external one is needed
# once a winner is picked for our bid we will receive another message directly from it with the link to the newly deployed VM (see STEP 3)

#just pick a channel
$channelBidNo = 2;

#set the name of the exchange used for asking the negotiator it should always be "asf.task.rescheduling"
$exchangeBid = "asf.task.rescheduling";

#set the routing key. it should always be "asf.task.rescheduling"
$routingKeyBid = "asf.task.rescheduling";

$messageBid = "todo";

############################################################
# uncomment for testing the functionality as standalone
# $mq->queue_declare($channelBidNo, "testbidreader");
# $mq->queue_bind($channelBidNo, "testbidreader", $exchangeBid, $routingKeyBid);
############################################################

# open the ping channel
$mq->channel_open($channelPingNo);
#send out the message
$mq->publish($channelBidNo, $routingKeyBid, $messageBid, {exchange => $exchangeBid});

############################################################
# uncomment for testing the functionality as standalone
# $result = $mq->get($channelBidNo, "testbidreader");
# print $result->{"body"};
############################################################


#######################################################
#PART 3: receive direct messages from other schedulers#
#######################################################

#just pick a channel
$channelSchedulerDirectNo = 3;

#set the name of the exchange used for quering requests to this scheduler it should always be "asf.direct.scheduler"
$exchangeDirect = "asf.direct.scheduler";

#set the routing key. it should always be "asf.direct.scheduler"
$routingKeyDirect = "asf.direct.scheduler";

$mq->queue_declare($channelSchedulerDirectNo, "testschedulerdirectreader");
$mq->queue_bind($channelSchedulerDirectNo, "testschedulerdirectreader", $exchangeDirect, $routingKeyDirect);

$result = $mq->get($channelSchedulerDirectNo, "testschedulerdirectreader");
print $result->{"body"};

# we are done so disconnect
$mq->disconnect();

