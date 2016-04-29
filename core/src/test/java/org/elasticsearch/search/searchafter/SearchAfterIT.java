begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.searchafter
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|searchafter
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
name|admin
operator|.
name|indices
operator|.
name|create
operator|.
name|CreateIndexRequestBuilder
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
name|index
operator|.
name|IndexRequestBuilder
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
name|SearchPhaseExecutionException
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
name|SearchRequestBuilder
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
name|common
operator|.
name|UUIDs
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
name|search
operator|.
name|SearchContextException
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
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|RemoteTransportException
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
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|Arrays
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
name|matchAllQuery
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

begin_class
DECL|class|SearchAfterIT
specifier|public
class|class
name|SearchAfterIT
extends|extends
name|ESIntegTestCase
block|{
DECL|field|INDEX_NAME
specifier|private
specifier|static
specifier|final
name|String
name|INDEX_NAME
init|=
literal|"test"
decl_stmt|;
DECL|field|TYPE_NAME
specifier|private
specifier|static
specifier|final
name|String
name|TYPE_NAME
init|=
literal|"type1"
decl_stmt|;
DECL|field|NUM_DOCS
specifier|private
specifier|static
specifier|final
name|int
name|NUM_DOCS
init|=
literal|100
decl_stmt|;
DECL|method|testsShouldFail
specifier|public
name|void
name|testsShouldFail
parameter_list|()
throws|throws
name|Exception
block|{
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"field2"
argument_list|,
literal|"type=keyword"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"0"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|0
argument_list|,
literal|"field2"
argument_list|,
literal|"toto"
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|searchAfter
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|0
block|}
argument_list|)
operator|.
name|setScroll
argument_list|(
literal|"1m"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on search_after cannot be used with scroll."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|RemoteTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|SearchContextException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"`search_after` cannot be used in a scroll context."
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|searchAfter
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|0
block|}
argument_list|)
operator|.
name|setFrom
argument_list|(
literal|10
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on search_after cannot be used with from> 0."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|RemoteTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|SearchContextException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"`from` parameter must be set to 0 when `search_after` is used."
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|searchAfter
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|0.75f
block|}
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on search_after on score only is disabled"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|RemoteTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"Sort must contain at least one field."
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field2"
argument_list|,
name|SortOrder
operator|.
name|DESC
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|searchAfter
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|1
block|}
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on search_after size differs from sort field size"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|RemoteTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"search_after has 1 value(s) but sort has 2."
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|searchAfter
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|1
block|,
literal|2
block|}
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on search_after size differs from sort field size"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|RemoteTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"search_after has 2 value(s) but sort has 1."
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|searchAfter
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"toto"
block|}
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"Should fail on search_after on score only is disabled"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|RemoteTransportException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getClass
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getCause
argument_list|()
operator|.
name|getCause
argument_list|()
operator|.
name|getMessage
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"Failed to parse search_after value for field [field1]."
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testWithNullStrings
specifier|public
name|void
name|testWithNullStrings
parameter_list|()
throws|throws
name|ExecutionException
throws|,
name|InterruptedException
block|{
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
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"field2"
argument_list|,
literal|"type=keyword"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"0"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|0
argument_list|)
argument_list|,
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
literal|"field1"
argument_list|,
literal|100
argument_list|,
literal|"field2"
argument_list|,
literal|"toto"
argument_list|)
argument_list|)
expr_stmt|;
name|SearchResponse
name|searchResponse
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field1"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|addSort
argument_list|(
literal|"field2"
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|searchAfter
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|0
block|,
literal|null
block|}
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|2L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|getHits
argument_list|()
operator|.
name|length
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
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
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|searchResponse
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
name|sourceAsMap
argument_list|()
operator|.
name|get
argument_list|(
literal|"field2"
argument_list|)
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
literal|"toto"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testWithSimpleTypes
specifier|public
name|void
name|testWithSimpleTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|numFields
init|=
name|randomInt
argument_list|(
literal|20
argument_list|)
operator|+
literal|1
decl_stmt|;
name|int
index|[]
name|types
init|=
operator|new
name|int
index|[
name|numFields
operator|-
literal|1
index|]
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
name|numFields
operator|-
literal|1
condition|;
name|i
operator|++
control|)
block|{
name|types
index|[
name|i
index|]
operator|=
name|randomInt
argument_list|(
literal|6
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|List
argument_list|>
name|documents
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|NUM_DOCS
condition|;
name|i
operator|++
control|)
block|{
name|List
name|values
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|type
range|:
name|types
control|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
literal|0
case|:
name|values
operator|.
name|add
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
name|values
operator|.
name|add
argument_list|(
name|randomByte
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|2
case|:
name|values
operator|.
name|add
argument_list|(
name|randomShort
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
name|values
operator|.
name|add
argument_list|(
name|randomInt
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|4
case|:
name|values
operator|.
name|add
argument_list|(
name|randomFloat
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|5
case|:
name|values
operator|.
name|add
argument_list|(
name|randomDouble
argument_list|()
argument_list|)
expr_stmt|;
break|break;
case|case
literal|6
case|:
name|values
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|5
argument_list|,
literal|20
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
block|}
name|values
operator|.
name|add
argument_list|(
operator|new
name|Text
argument_list|(
name|UUIDs
operator|.
name|randomBase64UUID
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|documents
operator|.
name|add
argument_list|(
name|values
argument_list|)
expr_stmt|;
block|}
name|int
name|reqSize
init|=
name|randomInt
argument_list|(
name|NUM_DOCS
operator|-
literal|1
argument_list|)
decl_stmt|;
if|if
condition|(
name|reqSize
operator|==
literal|0
condition|)
block|{
name|reqSize
operator|=
literal|1
expr_stmt|;
block|}
name|assertSearchFromWithSortValues
argument_list|(
name|INDEX_NAME
argument_list|,
name|TYPE_NAME
argument_list|,
name|documents
argument_list|,
name|reqSize
argument_list|)
expr_stmt|;
block|}
DECL|class|ListComparator
specifier|private
specifier|static
class|class
name|ListComparator
implements|implements
name|Comparator
argument_list|<
name|List
argument_list|>
block|{
annotation|@
name|Override
DECL|method|compare
specifier|public
name|int
name|compare
parameter_list|(
name|List
name|o1
parameter_list|,
name|List
name|o2
parameter_list|)
block|{
if|if
condition|(
name|o1
operator|.
name|size
argument_list|()
operator|>
name|o2
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
literal|1
return|;
block|}
if|if
condition|(
name|o2
operator|.
name|size
argument_list|()
operator|>
name|o1
operator|.
name|size
argument_list|()
condition|)
block|{
return|return
operator|-
literal|1
return|;
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
name|o1
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
operator|(
name|o1
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|Comparable
operator|)
condition|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|o1
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getClass
argument_list|()
operator|+
literal|" is not comparable"
argument_list|)
throw|;
block|}
name|Object
name|cmp1
init|=
name|o1
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Object
name|cmp2
init|=
name|o2
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|int
name|cmp
init|=
operator|(
operator|(
name|Comparable
operator|)
name|cmp1
operator|)
operator|.
name|compareTo
argument_list|(
name|cmp2
argument_list|)
decl_stmt|;
if|if
condition|(
name|cmp
operator|!=
literal|0
condition|)
block|{
return|return
name|cmp
return|;
block|}
block|}
return|return
literal|0
return|;
block|}
block|}
DECL|field|LST_COMPARATOR
specifier|private
name|ListComparator
name|LST_COMPARATOR
init|=
operator|new
name|ListComparator
argument_list|()
decl_stmt|;
DECL|method|assertSearchFromWithSortValues
specifier|private
name|void
name|assertSearchFromWithSortValues
parameter_list|(
name|String
name|indexName
parameter_list|,
name|String
name|typeName
parameter_list|,
name|List
argument_list|<
name|List
argument_list|>
name|documents
parameter_list|,
name|int
name|reqSize
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|numFields
init|=
name|documents
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
block|{
name|createIndexMappingsFromObjectType
argument_list|(
name|indexName
argument_list|,
name|typeName
argument_list|,
name|documents
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|IndexRequestBuilder
argument_list|>
name|requests
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|documents
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|documents
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|size
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|numFields
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|numFields
condition|;
name|j
operator|++
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|j
argument_list|)
argument_list|,
name|documents
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|get
argument_list|(
name|j
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|requests
operator|.
name|add
argument_list|(
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
name|INDEX_NAME
argument_list|,
name|TYPE_NAME
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
name|builder
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|indexRandom
argument_list|(
literal|true
argument_list|,
name|requests
argument_list|)
expr_stmt|;
block|}
name|Collections
operator|.
name|sort
argument_list|(
name|documents
argument_list|,
name|LST_COMPARATOR
argument_list|)
expr_stmt|;
name|int
name|offset
init|=
literal|0
decl_stmt|;
name|Object
index|[]
name|sortValues
init|=
literal|null
decl_stmt|;
while|while
condition|(
name|offset
operator|<
name|documents
operator|.
name|size
argument_list|()
condition|)
block|{
name|SearchRequestBuilder
name|req
init|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
name|indexName
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
name|documents
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|req
operator|.
name|addSort
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
name|SortOrder
operator|.
name|ASC
argument_list|)
expr_stmt|;
block|}
name|req
operator|.
name|setQuery
argument_list|(
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setSize
argument_list|(
name|reqSize
argument_list|)
expr_stmt|;
if|if
condition|(
name|sortValues
operator|!=
literal|null
condition|)
block|{
name|req
operator|.
name|searchAfter
argument_list|(
name|sortValues
argument_list|)
expr_stmt|;
block|}
name|SearchResponse
name|searchResponse
init|=
name|req
operator|.
name|get
argument_list|()
decl_stmt|;
for|for
control|(
name|SearchHit
name|hit
range|:
name|searchResponse
operator|.
name|getHits
argument_list|()
control|)
block|{
name|List
name|toCompare
init|=
name|convertSortValues
argument_list|(
name|documents
operator|.
name|get
argument_list|(
name|offset
operator|++
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|LST_COMPARATOR
operator|.
name|compare
argument_list|(
name|toCompare
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|hit
operator|.
name|sortValues
argument_list|()
argument_list|)
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|sortValues
operator|=
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
index|[
name|searchResponse
operator|.
name|getHits
argument_list|()
operator|.
name|hits
argument_list|()
operator|.
name|length
operator|-
literal|1
index|]
operator|.
name|getSortValues
argument_list|()
expr_stmt|;
block|}
block|}
DECL|method|createIndexMappingsFromObjectType
specifier|private
name|void
name|createIndexMappingsFromObjectType
parameter_list|(
name|String
name|indexName
parameter_list|,
name|String
name|typeName
parameter_list|,
name|List
argument_list|<
name|Object
argument_list|>
name|types
parameter_list|)
block|{
name|CreateIndexRequestBuilder
name|indexRequestBuilder
init|=
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
name|indexName
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|mappings
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|int
name|numFields
init|=
name|types
operator|.
name|size
argument_list|()
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
name|numFields
condition|;
name|i
operator|++
control|)
block|{
name|Class
name|type
init|=
name|types
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getClass
argument_list|()
decl_stmt|;
if|if
condition|(
name|type
operator|==
name|Integer
operator|.
name|class
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=integer"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Long
operator|.
name|class
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=long"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Float
operator|.
name|class
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=float"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Double
operator|.
name|class
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=double"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Byte
operator|.
name|class
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=byte"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Short
operator|.
name|class
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=short"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|type
operator|==
name|Boolean
operator|.
name|class
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=boolean"
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|types
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|instanceof
name|Text
condition|)
block|{
name|mappings
operator|.
name|add
argument_list|(
literal|"field"
operator|+
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|mappings
operator|.
name|add
argument_list|(
literal|"type=keyword"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
literal|"Can't match type ["
operator|+
name|type
operator|+
literal|"]"
argument_list|)
expr_stmt|;
block|}
block|}
name|indexRequestBuilder
operator|.
name|addMapping
argument_list|(
name|typeName
argument_list|,
name|mappings
operator|.
name|toArray
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
block|}
comment|// Convert Integer, Short, Byte and Boolean to Long in order to match the conversion done
comment|// by the internal hits when populating the sort values.
DECL|method|convertSortValues
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|convertSortValues
parameter_list|(
name|List
argument_list|<
name|Object
argument_list|>
name|sortValues
parameter_list|)
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|converted
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
name|sortValues
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|Object
name|from
init|=
name|sortValues
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
if|if
condition|(
name|from
operator|instanceof
name|Integer
condition|)
block|{
name|converted
operator|.
name|add
argument_list|(
operator|(
operator|(
name|Integer
operator|)
name|from
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|from
operator|instanceof
name|Short
condition|)
block|{
name|converted
operator|.
name|add
argument_list|(
operator|(
operator|(
name|Short
operator|)
name|from
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|from
operator|instanceof
name|Byte
condition|)
block|{
name|converted
operator|.
name|add
argument_list|(
operator|(
operator|(
name|Byte
operator|)
name|from
operator|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|from
operator|instanceof
name|Boolean
condition|)
block|{
name|boolean
name|b
init|=
operator|(
name|boolean
operator|)
name|from
decl_stmt|;
if|if
condition|(
name|b
condition|)
block|{
name|converted
operator|.
name|add
argument_list|(
literal|1L
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|converted
operator|.
name|add
argument_list|(
literal|0L
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|converted
operator|.
name|add
argument_list|(
name|from
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|converted
return|;
block|}
block|}
end_class

end_unit

