begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.functionscore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|functionscore
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
name|Explanation
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
name|ActionFuture
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
name|search
operator|.
name|SearchResponse
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
name|search
operator|.
name|SearchType
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
name|Priority
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
name|query
operator|.
name|functionscore
operator|.
name|DecayFunction
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
name|DecayFunctionBuilder
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
name|DecayFunctionParser
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
name|plugins
operator|.
name|AbstractPlugin
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
name|SearchHits
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
name|ElasticsearchIntegrationTest
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
name|ElasticsearchIntegrationTest
operator|.
name|ClusterScope
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
name|ElasticsearchIntegrationTest
operator|.
name|Scope
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|indexRequest
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|Requests
operator|.
name|searchRequest
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
name|settings
operator|.
name|Settings
operator|.
name|settingsBuilder
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
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|functionScoreQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|termQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
operator|.
name|searchSource
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

begin_comment
comment|/**  *  */
end_comment

begin_class
annotation|@
name|ClusterScope
argument_list|(
name|scope
operator|=
name|Scope
operator|.
name|SUITE
argument_list|,
name|numDataNodes
operator|=
literal|1
argument_list|)
DECL|class|FunctionScorePluginTests
specifier|public
class|class
name|FunctionScorePluginTests
extends|extends
name|ElasticsearchIntegrationTest
block|{
annotation|@
name|Override
DECL|method|nodeSettings
specifier|protected
name|Settings
name|nodeSettings
parameter_list|(
name|int
name|nodeOrdinal
parameter_list|)
block|{
return|return
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
name|super
operator|.
name|nodeSettings
argument_list|(
name|nodeOrdinal
argument_list|)
argument_list|)
operator|.
name|put
argument_list|(
literal|"plugin.types"
argument_list|,
name|CustomDistanceScorePlugin
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testPlugin
specifier|public
name|void
name|testPlugin
parameter_list|()
throws|throws
name|Exception
block|{
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"test"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"num1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"date"
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
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForEvents
argument_list|(
name|Priority
operator|.
name|LANGUID
argument_list|)
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"1"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num1"
argument_list|,
literal|"2013-05-26"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|index
argument_list|(
name|indexRequest
argument_list|(
literal|"test"
argument_list|)
operator|.
name|type
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|id
argument_list|(
literal|"2"
argument_list|)
operator|.
name|source
argument_list|(
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num1"
argument_list|,
literal|"2013-05-27"
argument_list|)
operator|.
name|endObject
argument_list|()
argument_list|)
argument_list|)
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareRefresh
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|DecayFunctionBuilder
name|gfb
init|=
operator|new
name|CustomDistanceScoreBuilder
argument_list|(
literal|"num1"
argument_list|,
literal|"2013-05-28"
argument_list|,
literal|"+1d"
argument_list|)
decl_stmt|;
name|ActionFuture
argument_list|<
name|SearchResponse
argument_list|>
name|response
init|=
name|client
argument_list|()
operator|.
name|search
argument_list|(
name|searchRequest
argument_list|()
operator|.
name|searchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|source
argument_list|(
name|searchSource
argument_list|()
operator|.
name|explain
argument_list|(
literal|false
argument_list|)
operator|.
name|query
argument_list|(
name|functionScoreQuery
argument_list|(
name|termQuery
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
argument_list|)
operator|.
name|add
argument_list|(
name|gfb
argument_list|)
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|SearchResponse
name|sr
init|=
name|response
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|ElasticsearchAssertions
operator|.
name|assertNoFailures
argument_list|(
name|sr
argument_list|)
expr_stmt|;
name|SearchHits
name|sh
init|=
name|sr
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|sh
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sh
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|sh
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|class|CustomDistanceScorePlugin
specifier|public
specifier|static
class|class
name|CustomDistanceScorePlugin
extends|extends
name|AbstractPlugin
block|{
annotation|@
name|Override
DECL|method|name
specifier|public
name|String
name|name
parameter_list|()
block|{
return|return
literal|"test-plugin-distance-score"
return|;
block|}
annotation|@
name|Override
DECL|method|description
specifier|public
name|String
name|description
parameter_list|()
block|{
return|return
literal|"Distance score plugin to test pluggable implementation"
return|;
block|}
DECL|method|onModule
specifier|public
name|void
name|onModule
parameter_list|(
name|FunctionScoreModule
name|scoreModule
parameter_list|)
block|{
name|scoreModule
operator|.
name|registerParser
argument_list|(
name|FunctionScorePluginTests
operator|.
name|CustomDistanceScoreParser
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
block|}
DECL|class|CustomDistanceScoreParser
specifier|public
specifier|static
class|class
name|CustomDistanceScoreParser
extends|extends
name|DecayFunctionParser
block|{
DECL|field|NAMES
specifier|public
specifier|static
specifier|final
name|String
index|[]
name|NAMES
init|=
block|{
literal|"linear_mult"
block|,
literal|"linearMult"
block|}
decl_stmt|;
annotation|@
name|Override
DECL|method|getNames
specifier|public
name|String
index|[]
name|getNames
parameter_list|()
block|{
return|return
name|NAMES
return|;
block|}
DECL|field|decayFunction
specifier|static
specifier|final
name|DecayFunction
name|decayFunction
init|=
operator|new
name|LinearMultScoreFunction
argument_list|()
decl_stmt|;
annotation|@
name|Override
DECL|method|getDecayFunction
specifier|public
name|DecayFunction
name|getDecayFunction
parameter_list|()
block|{
return|return
name|decayFunction
return|;
block|}
DECL|class|LinearMultScoreFunction
specifier|static
class|class
name|LinearMultScoreFunction
implements|implements
name|DecayFunction
block|{
DECL|method|LinearMultScoreFunction
name|LinearMultScoreFunction
parameter_list|()
block|{             }
annotation|@
name|Override
DECL|method|evaluate
specifier|public
name|double
name|evaluate
parameter_list|(
name|double
name|value
parameter_list|,
name|double
name|scale
parameter_list|)
block|{
return|return
name|value
return|;
block|}
annotation|@
name|Override
DECL|method|explainFunction
specifier|public
name|Explanation
name|explainFunction
parameter_list|(
name|String
name|distanceString
parameter_list|,
name|double
name|distanceVal
parameter_list|,
name|double
name|scale
parameter_list|)
block|{
return|return
name|Explanation
operator|.
name|match
argument_list|(
operator|(
name|float
operator|)
name|distanceVal
argument_list|,
literal|""
operator|+
name|distanceVal
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|processScale
specifier|public
name|double
name|processScale
parameter_list|(
name|double
name|userGivenScale
parameter_list|,
name|double
name|userGivenValue
parameter_list|)
block|{
return|return
name|userGivenScale
return|;
block|}
block|}
block|}
DECL|class|CustomDistanceScoreBuilder
specifier|public
class|class
name|CustomDistanceScoreBuilder
extends|extends
name|DecayFunctionBuilder
block|{
DECL|method|CustomDistanceScoreBuilder
specifier|public
name|CustomDistanceScoreBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|origin
parameter_list|,
name|Object
name|scale
parameter_list|)
block|{
name|super
argument_list|(
name|fieldName
argument_list|,
name|origin
argument_list|,
name|scale
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|CustomDistanceScoreParser
operator|.
name|NAMES
index|[
literal|0
index|]
return|;
block|}
block|}
block|}
end_class

end_unit
