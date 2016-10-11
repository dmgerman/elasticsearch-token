begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|ingest
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|message
operator|.
name|ParameterizedMessage
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|util
operator|.
name|Supplier
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
name|ActionRequest
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
name|ActionResponse
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
name|DocumentRequest
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
name|BulkAction
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
name|BulkItemResponse
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
name|BulkRequest
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
name|action
operator|.
name|index
operator|.
name|IndexAction
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
name|index
operator|.
name|IndexRequest
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
name|support
operator|.
name|ActionFilter
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
name|support
operator|.
name|ActionFilterChain
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
name|Strings
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
name|component
operator|.
name|AbstractComponent
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
name|settings
operator|.
name|Settings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
operator|.
name|PipelineExecutionService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|service
operator|.
name|NodeService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|tasks
operator|.
name|Task
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Iterator
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_class
DECL|class|IngestActionFilter
specifier|public
specifier|final
class|class
name|IngestActionFilter
extends|extends
name|AbstractComponent
implements|implements
name|ActionFilter
block|{
DECL|field|executionService
specifier|private
specifier|final
name|PipelineExecutionService
name|executionService
decl_stmt|;
annotation|@
name|Inject
DECL|method|IngestActionFilter
specifier|public
name|IngestActionFilter
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|NodeService
name|nodeService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|executionService
operator|=
name|nodeService
operator|.
name|getIngestService
argument_list|()
operator|.
name|getPipelineExecutionService
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
parameter_list|<
name|Request
extends|extends
name|ActionRequest
argument_list|<
name|Request
argument_list|>
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|apply
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|Request
name|request
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
name|chain
parameter_list|)
block|{
switch|switch
condition|(
name|action
condition|)
block|{
case|case
name|IndexAction
operator|.
name|NAME
case|:
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|request
decl_stmt|;
if|if
condition|(
name|Strings
operator|.
name|hasText
argument_list|(
name|indexRequest
operator|.
name|getPipeline
argument_list|()
argument_list|)
condition|)
block|{
name|processIndexRequest
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|listener
argument_list|,
name|chain
argument_list|,
operator|(
name|IndexRequest
operator|)
name|request
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
name|BulkAction
operator|.
name|NAME
case|:
name|BulkRequest
name|bulkRequest
init|=
operator|(
name|BulkRequest
operator|)
name|request
decl_stmt|;
if|if
condition|(
name|bulkRequest
operator|.
name|hasIndexRequestsWithPipelines
argument_list|()
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|actionListener
init|=
operator|(
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
operator|)
name|listener
decl_stmt|;
name|processBulkIndexRequest
argument_list|(
name|task
argument_list|,
name|bulkRequest
argument_list|,
name|action
argument_list|,
name|chain
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
break|break;
default|default:
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
annotation|@
name|Override
DECL|method|apply
specifier|public
parameter_list|<
name|Response
extends|extends
name|ActionResponse
parameter_list|>
name|void
name|apply
parameter_list|(
name|String
name|action
parameter_list|,
name|Response
name|response
parameter_list|,
name|ActionListener
argument_list|<
name|Response
argument_list|>
name|listener
parameter_list|,
name|ActionFilterChain
argument_list|<
name|?
argument_list|,
name|Response
argument_list|>
name|chain
parameter_list|)
block|{
name|chain
operator|.
name|proceed
argument_list|(
name|action
argument_list|,
name|response
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
DECL|method|processIndexRequest
name|void
name|processIndexRequest
parameter_list|(
name|Task
name|task
parameter_list|,
name|String
name|action
parameter_list|,
name|ActionListener
name|listener
parameter_list|,
name|ActionFilterChain
name|chain
parameter_list|,
name|IndexRequest
name|indexRequest
parameter_list|)
block|{
name|executionService
operator|.
name|executeIndexRequest
argument_list|(
name|indexRequest
argument_list|,
name|t
lambda|->
block|{
name|logger
operator|.
name|error
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to execute pipeline [{}]"
argument_list|,
name|indexRequest
operator|.
name|getPipeline
argument_list|()
argument_list|)
argument_list|,
name|t
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|success
lambda|->
block|{
comment|// TransportIndexAction uses IndexRequest and same action name on the node that receives the request and the node that
comment|// processes the primary action. This could lead to a pipeline being executed twice for the same
comment|// index request, hence we set the pipeline to null once its execution completed.
name|indexRequest
operator|.
name|setPipeline
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|indexRequest
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
DECL|method|processBulkIndexRequest
name|void
name|processBulkIndexRequest
parameter_list|(
name|Task
name|task
parameter_list|,
name|BulkRequest
name|original
parameter_list|,
name|String
name|action
parameter_list|,
name|ActionFilterChain
name|chain
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|long
name|ingestStartTimeInNanos
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
name|BulkRequestModifier
name|bulkRequestModifier
init|=
operator|new
name|BulkRequestModifier
argument_list|(
name|original
argument_list|)
decl_stmt|;
name|executionService
operator|.
name|executeBulkRequest
argument_list|(
parameter_list|()
lambda|->
name|bulkRequestModifier
argument_list|,
parameter_list|(
name|indexRequest
parameter_list|,
name|exception
parameter_list|)
lambda|->
block|{
name|logger
operator|.
name|debug
argument_list|(
call|(
name|Supplier
argument_list|<
name|?
argument_list|>
call|)
argument_list|()
operator|->
operator|new
name|ParameterizedMessage
argument_list|(
literal|"failed to execute pipeline [{}] for document [{}/{}/{}]"
argument_list|,
name|indexRequest
operator|.
name|getPipeline
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|index
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|id
argument_list|()
argument_list|)
argument_list|,
name|exception
argument_list|)
argument_list|;
name|bulkRequestModifier
operator|.
name|markCurrentItemAsFailed
argument_list|(
name|exception
argument_list|)
argument_list|;
block|}
operator|,
parameter_list|(
name|exception
parameter_list|)
lambda|->
block|{
if|if
condition|(
name|exception
operator|!=
literal|null
condition|)
block|{
name|logger
operator|.
name|error
argument_list|(
literal|"failed to execute pipeline for a bulk request"
argument_list|,
name|exception
argument_list|)
expr_stmt|;
name|listener
operator|.
name|onFailure
argument_list|(
name|exception
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|long
name|ingestTookInMillis
init|=
name|TimeUnit
operator|.
name|NANOSECONDS
operator|.
name|toMillis
argument_list|(
name|System
operator|.
name|nanoTime
argument_list|()
operator|-
name|ingestStartTimeInNanos
argument_list|)
decl_stmt|;
name|BulkRequest
name|bulkRequest
init|=
name|bulkRequestModifier
operator|.
name|getBulkRequest
argument_list|()
decl_stmt|;
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|actionListener
init|=
name|bulkRequestModifier
operator|.
name|wrapActionListenerIfNeeded
argument_list|(
name|ingestTookInMillis
argument_list|,
name|listener
argument_list|)
decl_stmt|;
if|if
condition|(
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
comment|// at this stage, the transport bulk action can't deal with a bulk request with no requests,
comment|// so we stop and send an empty response back to the client.
comment|// (this will happen if pre-processing all items in the bulk failed)
name|actionListener
operator|.
name|onResponse
argument_list|(
operator|new
name|BulkResponse
argument_list|(
operator|new
name|BulkItemResponse
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|chain
operator|.
name|proceed
argument_list|(
name|task
argument_list|,
name|action
argument_list|,
name|bulkRequest
argument_list|,
name|actionListener
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|)
class|;
end_class

begin_function
unit|}      @
name|Override
DECL|method|order
specifier|public
name|int
name|order
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
end_function

begin_class
DECL|class|BulkRequestModifier
specifier|static
specifier|final
class|class
name|BulkRequestModifier
implements|implements
name|Iterator
argument_list|<
name|DocumentRequest
argument_list|>
block|{
DECL|field|bulkRequest
specifier|final
name|BulkRequest
name|bulkRequest
decl_stmt|;
DECL|field|failedSlots
specifier|final
name|Set
argument_list|<
name|Integer
argument_list|>
name|failedSlots
decl_stmt|;
DECL|field|itemResponses
specifier|final
name|List
argument_list|<
name|BulkItemResponse
argument_list|>
name|itemResponses
decl_stmt|;
DECL|field|currentSlot
name|int
name|currentSlot
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|originalSlots
name|int
index|[]
name|originalSlots
decl_stmt|;
DECL|method|BulkRequestModifier
name|BulkRequestModifier
parameter_list|(
name|BulkRequest
name|bulkRequest
parameter_list|)
block|{
name|this
operator|.
name|bulkRequest
operator|=
name|bulkRequest
expr_stmt|;
name|this
operator|.
name|failedSlots
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
expr_stmt|;
name|this
operator|.
name|itemResponses
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
name|DocumentRequest
name|next
parameter_list|()
block|{
return|return
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
operator|++
name|currentSlot
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hasNext
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
operator|(
name|currentSlot
operator|+
literal|1
operator|)
operator|<
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
return|;
block|}
DECL|method|getBulkRequest
name|BulkRequest
name|getBulkRequest
parameter_list|()
block|{
if|if
condition|(
name|itemResponses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|bulkRequest
return|;
block|}
else|else
block|{
name|BulkRequest
name|modifiedBulkRequest
init|=
operator|new
name|BulkRequest
argument_list|()
decl_stmt|;
name|modifiedBulkRequest
operator|.
name|setRefreshPolicy
argument_list|(
name|bulkRequest
operator|.
name|getRefreshPolicy
argument_list|()
argument_list|)
expr_stmt|;
name|modifiedBulkRequest
operator|.
name|waitForActiveShards
argument_list|(
name|bulkRequest
operator|.
name|waitForActiveShards
argument_list|()
argument_list|)
expr_stmt|;
name|modifiedBulkRequest
operator|.
name|timeout
argument_list|(
name|bulkRequest
operator|.
name|timeout
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|slot
init|=
literal|0
decl_stmt|;
name|originalSlots
operator|=
operator|new
name|int
index|[
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
operator|-
name|failedSlots
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|DocumentRequest
name|request
init|=
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|failedSlots
operator|.
name|contains
argument_list|(
name|i
argument_list|)
operator|==
literal|false
condition|)
block|{
name|modifiedBulkRequest
operator|.
name|add
argument_list|(
name|request
argument_list|)
expr_stmt|;
name|originalSlots
index|[
name|slot
operator|++
index|]
operator|=
name|i
expr_stmt|;
block|}
block|}
return|return
name|modifiedBulkRequest
return|;
block|}
block|}
DECL|method|wrapActionListenerIfNeeded
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|wrapActionListenerIfNeeded
parameter_list|(
name|long
name|ingestTookInMillis
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|actionListener
parameter_list|)
block|{
if|if
condition|(
name|itemResponses
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
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
name|actionListener
operator|.
name|onResponse
argument_list|(
operator|new
name|BulkResponse
argument_list|(
name|response
operator|.
name|getItems
argument_list|()
argument_list|,
name|response
operator|.
name|getTookInMillis
argument_list|()
argument_list|,
name|ingestTookInMillis
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|actionListener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
return|;
block|}
else|else
block|{
return|return
operator|new
name|IngestBulkResponseListener
argument_list|(
name|ingestTookInMillis
argument_list|,
name|originalSlots
argument_list|,
name|itemResponses
argument_list|,
name|actionListener
argument_list|)
return|;
block|}
block|}
DECL|method|markCurrentItemAsFailed
name|void
name|markCurrentItemAsFailed
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|IndexRequest
name|indexRequest
init|=
operator|(
name|IndexRequest
operator|)
name|bulkRequest
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
name|currentSlot
argument_list|)
decl_stmt|;
comment|// We hit a error during preprocessing a request, so we:
comment|// 1) Remember the request item slot from the bulk, so that we're done processing all requests we know what failed
comment|// 2) Add a bulk item failure for this request
comment|// 3) Continue with the next request in the bulk.
name|failedSlots
operator|.
name|add
argument_list|(
name|currentSlot
argument_list|)
expr_stmt|;
name|BulkItemResponse
operator|.
name|Failure
name|failure
init|=
operator|new
name|BulkItemResponse
operator|.
name|Failure
argument_list|(
name|indexRequest
operator|.
name|index
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|type
argument_list|()
argument_list|,
name|indexRequest
operator|.
name|id
argument_list|()
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|itemResponses
operator|.
name|add
argument_list|(
operator|new
name|BulkItemResponse
argument_list|(
name|currentSlot
argument_list|,
name|indexRequest
operator|.
name|opType
argument_list|()
argument_list|,
name|failure
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

begin_class
DECL|class|IngestBulkResponseListener
specifier|static
specifier|final
class|class
name|IngestBulkResponseListener
implements|implements
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
block|{
DECL|field|ingestTookInMillis
specifier|private
specifier|final
name|long
name|ingestTookInMillis
decl_stmt|;
DECL|field|originalSlots
specifier|private
specifier|final
name|int
index|[]
name|originalSlots
decl_stmt|;
DECL|field|itemResponses
specifier|private
specifier|final
name|List
argument_list|<
name|BulkItemResponse
argument_list|>
name|itemResponses
decl_stmt|;
DECL|field|actionListener
specifier|private
specifier|final
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|actionListener
decl_stmt|;
DECL|method|IngestBulkResponseListener
name|IngestBulkResponseListener
parameter_list|(
name|long
name|ingestTookInMillis
parameter_list|,
name|int
index|[]
name|originalSlots
parameter_list|,
name|List
argument_list|<
name|BulkItemResponse
argument_list|>
name|itemResponses
parameter_list|,
name|ActionListener
argument_list|<
name|BulkResponse
argument_list|>
name|actionListener
parameter_list|)
block|{
name|this
operator|.
name|ingestTookInMillis
operator|=
name|ingestTookInMillis
expr_stmt|;
name|this
operator|.
name|itemResponses
operator|=
name|itemResponses
expr_stmt|;
name|this
operator|.
name|actionListener
operator|=
name|actionListener
expr_stmt|;
name|this
operator|.
name|originalSlots
operator|=
name|originalSlots
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onResponse
specifier|public
name|void
name|onResponse
parameter_list|(
name|BulkResponse
name|response
parameter_list|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|response
operator|.
name|getItems
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|itemResponses
operator|.
name|add
argument_list|(
name|originalSlots
index|[
name|i
index|]
argument_list|,
name|response
operator|.
name|getItems
argument_list|()
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|actionListener
operator|.
name|onResponse
argument_list|(
operator|new
name|BulkResponse
argument_list|(
name|itemResponses
operator|.
name|toArray
argument_list|(
operator|new
name|BulkItemResponse
index|[
name|itemResponses
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|,
name|response
operator|.
name|getTookInMillis
argument_list|()
argument_list|,
name|ingestTookInMillis
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|onFailure
specifier|public
name|void
name|onFailure
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|actionListener
operator|.
name|onFailure
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
end_class

unit|}
end_unit

