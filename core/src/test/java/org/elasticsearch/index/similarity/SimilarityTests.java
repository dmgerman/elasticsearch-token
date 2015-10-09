begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.similarity
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|similarity
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
name|search
operator|.
name|similarities
operator|.
name|AfterEffectL
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
name|similarities
operator|.
name|BM25Similarity
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
name|similarities
operator|.
name|BasicModelG
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
name|similarities
operator|.
name|DFRSimilarity
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
name|similarities
operator|.
name|DefaultSimilarity
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
name|similarities
operator|.
name|DistributionSPL
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
name|similarities
operator|.
name|IBSimilarity
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
name|similarities
operator|.
name|LMDirichletSimilarity
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
name|similarities
operator|.
name|LMJelinekMercerSimilarity
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
name|similarities
operator|.
name|LambdaTTF
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
name|similarities
operator|.
name|NormalizationH2
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
name|IndexService
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
name|test
operator|.
name|ESSingleNodeTestCase
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
name|CoreMatchers
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
name|CoreMatchers
operator|.
name|instanceOf
import|;
end_import

begin_class
DECL|class|SimilarityTests
specifier|public
class|class
name|SimilarityTests
extends|extends
name|ESSingleNodeTestCase
block|{
DECL|method|testResolveDefaultSimilarities
specifier|public
name|void
name|testResolveDefaultSimilarities
parameter_list|()
block|{
name|SimilarityService
name|similarityService
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|)
operator|.
name|similarityService
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|similarityService
operator|.
name|getSimilarity
argument_list|(
literal|"default"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|DefaultSimilarity
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|similarityService
operator|.
name|getSimilarity
argument_list|(
literal|"BM25"
argument_list|)
operator|.
name|get
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|BM25Similarity
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveSimilaritiesFromMapping_default
specifier|public
name|void
name|testResolveSimilaritiesFromMapping_default
parameter_list|()
throws|throws
name|IOException
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
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"similarity"
argument_list|,
literal|"my_similarity"
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
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.type"
argument_list|,
literal|"default"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.discount_overlaps"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|indexService
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
name|assertThat
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|DefaultSimilarityProvider
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|DefaultSimilarity
name|similarity
init|=
operator|(
name|DefaultSimilarity
operator|)
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getDiscountOverlaps
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveSimilaritiesFromMapping_bm25
specifier|public
name|void
name|testResolveSimilaritiesFromMapping_bm25
parameter_list|()
throws|throws
name|IOException
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
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"similarity"
argument_list|,
literal|"my_similarity"
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
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.type"
argument_list|,
literal|"BM25"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.k1"
argument_list|,
literal|2.0f
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.b"
argument_list|,
literal|1.5f
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.discount_overlaps"
argument_list|,
literal|false
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|indexService
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
name|assertThat
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|BM25SimilarityProvider
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|BM25Similarity
name|similarity
init|=
operator|(
name|BM25Similarity
operator|)
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getK1
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|2.0f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getB
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1.5f
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getDiscountOverlaps
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveSimilaritiesFromMapping_DFR
specifier|public
name|void
name|testResolveSimilaritiesFromMapping_DFR
parameter_list|()
throws|throws
name|IOException
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
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"similarity"
argument_list|,
literal|"my_similarity"
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
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.type"
argument_list|,
literal|"DFR"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.basic_model"
argument_list|,
literal|"g"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.after_effect"
argument_list|,
literal|"l"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.normalization"
argument_list|,
literal|"h2"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.normalization.h2.c"
argument_list|,
literal|3f
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|indexService
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
name|assertThat
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|DFRSimilarityProvider
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|DFRSimilarity
name|similarity
init|=
operator|(
name|DFRSimilarity
operator|)
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getBasicModel
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|BasicModelG
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getAfterEffect
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|AfterEffectL
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getNormalization
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NormalizationH2
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NormalizationH2
operator|)
name|similarity
operator|.
name|getNormalization
argument_list|()
operator|)
operator|.
name|getC
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3f
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveSimilaritiesFromMapping_IB
specifier|public
name|void
name|testResolveSimilaritiesFromMapping_IB
parameter_list|()
throws|throws
name|IOException
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
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"similarity"
argument_list|,
literal|"my_similarity"
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
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.type"
argument_list|,
literal|"IB"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.distribution"
argument_list|,
literal|"spl"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.lambda"
argument_list|,
literal|"ttf"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.normalization"
argument_list|,
literal|"h2"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.normalization.h2.c"
argument_list|,
literal|3f
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|indexService
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
name|assertThat
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|IBSimilarityProvider
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|IBSimilarity
name|similarity
init|=
operator|(
name|IBSimilarity
operator|)
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getDistribution
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|DistributionSPL
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getLambda
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|LambdaTTF
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getNormalization
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|NormalizationH2
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NormalizationH2
operator|)
name|similarity
operator|.
name|getNormalization
argument_list|()
operator|)
operator|.
name|getC
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3f
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveSimilaritiesFromMapping_LMDirichlet
specifier|public
name|void
name|testResolveSimilaritiesFromMapping_LMDirichlet
parameter_list|()
throws|throws
name|IOException
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
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"similarity"
argument_list|,
literal|"my_similarity"
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
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.type"
argument_list|,
literal|"LMDirichlet"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.mu"
argument_list|,
literal|3000f
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|indexService
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
name|assertThat
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|LMDirichletSimilarityProvider
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|LMDirichletSimilarity
name|similarity
init|=
operator|(
name|LMDirichletSimilarity
operator|)
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getMu
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|3000f
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testResolveSimilaritiesFromMapping_LMJelinekMercer
specifier|public
name|void
name|testResolveSimilaritiesFromMapping_LMJelinekMercer
parameter_list|()
throws|throws
name|IOException
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
literal|"properties"
argument_list|)
operator|.
name|startObject
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
literal|"string"
argument_list|)
operator|.
name|field
argument_list|(
literal|"similarity"
argument_list|,
literal|"my_similarity"
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
name|endObject
argument_list|()
operator|.
name|string
argument_list|()
decl_stmt|;
name|Settings
name|indexSettings
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.type"
argument_list|,
literal|"LMJelinekMercer"
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.similarity.my_similarity.lambda"
argument_list|,
literal|0.7f
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"foo"
argument_list|,
name|indexSettings
argument_list|)
decl_stmt|;
name|DocumentMapper
name|documentMapper
init|=
name|indexService
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
name|assertThat
argument_list|(
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|LMJelinekMercerSimilarityProvider
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|LMJelinekMercerSimilarity
name|similarity
init|=
operator|(
name|LMJelinekMercerSimilarity
operator|)
name|documentMapper
operator|.
name|mappers
argument_list|()
operator|.
name|getMapper
argument_list|(
literal|"field1"
argument_list|)
operator|.
name|fieldType
argument_list|()
operator|.
name|similarity
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|similarity
operator|.
name|getLambda
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0.7f
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

