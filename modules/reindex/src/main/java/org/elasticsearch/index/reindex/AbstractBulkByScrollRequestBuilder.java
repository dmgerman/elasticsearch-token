begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|Action
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
name|ActionRequestBuilder
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
name|WriteConsistencyLevel
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
name|search
operator|.
name|SearchRequestBuilder
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
name|ElasticsearchClient
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
name|index
operator|.
name|query
operator|.
name|QueryBuilder
import|;
end_import

begin_class
DECL|class|AbstractBulkByScrollRequestBuilder
specifier|public
specifier|abstract
class|class
name|AbstractBulkByScrollRequestBuilder
parameter_list|<
name|Request
extends|extends
name|AbstractBulkByScrollRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|ActionResponse
parameter_list|,
name|Self
extends|extends
name|AbstractBulkByScrollRequestBuilder
parameter_list|<
name|Request
parameter_list|,
name|Response
parameter_list|,
name|Self
parameter_list|>
parameter_list|>
extends|extends
name|ActionRequestBuilder
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|Self
argument_list|>
block|{
DECL|field|source
specifier|private
specifier|final
name|SearchRequestBuilder
name|source
decl_stmt|;
DECL|method|AbstractBulkByScrollRequestBuilder
specifier|protected
name|AbstractBulkByScrollRequestBuilder
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|,
name|Action
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|,
name|Self
argument_list|>
name|action
parameter_list|,
name|SearchRequestBuilder
name|source
parameter_list|,
name|Request
name|request
parameter_list|)
block|{
name|super
argument_list|(
name|client
argument_list|,
name|action
argument_list|,
name|request
argument_list|)
expr_stmt|;
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
DECL|method|self
specifier|protected
specifier|abstract
name|Self
name|self
parameter_list|()
function_decl|;
comment|/**      * The search used to find documents to process.      */
DECL|method|source
specifier|public
name|SearchRequestBuilder
name|source
parameter_list|()
block|{
return|return
name|source
return|;
block|}
comment|/**      * Set the source indices.      */
DECL|method|source
specifier|public
name|Self
name|source
parameter_list|(
name|String
modifier|...
name|indices
parameter_list|)
block|{
name|source
operator|.
name|setIndices
argument_list|(
name|indices
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Set the query that will filter the source. Just a convenience method for      * easy chaining.      */
DECL|method|filter
specifier|public
name|Self
name|filter
parameter_list|(
name|QueryBuilder
name|filter
parameter_list|)
block|{
name|source
operator|.
name|setQuery
argument_list|(
name|filter
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * The maximum number of documents to attempt.      */
DECL|method|size
specifier|public
name|Self
name|size
parameter_list|(
name|int
name|size
parameter_list|)
block|{
name|request
operator|.
name|setSize
argument_list|(
name|size
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Should we version conflicts cause the action to abort?      */
DECL|method|abortOnVersionConflict
specifier|public
name|Self
name|abortOnVersionConflict
parameter_list|(
name|boolean
name|abortOnVersionConflict
parameter_list|)
block|{
name|request
operator|.
name|setAbortOnVersionConflict
argument_list|(
name|abortOnVersionConflict
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Call refresh on the indexes we've written to after the request ends?      */
DECL|method|refresh
specifier|public
name|Self
name|refresh
parameter_list|(
name|boolean
name|refresh
parameter_list|)
block|{
name|request
operator|.
name|setRefresh
argument_list|(
name|refresh
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Timeout to wait for the shards on to be available for each bulk request.      */
DECL|method|timeout
specifier|public
name|Self
name|timeout
parameter_list|(
name|TimeValue
name|timeout
parameter_list|)
block|{
name|request
operator|.
name|setTimeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Consistency level for write requests.      */
DECL|method|consistency
specifier|public
name|Self
name|consistency
parameter_list|(
name|WriteConsistencyLevel
name|consistency
parameter_list|)
block|{
name|request
operator|.
name|setConsistency
argument_list|(
name|consistency
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Initial delay after a rejection before retrying a bulk request. With the default maxRetries the total backoff for retrying rejections      * is about one minute per bulk request. Once the entire bulk request is successful the retry counter resets.      */
DECL|method|setRetryBackoffInitialTime
specifier|public
name|Self
name|setRetryBackoffInitialTime
parameter_list|(
name|TimeValue
name|retryBackoffInitialTime
parameter_list|)
block|{
name|request
operator|.
name|setRetryBackoffInitialTime
argument_list|(
name|retryBackoffInitialTime
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Total number of retries attempted for rejections. There is no way to ask for unlimited retries.      */
DECL|method|setMaxRetries
specifier|public
name|Self
name|setMaxRetries
parameter_list|(
name|int
name|maxRetries
parameter_list|)
block|{
name|request
operator|.
name|setMaxRetries
argument_list|(
name|maxRetries
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
comment|/**      * Set the throttle for this request in sub-requests per second. {@link Float#POSITIVE_INFINITY} means set no throttle and that is the      * default. Throttling is done between batches, as we start the next scroll requests. That way we can increase the scroll's timeout to      * make sure that it contains any time that we might wait.      */
DECL|method|setRequestsPerSecond
specifier|public
name|Self
name|setRequestsPerSecond
parameter_list|(
name|float
name|requestsPerSecond
parameter_list|)
block|{
name|request
operator|.
name|setRequestsPerSecond
argument_list|(
name|requestsPerSecond
argument_list|)
expr_stmt|;
return|return
name|self
argument_list|()
return|;
block|}
block|}
end_class

end_unit

