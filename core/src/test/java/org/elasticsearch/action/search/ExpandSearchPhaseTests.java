begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.search
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|ActionListener
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
name|text
operator|.
name|Text
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
name|BoolQueryBuilder
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
name|InnerHitBuilder
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
name|QueryBuilder
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
name|SearchHitField
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
name|search
operator|.
name|builder
operator|.
name|SearchSourceBuilder
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
name|collapse
operator|.
name|CollapseBuilder
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
name|InternalSearchResponse
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
name|hamcrest
operator|.
name|Matchers
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|AtomicReference
import|;
end_import

begin_class
DECL|class|ExpandSearchPhaseTests
specifier|public
class|class
name|ExpandSearchPhaseTests
extends|extends
name|ESTestCase
block|{
DECL|method|testCollapseSingleHit
specifier|public
name|void
name|testCollapseSingleHit
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|int
name|iters
init|=
name|randomIntBetween
argument_list|(
literal|5
argument_list|,
literal|10
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|iters
condition|;
name|i
operator|++
control|)
block|{
name|SearchHits
name|collapsedHits
init|=
operator|new
name|SearchHits
argument_list|(
operator|new
name|SearchHit
index|[]
block|{
operator|new
name|SearchHit
argument_list|(
literal|2
argument_list|,
literal|"ID"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
block|,
operator|new
name|SearchHit
argument_list|(
literal|3
argument_list|,
literal|"ID"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
block|}
argument_list|,
literal|1
argument_list|,
literal|1.0F
argument_list|)
decl_stmt|;
name|AtomicBoolean
name|executedMultiSearch
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|QueryBuilder
name|originalQuery
init|=
name|randomBoolean
argument_list|()
condition|?
literal|null
else|:
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|MockSearchPhaseContext
name|mockSearchPhaseContext
init|=
operator|new
name|MockSearchPhaseContext
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|collapseValue
init|=
name|randomBoolean
argument_list|()
condition|?
literal|null
else|:
literal|"boom"
decl_stmt|;
name|mockSearchPhaseContext
operator|.
name|getRequest
argument_list|()
operator|.
name|source
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|collapse
argument_list|(
operator|new
name|CollapseBuilder
argument_list|(
literal|"someField"
argument_list|)
operator|.
name|setInnerHits
argument_list|(
operator|new
name|InnerHitBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"foobarbaz"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mockSearchPhaseContext
operator|.
name|getRequest
argument_list|()
operator|.
name|source
argument_list|()
operator|.
name|query
argument_list|(
name|originalQuery
argument_list|)
expr_stmt|;
name|mockSearchPhaseContext
operator|.
name|searchTransport
operator|=
operator|new
name|SearchTransportService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"search.remote.connect"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
name|void
name|sendExecuteMultiSearch
parameter_list|(
name|MultiSearchRequest
name|request
parameter_list|,
name|SearchTask
name|task
parameter_list|,
name|ActionListener
argument_list|<
name|MultiSearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|executedMultiSearch
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|request
operator|.
name|requests
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|SearchRequest
name|searchRequest
init|=
name|request
operator|.
name|requests
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|searchRequest
operator|.
name|source
argument_list|()
operator|.
name|query
argument_list|()
operator|instanceof
name|BoolQueryBuilder
argument_list|)
expr_stmt|;
name|BoolQueryBuilder
name|groupBuilder
init|=
operator|(
name|BoolQueryBuilder
operator|)
name|searchRequest
operator|.
name|source
argument_list|()
operator|.
name|query
argument_list|()
decl_stmt|;
if|if
condition|(
name|collapseValue
operator|==
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|groupBuilder
operator|.
name|mustNot
argument_list|()
argument_list|,
name|Matchers
operator|.
name|contains
argument_list|(
name|QueryBuilders
operator|.
name|existsQuery
argument_list|(
literal|"someField"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|groupBuilder
operator|.
name|filter
argument_list|()
argument_list|,
name|Matchers
operator|.
name|contains
argument_list|(
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"someField"
argument_list|,
literal|"boom"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|originalQuery
operator|!=
literal|null
condition|)
block|{
name|assertThat
argument_list|(
name|groupBuilder
operator|.
name|must
argument_list|()
argument_list|,
name|Matchers
operator|.
name|contains
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertArrayEquals
argument_list|(
name|mockSearchPhaseContext
operator|.
name|getRequest
argument_list|()
operator|.
name|indices
argument_list|()
argument_list|,
name|searchRequest
operator|.
name|indices
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|mockSearchPhaseContext
operator|.
name|getRequest
argument_list|()
operator|.
name|types
argument_list|()
argument_list|,
name|searchRequest
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|InternalSearchResponse
name|internalSearchResponse
init|=
operator|new
name|InternalSearchResponse
argument_list|(
name|collapsedHits
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|SearchResponse
name|response
init|=
name|mockSearchPhaseContext
operator|.
name|buildSearchResponse
argument_list|(
name|internalSearchResponse
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|MultiSearchResponse
argument_list|(
operator|new
name|MultiSearchResponse
operator|.
name|Item
index|[]
block|{
operator|new
name|MultiSearchResponse
operator|.
name|Item
argument_list|(
name|response
argument_list|,
literal|null
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
name|SearchHits
name|hits
init|=
operator|new
name|SearchHits
argument_list|(
operator|new
name|SearchHit
index|[]
block|{
operator|new
name|SearchHit
argument_list|(
literal|1
argument_list|,
literal|"ID"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"someField"
argument_list|,
operator|new
name|SearchHitField
argument_list|(
literal|"someField"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|collapseValue
argument_list|)
argument_list|)
argument_list|)
argument_list|)
block|}
argument_list|,
literal|1
argument_list|,
literal|1.0F
argument_list|)
decl_stmt|;
name|InternalSearchResponse
name|internalSearchResponse
init|=
operator|new
name|InternalSearchResponse
argument_list|(
name|hits
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|SearchResponse
name|response
init|=
name|mockSearchPhaseContext
operator|.
name|buildSearchResponse
argument_list|(
name|internalSearchResponse
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|SearchResponse
argument_list|>
name|reference
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|ExpandSearchPhase
name|phase
init|=
operator|new
name|ExpandSearchPhase
argument_list|(
name|mockSearchPhaseContext
argument_list|,
name|response
argument_list|,
name|r
lambda|->
operator|new
name|SearchPhase
argument_list|(
literal|"test"
argument_list|)
block|{
block|@Override                     public void run(
argument_list|)
throws|throws
name|IOException
block|{
name|reference
operator|.
name|set
argument_list|(
name|r
argument_list|)
decl_stmt|;
block|}
block|}
block|)
class|;
end_class

begin_expr_stmt
name|phase
operator|.
name|run
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|mockSearchPhaseContext
operator|.
name|assertNoFailure
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertNotNull
argument_list|(
name|reference
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_decl_stmt
name|SearchResponse
name|theResponse
init|=
name|reference
operator|.
name|get
argument_list|()
decl_stmt|;
end_decl_stmt

begin_expr_stmt
name|assertSame
argument_list|(
name|theResponse
argument_list|,
name|response
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|theResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getInnerHits
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertSame
argument_list|(
name|theResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
index|[
literal|0
index|]
operator|.
name|getInnerHits
argument_list|()
operator|.
name|get
argument_list|(
literal|"foobarbaz"
argument_list|)
argument_list|,
name|collapsedHits
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertTrue
argument_list|(
name|executedMultiSearch
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mockSearchPhaseContext
operator|.
name|phasesExecuted
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_function
unit|}     }
DECL|method|testFailOneItemFailsEntirePhase
specifier|public
name|void
name|testFailOneItemFailsEntirePhase
parameter_list|()
throws|throws
name|IOException
block|{
name|AtomicBoolean
name|executedMultiSearch
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|SearchHits
name|collapsedHits
init|=
operator|new
name|SearchHits
argument_list|(
operator|new
name|SearchHit
index|[]
block|{
operator|new
name|SearchHit
argument_list|(
literal|2
argument_list|,
literal|"ID"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
block|,
operator|new
name|SearchHit
argument_list|(
literal|3
argument_list|,
literal|"ID"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptyMap
argument_list|()
argument_list|)
block|}
argument_list|,
literal|1
argument_list|,
literal|1.0F
argument_list|)
decl_stmt|;
name|MockSearchPhaseContext
name|mockSearchPhaseContext
init|=
operator|new
name|MockSearchPhaseContext
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|String
name|collapseValue
init|=
name|randomBoolean
argument_list|()
condition|?
literal|null
else|:
literal|"boom"
decl_stmt|;
name|mockSearchPhaseContext
operator|.
name|getRequest
argument_list|()
operator|.
name|source
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|collapse
argument_list|(
operator|new
name|CollapseBuilder
argument_list|(
literal|"someField"
argument_list|)
operator|.
name|setInnerHits
argument_list|(
operator|new
name|InnerHitBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"foobarbaz"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|mockSearchPhaseContext
operator|.
name|searchTransport
operator|=
operator|new
name|SearchTransportService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"search.remote.connect"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
name|void
name|sendExecuteMultiSearch
parameter_list|(
name|MultiSearchRequest
name|request
parameter_list|,
name|SearchTask
name|task
parameter_list|,
name|ActionListener
argument_list|<
name|MultiSearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|executedMultiSearch
operator|.
name|compareAndSet
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|InternalSearchResponse
name|internalSearchResponse
init|=
operator|new
name|InternalSearchResponse
argument_list|(
name|collapsedHits
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|SearchResponse
name|response
init|=
name|mockSearchPhaseContext
operator|.
name|buildSearchResponse
argument_list|(
name|internalSearchResponse
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|listener
operator|.
name|onResponse
argument_list|(
operator|new
name|MultiSearchResponse
argument_list|(
operator|new
name|MultiSearchResponse
operator|.
name|Item
index|[]
block|{
operator|new
name|MultiSearchResponse
operator|.
name|Item
argument_list|(
literal|null
argument_list|,
operator|new
name|RuntimeException
argument_list|(
literal|"boom"
argument_list|)
argument_list|)
block|,
operator|new
name|MultiSearchResponse
operator|.
name|Item
argument_list|(
name|response
argument_list|,
literal|null
argument_list|)
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
name|SearchHits
name|hits
init|=
operator|new
name|SearchHits
argument_list|(
operator|new
name|SearchHit
index|[]
block|{
operator|new
name|SearchHit
argument_list|(
literal|1
argument_list|,
literal|"ID"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"someField"
argument_list|,
operator|new
name|SearchHitField
argument_list|(
literal|"someField"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|collapseValue
argument_list|)
argument_list|)
argument_list|)
argument_list|)
block|,
operator|new
name|SearchHit
argument_list|(
literal|2
argument_list|,
literal|"ID2"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"someField"
argument_list|,
operator|new
name|SearchHitField
argument_list|(
literal|"someField"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
name|collapseValue
argument_list|)
argument_list|)
argument_list|)
argument_list|)
block|}
argument_list|,
literal|1
argument_list|,
literal|1.0F
argument_list|)
decl_stmt|;
name|InternalSearchResponse
name|internalSearchResponse
init|=
operator|new
name|InternalSearchResponse
argument_list|(
name|hits
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|SearchResponse
name|response
init|=
name|mockSearchPhaseContext
operator|.
name|buildSearchResponse
argument_list|(
name|internalSearchResponse
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|SearchResponse
argument_list|>
name|reference
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|ExpandSearchPhase
name|phase
init|=
operator|new
name|ExpandSearchPhase
argument_list|(
name|mockSearchPhaseContext
argument_list|,
name|response
argument_list|,
name|r
lambda|->
operator|new
name|SearchPhase
argument_list|(
literal|"test"
argument_list|)
block|{
block|@Override                 public void run(
argument_list|)
throws|throws
name|IOException
block|{
name|reference
operator|.
name|set
argument_list|(
name|r
argument_list|)
decl_stmt|;
block|}
end_function

begin_empty_stmt
unit|}         )
empty_stmt|;
end_empty_stmt

begin_expr_stmt
name|phase
operator|.
name|run
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertThat
argument_list|(
name|mockSearchPhaseContext
operator|.
name|phaseFailure
operator|.
name|get
argument_list|()
argument_list|,
name|Matchers
operator|.
name|instanceOf
argument_list|(
name|RuntimeException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertEquals
argument_list|(
literal|"boom"
argument_list|,
name|mockSearchPhaseContext
operator|.
name|phaseFailure
operator|.
name|get
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertNotNull
argument_list|(
name|mockSearchPhaseContext
operator|.
name|phaseFailure
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertNull
argument_list|(
name|reference
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|mockSearchPhaseContext
operator|.
name|phasesExecuted
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_function
unit|}      public
DECL|method|testSkipPhase
name|void
name|testSkipPhase
parameter_list|()
throws|throws
name|IOException
block|{
name|MockSearchPhaseContext
name|mockSearchPhaseContext
init|=
operator|new
name|MockSearchPhaseContext
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|mockSearchPhaseContext
operator|.
name|searchTransport
operator|=
operator|new
name|SearchTransportService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"search.remote.connect"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
name|void
name|sendExecuteMultiSearch
parameter_list|(
name|MultiSearchRequest
name|request
parameter_list|,
name|SearchTask
name|task
parameter_list|,
name|ActionListener
argument_list|<
name|MultiSearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|fail
argument_list|(
literal|"no collapsing here"
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
name|SearchHits
name|hits
init|=
operator|new
name|SearchHits
argument_list|(
operator|new
name|SearchHit
index|[]
block|{
operator|new
name|SearchHit
argument_list|(
literal|1
argument_list|,
literal|"ID"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"someField"
argument_list|,
operator|new
name|SearchHitField
argument_list|(
literal|"someField"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|null
argument_list|)
argument_list|)
argument_list|)
argument_list|)
block|,
operator|new
name|SearchHit
argument_list|(
literal|2
argument_list|,
literal|"ID2"
argument_list|,
operator|new
name|Text
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
literal|"someField"
argument_list|,
operator|new
name|SearchHitField
argument_list|(
literal|"someField"
argument_list|,
name|Collections
operator|.
name|singletonList
argument_list|(
literal|null
argument_list|)
argument_list|)
argument_list|)
argument_list|)
block|}
argument_list|,
literal|1
argument_list|,
literal|1.0F
argument_list|)
decl_stmt|;
name|InternalSearchResponse
name|internalSearchResponse
init|=
operator|new
name|InternalSearchResponse
argument_list|(
name|hits
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|SearchResponse
name|response
init|=
name|mockSearchPhaseContext
operator|.
name|buildSearchResponse
argument_list|(
name|internalSearchResponse
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|SearchResponse
argument_list|>
name|reference
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|ExpandSearchPhase
name|phase
init|=
operator|new
name|ExpandSearchPhase
argument_list|(
name|mockSearchPhaseContext
argument_list|,
name|response
argument_list|,
name|r
lambda|->
operator|new
name|SearchPhase
argument_list|(
literal|"test"
argument_list|)
block|{
block|@Override                 public void run(
argument_list|)
throws|throws
name|IOException
block|{
name|reference
operator|.
name|set
argument_list|(
name|r
argument_list|)
decl_stmt|;
block|}
end_function

begin_empty_stmt
unit|}         )
empty_stmt|;
end_empty_stmt

begin_expr_stmt
name|phase
operator|.
name|run
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|mockSearchPhaseContext
operator|.
name|assertNoFailure
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertNotNull
argument_list|(
name|reference
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mockSearchPhaseContext
operator|.
name|phasesExecuted
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_function
unit|}      public
DECL|method|testSkipExpandCollapseNoHits
name|void
name|testSkipExpandCollapseNoHits
parameter_list|()
throws|throws
name|IOException
block|{
name|MockSearchPhaseContext
name|mockSearchPhaseContext
init|=
operator|new
name|MockSearchPhaseContext
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|mockSearchPhaseContext
operator|.
name|searchTransport
operator|=
operator|new
name|SearchTransportService
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"search.remote.connect"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
literal|null
argument_list|)
block|{
annotation|@
name|Override
name|void
name|sendExecuteMultiSearch
parameter_list|(
name|MultiSearchRequest
name|request
parameter_list|,
name|SearchTask
name|task
parameter_list|,
name|ActionListener
argument_list|<
name|MultiSearchResponse
argument_list|>
name|listener
parameter_list|)
block|{
name|fail
argument_list|(
literal|"expand should not try to send empty multi search request"
argument_list|)
expr_stmt|;
block|}
block|}
expr_stmt|;
name|mockSearchPhaseContext
operator|.
name|getRequest
argument_list|()
operator|.
name|source
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|collapse
argument_list|(
operator|new
name|CollapseBuilder
argument_list|(
literal|"someField"
argument_list|)
operator|.
name|setInnerHits
argument_list|(
operator|new
name|InnerHitBuilder
argument_list|()
operator|.
name|setName
argument_list|(
literal|"foobarbaz"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|SearchHits
name|hits
init|=
operator|new
name|SearchHits
argument_list|(
operator|new
name|SearchHit
index|[
literal|0
index|]
argument_list|,
literal|1
argument_list|,
literal|1.0f
argument_list|)
decl_stmt|;
name|InternalSearchResponse
name|internalSearchResponse
init|=
operator|new
name|InternalSearchResponse
argument_list|(
name|hits
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
literal|null
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|SearchResponse
name|response
init|=
name|mockSearchPhaseContext
operator|.
name|buildSearchResponse
argument_list|(
name|internalSearchResponse
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|AtomicReference
argument_list|<
name|SearchResponse
argument_list|>
name|reference
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
name|ExpandSearchPhase
name|phase
init|=
operator|new
name|ExpandSearchPhase
argument_list|(
name|mockSearchPhaseContext
argument_list|,
name|response
argument_list|,
name|r
lambda|->
operator|new
name|SearchPhase
argument_list|(
literal|"test"
argument_list|)
block|{
block|@Override                 public void run(
argument_list|)
throws|throws
name|IOException
block|{
name|reference
operator|.
name|set
argument_list|(
name|r
argument_list|)
decl_stmt|;
block|}
end_function

begin_empty_stmt
unit|}         )
empty_stmt|;
end_empty_stmt

begin_expr_stmt
name|phase
operator|.
name|run
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|mockSearchPhaseContext
operator|.
name|assertNoFailure
argument_list|()
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertNotNull
argument_list|(
name|reference
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

begin_expr_stmt
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mockSearchPhaseContext
operator|.
name|phasesExecuted
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
end_expr_stmt

unit|} }
end_unit

