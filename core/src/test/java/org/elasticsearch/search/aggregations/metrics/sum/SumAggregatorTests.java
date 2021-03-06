begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.metrics.sum
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
name|sum
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
name|NumericDocValuesField
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
name|SortedNumericDocValuesField
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
name|IndexReader
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
name|RandomIndexWriter
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
name|Term
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
name|FieldValueQuery
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
name|TermQuery
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
name|store
operator|.
name|Directory
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
name|CheckedConsumer
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
name|NumberFieldMapper
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
name|AggregatorTestCase
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
name|function
operator|.
name|Consumer
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|singleton
import|;
end_import

begin_class
DECL|class|SumAggregatorTests
specifier|public
class|class
name|SumAggregatorTests
extends|extends
name|AggregatorTestCase
block|{
DECL|field|FIELD_NAME
specifier|private
specifier|static
specifier|final
name|String
name|FIELD_NAME
init|=
literal|"field"
decl_stmt|;
DECL|method|testNoDocs
specifier|public
name|void
name|testNoDocs
parameter_list|()
throws|throws
name|IOException
block|{
name|testCase
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|iw
lambda|->
block|{
comment|// Intentionally not writing any docs
block|}
argument_list|,
name|count
lambda|->
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|count
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNoMatchingField
specifier|public
name|void
name|testNoMatchingField
parameter_list|()
throws|throws
name|IOException
block|{
name|testCase
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|iw
lambda|->
block|{
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
literal|"wrong_number"
argument_list|,
literal|7
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
literal|"wrong_number"
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|count
lambda|->
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|count
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testNumericDocValues
specifier|public
name|void
name|testNumericDocValues
parameter_list|()
throws|throws
name|IOException
block|{
name|testCase
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|iw
lambda|->
block|{
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|count
lambda|->
name|assertEquals
argument_list|(
literal|24L
argument_list|,
name|count
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSortedNumericDocValues
specifier|public
name|void
name|testSortedNumericDocValues
parameter_list|()
throws|throws
name|IOException
block|{
name|testCase
argument_list|(
operator|new
name|FieldValueQuery
argument_list|(
name|FIELD_NAME
argument_list|)
argument_list|,
name|iw
lambda|->
block|{
name|iw
operator|.
name|addDocument
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|3
argument_list|)
argument_list|,
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|3
argument_list|)
argument_list|,
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|SortedNumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|count
lambda|->
name|assertEquals
argument_list|(
literal|15L
argument_list|,
name|count
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testQueryFiltering
specifier|public
name|void
name|testQueryFiltering
parameter_list|()
throws|throws
name|IOException
block|{
name|testCase
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
literal|"match"
argument_list|,
literal|"yes"
argument_list|)
argument_list|)
argument_list|,
name|iw
lambda|->
block|{
name|iw
operator|.
name|addDocument
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"match"
argument_list|,
literal|"yes"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|,
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"match"
argument_list|,
literal|"no"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|,
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"match"
argument_list|,
literal|"yes"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|,
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|3
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"match"
argument_list|,
literal|"no"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|,
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|4
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|iw
operator|.
name|addDocument
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"match"
argument_list|,
literal|"yes"
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|,
operator|new
name|NumericDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
literal|5
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|count
lambda|->
name|assertEquals
argument_list|(
literal|9L
argument_list|,
name|count
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testStringField
specifier|public
name|void
name|testStringField
parameter_list|()
throws|throws
name|IOException
block|{
name|IllegalStateException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalStateException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
block|{
name|testCase
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|iw
lambda|->
block|{
name|iw
operator|.
name|addDocument
argument_list|(
name|singleton
argument_list|(
operator|new
name|SortedDocValuesField
argument_list|(
name|FIELD_NAME
argument_list|,
operator|new
name|BytesRef
argument_list|(
literal|"1"
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|,
name|count
lambda|->
name|assertEquals
argument_list|(
literal|0L
argument_list|,
name|count
operator|.
name|getValue
argument_list|()
argument_list|,
literal|0d
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"unexpected docvalues type SORTED for field 'field' (expected one of [SORTED_NUMERIC, NUMERIC]). "
operator|+
literal|"Re-index with correct docvalues type."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testCase
specifier|private
name|void
name|testCase
parameter_list|(
name|Query
name|query
parameter_list|,
name|CheckedConsumer
argument_list|<
name|RandomIndexWriter
argument_list|,
name|IOException
argument_list|>
name|indexer
parameter_list|,
name|Consumer
argument_list|<
name|Sum
argument_list|>
name|verify
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|Directory
name|directory
init|=
name|newDirectory
argument_list|()
init|)
block|{
try|try
init|(
name|RandomIndexWriter
name|indexWriter
init|=
operator|new
name|RandomIndexWriter
argument_list|(
name|random
argument_list|()
argument_list|,
name|directory
argument_list|)
init|)
block|{
name|indexer
operator|.
name|accept
argument_list|(
name|indexWriter
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|IndexReader
name|indexReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
init|)
block|{
name|IndexSearcher
name|indexSearcher
init|=
name|newSearcher
argument_list|(
name|indexReader
argument_list|,
literal|true
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|MappedFieldType
name|fieldType
init|=
operator|new
name|NumberFieldMapper
operator|.
name|NumberFieldType
argument_list|(
name|NumberFieldMapper
operator|.
name|NumberType
operator|.
name|LONG
argument_list|)
decl_stmt|;
name|fieldType
operator|.
name|setName
argument_list|(
name|FIELD_NAME
argument_list|)
expr_stmt|;
name|fieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|SumAggregationBuilder
name|aggregationBuilder
init|=
operator|new
name|SumAggregationBuilder
argument_list|(
literal|"_name"
argument_list|)
decl_stmt|;
name|aggregationBuilder
operator|.
name|field
argument_list|(
name|FIELD_NAME
argument_list|)
expr_stmt|;
name|SumAggregator
name|aggregator
init|=
name|createAggregator
argument_list|(
name|aggregationBuilder
argument_list|,
name|indexSearcher
argument_list|,
name|fieldType
argument_list|)
decl_stmt|;
name|aggregator
operator|.
name|preCollection
argument_list|()
expr_stmt|;
name|indexSearcher
operator|.
name|search
argument_list|(
name|query
argument_list|,
name|aggregator
argument_list|)
expr_stmt|;
name|aggregator
operator|.
name|postCollection
argument_list|()
expr_stmt|;
name|verify
operator|.
name|accept
argument_list|(
operator|(
name|Sum
operator|)
name|aggregator
operator|.
name|buildAggregation
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

