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
name|BytesRefs
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
name|junit
operator|.
name|Ignore
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
annotation|@
name|Ignore
DECL|class|BaseTermQueryTestCase
specifier|public
specifier|abstract
class|class
name|BaseTermQueryTestCase
parameter_list|<
name|QB
extends|extends
name|BaseTermQueryBuilder
parameter_list|<
name|QB
parameter_list|>
parameter_list|>
extends|extends
name|BaseQueryTestCase
argument_list|<
name|QB
argument_list|>
block|{
DECL|method|doCreateTestQueryBuilder
specifier|protected
specifier|final
name|QB
name|doCreateTestQueryBuilder
parameter_list|()
block|{
name|String
name|fieldName
init|=
literal|null
decl_stmt|;
name|Object
name|value
decl_stmt|;
switch|switch
condition|(
name|randomIntBetween
argument_list|(
literal|0
argument_list|,
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|fieldName
operator|=
name|BOOLEAN_FIELD_NAME
expr_stmt|;
block|}
name|value
operator|=
name|randomBoolean
argument_list|()
expr_stmt|;
break|break;
case|case
literal|1
case|:
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|fieldName
operator|=
name|STRING_FIELD_NAME
expr_stmt|;
block|}
if|if
condition|(
name|frequently
argument_list|()
condition|)
block|{
name|value
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// generate unicode string in 10% of cases
name|value
operator|=
name|randomUnicodeOfLength
argument_list|(
literal|10
argument_list|)
expr_stmt|;
block|}
break|break;
case|case
literal|2
case|:
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|fieldName
operator|=
name|INT_FIELD_NAME
expr_stmt|;
block|}
name|value
operator|=
name|randomInt
argument_list|(
literal|10000
argument_list|)
expr_stmt|;
break|break;
case|case
literal|3
case|:
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|fieldName
operator|=
name|DOUBLE_FIELD_NAME
expr_stmt|;
block|}
name|value
operator|=
name|randomDouble
argument_list|()
expr_stmt|;
break|break;
default|default:
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|()
throw|;
block|}
if|if
condition|(
name|fieldName
operator|==
literal|null
condition|)
block|{
name|fieldName
operator|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
expr_stmt|;
block|}
return|return
name|createQueryBuilder
argument_list|(
name|fieldName
argument_list|,
name|value
argument_list|)
return|;
block|}
DECL|method|createQueryBuilder
specifier|protected
specifier|abstract
name|QB
name|createQueryBuilder
parameter_list|(
name|String
name|fieldName
parameter_list|,
name|Object
name|value
parameter_list|)
function_decl|;
annotation|@
name|Test
DECL|method|testValidate
specifier|public
name|void
name|testValidate
parameter_list|()
throws|throws
name|QueryParsingException
block|{
name|QB
name|queryBuilder
init|=
name|createQueryBuilder
argument_list|(
literal|"all"
argument_list|,
literal|"good"
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|queryBuilder
operator|.
name|validate
argument_list|()
argument_list|)
expr_stmt|;
name|queryBuilder
operator|=
name|createQueryBuilder
argument_list|(
literal|null
argument_list|,
literal|"Term"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|queryBuilder
operator|.
name|validate
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryBuilder
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
name|queryBuilder
operator|=
name|createQueryBuilder
argument_list|(
literal|""
argument_list|,
literal|"Term"
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|queryBuilder
operator|.
name|validate
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryBuilder
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
name|queryBuilder
operator|=
name|createQueryBuilder
argument_list|(
literal|""
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|queryBuilder
operator|.
name|validate
argument_list|()
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|queryBuilder
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
annotation|@
name|Override
DECL|method|doCreateExpectedQuery
specifier|protected
name|Query
name|doCreateExpectedQuery
parameter_list|(
name|QB
name|queryBuilder
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
block|{
name|BytesRef
name|value
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|getCurrentTypes
argument_list|()
operator|.
name|length
operator|>
literal|0
condition|)
block|{
if|if
condition|(
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
condition|)
block|{
name|MappedFieldType
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
name|value
operator|=
name|mapper
operator|.
name|indexedValueForSearch
argument_list|(
name|queryBuilder
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|value
operator|==
literal|null
condition|)
block|{
name|value
operator|=
name|BytesRefs
operator|.
name|toBytesRef
argument_list|(
name|queryBuilder
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
return|return
name|createLuceneTermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|queryBuilder
operator|.
name|fieldName
argument_list|()
argument_list|,
name|value
argument_list|)
argument_list|)
return|;
block|}
DECL|method|createLuceneTermQuery
specifier|protected
specifier|abstract
name|Query
name|createLuceneTermQuery
parameter_list|(
name|Term
name|term
parameter_list|)
function_decl|;
block|}
end_class

end_unit

