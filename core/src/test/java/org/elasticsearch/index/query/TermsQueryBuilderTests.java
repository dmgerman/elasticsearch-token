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
name|MatchNoDocsQuery
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
name|util
operator|.
name|CollectionUtil
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchException
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
name|get
operator|.
name|GetRequest
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
name|get
operator|.
name|GetResponse
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
name|ParsingException
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
name|bytes
operator|.
name|BytesArray
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
name|XContentBuilder
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
name|get
operator|.
name|GetResult
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|indices
operator|.
name|TermsLookup
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
name|internal
operator|.
name|SearchContext
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
name|AbstractQueryTestCase
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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|instanceOf
import|;
end_import

begin_class
DECL|class|TermsQueryBuilderTests
specifier|public
class|class
name|TermsQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|TermsQueryBuilder
argument_list|>
block|{
DECL|field|randomTerms
specifier|private
name|List
argument_list|<
name|Object
argument_list|>
name|randomTerms
decl_stmt|;
DECL|field|termsPath
specifier|private
name|String
name|termsPath
decl_stmt|;
annotation|@
name|Before
DECL|method|randomTerms
specifier|public
name|void
name|randomTerms
parameter_list|()
block|{
name|List
argument_list|<
name|Object
argument_list|>
name|randomTerms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|String
index|[]
name|strings
init|=
name|generateRandomStringArray
argument_list|(
literal|10
argument_list|,
literal|10
argument_list|,
literal|false
argument_list|,
literal|true
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|string
range|:
name|strings
control|)
block|{
name|randomTerms
operator|.
name|add
argument_list|(
name|string
argument_list|)
expr_stmt|;
if|if
condition|(
name|rarely
argument_list|()
condition|)
block|{
name|randomTerms
operator|.
name|add
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|randomTerms
operator|=
name|randomTerms
expr_stmt|;
name|termsPath
operator|=
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
operator|.
name|replace
argument_list|(
literal|'.'
argument_list|,
literal|'_'
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|TermsQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|TermsQueryBuilder
name|query
decl_stmt|;
comment|// terms query or lookup query
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
comment|// make between 0 and 5 different values of the same type
name|String
name|fieldName
decl_stmt|;
do|do
block|{
name|fieldName
operator|=
name|getRandomFieldName
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|fieldName
operator|.
name|equals
argument_list|(
name|GEO_POINT_FIELD_NAME
argument_list|)
operator|||
name|fieldName
operator|.
name|equals
argument_list|(
name|GEO_SHAPE_FIELD_NAME
argument_list|)
condition|)
do|;
name|Object
index|[]
name|values
init|=
operator|new
name|Object
index|[
name|randomInt
argument_list|(
literal|5
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
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|values
index|[
name|i
index|]
operator|=
name|getRandomValueForFieldName
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
block|}
name|query
operator|=
operator|new
name|TermsQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// right now the mock service returns us a list of strings
name|query
operator|=
operator|new
name|TermsQueryBuilder
argument_list|(
name|randomBoolean
argument_list|()
condition|?
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
else|:
name|STRING_FIELD_NAME
argument_list|,
name|randomTermsLookup
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
DECL|method|randomTermsLookup
specifier|private
name|TermsLookup
name|randomTermsLookup
parameter_list|()
block|{
return|return
operator|new
name|TermsLookup
argument_list|(
name|randomBoolean
argument_list|()
condition|?
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
else|:
literal|null
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
argument_list|,
name|termsPath
argument_list|)
operator|.
name|routing
argument_list|(
name|randomBoolean
argument_list|()
condition|?
name|randomAsciiOfLength
argument_list|(
literal|10
argument_list|)
else|:
literal|null
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|TermsQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|SearchContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|queryBuilder
operator|.
name|termsLookup
argument_list|()
operator|==
literal|null
operator|&&
operator|(
name|queryBuilder
operator|.
name|values
argument_list|()
operator|==
literal|null
operator|||
name|queryBuilder
operator|.
name|values
argument_list|()
operator|.
name|isEmpty
argument_list|()
operator|)
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|MatchNoDocsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|MatchNoDocsQuery
name|matchNoDocsQuery
init|=
operator|(
name|MatchNoDocsQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|matchNoDocsQuery
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No terms supplied for \"terms\" query."
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|queryBuilder
operator|.
name|termsLookup
argument_list|()
operator|!=
literal|null
operator|&&
name|randomTerms
operator|.
name|size
argument_list|()
operator|==
literal|0
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|MatchNoDocsQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|MatchNoDocsQuery
name|matchNoDocsQuery
init|=
operator|(
name|MatchNoDocsQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|matchNoDocsQuery
operator|.
name|toString
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No terms supplied for \"terms\" query."
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
name|BooleanQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|BooleanQuery
name|booleanQuery
init|=
operator|(
name|BooleanQuery
operator|)
name|query
decl_stmt|;
comment|// we only do the check below for string fields (otherwise we'd have to decode the values)
if|if
condition|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|INT_FIELD_NAME
argument_list|)
operator|||
name|queryBuilder
operator|.
name|fieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|DOUBLE_FIELD_NAME
argument_list|)
operator|||
name|queryBuilder
operator|.
name|fieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|BOOLEAN_FIELD_NAME
argument_list|)
operator|||
name|queryBuilder
operator|.
name|fieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|DATE_FIELD_NAME
argument_list|)
condition|)
block|{
return|return;
block|}
comment|// expected returned terms depending on whether we have a terms query or a terms lookup query
name|List
argument_list|<
name|Object
argument_list|>
name|terms
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|termsLookup
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|terms
operator|=
name|randomTerms
expr_stmt|;
block|}
else|else
block|{
name|terms
operator|=
name|queryBuilder
operator|.
name|values
argument_list|()
expr_stmt|;
block|}
comment|// compare whether we have the expected list of terms returned
specifier|final
name|List
argument_list|<
name|Term
argument_list|>
name|booleanTerms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|BooleanClause
name|booleanClause
range|:
name|booleanQuery
control|)
block|{
name|assertThat
argument_list|(
name|booleanClause
operator|.
name|getOccur
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|booleanClause
operator|.
name|getQuery
argument_list|()
argument_list|,
name|instanceOf
argument_list|(
name|TermQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Term
name|term
init|=
operator|(
operator|(
name|TermQuery
operator|)
name|booleanClause
operator|.
name|getQuery
argument_list|()
operator|)
operator|.
name|getTerm
argument_list|()
decl_stmt|;
name|booleanTerms
operator|.
name|add
argument_list|(
name|term
argument_list|)
expr_stmt|;
block|}
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|booleanTerms
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Term
argument_list|>
name|expectedTerms
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Object
name|term
range|:
name|terms
control|)
block|{
if|if
condition|(
name|term
operator|!=
literal|null
condition|)
block|{
comment|// terms lookup filters this out
name|expectedTerms
operator|.
name|add
argument_list|(
operator|new
name|Term
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
argument_list|,
name|term
operator|.
name|toString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|CollectionUtil
operator|.
name|timSort
argument_list|(
name|expectedTerms
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedTerms
operator|+
literal|" vs. "
operator|+
name|booleanTerms
argument_list|,
name|expectedTerms
operator|.
name|size
argument_list|()
argument_list|,
name|booleanTerms
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|expectedTerms
operator|+
literal|" vs. "
operator|+
name|booleanTerms
argument_list|,
name|expectedTerms
argument_list|,
name|booleanTerms
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testEmtpyFieldName
specifier|public
name|void
name|testEmtpyFieldName
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|"term"
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"field name cannot be null."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|""
argument_list|,
literal|"term"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"field name cannot be null."
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testEmtpyTermsLookup
specifier|public
name|void
name|testEmtpyTermsLookup
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|TermsLookup
operator|)
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"No value or termsLookup specified for terms query"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNullValues
specifier|public
name|void
name|testNullValues
parameter_list|()
block|{
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|String
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No value specified for terms query"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|int
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No value specified for terms query"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|long
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No value specified for terms query"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|float
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No value specified for terms query"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|double
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No value specified for terms query"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|Object
index|[]
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No value specified for terms query"
argument_list|)
argument_list|)
expr_stmt|;
name|e
operator|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"field"
argument_list|,
operator|(
name|Iterable
argument_list|<
name|?
argument_list|>
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"No value specified for terms query"
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testBothValuesAndLookupSet
specifier|public
name|void
name|testBothValuesAndLookupSet
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"  \"terms\": {\n"
operator|+
literal|"    \"field\": [\n"
operator|+
literal|"      \"blue\",\n"
operator|+
literal|"      \"pill\"\n"
operator|+
literal|"    ],\n"
operator|+
literal|"    \"field_lookup\": {\n"
operator|+
literal|"      \"index\": \"pills\",\n"
operator|+
literal|"      \"type\": \"red\",\n"
operator|+
literal|"      \"id\": \"3\",\n"
operator|+
literal|"      \"path\": \"white rabbit\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|ParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|query
argument_list|)
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
name|containsString
argument_list|(
literal|"["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support more than one field."
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|executeGet
specifier|public
name|GetResponse
name|executeGet
parameter_list|(
name|GetRequest
name|getRequest
parameter_list|)
block|{
name|String
name|json
decl_stmt|;
try|try
block|{
name|XContentBuilder
name|builder
init|=
name|XContentFactory
operator|.
name|jsonBuilder
argument_list|()
operator|.
name|prettyPrint
argument_list|()
decl_stmt|;
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|array
argument_list|(
name|termsPath
argument_list|,
name|randomTerms
operator|.
name|toArray
argument_list|(
operator|new
name|Object
index|[
name|randomTerms
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|json
operator|=
name|builder
operator|.
name|string
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ex
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"boom"
argument_list|,
name|ex
argument_list|)
throw|;
block|}
return|return
operator|new
name|GetResponse
argument_list|(
operator|new
name|GetResult
argument_list|(
name|getRequest
operator|.
name|index
argument_list|()
argument_list|,
name|getRequest
operator|.
name|type
argument_list|()
argument_list|,
name|getRequest
operator|.
name|id
argument_list|()
argument_list|,
literal|0
argument_list|,
literal|true
argument_list|,
operator|new
name|BytesArray
argument_list|(
name|json
argument_list|)
argument_list|,
literal|null
argument_list|)
argument_list|)
return|;
block|}
DECL|method|testNumeric
specifier|public
name|void
name|testNumeric
parameter_list|()
throws|throws
name|IOException
block|{
block|{
name|TermsQueryBuilder
name|builder
init|=
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|int
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|4
block|}
argument_list|)
decl_stmt|;
name|TermsQueryBuilder
name|copy
init|=
operator|(
name|TermsQueryBuilder
operator|)
name|assertSerialization
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|copy
operator|.
name|values
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1
argument_list|,
literal|3
argument_list|,
literal|4
argument_list|)
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
block|{
name|TermsQueryBuilder
name|builder
init|=
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|double
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|4
block|}
argument_list|)
decl_stmt|;
name|TermsQueryBuilder
name|copy
init|=
operator|(
name|TermsQueryBuilder
operator|)
name|assertSerialization
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|copy
operator|.
name|values
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1d
argument_list|,
literal|3d
argument_list|,
literal|4d
argument_list|)
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
block|{
name|TermsQueryBuilder
name|builder
init|=
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|float
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|4
block|}
argument_list|)
decl_stmt|;
name|TermsQueryBuilder
name|copy
init|=
operator|(
name|TermsQueryBuilder
operator|)
name|assertSerialization
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|copy
operator|.
name|values
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1f
argument_list|,
literal|3f
argument_list|,
literal|4f
argument_list|)
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
block|{
name|TermsQueryBuilder
name|builder
init|=
operator|new
name|TermsQueryBuilder
argument_list|(
literal|"foo"
argument_list|,
operator|new
name|long
index|[]
block|{
literal|1
block|,
literal|3
block|,
literal|4
block|}
argument_list|)
decl_stmt|;
name|TermsQueryBuilder
name|copy
init|=
operator|(
name|TermsQueryBuilder
operator|)
name|assertSerialization
argument_list|(
name|builder
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Object
argument_list|>
name|values
init|=
name|copy
operator|.
name|values
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|1L
argument_list|,
literal|3L
argument_list|,
literal|4L
argument_list|)
argument_list|,
name|values
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testTermsQueryWithMultipleFields
specifier|public
name|void
name|testTermsQueryWithMultipleFields
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|query
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
literal|"terms"
argument_list|)
operator|.
name|array
argument_list|(
literal|"foo"
argument_list|,
literal|123
argument_list|)
operator|.
name|array
argument_list|(
literal|"bar"
argument_list|,
literal|456
argument_list|)
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
name|ParsingException
name|e
init|=
name|expectThrows
argument_list|(
name|ParsingException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|query
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"["
operator|+
name|TermsQueryBuilder
operator|.
name|NAME
operator|+
literal|"] query does not support multiple fields"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testFromJson
specifier|public
name|void
name|testFromJson
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"terms\" : {\n"
operator|+
literal|"    \"user\" : [ \"kimchy\", \"elasticsearch\" ],\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|TermsQueryBuilder
name|parsed
init|=
operator|(
name|TermsQueryBuilder
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
name|parsed
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|2
argument_list|,
name|parsed
operator|.
name|values
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|deprecatedJson
init|=
literal|"{\n"
operator|+
literal|"  \"in\" : {\n"
operator|+
literal|"    \"user\" : [ \"kimchy\", \"elasticsearch\" ],\n"
operator|+
literal|"    \"boost\" : 1.0\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|QueryBuilder
name|inShortcutParsed
init|=
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
name|inShortcutParsed
argument_list|,
name|equalTo
argument_list|(
name|parsed
argument_list|)
argument_list|)
expr_stmt|;
name|IllegalArgumentException
name|e
init|=
name|expectThrows
argument_list|(
name|IllegalArgumentException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|parseQuery
argument_list|(
name|deprecatedJson
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Deprecated field [in] used, expected [terms] instead"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testMustRewrite
specifier|public
name|void
name|testMustRewrite
parameter_list|()
throws|throws
name|IOException
block|{
name|TermsQueryBuilder
name|termsQueryBuilder
init|=
operator|new
name|TermsQueryBuilder
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
name|randomTermsLookup
argument_list|()
argument_list|)
decl_stmt|;
name|UnsupportedOperationException
name|e
init|=
name|expectThrows
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|termsQueryBuilder
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"query must be rewritten first"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|termsQueryBuilder
operator|.
name|rewrite
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
argument_list|,
operator|new
name|TermsQueryBuilder
argument_list|(
name|STRING_FIELD_NAME
argument_list|,
name|randomTerms
operator|.
name|stream
argument_list|()
operator|.
name|filter
argument_list|(
name|x
lambda|->
name|x
operator|!=
literal|null
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// terms lookup removes null values
block|}
DECL|method|testGeo
specifier|public
name|void
name|testGeo
parameter_list|()
throws|throws
name|Exception
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
name|TermsQueryBuilder
name|query
init|=
operator|new
name|TermsQueryBuilder
argument_list|(
name|GEO_POINT_FIELD_NAME
argument_list|,
literal|"2,3"
argument_list|)
decl_stmt|;
name|QueryShardContext
name|context
init|=
name|createShardContext
argument_list|()
decl_stmt|;
name|QueryShardException
name|e
init|=
name|expectThrows
argument_list|(
name|QueryShardException
operator|.
name|class
argument_list|,
parameter_list|()
lambda|->
name|query
operator|.
name|toQuery
argument_list|(
name|context
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Geo fields do not support exact searching, use dedicated geo queries instead: [mapped_geo_point]"
argument_list|,
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

