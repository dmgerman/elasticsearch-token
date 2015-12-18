begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.completion
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|completion
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
name|IndexableField
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
name|GeoHashUtils
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
name|common
operator|.
name|xcontent
operator|.
name|XContentParser
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
name|FieldMapper
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
name|MappedFieldType
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
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|context
operator|.
name|ContextBuilder
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
name|suggest
operator|.
name|completion
operator|.
name|context
operator|.
name|ContextMapping
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
name|suggest
operator|.
name|completion
operator|.
name|context
operator|.
name|GeoContextMapping
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
name|ESSingleNodeTestCase
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
name|Collection
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
name|search
operator|.
name|suggest
operator|.
name|completion
operator|.
name|CategoryContextMappingTests
operator|.
name|assertContextSuggestFields
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
name|isIn
import|;
end_import

begin_class
DECL|class|GeoContextMappingTests
specifier|public
class|class
name|GeoContextMappingTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testIndexingWithNoContexts
specifier|public
name|void
name|testIndexingWithNoContexts
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
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
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"contexts"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"ctx"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
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
name|defaultMapper
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
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|defaultMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"completion"
argument_list|)
decl_stmt|;
name|MappedFieldType
name|completionFieldType
init|=
name|fieldMapper
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
name|defaultMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"completion"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|array
argument_list|(
literal|"input"
argument_list|,
literal|"suggestion1"
argument_list|,
literal|"suggestion2"
argument_list|)
operator|.
name|field
argument_list|(
literal|"weight"
argument_list|,
literal|3
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|array
argument_list|(
literal|"input"
argument_list|,
literal|"suggestion3"
argument_list|,
literal|"suggestion4"
argument_list|)
operator|.
name|field
argument_list|(
literal|"weight"
argument_list|,
literal|4
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"input"
argument_list|,
literal|"suggestion5"
argument_list|,
literal|"suggestion6"
argument_list|,
literal|"suggestion7"
argument_list|)
operator|.
name|field
argument_list|(
literal|"weight"
argument_list|,
literal|5
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|parsedDocument
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
name|completionFieldType
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
name|assertContextSuggestFields
argument_list|(
name|fields
argument_list|,
literal|7
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexingWithSimpleContexts
specifier|public
name|void
name|testIndexingWithSimpleContexts
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
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
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"contexts"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"ctx"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
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
name|defaultMapper
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
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|defaultMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"completion"
argument_list|)
decl_stmt|;
name|MappedFieldType
name|completionFieldType
init|=
name|fieldMapper
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
name|defaultMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"completion"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"input"
argument_list|,
literal|"suggestion5"
argument_list|,
literal|"suggestion6"
argument_list|,
literal|"suggestion7"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"contexts"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"ctx"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|43.6624803
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|79.3863353
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"weight"
argument_list|,
literal|5
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|parsedDocument
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
name|completionFieldType
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
name|assertContextSuggestFields
argument_list|(
name|fields
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexingWithContextList
specifier|public
name|void
name|testIndexingWithContextList
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
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
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"contexts"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"ctx"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
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
name|defaultMapper
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
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|defaultMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"completion"
argument_list|)
decl_stmt|;
name|MappedFieldType
name|completionFieldType
init|=
name|fieldMapper
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
name|defaultMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"input"
argument_list|,
literal|"suggestion5"
argument_list|,
literal|"suggestion6"
argument_list|,
literal|"suggestion7"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"contexts"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"ctx"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|43.6624803
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|79.3863353
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|43.6624718
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
operator|-
literal|79.3873227
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"weight"
argument_list|,
literal|5
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|parsedDocument
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
name|completionFieldType
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
name|assertContextSuggestFields
argument_list|(
name|fields
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexingWithMultipleContexts
specifier|public
name|void
name|testIndexingWithMultipleContexts
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|mapping
init|=
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
literal|"completion"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"completion"
argument_list|)
operator|.
name|startArray
argument_list|(
literal|"contexts"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"loc1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"name"
argument_list|,
literal|"loc2"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"geo"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
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
name|defaultMapper
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
operator|.
name|parse
argument_list|(
name|mapping
argument_list|)
decl_stmt|;
name|FieldMapper
name|fieldMapper
init|=
name|defaultMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"completion"
argument_list|)
decl_stmt|;
name|MappedFieldType
name|completionFieldType
init|=
name|fieldMapper
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startArray
argument_list|(
literal|"completion"
argument_list|)
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"input"
argument_list|,
literal|"suggestion5"
argument_list|,
literal|"suggestion6"
argument_list|,
literal|"suggestion7"
argument_list|)
operator|.
name|field
argument_list|(
literal|"weight"
argument_list|,
literal|5
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"contexts"
argument_list|)
operator|.
name|array
argument_list|(
literal|"loc1"
argument_list|,
literal|"ezs42e44yx96"
argument_list|)
operator|.
name|array
argument_list|(
literal|"loc2"
argument_list|,
literal|"wh0n9447fwrc"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|ParsedDocument
name|parsedDocument
init|=
name|defaultMapper
operator|.
name|parse
argument_list|(
literal|"test"
argument_list|,
literal|"type1"
argument_list|,
literal|"1"
argument_list|,
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|IndexableField
index|[]
name|fields
init|=
name|parsedDocument
operator|.
name|rootDoc
argument_list|()
operator|.
name|getFields
argument_list|(
name|completionFieldType
operator|.
name|names
argument_list|()
operator|.
name|indexName
argument_list|()
argument_list|)
decl_stmt|;
name|assertContextSuggestFields
argument_list|(
name|fields
argument_list|,
literal|3
argument_list|)
expr_stmt|;
block|}
DECL|method|testParsingQueryContextBasic
specifier|public
name|void
name|testParsingQueryContextBasic
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|value
argument_list|(
literal|"ezs42e44yx96"
argument_list|)
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|GeoContextMapping
name|mapping
init|=
name|ContextBuilder
operator|.
name|geo
argument_list|(
literal|"geo"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ContextMapping
operator|.
name|QueryContext
argument_list|>
name|queryContexts
init|=
name|mapping
operator|.
name|parseQueryContext
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryContexts
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
operator|+
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|locations
operator|.
name|add
argument_list|(
literal|"ezs42e"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"ezs42e"
argument_list|,
name|GeoContextMapping
operator|.
name|DEFAULT_PRECISION
argument_list|,
name|locations
argument_list|)
expr_stmt|;
for|for
control|(
name|ContextMapping
operator|.
name|QueryContext
name|queryContext
range|:
name|queryContexts
control|)
block|{
name|assertThat
argument_list|(
name|queryContext
operator|.
name|context
argument_list|,
name|isIn
argument_list|(
name|locations
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryContext
operator|.
name|boost
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryContext
operator|.
name|isPrefix
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParsingQueryContextGeoPoint
specifier|public
name|void
name|testParsingQueryContextGeoPoint
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|23.654242
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
literal|90.047153
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|GeoContextMapping
name|mapping
init|=
name|ContextBuilder
operator|.
name|geo
argument_list|(
literal|"geo"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ContextMapping
operator|.
name|QueryContext
argument_list|>
name|queryContexts
init|=
name|mapping
operator|.
name|parseQueryContext
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryContexts
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
operator|+
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|locations
operator|.
name|add
argument_list|(
literal|"wh0n94"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"wh0n94"
argument_list|,
name|GeoContextMapping
operator|.
name|DEFAULT_PRECISION
argument_list|,
name|locations
argument_list|)
expr_stmt|;
for|for
control|(
name|ContextMapping
operator|.
name|QueryContext
name|queryContext
range|:
name|queryContexts
control|)
block|{
name|assertThat
argument_list|(
name|queryContext
operator|.
name|context
argument_list|,
name|isIn
argument_list|(
name|locations
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryContext
operator|.
name|boost
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryContext
operator|.
name|isPrefix
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParsingQueryContextObject
specifier|public
name|void
name|testParsingQueryContextObject
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"context"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|23.654242
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
literal|90.047153
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
literal|10
argument_list|)
operator|.
name|array
argument_list|(
literal|"neighbours"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
operator|.
name|endObject
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|GeoContextMapping
name|mapping
init|=
name|ContextBuilder
operator|.
name|geo
argument_list|(
literal|"geo"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ContextMapping
operator|.
name|QueryContext
argument_list|>
name|queryContexts
init|=
name|mapping
operator|.
name|parseQueryContext
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryContexts
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
operator|+
literal|1
operator|+
literal|8
operator|+
literal|1
operator|+
literal|8
operator|+
literal|1
operator|+
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|locations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|locations
operator|.
name|add
argument_list|(
literal|"wh0n94"
argument_list|)
expr_stmt|;
name|locations
operator|.
name|add
argument_list|(
literal|"w"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"w"
argument_list|,
literal|1
argument_list|,
name|locations
argument_list|)
expr_stmt|;
name|locations
operator|.
name|add
argument_list|(
literal|"wh"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"wh"
argument_list|,
literal|2
argument_list|,
name|locations
argument_list|)
expr_stmt|;
name|locations
operator|.
name|add
argument_list|(
literal|"wh0"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"wh0"
argument_list|,
literal|3
argument_list|,
name|locations
argument_list|)
expr_stmt|;
for|for
control|(
name|ContextMapping
operator|.
name|QueryContext
name|queryContext
range|:
name|queryContexts
control|)
block|{
name|assertThat
argument_list|(
name|queryContext
operator|.
name|context
argument_list|,
name|isIn
argument_list|(
name|locations
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryContext
operator|.
name|boost
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryContext
operator|.
name|isPrefix
argument_list|,
name|equalTo
argument_list|(
name|queryContext
operator|.
name|context
operator|.
name|length
argument_list|()
operator|<
name|GeoContextMapping
operator|.
name|DEFAULT_PRECISION
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParsingQueryContextObjectArray
specifier|public
name|void
name|testParsingQueryContextObjectArray
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"context"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|23.654242
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
literal|90.047153
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
literal|10
argument_list|)
operator|.
name|array
argument_list|(
literal|"neighbours"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|,
literal|3
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"context"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|22.337374
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
literal|92.112583
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
literal|2
argument_list|)
operator|.
name|array
argument_list|(
literal|"neighbours"
argument_list|,
literal|5
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|GeoContextMapping
name|mapping
init|=
name|ContextBuilder
operator|.
name|geo
argument_list|(
literal|"geo"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ContextMapping
operator|.
name|QueryContext
argument_list|>
name|queryContexts
init|=
name|mapping
operator|.
name|parseQueryContext
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryContexts
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
operator|+
literal|1
operator|+
literal|8
operator|+
literal|1
operator|+
literal|8
operator|+
literal|1
operator|+
literal|8
operator|+
literal|1
operator|+
literal|1
operator|+
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|firstLocations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|firstLocations
operator|.
name|add
argument_list|(
literal|"wh0n94"
argument_list|)
expr_stmt|;
name|firstLocations
operator|.
name|add
argument_list|(
literal|"w"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"w"
argument_list|,
literal|1
argument_list|,
name|firstLocations
argument_list|)
expr_stmt|;
name|firstLocations
operator|.
name|add
argument_list|(
literal|"wh"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"wh"
argument_list|,
literal|2
argument_list|,
name|firstLocations
argument_list|)
expr_stmt|;
name|firstLocations
operator|.
name|add
argument_list|(
literal|"wh0"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"wh0"
argument_list|,
literal|3
argument_list|,
name|firstLocations
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|secondLocations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|secondLocations
operator|.
name|add
argument_list|(
literal|"w5cx04"
argument_list|)
expr_stmt|;
name|secondLocations
operator|.
name|add
argument_list|(
literal|"w5cx0"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"w5cx0"
argument_list|,
literal|5
argument_list|,
name|secondLocations
argument_list|)
expr_stmt|;
for|for
control|(
name|ContextMapping
operator|.
name|QueryContext
name|queryContext
range|:
name|queryContexts
control|)
block|{
if|if
condition|(
name|firstLocations
operator|.
name|contains
argument_list|(
name|queryContext
operator|.
name|context
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|queryContext
operator|.
name|boost
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|secondLocations
operator|.
name|contains
argument_list|(
name|queryContext
operator|.
name|context
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|queryContext
operator|.
name|boost
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
name|queryContext
operator|.
name|context
operator|+
literal|" was not expected"
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|queryContext
operator|.
name|isPrefix
argument_list|,
name|equalTo
argument_list|(
name|queryContext
operator|.
name|context
operator|.
name|length
argument_list|()
operator|<
name|GeoContextMapping
operator|.
name|DEFAULT_PRECISION
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testParsingQueryContextMixed
specifier|public
name|void
name|testParsingQueryContextMixed
parameter_list|()
throws|throws
name|Exception
block|{
name|XContentBuilder
name|builder
init|=
name|jsonBuilder
argument_list|()
operator|.
name|startArray
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|startObject
argument_list|(
literal|"context"
argument_list|)
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|23.654242
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
literal|90.047153
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"boost"
argument_list|,
literal|10
argument_list|)
operator|.
name|array
argument_list|(
literal|"neighbours"
argument_list|,
literal|1
argument_list|,
literal|2
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"lat"
argument_list|,
literal|22.337374
argument_list|)
operator|.
name|field
argument_list|(
literal|"lon"
argument_list|,
literal|92.112583
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endArray
argument_list|()
decl_stmt|;
name|XContentParser
name|parser
init|=
name|XContentFactory
operator|.
name|xContent
argument_list|(
name|XContentType
operator|.
name|JSON
argument_list|)
operator|.
name|createParser
argument_list|(
name|builder
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|GeoContextMapping
name|mapping
init|=
name|ContextBuilder
operator|.
name|geo
argument_list|(
literal|"geo"
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ContextMapping
operator|.
name|QueryContext
argument_list|>
name|queryContexts
init|=
name|mapping
operator|.
name|parseQueryContext
argument_list|(
name|parser
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|queryContexts
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
operator|+
literal|1
operator|+
literal|8
operator|+
literal|1
operator|+
literal|8
operator|+
literal|1
operator|+
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|firstLocations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|firstLocations
operator|.
name|add
argument_list|(
literal|"wh0n94"
argument_list|)
expr_stmt|;
name|firstLocations
operator|.
name|add
argument_list|(
literal|"w"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"w"
argument_list|,
literal|1
argument_list|,
name|firstLocations
argument_list|)
expr_stmt|;
name|firstLocations
operator|.
name|add
argument_list|(
literal|"wh"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"wh"
argument_list|,
literal|2
argument_list|,
name|firstLocations
argument_list|)
expr_stmt|;
name|Collection
argument_list|<
name|String
argument_list|>
name|secondLocations
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|secondLocations
operator|.
name|add
argument_list|(
literal|"w5cx04"
argument_list|)
expr_stmt|;
name|GeoHashUtils
operator|.
name|addNeighbors
argument_list|(
literal|"w5cx04"
argument_list|,
literal|6
argument_list|,
name|secondLocations
argument_list|)
expr_stmt|;
for|for
control|(
name|ContextMapping
operator|.
name|QueryContext
name|queryContext
range|:
name|queryContexts
control|)
block|{
if|if
condition|(
name|firstLocations
operator|.
name|contains
argument_list|(
name|queryContext
operator|.
name|context
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|queryContext
operator|.
name|boost
argument_list|,
name|equalTo
argument_list|(
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|secondLocations
operator|.
name|contains
argument_list|(
name|queryContext
operator|.
name|context
argument_list|)
condition|)
block|{
name|assertThat
argument_list|(
name|queryContext
operator|.
name|boost
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|fail
argument_list|(
name|queryContext
operator|.
name|context
operator|+
literal|" was not expected"
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|queryContext
operator|.
name|isPrefix
argument_list|,
name|equalTo
argument_list|(
name|queryContext
operator|.
name|context
operator|.
name|length
argument_list|()
operator|<
name|GeoContextMapping
operator|.
name|DEFAULT_PRECISION
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

