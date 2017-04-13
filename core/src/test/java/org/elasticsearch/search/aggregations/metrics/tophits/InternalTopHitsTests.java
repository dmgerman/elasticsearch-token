begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.tophits
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|tophits
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
name|IndexWriter
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
name|FieldComparator
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
name|ScoreDoc
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
name|common
operator|.
name|collect
operator|.
name|Tuple
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
name|stream
operator|.
name|Writeable
operator|.
name|Reader
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
name|text
operator|.
name|Text
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
name|SearchHitField
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
name|aggregations
operator|.
name|InternalAggregationTestCase
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
name|aggregations
operator|.
name|pipeline
operator|.
name|PipelineAggregator
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
name|SearchHit
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
name|SearchHits
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Comparator
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
name|HashSet
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|max
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|lang
operator|.
name|Math
operator|.
name|min
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Comparator
operator|.
name|comparing
import|;
end_import

begin_class
DECL|class|InternalTopHitsTests
specifier|public
class|class
name|InternalTopHitsTests
extends|extends
name|InternalAggregationTestCase
argument_list|<
name|InternalTopHits
argument_list|>
block|{
comment|/**      * Should the test instances look like they are sorted by some fields (true) or sorted by score (false). Set here because these need      * to be the same across the entirety of {@link #testReduceRandom()}.      */
DECL|field|testInstancesLookSortedByField
specifier|private
specifier|final
name|boolean
name|testInstancesLookSortedByField
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
comment|/**      * Fields shared by all instances created by {@link #createTestInstance(String, List, Map)}.      */
DECL|field|testInstancesSortFields
specifier|private
specifier|final
name|SortField
index|[]
name|testInstancesSortFields
init|=
name|testInstancesLookSortedByField
condition|?
name|randomSortFields
argument_list|()
else|:
operator|new
name|SortField
index|[
literal|0
index|]
decl_stmt|;
annotation|@
name|Override
DECL|method|createTestInstance
specifier|protected
name|InternalTopHits
name|createTestInstance
parameter_list|(
name|String
name|name
parameter_list|,
name|List
argument_list|<
name|PipelineAggregator
argument_list|>
name|pipelineAggregators
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
block|{
name|int
name|from
init|=
literal|0
decl_stmt|;
name|int
name|requestedSize
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|40
argument_list|)
decl_stmt|;
name|int
name|actualSize
init|=
name|between
argument_list|(
literal|0
argument_list|,
name|requestedSize
argument_list|)
decl_stmt|;
name|float
name|maxScore
init|=
name|Float
operator|.
name|MIN_VALUE
decl_stmt|;
name|ScoreDoc
index|[]
name|scoreDocs
init|=
operator|new
name|ScoreDoc
index|[
name|actualSize
index|]
decl_stmt|;
name|SearchHit
index|[]
name|hits
init|=
operator|new
name|SearchHit
index|[
name|actualSize
index|]
decl_stmt|;
name|Set
argument_list|<
name|Integer
argument_list|>
name|usedDocIds
init|=
operator|new
name|HashSet
argument_list|<>
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
name|actualSize
condition|;
name|i
operator|++
control|)
block|{
name|float
name|score
init|=
name|randomFloat
argument_list|()
decl_stmt|;
name|maxScore
operator|=
name|max
argument_list|(
name|maxScore
argument_list|,
name|score
argument_list|)
expr_stmt|;
name|int
name|docId
init|=
name|randomValueOtherThanMany
argument_list|(
name|usedDocIds
operator|::
name|contains
argument_list|,
parameter_list|()
lambda|->
name|between
argument_list|(
literal|0
argument_list|,
name|IndexWriter
operator|.
name|MAX_DOCS
argument_list|)
argument_list|)
decl_stmt|;
name|usedDocIds
operator|.
name|add
argument_list|(
name|docId
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|SearchHitField
argument_list|>
name|searchHitFields
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
if|if
condition|(
name|testInstancesLookSortedByField
condition|)
block|{
name|Object
index|[]
name|fields
init|=
operator|new
name|Object
index|[
name|testInstancesSortFields
operator|.
name|length
index|]
decl_stmt|;
for|for
control|(
name|int
name|f
init|=
literal|0
init|;
name|f
operator|<
name|testInstancesSortFields
operator|.
name|length
condition|;
name|f
operator|++
control|)
block|{
name|fields
index|[
name|f
index|]
operator|=
name|randomOfType
argument_list|(
name|testInstancesSortFields
index|[
name|f
index|]
operator|.
name|getType
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|scoreDocs
index|[
name|i
index|]
operator|=
operator|new
name|FieldDoc
argument_list|(
name|docId
argument_list|,
name|score
argument_list|,
name|fields
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scoreDocs
index|[
name|i
index|]
operator|=
operator|new
name|ScoreDoc
argument_list|(
name|docId
argument_list|,
name|score
argument_list|)
expr_stmt|;
block|}
name|hits
index|[
name|i
index|]
operator|=
operator|new
name|SearchHit
argument_list|(
name|docId
argument_list|,
name|Integer
operator|.
name|toString
argument_list|(
name|i
argument_list|)
argument_list|,
operator|new
name|Text
argument_list|(
literal|"test"
argument_list|)
argument_list|,
name|searchHitFields
argument_list|)
expr_stmt|;
name|hits
index|[
name|i
index|]
operator|.
name|score
argument_list|(
name|score
argument_list|)
expr_stmt|;
block|}
name|int
name|totalHits
init|=
name|between
argument_list|(
name|actualSize
argument_list|,
literal|500000
argument_list|)
decl_stmt|;
name|SearchHits
name|searchHits
init|=
operator|new
name|SearchHits
argument_list|(
name|hits
argument_list|,
name|totalHits
argument_list|,
name|maxScore
argument_list|)
decl_stmt|;
name|TopDocs
name|topDocs
decl_stmt|;
name|Arrays
operator|.
name|sort
argument_list|(
name|scoreDocs
argument_list|,
name|scoreDocComparator
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|testInstancesLookSortedByField
condition|)
block|{
name|topDocs
operator|=
operator|new
name|TopFieldDocs
argument_list|(
name|totalHits
argument_list|,
name|scoreDocs
argument_list|,
name|testInstancesSortFields
argument_list|,
name|maxScore
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|topDocs
operator|=
operator|new
name|TopDocs
argument_list|(
name|totalHits
argument_list|,
name|scoreDocs
argument_list|,
name|maxScore
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|InternalTopHits
argument_list|(
name|name
argument_list|,
name|from
argument_list|,
name|requestedSize
argument_list|,
name|topDocs
argument_list|,
name|searchHits
argument_list|,
name|pipelineAggregators
argument_list|,
name|metaData
argument_list|)
return|;
block|}
DECL|method|randomOfType
specifier|private
name|Object
name|randomOfType
parameter_list|(
name|SortField
operator|.
name|Type
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|CUSTOM
case|:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
case|case
name|DOC
case|:
return|return
name|between
argument_list|(
literal|0
argument_list|,
name|IndexWriter
operator|.
name|MAX_DOCS
argument_list|)
return|;
case|case
name|DOUBLE
case|:
return|return
name|randomDouble
argument_list|()
return|;
case|case
name|FLOAT
case|:
return|return
name|randomFloat
argument_list|()
return|;
case|case
name|INT
case|:
return|return
name|randomInt
argument_list|()
return|;
case|case
name|LONG
case|:
return|return
name|randomLong
argument_list|()
return|;
case|case
name|REWRITEABLE
case|:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
case|case
name|SCORE
case|:
return|return
name|randomFloat
argument_list|()
return|;
case|case
name|STRING
case|:
return|return
operator|new
name|BytesRef
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
return|;
case|case
name|STRING_VAL
case|:
return|return
operator|new
name|BytesRef
argument_list|(
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Unkown SortField.Type: "
operator|+
name|type
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|assertReduced
specifier|protected
name|void
name|assertReduced
parameter_list|(
name|InternalTopHits
name|reduced
parameter_list|,
name|List
argument_list|<
name|InternalTopHits
argument_list|>
name|inputs
parameter_list|)
block|{
name|SearchHits
name|actualHits
init|=
name|reduced
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Tuple
argument_list|<
name|ScoreDoc
argument_list|,
name|SearchHit
argument_list|>
argument_list|>
name|allHits
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|float
name|maxScore
init|=
name|Float
operator|.
name|MIN_VALUE
decl_stmt|;
name|long
name|totalHits
init|=
literal|0
decl_stmt|;
for|for
control|(
name|int
name|input
init|=
literal|0
init|;
name|input
operator|<
name|inputs
operator|.
name|size
argument_list|()
condition|;
name|input
operator|++
control|)
block|{
name|SearchHits
name|internalHits
init|=
name|inputs
operator|.
name|get
argument_list|(
name|input
argument_list|)
operator|.
name|getHits
argument_list|()
decl_stmt|;
name|totalHits
operator|+=
name|internalHits
operator|.
name|getTotalHits
argument_list|()
expr_stmt|;
name|maxScore
operator|=
name|max
argument_list|(
name|maxScore
argument_list|,
name|internalHits
operator|.
name|getMaxScore
argument_list|()
argument_list|)
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
name|internalHits
operator|.
name|internalHits
argument_list|()
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|ScoreDoc
name|doc
init|=
name|inputs
operator|.
name|get
argument_list|(
name|input
argument_list|)
operator|.
name|getTopDocs
argument_list|()
operator|.
name|scoreDocs
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|testInstancesLookSortedByField
condition|)
block|{
name|doc
operator|=
operator|new
name|FieldDoc
argument_list|(
name|doc
operator|.
name|doc
argument_list|,
name|doc
operator|.
name|score
argument_list|,
operator|(
operator|(
name|FieldDoc
operator|)
name|doc
operator|)
operator|.
name|fields
argument_list|,
name|input
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|doc
operator|=
operator|new
name|ScoreDoc
argument_list|(
name|doc
operator|.
name|doc
argument_list|,
name|doc
operator|.
name|score
argument_list|,
name|input
argument_list|)
expr_stmt|;
block|}
name|allHits
operator|.
name|add
argument_list|(
operator|new
name|Tuple
argument_list|<>
argument_list|(
name|doc
argument_list|,
name|internalHits
operator|.
name|internalHits
argument_list|()
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|allHits
operator|.
name|sort
argument_list|(
name|comparing
argument_list|(
name|Tuple
operator|::
name|v1
argument_list|,
name|scoreDocComparator
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|SearchHit
index|[]
name|expectedHitsHits
init|=
operator|new
name|SearchHit
index|[
name|min
argument_list|(
name|inputs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getSize
argument_list|()
argument_list|,
name|allHits
operator|.
name|size
argument_list|()
argument_list|)
index|]
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
name|expectedHitsHits
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|expectedHitsHits
index|[
name|i
index|]
operator|=
name|allHits
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|v2
argument_list|()
expr_stmt|;
block|}
comment|// Lucene's TopDocs initializes the maxScore to Float.NaN, if there is no maxScore
name|SearchHits
name|expectedHits
init|=
operator|new
name|SearchHits
argument_list|(
name|expectedHitsHits
argument_list|,
name|totalHits
argument_list|,
name|maxScore
operator|==
name|Float
operator|.
name|MIN_VALUE
condition|?
name|Float
operator|.
name|NaN
else|:
name|maxScore
argument_list|)
decl_stmt|;
name|assertEqualsWithErrorMessageFromXContent
argument_list|(
name|expectedHits
argument_list|,
name|actualHits
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|instanceReader
specifier|protected
name|Reader
argument_list|<
name|InternalTopHits
argument_list|>
name|instanceReader
parameter_list|()
block|{
return|return
name|InternalTopHits
operator|::
operator|new
return|;
block|}
DECL|method|randomSortFields
specifier|private
name|SortField
index|[]
name|randomSortFields
parameter_list|()
block|{
name|SortField
index|[]
name|sortFields
init|=
operator|new
name|SortField
index|[
name|between
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
index|]
decl_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|usedSortFields
init|=
operator|new
name|HashSet
argument_list|<>
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
name|sortFields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|sortField
init|=
name|randomValueOtherThanMany
argument_list|(
name|usedSortFields
operator|::
name|contains
argument_list|,
parameter_list|()
lambda|->
name|randomAlphaOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|usedSortFields
operator|.
name|add
argument_list|(
name|sortField
argument_list|)
expr_stmt|;
name|SortField
operator|.
name|Type
name|type
init|=
name|randomValueOtherThanMany
argument_list|(
name|t
lambda|->
name|t
operator|==
name|SortField
operator|.
name|Type
operator|.
name|CUSTOM
operator|||
name|t
operator|==
name|SortField
operator|.
name|Type
operator|.
name|REWRITEABLE
argument_list|,
parameter_list|()
lambda|->
name|randomFrom
argument_list|(
name|SortField
operator|.
name|Type
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|sortFields
index|[
name|i
index|]
operator|=
operator|new
name|SortField
argument_list|(
name|sortField
argument_list|,
name|type
argument_list|)
expr_stmt|;
block|}
return|return
name|sortFields
return|;
block|}
DECL|method|scoreDocComparator
specifier|private
name|Comparator
argument_list|<
name|ScoreDoc
argument_list|>
name|scoreDocComparator
parameter_list|()
block|{
return|return
name|innerScoreDocComparator
argument_list|()
operator|.
name|thenComparing
argument_list|(
name|s
lambda|->
name|s
operator|.
name|shardIndex
argument_list|)
return|;
block|}
DECL|method|innerScoreDocComparator
specifier|private
name|Comparator
argument_list|<
name|ScoreDoc
argument_list|>
name|innerScoreDocComparator
parameter_list|()
block|{
if|if
condition|(
name|testInstancesLookSortedByField
condition|)
block|{
comment|// Values passed to getComparator shouldn't matter
annotation|@
name|SuppressWarnings
argument_list|(
literal|"rawtypes"
argument_list|)
name|FieldComparator
index|[]
name|comparators
init|=
operator|new
name|FieldComparator
index|[
name|testInstancesSortFields
operator|.
name|length
index|]
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
name|testInstancesSortFields
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|comparators
index|[
name|i
index|]
operator|=
name|testInstancesSortFields
index|[
name|i
index|]
operator|.
name|getComparator
argument_list|(
literal|0
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
return|return
parameter_list|(
name|lhs
parameter_list|,
name|rhs
parameter_list|)
lambda|->
block|{
name|FieldDoc
name|l
init|=
operator|(
name|FieldDoc
operator|)
name|lhs
decl_stmt|;
name|FieldDoc
name|r
init|=
operator|(
name|FieldDoc
operator|)
name|rhs
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|l
operator|.
name|fields
operator|.
name|length
condition|)
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|int
name|c
init|=
name|comparators
index|[
name|i
index|]
operator|.
name|compareValues
argument_list|(
name|l
operator|.
name|fields
index|[
name|i
index|]
argument_list|,
name|r
operator|.
name|fields
index|[
name|i
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|c
operator|!=
literal|0
condition|)
block|{
return|return
name|c
return|;
block|}
name|i
operator|++
expr_stmt|;
block|}
return|return
literal|0
return|;
block|}
return|;
block|}
else|else
block|{
name|Comparator
argument_list|<
name|ScoreDoc
argument_list|>
name|comparator
init|=
name|comparing
argument_list|(
name|d
lambda|->
name|d
operator|.
name|score
argument_list|)
decl_stmt|;
return|return
name|comparator
operator|.
name|reversed
argument_list|()
return|;
block|}
block|}
block|}
end_class

end_unit

