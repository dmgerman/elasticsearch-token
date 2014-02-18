begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
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
name|ElasticsearchException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchIllegalArgumentException
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
name|block
operator|.
name|ClusterBlockException
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
name|block
operator|.
name|ClusterBlockLevel
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
name|lucene
operator|.
name|Lucene
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
name|ImmutableSettings
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
name|analysis
operator|.
name|*
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
name|mapper
operator|.
name|internal
operator|.
name|AllFieldMapper
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
name|indices
operator|.
name|analysis
operator|.
name|IndicesAnalysisService
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
comment|/**  *  */
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
DECL|field|indicesAnalysisService
specifier|private
specifier|final
name|IndicesAnalysisService
name|indicesAnalysisService
decl_stmt|;
annotation|@
name|Inject
DECL|method|TransportAnalyzeAction
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
parameter_list|,
name|IndicesAnalysisService
name|indicesAnalysisService
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
name|this
operator|.
name|indicesAnalysisService
operator|=
name|indicesAnalysisService
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executor
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
annotation|@
name|Override
DECL|method|newRequest
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
annotation|@
name|Override
DECL|method|newResponse
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
annotation|@
name|Override
DECL|method|transportAction
specifier|protected
name|String
name|transportAction
parameter_list|()
block|{
return|return
name|AnalyzeAction
operator|.
name|NAME
return|;
block|}
annotation|@
name|Override
DECL|method|checkGlobalBlock
specifier|protected
name|ClusterBlockException
name|checkGlobalBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|AnalyzeRequest
name|request
parameter_list|)
block|{
return|return
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|globalBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|checkRequestBlock
specifier|protected
name|ClusterBlockException
name|checkRequestBlock
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|AnalyzeRequest
name|request
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|index
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|request
operator|.
name|index
argument_list|(
name|state
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
name|state
operator|.
name|blocks
argument_list|()
operator|.
name|indexBlockedException
argument_list|(
name|ClusterBlockLevel
operator|.
name|READ
argument_list|,
name|request
operator|.
name|index
argument_list|()
argument_list|)
return|;
block|}
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|shards
specifier|protected
name|ShardsIterator
name|shards
parameter_list|(
name|ClusterState
name|state
parameter_list|,
name|AnalyzeRequest
name|request
parameter_list|)
block|{
if|if
condition|(
name|request
operator|.
name|index
argument_list|()
operator|==
literal|null
condition|)
block|{
comment|// just execute locally....
return|return
literal|null
return|;
block|}
return|return
name|state
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
name|randomAllActiveShardsIt
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|shardOperation
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
name|ElasticsearchException
block|{
name|IndexService
name|indexService
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|index
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|indexService
operator|=
name|indicesService
operator|.
name|indexServiceSafe
argument_list|(
name|request
operator|.
name|index
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Analyzer
name|analyzer
init|=
literal|null
decl_stmt|;
name|boolean
name|closeAnalyzer
init|=
literal|false
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
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"No index provided, and trying to analyzer based on a specific field which requires the index parameter"
argument_list|)
throw|;
block|}
name|FieldMapper
argument_list|<
name|?
argument_list|>
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
if|if
condition|(
name|fieldMapper
operator|.
name|isNumeric
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"Can't process field ["
operator|+
name|request
operator|.
name|field
argument_list|()
operator|+
literal|"], Analysis requests are not supported on numeric fields"
argument_list|)
throw|;
block|}
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
if|if
condition|(
name|indexService
operator|!=
literal|null
condition|)
block|{
name|field
operator|=
name|indexService
operator|.
name|queryParserService
argument_list|()
operator|.
name|defaultField
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|field
operator|=
name|AllFieldMapper
operator|.
name|NAME
expr_stmt|;
block|}
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
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
name|analyzer
operator|=
name|indicesAnalysisService
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
else|else
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
if|if
condition|(
name|analyzer
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find analyzer ["
operator|+
name|request
operator|.
name|analyzer
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|request
operator|.
name|tokenizer
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|TokenizerFactory
name|tokenizerFactory
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
name|TokenizerFactoryFactory
name|tokenizerFactoryFactory
init|=
name|indicesAnalysisService
operator|.
name|tokenizerFactoryFactory
argument_list|(
name|request
operator|.
name|tokenizer
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenizerFactoryFactory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find global tokenizer under ["
operator|+
name|request
operator|.
name|tokenizer
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|tokenizerFactory
operator|=
name|tokenizerFactoryFactory
operator|.
name|create
argument_list|(
name|request
operator|.
name|tokenizer
argument_list|()
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tokenizerFactory
operator|=
name|indexService
operator|.
name|analysisService
argument_list|()
operator|.
name|tokenizer
argument_list|(
name|request
operator|.
name|tokenizer
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|tokenizerFactory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find tokenizer under ["
operator|+
name|request
operator|.
name|tokenizer
argument_list|()
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
name|TokenFilterFactory
index|[]
name|tokenFilterFactories
init|=
operator|new
name|TokenFilterFactory
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|tokenFilters
argument_list|()
operator|!=
literal|null
operator|&&
name|request
operator|.
name|tokenFilters
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|tokenFilterFactories
operator|=
operator|new
name|TokenFilterFactory
index|[
name|request
operator|.
name|tokenFilters
argument_list|()
operator|.
name|length
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
name|request
operator|.
name|tokenFilters
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|tokenFilterName
init|=
name|request
operator|.
name|tokenFilters
argument_list|()
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
name|TokenFilterFactoryFactory
name|tokenFilterFactoryFactory
init|=
name|indicesAnalysisService
operator|.
name|tokenFilterFactoryFactory
argument_list|(
name|tokenFilterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|tokenFilterFactoryFactory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find global token filter under ["
operator|+
name|tokenFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|tokenFilterFactories
index|[
name|i
index|]
operator|=
name|tokenFilterFactoryFactory
operator|.
name|create
argument_list|(
name|tokenFilterName
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|tokenFilterFactories
index|[
name|i
index|]
operator|=
name|indexService
operator|.
name|analysisService
argument_list|()
operator|.
name|tokenFilter
argument_list|(
name|tokenFilterName
argument_list|)
expr_stmt|;
if|if
condition|(
name|tokenFilterFactories
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find token filter under ["
operator|+
name|tokenFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|tokenFilterFactories
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find token filter under ["
operator|+
name|tokenFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|CharFilterFactory
index|[]
name|charFilterFactories
init|=
operator|new
name|CharFilterFactory
index|[
literal|0
index|]
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|charFilters
argument_list|()
operator|!=
literal|null
operator|&&
name|request
operator|.
name|charFilters
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|charFilterFactories
operator|=
operator|new
name|CharFilterFactory
index|[
name|request
operator|.
name|charFilters
argument_list|()
operator|.
name|length
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
name|request
operator|.
name|charFilters
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|charFilterName
init|=
name|request
operator|.
name|charFilters
argument_list|()
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
name|CharFilterFactoryFactory
name|charFilterFactoryFactory
init|=
name|indicesAnalysisService
operator|.
name|charFilterFactoryFactory
argument_list|(
name|charFilterName
argument_list|)
decl_stmt|;
if|if
condition|(
name|charFilterFactoryFactory
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find global char filter under ["
operator|+
name|charFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|charFilterFactories
index|[
name|i
index|]
operator|=
name|charFilterFactoryFactory
operator|.
name|create
argument_list|(
name|charFilterName
argument_list|,
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|EMPTY_SETTINGS
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|charFilterFactories
index|[
name|i
index|]
operator|=
name|indexService
operator|.
name|analysisService
argument_list|()
operator|.
name|charFilter
argument_list|(
name|charFilterName
argument_list|)
expr_stmt|;
if|if
condition|(
name|charFilterFactories
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find token char under ["
operator|+
name|charFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|charFilterFactories
index|[
name|i
index|]
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ElasticsearchIllegalArgumentException
argument_list|(
literal|"failed to find token char under ["
operator|+
name|charFilterName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
block|}
block|}
name|analyzer
operator|=
operator|new
name|CustomAnalyzer
argument_list|(
name|tokenizerFactory
argument_list|,
name|charFilterFactories
argument_list|,
name|tokenFilterFactories
argument_list|)
expr_stmt|;
name|closeAnalyzer
operator|=
literal|true
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
if|if
condition|(
name|indexService
operator|==
literal|null
condition|)
block|{
name|analyzer
operator|=
name|Lucene
operator|.
name|STANDARD_ANALYZER
expr_stmt|;
block|}
else|else
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
name|ElasticsearchIllegalArgumentException
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
name|tokenStream
argument_list|(
name|field
argument_list|,
name|request
operator|.
name|text
argument_list|()
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
name|ElasticsearchException
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
if|if
condition|(
name|closeAnalyzer
condition|)
block|{
name|analyzer
operator|.
name|close
argument_list|()
expr_stmt|;
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

