begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bulk.byscroll
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
operator|.
name|byscroll
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bulk
operator|.
name|byscroll
operator|.
name|AbstractBulkByScrollRequestTestCase
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
name|bulk
operator|.
name|byscroll
operator|.
name|DeleteByQueryRequest
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
name|search
operator|.
name|SearchRequest
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
name|support
operator|.
name|IndicesOptions
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|TestUtil
operator|.
name|randomSimpleString
import|;
end_import

begin_class
DECL|class|DeleteByQueryRequestTests
specifier|public
class|class
name|DeleteByQueryRequestTests
extends|extends
name|AbstractBulkByScrollRequestTestCase
argument_list|<
name|DeleteByQueryRequest
argument_list|>
block|{
DECL|method|testDeleteteByQueryRequestImplementsIndicesRequestReplaceable
specifier|public
name|void
name|testDeleteteByQueryRequestImplementsIndicesRequestReplaceable
parameter_list|()
block|{
name|int
name|numIndices
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|String
index|[]
name|indices
init|=
operator|new
name|String
index|[
name|numIndices
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
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
name|indices
index|[
name|i
index|]
operator|=
name|randomSimpleString
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|30
argument_list|)
expr_stmt|;
block|}
name|SearchRequest
name|searchRequest
init|=
operator|new
name|SearchRequest
argument_list|(
name|indices
argument_list|)
decl_stmt|;
name|IndicesOptions
name|indicesOptions
init|=
name|IndicesOptions
operator|.
name|fromOptions
argument_list|(
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|)
decl_stmt|;
name|searchRequest
operator|.
name|indicesOptions
argument_list|(
name|indicesOptions
argument_list|)
expr_stmt|;
name|DeleteByQueryRequest
name|request
init|=
operator|new
name|DeleteByQueryRequest
argument_list|(
name|searchRequest
argument_list|)
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
name|numIndices
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|indices
index|[
name|i
index|]
argument_list|,
name|request
operator|.
name|indices
argument_list|()
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
name|assertSame
argument_list|(
name|indicesOptions
argument_list|,
name|request
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
name|assertSame
argument_list|(
name|request
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|indicesOptions
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|numNewIndices
init|=
name|between
argument_list|(
literal|1
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|String
index|[]
name|newIndices
init|=
operator|new
name|String
index|[
name|numNewIndices
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
name|numNewIndices
condition|;
name|i
operator|++
control|)
block|{
name|newIndices
index|[
name|i
index|]
operator|=
name|randomSimpleString
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|30
argument_list|)
expr_stmt|;
block|}
name|request
operator|.
name|indices
argument_list|(
name|newIndices
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numNewIndices
condition|;
name|i
operator|++
control|)
block|{
empty_stmt|;
name|assertEquals
argument_list|(
name|newIndices
index|[
name|i
index|]
argument_list|,
name|request
operator|.
name|indices
argument_list|()
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|numNewIndices
condition|;
name|i
operator|++
control|)
block|{
empty_stmt|;
name|assertEquals
argument_list|(
name|newIndices
index|[
name|i
index|]
argument_list|,
name|request
operator|.
name|getSearchRequest
argument_list|()
operator|.
name|indices
argument_list|()
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
DECL|method|newRequest
specifier|protected
name|DeleteByQueryRequest
name|newRequest
parameter_list|()
block|{
return|return
operator|new
name|DeleteByQueryRequest
argument_list|(
operator|new
name|SearchRequest
argument_list|(
name|randomAsciiOfLength
argument_list|(
literal|5
argument_list|)
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|extraRandomizationForSlice
specifier|protected
name|void
name|extraRandomizationForSlice
parameter_list|(
name|DeleteByQueryRequest
name|original
parameter_list|)
block|{
comment|// Nothing else to randomize
block|}
annotation|@
name|Override
DECL|method|extraForSliceAssertions
specifier|protected
name|void
name|extraForSliceAssertions
parameter_list|(
name|DeleteByQueryRequest
name|original
parameter_list|,
name|DeleteByQueryRequest
name|forSliced
parameter_list|)
block|{
comment|// No extra assertions needed
block|}
block|}
end_class

end_unit
