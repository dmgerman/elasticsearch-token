begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.test.unit.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|unit
operator|.
name|index
operator|.
name|fielddata
package|;
end_package

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|iterator
operator|.
name|TLongIterator
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|TDoubleSet
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|TLongSet
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|hash
operator|.
name|TDoubleHashSet
import|;
end_import

begin_import
import|import
name|gnu
operator|.
name|trove
operator|.
name|set
operator|.
name|hash
operator|.
name|TLongHashSet
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
name|Document
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
name|Field
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
name|LongField
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
name|StringField
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
name|index
operator|.
name|fielddata
operator|.
name|*
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
name|plain
operator|.
name|PackedArrayAtomicFieldData
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
name|testng
operator|.
name|annotations
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
name|ArrayList
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
name|Random
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|assertThat
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
name|instanceOf
import|;
end_import

begin_comment
comment|/**  * Tests for all integer types (byte, short, int, long).  */
end_comment

begin_class
DECL|class|LongFieldDataTests
specifier|public
class|class
name|LongFieldDataTests
extends|extends
name|NumericFieldDataTests
block|{
annotation|@
name|Override
DECL|method|getFieldDataType
specifier|protected
name|FieldDataType
name|getFieldDataType
parameter_list|()
block|{
comment|// we don't want to optimize the type so it will always be a long...
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"long"
argument_list|,
name|ImmutableSettings
operator|.
name|builder
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Test
DECL|method|testOptimizeTypeLong
specifier|public
name|void
name|testOptimizeTypeLong
parameter_list|()
throws|throws
name|Exception
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
operator|+
literal|1l
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
name|Integer
operator|.
name|MIN_VALUE
operator|-
literal|1l
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|IndexNumericFieldData
name|indexFieldData
init|=
name|ifdService
operator|.
name|getForField
argument_list|(
operator|new
name|FieldMapper
operator|.
name|Names
argument_list|(
literal|"value"
argument_list|)
argument_list|,
operator|new
name|FieldDataType
argument_list|(
literal|"long"
argument_list|)
argument_list|)
decl_stmt|;
name|AtomicNumericFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|refreshReader
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|fieldData
argument_list|,
name|instanceOf
argument_list|(
name|PackedArrayAtomicFieldData
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldData
operator|.
name|getLongValues
argument_list|()
operator|.
name|getValue
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|Integer
operator|.
name|MAX_VALUE
operator|+
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|fieldData
operator|.
name|getLongValues
argument_list|()
operator|.
name|getValue
argument_list|(
literal|1
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|(
name|long
operator|)
name|Integer
operator|.
name|MIN_VALUE
operator|-
literal|1l
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testDateScripts
specifier|public
name|void
name|testDateScripts
parameter_list|()
throws|throws
name|Exception
block|{
name|fillSingleValueAllSet
argument_list|()
expr_stmt|;
name|IndexNumericFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
name|AtomicNumericFieldData
name|fieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|refreshReader
argument_list|()
argument_list|)
decl_stmt|;
name|ScriptDocValues
operator|.
name|Longs
name|scriptValues
init|=
operator|(
name|ScriptDocValues
operator|.
name|Longs
operator|)
name|fieldData
operator|.
name|getScriptValues
argument_list|()
decl_stmt|;
name|scriptValues
operator|.
name|setNextDocId
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|scriptValues
operator|.
name|getValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|scriptValues
operator|.
name|getDate
argument_list|()
operator|.
name|getMillis
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2l
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|scriptValues
operator|.
name|getDate
argument_list|()
operator|.
name|getZone
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|DateTimeZone
operator|.
name|UTC
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fillSingleValueAllSet
specifier|protected
name|void
name|fillSingleValueAllSet
parameter_list|()
throws|throws
name|Exception
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|2
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"3"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|3
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fillSingleValueWithMissing
specifier|protected
name|void
name|fillSingleValueWithMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|2
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
comment|//d.add(new StringField("value", one(), Field.Store.NO)); // MISSING....
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"3"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|3
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fillMultiValueAllSet
specifier|protected
name|void
name|fillMultiValueAllSet
parameter_list|()
throws|throws
name|Exception
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|2
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|4
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|1
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"3"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|3
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|fillMultiValueWithMissing
specifier|protected
name|void
name|fillMultiValueWithMissing
parameter_list|()
throws|throws
name|Exception
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|2
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|4
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
comment|//d.add(new StringField("value", one(), Field.Store.NO)); // MISSING
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"3"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|3
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
DECL|method|fillExtendedMvSet
specifier|protected
name|void
name|fillExtendedMvSet
parameter_list|()
throws|throws
name|Exception
block|{
name|Document
name|d
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"1"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|2
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|4
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"2"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"3"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|3
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"4"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|4
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|5
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|6
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"5"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|6
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|7
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|8
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"6"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"7"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|8
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|9
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
literal|10
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
name|writer
operator|.
name|commit
argument_list|()
expr_stmt|;
name|d
operator|=
operator|new
name|Document
argument_list|()
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|"8"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
operator|-
literal|8
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
operator|-
literal|9
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
operator|-
literal|10
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|writer
operator|.
name|addDocument
argument_list|(
name|d
argument_list|)
expr_stmt|;
block|}
DECL|field|SECONDS_PER_YEAR
specifier|private
specifier|static
specifier|final
name|int
name|SECONDS_PER_YEAR
init|=
literal|60
operator|*
literal|60
operator|*
literal|24
operator|*
literal|365
decl_stmt|;
comment|// TODO: use random() when migrating to Junit
DECL|enum|Data
specifier|public
specifier|static
enum|enum
name|Data
block|{
DECL|enum constant|SINGLE_VALUED_DENSE_ENUM
name|SINGLE_VALUED_DENSE_ENUM
block|{
specifier|public
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|16
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|SINGLE_VALUED_DENSE_DATE
name|SINGLE_VALUED_DENSE_DATE
block|{
specifier|public
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
literal|1
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
comment|// somewhere in-between 2010 and 2012
return|return
literal|1000L
operator|*
operator|(
literal|40L
operator|*
name|SECONDS_PER_YEAR
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|2
operator|*
name|SECONDS_PER_YEAR
argument_list|)
operator|)
return|;
block|}
block|}
block|,
DECL|enum constant|MULTI_VALUED_DATE
name|MULTI_VALUED_DATE
block|{
specifier|public
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
comment|// somewhere in-between 2010 and 2012
return|return
literal|1000L
operator|*
operator|(
literal|40L
operator|*
name|SECONDS_PER_YEAR
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|2
operator|*
name|SECONDS_PER_YEAR
argument_list|)
operator|)
return|;
block|}
block|}
block|,
DECL|enum constant|MULTI_VALUED_ENUM
name|MULTI_VALUED_ENUM
block|{
specifier|public
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
literal|3
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|8
argument_list|)
return|;
block|}
block|}
block|,
DECL|enum constant|SINGLE_VALUED_SPARSE_RANDOM
name|SINGLE_VALUED_SPARSE_RANDOM
block|{
specifier|public
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|nextFloat
argument_list|()
operator|<
literal|0.1f
condition|?
literal|1
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|nextLong
argument_list|()
return|;
block|}
block|}
block|,
DECL|enum constant|MULTI_VALUED_SPARSE_RANDOM
name|MULTI_VALUED_SPARSE_RANDOM
block|{
specifier|public
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|nextFloat
argument_list|()
operator|<
literal|0.1f
condition|?
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|5
argument_list|)
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|nextLong
argument_list|()
return|;
block|}
block|}
block|,
DECL|enum constant|MULTI_VALUED_DENSE_RANDOM
name|MULTI_VALUED_DENSE_RANDOM
block|{
specifier|public
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
literal|1
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|3
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|nextLong
argument_list|()
return|;
block|}
block|}
block|;
DECL|method|numValues
specifier|public
specifier|abstract
name|int
name|numValues
parameter_list|(
name|Random
name|r
parameter_list|)
function_decl|;
DECL|method|nextValue
specifier|public
specifier|abstract
name|long
name|nextValue
parameter_list|(
name|Random
name|r
parameter_list|)
function_decl|;
block|}
DECL|method|test
specifier|private
name|void
name|test
parameter_list|(
name|List
argument_list|<
name|TLongSet
argument_list|>
name|values
parameter_list|)
throws|throws
name|Exception
block|{
name|StringField
name|id
init|=
operator|new
name|StringField
argument_list|(
literal|"_id"
argument_list|,
literal|""
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
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
name|values
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
name|Document
name|doc
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|id
operator|.
name|setStringValue
argument_list|(
literal|""
operator|+
name|i
argument_list|)
expr_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|id
argument_list|)
expr_stmt|;
specifier|final
name|TLongSet
name|v
init|=
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
for|for
control|(
name|TLongIterator
name|it
init|=
name|v
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|LongField
name|value
init|=
operator|new
name|LongField
argument_list|(
literal|"value"
argument_list|,
name|it
operator|.
name|next
argument_list|()
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
decl_stmt|;
name|doc
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|addDocument
argument_list|(
name|doc
argument_list|)
expr_stmt|;
block|}
name|writer
operator|.
name|forceMerge
argument_list|(
literal|1
argument_list|)
expr_stmt|;
specifier|final
name|IndexNumericFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
literal|"value"
argument_list|)
decl_stmt|;
specifier|final
name|AtomicNumericFieldData
name|atomicFieldData
init|=
name|indexFieldData
operator|.
name|load
argument_list|(
name|refreshReader
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|LongValues
name|data
init|=
name|atomicFieldData
operator|.
name|getLongValues
argument_list|()
decl_stmt|;
specifier|final
name|DoubleValues
name|doubleData
init|=
name|atomicFieldData
operator|.
name|getDoubleValues
argument_list|()
decl_stmt|;
specifier|final
name|TLongSet
name|set
init|=
operator|new
name|TLongHashSet
argument_list|()
decl_stmt|;
specifier|final
name|TDoubleSet
name|doubleSet
init|=
operator|new
name|TDoubleHashSet
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
name|values
operator|.
name|size
argument_list|()
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|TLongSet
name|v
init|=
name|values
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|data
operator|.
name|hasValue
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|!
name|v
operator|.
name|isEmpty
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doubleData
operator|.
name|hasValue
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
operator|!
name|v
operator|.
name|isEmpty
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|v
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|assertThat
argument_list|(
name|data
operator|.
name|getValue
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|doubleData
operator|.
name|getValue
argument_list|(
name|i
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|0d
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|set
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|LongValues
operator|.
name|Iter
name|iter
init|=
name|data
operator|.
name|getIter
argument_list|(
name|i
argument_list|)
init|;
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|set
operator|.
name|add
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|set
argument_list|,
name|equalTo
argument_list|(
name|v
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|TDoubleSet
name|doubleV
init|=
operator|new
name|TDoubleHashSet
argument_list|()
decl_stmt|;
for|for
control|(
name|TLongIterator
name|it
init|=
name|v
operator|.
name|iterator
argument_list|()
init|;
name|it
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|doubleV
operator|.
name|add
argument_list|(
operator|(
name|double
operator|)
name|it
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|doubleSet
operator|.
name|clear
argument_list|()
expr_stmt|;
for|for
control|(
name|DoubleValues
operator|.
name|Iter
name|iter
init|=
name|doubleData
operator|.
name|getIter
argument_list|(
name|i
argument_list|)
init|;
name|iter
operator|.
name|hasNext
argument_list|()
condition|;
control|)
block|{
name|doubleSet
operator|.
name|add
argument_list|(
name|iter
operator|.
name|next
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|doubleSet
argument_list|,
name|equalTo
argument_list|(
name|doubleV
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|test
specifier|private
name|void
name|test
parameter_list|(
name|Data
name|data
parameter_list|)
throws|throws
name|Exception
block|{
name|Random
name|r
init|=
operator|new
name|Random
argument_list|(
name|data
operator|.
name|ordinal
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|int
name|numDocs
init|=
literal|1000
operator|+
name|r
operator|.
name|nextInt
argument_list|(
literal|19000
argument_list|)
decl_stmt|;
specifier|final
name|List
argument_list|<
name|TLongSet
argument_list|>
name|values
init|=
operator|new
name|ArrayList
argument_list|<
name|TLongSet
argument_list|>
argument_list|(
name|numDocs
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
name|numDocs
condition|;
operator|++
name|i
control|)
block|{
specifier|final
name|int
name|numValues
init|=
name|data
operator|.
name|numValues
argument_list|(
name|r
argument_list|)
decl_stmt|;
specifier|final
name|TLongSet
name|vals
init|=
operator|new
name|TLongHashSet
argument_list|(
name|numValues
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
name|numValues
condition|;
operator|++
name|j
control|)
block|{
name|vals
operator|.
name|add
argument_list|(
name|data
operator|.
name|nextValue
argument_list|(
name|r
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|values
operator|.
name|add
argument_list|(
name|vals
argument_list|)
expr_stmt|;
block|}
name|test
argument_list|(
name|values
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValuedDenseEnum
specifier|public
name|void
name|testSingleValuedDenseEnum
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
name|Data
operator|.
name|SINGLE_VALUED_DENSE_ENUM
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValuedDenseDate
specifier|public
name|void
name|testSingleValuedDenseDate
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
name|Data
operator|.
name|SINGLE_VALUED_DENSE_DATE
argument_list|)
expr_stmt|;
block|}
DECL|method|testSingleValuedSparseRandom
specifier|public
name|void
name|testSingleValuedSparseRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
name|Data
operator|.
name|SINGLE_VALUED_SPARSE_RANDOM
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiValuedDate
specifier|public
name|void
name|testMultiValuedDate
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
name|Data
operator|.
name|MULTI_VALUED_DATE
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiValuedEnum
specifier|public
name|void
name|testMultiValuedEnum
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
name|Data
operator|.
name|MULTI_VALUED_ENUM
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiValuedSparseRandom
specifier|public
name|void
name|testMultiValuedSparseRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
name|Data
operator|.
name|MULTI_VALUED_SPARSE_RANDOM
argument_list|)
expr_stmt|;
block|}
DECL|method|testMultiValuedDenseRandom
specifier|public
name|void
name|testMultiValuedDenseRandom
parameter_list|()
throws|throws
name|Exception
block|{
name|test
argument_list|(
name|Data
operator|.
name|MULTI_VALUED_DENSE_RANDOM
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

