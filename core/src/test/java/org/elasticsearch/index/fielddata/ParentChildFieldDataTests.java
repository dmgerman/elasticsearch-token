begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.fielddata
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|fielddata
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
name|SortedDocValuesField
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
name|apache
operator|.
name|lucene
operator|.
name|index
operator|.
name|DirectoryReader
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
name|LeafReaderContext
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
name|SortedDocValues
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
name|search
operator|.
name|FieldDoc
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
name|search
operator|.
name|IndexSearcher
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
name|search
operator|.
name|MatchAllDocsQuery
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
name|search
operator|.
name|Sort
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
name|search
operator|.
name|SortField
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
name|search
operator|.
name|TopFieldDocs
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
name|PutMappingRequest
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
name|index
operator|.
name|fielddata
operator|.
name|plain
operator|.
name|ParentChildIndexFieldData
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
name|Uid
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
name|internal
operator|.
name|ParentFieldMapper
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
name|internal
operator|.
name|UidFieldMapper
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
name|MultiValueMode
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
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|CountDownLatch
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
name|nullValue
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|ParentChildFieldDataTests
specifier|public
class|class
name|ParentChildFieldDataTests
extends|extends
name|AbstractFieldDataTestCase
block|{
DECL|field|parentType
specifier|private
specifier|final
name|String
name|parentType
init|=
literal|"parent"
decl_stmt|;
DECL|field|childType
specifier|private
specifier|final
name|String
name|childType
init|=
literal|"child"
decl_stmt|;
DECL|field|grandChildType
specifier|private
specifier|final
name|String
name|grandChildType
init|=
literal|"grand-child"
decl_stmt|;
annotation|@
name|Before
DECL|method|before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|mapperService
operator|.
name|merge
argument_list|(
name|childType
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
name|childType
argument_list|,
literal|"_parent"
argument_list|,
literal|"type="
operator|+
name|parentType
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
name|grandChildType
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
name|PutMappingRequest
operator|.
name|buildFromSimplifiedDef
argument_list|(
name|grandChildType
argument_list|,
literal|"_parent"
argument_list|,
literal|"type="
operator|+
name|childType
argument_list|)
operator|.
name|string
argument_list|()
argument_list|)
argument_list|,
name|MapperService
operator|.
name|MergeReason
operator|.
name|MAPPING_UPDATE
argument_list|,
literal|false
argument_list|)
expr_stmt|;
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|parentType
argument_list|,
literal|"1"
argument_list|)
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
name|createJoinField
argument_list|(
name|parentType
argument_list|,
literal|"1"
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|childType
argument_list|,
literal|"2"
argument_list|)
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
name|StringField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|parentType
argument_list|,
literal|"1"
argument_list|)
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
name|createJoinField
argument_list|(
name|parentType
argument_list|,
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|createJoinField
argument_list|(
name|childType
argument_list|,
literal|"2"
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|childType
argument_list|,
literal|"3"
argument_list|)
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
name|StringField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|parentType
argument_list|,
literal|"1"
argument_list|)
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
name|createJoinField
argument_list|(
name|parentType
argument_list|,
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|createJoinField
argument_list|(
name|childType
argument_list|,
literal|"3"
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|parentType
argument_list|,
literal|"2"
argument_list|)
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
name|createJoinField
argument_list|(
name|parentType
argument_list|,
literal|"2"
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|childType
argument_list|,
literal|"4"
argument_list|)
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
name|StringField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|parentType
argument_list|,
literal|"2"
argument_list|)
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
name|createJoinField
argument_list|(
name|parentType
argument_list|,
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|createJoinField
argument_list|(
name|childType
argument_list|,
literal|"4"
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|childType
argument_list|,
literal|"5"
argument_list|)
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
name|StringField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|parentType
argument_list|,
literal|"1"
argument_list|)
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
name|createJoinField
argument_list|(
name|parentType
argument_list|,
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|d
operator|.
name|add
argument_list|(
name|createJoinField
argument_list|(
name|childType
argument_list|,
literal|"5"
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|grandChildType
argument_list|,
literal|"6"
argument_list|)
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
name|StringField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
name|childType
argument_list|,
literal|"2"
argument_list|)
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
name|createJoinField
argument_list|(
name|childType
argument_list|,
literal|"2"
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
name|UidFieldMapper
operator|.
name|NAME
argument_list|,
name|Uid
operator|.
name|createUid
argument_list|(
literal|"other-type"
argument_list|,
literal|"1"
argument_list|)
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
DECL|method|createJoinField
specifier|private
name|SortedDocValuesField
name|createJoinField
parameter_list|(
name|String
name|parentType
parameter_list|,
name|String
name|id
parameter_list|)
block|{
return|return
operator|new
name|SortedDocValuesField
argument_list|(
name|ParentFieldMapper
operator|.
name|joinField
argument_list|(
name|parentType
argument_list|)
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|id
argument_list|)
argument_list|)
return|;
block|}
DECL|method|testGetBytesValues
specifier|public
name|void
name|testGetBytesValues
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
name|childType
argument_list|)
decl_stmt|;
name|AtomicFieldData
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
name|SortedBinaryDocValues
name|bytesValues
init|=
name|fieldData
operator|.
name|getBytesValues
argument_list|()
decl_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"1"
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|1
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|1
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"3"
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|1
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"4"
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|1
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5"
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|6
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
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
name|bytesValues
operator|.
name|valueAt
argument_list|(
literal|0
argument_list|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|bytesValues
operator|.
name|setDocument
argument_list|(
literal|7
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|bytesValues
operator|.
name|count
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSorting
specifier|public
name|void
name|testSorting
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
name|childType
argument_list|)
decl_stmt|;
name|IndexSearcher
name|searcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
argument_list|)
decl_stmt|;
name|IndexFieldData
operator|.
name|XFieldComparatorSource
name|comparator
init|=
name|indexFieldData
operator|.
name|comparatorSource
argument_list|(
literal|"_last"
argument_list|,
name|MultiValueMode
operator|.
name|MIN
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|TopFieldDocs
name|topDocs
init|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
operator|new
name|SortField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|comparator
argument_list|,
literal|false
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
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
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
argument_list|,
name|equalTo
argument_list|(
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|topDocs
operator|=
name|searcher
operator|.
name|search
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
literal|10
argument_list|,
operator|new
name|Sort
argument_list|(
operator|new
name|SortField
argument_list|(
name|ParentFieldMapper
operator|.
name|NAME
argument_list|,
name|comparator
argument_list|,
literal|true
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|totalHits
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
operator|.
name|length
argument_list|,
name|equalTo
argument_list|(
literal|8
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|0
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|4
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|1
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|6
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|2
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|3
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
operator|.
name|doc
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
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|4
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|5
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|5
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|BytesRef
call|)
argument_list|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|6
index|]
argument_list|)
operator|.
name|fields
index|[
literal|0
index|]
operator|)
operator|.
name|utf8ToString
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
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
operator|.
name|doc
argument_list|,
name|equalTo
argument_list|(
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|FieldDoc
operator|)
name|topDocs
operator|.
name|scoreDocs
index|[
literal|7
index|]
operator|)
operator|.
name|fields
index|[
literal|0
index|]
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testThreads
specifier|public
name|void
name|testThreads
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|ParentChildIndexFieldData
name|indexFieldData
init|=
name|getForField
argument_list|(
name|childType
argument_list|)
decl_stmt|;
specifier|final
name|DirectoryReader
name|reader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|writer
argument_list|,
literal|true
argument_list|)
decl_stmt|;
specifier|final
name|IndexParentChildFieldData
name|global
init|=
name|indexFieldData
operator|.
name|loadGlobal
argument_list|(
name|reader
argument_list|)
decl_stmt|;
specifier|final
name|AtomicReference
argument_list|<
name|Exception
argument_list|>
name|error
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|int
name|numThreads
init|=
name|scaledRandomIntBetween
argument_list|(
literal|3
argument_list|,
literal|8
argument_list|)
decl_stmt|;
specifier|final
name|Thread
index|[]
name|threads
init|=
operator|new
name|Thread
index|[
name|numThreads
index|]
decl_stmt|;
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|Map
argument_list|<
name|Object
argument_list|,
name|BytesRef
index|[]
argument_list|>
name|expected
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|reader
operator|.
name|leaves
argument_list|()
control|)
block|{
name|AtomicParentChildFieldData
name|leafData
init|=
name|global
operator|.
name|load
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|SortedDocValues
name|parentIds
init|=
name|leafData
operator|.
name|getOrdinalsValues
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
specifier|final
name|BytesRef
index|[]
name|ids
init|=
operator|new
name|BytesRef
index|[
name|parentIds
operator|.
name|getValueCount
argument_list|()
index|]
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
name|parentIds
operator|.
name|getValueCount
argument_list|()
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|BytesRef
name|id
init|=
name|parentIds
operator|.
name|lookupOrd
argument_list|(
name|j
argument_list|)
decl_stmt|;
if|if
condition|(
name|id
operator|!=
literal|null
condition|)
block|{
name|ids
index|[
name|j
index|]
operator|=
name|BytesRef
operator|.
name|deepCopyOf
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
block|}
name|expected
operator|.
name|put
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
argument_list|,
name|ids
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numThreads
condition|;
operator|++
name|i
control|)
block|{
name|threads
index|[
name|i
index|]
operator|=
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
name|latch
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
literal|100000
condition|;
operator|++
name|i
control|)
block|{
for|for
control|(
name|LeafReaderContext
name|context
range|:
name|reader
operator|.
name|leaves
argument_list|()
control|)
block|{
name|AtomicParentChildFieldData
name|leafData
init|=
name|global
operator|.
name|load
argument_list|(
name|context
argument_list|)
decl_stmt|;
name|SortedDocValues
name|parentIds
init|=
name|leafData
operator|.
name|getOrdinalsValues
argument_list|(
name|parentType
argument_list|)
decl_stmt|;
specifier|final
name|BytesRef
index|[]
name|expectedIds
init|=
name|expected
operator|.
name|get
argument_list|(
name|context
operator|.
name|reader
argument_list|()
operator|.
name|getCoreCacheKey
argument_list|()
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
name|parentIds
operator|.
name|getValueCount
argument_list|()
condition|;
operator|++
name|j
control|)
block|{
specifier|final
name|BytesRef
name|id
init|=
name|parentIds
operator|.
name|lookupOrd
argument_list|(
name|j
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedIds
index|[
name|j
index|]
argument_list|,
name|id
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|error
operator|.
name|compareAndSet
argument_list|(
literal|null
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
expr_stmt|;
name|threads
index|[
name|i
index|]
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|thread
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
annotation|@
name|Override
DECL|method|getFieldDataType
specifier|protected
name|FieldDataType
name|getFieldDataType
parameter_list|()
block|{
return|return
operator|new
name|FieldDataType
argument_list|(
literal|"_parent"
argument_list|)
return|;
block|}
block|}
end_class

end_unit

