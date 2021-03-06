begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.bucket.sampler
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|sampler
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
name|DoubleDocValuesField
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
name|document
operator|.
name|TextField
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
name|lucene
operator|.
name|search
operator|.
name|function
operator|.
name|FieldValueFactorFunction
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
name|function
operator|.
name|FunctionScoreQuery
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
name|Index
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
name|IndexNumericFieldData
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
name|SortedNumericDVIndexFieldData
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
name|KeywordFieldMapper
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
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|bucket
operator|.
name|terms
operator|.
name|Terms
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
name|bucket
operator|.
name|terms
operator|.
name|TermsAggregationBuilder
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
name|function
operator|.
name|Consumer
import|;
end_import

begin_class
DECL|class|DiversifiedSamplerTests
specifier|public
class|class
name|DiversifiedSamplerTests
extends|extends
name|AggregatorTestCase
block|{
DECL|method|testDiversifiedSampler
specifier|public
name|void
name|testDiversifiedSampler
parameter_list|()
throws|throws
name|Exception
block|{
name|String
name|data
index|[]
init|=
block|{
comment|// "id,cat,name,price,inStock,author_t,series_t,sequence_i,genre_s,genre_id",
literal|"0553573403,book,A Game of Thrones,7.99,true,George R.R. Martin,A Song of Ice and Fire,1,fantasy,0"
block|,
literal|"0553579908,book,A Clash of Kings,7.99,true,George R.R. Martin,A Song of Ice and Fire,2,fantasy,0"
block|,
literal|"055357342X,book,A Storm of Swords,7.99,true,George R.R. Martin,A Song of Ice and Fire,3,fantasy,0"
block|,
literal|"0553293354,book,Foundation,17.99,true,Isaac Asimov,Foundation Novels,1,scifi,1"
block|,
literal|"0812521390,book,The Black Company,6.99,false,Glen Cook,The Chronicles of The Black Company,1,fantasy,0"
block|,
literal|"0812550706,book,Ender's Game,6.99,true,Orson Scott Card,Ender,1,scifi,1"
block|,
literal|"0441385532,book,Jhereg,7.95,false,Steven Brust,Vlad Taltos,1,fantasy,0"
block|,
literal|"0380014300,book,Nine Princes In Amber,6.99,true,Roger Zelazny,the Chronicles of Amber,1,fantasy,0"
block|,
literal|"0805080481,book,The Book of Three,5.99,true,Lloyd Alexander,The Chronicles of Prydain,1,fantasy,0"
block|,
literal|"080508049X,book,The Black Cauldron,5.99,true,Lloyd Alexander,The Chronicles of Prydain,2,fantasy,0"
block|}
decl_stmt|;
name|Directory
name|directory
init|=
name|newDirectory
argument_list|()
decl_stmt|;
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
decl_stmt|;
for|for
control|(
name|String
name|entry
range|:
name|data
control|)
block|{
name|String
index|[]
name|parts
init|=
name|entry
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
name|Document
name|document
init|=
operator|new
name|Document
argument_list|()
decl_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|SortedDocValuesField
argument_list|(
literal|"id"
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|parts
index|[
literal|0
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"cat"
argument_list|,
name|parts
index|[
literal|1
index|]
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|TextField
argument_list|(
literal|"name"
argument_list|,
name|parts
index|[
literal|2
index|]
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|DoubleDocValuesField
argument_list|(
literal|"price"
argument_list|,
name|Double
operator|.
name|valueOf
argument_list|(
name|parts
index|[
literal|3
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"inStock"
argument_list|,
name|parts
index|[
literal|4
index|]
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"author"
argument_list|,
name|parts
index|[
literal|5
index|]
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"series"
argument_list|,
name|parts
index|[
literal|6
index|]
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|StringField
argument_list|(
literal|"sequence"
argument_list|,
name|parts
index|[
literal|7
index|]
argument_list|,
name|Field
operator|.
name|Store
operator|.
name|NO
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|SortedDocValuesField
argument_list|(
literal|"genre"
argument_list|,
operator|new
name|BytesRef
argument_list|(
name|parts
index|[
literal|8
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|document
operator|.
name|add
argument_list|(
operator|new
name|NumericDocValuesField
argument_list|(
literal|"genre_id"
argument_list|,
name|Long
operator|.
name|valueOf
argument_list|(
name|parts
index|[
literal|9
index|]
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|indexWriter
operator|.
name|addDocument
argument_list|(
name|document
argument_list|)
expr_stmt|;
block|}
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|IndexReader
name|indexReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
decl_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
name|MappedFieldType
name|genreFieldType
init|=
operator|new
name|KeywordFieldMapper
operator|.
name|KeywordFieldType
argument_list|()
decl_stmt|;
name|genreFieldType
operator|.
name|setName
argument_list|(
literal|"genre"
argument_list|)
expr_stmt|;
name|genreFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Consumer
argument_list|<
name|InternalSampler
argument_list|>
name|verify
init|=
name|result
lambda|->
block|{
name|Terms
name|terms
init|=
name|result
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"terms"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"0805080481"
argument_list|,
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"0812550706"
argument_list|,
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
decl_stmt|;
name|testCase
argument_list|(
name|indexSearcher
argument_list|,
name|genreFieldType
argument_list|,
literal|"map"
argument_list|,
name|verify
argument_list|)
expr_stmt|;
name|testCase
argument_list|(
name|indexSearcher
argument_list|,
name|genreFieldType
argument_list|,
literal|"global_ordinals"
argument_list|,
name|verify
argument_list|)
expr_stmt|;
name|testCase
argument_list|(
name|indexSearcher
argument_list|,
name|genreFieldType
argument_list|,
literal|"bytes_hash"
argument_list|,
name|verify
argument_list|)
expr_stmt|;
name|genreFieldType
operator|=
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
expr_stmt|;
name|genreFieldType
operator|.
name|setName
argument_list|(
literal|"genre_id"
argument_list|)
expr_stmt|;
name|testCase
argument_list|(
name|indexSearcher
argument_list|,
name|genreFieldType
argument_list|,
literal|null
argument_list|,
name|verify
argument_list|)
expr_stmt|;
comment|// wrong field:
name|genreFieldType
operator|=
operator|new
name|KeywordFieldMapper
operator|.
name|KeywordFieldType
argument_list|()
expr_stmt|;
name|genreFieldType
operator|.
name|setName
argument_list|(
literal|"wrong_field"
argument_list|)
expr_stmt|;
name|genreFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|testCase
argument_list|(
name|indexSearcher
argument_list|,
name|genreFieldType
argument_list|,
literal|null
argument_list|,
name|result
lambda|->
block|{
name|Terms
name|terms
init|=
name|result
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"terms"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"0805080481"
argument_list|,
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getKeyAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|indexReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|directory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testCase
specifier|private
name|void
name|testCase
parameter_list|(
name|IndexSearcher
name|indexSearcher
parameter_list|,
name|MappedFieldType
name|genreFieldType
parameter_list|,
name|String
name|executionHint
parameter_list|,
name|Consumer
argument_list|<
name|InternalSampler
argument_list|>
name|verify
parameter_list|)
throws|throws
name|IOException
block|{
name|MappedFieldType
name|idFieldType
init|=
operator|new
name|KeywordFieldMapper
operator|.
name|KeywordFieldType
argument_list|()
decl_stmt|;
name|idFieldType
operator|.
name|setName
argument_list|(
literal|"id"
argument_list|)
expr_stmt|;
name|idFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|SortedNumericDVIndexFieldData
name|fieldData
init|=
operator|new
name|SortedNumericDVIndexFieldData
argument_list|(
operator|new
name|Index
argument_list|(
literal|"index"
argument_list|,
literal|"index"
argument_list|)
argument_list|,
literal|"price"
argument_list|,
name|IndexNumericFieldData
operator|.
name|NumericType
operator|.
name|DOUBLE
argument_list|)
decl_stmt|;
name|FunctionScoreQuery
name|query
init|=
operator|new
name|FunctionScoreQuery
argument_list|(
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
operator|new
name|FieldValueFactorFunction
argument_list|(
literal|"price"
argument_list|,
literal|1
argument_list|,
name|FieldValueFactorFunction
operator|.
name|Modifier
operator|.
name|RECIPROCAL
argument_list|,
literal|null
argument_list|,
name|fieldData
argument_list|)
argument_list|)
decl_stmt|;
name|DiversifiedAggregationBuilder
name|builder
init|=
operator|new
name|DiversifiedAggregationBuilder
argument_list|(
literal|"_name"
argument_list|)
operator|.
name|field
argument_list|(
name|genreFieldType
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|executionHint
argument_list|(
name|executionHint
argument_list|)
operator|.
name|subAggregation
argument_list|(
operator|new
name|TermsAggregationBuilder
argument_list|(
literal|"terms"
argument_list|,
literal|null
argument_list|)
operator|.
name|field
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
name|InternalSampler
name|result
init|=
name|search
argument_list|(
name|indexSearcher
argument_list|,
name|query
argument_list|,
name|builder
argument_list|,
name|genreFieldType
argument_list|,
name|idFieldType
argument_list|)
decl_stmt|;
name|verify
operator|.
name|accept
argument_list|(
name|result
argument_list|)
expr_stmt|;
block|}
DECL|method|testDiversifiedSampler_noDocs
specifier|public
name|void
name|testDiversifiedSampler_noDocs
parameter_list|()
throws|throws
name|Exception
block|{
name|Directory
name|directory
init|=
name|newDirectory
argument_list|()
decl_stmt|;
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
decl_stmt|;
name|indexWriter
operator|.
name|close
argument_list|()
expr_stmt|;
name|IndexReader
name|indexReader
init|=
name|DirectoryReader
operator|.
name|open
argument_list|(
name|directory
argument_list|)
decl_stmt|;
name|IndexSearcher
name|indexSearcher
init|=
operator|new
name|IndexSearcher
argument_list|(
name|indexReader
argument_list|)
decl_stmt|;
name|MappedFieldType
name|idFieldType
init|=
operator|new
name|KeywordFieldMapper
operator|.
name|KeywordFieldType
argument_list|()
decl_stmt|;
name|idFieldType
operator|.
name|setName
argument_list|(
literal|"id"
argument_list|)
expr_stmt|;
name|idFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|MappedFieldType
name|genreFieldType
init|=
operator|new
name|KeywordFieldMapper
operator|.
name|KeywordFieldType
argument_list|()
decl_stmt|;
name|genreFieldType
operator|.
name|setName
argument_list|(
literal|"genre"
argument_list|)
expr_stmt|;
name|genreFieldType
operator|.
name|setHasDocValues
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|DiversifiedAggregationBuilder
name|builder
init|=
operator|new
name|DiversifiedAggregationBuilder
argument_list|(
literal|"_name"
argument_list|)
operator|.
name|field
argument_list|(
name|genreFieldType
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|subAggregation
argument_list|(
operator|new
name|TermsAggregationBuilder
argument_list|(
literal|"terms"
argument_list|,
literal|null
argument_list|)
operator|.
name|field
argument_list|(
literal|"id"
argument_list|)
argument_list|)
decl_stmt|;
name|InternalSampler
name|result
init|=
name|search
argument_list|(
name|indexSearcher
argument_list|,
operator|new
name|MatchAllDocsQuery
argument_list|()
argument_list|,
name|builder
argument_list|,
name|genreFieldType
argument_list|,
name|idFieldType
argument_list|)
decl_stmt|;
name|Terms
name|terms
init|=
name|result
operator|.
name|getAggregations
argument_list|()
operator|.
name|get
argument_list|(
literal|"terms"
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|terms
operator|.
name|getBuckets
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|indexReader
operator|.
name|close
argument_list|()
expr_stmt|;
name|directory
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

