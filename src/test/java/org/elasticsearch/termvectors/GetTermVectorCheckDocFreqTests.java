begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.termvectors
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|termvectors
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
name|index
operator|.
name|DocsAndPositionsEnum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Fields
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|Terms
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|TermsEnum
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|BytesRef
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchException
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
name|termvector
operator|.
name|TermVectorRequestBuilder
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
name|termvector
operator|.
name|TermVectorResponse
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
name|BytesStream
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
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
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
name|AbstractIntegrationTest
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
name|ElasticSearchAssertions
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
name|io
operator|.
name|IOException
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
DECL|class|GetTermVectorCheckDocFreqTests
specifier|public
class|class
name|GetTermVectorCheckDocFreqTests
extends|extends
name|AbstractIntegrationTest
block|{
annotation|@
name|Test
DECL|method|testSimpleTermVectors
specifier|public
name|void
name|testSimpleTermVectors
parameter_list|()
throws|throws
name|ElasticSearchException
throws|,
name|IOException
block|{
name|XContentBuilder
name|mapping
init|=
name|XContentFactory
operator|.
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
literal|"field"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"term_vector"
argument_list|,
literal|"with_positions_offsets_payloads"
argument_list|)
operator|.
name|field
argument_list|(
literal|"analyzer"
argument_list|,
literal|"tv_test"
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
decl_stmt|;
name|ElasticSearchAssertions
operator|.
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
argument_list|,
name|mapping
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
literal|1
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.analysis.analyzer.tv_test.tokenizer"
argument_list|,
literal|"whitespace"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.number_of_replicas"
argument_list|,
literal|0
argument_list|)
operator|.
name|putArray
argument_list|(
literal|"index.analysis.analyzer.tv_test.filter"
argument_list|,
literal|"type_as_payload"
argument_list|,
literal|"lowercase"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ensureGreen
argument_list|()
expr_stmt|;
name|int
name|numDocs
init|=
literal|15
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|client
argument_list|()
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
name|XContentFactory
operator|.
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
literal|"the quick brown fox jumps over the lazy dog"
argument_list|)
comment|// 0the3 4quick9 10brown15 16fox19 20jumps25 26over30
comment|// 31the34 35lazy39 40dog43
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
block|}
name|String
index|[]
name|values
init|=
block|{
literal|"brown"
block|,
literal|"dog"
block|,
literal|"fox"
block|,
literal|"jumps"
block|,
literal|"lazy"
block|,
literal|"over"
block|,
literal|"quick"
block|,
literal|"the"
block|}
decl_stmt|;
name|int
index|[]
name|freq
init|=
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|2
block|}
decl_stmt|;
name|int
index|[]
index|[]
name|pos
init|=
block|{
block|{
literal|2
block|}
block|,
block|{
literal|8
block|}
block|,
block|{
literal|3
block|}
block|,
block|{
literal|4
block|}
block|,
block|{
literal|7
block|}
block|,
block|{
literal|5
block|}
block|,
block|{
literal|1
block|}
block|,
block|{
literal|0
block|,
literal|6
block|}
block|}
decl_stmt|;
name|int
index|[]
index|[]
name|startOffset
init|=
block|{
block|{
literal|10
block|}
block|,
block|{
literal|40
block|}
block|,
block|{
literal|16
block|}
block|,
block|{
literal|20
block|}
block|,
block|{
literal|35
block|}
block|,
block|{
literal|26
block|}
block|,
block|{
literal|4
block|}
block|,
block|{
literal|0
block|,
literal|31
block|}
block|}
decl_stmt|;
name|int
index|[]
index|[]
name|endOffset
init|=
block|{
block|{
literal|15
block|}
block|,
block|{
literal|43
block|}
block|,
block|{
literal|19
block|}
block|,
block|{
literal|25
block|}
block|,
block|{
literal|39
block|}
block|,
block|{
literal|30
block|}
block|,
block|{
literal|9
block|}
block|,
block|{
literal|3
block|,
literal|34
block|}
block|}
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
name|numDocs
condition|;
name|i
operator|++
control|)
block|{
name|checkAllInfo
argument_list|(
name|numDocs
argument_list|,
name|values
argument_list|,
name|freq
argument_list|,
name|pos
argument_list|,
name|startOffset
argument_list|,
name|endOffset
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|checkWithoutTermStatistics
argument_list|(
name|numDocs
argument_list|,
name|values
argument_list|,
name|freq
argument_list|,
name|pos
argument_list|,
name|startOffset
argument_list|,
name|endOffset
argument_list|,
name|i
argument_list|)
expr_stmt|;
name|checkWithoutFieldStatistics
argument_list|(
name|numDocs
argument_list|,
name|values
argument_list|,
name|freq
argument_list|,
name|pos
argument_list|,
name|startOffset
argument_list|,
name|endOffset
argument_list|,
name|i
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|checkWithoutFieldStatistics
specifier|private
name|void
name|checkWithoutFieldStatistics
parameter_list|(
name|int
name|numDocs
parameter_list|,
name|String
index|[]
name|values
parameter_list|,
name|int
index|[]
name|freq
parameter_list|,
name|int
index|[]
index|[]
name|pos
parameter_list|,
name|int
index|[]
index|[]
name|startOffset
parameter_list|,
name|int
index|[]
index|[]
name|endOffset
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|TermVectorRequestBuilder
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareTermVector
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
name|setPayloads
argument_list|(
literal|true
argument_list|)
operator|.
name|setOffsets
argument_list|(
literal|true
argument_list|)
operator|.
name|setPositions
argument_list|(
literal|true
argument_list|)
operator|.
name|setTermStatistics
argument_list|(
literal|true
argument_list|)
operator|.
name|setFieldStatistics
argument_list|(
literal|false
argument_list|)
operator|.
name|setSelectedFields
argument_list|()
decl_stmt|;
name|TermVectorResponse
name|response
init|=
name|resp
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"doc id: "
operator|+
name|i
operator|+
literal|" doesn't exists but should"
argument_list|,
name|response
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
name|Fields
name|fields
init|=
name|response
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fields
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
name|Terms
name|terms
init|=
name|fields
operator|.
name|terms
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|8l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getSumTotalTermFreq
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
operator|(
name|long
operator|)
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getSumDocFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|TermsEnum
name|iterator
init|=
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|String
name|string
init|=
name|values
index|[
name|j
index|]
decl_stmt|;
name|BytesRef
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|next
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"expected "
operator|+
name|string
argument_list|,
name|string
argument_list|,
name|equalTo
argument_list|(
name|next
operator|.
name|utf8ToString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|string
operator|.
name|equals
argument_list|(
literal|"the"
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
literal|"expected ttf of "
operator|+
name|string
argument_list|,
name|numDocs
operator|*
literal|2
argument_list|,
name|equalTo
argument_list|(
operator|(
name|int
operator|)
name|iterator
operator|.
name|totalTermFreq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
literal|"expected ttf of "
operator|+
name|string
argument_list|,
name|numDocs
argument_list|,
name|equalTo
argument_list|(
operator|(
name|int
operator|)
name|iterator
operator|.
name|totalTermFreq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|DocsAndPositionsEnum
name|docsAndPositions
init|=
name|iterator
operator|.
name|docsAndPositions
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docsAndPositions
operator|.
name|nextDoc
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|,
name|equalTo
argument_list|(
name|docsAndPositions
operator|.
name|freq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|iterator
operator|.
name|docFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
name|int
index|[]
name|termPos
init|=
name|pos
index|[
name|j
index|]
decl_stmt|;
name|int
index|[]
name|termStartOffset
init|=
name|startOffset
index|[
name|j
index|]
decl_stmt|;
name|int
index|[]
name|termEndOffset
init|=
name|endOffset
index|[
name|j
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|termPos
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termStartOffset
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termEndOffset
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|freq
index|[
name|j
index|]
condition|;
name|k
operator|++
control|)
block|{
name|int
name|nextPosition
init|=
name|docsAndPositions
operator|.
name|nextPosition
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|nextPosition
argument_list|,
name|equalTo
argument_list|(
name|termPos
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|startOffset
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termStartOffset
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|endOffset
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termEndOffset
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|getPayload
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"word"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
argument_list|,
name|Matchers
operator|.
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|XContentBuilder
name|xBuilder
init|=
operator|new
name|XContentFactory
argument_list|()
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|response
operator|.
name|toXContent
argument_list|(
name|xBuilder
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BytesStream
name|bytesStream
init|=
name|xBuilder
operator|.
name|bytesStream
argument_list|()
decl_stmt|;
name|String
name|utf8
init|=
name|bytesStream
operator|.
name|bytes
argument_list|()
operator|.
name|toUtf8
argument_list|()
decl_stmt|;
name|String
name|expectedString
init|=
literal|"{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\""
operator|+
name|i
operator|+
literal|"\",\"_version\":1,\"exists\":true,\"term_vectors\":{\"field\":{\"terms\":{\"brown\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":2,\"start_offset\":10,\"end_offset\":15,\"payload\":\"d29yZA==\"}]},\"dog\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":8,\"start_offset\":40,\"end_offset\":43,\"payload\":\"d29yZA==\"}]},\"fox\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":3,\"start_offset\":16,\"end_offset\":19,\"payload\":\"d29yZA==\"}]},\"jumps\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":4,\"start_offset\":20,\"end_offset\":25,\"payload\":\"d29yZA==\"}]},\"lazy\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":7,\"start_offset\":35,\"end_offset\":39,\"payload\":\"d29yZA==\"}]},\"over\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":5,\"start_offset\":26,\"end_offset\":30,\"payload\":\"d29yZA==\"}]},\"quick\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":1,\"start_offset\":4,\"end_offset\":9,\"payload\":\"d29yZA==\"}]},\"the\":{\"doc_freq\":15,\"ttf\":30,\"term_freq\":2,\"tokens\":[{\"position\":0,\"start_offset\":0,\"end_offset\":3,\"payload\":\"d29yZA==\"},{\"position\":6,\"start_offset\":31,\"end_offset\":34,\"payload\":\"d29yZA==\"}]}}}}}"
decl_stmt|;
name|assertThat
argument_list|(
name|utf8
argument_list|,
name|equalTo
argument_list|(
name|expectedString
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|checkWithoutTermStatistics
specifier|private
name|void
name|checkWithoutTermStatistics
parameter_list|(
name|int
name|numDocs
parameter_list|,
name|String
index|[]
name|values
parameter_list|,
name|int
index|[]
name|freq
parameter_list|,
name|int
index|[]
index|[]
name|pos
parameter_list|,
name|int
index|[]
index|[]
name|startOffset
parameter_list|,
name|int
index|[]
index|[]
name|endOffset
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|TermVectorRequestBuilder
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareTermVector
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
name|setPayloads
argument_list|(
literal|true
argument_list|)
operator|.
name|setOffsets
argument_list|(
literal|true
argument_list|)
operator|.
name|setPositions
argument_list|(
literal|true
argument_list|)
operator|.
name|setTermStatistics
argument_list|(
literal|false
argument_list|)
operator|.
name|setFieldStatistics
argument_list|(
literal|true
argument_list|)
operator|.
name|setSelectedFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resp
operator|.
name|request
argument_list|()
operator|.
name|termStatistics
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|TermVectorResponse
name|response
init|=
name|resp
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"doc id: "
operator|+
name|i
operator|+
literal|" doesn't exists but should"
argument_list|,
name|response
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
name|Fields
name|fields
init|=
name|response
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fields
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
name|Terms
name|terms
init|=
name|fields
operator|.
name|terms
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|8l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getSumTotalTermFreq
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|9
operator|*
name|numDocs
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getSumDocFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocs
operator|*
name|values
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|TermsEnum
name|iterator
init|=
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|String
name|string
init|=
name|values
index|[
name|j
index|]
decl_stmt|;
name|BytesRef
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|next
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"expected "
operator|+
name|string
argument_list|,
name|string
argument_list|,
name|equalTo
argument_list|(
name|next
operator|.
name|utf8ToString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"expected ttf of "
operator|+
name|string
argument_list|,
operator|-
literal|1
argument_list|,
name|equalTo
argument_list|(
operator|(
name|int
operator|)
name|iterator
operator|.
name|totalTermFreq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|DocsAndPositionsEnum
name|docsAndPositions
init|=
name|iterator
operator|.
name|docsAndPositions
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docsAndPositions
operator|.
name|nextDoc
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|,
name|equalTo
argument_list|(
name|docsAndPositions
operator|.
name|freq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|iterator
operator|.
name|docFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|-
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|int
index|[]
name|termPos
init|=
name|pos
index|[
name|j
index|]
decl_stmt|;
name|int
index|[]
name|termStartOffset
init|=
name|startOffset
index|[
name|j
index|]
decl_stmt|;
name|int
index|[]
name|termEndOffset
init|=
name|endOffset
index|[
name|j
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|termPos
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termStartOffset
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termEndOffset
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|freq
index|[
name|j
index|]
condition|;
name|k
operator|++
control|)
block|{
name|int
name|nextPosition
init|=
name|docsAndPositions
operator|.
name|nextPosition
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|nextPosition
argument_list|,
name|equalTo
argument_list|(
name|termPos
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|startOffset
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termStartOffset
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|endOffset
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termEndOffset
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|getPayload
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"word"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
argument_list|,
name|Matchers
operator|.
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|XContentBuilder
name|xBuilder
init|=
operator|new
name|XContentFactory
argument_list|()
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|response
operator|.
name|toXContent
argument_list|(
name|xBuilder
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BytesStream
name|bytesStream
init|=
name|xBuilder
operator|.
name|bytesStream
argument_list|()
decl_stmt|;
name|String
name|utf8
init|=
name|bytesStream
operator|.
name|bytes
argument_list|()
operator|.
name|toUtf8
argument_list|()
decl_stmt|;
name|String
name|expectedString
init|=
literal|"{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\""
operator|+
name|i
operator|+
literal|"\",\"_version\":1,\"exists\":true,\"term_vectors\":{\"field\":{\"field_statistics\":{\"sum_doc_freq\":120,\"doc_count\":15,\"sum_ttf\":135},\"terms\":{\"brown\":{\"term_freq\":1,\"tokens\":[{\"position\":2,\"start_offset\":10,\"end_offset\":15,\"payload\":\"d29yZA==\"}]},\"dog\":{\"term_freq\":1,\"tokens\":[{\"position\":8,\"start_offset\":40,\"end_offset\":43,\"payload\":\"d29yZA==\"}]},\"fox\":{\"term_freq\":1,\"tokens\":[{\"position\":3,\"start_offset\":16,\"end_offset\":19,\"payload\":\"d29yZA==\"}]},\"jumps\":{\"term_freq\":1,\"tokens\":[{\"position\":4,\"start_offset\":20,\"end_offset\":25,\"payload\":\"d29yZA==\"}]},\"lazy\":{\"term_freq\":1,\"tokens\":[{\"position\":7,\"start_offset\":35,\"end_offset\":39,\"payload\":\"d29yZA==\"}]},\"over\":{\"term_freq\":1,\"tokens\":[{\"position\":5,\"start_offset\":26,\"end_offset\":30,\"payload\":\"d29yZA==\"}]},\"quick\":{\"term_freq\":1,\"tokens\":[{\"position\":1,\"start_offset\":4,\"end_offset\":9,\"payload\":\"d29yZA==\"}]},\"the\":{\"term_freq\":2,\"tokens\":[{\"position\":0,\"start_offset\":0,\"end_offset\":3,\"payload\":\"d29yZA==\"},{\"position\":6,\"start_offset\":31,\"end_offset\":34,\"payload\":\"d29yZA==\"}]}}}}}"
decl_stmt|;
name|assertThat
argument_list|(
name|utf8
argument_list|,
name|equalTo
argument_list|(
name|expectedString
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|checkAllInfo
specifier|private
name|void
name|checkAllInfo
parameter_list|(
name|int
name|numDocs
parameter_list|,
name|String
index|[]
name|values
parameter_list|,
name|int
index|[]
name|freq
parameter_list|,
name|int
index|[]
index|[]
name|pos
parameter_list|,
name|int
index|[]
index|[]
name|startOffset
parameter_list|,
name|int
index|[]
index|[]
name|endOffset
parameter_list|,
name|int
name|i
parameter_list|)
throws|throws
name|IOException
block|{
name|TermVectorRequestBuilder
name|resp
init|=
name|client
argument_list|()
operator|.
name|prepareTermVector
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
name|setPayloads
argument_list|(
literal|true
argument_list|)
operator|.
name|setOffsets
argument_list|(
literal|true
argument_list|)
operator|.
name|setPositions
argument_list|(
literal|true
argument_list|)
operator|.
name|setFieldStatistics
argument_list|(
literal|true
argument_list|)
operator|.
name|setTermStatistics
argument_list|(
literal|true
argument_list|)
operator|.
name|setSelectedFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|resp
operator|.
name|request
argument_list|()
operator|.
name|fieldStatistics
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|TermVectorResponse
name|response
init|=
name|resp
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"doc id: "
operator|+
name|i
operator|+
literal|" doesn't exists but should"
argument_list|,
name|response
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
name|Fields
name|fields
init|=
name|response
operator|.
name|getFields
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|fields
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
name|Terms
name|terms
init|=
name|fields
operator|.
name|terms
argument_list|(
literal|"field"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|8l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getSumTotalTermFreq
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
call|(
name|long
call|)
argument_list|(
literal|9
operator|*
name|numDocs
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getDocCount
argument_list|()
argument_list|,
name|Matchers
operator|.
name|equalTo
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|terms
operator|.
name|getSumDocFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|numDocs
operator|*
name|values
operator|.
name|length
argument_list|)
argument_list|)
expr_stmt|;
name|TermsEnum
name|iterator
init|=
name|terms
operator|.
name|iterator
argument_list|(
literal|null
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|j
init|=
literal|0
init|;
name|j
operator|<
name|values
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|String
name|string
init|=
name|values
index|[
name|j
index|]
decl_stmt|;
name|BytesRef
name|next
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|next
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"expected "
operator|+
name|string
argument_list|,
name|string
argument_list|,
name|equalTo
argument_list|(
name|next
operator|.
name|utf8ToString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|next
argument_list|,
name|Matchers
operator|.
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|string
operator|.
name|equals
argument_list|(
literal|"the"
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
literal|"expected ttf of "
operator|+
name|string
argument_list|,
name|numDocs
operator|*
literal|2
argument_list|,
name|equalTo
argument_list|(
operator|(
name|int
operator|)
name|iterator
operator|.
name|totalTermFreq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
literal|"expected ttf of "
operator|+
name|string
argument_list|,
name|numDocs
argument_list|,
name|equalTo
argument_list|(
operator|(
name|int
operator|)
name|iterator
operator|.
name|totalTermFreq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|DocsAndPositionsEnum
name|docsAndPositions
init|=
name|iterator
operator|.
name|docsAndPositions
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|docsAndPositions
operator|.
name|nextDoc
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|,
name|equalTo
argument_list|(
name|docsAndPositions
operator|.
name|freq
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|iterator
operator|.
name|docFreq
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|numDocs
argument_list|)
argument_list|)
expr_stmt|;
name|int
index|[]
name|termPos
init|=
name|pos
index|[
name|j
index|]
decl_stmt|;
name|int
index|[]
name|termStartOffset
init|=
name|startOffset
index|[
name|j
index|]
decl_stmt|;
name|int
index|[]
name|termEndOffset
init|=
name|endOffset
index|[
name|j
index|]
decl_stmt|;
name|assertThat
argument_list|(
name|termPos
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termStartOffset
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termEndOffset
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
name|freq
index|[
name|j
index|]
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|k
init|=
literal|0
init|;
name|k
operator|<
name|freq
index|[
name|j
index|]
condition|;
name|k
operator|++
control|)
block|{
name|int
name|nextPosition
init|=
name|docsAndPositions
operator|.
name|nextPosition
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|nextPosition
argument_list|,
name|equalTo
argument_list|(
name|termPos
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|startOffset
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termStartOffset
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|endOffset
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|termEndOffset
index|[
name|k
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"term: "
operator|+
name|string
argument_list|,
name|docsAndPositions
operator|.
name|getPayload
argument_list|()
argument_list|,
name|equalTo
argument_list|(
operator|new
name|BytesRef
argument_list|(
literal|"word"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|assertThat
argument_list|(
name|iterator
operator|.
name|next
argument_list|()
argument_list|,
name|Matchers
operator|.
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|XContentBuilder
name|xBuilder
init|=
operator|new
name|XContentFactory
argument_list|()
operator|.
name|jsonBuilder
argument_list|()
decl_stmt|;
name|response
operator|.
name|toXContent
argument_list|(
name|xBuilder
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|BytesStream
name|bytesStream
init|=
name|xBuilder
operator|.
name|bytesStream
argument_list|()
decl_stmt|;
name|String
name|utf8
init|=
name|bytesStream
operator|.
name|bytes
argument_list|()
operator|.
name|toUtf8
argument_list|()
decl_stmt|;
name|String
name|expectedString
init|=
literal|"{\"_index\":\"test\",\"_type\":\"type1\",\"_id\":\""
operator|+
name|i
operator|+
literal|"\",\"_version\":1,\"exists\":true,\"term_vectors\":{\"field\":{\"field_statistics\":{\"sum_doc_freq\":120,\"doc_count\":15,\"sum_ttf\":135},\"terms\":{\"brown\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":2,\"start_offset\":10,\"end_offset\":15,\"payload\":\"d29yZA==\"}]},\"dog\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":8,\"start_offset\":40,\"end_offset\":43,\"payload\":\"d29yZA==\"}]},\"fox\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":3,\"start_offset\":16,\"end_offset\":19,\"payload\":\"d29yZA==\"}]},\"jumps\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":4,\"start_offset\":20,\"end_offset\":25,\"payload\":\"d29yZA==\"}]},\"lazy\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":7,\"start_offset\":35,\"end_offset\":39,\"payload\":\"d29yZA==\"}]},\"over\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":5,\"start_offset\":26,\"end_offset\":30,\"payload\":\"d29yZA==\"}]},\"quick\":{\"doc_freq\":15,\"ttf\":15,\"term_freq\":1,\"tokens\":[{\"position\":1,\"start_offset\":4,\"end_offset\":9,\"payload\":\"d29yZA==\"}]},\"the\":{\"doc_freq\":15,\"ttf\":30,\"term_freq\":2,\"tokens\":[{\"position\":0,\"start_offset\":0,\"end_offset\":3,\"payload\":\"d29yZA==\"},{\"position\":6,\"start_offset\":31,\"end_offset\":34,\"payload\":\"d29yZA==\"}]}}}}}"
decl_stmt|;
name|assertThat
argument_list|(
name|utf8
argument_list|,
name|equalTo
argument_list|(
name|expectedString
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

