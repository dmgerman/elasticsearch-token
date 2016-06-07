begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.percolator
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|percolator
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|ElasticsearchClient
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
name|junit
operator|.
name|Assert
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
name|greaterThan
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|assertVersionSerializable
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
name|hamcrest
operator|.
name|ElasticsearchAssertions
operator|.
name|formatShardStatus
import|;
end_import

begin_comment
comment|/** Static method pulled out of PercolatorIT, used by other tests */
end_comment

begin_class
DECL|class|PercolatorTestUtil
specifier|public
class|class
name|PercolatorTestUtil
extends|extends
name|Assert
block|{
DECL|method|preparePercolate
specifier|public
specifier|static
name|PercolateRequestBuilder
name|preparePercolate
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|)
block|{
return|return
operator|new
name|PercolateRequestBuilder
argument_list|(
name|client
argument_list|,
name|PercolateAction
operator|.
name|INSTANCE
argument_list|)
return|;
block|}
DECL|method|prepareMultiPercolate
specifier|public
specifier|static
name|MultiPercolateRequestBuilder
name|prepareMultiPercolate
parameter_list|(
name|ElasticsearchClient
name|client
parameter_list|)
block|{
return|return
operator|new
name|MultiPercolateRequestBuilder
argument_list|(
name|client
argument_list|,
name|MultiPercolateAction
operator|.
name|INSTANCE
argument_list|)
return|;
block|}
DECL|method|assertMatchCount
specifier|public
specifier|static
name|void
name|assertMatchCount
parameter_list|(
name|PercolateResponse
name|percolateResponse
parameter_list|,
name|long
name|expectedHitCount
parameter_list|)
block|{
if|if
condition|(
name|percolateResponse
operator|.
name|getCount
argument_list|()
operator|!=
name|expectedHitCount
condition|)
block|{
name|fail
argument_list|(
literal|"Count is "
operator|+
name|percolateResponse
operator|.
name|getCount
argument_list|()
operator|+
literal|" but "
operator|+
name|expectedHitCount
operator|+
literal|" was expected. "
operator|+
name|formatShardStatus
argument_list|(
name|percolateResponse
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertVersionSerializable
argument_list|(
name|percolateResponse
argument_list|)
expr_stmt|;
block|}
DECL|method|convertFromTextArray
specifier|public
specifier|static
name|String
index|[]
name|convertFromTextArray
parameter_list|(
name|PercolateResponse
operator|.
name|Match
index|[]
name|matches
parameter_list|,
name|String
name|index
parameter_list|)
block|{
if|if
condition|(
name|matches
operator|.
name|length
operator|==
literal|0
condition|)
block|{
return|return
name|Strings
operator|.
name|EMPTY_ARRAY
return|;
block|}
name|String
index|[]
name|strings
init|=
operator|new
name|String
index|[
name|matches
operator|.
name|length
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
name|matches
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|assertEquals
argument_list|(
name|index
argument_list|,
name|matches
index|[
name|i
index|]
operator|.
name|getIndex
argument_list|()
operator|.
name|string
argument_list|()
argument_list|)
expr_stmt|;
name|strings
index|[
name|i
index|]
operator|=
name|matches
index|[
name|i
index|]
operator|.
name|getId
argument_list|()
operator|.
name|string
argument_list|()
expr_stmt|;
block|}
return|return
name|strings
return|;
block|}
block|}
end_class

end_unit
