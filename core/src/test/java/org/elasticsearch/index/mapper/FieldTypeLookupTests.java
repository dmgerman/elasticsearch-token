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
name|Settings
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
name|Arrays
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
name|Collections
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
DECL|class|FieldTypeLookupTests
specifier|public
class|class
name|FieldTypeLookupTests
extends|extends
name|ESTestCase
block|{
DECL|method|testEmpty
specifier|public
name|void
name|testEmpty
parameter_list|()
block|{
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
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
name|assertNull
argument_list|(
name|lookup
operator|.
name|getByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup
operator|.
name|getTypes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup
operator|.
name|getTypesByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
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
name|simpleMatchToIndexNames
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
name|Iterator
argument_list|<
name|MappedFieldType
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
DECL|method|testDefaultMapping
specifier|public
name|void
name|testDefaultMapping
parameter_list|()
block|{
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
try|try
block|{
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
name|Collections
operator|.
name|emptyList
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|expected
parameter_list|)
block|{
name|assertEquals
argument_list|(
literal|"Default mappings should not be added to the lookup"
argument_list|,
name|expected
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testAddNewField
specifier|public
name|void
name|testAddNewField
parameter_list|()
block|{
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
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
name|FieldTypeLookup
name|lookup2
init|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f
argument_list|)
argument_list|)
decl_stmt|;
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
name|assertNull
argument_list|(
name|lookup
operator|.
name|getByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|getByIndexName
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
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
name|assertEquals
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|getByIndexName
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|lookup
operator|.
name|getByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup
operator|.
name|getTypes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup
operator|.
name|getTypesByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup
operator|.
name|getTypes
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup
operator|.
name|getTypesByIndexName
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|lookup2
operator|.
name|getTypes
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup2
operator|.
name|getTypesByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|lookup2
operator|.
name|getTypes
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
literal|"type"
argument_list|)
argument_list|,
name|lookup2
operator|.
name|getTypesByIndexName
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
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
DECL|method|testAddExistingField
specifier|public
name|void
name|testAddExistingField
parameter_list|()
block|{
name|FakeFieldMapper
name|f
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|MappedFieldType
name|originalFieldType
init|=
name|f
operator|.
name|fieldType
argument_list|()
decl_stmt|;
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
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type1"
argument_list|,
name|newList
argument_list|(
name|f
argument_list|)
argument_list|)
expr_stmt|;
name|FieldTypeLookup
name|lookup2
init|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|originalFieldType
argument_list|,
name|f
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|f2
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|getByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
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
DECL|method|testAddExistingIndexName
specifier|public
name|void
name|testAddExistingIndexName
parameter_list|()
block|{
name|FakeFieldMapper
name|f
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
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
literal|"foo"
argument_list|)
decl_stmt|;
name|MappedFieldType
name|originalFieldType
init|=
name|f
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type1"
argument_list|,
name|newList
argument_list|(
name|f
argument_list|)
argument_list|)
expr_stmt|;
name|FieldTypeLookup
name|lookup2
init|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|originalFieldType
argument_list|,
name|f
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|f2
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|get
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|getByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
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
DECL|method|testAddExistingFullName
specifier|public
name|void
name|testAddExistingFullName
parameter_list|()
block|{
name|FakeFieldMapper
name|f
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
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
literal|"bar"
argument_list|)
decl_stmt|;
name|MappedFieldType
name|originalFieldType
init|=
name|f
operator|.
name|fieldType
argument_list|()
decl_stmt|;
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type1"
argument_list|,
name|newList
argument_list|(
name|f
argument_list|)
argument_list|)
expr_stmt|;
name|FieldTypeLookup
name|lookup2
init|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|)
decl_stmt|;
name|assertNotSame
argument_list|(
name|originalFieldType
argument_list|,
name|f
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|f2
operator|.
name|fieldType
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|getByIndexName
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|f
operator|.
name|fieldType
argument_list|()
argument_list|,
name|lookup2
operator|.
name|getByIndexName
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
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
DECL|method|testAddExistingBridgeName
specifier|public
name|void
name|testAddExistingBridgeName
parameter_list|()
block|{
name|FakeFieldMapper
name|f
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
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
literal|"bar"
argument_list|)
decl_stmt|;
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type1"
argument_list|,
name|newList
argument_list|(
name|f
argument_list|,
name|f2
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|FakeFieldMapper
name|f3
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f3
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"insane mappings"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|FakeFieldMapper
name|f3
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"bar"
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f3
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalStateException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"insane mappings"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCheckCompatibilityNewField
specifier|public
name|void
name|testCheckCompatibilityNewField
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
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f1
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
DECL|method|testCheckCompatibilityMismatchedTypes
specifier|public
name|void
name|testCheckCompatibilityMismatchedTypes
parameter_list|()
block|{
name|FieldMapper
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
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f1
argument_list|)
argument_list|)
expr_stmt|;
name|MappedFieldType
name|ft2
init|=
name|FakeFieldMapper
operator|.
name|makeOtherFieldType
argument_list|(
literal|"foo"
argument_list|,
literal|"foo"
argument_list|)
decl_stmt|;
name|FieldMapper
name|f2
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
name|ft2
argument_list|)
decl_stmt|;
try|try
block|{
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected type mismatch"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"cannot be changed from type [faketype] to [otherfaketype]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// fails even if updateAllTypes == true
try|try
block|{
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected type mismatch"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"cannot be changed from type [faketype] to [otherfaketype]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testCheckCompatibilityConflict
specifier|public
name|void
name|testCheckCompatibilityConflict
parameter_list|()
block|{
name|FieldMapper
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
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f1
argument_list|)
argument_list|)
expr_stmt|;
name|MappedFieldType
name|ft2
init|=
name|FakeFieldMapper
operator|.
name|makeFieldType
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|ft2
operator|.
name|setBoost
argument_list|(
literal|2.0f
argument_list|)
expr_stmt|;
name|FieldMapper
name|f2
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
name|ft2
argument_list|)
decl_stmt|;
try|try
block|{
comment|// different type
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected conflict"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"to update [boost] across all types"
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// boost is updateable, so ok since we are implicitly updating all types
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f2
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// boost is updateable, so ok if forcing
comment|// now with a non changeable setting
name|MappedFieldType
name|ft3
init|=
name|FakeFieldMapper
operator|.
name|makeFieldType
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
decl_stmt|;
name|ft3
operator|.
name|setStored
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|FieldMapper
name|f3
init|=
operator|new
name|FakeFieldMapper
argument_list|(
literal|"foo"
argument_list|,
name|ft3
argument_list|)
decl_stmt|;
try|try
block|{
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f3
argument_list|)
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected conflict"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"has different [store] values"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// even with updateAllTypes == true, incompatible
try|try
block|{
name|lookup
operator|.
name|checkCompatibility
argument_list|(
literal|"type2"
argument_list|,
name|newList
argument_list|(
name|f3
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"expected conflict"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"has different [store] values"
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f1
argument_list|,
name|f2
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
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
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f1
argument_list|,
name|f2
argument_list|)
argument_list|)
expr_stmt|;
name|Collection
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
name|FieldTypeLookup
name|lookup
init|=
operator|new
name|FieldTypeLookup
argument_list|()
decl_stmt|;
name|lookup
operator|=
name|lookup
operator|.
name|copyAndAddAll
argument_list|(
literal|"type"
argument_list|,
name|newList
argument_list|(
name|f1
argument_list|)
argument_list|)
expr_stmt|;
try|try
block|{
name|Iterator
argument_list|<
name|MappedFieldType
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
operator|.
name|fieldType
argument_list|()
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
DECL|method|newList
specifier|static
name|List
argument_list|<
name|FieldMapper
argument_list|>
name|newList
parameter_list|(
name|FieldMapper
modifier|...
name|mapper
parameter_list|)
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
name|mapper
argument_list|)
return|;
block|}
comment|// this sucks how much must be overridden just do get a dummy field mapper...
DECL|class|FakeFieldMapper
specifier|static
class|class
name|FakeFieldMapper
extends|extends
name|FieldMapper
block|{
DECL|field|dummySettings
specifier|static
name|Settings
name|dummySettings
init|=
name|Settings
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
name|fullName
argument_list|,
name|makeFieldType
argument_list|(
name|fullName
argument_list|,
name|indexName
argument_list|)
argument_list|,
name|makeFieldType
argument_list|(
name|fullName
argument_list|,
name|indexName
argument_list|)
argument_list|,
name|dummySettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|FakeFieldMapper
specifier|public
name|FakeFieldMapper
parameter_list|(
name|String
name|fullName
parameter_list|,
name|MappedFieldType
name|fieldType
parameter_list|)
block|{
name|super
argument_list|(
name|fullName
argument_list|,
name|fieldType
argument_list|,
name|fieldType
argument_list|,
name|dummySettings
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
DECL|method|makeFieldType
specifier|static
name|MappedFieldType
name|makeFieldType
parameter_list|(
name|String
name|fullName
parameter_list|,
name|String
name|indexName
parameter_list|)
block|{
name|FakeFieldType
name|fieldType
init|=
operator|new
name|FakeFieldType
argument_list|()
decl_stmt|;
name|fieldType
operator|.
name|setNames
argument_list|(
operator|new
name|MappedFieldType
operator|.
name|Names
argument_list|(
name|indexName
argument_list|,
name|indexName
argument_list|,
name|fullName
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|fieldType
return|;
block|}
DECL|method|makeOtherFieldType
specifier|static
name|MappedFieldType
name|makeOtherFieldType
parameter_list|(
name|String
name|fullName
parameter_list|,
name|String
name|indexName
parameter_list|)
block|{
name|OtherFakeFieldType
name|fieldType
init|=
operator|new
name|OtherFakeFieldType
argument_list|()
decl_stmt|;
name|fieldType
operator|.
name|setNames
argument_list|(
operator|new
name|MappedFieldType
operator|.
name|Names
argument_list|(
name|indexName
argument_list|,
name|indexName
argument_list|,
name|fullName
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|fieldType
return|;
block|}
DECL|class|FakeFieldType
specifier|static
class|class
name|FakeFieldType
extends|extends
name|MappedFieldType
block|{
DECL|method|FakeFieldType
specifier|public
name|FakeFieldType
parameter_list|()
block|{}
DECL|method|FakeFieldType
specifier|protected
name|FakeFieldType
parameter_list|(
name|FakeFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MappedFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|FakeFieldType
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|typeName
specifier|public
name|String
name|typeName
parameter_list|()
block|{
return|return
literal|"faketype"
return|;
block|}
block|}
DECL|class|OtherFakeFieldType
specifier|static
class|class
name|OtherFakeFieldType
extends|extends
name|MappedFieldType
block|{
DECL|method|OtherFakeFieldType
specifier|public
name|OtherFakeFieldType
parameter_list|()
block|{}
DECL|method|OtherFakeFieldType
specifier|protected
name|OtherFakeFieldType
parameter_list|(
name|OtherFakeFieldType
name|ref
parameter_list|)
block|{
name|super
argument_list|(
name|ref
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MappedFieldType
name|clone
parameter_list|()
block|{
return|return
operator|new
name|OtherFakeFieldType
argument_list|(
name|this
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|typeName
specifier|public
name|String
name|typeName
parameter_list|()
block|{
return|return
literal|"otherfaketype"
return|;
block|}
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
block|}
DECL|method|size
specifier|private
name|int
name|size
parameter_list|(
name|Iterator
argument_list|<
name|MappedFieldType
argument_list|>
name|iterator
parameter_list|)
block|{
if|if
condition|(
name|iterator
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"iterator"
argument_list|)
throw|;
block|}
name|int
name|count
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|count
operator|++
expr_stmt|;
name|iterator
operator|.
name|next
argument_list|()
expr_stmt|;
block|}
return|return
name|count
return|;
block|}
block|}
end_class

end_unit

