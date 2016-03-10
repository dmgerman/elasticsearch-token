begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
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
name|index
operator|.
name|Term
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
name|TermQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
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
name|percolate
operator|.
name|PercolateShardRequest
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
name|metadata
operator|.
name|IndexMetaData
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
name|stream
operator|.
name|NamedWriteableRegistry
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
name|index
operator|.
name|Index
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
name|analysis
operator|.
name|AnalysisService
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
name|AnalyzerProvider
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
name|CharFilterFactory
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
name|TokenFilterFactory
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
name|TokenizerFactory
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
name|MapperService
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
name|ParsedDocument
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
name|QueryParser
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
name|QueryShardContext
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
name|TermQueryParser
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
name|shard
operator|.
name|ShardId
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
name|similarity
operator|.
name|SimilarityService
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
name|IndicesModule
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
name|query
operator|.
name|IndicesQueriesRegistry
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|SearchShardTarget
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationBinaryParseElement
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationParseElement
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregationPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|AggregatorParsers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
operator|.
name|HighlightPhase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|highlight
operator|.
name|Highlighters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|sort
operator|.
name|SortParseElement
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
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singletonMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|jsonBuilder
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_class
DECL|class|PercolateDocumentParserTests
specifier|public
class|class
name|PercolateDocumentParserTests
extends|extends
name|ESTestCase
block|{
DECL|field|mapperService
specifier|private
name|MapperService
name|mapperService
decl_stmt|;
DECL|field|parser
specifier|private
name|PercolateDocumentParser
name|parser
decl_stmt|;
DECL|field|queryShardContext
specifier|private
name|QueryShardContext
name|queryShardContext
decl_stmt|;
DECL|field|request
specifier|private
name|PercolateShardRequest
name|request
decl_stmt|;
annotation|@
name|Before
DECL|method|init
specifier|public
name|void
name|init
parameter_list|()
block|{
name|IndexSettings
name|indexSettings
init|=
operator|new
name|IndexSettings
argument_list|(
operator|new
name|IndexMetaData
operator|.
name|Builder
argument_list|(
literal|"_index"
argument_list|)
operator|.
name|settings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_SHARDS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_NUMBER_OF_REPLICAS
argument_list|,
literal|1
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|AnalysisService
name|analysisService
init|=
operator|new
name|AnalysisService
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|AnalyzerProvider
operator|>
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|TokenizerFactory
operator|>
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|CharFilterFactory
operator|>
name|emptyMap
argument_list|()
argument_list|,
name|Collections
operator|.
expr|<
name|String
argument_list|,
name|TokenFilterFactory
operator|>
name|emptyMap
argument_list|()
argument_list|)
decl_stmt|;
name|IndicesModule
name|indicesModule
init|=
operator|new
name|IndicesModule
argument_list|()
decl_stmt|;
name|mapperService
operator|=
operator|new
name|MapperService
argument_list|(
name|indexSettings
argument_list|,
name|analysisService
argument_list|,
operator|new
name|SimilarityService
argument_list|(
name|indexSettings
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
argument_list|,
name|indicesModule
operator|.
name|getMapperRegistry
argument_list|()
argument_list|,
parameter_list|()
lambda|->
literal|null
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|QueryParser
argument_list|<
name|?
argument_list|>
argument_list|>
name|parsers
init|=
name|singletonMap
argument_list|(
literal|"term"
argument_list|,
operator|new
name|TermQueryParser
argument_list|()
argument_list|)
decl_stmt|;
name|IndicesQueriesRegistry
name|indicesQueriesRegistry
init|=
operator|new
name|IndicesQueriesRegistry
argument_list|(
name|indexSettings
operator|.
name|getSettings
argument_list|()
argument_list|,
name|parsers
argument_list|)
decl_stmt|;
name|queryShardContext
operator|=
operator|new
name|QueryShardContext
argument_list|(
name|indexSettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|mapperService
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|indicesQueriesRegistry
argument_list|)
expr_stmt|;
name|HighlightPhase
name|highlightPhase
init|=
operator|new
name|HighlightPhase
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|,
operator|new
name|Highlighters
argument_list|()
argument_list|)
decl_stmt|;
name|AggregatorParsers
name|aggregatorParsers
init|=
operator|new
name|AggregatorParsers
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
operator|new
name|NamedWriteableRegistry
argument_list|()
argument_list|)
decl_stmt|;
name|AggregationPhase
name|aggregationPhase
init|=
operator|new
name|AggregationPhase
argument_list|(
operator|new
name|AggregationParseElement
argument_list|(
name|aggregatorParsers
argument_list|,
name|indicesQueriesRegistry
argument_list|)
argument_list|,
operator|new
name|AggregationBinaryParseElement
argument_list|(
name|aggregatorParsers
argument_list|,
name|indicesQueriesRegistry
argument_list|)
argument_list|)
decl_stmt|;
name|parser
operator|=
operator|new
name|PercolateDocumentParser
argument_list|(
name|highlightPhase
argument_list|,
operator|new
name|SortParseElement
argument_list|()
argument_list|,
name|aggregationPhase
argument_list|)
expr_stmt|;
name|request
operator|=
name|Mockito
operator|.
name|mock
argument_list|(
name|PercolateShardRequest
operator|.
name|class
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|shardId
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
operator|new
name|ShardId
argument_list|(
literal|"_index"
argument_list|,
literal|"_na_"
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|documentType
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"type"
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseDoc
specifier|public
name|void
name|testParseDoc
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|source
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|PercolateContext
name|context
init|=
operator|new
name|PercolateContext
argument_list|(
name|request
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"_node"
argument_list|,
operator|new
name|Index
argument_list|(
literal|"_index"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|mapperService
argument_list|,
name|queryShardContext
argument_list|)
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
name|parser
operator|.
name|parse
argument_list|(
name|request
argument_list|,
name|context
argument_list|,
name|mapperService
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedDocument
operator|.
name|rootDoc
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseDocAndOtherOptions
specifier|public
name|void
name|testParseDocAndOtherOptions
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"query"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"term"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"track_scores"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
literal|123
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"sort"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_score"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|source
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|PercolateContext
name|context
init|=
operator|new
name|PercolateContext
argument_list|(
name|request
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"_node"
argument_list|,
operator|new
name|Index
argument_list|(
literal|"_index"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|mapperService
argument_list|,
name|queryShardContext
argument_list|)
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
name|parser
operator|.
name|parse
argument_list|(
name|request
argument_list|,
name|context
argument_list|,
name|mapperService
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedDocument
operator|.
name|rootDoc
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|percolateQuery
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|trackScores
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|123
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|sort
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseDocSource
specifier|public
name|void
name|testParseDocSource
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"query"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"term"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"track_scores"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
literal|123
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"sort"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_score"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentBuilder
name|docSource
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|source
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|docSource
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|docSource
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|PercolateContext
name|context
init|=
operator|new
name|PercolateContext
argument_list|(
name|request
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"_node"
argument_list|,
operator|new
name|Index
argument_list|(
literal|"_index"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|mapperService
argument_list|,
name|queryShardContext
argument_list|)
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
name|parser
operator|.
name|parse
argument_list|(
name|request
argument_list|,
name|context
argument_list|,
name|mapperService
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedDocument
operator|.
name|rootDoc
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|percolateQuery
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|trackScores
argument_list|()
argument_list|,
name|is
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|123
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|context
operator|.
name|sort
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testParseDocSourceAndSource
specifier|public
name|void
name|testParseDocSourceAndSource
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|source
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"doc"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"query"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"term"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"track_scores"
argument_list|,
literal|true
argument_list|)
operator|.
name|field
argument_list|(
literal|"size"
argument_list|,
literal|123
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"sort"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_score"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentBuilder
name|docSource
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|source
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|source
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|request
operator|.
name|docSource
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|docSource
operator|.
name|bytes
argument_list|()
argument_list|)
expr_stmt|;
name|PercolateContext
name|context
init|=
operator|new
name|PercolateContext
argument_list|(
name|request
argument_list|,
operator|new
name|SearchShardTarget
argument_list|(
literal|"_node"
argument_list|,
operator|new
name|Index
argument_list|(
literal|"_index"
argument_list|,
literal|"_na_"
argument_list|)
argument_list|,
literal|0
argument_list|)
argument_list|,
name|mapperService
argument_list|,
name|queryShardContext
argument_list|)
decl_stmt|;
try|try
block|{
name|parser
operator|.
name|parse
argument_list|(
name|request
argument_list|,
name|context
argument_list|,
name|mapperService
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"Can't specify the document to percolate in the source of the request and as document id"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
