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
name|Version
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|Nullable
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
name|joda
operator|.
name|DateMathParser
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
name|joda
operator|.
name|Joda
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
name|index
operator|.
name|mapper
operator|.
name|ContentPath
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
name|FieldMapper
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
name|Mapper
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
name|core
operator|.
name|DateFieldMapper
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
name|core
operator|.
name|DateFieldMapper
operator|.
name|DateFieldType
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
name|List
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
name|is
import|;
end_import

begin_class
DECL|class|RangeQueryBuilderTest
specifier|public
class|class
name|RangeQueryBuilderTest
extends|extends
name|BaseQueryTestCase
argument_list|<
name|RangeQueryBuilder
argument_list|>
block|{
DECL|field|TIMEZONE_IDS
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|TIMEZONE_IDS
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|DateTimeZone
operator|.
name|getAvailableIDs
argument_list|()
argument_list|)
decl_stmt|;
annotation|@
name|Override
DECL|method|createTestQueryBuilder
specifier|protected
name|RangeQueryBuilder
name|createTestQueryBuilder
parameter_list|()
block|{
name|RangeQueryBuilder
name|query
decl_stmt|;
comment|// switch between numeric and date ranges
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
block|}
else|else
block|{
comment|// use unmapped field for numeric range queries
name|query
operator|=
operator|new
name|RangeQueryBuilder
argument_list|(
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
name|from
argument_list|(
literal|0.0
operator|-
name|randomDouble
argument_list|()
argument_list|)
expr_stmt|;
name|query
operator|.
name|to
argument_list|(
name|randomDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
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
argument_list|)
operator|.
name|toString
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
name|timeZone
argument_list|(
name|TIMEZONE_IDS
operator|.
name|get
argument_list|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
name|TIMEZONE_IDS
operator|.
name|size
argument_list|()
operator|-
literal|1
argument_list|)
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
name|format
argument_list|(
literal|"yyyy-MM-dd'T'HH:mm:ss.SSSZZ"
argument_list|)
expr_stmt|;
block|}
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
name|boost
argument_list|(
literal|2.0f
operator|/
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|20
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
name|queryName
argument_list|(
name|randomAsciiOfLengthBetween
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
DECL|method|createExpectedQuery
specifier|protected
name|Query
name|createExpectedQuery
parameter_list|(
name|RangeQueryBuilder
name|queryBuilder
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|Query
name|expectedQuery
decl_stmt|;
name|String
name|fieldName
init|=
name|queryBuilder
operator|.
name|fieldName
argument_list|()
decl_stmt|;
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
name|fieldName
operator|.
name|equals
argument_list|(
name|DATE_FIELD_NAME
argument_list|)
operator|==
literal|false
operator|&&
name|fieldName
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
name|expectedQuery
operator|=
operator|new
name|TermRangeQuery
argument_list|(
name|fieldName
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|queryBuilder
operator|.
name|from
argument_list|()
argument_list|)
argument_list|,
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|queryBuilder
operator|.
name|to
argument_list|()
argument_list|)
argument_list|,
name|queryBuilder
operator|.
name|includeLower
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|includeUpper
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
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
name|DateMathParser
name|forcedDateParser
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|format
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|forcedDateParser
operator|=
operator|new
name|DateMathParser
argument_list|(
name|Joda
operator|.
name|forPattern
argument_list|(
name|queryBuilder
operator|.
name|format
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|DateTimeZone
name|dateTimeZone
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|queryBuilder
operator|.
name|timeZone
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|dateTimeZone
operator|=
name|DateTimeZone
operator|.
name|forID
argument_list|(
name|queryBuilder
operator|.
name|timeZone
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|FieldMapper
name|mapper
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
name|expectedQuery
operator|=
operator|(
operator|(
name|DateFieldMapper
operator|)
name|mapper
operator|)
operator|.
name|fieldType
argument_list|()
operator|.
name|rangeQuery
argument_list|(
name|queryBuilder
operator|.
name|from
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|to
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|includeLower
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|includeUpper
argument_list|()
argument_list|,
name|dateTimeZone
argument_list|,
name|forcedDateParser
argument_list|,
name|context
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
name|INT_FIELD_NAME
argument_list|)
condition|)
block|{
name|expectedQuery
operator|=
name|NumericRangeQuery
operator|.
name|newIntRange
argument_list|(
name|INT_FIELD_NAME
argument_list|,
operator|(
name|Integer
operator|)
name|queryBuilder
operator|.
name|from
argument_list|()
argument_list|,
operator|(
name|Integer
operator|)
name|queryBuilder
operator|.
name|to
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|includeLower
argument_list|()
argument_list|,
name|queryBuilder
operator|.
name|includeUpper
argument_list|()
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
name|expectedQuery
operator|.
name|setBoost
argument_list|(
name|queryBuilder
operator|.
name|boost
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|expectedQuery
return|;
block|}
annotation|@
name|Override
DECL|method|assertLuceneQuery
specifier|protected
name|void
name|assertLuceneQuery
parameter_list|(
name|RangeQueryBuilder
name|queryBuilder
parameter_list|,
name|Query
name|query
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
block|{
if|if
condition|(
name|queryBuilder
operator|.
name|queryName
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|Query
name|namedQuery
init|=
name|context
operator|.
name|copyNamedFilters
argument_list|()
operator|.
name|get
argument_list|(
name|queryBuilder
operator|.
name|queryName
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|namedQuery
argument_list|,
name|equalTo
argument_list|(
name|query
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
DECL|method|testValidate
specifier|public
name|void
name|testValidate
parameter_list|()
block|{
name|RangeQueryBuilder
name|rangeQueryBuilder
init|=
operator|new
name|RangeQueryBuilder
argument_list|(
literal|""
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|rangeQueryBuilder
operator|.
name|validate
argument_list|()
operator|.
name|validationErrors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|rangeQueryBuilder
operator|=
operator|new
name|RangeQueryBuilder
argument_list|(
literal|"okay"
argument_list|)
operator|.
name|timeZone
argument_list|(
literal|"UTC"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rangeQueryBuilder
operator|.
name|validate
argument_list|()
argument_list|)
expr_stmt|;
name|rangeQueryBuilder
operator|.
name|timeZone
argument_list|(
literal|"blab"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQueryBuilder
operator|.
name|validate
argument_list|()
operator|.
name|validationErrors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|rangeQueryBuilder
operator|.
name|timeZone
argument_list|(
literal|"UTC"
argument_list|)
operator|.
name|format
argument_list|(
literal|"basicDate"
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|rangeQueryBuilder
operator|.
name|validate
argument_list|()
argument_list|)
expr_stmt|;
name|rangeQueryBuilder
operator|.
name|timeZone
argument_list|(
literal|"UTC"
argument_list|)
operator|.
name|format
argument_list|(
literal|"broken_xx"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQueryBuilder
operator|.
name|validate
argument_list|()
operator|.
name|validationErrors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|rangeQueryBuilder
operator|.
name|timeZone
argument_list|(
literal|"xXx"
argument_list|)
operator|.
name|format
argument_list|(
literal|"broken_xx"
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|rangeQueryBuilder
operator|.
name|validate
argument_list|()
operator|.
name|validationErrors
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|is
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**      * Specifying a timezone together with a numeric range query should throw an error.      */
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|QueryParsingException
operator|.
name|class
argument_list|)
DECL|method|testToQueryNonDateWithTimezone
specifier|public
name|void
name|testToQueryNonDateWithTimezone
parameter_list|()
throws|throws
name|QueryParsingException
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
name|query
operator|.
name|toQuery
argument_list|(
name|createContext
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|createEmptyQueryBuilder
specifier|protected
name|RangeQueryBuilder
name|createEmptyQueryBuilder
parameter_list|()
block|{
return|return
operator|new
name|RangeQueryBuilder
argument_list|(
literal|null
argument_list|)
return|;
block|}
block|}
end_class

end_unit

