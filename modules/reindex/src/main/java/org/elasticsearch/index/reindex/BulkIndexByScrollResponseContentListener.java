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
name|ExceptionsHelper
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
operator|.
name|Failure
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
name|byscroll
operator|.
name|BulkByScrollResponse
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
name|byscroll
operator|.
name|ScrollableHitSource
operator|.
name|SearchFailure
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
name|ToXContent
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
name|XContentBuilder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|BytesRestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestChannel
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestResponse
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|action
operator|.
name|RestBuilderListener
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
comment|/**  * RestBuilderListener that returns higher than 200 status if there are any failures and allows to set XContent.Params.  */
end_comment

begin_class
DECL|class|BulkIndexByScrollResponseContentListener
specifier|public
class|class
name|BulkIndexByScrollResponseContentListener
extends|extends
name|RestBuilderListener
argument_list|<
name|BulkByScrollResponse
argument_list|>
block|{
DECL|field|params
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
decl_stmt|;
DECL|method|BulkIndexByScrollResponseContentListener
specifier|public
name|BulkIndexByScrollResponseContentListener
parameter_list|(
name|RestChannel
name|channel
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|params
parameter_list|)
block|{
name|super
argument_list|(
name|channel
argument_list|)
expr_stmt|;
name|this
operator|.
name|params
operator|=
name|params
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|buildResponse
specifier|public
name|RestResponse
name|buildResponse
parameter_list|(
name|BulkByScrollResponse
name|response
parameter_list|,
name|XContentBuilder
name|builder
parameter_list|)
throws|throws
name|Exception
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|response
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
operator|new
name|ToXContent
operator|.
name|DelegatingMapParams
argument_list|(
name|params
argument_list|,
name|channel
operator|.
name|request
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
operator|new
name|BytesRestResponse
argument_list|(
name|getStatus
argument_list|(
name|response
argument_list|)
argument_list|,
name|builder
argument_list|)
return|;
block|}
DECL|method|getStatus
specifier|private
name|RestStatus
name|getStatus
parameter_list|(
name|BulkByScrollResponse
name|response
parameter_list|)
block|{
comment|/*          * Return the highest numbered rest status under the assumption that higher numbered statuses are "more error" and thus more          * interesting to the user.          */
name|RestStatus
name|status
init|=
name|RestStatus
operator|.
name|OK
decl_stmt|;
if|if
condition|(
name|response
operator|.
name|isTimedOut
argument_list|()
condition|)
block|{
name|status
operator|=
name|RestStatus
operator|.
name|REQUEST_TIMEOUT
expr_stmt|;
block|}
for|for
control|(
name|Failure
name|failure
range|:
name|response
operator|.
name|getBulkFailures
argument_list|()
control|)
block|{
if|if
condition|(
name|failure
operator|.
name|getStatus
argument_list|()
operator|.
name|getStatus
argument_list|()
operator|>
name|status
operator|.
name|getStatus
argument_list|()
condition|)
block|{
name|status
operator|=
name|failure
operator|.
name|getStatus
argument_list|()
expr_stmt|;
block|}
block|}
for|for
control|(
name|SearchFailure
name|failure
range|:
name|response
operator|.
name|getSearchFailures
argument_list|()
control|)
block|{
name|RestStatus
name|failureStatus
init|=
name|ExceptionsHelper
operator|.
name|status
argument_list|(
name|failure
operator|.
name|getReason
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|failureStatus
operator|.
name|getStatus
argument_list|()
operator|>
name|status
operator|.
name|getStatus
argument_list|()
condition|)
block|{
name|status
operator|=
name|failureStatus
expr_stmt|;
block|}
block|}
return|return
name|status
return|;
block|}
block|}
end_class

end_unit

