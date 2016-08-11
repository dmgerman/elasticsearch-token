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
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|ClusterScope
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|SUITE
import|;
end_import

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|SUITE
argument_list|,
name|transportClientRatio
operator|=
literal|0
argument_list|)
DECL|class|ReindexTestCase
specifier|public
specifier|abstract
class|class
name|ReindexTestCase
extends|extends
name|ESIntegTestCase
block|{
annotation|@
name|Override
DECL|method|nodePlugins
specifier|protected
name|Collection
argument_list|<
name|Class
argument_list|<
name|?
extends|extends
name|Plugin
argument_list|>
argument_list|>
name|nodePlugins
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|ReindexPlugin
operator|.
name|class
argument_list|)
return|;
block|}
DECL|method|reindex
specifier|protected
name|ReindexRequestBuilder
name|reindex
parameter_list|()
block|{
return|return
name|ReindexAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
return|;
block|}
DECL|method|updateByQuery
specifier|protected
name|UpdateByQueryRequestBuilder
name|updateByQuery
parameter_list|()
block|{
return|return
name|UpdateByQueryAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
return|;
block|}
DECL|method|deleteByQuery
specifier|protected
name|DeleteByQueryRequestBuilder
name|deleteByQuery
parameter_list|()
block|{
return|return
name|DeleteByQueryAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
return|;
block|}
DECL|method|rethrottle
specifier|protected
name|RethrottleRequestBuilder
name|rethrottle
parameter_list|()
block|{
return|return
name|RethrottleAction
operator|.
name|INSTANCE
operator|.
name|newRequestBuilder
argument_list|(
name|client
argument_list|()
argument_list|)
return|;
block|}
DECL|method|matcher
specifier|public
specifier|static
name|BulkIndexByScrollResponseMatcher
name|matcher
parameter_list|()
block|{
return|return
operator|new
name|BulkIndexByScrollResponseMatcher
argument_list|()
return|;
block|}
block|}
end_class

end_unit

