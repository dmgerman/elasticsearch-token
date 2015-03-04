begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper.internal
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
operator|.
name|internal
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|ParsedDocument
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|SortedSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
import|;
end_import

begin_class
DECL|class|FieldNamesFieldMapperTests
specifier|public
class|class
name|FieldNamesFieldMapperTests
extends|extends
name|ElasticsearchSingleNodeTest
block|{
DECL|method|extract
specifier|private
specifier|static
name|SortedSet
argument_list|<
name|String
argument_list|>
name|extract
parameter_list|(
name|String
name|path
parameter_list|)
block|{
name|SortedSet
argument_list|<
name|String
argument_list|>
name|set
init|=
operator|new
name|TreeSet
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|FieldNamesFieldMapper
operator|.
name|extractFieldNames
argument_list|(
name|path
argument_list|)
control|)
block|{
name|set
operator|.
name|add
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
return|return
name|set
return|;
block|}
DECL|method|set
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|SortedSet
argument_list|<
name|T
argument_list|>
name|set
parameter_list|(
name|T
modifier|...
name|values
parameter_list|)
block|{
return|return
operator|new
name|TreeSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|values
argument_list|)
argument_list|)
return|;
block|}
DECL|method|assertFieldNames
name|void
name|assertFieldNames
parameter_list|(
name|SortedSet
argument_list|<
name|String
argument_list|>
name|expected
parameter_list|,
name|ParsedDocument
name|doc
parameter_list|)
block|{
name|String
index|[]
name|got
init|=
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|getValues
argument_list|(
literal|"_field_names"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expected
argument_list|,
name|set
argument_list|(
name|got
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExtractFieldNames
specifier|public
name|void
name|testExtractFieldNames
parameter_list|()
block|{
name|assertEquals
argument_list|(
name|set
argument_list|(
literal|"abc"
argument_list|)
argument_list|,
name|extract
argument_list|(
literal|"abc"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|set
argument_list|(
literal|"a"
argument_list|,
literal|"a.b"
argument_list|)
argument_list|,
name|extract
argument_list|(
literal|"a.b"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|set
argument_list|(
literal|"a"
argument_list|,
literal|"a.b"
argument_list|,
literal|"a.b.c"
argument_list|)
argument_list|,
name|extract
argument_list|(
literal|"a.b.c"
argument_list|)
argument_list|)
expr_stmt|;
comment|// and now corner cases
name|assertEquals
argument_list|(
name|set
argument_list|(
literal|""
argument_list|,
literal|".a"
argument_list|)
argument_list|,
name|extract
argument_list|(
literal|".a"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|set
argument_list|(
literal|"a"
argument_list|,
literal|"a."
argument_list|)
argument_list|,
name|extract
argument_list|(
literal|"a."
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|set
argument_list|(
literal|""
argument_list|,
literal|"."
argument_list|,
literal|".."
argument_list|)
argument_list|,
name|extract
argument_list|(
literal|".."
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testInjectIntoDocDuringParsing
specifier|public
name|void
name|testInjectIntoDocDuringParsing
parameter_list|()
throws|throws
name|Exception
block|{
name|String
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
literal|"type"
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
name|ParsedDocument
name|doc
init|=
name|defaultMapper
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
literal|"1"
argument_list|,
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
literal|"a"
argument_list|,
literal|"100"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"b"
argument_list|)
operator|.
name|field
argument_list|(
literal|"c"
argument_list|,
literal|42
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertFieldNames
argument_list|(
name|set
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|,
literal|"b.c"
argument_list|,
literal|"_uid"
argument_list|,
literal|"_type"
argument_list|,
literal|"_version"
argument_list|,
literal|"_source"
argument_list|,
literal|"_all"
argument_list|)
argument_list|,
name|doc
argument_list|)
expr_stmt|;
block|}
DECL|method|testExplicitEnabled
specifier|public
name|void
name|testExplicitEnabled
parameter_list|()
throws|throws
name|Exception
block|{
name|String
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
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_field_names"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
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
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
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
name|FieldNamesFieldMapper
name|fieldNamesMapper
init|=
name|docMapper
operator|.
name|rootMapper
argument_list|(
name|FieldNamesFieldMapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fieldNamesMapper
operator|.
name|enabled
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
literal|"1"
argument_list|,
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
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertFieldNames
argument_list|(
name|set
argument_list|(
literal|"field"
argument_list|,
literal|"_uid"
argument_list|,
literal|"_type"
argument_list|,
literal|"_version"
argument_list|,
literal|"_source"
argument_list|,
literal|"_all"
argument_list|)
argument_list|,
name|doc
argument_list|)
expr_stmt|;
block|}
DECL|method|testDisabled
specifier|public
name|void
name|testDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|String
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
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_field_names"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|false
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
name|string
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
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
name|FieldNamesFieldMapper
name|fieldNamesMapper
init|=
name|docMapper
operator|.
name|rootMapper
argument_list|(
name|FieldNamesFieldMapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|fieldNamesMapper
operator|.
name|enabled
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
literal|"1"
argument_list|,
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
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|get
argument_list|(
literal|"_field_names"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDisablingBackcompat
specifier|public
name|void
name|testDisablingBackcompat
parameter_list|()
throws|throws
name|Exception
block|{
comment|// before 1.5, disabling happened by setting index:no
name|String
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
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_field_names"
argument_list|)
operator|.
name|field
argument_list|(
literal|"index"
argument_list|,
literal|"no"
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
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|V_1_4_2
operator|.
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|indexSettings
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
name|FieldNamesFieldMapper
name|fieldNamesMapper
init|=
name|docMapper
operator|.
name|rootMapper
argument_list|(
name|FieldNamesFieldMapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|fieldNamesMapper
operator|.
name|enabled
argument_list|()
argument_list|)
expr_stmt|;
name|ParsedDocument
name|doc
init|=
name|docMapper
operator|.
name|parse
argument_list|(
literal|"type"
argument_list|,
literal|"1"
argument_list|,
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
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|doc
operator|.
name|rootDoc
argument_list|()
operator|.
name|get
argument_list|(
literal|"_field_names"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testFieldTypeSettingsBackcompat
specifier|public
name|void
name|testFieldTypeSettingsBackcompat
parameter_list|()
throws|throws
name|Exception
block|{
name|String
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
literal|"type"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"_field_names"
argument_list|)
operator|.
name|field
argument_list|(
literal|"store"
argument_list|,
literal|"yes"
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
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|ImmutableSettings
operator|.
name|builder
argument_list|()
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|Version
operator|.
name|V_1_4_2
operator|.
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|DocumentMapper
name|docMapper
init|=
name|createIndex
argument_list|(
literal|"test"
argument_list|,
name|indexSettings
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
name|FieldNamesFieldMapper
name|fieldNamesMapper
init|=
name|docMapper
operator|.
name|rootMapper
argument_list|(
name|FieldNamesFieldMapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fieldNamesMapper
operator|.
name|fieldType
argument_list|()
operator|.
name|stored
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testMergingMappings
specifier|public
name|void
name|testMergingMappings
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|enabledMapping
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
literal|"_field_names"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|true
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
name|string
argument_list|()
decl_stmt|;
name|String
name|disabledMapping
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
literal|"_field_names"
argument_list|)
operator|.
name|field
argument_list|(
literal|"enabled"
argument_list|,
literal|false
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
name|mapperEnabled
init|=
name|parser
operator|.
name|parse
argument_list|(
name|enabledMapping
argument_list|)
decl_stmt|;
name|DocumentMapper
name|mapperDisabled
init|=
name|parser
operator|.
name|parse
argument_list|(
name|disabledMapping
argument_list|)
decl_stmt|;
name|mapperEnabled
operator|.
name|merge
argument_list|(
name|mapperDisabled
argument_list|,
name|DocumentMapper
operator|.
name|MergeFlags
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|mapperEnabled
operator|.
name|rootMapper
argument_list|(
name|FieldNamesFieldMapper
operator|.
name|class
argument_list|)
operator|.
name|enabled
argument_list|()
argument_list|)
expr_stmt|;
name|mapperEnabled
operator|=
name|parser
operator|.
name|parse
argument_list|(
name|enabledMapping
argument_list|)
expr_stmt|;
name|mapperDisabled
operator|.
name|merge
argument_list|(
name|mapperEnabled
argument_list|,
name|DocumentMapper
operator|.
name|MergeFlags
operator|.
name|mergeFlags
argument_list|()
operator|.
name|simulate
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|mapperEnabled
operator|.
name|rootMapper
argument_list|(
name|FieldNamesFieldMapper
operator|.
name|class
argument_list|)
operator|.
name|enabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

