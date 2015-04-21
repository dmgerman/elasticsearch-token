begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterators
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
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
name|document
operator|.
name|FieldType
import|;
end_import

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
name|index
operator|.
name|fielddata
operator|.
name|FieldDataType
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
name|AbstractFieldMapper
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
name|ElasticsearchTestCase
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
name|Iterator
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

begin_class
DECL|class|FieldMappersLookupTests
specifier|public
class|class
name|FieldMappersLookupTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|method|testEmpty
specifier|public
name|void
name|testEmpty
parameter_list|()
block|{
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|fullName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|indexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
name|lookup
operator|.
name|simpleMatchToFullName
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|names
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|names
operator|=
name|lookup
operator|.
name|simpleMatchToFullName
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|names
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|smartName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|Iterator
argument_list|<
name|FieldMapper
argument_list|<
name|?
argument_list|>
argument_list|>
name|itr
init|=
name|lookup
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|itr
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNewField
specifier|public
name|void
name|testNewField
parameter_list|()
block|{
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|FakeFieldMapper
name|f
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup2
init|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f
argument_list|)
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|fullName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|indexName
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|FieldMappers
name|mappers
init|=
name|lookup2
operator|.
name|fullName
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mappers
operator|.
name|mappers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f
argument_list|,
name|mappers
operator|.
name|mapper
argument_list|()
argument_list|)
expr_stmt|;
name|mappers
operator|=
name|lookup2
operator|.
name|indexName
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mappers
operator|.
name|mappers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f
argument_list|,
name|mappers
operator|.
name|mapper
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|Iterators
operator|.
name|size
argument_list|(
name|lookup2
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testExtendField
specifier|public
name|void
name|testExtendField
parameter_list|()
block|{
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|FakeFieldMapper
name|f
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|FakeFieldMapper
name|other
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"blah"
argument_list|,
literal|"blah"
argument_list|)
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f
argument_list|,
name|other
argument_list|)
argument_list|)
expr_stmt|;
name|FakeFieldMapper
name|f2
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup2
init|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f2
argument_list|)
argument_list|)
decl_stmt|;
name|FieldMappers
name|mappers
init|=
name|lookup2
operator|.
name|fullName
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|mappers
operator|.
name|mappers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|mappers
operator|=
name|lookup2
operator|.
name|indexName
argument_list|(
literal|"bar"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|mappers
operator|.
name|mappers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|Iterators
operator|.
name|size
argument_list|(
name|lookup2
operator|.
name|iterator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexName
specifier|public
name|void
name|testIndexName
parameter_list|()
block|{
name|FakeFieldMapper
name|f1
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f1
argument_list|)
argument_list|)
expr_stmt|;
name|FieldMappers
name|mappers
init|=
name|lookup
operator|.
name|indexName
argument_list|(
literal|"foo"
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|mappers
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|mappers
operator|.
name|mappers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f1
argument_list|,
name|mappers
operator|.
name|mapper
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleMatchIndexNames
specifier|public
name|void
name|testSimpleMatchIndexNames
parameter_list|()
block|{
name|FakeFieldMapper
name|f1
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"baz"
argument_list|)
decl_stmt|;
name|FakeFieldMapper
name|f2
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"bar"
argument_list|,
literal|"boo"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f1
argument_list|,
name|f2
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
name|lookup
operator|.
name|simpleMatchToIndexNames
argument_list|(
literal|"b*"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"baz"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"boo"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleMatchFullNames
specifier|public
name|void
name|testSimpleMatchFullNames
parameter_list|()
block|{
name|FakeFieldMapper
name|f1
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"baz"
argument_list|)
decl_stmt|;
name|FakeFieldMapper
name|f2
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"bar"
argument_list|,
literal|"boo"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f1
argument_list|,
name|f2
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|names
init|=
name|lookup
operator|.
name|simpleMatchToFullName
argument_list|(
literal|"b*"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|names
operator|.
name|contains
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSmartName
specifier|public
name|void
name|testSmartName
parameter_list|()
block|{
name|FakeFieldMapper
name|f1
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"realfoo"
argument_list|)
decl_stmt|;
name|FakeFieldMapper
name|f2
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"realbar"
argument_list|)
decl_stmt|;
name|FakeFieldMapper
name|f3
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"baz"
argument_list|,
literal|"realfoo"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f1
argument_list|,
name|f2
argument_list|,
name|f3
argument_list|)
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|lookup
operator|.
name|smartName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|lookup
operator|.
name|smartName
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|mappers
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|lookup
operator|.
name|smartName
argument_list|(
literal|"realfoo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f1
argument_list|,
name|lookup
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f2
argument_list|,
name|lookup
operator|.
name|smartNameFieldMapper
argument_list|(
literal|"realbar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIteratorImmutable
specifier|public
name|void
name|testIteratorImmutable
parameter_list|()
block|{
name|FakeFieldMapper
name|f1
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f1
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Iterator
argument_list|<
name|FieldMapper
argument_list|<
name|?
argument_list|>
argument_list|>
name|itr
init|=
name|lookup
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|itr
operator|.
name|hasNext
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f1
argument_list|,
name|itr
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
name|itr
operator|.
name|remove
argument_list|()
expr_stmt|;
name|fail
argument_list|(
literal|"remove should have failed"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
DECL|method|testGetMapper
specifier|public
name|void
name|testGetMapper
parameter_list|()
block|{
name|FakeFieldMapper
name|f1
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|FieldMappersLookup
name|lookup
init|=
operator|new
name|FieldMappersLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f1
argument_list|,
name|lookup
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|get
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
comment|// get is only by full name
name|FakeFieldMapper
name|f2
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|Lists
operator|.
name|newArrayList
argument_list|(
name|f2
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|lookup
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"get should have enforced foo is unique"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
comment|// this sucks how much must be overriden just do get a dummy field mapper...
DECL|class|FakeFieldMapper
specifier|static
class|class
name|FakeFieldMapper
extends|extends
name|AbstractFieldMapper
argument_list|<
name|String
argument_list|>
block|{
DECL|field|dummySettings
specifier|static
name|Settings
name|dummySettings
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
name|CURRENT
operator|.
name|id
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
DECL|method|FakeFieldMapper
specifier|public
name|FakeFieldMapper
parameter_list|(
name|String
name|fullName
parameter_list|,
name|String
name|indexName
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|Names
argument_list|(
name|fullName
argument_list|,
name|indexName
argument_list|,
name|indexName
argument_list|,
name|fullName
argument_list|)
argument_list|,
literal|1.0f
argument_list|,
name|AbstractFieldMapper
operator|.
name|Defaults
operator|.
name|FIELD_TYPE
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|dummySettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldType
specifier|public
name|FieldType
name|defaultFieldType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|defaultFieldDataType
specifier|public
name|FieldDataType
name|defaultFieldDataType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|contentType
specifier|protected
name|String
name|contentType
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
DECL|method|parseCreateField
specifier|protected
name|void
name|parseCreateField
parameter_list|(
name|ParseContext
name|context
parameter_list|,
name|List
name|list
parameter_list|)
throws|throws
name|IOException
block|{}
annotation|@
name|Override
DECL|method|value
specifier|public
name|String
name|value
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

