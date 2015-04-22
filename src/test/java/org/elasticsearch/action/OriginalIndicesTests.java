begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
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
name|support
operator|.
name|IndicesOptions
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamInput
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
name|io
operator|.
name|stream
operator|.
name|BytesStreamOutput
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
name|ElasticsearchTestCase
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|VersionUtils
operator|.
name|randomVersion
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
name|equalTo
import|;
end_import

begin_class
DECL|class|OriginalIndicesTests
specifier|public
class|class
name|OriginalIndicesTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|indicesOptionsValues
specifier|private
specifier|static
specifier|final
name|IndicesOptions
index|[]
name|indicesOptionsValues
init|=
operator|new
name|IndicesOptions
index|[]
block|{
name|IndicesOptions
operator|.
name|lenientExpandOpen
argument_list|()
block|,
name|IndicesOptions
operator|.
name|strictExpand
argument_list|()
block|,
name|IndicesOptions
operator|.
name|strictExpandOpen
argument_list|()
block|,
name|IndicesOptions
operator|.
name|strictExpandOpenAndForbidClosed
argument_list|()
block|,
name|IndicesOptions
operator|.
name|strictSingleIndexNoExpandForbidClosed
argument_list|()
block|}
decl_stmt|;
annotation|@
name|Test
DECL|method|testOriginalIndicesSerialization
specifier|public
name|void
name|testOriginalIndicesSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|iterations
init|=
name|iterations
argument_list|(
literal|10
argument_list|,
literal|30
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
name|iterations
condition|;
name|i
operator|++
control|)
block|{
name|OriginalIndices
name|originalIndices
init|=
name|randomOriginalIndices
argument_list|()
decl_stmt|;
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
decl_stmt|;
name|out
operator|.
name|setVersion
argument_list|(
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|OriginalIndices
operator|.
name|writeOriginalIndices
argument_list|(
name|originalIndices
argument_list|,
name|out
argument_list|)
expr_stmt|;
name|BytesStreamInput
name|in
init|=
operator|new
name|BytesStreamInput
argument_list|(
name|out
operator|.
name|bytes
argument_list|()
argument_list|)
decl_stmt|;
name|in
operator|.
name|setVersion
argument_list|(
name|out
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
name|OriginalIndices
name|originalIndices2
init|=
name|OriginalIndices
operator|.
name|readOriginalIndices
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|originalIndices2
operator|.
name|indices
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|originalIndices
operator|.
name|indices
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|originalIndices2
operator|.
name|indicesOptions
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|originalIndices
operator|.
name|indicesOptions
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|randomOriginalIndices
specifier|private
specifier|static
name|OriginalIndices
name|randomOriginalIndices
parameter_list|()
block|{
name|int
name|numIndices
init|=
name|randomInt
argument_list|(
literal|10
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
name|j
init|=
literal|0
init|;
name|j
operator|<
name|indices
operator|.
name|length
condition|;
name|j
operator|++
control|)
block|{
name|indices
index|[
name|j
index|]
operator|=
name|randomAsciiOfLength
argument_list|(
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|10
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|IndicesOptions
name|indicesOptions
init|=
name|randomFrom
argument_list|(
name|indicesOptionsValues
argument_list|)
decl_stmt|;
return|return
operator|new
name|OriginalIndices
argument_list|(
name|indices
argument_list|,
name|indicesOptions
argument_list|)
return|;
block|}
block|}
end_class

end_unit

