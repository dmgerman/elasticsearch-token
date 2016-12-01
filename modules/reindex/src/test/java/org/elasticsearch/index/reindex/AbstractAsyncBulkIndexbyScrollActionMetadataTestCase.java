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

begin_class
DECL|class|AbstractAsyncBulkIndexbyScrollActionMetadataTestCase
specifier|public
specifier|abstract
class|class
name|AbstractAsyncBulkIndexbyScrollActionMetadataTestCase
parameter_list|<
name|Request
extends|extends
name|AbstractBulkIndexByScrollRequest
parameter_list|<
name|Request
parameter_list|>
parameter_list|,
name|Response
extends|extends
name|BulkIndexByScrollResponse
parameter_list|>
extends|extends
name|AbstractAsyncBulkIndexByScrollActionTestCase
argument_list|<
name|Request
argument_list|,
name|Response
argument_list|>
block|{
DECL|method|doc
specifier|protected
name|ScrollableHitSource
operator|.
name|BasicHit
name|doc
parameter_list|()
block|{
return|return
operator|new
name|ScrollableHitSource
operator|.
name|BasicHit
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
literal|0
argument_list|)
return|;
block|}
DECL|method|action
specifier|protected
specifier|abstract
name|AbstractAsyncBulkIndexByScrollAction
argument_list|<
name|Request
argument_list|>
name|action
parameter_list|()
function_decl|;
block|}
end_class

end_unit

