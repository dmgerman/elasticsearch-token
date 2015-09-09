begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.query
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|query
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
name|queries
operator|.
name|TermsQuery
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
name|elasticsearch
operator|.
name|common
operator|.
name|unit
operator|.
name|DistanceUnit
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
name|geo
operator|.
name|GeoPointFieldMapper
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
name|query
operator|.
name|GeohashCellQuery
operator|.
name|Builder
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|notNullValue
import|;
end_import

begin_class
DECL|class|GeohashCellQueryBuilderTests
specifier|public
class|class
name|GeohashCellQueryBuilderTests
extends|extends
name|BaseQueryTestCase
argument_list|<
name|GeohashCellQuery
operator|.
name|Builder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|Builder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|GeohashCellQuery
operator|.
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|GEO_FIELD_NAME
argument_list|)
decl_stmt|;
name|builder
operator|.
name|geohash
argument_list|(
name|randomGeohash
argument_list|(
literal|1
argument_list|,
literal|12
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|neighbors
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|precision
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|12
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|precision
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|1000000
argument_list|)
operator|+
name|randomFrom
argument_list|(
name|DistanceUnit
operator|.
name|values
argument_list|()
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|Builder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|QueryShardContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|queryBuilder
operator|.
name|neighbors
argument_list|()
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|TermsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|TermQuery
name|termQuery
init|=
operator|(
name|TermQuery
operator|)
name|query
decl_stmt|;
name|Term
name|term
init|=
name|termQuery
operator|.
name|getTerm
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|term
operator|.
name|field
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
operator|+
name|GeoPointFieldMapper
operator|.
name|Names
operator|.
name|GEOHASH_SUFFIX
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|geohash
init|=
name|queryBuilder
operator|.
name|geohash
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|precision
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|int
name|len
init|=
name|Math
operator|.
name|min
argument_list|(
name|queryBuilder
operator|.
name|precision
argument_list|()
argument_list|,
name|geohash
operator|.
name|length
argument_list|()
argument_list|)
decl_stmt|;
name|geohash
operator|=
name|geohash
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|term
operator|.
name|text
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|geohash
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Overridden here to ensure the test is only run if at least one type is      * present in the mappings. Geo queries do not execute if the field is not      * explicitly mapped      */
annotation|@
name|Override
DECL|method|testToQuery
specifier|public
name|void
name|testToQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|assumeTrue
argument_list|(
literal|"test runs only when at least a type is registered"
argument_list|,
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
argument_list|)
expr_stmt|;
name|super
operator|.
name|testToQuery
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNullField
specifier|public
name|void
name|testNullField
parameter_list|()
block|{
name|GeohashCellQuery
operator|.
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|builder
operator|.
name|geohash
argument_list|(
name|randomGeohash
argument_list|(
literal|1
argument_list|,
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|QueryValidationException
name|exception
init|=
name|builder
operator|.
name|validate
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|exception
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|validationErrors
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|validationErrors
argument_list|()
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
name|exception
operator|.
name|validationErrors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"["
operator|+
name|GeohashCellQuery
operator|.
name|NAME
operator|+
literal|"] fieldName must not be null"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testNullGeohash
specifier|public
name|void
name|testNullGeohash
parameter_list|()
block|{
name|GeohashCellQuery
operator|.
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|GEO_FIELD_NAME
argument_list|)
decl_stmt|;
name|QueryValidationException
name|exception
init|=
name|builder
operator|.
name|validate
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|exception
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|validationErrors
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|validationErrors
argument_list|()
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
name|exception
operator|.
name|validationErrors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"["
operator|+
name|GeohashCellQuery
operator|.
name|NAME
operator|+
literal|"] geohash or point must be defined"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testInvalidPrecision
specifier|public
name|void
name|testInvalidPrecision
parameter_list|()
block|{
name|GeohashCellQuery
operator|.
name|Builder
name|builder
init|=
operator|new
name|Builder
argument_list|(
name|GEO_FIELD_NAME
argument_list|)
decl_stmt|;
name|builder
operator|.
name|geohash
argument_list|(
name|randomGeohash
argument_list|(
literal|1
argument_list|,
literal|12
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|precision
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|QueryValidationException
name|exception
init|=
name|builder
operator|.
name|validate
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|exception
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|validationErrors
argument_list|()
argument_list|,
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|exception
operator|.
name|validationErrors
argument_list|()
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
name|exception
operator|.
name|validationErrors
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"["
operator|+
name|GeohashCellQuery
operator|.
name|NAME
operator|+
literal|"] precision must be greater than 0. Found ["
operator|+
operator|-
literal|1
operator|+
literal|"]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

