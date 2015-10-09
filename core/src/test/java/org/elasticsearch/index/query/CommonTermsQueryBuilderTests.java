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
name|queries
operator|.
name|ExtendedCommonTermsQuery
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
name|elasticsearch
operator|.
name|index
operator|.
name|query
operator|.
name|QueryBuilders
operator|.
name|commonTermsQuery
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|StreamsUtils
operator|.
name|copyToStringFromClasspath
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
name|nullValue
import|;
end_import

begin_class
DECL|class|CommonTermsQueryBuilderTests
specifier|public
class|class
name|CommonTermsQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|CommonTermsQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|CommonTermsQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|CommonTermsQueryBuilder
name|query
decl_stmt|;
comment|// mapped or unmapped field
name|String
name|text
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|=
operator|new
name|CommonTermsQueryBuilder
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
name|text
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|query
operator|=
operator|new
name|CommonTermsQueryBuilder
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|,
name|text
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|cutoffFrequency
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|lowFreqOperator
argument_list|(
name|randomFrom
argument_list|(
name|Operator
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// number of low frequency terms that must match
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|lowFreqMinimumShouldMatch
argument_list|(
literal|""
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|highFreqOperator
argument_list|(
name|randomFrom
argument_list|(
name|Operator
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// number of high frequency terms that must match
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|highFreqMinimumShouldMatch
argument_list|(
literal|""
operator|+
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|analyzer
argument_list|(
name|randomAnalyzer
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
name|query
operator|.
name|disableCoord
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|CommonTermsQueryBuilder
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
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|ExtendedCommonTermsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ExtendedCommonTermsQuery
name|extendedCommonTermsQuery
init|=
operator|(
name|ExtendedCommonTermsQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|extendedCommonTermsQuery
operator|.
name|getHighFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|highFreqMinimumShouldMatch
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|extendedCommonTermsQuery
operator|.
name|getLowFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|lowFreqMinimumShouldMatch
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testIllegalArguments
specifier|public
name|void
name|testIllegalArguments
parameter_list|()
block|{
try|try
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
operator|new
name|CommonTermsQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|CommonTermsQueryBuilder
argument_list|(
literal|""
argument_list|,
literal|"text"
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"must be non null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// okay
block|}
try|try
block|{
operator|new
name|CommonTermsQueryBuilder
argument_list|(
literal|"fieldName"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must be non null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// okay
block|}
block|}
DECL|method|testNoTermsFromQueryString
specifier|public
name|void
name|testNoTermsFromQueryString
parameter_list|()
throws|throws
name|IOException
block|{
name|CommonTermsQueryBuilder
name|builder
init|=
operator|new
name|CommonTermsQueryBuilder
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
literal|""
argument_list|)
decl_stmt|;
name|QueryShardContext
name|context
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|context
operator|.
name|setAllowUnmappedFields
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|builder
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCommonTermsQuery1
specifier|public
name|void
name|testCommonTermsQuery1
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/commonTerms-query1.json"
argument_list|)
decl_stmt|;
name|Query
name|parsedQuery
init|=
name|parseQuery
argument_list|(
name|query
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ExtendedCommonTermsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ExtendedCommonTermsQuery
name|ectQuery
init|=
operator|(
name|ExtendedCommonTermsQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|ectQuery
operator|.
name|getHighFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ectQuery
operator|.
name|getLowFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCommonTermsQuery2
specifier|public
name|void
name|testCommonTermsQuery2
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/commonTerms-query2.json"
argument_list|)
decl_stmt|;
name|Query
name|parsedQuery
init|=
name|parseQuery
argument_list|(
name|query
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ExtendedCommonTermsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ExtendedCommonTermsQuery
name|ectQuery
init|=
operator|(
name|ExtendedCommonTermsQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|ectQuery
operator|.
name|getHighFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"50%"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ectQuery
operator|.
name|getLowFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"5<20%"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testCommonTermsQuery3
specifier|public
name|void
name|testCommonTermsQuery3
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
name|copyToStringFromClasspath
argument_list|(
literal|"/org/elasticsearch/index/query/commonTerms-query3.json"
argument_list|)
decl_stmt|;
name|Query
name|parsedQuery
init|=
name|parseQuery
argument_list|(
name|query
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ExtendedCommonTermsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ExtendedCommonTermsQuery
name|ectQuery
init|=
operator|(
name|ExtendedCommonTermsQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|ectQuery
operator|.
name|getHighFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|nullValue
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|ectQuery
operator|.
name|getLowFreqMinimumNumberShouldMatchSpec
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|"2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// see #11730
DECL|method|testCommonTermsQuery4
specifier|public
name|void
name|testCommonTermsQuery4
parameter_list|()
throws|throws
name|IOException
block|{
name|boolean
name|disableCoord
init|=
name|randomBoolean
argument_list|()
decl_stmt|;
name|Query
name|parsedQuery
init|=
name|parseQuery
argument_list|(
name|commonTermsQuery
argument_list|(
literal|"field"
argument_list|,
literal|"text"
argument_list|)
operator|.
name|disableCoord
argument_list|(
name|disableCoord
argument_list|)
operator|.
name|buildAsBytes
argument_list|()
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|ExtendedCommonTermsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|ExtendedCommonTermsQuery
name|ectQuery
init|=
operator|(
name|ExtendedCommonTermsQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|ectQuery
operator|.
name|isCoordDisabled
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|disableCoord
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

