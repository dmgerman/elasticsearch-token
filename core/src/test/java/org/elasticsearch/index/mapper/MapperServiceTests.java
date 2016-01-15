begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.mapper
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|mapper
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
name|ConstantScoreQuery
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
name|ExceptionsHelper
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
name|compress
operator|.
name|CompressedXContent
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
name|internal
operator|.
name|TypeFieldMapper
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
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|hasToString
import|;
end_import

begin_class
DECL|class|MapperServiceTests
specifier|public
class|class
name|MapperServiceTests
extends|extends
name|ESSingleNodeTestCase
block|{
annotation|@
name|Rule
DECL|field|expectedException
specifier|public
name|ExpectedException
name|expectedException
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
DECL|method|testTypeNameStartsWithIllegalDot
specifier|public
name|void
name|testTypeNameStartsWithIllegalDot
parameter_list|()
block|{
name|expectedException
operator|.
name|expect
argument_list|(
name|MapperParsingException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expect
argument_list|(
name|hasToString
argument_list|(
name|containsString
argument_list|(
literal|"mapping type name [.test-type] must not start with a '.'"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|index
init|=
literal|"test-index"
decl_stmt|;
name|String
name|type
init|=
literal|".test-type"
decl_stmt|;
name|String
name|field
init|=
literal|"field"
decl_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
name|index
argument_list|)
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|field
argument_list|,
literal|"type=string"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
DECL|method|testTypeNameTooLong
specifier|public
name|void
name|testTypeNameTooLong
parameter_list|()
block|{
name|String
name|index
init|=
literal|"text-index"
decl_stmt|;
name|String
name|field
init|=
literal|"field"
decl_stmt|;
name|String
name|type
init|=
operator|new
name|String
argument_list|(
operator|new
name|char
index|[
literal|256
index|]
argument_list|)
operator|.
name|replace
argument_list|(
literal|"\0"
argument_list|,
literal|"a"
argument_list|)
decl_stmt|;
name|expectedException
operator|.
name|expect
argument_list|(
name|MapperParsingException
operator|.
name|class
argument_list|)
expr_stmt|;
name|expectedException
operator|.
name|expect
argument_list|(
name|hasToString
argument_list|(
name|containsString
argument_list|(
literal|"mapping type name ["
operator|+
name|type
operator|+
literal|"] is too long; limit is length 255 but was [256]"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
name|index
argument_list|)
operator|.
name|addMapping
argument_list|(
name|type
argument_list|,
name|field
argument_list|,
literal|"type=string"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
expr_stmt|;
block|}
DECL|method|testTypes
specifier|public
name|void
name|testTypes
parameter_list|()
throws|throws
name|Exception
block|{
name|IndexService
name|indexService1
init|=
name|createIndex
argument_list|(
literal|"index1"
argument_list|)
decl_stmt|;
name|MapperService
name|mapperService
init|=
name|indexService1
operator|.
name|mapperService
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|,
name|mapperService
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type1"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
literal|"{\"type1\":{}}"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertNull
argument_list|(
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
literal|"type1"
argument_list|)
argument_list|,
name|mapperService
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
literal|"{\"_default_\":{}}"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Collections
operator|.
name|singleton
argument_list|(
literal|"type1"
argument_list|)
argument_list|,
name|mapperService
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
name|mapperService
operator|.
name|merge
argument_list|(
literal|"type2"
argument_list|,
operator|new
name|CompressedXContent
argument_list|(
literal|"{\"type2\":{}}"
argument_list|)
argument_list|,
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|mapperService
operator|.
name|documentMapper
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
literal|"type1"
argument_list|,
literal|"type2"
argument_list|)
argument_list|)
argument_list|,
name|mapperService
operator|.
name|types
argument_list|()
argument_list|)
expr_stmt|;
block|}
DECL|method|testIndexIntoDefaultMapping
specifier|public
name|void
name|testIndexIntoDefaultMapping
parameter_list|()
throws|throws
name|Throwable
block|{
comment|// 1. test implicit index creation
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index1"
argument_list|,
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
literal|"1"
argument_list|)
operator|.
name|setSource
argument_list|(
literal|"{"
argument_list|)
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|ExecutionException
condition|)
block|{
name|t
operator|=
operator|(
operator|(
name|ExecutionException
operator|)
name|t
operator|)
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
specifier|final
name|Throwable
name|throwable
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|throwable
operator|instanceof
name|IllegalArgumentException
condition|)
block|{
name|assertEquals
argument_list|(
literal|"It is forbidden to index into the default mapping [_default_]"
argument_list|,
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|t
throw|;
block|}
block|}
comment|// 2. already existing index
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index2"
argument_list|)
decl_stmt|;
try|try
block|{
name|client
argument_list|()
operator|.
name|prepareIndex
argument_list|(
literal|"index2"
argument_list|,
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|,
literal|"2"
argument_list|)
operator|.
name|setSource
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|get
argument_list|()
expr_stmt|;
name|fail
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|instanceof
name|ExecutionException
condition|)
block|{
name|t
operator|=
operator|(
operator|(
name|ExecutionException
operator|)
name|t
operator|)
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
specifier|final
name|Throwable
name|throwable
init|=
name|ExceptionsHelper
operator|.
name|unwrapCause
argument_list|(
name|t
argument_list|)
decl_stmt|;
if|if
condition|(
name|throwable
operator|instanceof
name|IllegalArgumentException
condition|)
block|{
name|assertEquals
argument_list|(
literal|"It is forbidden to index into the default mapping [_default_]"
argument_list|,
name|throwable
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
name|t
throw|;
block|}
block|}
name|assertFalse
argument_list|(
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|hasMapping
argument_list|(
name|MapperService
operator|.
name|DEFAULT_MAPPING
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|testSearchFilter
specifier|public
name|void
name|testSearchFilter
parameter_list|()
block|{
name|IndexService
name|indexService
init|=
name|createIndex
argument_list|(
literal|"index1"
argument_list|,
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|indices
argument_list|()
operator|.
name|prepareCreate
argument_list|(
literal|"index1"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type1"
argument_list|,
literal|"field1"
argument_list|,
literal|"type=nested"
argument_list|)
operator|.
name|addMapping
argument_list|(
literal|"type2"
argument_list|,
operator|new
name|Object
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|Query
name|searchFilter
init|=
name|indexService
operator|.
name|mapperService
argument_list|()
operator|.
name|searchFilter
argument_list|(
literal|"type1"
argument_list|,
literal|"type3"
argument_list|)
decl_stmt|;
name|Query
name|expectedQuery
init|=
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|BooleanQuery
operator|.
name|Builder
argument_list|()
operator|.
name|add
argument_list|(
operator|new
name|ConstantScoreQuery
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type1"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|add
argument_list|(
operator|new
name|TermQuery
argument_list|(
operator|new
name|Term
argument_list|(
name|TypeFieldMapper
operator|.
name|NAME
argument_list|,
literal|"type3"
argument_list|)
argument_list|)
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|SHOULD
argument_list|)
operator|.
name|build
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
operator|.
name|add
argument_list|(
name|Queries
operator|.
name|newNonNestedFilter
argument_list|()
argument_list|,
name|BooleanClause
operator|.
name|Occur
operator|.
name|MUST
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|searchFilter
argument_list|,
name|equalTo
argument_list|(
operator|new
name|ConstantScoreQuery
argument_list|(
name|expectedQuery
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

