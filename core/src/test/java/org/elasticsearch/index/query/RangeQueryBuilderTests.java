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
name|search
operator|.
name|NumericRangeQuery
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
name|TermRangeQuery
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|lucene
operator|.
name|BytesRefs
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
name|DateTime
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
name|DateTimeZone
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
name|HashMap
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
name|rangeQuery
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

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|is
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
name|lessThanOrEqualTo
import|;
end_import

begin_class
DECL|class|RangeQueryBuilderTests
specifier|public
class|class
name|RangeQueryBuilderTests
extends|extends
name|AbstractQueryTestCase
argument_list|<
name|RangeQueryBuilder
argument_list|>
block|{
annotation|@
name|Override
DECL|method|doCreateTestQueryBuilder
specifier|protected
name|RangeQueryBuilder
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|RangeQueryBuilder
name|query
decl_stmt|;
comment|// switch between numeric and date ranges
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|2
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
comment|// use mapped integer field for numeric range queries
name|query
operator|=
operator|new
name|RangeQueryBuilder
argument_list|(
name|INT_FIELD_NAME
argument_list|)
expr_stmt|;
name|query
operator|.
name|from
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|.
name|to
argument_list|(
name|randomIntBetween
argument_list|(
literal|101
argument_list|,
literal|200
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1
case|:
comment|// use mapped date field, using date string representation
name|query
operator|=
operator|new
name|RangeQueryBuilder
argument_list|(
name|DATE_FIELD_NAME
argument_list|)
expr_stmt|;
name|query
operator|.
name|from
argument_list|(
operator|new
name|DateTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|1000000
argument_list|)
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|query
operator|.
name|to
argument_list|(
operator|new
name|DateTime
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|1000000
argument_list|)
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
comment|// Create timestamp option only then we have a date mapper,
comment|// otherwise we could trigger exception.
if|if
condition|(
name|createShardContext
argument_list|()
operator|.
name|getMapperService
argument_list|()
operator|.
name|fullName
argument_list|(
name|DATE_FIELD_NAME
argument_list|)
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|timeZone
argument_list|(
name|randomTimeZone
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
name|format
argument_list|(
literal|"yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
argument_list|)
expr_stmt|;
block|}
block|}
break|break;
case|case
literal|2
case|:
default|default:
name|query
operator|=
operator|new
name|RangeQueryBuilder
argument_list|(
name|STRING_FIELD_NAME
argument_list|)
expr_stmt|;
name|query
operator|.
name|from
argument_list|(
literal|"a"
operator|+
name|randomAsciiOfLengthBetween
argument_list|(
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
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
break|break;
block|}
name|query
operator|.
name|includeLower
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
operator|.
name|includeUpper
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|query
operator|.
name|from
argument_list|(
literal|null
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
name|to
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
return|return
name|query
return|;
block|}
annotation|@
name|Override
DECL|method|getAlternateVersions
specifier|protected
name|Map
argument_list|<
name|String
argument_list|,
name|RangeQueryBuilder
argument_list|>
name|getAlternateVersions
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|RangeQueryBuilder
argument_list|>
name|alternateVersions
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|RangeQueryBuilder
name|rangeQueryBuilder
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
name|INT_FIELD_NAME
argument_list|)
decl_stmt|;
name|rangeQueryBuilder
operator|.
name|from
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
argument_list|)
operator|.
name|to
argument_list|(
name|randomIntBetween
argument_list|(
literal|101
argument_list|,
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|rangeQueryBuilder
operator|.
name|includeLower
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|rangeQueryBuilder
operator|.
name|includeUpper
argument_list|(
name|randomBoolean
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"range\":{\n"
operator|+
literal|"        \""
operator|+
name|INT_FIELD_NAME
operator|+
literal|"\": {\n"
operator|+
literal|"            \""
operator|+
operator|(
name|rangeQueryBuilder
operator|.
name|includeLower
argument_list|()
condition|?
literal|"gte"
else|:
literal|"gt"
operator|)
operator|+
literal|"\": "
operator|+
name|rangeQueryBuilder
operator|.
name|from
argument_list|()
operator|+
literal|",\n"
operator|+
literal|"            \""
operator|+
operator|(
name|rangeQueryBuilder
operator|.
name|includeUpper
argument_list|()
condition|?
literal|"lte"
else|:
literal|"lt"
operator|)
operator|+
literal|"\": "
operator|+
name|rangeQueryBuilder
operator|.
name|to
argument_list|()
operator|+
literal|"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
decl_stmt|;
name|alternateVersions
operator|.
name|put
argument_list|(
name|query
argument_list|,
name|rangeQueryBuilder
argument_list|)
expr_stmt|;
return|return
name|alternateVersions
return|;
block|}
annotation|@
name|Override
DECL|method|doAssertLuceneQuery
specifier|protected
name|void
name|doAssertLuceneQuery
parameter_list|(
name|RangeQueryBuilder
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
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|==
literal|0
operator|||
operator|(
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
operator|&&
name|queryBuilder
operator|.
name|fieldName
argument_list|()
operator|.
name|equals
argument_list|(
name|INT_FIELD_NAME
argument_list|)
operator|==
literal|false
operator|)
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|TermRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|TermRangeQuery
name|termRangeQuery
init|=
operator|(
name|TermRangeQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|termRangeQuery
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termRangeQuery
operator|.
name|getLowerTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|queryBuilder
operator|.
name|from
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termRangeQuery
operator|.
name|getUpperTerm
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|queryBuilder
operator|.
name|to
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termRangeQuery
operator|.
name|includesLower
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|includeLower
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|termRangeQuery
operator|.
name|includesUpper
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|includeUpper
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
elseif|else
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
condition|)
block|{
comment|//we can't properly test unmapped dates because LateParsingQuery is package private
block|}
elseif|else
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
condition|)
block|{
name|assertThat
argument_list|(
name|query
argument_list|,
name|instanceOf
argument_list|(
name|NumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|NumericRangeQuery
name|numericRangeQuery
init|=
operator|(
name|NumericRangeQuery
operator|)
name|query
decl_stmt|;
name|assertThat
argument_list|(
name|numericRangeQuery
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|numericRangeQuery
operator|.
name|getMin
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|from
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|numericRangeQuery
operator|.
name|getMax
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|to
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|numericRangeQuery
operator|.
name|includesMin
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|includeLower
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|numericRangeQuery
operator|.
name|includesMax
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|queryBuilder
operator|.
name|includeUpper
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
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
name|RangeQueryBuilder
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
operator|new
name|RangeQueryBuilder
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"cannot be null or empty"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
name|RangeQueryBuilder
name|rangeQueryBuilder
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
literal|"test"
argument_list|)
decl_stmt|;
try|try
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|rangeQueryBuilder
operator|.
name|timeZone
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rangeQueryBuilder
operator|.
name|timeZone
argument_list|(
literal|"badID"
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"cannot be null or unknown id"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
try|try
block|{
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|rangeQueryBuilder
operator|.
name|format
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rangeQueryBuilder
operator|.
name|format
argument_list|(
literal|"badFormat"
argument_list|)
expr_stmt|;
block|}
name|fail
argument_list|(
literal|"cannot be null or bad format"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
comment|// expected
block|}
block|}
comment|/**      * Specifying a timezone together with a numeric range query should throw an exception.      */
DECL|method|testToQueryNonDateWithTimezone
specifier|public
name|void
name|testToQueryNonDateWithTimezone
parameter_list|()
throws|throws
name|QueryShardException
throws|,
name|IOException
block|{
name|RangeQueryBuilder
name|query
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
name|INT_FIELD_NAME
argument_list|)
decl_stmt|;
name|query
operator|.
name|from
argument_list|(
literal|1
argument_list|)
operator|.
name|to
argument_list|(
literal|10
argument_list|)
operator|.
name|timeZone
argument_list|(
literal|"UTC"
argument_list|)
expr_stmt|;
try|try
block|{
name|query
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
literal|"[range] time_zone can not be applied"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Specifying a timezone together with an unmapped field should throw an exception.      */
DECL|method|testToQueryUnmappedWithTimezone
specifier|public
name|void
name|testToQueryUnmappedWithTimezone
parameter_list|()
throws|throws
name|QueryShardException
throws|,
name|IOException
block|{
name|RangeQueryBuilder
name|query
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
literal|"bogus_field"
argument_list|)
decl_stmt|;
name|query
operator|.
name|from
argument_list|(
literal|1
argument_list|)
operator|.
name|to
argument_list|(
literal|10
argument_list|)
operator|.
name|timeZone
argument_list|(
literal|"UTC"
argument_list|)
expr_stmt|;
try|try
block|{
name|query
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
literal|"[range] time_zone can not be applied"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|testToQueryNumericField
specifier|public
name|void
name|testToQueryNumericField
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
name|Query
name|parsedQuery
init|=
name|rangeQuery
argument_list|(
name|INT_FIELD_NAME
argument_list|)
operator|.
name|from
argument_list|(
literal|23
argument_list|)
operator|.
name|to
argument_list|(
literal|54
argument_list|)
operator|.
name|includeLower
argument_list|(
literal|true
argument_list|)
operator|.
name|includeUpper
argument_list|(
literal|false
argument_list|)
operator|.
name|toQuery
argument_list|(
name|createShardContext
argument_list|()
argument_list|)
decl_stmt|;
comment|// since age is automatically registered in data, we encode it as numeric
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|NumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|NumericRangeQuery
name|rangeQuery
init|=
operator|(
name|NumericRangeQuery
operator|)
name|parsedQuery
decl_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|getField
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|INT_FIELD_NAME
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|getMin
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|23
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|getMax
argument_list|()
operator|.
name|intValue
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|54
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|includesMin
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|includesMax
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testDateRangeQueryFormat
specifier|public
name|void
name|testDateRangeQueryFormat
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
comment|// We test 01/01/2012 from gte and 2030 for lt
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"range\" : {\n"
operator|+
literal|"        \""
operator|+
name|DATE_FIELD_NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"            \"gte\": \"01/01/2012\",\n"
operator|+
literal|"            \"lt\": \"2030\",\n"
operator|+
literal|"            \"format\": \"dd/MM/yyyy||yyyy\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
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
operator|.
name|rewrite
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|NumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// Min value was 01/01/2012 (dd/MM/yyyy)
name|DateTime
name|min
init|=
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2012-01-01T00:00:00.000+00"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NumericRangeQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getMin
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|min
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Max value was 2030 (yyyy)
name|DateTime
name|max
init|=
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2030-01-01T00:00:00.000+00"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NumericRangeQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getMax
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|max
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test Invalid format
name|query
operator|=
literal|"{\n"
operator|+
literal|"    \"range\" : {\n"
operator|+
literal|"        \""
operator|+
name|DATE_FIELD_NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"            \"gte\": \"01/01/2012\",\n"
operator|+
literal|"            \"lt\": \"2030\",\n"
operator|+
literal|"            \"format\": \"yyyy\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
expr_stmt|;
try|try
block|{
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
operator|.
name|rewrite
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"A Range Query with a specific format but with an unexpected date should raise a ParsingException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ElasticsearchParseException
name|e
parameter_list|)
block|{
comment|// We expect it
block|}
block|}
DECL|method|testDateRangeBoundaries
specifier|public
name|void
name|testDateRangeBoundaries
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
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"range\" : {\n"
operator|+
literal|"        \""
operator|+
name|DATE_FIELD_NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"            \"gte\": \"2014-11-05||/M\",\n"
operator|+
literal|"            \"lte\": \"2014-12-08||/d\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}\n"
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
operator|.
name|rewrite
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|NumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|NumericRangeQuery
name|rangeQuery
init|=
operator|(
name|NumericRangeQuery
operator|)
name|parsedQuery
decl_stmt|;
name|DateTime
name|min
init|=
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2014-11-01T00:00:00.000+00"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|getMin
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|min
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rangeQuery
operator|.
name|includesMin
argument_list|()
argument_list|)
expr_stmt|;
name|DateTime
name|max
init|=
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2014-12-08T23:59:59.999+00"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|getMax
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|max
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rangeQuery
operator|.
name|includesMax
argument_list|()
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"{\n"
operator|+
literal|"    \"range\" : {\n"
operator|+
literal|"        \""
operator|+
name|DATE_FIELD_NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"            \"gt\": \"2014-11-05||/M\",\n"
operator|+
literal|"            \"lt\": \"2014-12-08||/d\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
expr_stmt|;
name|parsedQuery
operator|=
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
operator|.
name|rewrite
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|NumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|rangeQuery
operator|=
operator|(
name|NumericRangeQuery
operator|)
name|parsedQuery
expr_stmt|;
name|min
operator|=
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2014-11-30T23:59:59.999+00"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|getMin
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|min
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|rangeQuery
operator|.
name|includesMin
argument_list|()
argument_list|)
expr_stmt|;
name|max
operator|=
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2014-12-08T00:00:00.000+00"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQuery
operator|.
name|getMax
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|max
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|rangeQuery
operator|.
name|includesMax
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testDateRangeQueryTimezone
specifier|public
name|void
name|testDateRangeQueryTimezone
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
name|long
name|startDate
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|String
name|query
init|=
literal|"{\n"
operator|+
literal|"    \"range\" : {\n"
operator|+
literal|"        \""
operator|+
name|DATE_FIELD_NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"            \"gte\": \"2012-01-01\",\n"
operator|+
literal|"            \"lte\": \"now\",\n"
operator|+
literal|"            \"time_zone\": \"+01:00\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
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
operator|.
name|rewrite
argument_list|(
literal|null
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|parsedQuery
argument_list|,
name|instanceOf
argument_list|(
name|NumericRangeQuery
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
comment|// Min value was 2012-01-01 (UTC) so we need to remove one hour
name|DateTime
name|min
init|=
name|DateTime
operator|.
name|parse
argument_list|(
literal|"2012-01-01T00:00:00.000+01:00"
argument_list|)
decl_stmt|;
comment|// Max value is when we started the test. So it should be some ms from now
name|DateTime
name|max
init|=
operator|new
name|DateTime
argument_list|(
name|startDate
argument_list|,
name|DateTimeZone
operator|.
name|UTC
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
operator|(
operator|(
name|NumericRangeQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getMin
argument_list|()
operator|.
name|longValue
argument_list|()
argument_list|,
name|is
argument_list|(
name|min
operator|.
name|getMillis
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
comment|// We should not have a big difference here (should be some ms)
name|assertThat
argument_list|(
operator|(
operator|(
name|NumericRangeQuery
operator|)
name|parsedQuery
operator|)
operator|.
name|getMax
argument_list|()
operator|.
name|longValue
argument_list|()
operator|-
name|max
operator|.
name|getMillis
argument_list|()
argument_list|,
name|lessThanOrEqualTo
argument_list|(
literal|60000L
argument_list|)
argument_list|)
expr_stmt|;
name|query
operator|=
literal|"{\n"
operator|+
literal|"    \"range\" : {\n"
operator|+
literal|"        \""
operator|+
name|INT_FIELD_NAME
operator|+
literal|"\" : {\n"
operator|+
literal|"            \"gte\": \"0\",\n"
operator|+
literal|"            \"lte\": \"100\",\n"
operator|+
literal|"            \"time_zone\": \"-01:00\"\n"
operator|+
literal|"        }\n"
operator|+
literal|"    }\n"
operator|+
literal|"}"
expr_stmt|;
try|try
block|{
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
expr_stmt|;
name|fail
argument_list|(
literal|"A Range Query on a numeric field with a TimeZone should raise a ParsingException"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|QueryShardException
name|e
parameter_list|)
block|{
comment|// We expect it
block|}
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
literal|"  \"range\" : {\n"
operator|+
literal|"    \"timestamp\" : {\n"
operator|+
literal|"      \"from\" : \"2015-01-01 00:00:00\",\n"
operator|+
literal|"      \"to\" : \"now\",\n"
operator|+
literal|"      \"include_lower\" : true,\n"
operator|+
literal|"      \"include_upper\" : true,\n"
operator|+
literal|"      \"time_zone\" : \"+01:00\",\n"
operator|+
literal|"      \"boost\" : 1.0\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|RangeQueryBuilder
name|parsed
init|=
operator|(
name|RangeQueryBuilder
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
literal|"2015-01-01 00:00:00"
argument_list|,
name|parsed
operator|.
name|from
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|json
argument_list|,
literal|"now"
argument_list|,
name|parsed
operator|.
name|to
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testNamedQueryParsing
specifier|public
name|void
name|testNamedQueryParsing
parameter_list|()
throws|throws
name|IOException
block|{
name|String
name|json
init|=
literal|"{\n"
operator|+
literal|"  \"range\" : {\n"
operator|+
literal|"    \"timestamp\" : {\n"
operator|+
literal|"      \"from\" : \"2015-01-01 00:00:00\",\n"
operator|+
literal|"      \"to\" : \"now\",\n"
operator|+
literal|"      \"boost\" : 1.0,\n"
operator|+
literal|"      \"_name\" : \"my_range\"\n"
operator|+
literal|"    }\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
decl_stmt|;
name|assertNotNull
argument_list|(
name|parseQuery
argument_list|(
name|json
argument_list|)
argument_list|)
expr_stmt|;
name|json
operator|=
literal|"{\n"
operator|+
literal|"  \"range\" : {\n"
operator|+
literal|"    \"timestamp\" : {\n"
operator|+
literal|"      \"from\" : \"2015-01-01 00:00:00\",\n"
operator|+
literal|"      \"to\" : \"now\",\n"
operator|+
literal|"      \"boost\" : 1.0\n"
operator|+
literal|"    },\n"
operator|+
literal|"    \"_name\" : \"my_range\"\n"
operator|+
literal|"  }\n"
operator|+
literal|"}"
expr_stmt|;
comment|// non strict parsing should accept "_name" on top level
name|assertNotNull
argument_list|(
name|parseQuery
argument_list|(
name|json
argument_list|,
name|ParseFieldMatcher
operator|.
name|EMPTY
argument_list|)
argument_list|)
expr_stmt|;
comment|// with strict parsing, ParseField will throw exception
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
literal|"Strict parsing should trigger exception for '_name' on top level"
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
name|equalTo
argument_list|(
literal|"Deprecated field [_name] used, replaced by [query name is not supported in short version of range query]"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

