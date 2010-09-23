begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.river.rabbitmq
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|rabbitmq
package|;
end_package

begin_import
import|import
name|com
operator|.
name|rabbitmq
operator|.
name|client
operator|.
name|Channel
import|;
end_import

begin_import
import|import
name|com
operator|.
name|rabbitmq
operator|.
name|client
operator|.
name|Connection
import|;
end_import

begin_import
import|import
name|com
operator|.
name|rabbitmq
operator|.
name|client
operator|.
name|ConnectionFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|rabbitmq
operator|.
name|client
operator|.
name|QueueingConsumer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ActionListener
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
operator|.
name|BulkResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Client
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|action
operator|.
name|bulk
operator|.
name|BulkRequestBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|inject
operator|.
name|Inject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|EsExecutors
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|support
operator|.
name|XContentMapValues
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|AbstractRiverComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|River
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|RiverName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|river
operator|.
name|RiverSettings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|RabbitmqRiver
specifier|public
class|class
name|RabbitmqRiver
extends|extends
name|AbstractRiverComponent
implements|implements
name|River
block|{
DECL|field|client
specifier|private
specifier|final
name|Client
name|client
decl_stmt|;
DECL|field|rabbitHost
specifier|private
specifier|final
name|String
name|rabbitHost
decl_stmt|;
DECL|field|rabbitPort
specifier|private
specifier|final
name|int
name|rabbitPort
decl_stmt|;
DECL|field|rabbitUser
specifier|private
specifier|final
name|String
name|rabbitUser
decl_stmt|;
DECL|field|rabbitPassword
specifier|private
specifier|final
name|String
name|rabbitPassword
decl_stmt|;
DECL|field|rabbitVhost
specifier|private
specifier|final
name|String
name|rabbitVhost
decl_stmt|;
DECL|field|rabbitQueue
specifier|private
specifier|final
name|String
name|rabbitQueue
decl_stmt|;
DECL|field|rabbitExchange
specifier|private
specifier|final
name|String
name|rabbitExchange
decl_stmt|;
DECL|field|rabbitRoutingKey
specifier|private
specifier|final
name|String
name|rabbitRoutingKey
decl_stmt|;
DECL|field|bulkSize
specifier|private
specifier|final
name|int
name|bulkSize
decl_stmt|;
DECL|field|bulkTimeout
specifier|private
specifier|final
name|TimeValue
name|bulkTimeout
decl_stmt|;
DECL|field|ordered
specifier|private
specifier|final
name|boolean
name|ordered
decl_stmt|;
DECL|field|closed
specifier|private
specifier|volatile
name|boolean
name|closed
init|=
literal|false
decl_stmt|;
DECL|field|thread
specifier|private
specifier|volatile
name|Thread
name|thread
decl_stmt|;
DECL|field|connectionFactory
specifier|private
specifier|volatile
name|ConnectionFactory
name|connectionFactory
decl_stmt|;
DECL|method|RabbitmqRiver
annotation|@
name|Inject
specifier|public
name|RabbitmqRiver
parameter_list|(
name|RiverName
name|riverName
parameter_list|,
name|RiverSettings
name|settings
parameter_list|,
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
name|riverName
argument_list|,
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|client
operator|=
name|client
expr_stmt|;
if|if
condition|(
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"rabbitmq"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|rabbitSettings
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"rabbitmq"
argument_list|)
decl_stmt|;
name|rabbitHost
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"host"
argument_list|)
argument_list|,
name|ConnectionFactory
operator|.
name|DEFAULT_HOST
argument_list|)
expr_stmt|;
name|rabbitPort
operator|=
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"port"
argument_list|)
argument_list|,
name|ConnectionFactory
operator|.
name|DEFAULT_AMQP_PORT
argument_list|)
expr_stmt|;
name|rabbitUser
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"host"
argument_list|)
argument_list|,
name|ConnectionFactory
operator|.
name|DEFAULT_USER
argument_list|)
expr_stmt|;
name|rabbitPassword
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"host"
argument_list|)
argument_list|,
name|ConnectionFactory
operator|.
name|DEFAULT_PASS
argument_list|)
expr_stmt|;
name|rabbitVhost
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"host"
argument_list|)
argument_list|,
name|ConnectionFactory
operator|.
name|DEFAULT_VHOST
argument_list|)
expr_stmt|;
name|rabbitQueue
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"queue"
argument_list|)
argument_list|,
literal|"elasticsearch"
argument_list|)
expr_stmt|;
name|rabbitExchange
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"exchange"
argument_list|)
argument_list|,
literal|"elasticsearch"
argument_list|)
expr_stmt|;
name|rabbitRoutingKey
operator|=
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|rabbitSettings
operator|.
name|get
argument_list|(
literal|"routing_key"
argument_list|)
argument_list|,
literal|"elasticsearch"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rabbitHost
operator|=
name|ConnectionFactory
operator|.
name|DEFAULT_HOST
expr_stmt|;
name|rabbitPort
operator|=
name|ConnectionFactory
operator|.
name|DEFAULT_AMQP_PORT
expr_stmt|;
name|rabbitUser
operator|=
name|ConnectionFactory
operator|.
name|DEFAULT_USER
expr_stmt|;
name|rabbitPassword
operator|=
name|ConnectionFactory
operator|.
name|DEFAULT_PASS
expr_stmt|;
name|rabbitVhost
operator|=
name|ConnectionFactory
operator|.
name|DEFAULT_VHOST
expr_stmt|;
name|rabbitQueue
operator|=
literal|"elasticsearch"
expr_stmt|;
name|rabbitExchange
operator|=
literal|"elasticsearch"
expr_stmt|;
name|rabbitRoutingKey
operator|=
literal|"elasticsearch"
expr_stmt|;
block|}
if|if
condition|(
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"index"
argument_list|)
condition|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|indexSettings
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|settings
operator|.
name|settings
argument_list|()
operator|.
name|get
argument_list|(
literal|"index"
argument_list|)
decl_stmt|;
name|bulkSize
operator|=
name|XContentMapValues
operator|.
name|nodeIntegerValue
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
literal|"bulk_size"
argument_list|)
argument_list|,
literal|100
argument_list|)
expr_stmt|;
if|if
condition|(
name|indexSettings
operator|.
name|containsKey
argument_list|(
literal|"bulk_timeout"
argument_list|)
condition|)
block|{
name|bulkTimeout
operator|=
name|TimeValue
operator|.
name|parseTimeValue
argument_list|(
name|XContentMapValues
operator|.
name|nodeStringValue
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
literal|"bulk_timeout"
argument_list|)
argument_list|,
literal|"10ms"
argument_list|)
argument_list|,
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bulkTimeout
operator|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
name|ordered
operator|=
name|XContentMapValues
operator|.
name|nodeBooleanValue
argument_list|(
name|indexSettings
operator|.
name|get
argument_list|(
literal|"ordered"
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|bulkSize
operator|=
literal|100
expr_stmt|;
name|bulkTimeout
operator|=
name|TimeValue
operator|.
name|timeValueMillis
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|ordered
operator|=
literal|false
expr_stmt|;
block|}
block|}
DECL|method|start
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
block|{
name|connectionFactory
operator|=
operator|new
name|ConnectionFactory
argument_list|()
expr_stmt|;
name|connectionFactory
operator|.
name|setHost
argument_list|(
name|rabbitHost
argument_list|)
expr_stmt|;
name|connectionFactory
operator|.
name|setPort
argument_list|(
name|rabbitPort
argument_list|)
expr_stmt|;
name|connectionFactory
operator|.
name|setUsername
argument_list|(
name|rabbitUser
argument_list|)
expr_stmt|;
name|connectionFactory
operator|.
name|setPassword
argument_list|(
name|rabbitPassword
argument_list|)
expr_stmt|;
name|connectionFactory
operator|.
name|setVirtualHost
argument_list|(
name|rabbitVhost
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"creating rabbitmq river, host [{}], port [{}], user [{}], vhost [{}]"
argument_list|,
name|connectionFactory
operator|.
name|getHost
argument_list|()
argument_list|,
name|connectionFactory
operator|.
name|getPort
argument_list|()
argument_list|,
name|connectionFactory
operator|.
name|getUsername
argument_list|()
argument_list|,
name|connectionFactory
operator|.
name|getVirtualHost
argument_list|()
argument_list|)
expr_stmt|;
name|thread
operator|=
name|EsExecutors
operator|.
name|daemonThreadFactory
argument_list|(
name|settings
operator|.
name|globalSettings
argument_list|()
argument_list|,
literal|"rabbitmq_river"
argument_list|)
operator|.
name|newThread
argument_list|(
operator|new
name|Consumer
argument_list|()
argument_list|)
expr_stmt|;
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
DECL|method|close
annotation|@
name|Override
specifier|public
name|void
name|close
parameter_list|()
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
name|logger
operator|.
name|info
argument_list|(
literal|"closing rabbitmq river"
argument_list|)
expr_stmt|;
name|closed
operator|=
literal|true
expr_stmt|;
name|thread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
DECL|class|Consumer
specifier|private
class|class
name|Consumer
implements|implements
name|Runnable
block|{
DECL|field|connection
specifier|private
name|Connection
name|connection
decl_stmt|;
DECL|field|channel
specifier|private
name|Channel
name|channel
decl_stmt|;
DECL|method|run
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
return|return;
block|}
try|try
block|{
name|connection
operator|=
name|connectionFactory
operator|.
name|newConnection
argument_list|()
expr_stmt|;
name|channel
operator|=
name|connection
operator|.
name|createChannel
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|closed
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to created a connection / channel"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
continue|continue;
block|}
name|cleanup
argument_list|(
literal|0
argument_list|,
literal|"failed to connect"
argument_list|)
expr_stmt|;
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|5000
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e1
parameter_list|)
block|{
comment|// ignore, if we are closing, we will exit later
block|}
block|}
name|QueueingConsumer
name|consumer
init|=
operator|new
name|QueueingConsumer
argument_list|(
name|channel
argument_list|)
decl_stmt|;
comment|// define the queue
try|try
block|{
name|channel
operator|.
name|exchangeDeclare
argument_list|(
name|rabbitExchange
comment|/*exchange*/
argument_list|,
literal|"direct"
comment|/*type*/
argument_list|,
literal|true
comment|/*durable*/
argument_list|)
expr_stmt|;
name|channel
operator|.
name|queueDeclare
argument_list|(
name|rabbitQueue
comment|/*queue*/
argument_list|,
literal|true
comment|/*durable*/
argument_list|,
literal|false
comment|/*exclusive*/
argument_list|,
literal|false
comment|/*autoDelete*/
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|channel
operator|.
name|queueBind
argument_list|(
name|rabbitQueue
comment|/*queue*/
argument_list|,
name|rabbitExchange
comment|/*exchange*/
argument_list|,
name|rabbitRoutingKey
comment|/*routingKey*/
argument_list|)
expr_stmt|;
name|channel
operator|.
name|basicConsume
argument_list|(
name|rabbitQueue
comment|/*queue*/
argument_list|,
literal|false
comment|/*noAck*/
argument_list|,
name|consumer
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|closed
condition|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to create queue [{}]"
argument_list|,
name|e
argument_list|,
name|rabbitQueue
argument_list|)
expr_stmt|;
block|}
name|cleanup
argument_list|(
literal|0
argument_list|,
literal|"failed to create queue"
argument_list|)
expr_stmt|;
continue|continue;
block|}
comment|// now use the queue to listen for messages
while|while
condition|(
literal|true
condition|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
break|break;
block|}
name|QueueingConsumer
operator|.
name|Delivery
name|task
decl_stmt|;
try|try
block|{
name|task
operator|=
name|consumer
operator|.
name|nextDelivery
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
if|if
condition|(
operator|!
name|closed
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed to get next message, reconnecting..."
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
name|cleanup
argument_list|(
literal|0
argument_list|,
literal|"failed to get message"
argument_list|)
expr_stmt|;
break|break;
block|}
if|if
condition|(
name|task
operator|!=
literal|null
operator|&&
name|task
operator|.
name|getBody
argument_list|()
operator|!=
literal|null
condition|)
block|{
specifier|final
name|List
argument_list|<
name|Long
argument_list|>
name|deliveryTags
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|BulkRequestBuilder
name|bulkRequestBuilder
init|=
name|client
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
try|try
block|{
name|bulkRequestBuilder
operator|.
name|add
argument_list|(
name|task
operator|.
name|getBody
argument_list|()
argument_list|,
literal|0
argument_list|,
name|task
operator|.
name|getBody
argument_list|()
operator|.
name|length
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to parse request for delivery tag [{}], ack'ing..."
argument_list|,
name|e
argument_list|,
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|basicAck
argument_list|(
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to ack [{}]"
argument_list|,
name|e1
argument_list|,
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
name|deliveryTags
operator|.
name|add
argument_list|(
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|bulkRequestBuilder
operator|.
name|numberOfActions
argument_list|()
operator|<
name|bulkSize
condition|)
block|{
comment|// try and spin some more of those without timeout, so we have a bigger bulk (bounded by the bulk size)
try|try
block|{
while|while
condition|(
operator|(
name|task
operator|=
name|consumer
operator|.
name|nextDelivery
argument_list|(
name|bulkTimeout
operator|.
name|millis
argument_list|()
argument_list|)
operator|)
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|bulkRequestBuilder
operator|.
name|add
argument_list|(
name|task
operator|.
name|getBody
argument_list|()
argument_list|,
literal|0
argument_list|,
name|task
operator|.
name|getBody
argument_list|()
operator|.
name|length
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to parse request for delivery tag [{}], ack'ing..."
argument_list|,
name|e
argument_list|,
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
name|channel
operator|.
name|basicAck
argument_list|(
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to ack on failure [{}]"
argument_list|,
name|e1
argument_list|,
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|deliveryTags
operator|.
name|add
argument_list|(
name|task
operator|.
name|getEnvelope
argument_list|()
operator|.
name|getDeliveryTag
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|bulkRequestBuilder
operator|.
name|numberOfActions
argument_list|()
operator|>=
name|bulkSize
condition|)
block|{
break|break;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
if|if
condition|(
name|closed
condition|)
block|{
break|break;
block|}
block|}
block|}
if|if
condition|(
name|logger
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|trace
argument_list|(
literal|"executing bulk with [{}] actions"
argument_list|,
name|bulkRequestBuilder
operator|.
name|numberOfActions
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|ordered
condition|)
block|{
try|try
block|{
name|BulkResponse
name|response
init|=
name|bulkRequestBuilder
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|hasFailures
argument_list|()
condition|)
block|{
comment|// TODO write to exception queue?
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute"
operator|+
name|response
operator|.
name|buildFailureMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Long
name|deliveryTag
range|:
name|deliveryTags
control|)
block|{
try|try
block|{
name|channel
operator|.
name|basicAck
argument_list|(
name|deliveryTag
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to ack [{}]"
argument_list|,
name|e1
argument_list|,
name|deliveryTag
argument_list|)
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute bulk"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|bulkRequestBuilder
operator|.
name|execute
argument_list|(
operator|new
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|response
parameter_list|)
block|{
if|if
condition|(
name|response
operator|.
name|hasFailures
argument_list|()
condition|)
block|{
comment|// TODO write to exception queue?
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute"
operator|+
name|response
operator|.
name|buildFailureMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|Long
name|deliveryTag
range|:
name|deliveryTags
control|)
block|{
try|try
block|{
name|channel
operator|.
name|basicAck
argument_list|(
name|deliveryTag
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e1
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to ack [{}]"
argument_list|,
name|e1
argument_list|,
name|deliveryTag
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Throwable
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"failed to execute bulk for delivery tags , not ack'ing"
argument_list|,
name|e
argument_list|,
name|deliveryTags
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
block|}
DECL|method|cleanup
specifier|private
name|void
name|cleanup
parameter_list|(
name|int
name|code
parameter_list|,
name|String
name|message
parameter_list|)
block|{
try|try
block|{
name|channel
operator|.
name|close
argument_list|(
name|code
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to close channel"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|connection
operator|.
name|close
argument_list|(
name|code
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"failed to close connection"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

