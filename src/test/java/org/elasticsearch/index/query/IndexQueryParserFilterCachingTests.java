begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|ConstantScoreQuery
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
name|Query
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
name|util
operator|.
name|BytesRef
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
name|util
operator|.
name|NumericUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cache
operator|.
name|recycler
operator|.
name|CacheRecyclerModule
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
name|common
operator|.
name|bytes
operator|.
name|BytesArray
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
name|compress
operator|.
name|CompressedString
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
name|AbstractModule
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
name|Injector
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
name|ModulesBuilder
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
name|util
operator|.
name|Providers
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
name|search
operator|.
name|AndFilter
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
name|search
operator|.
name|CachedFilter
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
name|search
operator|.
name|NoCacheFilter
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
name|search
operator|.
name|XBooleanFilter
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
name|common
operator|.
name|settings
operator|.
name|SettingsModule
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
name|IndexNameModule
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
name|AnalysisModule
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
name|IndexCacheModule
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
name|codec
operator|.
name|CodecModule
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
name|engine
operator|.
name|IndexEngineModule
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
name|MapperServiceModule
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
name|functionscore
operator|.
name|FunctionScoreModule
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
name|search
operator|.
name|child
operator|.
name|TestSearchContext
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
name|settings
operator|.
name|IndexSettingsModule
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
name|SimilarityModule
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
name|fielddata
operator|.
name|breaker
operator|.
name|CircuitBreakerService
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
name|fielddata
operator|.
name|breaker
operator|.
name|DummyCircuitBreakerService
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
name|IndicesQueriesModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|script
operator|.
name|ScriptModule
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
name|internal
operator|.
name|SearchContext
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
name|ElasticsearchTestCase
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
name|threadpool
operator|.
name|ThreadPoolModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|Streams
operator|.
name|copyToBytesFromClasspath
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
name|io
operator|.
name|Streams
operator|.
name|copyToStringFromClasspath
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
name|instanceOf
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|IndexQueryParserFilterCachingTests
specifier|public
class|class
name|IndexQueryParserFilterCachingTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|injector
specifier|private
specifier|static
name|Injector
name|injector
decl_stmt|;
DECL|field|queryParser
specifier|private
specifier|static
name|IndexQueryParserService
name|queryParser
decl_stmt|;
annotation|@
name|BeforeClass
DECL|method|setupQueryParser
specifier|public
specifier|static
name|void
name|setupQueryParser
parameter_list|()
throws|throws
name|IOException
block|{
name|Settings
name|settings
init|=
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.cache.filter.type"
argument_list|,
literal|"weighted"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|Index
name|index
init|=
operator|new
name|Index
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|injector
operator|=
operator|new
name|ModulesBuilder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|CacheRecyclerModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|CodecModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|SettingsModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|ThreadPoolModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndicesQueriesModule
argument_list|()
argument_list|,
operator|new
name|ScriptModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|MapperServiceModule
argument_list|()
argument_list|,
operator|new
name|IndexSettingsModule
argument_list|(
name|index
argument_list|,
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexCacheModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|AnalysisModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexEngineModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|SimilarityModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexQueryParserModule
argument_list|(
name|settings
argument_list|)
argument_list|,
operator|new
name|IndexNameModule
argument_list|(
name|index
argument_list|)
argument_list|,
operator|new
name|FunctionScoreModule
argument_list|()
argument_list|,
operator|new
name|AbstractModule
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bind
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|toProvider
argument_list|(
name|Providers
operator|.
name|of
argument_list|(
operator|(
name|ClusterService
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|CircuitBreakerService
operator|.
name|class
argument_list|)
operator|.
name|to
argument_list|(
name|DummyCircuitBreakerService
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|createInjector
argument_list|()
expr_stmt|;
name|String
name|mapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/mapping.json"
argument_list|)
decl_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|MapperService
operator|.
name|class
argument_list|)
operator|.
name|merge
argument_list|(
literal|"person"
argument_list|,
operator|new
name|CompressedString
argument_list|(
name|mapping
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|String
name|childMapping
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/child-mapping.json"
argument_list|)
decl_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|MapperService
operator|.
name|class
argument_list|)
operator|.
name|merge
argument_list|(
literal|"child"
argument_list|,
operator|new
name|CompressedString
argument_list|(
name|childMapping
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|injector
operator|.
name|getInstance
argument_list|(
name|MapperService
operator|.
name|class
argument_list|)
operator|.
name|documentMapper
argument_list|(
literal|"person"
argument_list|)
operator|.
name|parse
argument_list|(
operator|new
name|BytesArray
argument_list|(
name|copyToBytesFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/data.json"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|queryParser
operator|=
name|injector
operator|.
name|getInstance
argument_list|(
name|IndexQueryParserService
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|close
specifier|public
specifier|static
name|void
name|close
parameter_list|()
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
operator|.
name|shutdownNow
argument_list|()
expr_stmt|;
name|queryParser
operator|=
literal|null
expr_stmt|;
name|injector
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|queryParser
specifier|private
name|IndexQueryParserService
name|queryParser
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|queryParser
return|;
block|}
DECL|method|longToPrefixCoded
specifier|private
name|BytesRef
name|longToPrefixCoded
parameter_list|(
name|long
name|val
parameter_list|,
name|int
name|shift
parameter_list|)
block|{
name|BytesRef
name|bytesRef
init|=
operator|new
name|BytesRef
argument_list|()
decl_stmt|;
name|NumericUtils
operator|.
name|longToPrefixCoded
argument_list|(
name|val
argument_list|,
name|shift
argument_list|,
name|bytesRef
argument_list|)
expr_stmt|;
return|return
name|bytesRef
return|;
block|}
annotation|@
name|Test
DECL|method|testNoFilterParsing
specifier|public
name|void
name|testNoFilterParsing
parameter_list|()
throws|throws
name|IOException
block|{
name|IndexQueryParserService
name|queryParser
init|=
name|queryParser
argument_list|()
decl_stmt|;
name|String
name|query
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/date_range_in_boolean.json"
argument_list|)
decl_stmt|;
name|Query
name|parsedQuery
init|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|XBooleanFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|XBooleanFilter
call|)
argument_list|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
argument_list|)
operator|.
name|getFilter
argument_list|()
operator|)
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NoCacheFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|XBooleanFilter
call|)
argument_list|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
argument_list|)
operator|.
name|getFilter
argument_list|()
operator|)
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/date_range_in_boolean_cached_now.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|XBooleanFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|XBooleanFilter
call|)
argument_list|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
argument_list|)
operator|.
name|getFilter
argument_list|()
operator|)
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NoCacheFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|XBooleanFilter
call|)
argument_list|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
argument_list|)
operator|.
name|getFilter
argument_list|()
operator|)
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/date_range_in_boolean_cached_complex_now.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|XBooleanFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|XBooleanFilter
call|)
argument_list|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
argument_list|)
operator|.
name|getFilter
argument_list|()
operator|)
operator|.
name|clauses
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NoCacheFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|XBooleanFilter
call|)
argument_list|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
argument_list|)
operator|.
name|getFilter
argument_list|()
operator|)
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/date_range_in_boolean_cached.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|CachedFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/date_range_in_boolean_cached_now_with_rounding.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|CachedFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/date_range_in_boolean_cached_complex_now_with_rounding.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|CachedFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|SearchContext
operator|.
name|setCurrent
argument_list|(
operator|new
name|TestSearchContext
argument_list|()
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/has-child.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NoCacheFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/and-filter-cache.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|CachedFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/has-child-in-and-filter-cached.json"
argument_list|)
expr_stmt|;
name|parsedQuery
operator|=
name|queryParser
operator|.
name|parse
argument_list|(
name|query
argument_list|)
operator|.
name|query
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ConstantScoreQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|ConstantScoreQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getFilter
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|AndFilter
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|SearchContext
operator|.
name|removeCurrent
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

