begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.suggest.term
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|suggest
operator|.
name|term
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|AbstractWriteableEnumTestCase
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
name|search
operator|.
name|suggest
operator|.
name|term
operator|.
name|TermSuggestionBuilder
operator|.
name|SortBy
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

begin_comment
comment|/**  * Test the {@link SortBy} enum.  */
end_comment

begin_class
DECL|class|SortByTests
specifier|public
class|class
name|SortByTests
extends|extends
name|AbstractWriteableEnumTestCase
block|{
annotation|@
name|Override
DECL|method|testValidOrdinals
specifier|public
name|void
name|testValidOrdinals
parameter_list|()
block|{
name|assertThat
argument_list|(
name|SortBy
operator|.
name|SCORE
operator|.
name|ordinal
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|SortBy
operator|.
name|FREQUENCY
operator|.
name|ordinal
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testFromString
specifier|public
name|void
name|testFromString
parameter_list|()
block|{
name|assertThat
argument_list|(
name|SortBy
operator|.
name|resolve
argument_list|(
literal|"score"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|SortBy
operator|.
name|SCORE
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|SortBy
operator|.
name|resolve
argument_list|(
literal|"frequency"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
name|SortBy
operator|.
name|FREQUENCY
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|String
name|doesntExist
init|=
literal|"doesnt_exist"
decl_stmt|;
try|try
block|{
name|SortBy
operator|.
name|resolve
argument_list|(
name|doesntExist
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"SortBy should not have an element "
operator|+
name|doesntExist
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{         }
try|try
block|{
name|SortBy
operator|.
name|resolve
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"SortBy.resolve on a null value should throw an exception."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
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
literal|"Input string is null"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|testWriteTo
specifier|public
name|void
name|testWriteTo
parameter_list|()
throws|throws
name|IOException
block|{
name|assertWriteToStream
argument_list|(
name|SortBy
operator|.
name|SCORE
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertWriteToStream
argument_list|(
name|SortBy
operator|.
name|FREQUENCY
argument_list|,
literal|1
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|testReadFrom
specifier|public
name|void
name|testReadFrom
parameter_list|()
throws|throws
name|IOException
block|{
name|assertReadFromStream
argument_list|(
literal|0
argument_list|,
name|SortBy
operator|.
name|SCORE
argument_list|)
expr_stmt|;
name|assertReadFromStream
argument_list|(
literal|1
argument_list|,
name|SortBy
operator|.
name|FREQUENCY
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

