begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.cluster.block
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|block
package|;
end_package

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
name|common
operator|.
name|io
operator|.
name|stream
operator|.
name|StreamInput
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|rest
operator|.
name|RestStatus
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
name|util
operator|.
name|EnumSet
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
DECL|class|ClusterBlockTests
specifier|public
class|class
name|ClusterBlockTests
extends|extends
name|ElasticsearchTestCase
block|{
annotation|@
name|Test
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|Exception
block|{
name|int
name|iterations
init|=
name|randomIntBetween
argument_list|(
literal|10
argument_list|,
literal|100
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
comment|// Get a random version
name|Version
name|version
init|=
name|randomVersion
argument_list|(
name|random
argument_list|()
argument_list|)
decl_stmt|;
comment|// Get a random list of ClusterBlockLevels
name|EnumSet
argument_list|<
name|ClusterBlockLevel
argument_list|>
name|levels
init|=
name|EnumSet
operator|.
name|noneOf
argument_list|(
name|ClusterBlockLevel
operator|.
name|class
argument_list|)
decl_stmt|;
name|int
name|nbLevels
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|ClusterBlockLevel
operator|.
name|values
argument_list|()
operator|.
name|length
argument_list|)
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
name|nbLevels
condition|;
name|j
operator|++
control|)
block|{
name|levels
operator|.
name|add
argument_list|(
name|randomFrom
argument_list|(
name|ClusterBlockLevel
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|ClusterBlock
name|clusterBlock
init|=
operator|new
name|ClusterBlock
argument_list|(
name|randomInt
argument_list|()
argument_list|,
literal|"cluster block #"
operator|+
name|randomInt
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomBoolean
argument_list|()
argument_list|,
name|randomFrom
argument_list|(
name|RestStatus
operator|.
name|values
argument_list|()
argument_list|)
argument_list|,
name|levels
argument_list|)
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
name|version
argument_list|)
expr_stmt|;
name|clusterBlock
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|StreamInput
name|in
init|=
name|StreamInput
operator|.
name|wrap
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
name|version
argument_list|)
expr_stmt|;
name|ClusterBlock
name|result
init|=
name|ClusterBlock
operator|.
name|readClusterBlock
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|id
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|clusterBlock
operator|.
name|id
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|status
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|clusterBlock
operator|.
name|status
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|description
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|clusterBlock
operator|.
name|description
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|retryable
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|clusterBlock
operator|.
name|retryable
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|result
operator|.
name|disableStatePersistence
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|clusterBlock
operator|.
name|disableStatePersistence
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|result
operator|.
name|levels
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|,
name|clusterBlock
operator|.
name|levels
argument_list|()
operator|.
name|toArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
