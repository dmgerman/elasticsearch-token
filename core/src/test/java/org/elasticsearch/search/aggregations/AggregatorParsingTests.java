begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

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
name|cluster
operator|.
name|metadata
operator|.
name|MetaData
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
name|service
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
name|ParseFieldMatcher
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
name|ParsingException
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
name|Setting
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|XContentParser
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
name|json
operator|.
name|JsonXContent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|env
operator|.
name|Environment
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
name|test
operator|.
name|AbstractQueryTestCase
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
name|QueryParseContext
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
name|breaker
operator|.
name|NoneCircuitBreakerService
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
name|script
operator|.
name|ScriptService
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
name|SearchModule
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
name|elasticsearch
operator|.
name|test
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
name|test
operator|.
name|InternalSettingsPlugin
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
name|VersionUtils
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
name|Collections
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Matcher
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
name|ClusterServiceUtils
operator|.
name|createClusterService
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
name|ClusterServiceUtils
operator|.
name|setState
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
name|containsString
import|;
end_import

begin_class
DECL|class|AggregatorParsingTests
specifier|public
class|class
name|AggregatorParsingTests
extends|extends
name|ESTestCase
block|{
DECL|field|injector
specifier|private
specifier|static
name|Injector
name|injector
decl_stmt|;
DECL|field|index
specifier|private
specifier|static
name|Index
name|index
decl_stmt|;
DECL|field|currentTypes
specifier|private
specifier|static
name|String
index|[]
name|currentTypes
decl_stmt|;
DECL|method|getCurrentTypes
specifier|protected
specifier|static
name|String
index|[]
name|getCurrentTypes
parameter_list|()
block|{
return|return
name|currentTypes
return|;
block|}
DECL|field|namedWriteableRegistry
specifier|private
specifier|static
name|NamedWriteableRegistry
name|namedWriteableRegistry
decl_stmt|;
DECL|field|aggParsers
specifier|protected
specifier|static
name|AggregatorParsers
name|aggParsers
decl_stmt|;
DECL|field|queriesRegistry
specifier|protected
specifier|static
name|IndicesQueriesRegistry
name|queriesRegistry
decl_stmt|;
DECL|field|parseFieldMatcher
specifier|protected
specifier|static
name|ParseFieldMatcher
name|parseFieldMatcher
decl_stmt|;
comment|/**      * Setup for the whole base test class.      */
annotation|@
name|BeforeClass
DECL|method|init
specifier|public
specifier|static
name|void
name|init
parameter_list|()
throws|throws
name|IOException
block|{
comment|// we have to prefer CURRENT since with the range of versions we support
comment|// it's rather unlikely to get the current actually.
name|Version
name|version
init|=
name|randomBoolean
argument_list|()
condition|?
name|Version
operator|.
name|CURRENT
else|:
name|VersionUtils
operator|.
name|randomVersionBetween
argument_list|(
name|random
argument_list|()
argument_list|,
name|Version
operator|.
name|V_2_0_0_beta1
argument_list|,
name|Version
operator|.
name|CURRENT
argument_list|)
decl_stmt|;
name|Settings
name|settings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"node.name"
argument_list|,
name|AbstractQueryTestCase
operator|.
name|class
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|Environment
operator|.
name|PATH_HOME_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
name|createTempDir
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
name|ScriptService
operator|.
name|SCRIPT_AUTO_RELOAD_ENABLED_SETTING
operator|.
name|getKey
argument_list|()
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|namedWriteableRegistry
operator|=
operator|new
name|NamedWriteableRegistry
argument_list|()
expr_stmt|;
name|index
operator|=
operator|new
name|Index
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|"_na_"
argument_list|)
expr_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|version
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|ThreadPool
name|threadPool
init|=
operator|new
name|ThreadPool
argument_list|(
name|settings
argument_list|)
decl_stmt|;
specifier|final
name|ClusterService
name|clusterService
init|=
name|createClusterService
argument_list|(
name|threadPool
argument_list|)
decl_stmt|;
name|setState
argument_list|(
name|clusterService
argument_list|,
operator|new
name|ClusterState
operator|.
name|Builder
argument_list|(
name|clusterService
operator|.
name|state
argument_list|()
argument_list|)
operator|.
name|metaData
argument_list|(
operator|new
name|MetaData
operator|.
name|Builder
argument_list|()
operator|.
name|put
argument_list|(
operator|new
name|IndexMetaData
operator|.
name|Builder
argument_list|(
name|index
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|settings
argument_list|(
name|indexSettings
argument_list|)
operator|.
name|numberOfShards
argument_list|(
literal|1
argument_list|)
operator|.
name|numberOfReplicas
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ScriptModule
name|scriptModule
init|=
name|newTestScriptModule
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Setting
argument_list|<
name|?
argument_list|>
argument_list|>
name|scriptSettings
init|=
name|scriptModule
operator|.
name|getSettings
argument_list|()
decl_stmt|;
name|scriptSettings
operator|.
name|add
argument_list|(
name|InternalSettingsPlugin
operator|.
name|VERSION_CREATED
argument_list|)
expr_stmt|;
name|SettingsModule
name|settingsModule
init|=
operator|new
name|SettingsModule
argument_list|(
name|settings
argument_list|,
name|scriptSettings
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
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
parameter_list|(
name|b
parameter_list|)
lambda|->
block|{
name|b
operator|.
name|bind
argument_list|(
name|Environment
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
operator|new
name|Environment
argument_list|(
name|settings
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|.
name|bind
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|threadPool
argument_list|)
expr_stmt|;
name|b
operator|.
name|bind
argument_list|(
name|ScriptService
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|scriptModule
operator|.
name|getScriptService
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|settingsModule
argument_list|,
operator|new
name|IndicesModule
argument_list|(
name|namedWriteableRegistry
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|bindMapperExtension
argument_list|()
expr_stmt|;
block|}
block|}
argument_list|,
operator|new
name|SearchModule
argument_list|(
name|settings
argument_list|,
name|namedWriteableRegistry
argument_list|)
block|{
annotation|@
name|Override
specifier|protected
name|void
name|configureSearch
parameter_list|()
block|{
comment|// Skip me
block|}
block|}
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
name|toInstance
argument_list|(
name|clusterService
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|CircuitBreakerService
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
operator|new
name|NoneCircuitBreakerService
argument_list|()
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|NamedWriteableRegistry
operator|.
name|class
argument_list|)
operator|.
name|toInstance
argument_list|(
name|namedWriteableRegistry
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
operator|.
name|createInjector
argument_list|()
expr_stmt|;
name|aggParsers
operator|=
name|injector
operator|.
name|getInstance
argument_list|(
name|AggregatorParsers
operator|.
name|class
argument_list|)
expr_stmt|;
comment|// create some random type with some default field, those types will
comment|// stick around for all of the subclasses
name|currentTypes
operator|=
operator|new
name|String
index|[
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|5
argument_list|)
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
name|currentTypes
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|type
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|currentTypes
index|[
name|i
index|]
operator|=
name|type
expr_stmt|;
block|}
name|queriesRegistry
operator|=
name|injector
operator|.
name|getInstance
argument_list|(
name|IndicesQueriesRegistry
operator|.
name|class
argument_list|)
expr_stmt|;
name|parseFieldMatcher
operator|=
name|ParseFieldMatcher
operator|.
name|STRICT
expr_stmt|;
block|}
annotation|@
name|AfterClass
DECL|method|afterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|injector
operator|.
name|getInstance
argument_list|(
name|ClusterService
operator|.
name|class
argument_list|)
operator|.
name|close
argument_list|()
expr_stmt|;
name|terminate
argument_list|(
name|injector
operator|.
name|getInstance
argument_list|(
name|ThreadPool
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|injector
operator|=
literal|null
expr_stmt|;
name|index
operator|=
literal|null
expr_stmt|;
name|aggParsers
operator|=
literal|null
expr_stmt|;
name|currentTypes
operator|=
literal|null
expr_stmt|;
name|namedWriteableRegistry
operator|=
literal|null
expr_stmt|;
block|}
DECL|method|testTwoTypes
specifier|public
name|void
name|testTwoTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"in_stock"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"filter"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"range"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"stock"
argument_list|)
operator|.
name|field
argument_list|(
literal|"gt"
argument_list|,
literal|0
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
operator|.
name|startObject
argument_list|(
literal|"terms"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"stock"
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
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|parseAggregators
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Found two aggregation type definitions in [in_stock]: [filter] and [terms]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testTwoAggs
specifier|public
name|void
name|testTwoAggs
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"by_date"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"date_histogram"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"interval"
argument_list|,
literal|"month"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"aggs"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"tag_count"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"cardinality"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"tag"
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
operator|.
name|startObject
argument_list|(
literal|"aggs"
argument_list|)
comment|// 2nd "aggs": illegal
operator|.
name|startObject
argument_list|(
literal|"tag_count2"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"cardinality"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"tag"
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
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|parseAggregators
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Found two sub aggregation definitions under [by_date]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testInvalidAggregationName
specifier|public
name|void
name|testInvalidAggregationName
parameter_list|()
throws|throws
name|Exception
block|{
name|Matcher
name|matcher
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"[^\\[\\]>]+"
argument_list|)
operator|.
name|matcher
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|String
name|name
decl_stmt|;
name|Random
name|rand
init|=
name|random
argument_list|()
decl_stmt|;
name|int
name|len
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|char
index|[]
name|word
init|=
operator|new
name|char
index|[
name|len
index|]
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|word
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|word
index|[
name|i
index|]
operator|=
operator|(
name|char
operator|)
name|rand
operator|.
name|nextInt
argument_list|(
literal|127
argument_list|)
expr_stmt|;
block|}
name|name
operator|=
name|String
operator|.
name|valueOf
argument_list|(
name|word
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|matcher
operator|.
name|reset
argument_list|(
name|name
argument_list|)
operator|.
name|matches
argument_list|()
condition|)
block|{
break|break;
block|}
block|}
name|String
name|source
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"filter"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"range"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"stock"
argument_list|)
operator|.
name|field
argument_list|(
literal|"gt"
argument_list|,
literal|0
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
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|parseAggregators
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Invalid aggregation name ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSameAggregationName
specifier|public
name|void
name|testSameAggregationName
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|name
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|source
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"terms"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"a"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"terms"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"b"
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
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|parseAggregators
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
name|fail
argument_list|()
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
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"Two sibling aggregations cannot have the same name: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testMissingName
specifier|public
name|void
name|testMissingName
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"by_date"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"date_histogram"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"interval"
argument_list|,
literal|"month"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"aggs"
argument_list|)
comment|// the aggregation name is missing
comment|//.startObject("tag_count")
operator|.
name|startObject
argument_list|(
literal|"cardinality"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"tag"
argument_list|)
operator|.
name|endObject
argument_list|()
comment|//.endObject()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|parseAggregators
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
comment|// All Good
block|}
block|}
DECL|method|testMissingType
specifier|public
name|void
name|testMissingType
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|source
init|=
name|JsonXContent
operator|.
name|contentBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"by_date"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"date_histogram"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"timestamp"
argument_list|)
operator|.
name|field
argument_list|(
literal|"interval"
argument_list|,
literal|"month"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"aggs"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"tag_count"
argument_list|)
comment|// the aggregation type is missing
comment|//.startObject("cardinality")
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
literal|"tag"
argument_list|)
comment|//.endObject()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
try|try
block|{
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|source
argument_list|)
operator|.
name|createParser
argument_list|(
name|source
argument_list|)
decl_stmt|;
name|QueryParseContext
name|parseContext
init|=
operator|new
name|QueryParseContext
argument_list|(
name|queriesRegistry
argument_list|,
name|parser
argument_list|,
name|parseFieldMatcher
argument_list|)
decl_stmt|;
name|assertSame
argument_list|(
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
argument_list|,
name|parser
operator|.
name|nextToken
argument_list|()
argument_list|)
expr_stmt|;
name|aggParsers
operator|.
name|parseAggregators
argument_list|(
name|parseContext
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParsingException
name|e
parameter_list|)
block|{
comment|// All Good
block|}
block|}
block|}
end_class

end_unit

