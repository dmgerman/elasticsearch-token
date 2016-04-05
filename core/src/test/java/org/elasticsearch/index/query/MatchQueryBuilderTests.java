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
name|BooleanClause
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
name|BooleanQuery
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
name|FuzzyQuery
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
name|LegacyNumericRangeQuery
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
name|PhraseQuery
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
name|ParseFieldMatcher
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
name|Strings
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
name|MultiPhrasePrefixQuery
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
name|common
operator|.
name|unit
operator|.
name|Fuzziness
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
name|search
operator|.
name|MatchQuery
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
name|search
operator|.
name|MatchQuery
operator|.
name|Type
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
name|search
operator|.
name|MatchQuery
operator|.
name|ZeroTermsQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matcher
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
name|format
operator|.
name|ISODateTimeFormat
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
name|Locale
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
name|either
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|containsString
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
name|notNullValue
import|;
end_import

begin_class
DECL|class|MatchQueryBuilderTests
specifier|public
class|class
name|MatchQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|MatchQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|MatchQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|String
name|fieldName
init|=
name|randomFrom
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
name|BOOLEAN_FIELD_NAME
argument_list|,
name|INT_FIELD_NAME
argument_list|,
name|DOUBLE_FIELD_NAME
argument_list|,
name|DATE_FIELD_NAME
argument_list|)
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|DATE_FIELD_NAME
argument_list|)
condition|)
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
block|}
name|Object
name|value
decl_stmt|;
if|if
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|STRING_FIELD_NAME
argument_list|)
condition|)
block|{
name|int
name|terms
init|=
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|StringBuilder
name|builder
init|=
operator|new
name|StringBuilder
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
name|terms
condition|;
name|i
operator|++
control|)
block|{
name|builder
operator|.
name|append
argument_list|(
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
operator|.
name|append
argument_list|(
literal|" "
argument_list|)
expr_stmt|;
block|}
name|value
operator|=
name|builder
operator|.
name|toString
argument_list|()
operator|.
name|trim
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|value
operator|=
name|getRandomValueForFieldName
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
name|MatchQueryBuilder
name|matchQuery
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|value
argument_list|)
decl_stmt|;
name|matchQuery
operator|.
name|operator
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
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|matchQuery
operator|.
name|analyzer
argument_list|(
name|randomFrom
argument_list|(
literal|"simple"
argument_list|,
literal|"keyword"
argument_list|,
literal|"whitespace"
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
name|matchQuery
operator|.
name|fuzziness
argument_list|(
name|randomFuzziness
argument_list|(
name|fieldName
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
name|matchQuery
operator|.
name|prefixLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
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
name|matchQuery
operator|.
name|maxExpansions
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|1000
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
name|matchQuery
operator|.
name|minimumShouldMatch
argument_list|(
name|randomMinimumShouldMatch
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
name|matchQuery
operator|.
name|fuzzyRewrite
argument_list|(
name|getRandomRewriteMethod
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
name|matchQuery
operator|.
name|fuzzyTranspositions
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
name|matchQuery
operator|.
name|lenient
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
name|matchQuery
operator|.
name|zeroTermsQuery
argument_list|(
name|randomFrom
argument_list|(
name|MatchQuery
operator|.
name|ZeroTermsQuery
operator|.
name|values
argument_list|()
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
name|matchQuery
operator|.
name|cutoffFrequency
argument_list|(
operator|(
name|float
operator|)
literal|10
operator|/
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|matchQuery
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|MatchQueryBuilder
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
name|notNullValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|query
operator|instanceof
name|MatchAllDocsQuery
condition|)
block|{
name|assertThat
argument_list|(
name|queryBuilder
operator|.
name|zeroTermsQuery
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|ZeroTermsQuery
operator|.
name|ALL
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
switch|switch
condition|(
name|queryBuilder
operator|.
name|type
argument_list|()
condition|)
block|{
case|case
name|BOOLEAN
case|:
name|assertThat
argument_list|(
name|query
argument_list|,
name|either
argument_list|(
name|instanceOf
argument_list|(
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|ExtendedCommonTermsQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|FuzzyQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|LegacyNumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|PHRASE
case|:
name|assertThat
argument_list|(
name|query
argument_list|,
name|either
argument_list|(
name|instanceOf
argument_list|(
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|PhraseQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|FuzzyQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|LegacyNumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
name|PHRASE_PREFIX
case|:
name|assertThat
argument_list|(
name|query
argument_list|,
name|either
argument_list|(
name|instanceOf
argument_list|(
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|MultiPhrasePrefixQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|FuzzyQuery
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|or
argument_list|(
name|instanceOf
argument_list|(
name|LegacyNumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|MappedFieldType
name|fieldType
init|=
name|context
operator|.
name|fieldMapper
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|query
operator|instanceof
name|TermQuery
operator|&&
name|fieldType
operator|!=
literal|null
condition|)
block|{
name|String
name|queryValue
init|=
name|queryBuilder
operator|.
name|value
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|analyzer
argument_list|()
operator|==
literal|null
operator|||
name|queryBuilder
operator|.
name|analyzer
argument_list|()
operator|.
name|equals
argument_list|(
literal|"simple"
argument_list|)
condition|)
block|{
name|queryValue
operator|=
name|queryValue
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
expr_stmt|;
block|}
name|Query
name|expectedTermQuery
init|=
name|fieldType
operator|.
name|termQuery
argument_list|(
name|queryValue
argument_list|,
name|context
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|expectedTermQuery
argument_list|,
name|query
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|query
operator|instanceof
name|BooleanQuery
condition|)
block|{
name|BooleanQuery
name|bq
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|minimumShouldMatch
argument_list|()
operator|!=
literal|null
condition|)
block|{
comment|// calculate expected minimumShouldMatch value
name|int
name|optionalClauses
init|=
literal|0
decl_stmt|;
for|for
control|(
name|BooleanClause
name|c
range|:
name|bq
operator|.
name|clauses
argument_list|()
control|)
block|{
if|if
condition|(
name|c
operator|.
name|getOccur
argument_list|()
operator|==
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
condition|)
block|{
name|optionalClauses
operator|++
expr_stmt|;
block|}
block|}
name|int
name|msm
init|=
name|Queries
operator|.
name|calculateMinShouldMatch
argument_list|(
name|optionalClauses
argument_list|,
name|queryBuilder
operator|.
name|minimumShouldMatch
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|bq
operator|.
name|getMinimumNumberShouldMatch
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|msm
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|queryBuilder
operator|.
name|analyzer
argument_list|()
operator|==
literal|null
operator|&&
name|queryBuilder
operator|.
name|value
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|assertEquals
argument_list|(
name|bq
operator|.
name|clauses
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|value
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|split
argument_list|(
literal|" "
argument_list|)
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|query
operator|instanceof
name|ExtendedCommonTermsQuery
condition|)
block|{
name|assertTrue
argument_list|(
name|queryBuilder
operator|.
name|cutoffFrequency
argument_list|()
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|ExtendedCommonTermsQuery
name|ectq
init|=
operator|(
name|ExtendedCommonTermsQuery
operator|)
name|query
decl_stmt|;
name|assertEquals
argument_list|(
name|queryBuilder
operator|.
name|cutoffFrequency
argument_list|()
argument_list|,
name|ectq
operator|.
name|getMaxTermFrequency
argument_list|()
argument_list|,
name|Float
operator|.
name|MIN_VALUE
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|query
operator|instanceof
name|FuzzyQuery
condition|)
block|{
name|assertTrue
argument_list|(
name|queryBuilder
operator|.
name|fuzziness
argument_list|()
operator|!=
literal|null
argument_list|)
expr_stmt|;
name|FuzzyQuery
name|fuzzyQuery
init|=
operator|(
name|FuzzyQuery
operator|)
name|query
decl_stmt|;
comment|// depending on analyzer being set or not we can have term lowercased along the way, so to simplify test we just
comment|// compare lowercased terms here
name|String
name|originalTermLc
init|=
name|queryBuilder
operator|.
name|value
argument_list|()
operator|.
name|toString
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|String
name|actualTermLc
init|=
name|fuzzyQuery
operator|.
name|getTerm
argument_list|()
operator|.
name|text
argument_list|()
operator|.
name|toLowerCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
decl_stmt|;
name|Matcher
argument_list|<
name|String
argument_list|>
name|termLcMatcher
init|=
name|equalTo
argument_list|(
name|originalTermLc
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"false"
operator|.
name|equals
argument_list|(
name|originalTermLc
argument_list|)
operator|||
literal|"true"
operator|.
name|equals
argument_list|(
name|originalTermLc
argument_list|)
condition|)
block|{
comment|// Booleans become t/f when querying a boolean field
name|termLcMatcher
operator|=
name|either
argument_list|(
name|termLcMatcher
argument_list|)
operator|.
name|or
argument_list|(
name|equalTo
argument_list|(
name|originalTermLc
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
literal|1
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertThat
argument_list|(
name|actualTermLc
argument_list|,
name|termLcMatcher
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryBuilder
operator|.
name|prefixLength
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|fuzzyQuery
operator|.
name|getPrefixLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryBuilder
operator|.
name|fuzzyTranspositions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|fuzzyQuery
operator|.
name|getTranspositions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|query
operator|instanceof
name|LegacyNumericRangeQuery
condition|)
block|{
comment|// These are fuzzy numeric queries
name|assertTrue
argument_list|(
name|queryBuilder
operator|.
name|fuzziness
argument_list|()
operator|!=
literal|null
argument_list|)
expr_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
name|LegacyNumericRangeQuery
argument_list|<
name|Number
argument_list|>
name|numericRangeQuery
init|=
operator|(
name|LegacyNumericRangeQuery
argument_list|<
name|Number
argument_list|>
operator|)
name|query
decl_stmt|;
name|assertTrue
argument_list|(
name|numericRangeQuery
operator|.
name|includesMin
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|numericRangeQuery
operator|.
name|includesMax
argument_list|()
argument_list|)
expr_stmt|;
name|double
name|value
decl_stmt|;
name|double
name|width
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|DATE_FIELD_NAME
argument_list|)
operator|==
literal|false
condition|)
block|{
name|value
operator|=
name|Double
operator|.
name|parseDouble
argument_list|(
name|queryBuilder
operator|.
name|value
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|fuzziness
argument_list|()
operator|.
name|equals
argument_list|(
name|Fuzziness
operator|.
name|AUTO
argument_list|)
condition|)
block|{
name|width
operator|=
literal|1
expr_stmt|;
block|}
else|else
block|{
name|width
operator|=
name|queryBuilder
operator|.
name|fuzziness
argument_list|()
operator|.
name|asDouble
argument_list|()
expr_stmt|;
block|}
block|}
else|else
block|{
name|value
operator|=
name|ISODateTimeFormat
operator|.
name|dateTimeParser
argument_list|()
operator|.
name|parseMillis
argument_list|(
name|queryBuilder
operator|.
name|value
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|width
operator|=
name|queryBuilder
operator|.
name|fuzziness
argument_list|()
operator|.
name|asTimeValue
argument_list|()
operator|.
name|getMillis
argument_list|()
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|value
operator|-
name|width
argument_list|,
name|numericRangeQuery
operator|.
name|getMin
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
name|width
operator|*
literal|.1
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|value
operator|+
name|width
argument_list|,
name|numericRangeQuery
operator|.
name|getMax
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|,
name|width
operator|*
literal|.1
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testIllegalValues
specifier|public
name|void
name|testIllegalValues
parameter_list|()
block|{
try|try
block|{
operator|new
name|MatchQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|"value"
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"value must not be non-null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"fieldName"
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"value must not be non-null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
name|MatchQueryBuilder
name|matchQuery
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"fieldName"
argument_list|,
literal|"text"
argument_list|)
decl_stmt|;
try|try
block|{
name|matchQuery
operator|.
name|prefixLength
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be positive"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|matchQuery
operator|.
name|maxExpansions
argument_list|(
operator|-
literal|1
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be positive"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|matchQuery
operator|.
name|operator
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be non-null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|matchQuery
operator|.
name|type
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be non-null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
name|matchQuery
operator|.
name|zeroTermsQuery
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"must not be non-null"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|ex
parameter_list|)
block|{
comment|// expected
block|}
block|}
DECL|method|testBadAnalyzer
specifier|public
name|void
name|testBadAnalyzer
parameter_list|()
throws|throws
name|IOException
block|{
name|MatchQueryBuilder
name|matchQuery
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"fieldName"
argument_list|,
literal|"text"
argument_list|)
decl_stmt|;
name|matchQuery
operator|.
name|analyzer
argument_list|(
literal|"bogusAnalyzer"
argument_list|)
expr_stmt|;
try|try
block|{
name|matchQuery
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected QueryShardException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|QueryShardException
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
name|containsString
argument_list|(
literal|"analyzer [bogusAnalyzer] not found"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testSimpleMatchQuery
specifier|public
name|void
name|testSimpleMatchQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"match\" : {\n"
operator|+
literal|"    \"message\" : {\n"
operator|+
literal|"      \"query\" : \"to be or not to be\",\n"
operator|+
literal|"      \"operator\" : \"AND\",\n"
operator|+
literal|"      \"prefix_length\" : 0,\n"
operator|+
literal|"      \"max_expansions\" : 50,\n"
operator|+
literal|"      \"fuzzy_transpositions\" : true,\n"
operator|+
literal|"      \"lenient\" : false,\n"
operator|+
literal|"      \"zero_terms_query\" : \"ALL\",\n"
operator|+
literal|"      \"boost\" : 1.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|MatchQueryBuilder
name|qb
init|=
operator|(
name|MatchQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|)
decl_stmt|;
name|checkGeneratedJson
argument_list|(
name|json
argument_list|,
name|qb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|"to be or not to be"
argument_list|,
name|qb
operator|.
name|value
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
name|Operator
operator|.
name|AND
argument_list|,
name|qb
operator|.
name|operator
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testLegacyMatchPhrasePrefixQuery
specifier|public
name|void
name|testLegacyMatchPhrasePrefixQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|MatchQueryBuilder
name|expectedQB
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"message"
argument_list|,
literal|"to be or not to be"
argument_list|)
decl_stmt|;
name|expectedQB
operator|.
name|type
argument_list|(
name|Type
operator|.
name|PHRASE_PREFIX
argument_list|)
expr_stmt|;
name|expectedQB
operator|.
name|slop
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|expectedQB
operator|.
name|maxExpansions
argument_list|(
literal|30
argument_list|)
expr_stmt|;
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"match\" : {\n"
operator|+
literal|"    \"message\" : {\n"
operator|+
literal|"      \"query\" : \"to be or not to be\",\n"
operator|+
literal|"      \"type\" : \"phrase_prefix\",\n"
operator|+
literal|"      \"operator\" : \"OR\",\n"
operator|+
literal|"      \"slop\" : 2,\n"
operator|+
literal|"      \"prefix_length\" : 0,\n"
operator|+
literal|"      \"max_expansions\" : 30,\n"
operator|+
literal|"      \"fuzzy_transpositions\" : true,\n"
operator|+
literal|"      \"lenient\" : false,\n"
operator|+
literal|"      \"zero_terms_query\" : \"NONE\",\n"
operator|+
literal|"      \"boost\" : 1.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|MatchQueryBuilder
name|qb
init|=
operator|(
name|MatchQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|checkGeneratedJson
argument_list|(
name|json
argument_list|,
name|qb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
name|expectedQB
argument_list|,
name|qb
argument_list|)
expr_stmt|;
name|assertSerialization
argument_list|(
name|qb
argument_list|)
expr_stmt|;
comment|// Now check with strict parsing an exception is thrown
try|try
block|{
name|parseQuery
argument_list|(
name|json
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected query to fail with strict parsing"
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
name|containsString
argument_list|(
literal|"Deprecated field [type] used, replaced by [match_phrase and match_phrase_prefix query]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLegacyMatchPhraseQuery
specifier|public
name|void
name|testLegacyMatchPhraseQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|MatchQueryBuilder
name|expectedQB
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"message"
argument_list|,
literal|"to be or not to be"
argument_list|)
decl_stmt|;
name|expectedQB
operator|.
name|type
argument_list|(
name|Type
operator|.
name|PHRASE
argument_list|)
expr_stmt|;
name|expectedQB
operator|.
name|slop
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"match\" : {\n"
operator|+
literal|"    \"message\" : {\n"
operator|+
literal|"      \"query\" : \"to be or not to be\",\n"
operator|+
literal|"      \"type\" : \"phrase\",\n"
operator|+
literal|"      \"operator\" : \"OR\",\n"
operator|+
literal|"      \"slop\" : 2,\n"
operator|+
literal|"      \"prefix_length\" : 0,\n"
operator|+
literal|"      \"max_expansions\" : 50,\n"
operator|+
literal|"      \"fuzzy_transpositions\" : true,\n"
operator|+
literal|"      \"lenient\" : false,\n"
operator|+
literal|"      \"zero_terms_query\" : \"NONE\",\n"
operator|+
literal|"      \"boost\" : 1.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|MatchQueryBuilder
name|qb
init|=
operator|(
name|MatchQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|checkGeneratedJson
argument_list|(
name|json
argument_list|,
name|qb
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
name|expectedQB
argument_list|,
name|qb
argument_list|)
expr_stmt|;
name|assertSerialization
argument_list|(
name|qb
argument_list|)
expr_stmt|;
comment|// Now check with strict parsing an exception is thrown
try|try
block|{
name|parseQuery
argument_list|(
name|json
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected query to fail with strict parsing"
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
name|containsString
argument_list|(
literal|"Deprecated field [type] used, replaced by [match_phrase and match_phrase_prefix query]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testLegacyFuzzyMatchQuery
specifier|public
name|void
name|testLegacyFuzzyMatchQuery
parameter_list|()
throws|throws
name|IOException
block|{
name|MatchQueryBuilder
name|expectedQB
init|=
operator|new
name|MatchQueryBuilder
argument_list|(
literal|"message"
argument_list|,
literal|"to be or not to be"
argument_list|)
decl_stmt|;
name|String
name|type
init|=
name|randomFrom
argument_list|(
literal|"fuzzy_match"
argument_list|,
literal|"match_fuzzy"
argument_list|)
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|type
operator|=
name|Strings
operator|.
name|toCamelCase
argument_list|(
name|type
argument_list|)
expr_stmt|;
block|}
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \""
operator|+
name|type
operator|+
literal|"\" : {\n"
operator|+
literal|"    \"message\" : {\n"
operator|+
literal|"      \"query\" : \"to be or not to be\",\n"
operator|+
literal|"      \"operator\" : \"OR\",\n"
operator|+
literal|"      \"slop\" : 0,\n"
operator|+
literal|"      \"prefix_length\" : 0,\n"
operator|+
literal|"      \"max_expansions\" : 50,\n"
operator|+
literal|"      \"fuzzy_transpositions\" : true,\n"
operator|+
literal|"      \"lenient\" : false,\n"
operator|+
literal|"      \"zero_terms_query\" : \"NONE\",\n"
operator|+
literal|"      \"boost\" : 1.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|MatchQueryBuilder
name|qb
init|=
operator|(
name|MatchQueryBuilder
operator|)
name|parseQuery
argument_list|(
name|json
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|qb
argument_list|,
name|equalTo
argument_list|(
name|expectedQB
argument_list|)
argument_list|)
expr_stmt|;
comment|// Now check with strict parsing an exception is thrown
try|try
block|{
name|parseQuery
argument_list|(
name|json
argument_list|,
name|ParseFieldMatcher
operator|.
name|STRICT
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Expected query to fail with strict parsing"
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
name|containsString
argument_list|(
literal|"Deprecated field ["
operator|+
name|type
operator|+
literal|"] used, expected [match] instead"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

