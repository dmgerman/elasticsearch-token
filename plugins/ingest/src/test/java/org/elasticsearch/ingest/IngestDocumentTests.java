begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.ingest
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|ingest
package|;
end_package

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
name|junit
operator|.
name|Before
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
name|HashMap
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
name|hamcrest
operator|.
name|Matchers
operator|.
name|*
import|;
end_import

begin_class
DECL|class|IngestDocumentTests
specifier|public
class|class
name|IngestDocumentTests
extends|extends
name|ESTestCase
block|{
DECL|field|ingestDocument
specifier|private
name|IngestDocument
name|ingestDocument
decl_stmt|;
annotation|@
name|Before
DECL|method|setIngestDocument
specifier|public
name|void
name|setIngestDocument
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"foo"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"int"
argument_list|,
literal|123
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|innerObject
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|innerObject
operator|.
name|put
argument_list|(
literal|"buzz"
argument_list|,
literal|"hello world"
argument_list|)
expr_stmt|;
name|innerObject
operator|.
name|put
argument_list|(
literal|"foo_null"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|document
operator|.
name|put
argument_list|(
literal|"fizz"
argument_list|,
name|innerObject
argument_list|)
expr_stmt|;
name|ingestDocument
operator|=
operator|new
name|IngestDocument
argument_list|(
literal|"index"
argument_list|,
literal|"type"
argument_list|,
literal|"id"
argument_list|,
name|document
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleGetFieldValue
specifier|public
name|void
name|testSimpleGetFieldValue
parameter_list|()
block|{
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"int"
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|123
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetFieldValueNullValue
specifier|public
name|void
name|testGetFieldValueNullValue
parameter_list|()
block|{
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"fizz.foo_null"
argument_list|,
name|Object
operator|.
name|class
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleGetFieldValueTypeMismatch
specifier|public
name|void
name|testSimpleGetFieldValueTypeMismatch
parameter_list|()
block|{
try|try
block|{
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"int"
argument_list|,
name|String
operator|.
name|class
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"getFieldValue should have failed"
argument_list|)
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
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field [int] of type [java.lang.Integer] cannot be cast to [java.lang.String]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"foo"
argument_list|,
name|Integer
operator|.
name|class
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"getFieldValue should have failed"
argument_list|)
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
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"field [foo] of type [java.lang.String] cannot be cast to [java.lang.Integer]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testNestedGetFieldValue
specifier|public
name|void
name|testNestedGetFieldValue
parameter_list|()
block|{
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"fizz.buzz"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"hello world"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetFieldValueNotFound
specifier|public
name|void
name|testGetFieldValueNotFound
parameter_list|()
block|{
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|"not.here"
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetFieldValueNull
specifier|public
name|void
name|testGetFieldValueNull
parameter_list|()
block|{
name|assertNull
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|null
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testGetFieldValueEmpty
specifier|public
name|void
name|testGetFieldValueEmpty
parameter_list|()
block|{
name|assertNull
argument_list|(
name|ingestDocument
operator|.
name|getFieldValue
argument_list|(
literal|""
argument_list|,
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasFieldValue
specifier|public
name|void
name|testHasFieldValue
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|ingestDocument
operator|.
name|hasFieldValue
argument_list|(
literal|"fizz"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasFieldValueNested
specifier|public
name|void
name|testHasFieldValueNested
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|ingestDocument
operator|.
name|hasFieldValue
argument_list|(
literal|"fizz.buzz"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasFieldValueNotFound
specifier|public
name|void
name|testHasFieldValueNotFound
parameter_list|()
block|{
name|assertFalse
argument_list|(
name|ingestDocument
operator|.
name|hasFieldValue
argument_list|(
literal|"doesnotexist"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasFieldValueNestedNotFound
specifier|public
name|void
name|testHasFieldValueNestedNotFound
parameter_list|()
block|{
name|assertFalse
argument_list|(
name|ingestDocument
operator|.
name|hasFieldValue
argument_list|(
literal|"fizz.doesnotexist"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasFieldValueNull
specifier|public
name|void
name|testHasFieldValueNull
parameter_list|()
block|{
name|assertFalse
argument_list|(
name|ingestDocument
operator|.
name|hasFieldValue
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasFieldValueNullValue
specifier|public
name|void
name|testHasFieldValueNullValue
parameter_list|()
block|{
name|assertTrue
argument_list|(
name|ingestDocument
operator|.
name|hasFieldValue
argument_list|(
literal|"fizz.foo_null"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testHasFieldValueEmpty
specifier|public
name|void
name|testHasFieldValueEmpty
parameter_list|()
block|{
name|assertFalse
argument_list|(
name|ingestDocument
operator|.
name|hasFieldValue
argument_list|(
literal|""
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSimpleSetFieldValue
specifier|public
name|void
name|testSimpleSetFieldValue
parameter_list|()
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"new_field"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"new_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSetFieldValueNullValue
specifier|public
name|void
name|testSetFieldValueNullValue
parameter_list|()
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"new_field"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"new_field"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"new_field"
argument_list|)
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testNestedSetFieldValue
specifier|public
name|void
name|testNestedSetFieldValue
parameter_list|()
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"a.b.c.d"
argument_list|,
literal|"foo"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|a
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|a
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|b
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|a
operator|.
name|get
argument_list|(
literal|"b"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|b
operator|.
name|get
argument_list|(
literal|"c"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|c
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|b
operator|.
name|get
argument_list|(
literal|"c"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|c
operator|.
name|get
argument_list|(
literal|"d"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|d
init|=
operator|(
name|String
operator|)
name|c
operator|.
name|get
argument_list|(
literal|"d"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|d
argument_list|,
name|equalTo
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSetFieldValueOnExistingField
specifier|public
name|void
name|testSetFieldValueOnExistingField
parameter_list|()
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"foo"
argument_list|,
literal|"newbar"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"newbar"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|testSetFieldValueOnExistingParent
specifier|public
name|void
name|testSetFieldValueOnExistingParent
parameter_list|()
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"fizz.new"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"fizz"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|innerMap
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"fizz"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|innerMap
operator|.
name|get
argument_list|(
literal|"new"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|String
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|value
init|=
operator|(
name|String
operator|)
name|innerMap
operator|.
name|get
argument_list|(
literal|"new"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|value
argument_list|,
name|equalTo
argument_list|(
literal|"bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSetFieldValueOnExistingParentTypeMismatch
specifier|public
name|void
name|testSetFieldValueOnExistingParentTypeMismatch
parameter_list|()
block|{
try|try
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"fizz.buzz.new"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"add field should have failed"
argument_list|)
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
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cannot add field to parent [buzz] of type [java.lang.String], [java.util.Map] expected instead."
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetFieldValueOnExistingNullParent
specifier|public
name|void
name|testSetFieldValueOnExistingNullParent
parameter_list|()
block|{
try|try
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|"fizz.foo_null.test"
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"add field should have failed"
argument_list|)
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
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cannot add field to null parent, [java.util.Map] expected instead."
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetFieldValueNullName
specifier|public
name|void
name|testSetFieldValueNullName
parameter_list|()
block|{
try|try
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|null
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"add field should have failed"
argument_list|)
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
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cannot add null or empty field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSetFieldValueEmptyName
specifier|public
name|void
name|testSetFieldValueEmptyName
parameter_list|()
block|{
try|try
block|{
name|ingestDocument
operator|.
name|setFieldValue
argument_list|(
literal|""
argument_list|,
literal|"bar"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"add field should have failed"
argument_list|)
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
name|getMessage
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"cannot add null or empty field"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testRemoveField
specifier|public
name|void
name|testRemoveField
parameter_list|()
block|{
name|ingestDocument
operator|.
name|removeField
argument_list|(
literal|"foo"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
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
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"foo"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemoveInnerField
specifier|public
name|void
name|testRemoveInnerField
parameter_list|()
block|{
name|ingestDocument
operator|.
name|removeField
argument_list|(
literal|"fizz.buzz"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"fizz"
argument_list|)
argument_list|,
name|instanceOf
argument_list|(
name|Map
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
operator|)
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|get
argument_list|(
literal|"fizz"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|map
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
name|map
operator|.
name|containsKey
argument_list|(
literal|"buzz"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|ingestDocument
operator|.
name|removeField
argument_list|(
literal|"fizz.foo_null"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|map
operator|.
name|size
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
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|containsKey
argument_list|(
literal|"fizz"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemoveNonExistingField
specifier|public
name|void
name|testRemoveNonExistingField
parameter_list|()
block|{
name|ingestDocument
operator|.
name|removeField
argument_list|(
literal|"does_not_exist"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
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
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemoveExistingParentTypeMismatch
specifier|public
name|void
name|testRemoveExistingParentTypeMismatch
parameter_list|()
block|{
name|ingestDocument
operator|.
name|removeField
argument_list|(
literal|"foo.test"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
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
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemoveNullField
specifier|public
name|void
name|testRemoveNullField
parameter_list|()
block|{
name|ingestDocument
operator|.
name|removeField
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
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
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testRemoveEmptyField
specifier|public
name|void
name|testRemoveEmptyField
parameter_list|()
block|{
name|ingestDocument
operator|.
name|removeField
argument_list|(
literal|""
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
operator|.
name|isSourceModified
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
name|ingestDocument
operator|.
name|getSource
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testEqualsAndHashcode
specifier|public
name|void
name|testEqualsAndHashcode
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|index
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|id
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|fieldName
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|fieldValue
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|IngestDocument
name|ingestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|changed
init|=
literal|false
decl_stmt|;
name|String
name|otherIndex
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|otherIndex
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|otherIndex
operator|=
name|index
expr_stmt|;
block|}
name|String
name|otherType
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|otherType
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|otherType
operator|=
name|type
expr_stmt|;
block|}
name|String
name|otherId
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|otherId
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|otherId
operator|=
name|id
expr_stmt|;
block|}
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|document
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|document
operator|=
name|Collections
operator|.
name|singletonMap
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|changed
operator|=
literal|true
expr_stmt|;
block|}
else|else
block|{
name|document
operator|=
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
expr_stmt|;
block|}
name|IngestDocument
name|otherIngestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|otherIndex
argument_list|,
name|otherType
argument_list|,
name|otherId
argument_list|,
name|document
argument_list|)
decl_stmt|;
if|if
condition|(
name|changed
condition|)
block|{
name|assertThat
argument_list|(
name|ingestDocument
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|otherIngestDocument
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|otherIngestDocument
argument_list|,
name|not
argument_list|(
name|equalTo
argument_list|(
name|ingestDocument
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|ingestDocument
argument_list|,
name|equalTo
argument_list|(
name|otherIngestDocument
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|otherIngestDocument
argument_list|,
name|equalTo
argument_list|(
name|ingestDocument
argument_list|)
argument_list|)
expr_stmt|;
name|IngestDocument
name|thirdIngestDocument
init|=
operator|new
name|IngestDocument
argument_list|(
name|index
argument_list|,
name|type
argument_list|,
name|id
argument_list|,
name|Collections
operator|.
name|singletonMap
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|thirdIngestDocument
argument_list|,
name|equalTo
argument_list|(
name|ingestDocument
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ingestDocument
argument_list|,
name|equalTo
argument_list|(
name|thirdIngestDocument
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

