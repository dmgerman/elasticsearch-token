begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.merge
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|merge
package|;
end_package

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
name|bytes
operator|.
name|BytesReference
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
name|compress
operator|.
name|CompressedXContent
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
name|index
operator|.
name|analysis
operator|.
name|FieldNameAnalyzer
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
name|analysis
operator|.
name|NamedAnalyzer
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
name|DocumentFieldMappers
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
name|DocumentMapper
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
name|DocumentMapperParser
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
name|MapperService
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
name|Mapping
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
name|MergeResult
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
name|ParsedDocument
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
name|core
operator|.
name|StringFieldMapper
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
name|object
operator|.
name|ObjectMapper
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
name|concurrent
operator|.
name|CyclicBarrier
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|nullValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|TestMergeMapperTests
specifier|public
class|class
name|TestMergeMapperTests
extends|extends
name|ElasticsearchSingleNodeTest
block|{
annotation|@
name|Test
DECL|method|test1Merge
specifier|public
name|void
name|test1Merge
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|stage1Mapping
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
literal|"person"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"name"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapperParser
name|parser
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
name|DocumentMapper
name|stage1
init|=
name|parser
operator|.
name|parse
argument_list|(
name|stage1Mapping
argument_list|)
decl_stmt|;
name|String
name|stage2Mapping
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
literal|"person"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"name"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"age"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"obj1"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"prop1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"integer"
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
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|stage2
init|=
name|parser
operator|.
name|parse
argument_list|(
name|stage2Mapping
argument_list|)
decl_stmt|;
name|MergeResult
name|mergeResult
init|=
name|stage1
operator|.
name|merge
argument_list|(
name|stage2
operator|.
name|mapping
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// since we are simulating, we should not have the age mapping
name|assertThat
argument_list|(
name|stage1
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"age"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stage1
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"obj1.prop1"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
comment|// now merge, don't simulate
name|mergeResult
operator|=
name|stage1
operator|.
name|merge
argument_list|(
name|stage2
operator|.
name|mapping
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// there is still merge failures
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
comment|// but we have the age in
name|assertThat
argument_list|(
name|stage1
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"age"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|stage1
operator|.
name|mappers
argument_list|()
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"obj1.prop1"
argument_list|)
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMergeObjectDynamic
specifier|public
name|void
name|testMergeObjectDynamic
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapperParser
name|parser
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
name|String
name|objectMapping
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
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|mapper
init|=
name|parser
operator|.
name|parse
argument_list|(
name|objectMapping
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|mapper
operator|.
name|root
argument_list|()
operator|.
name|dynamic
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|withDynamicMapping
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
name|field
argument_list|(
literal|"dynamic"
argument_list|,
literal|"false"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|withDynamicMapper
init|=
name|parser
operator|.
name|parse
argument_list|(
name|withDynamicMapping
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|withDynamicMapper
operator|.
name|root
argument_list|()
operator|.
name|dynamic
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ObjectMapper
operator|.
name|Dynamic
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
name|MergeResult
name|mergeResult
init|=
name|mapper
operator|.
name|merge
argument_list|(
name|withDynamicMapper
operator|.
name|mapping
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
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
name|mapper
operator|.
name|root
argument_list|()
operator|.
name|dynamic
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ObjectMapper
operator|.
name|Dynamic
operator|.
name|FALSE
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMergeObjectAndNested
specifier|public
name|void
name|testMergeObjectAndNested
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapperParser
name|parser
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
name|String
name|objectMapping
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
literal|"obj"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"object"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|objectMapper
init|=
name|parser
operator|.
name|parse
argument_list|(
name|objectMapping
argument_list|)
decl_stmt|;
name|String
name|nestedMapping
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
literal|"obj"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"nested"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|nestedMapper
init|=
name|parser
operator|.
name|parse
argument_list|(
name|nestedMapping
argument_list|)
decl_stmt|;
name|MergeResult
name|mergeResult
init|=
name|objectMapper
operator|.
name|merge
argument_list|(
name|nestedMapper
operator|.
name|mapping
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
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
name|mergeResult
operator|.
name|buildConflicts
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
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|buildConflicts
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"object mapping [obj] can't be changed from non-nested to nested"
argument_list|)
argument_list|)
expr_stmt|;
name|mergeResult
operator|=
name|nestedMapper
operator|.
name|merge
argument_list|(
name|objectMapper
operator|.
name|mapping
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|buildConflicts
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
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|buildConflicts
argument_list|()
index|[
literal|0
index|]
argument_list|,
name|equalTo
argument_list|(
literal|"object mapping [obj] can't be changed from nested to non-nested"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testMergeSearchAnalyzer
specifier|public
name|void
name|testMergeSearchAnalyzer
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapperParser
name|parser
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
name|String
name|mapping1
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
literal|"type"
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
literal|"analyzer"
argument_list|,
literal|"standard"
argument_list|)
operator|.
name|field
argument_list|(
literal|"search_analyzer"
argument_list|,
literal|"whitespace"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|String
name|mapping2
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
literal|"type"
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
literal|"analyzer"
argument_list|,
literal|"standard"
argument_list|)
operator|.
name|field
argument_list|(
literal|"search_analyzer"
argument_list|,
literal|"keyword"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|existing
init|=
name|parser
operator|.
name|parse
argument_list|(
name|mapping1
argument_list|)
decl_stmt|;
name|DocumentMapper
name|changed
init|=
name|parser
operator|.
name|parse
argument_list|(
name|mapping2
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|existing
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"whitespace"
argument_list|)
argument_list|)
expr_stmt|;
name|MergeResult
name|mergeResult
init|=
name|existing
operator|.
name|merge
argument_list|(
name|changed
operator|.
name|mapping
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
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
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|existing
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"keyword"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testChangeSearchAnalyzerToDefault
specifier|public
name|void
name|testChangeSearchAnalyzerToDefault
parameter_list|()
throws|throws
name|Exception
block|{
name|DocumentMapperParser
name|parser
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
operator|.
name|documentMapperParser
argument_list|()
decl_stmt|;
name|String
name|mapping1
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
literal|"type"
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
literal|"analyzer"
argument_list|,
literal|"standard"
argument_list|)
operator|.
name|field
argument_list|(
literal|"search_analyzer"
argument_list|,
literal|"whitespace"
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|String
name|mapping2
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
literal|"type"
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
literal|"analyzer"
argument_list|,
literal|"standard"
argument_list|)
operator|.
name|field
argument_list|(
literal|"ignore_above"
argument_list|,
literal|14
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
operator|.
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|existing
init|=
name|parser
operator|.
name|parse
argument_list|(
name|mapping1
argument_list|)
decl_stmt|;
name|DocumentMapper
name|changed
init|=
name|parser
operator|.
name|parse
argument_list|(
name|mapping2
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|existing
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"whitespace"
argument_list|)
argument_list|)
expr_stmt|;
name|MergeResult
name|mergeResult
init|=
name|existing
operator|.
name|merge
argument_list|(
name|changed
operator|.
name|mapping
argument_list|()
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|mergeResult
operator|.
name|hasConflicts
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
operator|(
operator|(
name|NamedAnalyzer
operator|)
name|existing
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|searchAnalyzer
argument_list|()
operator|)
operator|.
name|name
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"standard"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|StringFieldMapper
call|)
argument_list|(
name|existing
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field"
argument_list|)
argument_list|)
operator|)
operator|.
name|getIgnoreAbove
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|14
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testConcurrentMergeTest
specifier|public
name|void
name|testConcurrentMergeTest
parameter_list|()
throws|throws
name|Throwable
block|{
specifier|final
name|MapperService
name|mapperService
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|)
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
literal|"test"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
literal|"{\"test\":{}}"
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
specifier|final
name|DocumentMapper
name|documentMapper
init|=
name|mapperService
operator|.
name|documentMapper
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
name|DocumentFieldMappers
name|dfm
init|=
name|documentMapper
operator|.
name|mappers
argument_list|()
decl_stmt|;
try|try
block|{
operator|(
operator|(
name|FieldNameAnalyzer
operator|)
name|dfm
operator|.
name|indexAnalyzer
argument_list|()
operator|)
operator|.
name|getWrappedAnalyzer
argument_list|(
literal|"non_existing_field"
argument_list|)
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
comment|// ok that's expected
block|}
specifier|final
name|AtomicBoolean
name|stopped
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|final
name|CyclicBarrier
name|barrier
init|=
operator|new
name|CyclicBarrier
argument_list|(
literal|2
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|String
argument_list|>
name|lastIntroducedFieldName
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Throwable
argument_list|>
name|error
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Thread
name|updater
init|=
operator|new
name|Thread
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
try|try
block|{
name|barrier
operator|.
name|await
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
literal|200
operator|&&
name|stopped
operator|.
name|get
argument_list|()
operator|==
literal|false
condition|;
name|i
operator|++
control|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|ParsedDocument
name|doc
init|=
name|documentMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
name|fieldName
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"{ \""
operator|+
name|fieldName
operator|+
literal|"\" : \"test\" }"
argument_list|)
argument_list|)
decl_stmt|;
name|Mapping
name|update
init|=
name|doc
operator|.
name|dynamicMappingsUpdate
argument_list|()
decl_stmt|;
assert|assert
name|update
operator|!=
literal|null
assert|;
name|lastIntroducedFieldName
operator|.
name|set
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
literal|"test"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|update
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|error
operator|.
name|set
argument_list|(
name|t
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|stopped
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
block|}
decl_stmt|;
name|updater
operator|.
name|start
argument_list|()
expr_stmt|;
try|try
block|{
name|barrier
operator|.
name|await
argument_list|()
expr_stmt|;
while|while
condition|(
name|stopped
operator|.
name|get
argument_list|()
operator|==
literal|false
condition|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|lastIntroducedFieldName
operator|.
name|get
argument_list|()
decl_stmt|;
specifier|final
name|BytesReference
name|source
init|=
operator|new
name|BytesArray
argument_list|(
literal|"{ \""
operator|+
name|fieldName
operator|+
literal|"\" : \"test\" }"
argument_list|)
decl_stmt|;
name|ParsedDocument
name|parsedDoc
init|=
name|documentMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"random"
argument_list|,
name|source
argument_list|)
decl_stmt|;
if|if
condition|(
name|parsedDoc
operator|.
name|dynamicMappingsUpdate
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// not in the mapping yet, try again
continue|continue;
block|}
name|dfm
operator|=
name|documentMapper
operator|.
name|mappers
argument_list|()
expr_stmt|;
operator|(
operator|(
name|FieldNameAnalyzer
operator|)
name|dfm
operator|.
name|indexAnalyzer
argument_list|()
operator|)
operator|.
name|getWrappedAnalyzer
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|stopped
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|updater
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|error
operator|.
name|get
argument_list|()
operator|!=
literal|null
condition|)
block|{
throw|throw
name|error
operator|.
name|get
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

