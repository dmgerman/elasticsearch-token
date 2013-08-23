begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_package
DECL|package|org.elasticsearch.action.termvector
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|termvector
package|;
end_package

begin_comment
comment|/*  * Licensed to ElasticSearch under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

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
name|ActionRequestBuilder
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
name|internal
operator|.
name|InternalClient
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
name|Nullable
import|;
end_import

begin_class
DECL|class|MultiTermVectorsRequestBuilder
specifier|public
class|class
name|MultiTermVectorsRequestBuilder
extends|extends
name|ActionRequestBuilder
argument_list|<
name|MultiTermVectorsRequest
argument_list|,
name|MultiTermVectorsResponse
argument_list|,
name|MultiTermVectorsRequestBuilder
argument_list|>
block|{
DECL|method|MultiTermVectorsRequestBuilder
specifier|public
name|MultiTermVectorsRequestBuilder
parameter_list|(
name|Client
name|client
parameter_list|)
block|{
name|super
argument_list|(
operator|(
name|InternalClient
operator|)
name|client
argument_list|,
operator|new
name|MultiTermVectorsRequest
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|add
specifier|public
name|MultiTermVectorsRequestBuilder
name|add
parameter_list|(
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|Iterable
argument_list|<
name|String
argument_list|>
name|ids
parameter_list|)
block|{
for|for
control|(
name|String
name|id
range|:
name|ids
control|)
block|{
name|request
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|MultiTermVectorsRequestBuilder
name|add
parameter_list|(
name|String
name|index
parameter_list|,
annotation|@
name|Nullable
name|String
name|type
parameter_list|,
name|String
modifier|...
name|ids
parameter_list|)
block|{
for|for
control|(
name|String
name|id
range|:
name|ids
control|)
block|{
name|request
operator|.
name|add
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
return|return
name|this
return|;
block|}
DECL|method|add
specifier|public
name|MultiTermVectorsRequestBuilder
name|add
parameter_list|(
name|TermVectorRequest
name|termVectorRequest
parameter_list|)
block|{
name|request
operator|.
name|add
argument_list|(
name|termVectorRequest
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the preference to execute the search. Defaults to randomize across shards. Can be set to      *<tt>_local</tt> to prefer local shards,<tt>_primary</tt> to execute only on primary shards, or      * a custom value, which guarantees that the same order will be used across different requests.      */
DECL|method|setPreference
specifier|public
name|MultiTermVectorsRequestBuilder
name|setPreference
parameter_list|(
name|String
name|preference
parameter_list|)
block|{
name|request
operator|.
name|preference
argument_list|(
name|preference
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|doExecute
specifier|protected
name|void
name|doExecute
parameter_list|(
name|ActionListener
argument_list|<
name|MultiTermVectorsResponse
argument_list|>
name|listener
parameter_list|)
block|{
operator|(
operator|(
name|Client
operator|)
name|client
operator|)
operator|.
name|multiTermVectors
argument_list|(
name|request
argument_list|,
name|listener
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

