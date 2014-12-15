begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
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
name|test
operator|.
name|ElasticsearchSingleNodeTest
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
name|ExecutionException
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertAcked
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
name|notNullValue
import|;
end_import

begin_class
DECL|class|SearchServiceTests
specifier|public
class|class
name|SearchServiceTests
extends|extends
name|ElasticsearchSingleNodeTest
block|{
annotation|@
name|Override
DECL|method|resetNodeAfterTest
specifier|protected
name|boolean
name|resetNodeAfterTest
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|testClearOnClose
specifier|public
name|void
name|testClearOnClose
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
name|createIndex
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setScroll
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SearchService
name|service
init|=
name|getInstanceFromNode
argument_list|(
name|SearchService
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|service
operator|.
name|getActiveContexts
argument_list|()
argument_list|)
expr_stmt|;
name|service
operator|.
name|doClose
argument_list|()
expr_stmt|;
comment|// this kills the keep-alive reaper we have to reset the node after this test
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|service
operator|.
name|getActiveContexts
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testClearOnStop
specifier|public
name|void
name|testClearOnStop
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
name|createIndex
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setScroll
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SearchService
name|service
init|=
name|getInstanceFromNode
argument_list|(
name|SearchService
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|service
operator|.
name|getActiveContexts
argument_list|()
argument_list|)
expr_stmt|;
name|service
operator|.
name|doStop
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|service
operator|.
name|getActiveContexts
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testClearIndexDelete
specifier|public
name|void
name|testClearIndexDelete
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
name|createIndex
argument_list|(
literal|"index"
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|setRefresh
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"index"
argument_list|)
operator|.
name|setSize
argument_list|(
literal|1
argument_list|)
operator|.
name|setScroll
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getScrollId
argument_list|()
argument_list|,
name|is
argument_list|(
name|notNullValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SearchService
name|service
init|=
name|getInstanceFromNode
argument_list|(
name|SearchService
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|service
operator|.
name|getActiveContexts
argument_list|()
argument_list|)
expr_stmt|;
name|assertAcked
argument_list|(
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareDelete
argument_list|(
literal|"index"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|service
operator|.
name|getActiveContexts
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

