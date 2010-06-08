begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.integration.search.facets
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
name|facets
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
name|index
operator|.
name|query
operator|.
name|xcontent
operator|.
name|QueryBuilders
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
name|facets
operator|.
name|histogram
operator|.
name|HistogramFacet
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
name|facets
operator|.
name|statistical
operator|.
name|StatisticalFacet
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
name|facets
operator|.
name|terms
operator|.
name|TermsFacet
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|xcontent
operator|.
name|FilterBuilders
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
name|xcontent
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
name|elasticsearch
operator|.
name|util
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
DECL|class|SimpleFacetsTests
specifier|public
class|class
name|SimpleFacetsTests
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
literal|"server1"
argument_list|)
expr_stmt|;
name|startNode
argument_list|(
literal|"server2"
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
literal|"server1"
argument_list|)
return|;
block|}
DECL|method|testTermsFacets
annotation|@
name|Test
specifier|public
name|void
name|testTermsFacets
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
literal|"stag"
argument_list|,
literal|"111"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"tag"
argument_list|)
operator|.
name|value
argument_list|(
literal|"xxx"
argument_list|)
operator|.
name|value
argument_list|(
literal|"yyy"
argument_list|)
operator|.
name|endArray
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
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|setRefresh
argument_list|(
literal|true
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
literal|"stag"
argument_list|,
literal|"111"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"tag"
argument_list|)
operator|.
name|value
argument_list|(
literal|"zzz"
argument_list|)
operator|.
name|value
argument_list|(
literal|"yyy"
argument_list|)
operator|.
name|endArray
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
name|termQuery
argument_list|(
literal|"stag"
argument_list|,
literal|"111"
argument_list|)
argument_list|)
operator|.
name|addFacetTerms
argument_list|(
literal|"facet1"
argument_list|,
literal|"stag"
argument_list|,
literal|10
argument_list|)
operator|.
name|addFacetTerms
argument_list|(
literal|"facet2"
argument_list|,
literal|"tag"
argument_list|,
literal|10
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|TermsFacet
name|facet
init|=
name|searchResponse
operator|.
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
name|TermsFacet
operator|.
name|class
argument_list|,
literal|"facet1"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"facet1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|term
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"111"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|facet
operator|=
name|searchResponse
operator|.
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
name|TermsFacet
operator|.
name|class
argument_list|,
literal|"facet2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"facet2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|term
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"yyy"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|searchResponse
operator|=
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
name|addFacetTerms
argument_list|(
literal|"facet1"
argument_list|,
literal|"stag"
argument_list|,
literal|10
argument_list|,
name|termFilter
argument_list|(
literal|"tag"
argument_list|,
literal|"xxx"
argument_list|)
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
name|facet
operator|=
name|searchResponse
operator|.
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
name|TermsFacet
operator|.
name|class
argument_list|,
literal|"facet1"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"facet1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|term
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"111"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testStatsFacets
annotation|@
name|Test
specifier|public
name|void
name|testStatsFacets
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
literal|"num"
argument_list|,
literal|1
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"multi_num"
argument_list|)
operator|.
name|value
argument_list|(
literal|1.0
argument_list|)
operator|.
name|value
argument_list|(
literal|2.0f
argument_list|)
operator|.
name|endArray
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
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|setRefresh
argument_list|(
literal|true
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
literal|"num"
argument_list|,
literal|2
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"multi_num"
argument_list|)
operator|.
name|value
argument_list|(
literal|3.0
argument_list|)
operator|.
name|value
argument_list|(
literal|4.0f
argument_list|)
operator|.
name|endArray
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
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addFacetStatistical
argument_list|(
literal|"stats1"
argument_list|,
literal|"num"
argument_list|)
operator|.
name|addFacetStatistical
argument_list|(
literal|"stats2"
argument_list|,
literal|"multi_num"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|StatisticalFacet
name|facet
init|=
name|searchResponse
operator|.
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
name|StatisticalFacet
operator|.
name|class
argument_list|,
literal|"stats1"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|count
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
name|facet
operator|.
name|total
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|min
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|max
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|mean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.5d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|sumOfSquares
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|5d
argument_list|)
argument_list|)
expr_stmt|;
name|facet
operator|=
name|searchResponse
operator|.
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
name|StatisticalFacet
operator|.
name|class
argument_list|,
literal|"stats2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|total
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|min
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|max
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|4d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|mean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2.5d
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHistoFacets
annotation|@
name|Test
specifier|public
name|void
name|testHistoFacets
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
literal|"num"
argument_list|,
literal|1055
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"multi_num"
argument_list|)
operator|.
name|value
argument_list|(
literal|13.0f
argument_list|)
operator|.
name|value
argument_list|(
literal|23.f
argument_list|)
operator|.
name|endArray
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
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareFlush
argument_list|()
operator|.
name|setRefresh
argument_list|(
literal|true
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
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
literal|"num"
argument_list|,
literal|1065
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"multi_num"
argument_list|)
operator|.
name|value
argument_list|(
literal|15.0f
argument_list|)
operator|.
name|value
argument_list|(
literal|31.0f
argument_list|)
operator|.
name|endArray
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
name|client
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
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
literal|"num"
argument_list|,
literal|1175
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"multi_num"
argument_list|)
operator|.
name|value
argument_list|(
literal|17.0f
argument_list|)
operator|.
name|value
argument_list|(
literal|25.0f
argument_list|)
operator|.
name|endArray
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
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addFacetHistogram
argument_list|(
literal|"stats1"
argument_list|,
literal|"num"
argument_list|,
literal|100
argument_list|)
operator|.
name|addFacetHistogram
argument_list|(
literal|"stats2"
argument_list|,
literal|"multi_num"
argument_list|,
literal|10
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|HistogramFacet
name|facet
init|=
name|searchResponse
operator|.
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
name|HistogramFacet
operator|.
name|class
argument_list|,
literal|"stats1"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"stats1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1000l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|count
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
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|total
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2120d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|mean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1060d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1100l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|count
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
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|total
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1175d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|mean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1175d
argument_list|)
argument_list|)
expr_stmt|;
name|facet
operator|=
name|searchResponse
operator|.
name|facets
argument_list|()
operator|.
name|facet
argument_list|(
name|HistogramFacet
operator|.
name|class
argument_list|,
literal|"stats2"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"stats2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|10l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|count
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
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|total
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|45d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|mean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|15d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|20l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|count
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
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|total
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|48d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|mean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|24d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|value
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|30l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|count
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
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|total
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|31d
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|facet
operator|.
name|entries
argument_list|()
operator|.
name|get
argument_list|(
literal|2
argument_list|)
operator|.
name|mean
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|31d
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

