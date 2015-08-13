begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.scriptfilter
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|scriptfilter
package|;
end_package

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
name|cache
operator|.
name|query
operator|.
name|index
operator|.
name|IndexQueryCache
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
name|Script
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
operator|.
name|ScriptType
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
name|SortOrder
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
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|filteredQuery
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
name|matchAllQuery
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
name|scriptQuery
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
name|ESIntegTestCase
operator|.
name|ClusterScope
argument_list|(
name|scope
operator|=
name|ESIntegTestCase
operator|.
name|Scope
operator|.
name|SUITE
argument_list|)
DECL|class|ScriptQuerySearchIT
specifier|public
class|class
name|ScriptQuerySearchIT
extends|extends
name|ESIntegTestCase
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
name|Settings
operator|.
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
comment|// aggressive filter caching so that we can assert on the number of iterations of the script filters
operator|.
name|put
argument_list|(
name|IndexCacheModule
operator|.
name|QUERY_CACHE_TYPE
argument_list|,
name|IndexCacheModule
operator|.
name|INDEX_QUERY_CACHE
argument_list|)
operator|.
name|put
argument_list|(
name|IndexCacheModule
operator|.
name|QUERY_CACHE_EVERYTHING
argument_list|,
literal|true
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Test
DECL|method|testCustomScriptBoost
specifier|public
name|void
name|testCustomScriptBoost
parameter_list|()
throws|throws
name|Exception
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
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
literal|"value beck"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num1"
argument_list|,
literal|1.0f
argument_list|)
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
name|flush
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
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
literal|"value beck"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num1"
argument_list|,
literal|2.0f
argument_list|)
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
name|flush
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"3"
argument_list|)
operator|.
name|setSource
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
literal|"value beck"
argument_list|)
operator|.
name|field
argument_list|(
literal|"num1"
argument_list|,
literal|3.0f
argument_list|)
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
name|refresh
argument_list|()
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running doc['num1'].value> 1"
argument_list|)
expr_stmt|;
name|SearchResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|filteredQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc['num1'].value> 1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"num1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"sNum1"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['num1'].value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|Double
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
literal|"sNum1"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|Double
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
literal|"sNum1"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3.0
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|params
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"param1"
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running doc['num1'].value> param1"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc['num1'].value> param1"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|null
argument_list|,
name|params
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"num1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"sNum1"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['num1'].value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|Double
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
literal|"sNum1"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3.0
argument_list|)
argument_list|)
expr_stmt|;
name|params
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|params
operator|.
name|put
argument_list|(
literal|"param1"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
name|logger
operator|.
name|info
argument_list|(
literal|"running doc['num1'].value> param1"
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|filteredQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|,
name|scriptQuery
argument_list|(
operator|new
name|Script
argument_list|(
literal|"doc['num1'].value> param1"
argument_list|,
name|ScriptType
operator|.
name|INLINE
argument_list|,
literal|null
argument_list|,
name|params
argument_list|)
argument_list|)
argument_list|)
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"num1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|addScriptField
argument_list|(
literal|"sNum1"
argument_list|,
operator|new
name|Script
argument_list|(
literal|"doc['num1'].value"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|totalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|id
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
operator|(
name|Double
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|0
argument_list|)
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
literal|"sNum1"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|1.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|Double
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|1
argument_list|)
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
literal|"sNum1"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|2.0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|2
argument_list|)
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|Double
operator|)
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getAt
argument_list|(
literal|2
argument_list|)
operator|.
name|fields
argument_list|()
operator|.
name|get
argument_list|(
literal|"sNum1"
argument_list|)
operator|.
name|values
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|3.0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|field|scriptCounter
specifier|private
specifier|static
name|AtomicInteger
name|scriptCounter
init|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
decl_stmt|;
DECL|method|incrementScriptCounter
specifier|public
specifier|static
name|int
name|incrementScriptCounter
parameter_list|()
block|{
return|return
name|scriptCounter
operator|.
name|incrementAndGet
argument_list|()
return|;
block|}
block|}
end_class

end_unit

