begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.search.nested
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|search
operator|.
name|nested
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
name|DoubleField
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
name|Filter
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
name|FilteredQuery
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
name|Query
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
name|QueryWrapperFilter
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
name|TopDocs
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
name|join
operator|.
name|BitDocIdSetCachingWrapperFilter
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
name|join
operator|.
name|ScoreMode
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
name|join
operator|.
name|ToParentBlockJoinQuery
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
name|search
operator|.
name|Queries
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
name|fielddata
operator|.
name|IndexFieldData
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
name|IndexFieldData
operator|.
name|XFieldComparatorSource
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
name|IndexFieldData
operator|.
name|XFieldComparatorSource
operator|.
name|Nested
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
name|fieldcomparator
operator|.
name|DoubleValuesComparatorSource
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
name|DoubleArrayIndexFieldData
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

begin_comment
comment|/**  */
end_comment

begin_class
DECL|class|DoubleNestedSortingTests
specifier|public
class|class
name|DoubleNestedSortingTests
extends|extends
name|AbstractNumberNestedSortingTests
block|{
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
literal|"double"
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createFieldComparator
specifier|protected
name|IndexFieldData
operator|.
name|XFieldComparatorSource
name|createFieldComparator
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|MultiValueMode
name|sortMode
parameter_list|,
name|Object
name|missingValue
parameter_list|,
name|Nested
name|nested
parameter_list|)
block|{
name|DoubleArrayIndexFieldData
name|fieldData
init|=
name|getForField
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
return|return
operator|new
name|DoubleValuesComparatorSource
argument_list|(
name|fieldData
argument_list|,
name|missingValue
argument_list|,
name|sortMode
argument_list|,
name|nested
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|createField
specifier|protected
name|IndexableField
name|createField
parameter_list|(
name|String
name|name
parameter_list|,
name|int
name|value
parameter_list|,
name|Field
operator|.
name|Store
name|store
parameter_list|)
block|{
return|return
operator|new
name|DoubleField
argument_list|(
name|name
argument_list|,
name|value
argument_list|,
name|store
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|assertAvgScoreMode
specifier|protected
name|void
name|assertAvgScoreMode
parameter_list|(
name|Filter
name|parentFilter
parameter_list|,
name|IndexSearcher
name|searcher
parameter_list|)
throws|throws
name|IOException
block|{
name|MultiValueMode
name|sortMode
init|=
name|MultiValueMode
operator|.
name|AVG
decl_stmt|;
name|Filter
name|childFilter
init|=
operator|new
name|QueryWrapperFilter
argument_list|(
name|Queries
operator|.
name|not
argument_list|(
name|parentFilter
argument_list|)
argument_list|)
decl_stmt|;
name|XFieldComparatorSource
name|nestedComparatorSource
init|=
name|createFieldComparator
argument_list|(
literal|"field2"
argument_list|,
name|sortMode
argument_list|,
operator|-
literal|127
argument_list|,
name|createNested
argument_list|(
name|parentFilter
argument_list|,
name|childFilter
argument_list|)
argument_list|)
decl_stmt|;
name|Query
name|query
init|=
operator|new
name|ToParentBlockJoinQuery
argument_list|(
operator|new
name|FilteredQuery
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|childFilter
argument_list|)
argument_list|,
operator|new
name|BitDocIdSetCachingWrapperFilter
argument_list|(
name|parentFilter
argument_list|)
argument_list|,
name|ScoreMode
operator|.
name|None
argument_list|)
decl_stmt|;
name|Sort
name|sort
init|=
operator|new
name|Sort
argument_list|(
operator|new
name|SortField
argument_list|(
literal|"field2"
argument_list|,
name|nestedComparatorSource
argument_list|)
argument_list|)
decl_stmt|;
name|TopDocs
name|topDocs
init|=
name|searcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
literal|5
argument_list|,
name|sort
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
literal|7
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
literal|5
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
literal|11
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|Number
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
name|intValue
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
literal|7
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|Number
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
name|intValue
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
literal|3
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|Number
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
name|intValue
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
literal|15
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|Number
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
name|intValue
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
literal|19
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
call|(
name|Number
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
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
