begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.search.scroll
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|integration
operator|.
name|search
operator|.
name|scroll
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
name|unit
operator|.
name|TimeValue
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
name|SearchHit
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
name|integration
operator|.
name|AbstractNodesTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
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
name|Map
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
name|*
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
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|SearchScrollTests
specifier|public
class|class
name|SearchScrollTests
extends|extends
name|AbstractNodesTests
block|{
DECL|field|client
specifier|private
name|Client
name|client
decl_stmt|;
DECL|method|createNodes
annotation|@
name|BeforeClass
specifier|public
name|void
name|createNodes
parameter_list|()
throws|throws
name|Exception
block|{
name|startNode
argument_list|(
literal|"node1"
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"node2"
argument_list|)
expr_stmt|;
name|client
operator|=
name|getClient
argument_list|()
expr_stmt|;
block|}
DECL|method|closeNodes
annotation|@
name|AfterClass
specifier|public
name|void
name|closeNodes
parameter_list|()
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
name|closeAllNodes
argument_list|()
expr_stmt|;
block|}
DECL|method|getClient
specifier|protected
name|Client
name|getClient
parameter_list|()
block|{
return|return
name|client
argument_list|(
literal|"node1"
argument_list|)
return|;
block|}
DECL|method|testSimpleScrollQueryThenFetch
annotation|@
name|Test
specifier|public
name|void
name|testSimpleScrollQueryThenFetch
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|client
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
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|3
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
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
literal|"field"
argument_list|,
name|i
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
block|}
name|client
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
name|SearchResponse
name|searchResponse
init|=
name|client
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSize
argument_list|(
literal|35
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|long
name|counter
init|=
literal|0
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|35
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|hit
operator|.
name|sortValues
argument_list|()
index|[
literal|0
index|]
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|scrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|35
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|hit
operator|.
name|sortValues
argument_list|()
index|[
literal|0
index|]
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|scrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|30
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|hit
operator|.
name|sortValues
argument_list|()
index|[
literal|0
index|]
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSimpleScrollQueryThenFetchSmallSizeUnevenDistribution
annotation|@
name|Test
specifier|public
name|void
name|testSimpleScrollQueryThenFetchSmallSizeUnevenDistribution
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|client
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
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|3
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
literal|100
condition|;
name|i
operator|++
control|)
block|{
name|String
name|routing
init|=
literal|"0"
decl_stmt|;
if|if
condition|(
name|i
operator|>
literal|90
condition|)
block|{
name|routing
operator|=
literal|"1"
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|i
operator|>
literal|60
condition|)
block|{
name|routing
operator|=
literal|"2"
expr_stmt|;
block|}
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
name|i
argument_list|)
operator|.
name|setRouting
argument_list|(
name|routing
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|client
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
name|SearchResponse
name|searchResponse
init|=
name|client
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setSearchType
argument_list|(
name|SearchType
operator|.
name|QUERY_THEN_FETCH
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSize
argument_list|(
literal|3
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|long
name|counter
init|=
literal|0
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|hit
operator|.
name|sortValues
argument_list|()
index|[
literal|0
index|]
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|32
condition|;
name|i
operator|++
control|)
block|{
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|scrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|hit
operator|.
name|sortValues
argument_list|()
index|[
literal|0
index|]
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// and now, the last one is one
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|scrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|hit
operator|.
name|sortValues
argument_list|()
index|[
literal|0
index|]
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// a the last is zero
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|scrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
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
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
control|)
block|{
name|assertThat
argument_list|(
operator|(
operator|(
name|Number
operator|)
name|hit
operator|.
name|sortValues
argument_list|()
index|[
literal|0
index|]
operator|)
operator|.
name|longValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|counter
operator|++
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testScrollAndUpdateIndex
annotation|@
name|Test
specifier|public
name|void
name|testScrollAndUpdateIndex
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|client
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"test"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// ignore
block|}
name|client
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
name|setSettings
argument_list|(
name|ImmutableSettings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.number_of_shards"
argument_list|,
literal|5
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|client
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
name|setWaitForGreenStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
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
literal|500
condition|;
name|i
operator|++
control|)
block|{
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"tweet"
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
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
literal|"user"
argument_list|,
literal|"kimchy"
argument_list|)
operator|.
name|field
argument_list|(
literal|"postDate"
argument_list|,
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
operator|.
name|field
argument_list|(
literal|"message"
argument_list|,
literal|"test"
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
block|}
name|client
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
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|500l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|500l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|500l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"update"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"update"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
operator|.
name|prepareSearch
argument_list|()
operator|.
name|setQuery
argument_list|(
name|queryString
argument_list|(
literal|"user:kimchy"
argument_list|)
argument_list|)
operator|.
name|setSize
argument_list|(
literal|35
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"postDate"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
do|do
block|{
for|for
control|(
name|SearchHit
name|searchHit
range|:
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
control|)
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
name|searchHit
operator|.
name|sourceAsMap
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"message"
argument_list|,
literal|"update"
argument_list|)
expr_stmt|;
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"tweet"
argument_list|,
name|searchHit
operator|.
name|id
argument_list|()
argument_list|)
operator|.
name|setSource
argument_list|(
name|map
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
name|searchResponse
operator|=
name|client
operator|.
name|prepareSearchScroll
argument_list|(
name|searchResponse
operator|.
name|scrollId
argument_list|()
argument_list|)
operator|.
name|setScroll
argument_list|(
name|TimeValue
operator|.
name|timeValueMinutes
argument_list|(
literal|2
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|searchResponse
operator|.
name|hits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
do|;
name|client
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
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|500l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"test"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"update"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|500l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|client
operator|.
name|prepareCount
argument_list|()
operator|.
name|setQuery
argument_list|(
name|termQuery
argument_list|(
literal|"message"
argument_list|,
literal|"update"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|500l
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

