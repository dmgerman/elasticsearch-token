begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.join.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|join
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
name|join
operator|.
name|ScoreMode
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|get
operator|.
name|GetMappingsResponse
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
name|admin
operator|.
name|indices
operator|.
name|mapping
operator|.
name|put
operator|.
name|PutMappingResponse
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
name|bulk
operator|.
name|BulkRequestBuilder
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
name|bulk
operator|.
name|BulkResponse
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
name|xcontent
operator|.
name|XContentType
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
name|builder
operator|.
name|SearchSourceBuilder
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
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|multiMatchQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|join
operator|.
name|query
operator|.
name|JoinQueryBuilders
operator|.
name|hasChildQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|join
operator|.
name|query
operator|.
name|JoinQueryBuilders
operator|.
name|hasParentQuery
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
name|test
operator|.
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertHitCount
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
name|assertNoFailures
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
name|assertSearchHits
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|greaterThanOrEqualTo
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
DECL|class|LegacyChildQuerySearchIT
specifier|public
class|class
name|LegacyChildQuerySearchIT
extends|extends
name|ChildQuerySearchIT
block|{
annotation|@
name|Override
DECL|method|legacy
specifier|protected
name|boolean
name|legacy
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
DECL|method|testIndexChildDocWithNoParentMapping
specifier|public
name|void
name|testIndexChildDocWithNoParentMapping
parameter_list|()
throws|throws
name|IOException
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"parent"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"child1"
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"parent"
argument_list|,
literal|"p1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"p_field"
argument_list|,
literal|"p_value1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"child1"
argument_list|,
literal|"c1"
argument_list|)
operator|.
name|setParent
argument_list|(
literal|"p1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"c_field"
argument_list|,
literal|"blue"
argument_list|)
operator|.
name|get
argument_list|()
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
literal|"can't specify parent if no parent field has been configured"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"child2"
argument_list|,
literal|"c2"
argument_list|)
operator|.
name|setParent
argument_list|(
literal|"p1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"c_field"
argument_list|,
literal|"blue"
argument_list|)
operator|.
name|get
argument_list|()
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
literal|"can't specify parent if no parent field has been configured"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|refresh
argument_list|()
expr_stmt|;
block|}
DECL|method|testAddingParentToExistingMapping
specifier|public
name|void
name|testAddingParentToExistingMapping
parameter_list|()
throws|throws
name|IOException
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|PutMappingResponse
name|putMappingResponse
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
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"child"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"number"
argument_list|,
literal|"type=integer"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|putMappingResponse
operator|.
name|isAcknowledged
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|GetMappingsResponse
name|getMappingsResponse
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
name|prepareGetMappings
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|mapping
init|=
name|getMappingsResponse
operator|.
name|getMappings
argument_list|()
operator|.
name|get
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|(
literal|"child"
argument_list|)
operator|.
name|getSourceAsMap
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|mapping
operator|.
name|size
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
comment|// there are potentially some meta fields configured randomly
name|assertThat
argument_list|(
name|mapping
operator|.
name|get
argument_list|(
literal|"properties"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
try|try
block|{
comment|// Adding _parent metadata field to existing mapping is prohibited:
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|preparePutMapping
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setType
argument_list|(
literal|"child"
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
name|startObject
argument_list|(
literal|"child"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_parent"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"parent"
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
argument_list|)
operator|.
name|get
argument_list|()
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
literal|"The _parent field's type option can't be changed: [null]->[parent]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|// Issue #5783
DECL|method|testQueryBeforeChildType
specifier|public
name|void
name|testQueryBeforeChildType
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"features"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"posts"
argument_list|,
literal|"_parent"
argument_list|,
literal|"type=features"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"specials"
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"features"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"foo"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"posts"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setParent
argument_list|(
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"bar"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|resp
decl_stmt|;
name|resp
operator|=
name|client
argument_list|()
operator|.
name|prepareSearch
argument_list|(
literal|"test"
argument_list|)
operator|.
name|setSource
argument_list|(
operator|new
name|SearchSourceBuilder
argument_list|()
operator|.
name|query
argument_list|(
name|hasChildQuery
argument_list|(
literal|"posts"
argument_list|,
name|QueryBuilders
operator|.
name|matchQuery
argument_list|(
literal|"field"
argument_list|,
literal|"bar"
argument_list|)
argument_list|,
name|ScoreMode
operator|.
name|None
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertHitCount
argument_list|(
name|resp
argument_list|,
literal|1L
argument_list|)
expr_stmt|;
block|}
comment|// Issue #6256
DECL|method|testParentFieldInMultiMatchField
specifier|public
name|void
name|testParentFieldInMultiMatchField
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type2"
argument_list|,
literal|"_parent"
argument_list|,
literal|"type=type1"
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type2"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setParent
argument_list|(
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
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|SearchResponse
name|response
init|=
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
name|multiMatchQuery
argument_list|(
literal|"1"
argument_list|,
literal|"_parent#type1"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getHits
argument_list|()
operator|.
name|getTotalHits
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1L
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
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testParentFieldToNonExistingType
specifier|public
name|void
name|testParentFieldToNonExistingType
parameter_list|()
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"parent"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"child"
argument_list|,
literal|"_parent"
argument_list|,
literal|"type=parent2"
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"parent"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"child"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setParent
argument_list|(
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
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
name|setQuery
argument_list|(
name|hasChildQuery
argument_list|(
literal|"child"
argument_list|,
name|matchAllQuery
argument_list|()
argument_list|,
name|ScoreMode
operator|.
name|None
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|SearchPhaseExecutionException
name|e
parameter_list|)
block|{         }
block|}
comment|/*    Test for https://github.com/elastic/elasticsearch/issues/3444     */
DECL|method|testBulkUpdateDocAsUpsertWithParent
specifier|public
name|void
name|testBulkUpdateDocAsUpsertWithParent
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"parent"
argument_list|,
literal|"{\"parent\":{}}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"child"
argument_list|,
literal|"{\"child\": {\"_parent\": {\"type\": \"parent\"}}}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|BulkRequestBuilder
name|builder
init|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
comment|// It's important to use JSON parsing here and request objects: issue 3444 is related to incomplete option parsing
name|byte
index|[]
name|addParent
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{"
operator|+
literal|"  \"index\" : {"
operator|+
literal|"    \"_index\" : \"test\","
operator|+
literal|"    \"_type\"  : \"parent\","
operator|+
literal|"    \"_id\"    : \"parent1\""
operator|+
literal|"  }"
operator|+
literal|"}"
operator|+
literal|"\n"
operator|+
literal|"{"
operator|+
literal|"  \"field1\" : \"value1\""
operator|+
literal|"}"
operator|+
literal|"\n"
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|byte
index|[]
name|addChild
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{"
operator|+
literal|"  \"update\" : {"
operator|+
literal|"    \"_index\" : \"test\","
operator|+
literal|"    \"_type\"  : \"child\","
operator|+
literal|"    \"_id\"    : \"child1\","
operator|+
literal|"    \"parent\" : \"parent1\""
operator|+
literal|"  }"
operator|+
literal|"}"
operator|+
literal|"\n"
operator|+
literal|"{"
operator|+
literal|"  \"doc\" : {"
operator|+
literal|"    \"field1\" : \"value1\""
operator|+
literal|"  },"
operator|+
literal|"  \"doc_as_upsert\" : \"true\""
operator|+
literal|"}"
operator|+
literal|"\n"
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|addParent
argument_list|,
literal|0
argument_list|,
name|addParent
operator|.
name|length
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|addChild
argument_list|,
literal|0
argument_list|,
name|addChild
operator|.
name|length
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
name|BulkResponse
name|bulkResponse
init|=
name|builder
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
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
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
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
argument_list|(
literal|"test"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
comment|//we check that the _parent field was set on the child document by using the has parent query
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
name|setQuery
argument_list|(
name|hasParentQuery
argument_list|(
literal|"parent"
argument_list|,
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNoFailures
argument_list|(
name|searchResponse
argument_list|)
expr_stmt|;
name|assertSearchHits
argument_list|(
name|searchResponse
argument_list|,
literal|"child1"
argument_list|)
expr_stmt|;
block|}
comment|/*     Test for https://github.com/elastic/elasticsearch/issues/3444      */
DECL|method|testBulkUpdateUpsertWithParent
specifier|public
name|void
name|testBulkUpdateUpsertWithParent
parameter_list|()
throws|throws
name|Exception
block|{
name|assertAcked
argument_list|(
name|prepareCreate
argument_list|(
literal|"test"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"parent"
argument_list|,
literal|"{\"parent\":{}}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"child"
argument_list|,
literal|"{\"child\": {\"_parent\": {\"type\": \"parent\"}}}"
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|BulkRequestBuilder
name|builder
init|=
name|client
argument_list|()
operator|.
name|prepareBulk
argument_list|()
decl_stmt|;
name|byte
index|[]
name|addParent
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{"
operator|+
literal|"  \"index\" : {"
operator|+
literal|"    \"_index\" : \"test\","
operator|+
literal|"    \"_type\"  : \"parent\","
operator|+
literal|"    \"_id\"    : \"parent1\""
operator|+
literal|"  }"
operator|+
literal|"}"
operator|+
literal|"\n"
operator|+
literal|"{"
operator|+
literal|"  \"field1\" : \"value1\""
operator|+
literal|"}"
operator|+
literal|"\n"
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|byte
index|[]
name|addChild1
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{"
operator|+
literal|"  \"update\" : {"
operator|+
literal|"    \"_index\" : \"test\","
operator|+
literal|"    \"_type\"  : \"child\","
operator|+
literal|"    \"_id\"    : \"child1\","
operator|+
literal|"    \"parent\" : \"parent1\""
operator|+
literal|"  }"
operator|+
literal|"}"
operator|+
literal|"\n"
operator|+
literal|"{"
operator|+
literal|"  \"script\" : {"
operator|+
literal|"    \"inline\" : \"ctx._source.field2 = 'value2'\""
operator|+
literal|"  },"
operator|+
literal|"  \"lang\" : \""
operator|+
name|InnerHitsIT
operator|.
name|CustomScriptPlugin
operator|.
name|NAME
operator|+
literal|"\","
operator|+
literal|"  \"upsert\" : {"
operator|+
literal|"    \"field1\" : \"value1'\""
operator|+
literal|"  }"
operator|+
literal|"}"
operator|+
literal|"\n"
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|byte
index|[]
name|addChild2
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{"
operator|+
literal|"  \"update\" : {"
operator|+
literal|"    \"_index\" : \"test\","
operator|+
literal|"    \"_type\"  : \"child\","
operator|+
literal|"    \"_id\"    : \"child1\","
operator|+
literal|"    \"parent\" : \"parent1\""
operator|+
literal|"  }"
operator|+
literal|"}"
operator|+
literal|"\n"
operator|+
literal|"{"
operator|+
literal|"  \"script\" : \"ctx._source.field2 = 'value2'\","
operator|+
literal|"  \"upsert\" : {"
operator|+
literal|"    \"field1\" : \"value1'\""
operator|+
literal|"  }"
operator|+
literal|"}"
operator|+
literal|"\n"
argument_list|)
operator|.
name|array
argument_list|()
decl_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|addParent
argument_list|,
literal|0
argument_list|,
name|addParent
operator|.
name|length
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|addChild1
argument_list|,
literal|0
argument_list|,
name|addChild1
operator|.
name|length
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
name|builder
operator|.
name|add
argument_list|(
name|addChild2
argument_list|,
literal|0
argument_list|,
name|addChild2
operator|.
name|length
argument_list|,
name|XContentType
operator|.
name|JSON
argument_list|)
expr_stmt|;
name|BulkResponse
name|bulkResponse
init|=
name|builder
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
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
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|0
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|1
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
operator|.
name|isFailed
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bulkResponse
operator|.
name|getItems
argument_list|()
index|[
literal|2
index|]
operator|.
name|getFailure
argument_list|()
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
name|equalTo
argument_list|(
literal|"script_lang not supported [painless]"
argument_list|)
argument_list|)
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
argument_list|(
literal|"test"
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
literal|"test"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|hasParentQuery
argument_list|(
literal|"parent"
argument_list|,
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|,
literal|false
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertSearchHits
argument_list|(
name|searchResponse
argument_list|,
literal|"child1"
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
