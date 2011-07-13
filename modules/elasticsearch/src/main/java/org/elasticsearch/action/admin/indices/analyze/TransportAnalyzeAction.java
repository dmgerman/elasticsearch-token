begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.admin.indices.analyze
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|admin
operator|.
name|indices
operator|.
name|analyze
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
name|analysis
operator|.
name|Analyzer
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
name|analysis
operator|.
name|TokenStream
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|CharTermAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|OffsetAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|PositionIncrementAttribute
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
name|analysis
operator|.
name|tokenattributes
operator|.
name|TypeAttribute
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchIllegalArgumentException
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
name|TransportActions
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
name|single
operator|.
name|custom
operator|.
name|TransportSingleCustomOperationAction
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|ClusterState
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|routing
operator|.
name|ShardsIterator
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
name|io
operator|.
name|FastStringReader
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
name|index
operator|.
name|mapper
operator|.
name|FieldMapper
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
name|service
operator|.
name|IndexService
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
name|IndicesService
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|threadpool
operator|.
name|ThreadPool
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|TransportService
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

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|TransportAnalyzeAction
specifier|public
class|class
name|TransportAnalyzeAction
extends|extends
name|TransportSingleCustomOperationAction
argument_list|<
name|AnalyzeRequest
argument_list|,
name|AnalyzeResponse
argument_list|>
block|{
DECL|field|indicesService
specifier|private
specifier|final
name|IndicesService
name|indicesService
decl_stmt|;
DECL|method|TransportAnalyzeAction
annotation|@
name|Inject
specifier|public
name|TransportAnalyzeAction
parameter_list|(
name|Settings
name|settings
parameter_list|,
name|ThreadPool
name|threadPool
parameter_list|,
name|ClusterService
name|clusterService
parameter_list|,
name|TransportService
name|transportService
parameter_list|,
name|IndicesService
name|indicesService
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|,
name|threadPool
argument_list|,
name|clusterService
argument_list|,
name|transportService
argument_list|)
expr_stmt|;
name|this
operator|.
name|indicesService
operator|=
name|indicesService
expr_stmt|;
block|}
DECL|method|executor
annotation|@
name|Override
specifier|protected
name|String
name|executor
parameter_list|()
block|{
return|return
name|ThreadPool
operator|.
name|Names
operator|.
name|INDEX
return|;
block|}
DECL|method|newRequest
annotation|@
name|Override
specifier|protected
name|AnalyzeRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|AnalyzeRequest
argument_list|()
return|;
block|}
DECL|method|newResponse
annotation|@
name|Override
specifier|protected
name|AnalyzeResponse
name|newResponse
parameter_list|()
block|{
return|return
operator|new
name|AnalyzeResponse
argument_list|()
return|;
block|}
DECL|method|transportAction
annotation|@
name|Override
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|TransportActions
operator|.
name|Admin
operator|.
name|Indices
operator|.
name|ANALYZE
return|;
block|}
DECL|method|transportShardAction
annotation|@
name|Override
specifier|protected
name|String
name|transportShardAction
parameter_list|()
block|{
return|return
literal|"indices/analyze/shard"
return|;
block|}
DECL|method|shards
annotation|@
name|Override
specifier|protected
name|ShardsIterator
name|shards
parameter_list|(
name|ClusterState
name|clusterState
parameter_list|,
name|AnalyzeRequest
name|request
parameter_list|)
block|{
name|request
operator|.
name|index
argument_list|(
name|clusterState
operator|.
name|metaData
argument_list|()
operator|.
name|concreteIndex
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|clusterState
operator|.
name|routingTable
argument_list|()
operator|.
name|index
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
operator|.
name|randomAllShardsIt
argument_list|()
return|;
block|}
DECL|method|shardOperation
annotation|@
name|Override
specifier|protected
name|AnalyzeResponse
name|shardOperation
parameter_list|(
name|AnalyzeRequest
name|request
parameter_list|,
name|int
name|shardId
parameter_list|)
throws|throws
name|ElasticSearchException
block|{
name|IndexService
name|indexService
init|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
decl_stmt|;
name|Analyzer
name|analyzer
init|=
literal|null
decl_stmt|;
name|String
name|field
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|field
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|FieldMapper
name|fieldMapper
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
name|request
operator|.
name|field
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldMapper
operator|!=
literal|null
condition|)
block|{
name|analyzer
operator|=
name|fieldMapper
operator|.
name|indexAnalyzer
argument_list|()
expr_stmt|;
name|field
operator|=
name|fieldMapper
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
expr_stmt|;
block|}
block|}
if|if
condition|(
name|field
operator|==
literal|null
condition|)
block|{
name|field
operator|=
literal|"_all"
expr_stmt|;
block|}
if|if
condition|(
name|analyzer
operator|==
literal|null
operator|&&
name|request
operator|.
name|analyzer
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|analyzer
operator|=
name|indexService
operator|.
name|analysisService
argument_list|()
operator|.
name|analyzer
argument_list|(
name|request
operator|.
name|analyzer
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
name|analyzer
operator|=
name|indexService
operator|.
name|analysisService
argument_list|()
operator|.
name|defaultIndexAnalyzer
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticSearchIllegalArgumentException
argument_list|(
literal|"failed to find analyzer"
argument_list|)
throw|;
block|}
name|List
argument_list|<
name|AnalyzeResponse
operator|.
name|AnalyzeToken
argument_list|>
name|tokens
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|TokenStream
name|stream
init|=
literal|null
decl_stmt|;
try|try
block|{
name|stream
operator|=
name|analyzer
operator|.
name|reusableTokenStream
argument_list|(
name|field
argument_list|,
operator|new
name|FastStringReader
argument_list|(
name|request
operator|.
name|text
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|stream
operator|.
name|reset
argument_list|()
expr_stmt|;
name|CharTermAttribute
name|term
init|=
name|stream
operator|.
name|addAttribute
argument_list|(
name|CharTermAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|PositionIncrementAttribute
name|posIncr
init|=
name|stream
operator|.
name|addAttribute
argument_list|(
name|PositionIncrementAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|OffsetAttribute
name|offset
init|=
name|stream
operator|.
name|addAttribute
argument_list|(
name|OffsetAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|TypeAttribute
name|type
init|=
name|stream
operator|.
name|addAttribute
argument_list|(
name|TypeAttribute
operator|.
name|class
argument_list|)
decl_stmt|;
name|int
name|position
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|stream
operator|.
name|incrementToken
argument_list|()
condition|)
block|{
name|int
name|increment
init|=
name|posIncr
operator|.
name|getPositionIncrement
argument_list|()
decl_stmt|;
if|if
condition|(
name|increment
operator|>
literal|0
condition|)
block|{
name|position
operator|=
name|position
operator|+
name|increment
expr_stmt|;
block|}
name|tokens
operator|.
name|add
argument_list|(
operator|new
name|AnalyzeResponse
operator|.
name|AnalyzeToken
argument_list|(
name|term
operator|.
name|toString
argument_list|()
argument_list|,
name|position
argument_list|,
name|offset
operator|.
name|startOffset
argument_list|()
argument_list|,
name|offset
operator|.
name|endOffset
argument_list|()
argument_list|,
name|type
operator|.
name|type
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|stream
operator|.
name|end
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchException
argument_list|(
literal|"failed to analyze"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|stream
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|stream
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
comment|// ignore
block|}
block|}
block|}
return|return
operator|new
name|AnalyzeResponse
argument_list|(
name|tokens
argument_list|)
return|;
block|}
block|}
end_class

end_unit

