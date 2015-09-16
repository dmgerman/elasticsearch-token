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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomInts
import|;
end_import

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomStrings
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_comment
comment|/**  * Utility class for creating random QueryBuilders.  * So far only leaf queries like {@link MatchAllQueryBuilder}, {@link TermQueryBuilder} or  * {@link IdsQueryBuilder} are returned.  */
end_comment

begin_class
DECL|class|RandomQueryBuilder
specifier|public
class|class
name|RandomQueryBuilder
block|{
comment|/**      * Create a new query of a random type      * @param r random seed      * @return a random {@link QueryBuilder}      */
DECL|method|createQuery
specifier|public
specifier|static
name|QueryBuilder
name|createQuery
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
switch|switch
condition|(
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|r
argument_list|,
literal|0
argument_list|,
literal|4
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|MatchAllQueryBuilderTests
argument_list|()
operator|.
name|createTestQueryBuilder
argument_list|()
return|;
case|case
literal|1
case|:
return|return
operator|new
name|TermQueryBuilderTests
argument_list|()
operator|.
name|createTestQueryBuilder
argument_list|()
return|;
case|case
literal|2
case|:
return|return
operator|new
name|IdsQueryBuilderTests
argument_list|()
operator|.
name|createTestQueryBuilder
argument_list|()
return|;
case|case
literal|3
case|:
return|return
name|createMultiTermQuery
argument_list|(
name|r
argument_list|)
return|;
case|case
literal|4
case|:
return|return
name|EmptyQueryBuilder
operator|.
name|PROTOTYPE
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
comment|/**      * Create a new multi term query of a random type      * @param r random seed      * @return a random {@link MultiTermQueryBuilder}      */
DECL|method|createMultiTermQuery
specifier|public
specifier|static
name|MultiTermQueryBuilder
name|createMultiTermQuery
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
comment|// for now, only use String Rangequeries for MultiTerm test, numeric and date makes little sense
comment|// see issue #12123 for discussion
comment|// Prefix / Fuzzy / RegEx / Wildcard can go here later once refactored and they have random query generators
name|RangeQueryBuilder
name|query
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
name|AbstractQueryTestCase
operator|.
name|STRING_FIELD_NAME
argument_list|)
decl_stmt|;
name|query
operator|.
name|from
argument_list|(
literal|"a"
operator|+
name|RandomStrings
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
name|r
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|.
name|to
argument_list|(
literal|"z"
operator|+
name|RandomStrings
operator|.
name|randomAsciiOfLengthBetween
argument_list|(
name|r
argument_list|,
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|query
return|;
block|}
comment|/**      * Create a new invalid query of a random type      * @param r random seed      * @return a random {@link QueryBuilder} that is invalid, meaning that calling validate against it      * will return an error. We can rely on the fact that a single error will be returned per query.      */
DECL|method|createInvalidQuery
specifier|public
specifier|static
name|QueryBuilder
name|createInvalidQuery
parameter_list|(
name|Random
name|r
parameter_list|)
block|{
switch|switch
condition|(
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|r
argument_list|,
literal|0
argument_list|,
literal|1
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|TermQueryBuilder
argument_list|(
literal|""
argument_list|,
literal|"test"
argument_list|)
return|;
case|case
literal|1
case|:
return|return
operator|new
name|SimpleQueryStringBuilder
argument_list|(
literal|null
argument_list|)
return|;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
block|}
block|}
end_class

end_unit

