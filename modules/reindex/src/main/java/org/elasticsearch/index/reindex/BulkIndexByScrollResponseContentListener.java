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
name|support
operator|.
name|RestToXContentListener
import|;
end_import

begin_comment
comment|/**  * Just like RestToXContentListener but will return higher than 200 status if  * there are any failures.  */
end_comment

begin_class
DECL|class|BulkIndexByScrollResponseContentListener
specifier|public
class|class
name|BulkIndexByScrollResponseContentListener
parameter_list|<
name|R
extends|extends
name|BulkIndexByScrollResponse
parameter_list|>
extends|extends
name|RestToXContentListener
argument_list|<
name|R
argument_list|>
block|{
DECL|method|BulkIndexByScrollResponseContentListener
specifier|public
name|BulkIndexByScrollResponseContentListener
parameter_list|(
name|RestChannel
name|channel
parameter_list|)
block|{
name|super
argument_list|(
name|channel
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getStatus
specifier|protected
name|RestStatus
name|getStatus
parameter_list|(
name|R
name|response
parameter_list|)
block|{
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
name|getIndexingFailures
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
return|return
name|status
return|;
block|}
block|}
end_class

end_unit

