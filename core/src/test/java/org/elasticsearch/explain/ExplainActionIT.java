begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.explain
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|explain
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
name|admin
operator|.
name|indices
operator|.
name|alias
operator|.
name|Alias
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
name|explain
operator|.
name|ExplainResponse
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
name|InputStreamStreamInput
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
name|OutputStreamStreamOutput
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
name|lucene
operator|.
name|Lucene
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
name|mapper
operator|.
name|TimestampFieldMapper
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
name|test
operator|.
name|ESIntegTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTime
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|DateTimeZone
import|;
end_import

begin_import
import|import
name|org
operator|.
name|joda
operator|.
name|time
operator|.
name|format
operator|.
name|ISODateTimeFormat
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|Set
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singleton
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
name|queryStringQuery
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
name|notNullValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ExplainActionIT
specifier|public
class|class
name|ExplainActionIT
extends|extends
name|ESIntegTestCase
block|{
DECL|method|testSimple
specifier|public
name|void
name|testSimple
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
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
operator|.
name|setSettings
argument_list|(
name|Settings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.refresh_interval"
argument_list|,
operator|-
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
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
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|ExplainResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
comment|// not a match b/c not realtime
name|assertThat
argument_list|(
name|response
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|assertFalse
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
comment|// not a match b/c not realtime
name|refresh
argument_list|()
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0f
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|assertNotNull
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|boolQuery
argument_list|()
operator|.
name|must
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value1"
argument_list|)
argument_list|)
operator|.
name|must
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|assertNotNull
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|getDetails
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
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
DECL|method|testExplainWithFields
specifier|public
name|void
name|testExplainWithFields
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
literal|"test"
argument_list|,
literal|"obj1.field1"
argument_list|,
literal|"type=keyword,store=true"
argument_list|,
literal|"obj1.field2"
argument_list|,
literal|"type=keyword,store=true"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
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
literal|"test"
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
name|startObject
argument_list|(
literal|"obj1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
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
name|refresh
argument_list|()
expr_stmt|;
name|ExplainResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"obj1.field1"
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|isExists
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
name|response
operator|.
name|getGetResult
argument_list|()
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
name|Set
argument_list|<
name|String
argument_list|>
name|fields
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
decl_stmt|;
name|fields
operator|.
name|remove
argument_list|(
name|TimestampFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
comment|// randomly added via templates
name|assertThat
argument_list|(
name|fields
argument_list|,
name|equalTo
argument_list|(
name|singleton
argument_list|(
literal|"obj1.field1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"obj1.field1"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|isSourceEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"obj1.field1"
argument_list|)
operator|.
name|setFetchSource
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|isExists
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
name|response
operator|.
name|getGetResult
argument_list|()
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
name|fields
operator|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|keySet
argument_list|()
argument_list|)
expr_stmt|;
name|fields
operator|.
name|remove
argument_list|(
name|TimestampFieldMapper
operator|.
name|NAME
argument_list|)
expr_stmt|;
comment|// randomly added via templates
name|assertThat
argument_list|(
name|fields
argument_list|,
name|equalTo
argument_list|(
name|singleton
argument_list|(
literal|"obj1.field1"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getFields
argument_list|()
operator|.
name|get
argument_list|(
literal|"obj1.field1"
argument_list|)
operator|.
name|getValue
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|isSourceEmpty
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setStoredFields
argument_list|(
literal|"obj1.field1"
argument_list|,
literal|"obj1.field2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|v1
init|=
operator|(
name|String
operator|)
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|field
argument_list|(
literal|"obj1.field1"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|String
name|v2
init|=
operator|(
name|String
operator|)
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|field
argument_list|(
literal|"obj1.field2"
argument_list|)
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|v1
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|v2
argument_list|,
name|equalTo
argument_list|(
literal|"value2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testExplainWitSource
specifier|public
name|void
name|testExplainWitSource
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
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"alias"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
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
literal|"test"
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
name|startObject
argument_list|(
literal|"obj1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
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
name|refresh
argument_list|()
expr_stmt|;
name|ExplainResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setFetchSource
argument_list|(
literal|"obj1.field1"
argument_list|,
literal|null
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getExplanation
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.0f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|isExists
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
name|response
operator|.
name|getGetResult
argument_list|()
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
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getSource
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
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"obj1"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
name|response
operator|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
name|indexOrAlias
argument_list|()
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setFetchSource
argument_list|(
literal|null
argument_list|,
literal|"obj1.field2"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"obj1"
argument_list|)
operator|)
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExplainWithFilteredAlias
specifier|public
name|void
name|testExplainWithFilteredAlias
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
literal|"test"
argument_list|,
literal|"field2"
argument_list|,
literal|"type=text"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"alias1"
argument_list|)
operator|.
name|filter
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
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
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|,
literal|"field2"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|ExplainResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
literal|"alias1"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testExplainWithFilteredAliasFetchSource
specifier|public
name|void
name|testExplainWithFilteredAliasFetchSource
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
literal|"test"
argument_list|,
literal|"field2"
argument_list|,
literal|"type=text"
argument_list|)
operator|.
name|addAlias
argument_list|(
operator|new
name|Alias
argument_list|(
literal|"alias1"
argument_list|)
operator|.
name|filter
argument_list|(
name|QueryBuilders
operator|.
name|termQuery
argument_list|(
literal|"field2"
argument_list|,
literal|"value2"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
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
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"field1"
argument_list|,
literal|"value1"
argument_list|,
literal|"field2"
argument_list|,
literal|"value1"
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|ExplainResponse
name|response
init|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
literal|"alias1"
argument_list|,
literal|"test"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|QueryBuilders
operator|.
name|matchAllQuery
argument_list|()
argument_list|)
operator|.
name|setFetchSource
argument_list|(
literal|true
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|response
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|response
operator|.
name|isExists
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|response
operator|.
name|isMatch
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
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
name|response
operator|.
name|getGetResult
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getIndex
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getType
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"test"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getGetResult
argument_list|()
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
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getSource
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
name|String
operator|)
name|response
operator|.
name|getGetResult
argument_list|()
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"field1"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"value1"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExplainDateRangeInQueryString
specifier|public
name|void
name|testExplainDateRangeInQueryString
parameter_list|()
block|{
name|createIndex
argument_list|(
literal|"test"
argument_list|)
expr_stmt|;
name|String
name|aMonthAgo
init|=
name|ISODateTimeFormat
operator|.
name|yearMonthDay
argument_list|()
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|minusMonths
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|aMonthFromNow
init|=
name|ISODateTimeFormat
operator|.
name|yearMonthDay
argument_list|()
operator|.
name|print
argument_list|(
operator|new
name|DateTime
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|plusMonths
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"past"
argument_list|,
name|aMonthAgo
argument_list|,
literal|"future"
argument_list|,
name|aMonthFromNow
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
name|refresh
argument_list|()
expr_stmt|;
name|ExplainResponse
name|explainResponse
init|=
name|client
argument_list|()
operator|.
name|prepareExplain
argument_list|(
literal|"test"
argument_list|,
literal|"type"
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setQuery
argument_list|(
name|queryStringQuery
argument_list|(
literal|"past:[now-2M/d TO now/d]"
argument_list|)
argument_list|)
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|explainResponse
operator|.
name|isExists
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
name|explainResponse
operator|.
name|isMatch
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|indexOrAlias
specifier|private
specifier|static
name|String
name|indexOrAlias
parameter_list|()
block|{
return|return
name|randomBoolean
argument_list|()
condition|?
literal|"test"
else|:
literal|"alias"
return|;
block|}
DECL|method|testStreamExplain
specifier|public
name|void
name|testStreamExplain
parameter_list|()
throws|throws
name|Exception
block|{
name|Explanation
name|exp
init|=
name|Explanation
operator|.
name|match
argument_list|(
literal|2f
argument_list|,
literal|"some explanation"
argument_list|)
decl_stmt|;
comment|// write
name|ByteArrayOutputStream
name|outBuffer
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|OutputStreamStreamOutput
name|out
init|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|outBuffer
argument_list|)
decl_stmt|;
name|Lucene
operator|.
name|writeExplanation
argument_list|(
name|out
argument_list|,
name|exp
argument_list|)
expr_stmt|;
comment|// read
name|ByteArrayInputStream
name|esInBuffer
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|outBuffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|InputStreamStreamInput
name|esBuffer
init|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|esInBuffer
argument_list|)
decl_stmt|;
name|Explanation
name|result
init|=
name|Lucene
operator|.
name|readExplanation
argument_list|(
name|esBuffer
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|exp
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|result
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|exp
operator|=
name|Explanation
operator|.
name|match
argument_list|(
literal|2.0f
argument_list|,
literal|"some explanation"
argument_list|,
name|Explanation
operator|.
name|match
argument_list|(
literal|2.0f
argument_list|,
literal|"another explanation"
argument_list|)
argument_list|)
expr_stmt|;
comment|// write complex
name|outBuffer
operator|=
operator|new
name|ByteArrayOutputStream
argument_list|()
expr_stmt|;
name|out
operator|=
operator|new
name|OutputStreamStreamOutput
argument_list|(
name|outBuffer
argument_list|)
expr_stmt|;
name|Lucene
operator|.
name|writeExplanation
argument_list|(
name|out
argument_list|,
name|exp
argument_list|)
expr_stmt|;
comment|// read complex
name|esInBuffer
operator|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|outBuffer
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
name|esBuffer
operator|=
operator|new
name|InputStreamStreamInput
argument_list|(
name|esInBuffer
argument_list|)
expr_stmt|;
name|result
operator|=
name|Lucene
operator|.
name|readExplanation
argument_list|(
name|esBuffer
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exp
operator|.
name|toString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|result
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

