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
comment|/**  * @author kimchy (Shay Banon)  */
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
DECL|method|testFieldFacets
annotation|@
name|Test
specifier|public
name|void
name|testFieldFacets
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
block|}
block|}
end_class

end_unit

