begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.cache.query.index
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|cache
operator|.
name|query
operator|.
name|index
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|QueryCachingPolicy
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|search
operator|.
name|Weight
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|index
operator|.
name|AbstractIndexComponent
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
name|IndexSettings
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
name|cache
operator|.
name|query
operator|.
name|QueryCache
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|cache
operator|.
name|query
operator|.
name|IndicesQueryCache
import|;
end_import

begin_comment
comment|/**  * The index-level query cache. This class mostly delegates to the node-level  * query cache: {@link IndicesQueryCache}.  */
end_comment

begin_class
DECL|class|IndexQueryCache
specifier|public
class|class
name|IndexQueryCache
extends|extends
name|AbstractIndexComponent
implements|implements
name|QueryCache
block|{
DECL|field|indicesQueryCache
specifier|final
name|IndicesQueryCache
name|indicesQueryCache
decl_stmt|;
annotation|@
name|Inject
DECL|method|IndexQueryCache
specifier|public
name|IndexQueryCache
parameter_list|(
name|IndexSettings
name|indexSettings
parameter_list|,
name|IndicesQueryCache
name|indicesQueryCache
parameter_list|)
block|{
name|super
argument_list|(
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesQueryCache
operator|=
name|indicesQueryCache
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|close
specifier|public
name|void
name|close
parameter_list|()
throws|throws
name|ElasticsearchException
block|{
name|clear
argument_list|(
literal|"close"
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clear
specifier|public
name|void
name|clear
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"full cache clear, reason [{}]"
argument_list|,
name|reason
argument_list|)
expr_stmt|;
name|indicesQueryCache
operator|.
name|clearIndex
argument_list|(
name|index
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCache
specifier|public
name|Weight
name|doCache
parameter_list|(
name|Weight
name|weight
parameter_list|,
name|QueryCachingPolicy
name|policy
parameter_list|)
block|{
return|return
name|indicesQueryCache
operator|.
name|doCache
argument_list|(
name|weight
argument_list|,
name|policy
argument_list|)
return|;
block|}
block|}
end_class

end_unit

