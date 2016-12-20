begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.reindex
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|reindex
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
name|RestClient
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
name|bytes
operator|.
name|BytesArray
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
name|reindex
operator|.
name|remote
operator|.
name|RemoteInfo
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
name|ESTestCase
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
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|emptyMap
import|;
end_import

begin_import
import|import static
name|java
operator|.
name|util
operator|.
name|Collections
operator|.
name|synchronizedList
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
name|hasSize
import|;
end_import

begin_class
DECL|class|ReindexFromRemoteBuildRestClientTests
specifier|public
class|class
name|ReindexFromRemoteBuildRestClientTests
extends|extends
name|ESTestCase
block|{
DECL|method|testBuildRestClient
specifier|public
name|void
name|testBuildRestClient
parameter_list|()
throws|throws
name|Exception
block|{
name|RemoteInfo
name|remoteInfo
init|=
operator|new
name|RemoteInfo
argument_list|(
literal|"https"
argument_list|,
literal|"localhost"
argument_list|,
literal|9200
argument_list|,
operator|new
name|BytesArray
argument_list|(
literal|"ignored"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|emptyMap
argument_list|()
argument_list|,
name|RemoteInfo
operator|.
name|DEFAULT_SOCKET_TIMEOUT
argument_list|,
name|RemoteInfo
operator|.
name|DEFAULT_CONNECT_TIMEOUT
argument_list|)
decl_stmt|;
name|long
name|taskId
init|=
name|randomLong
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Thread
argument_list|>
name|threads
init|=
name|synchronizedList
argument_list|(
operator|new
name|ArrayList
argument_list|<>
argument_list|()
argument_list|)
decl_stmt|;
name|RestClient
name|client
init|=
name|TransportReindexAction
operator|.
name|buildRestClient
argument_list|(
name|remoteInfo
argument_list|,
name|taskId
argument_list|,
name|threads
argument_list|)
decl_stmt|;
try|try
block|{
name|assertBusy
argument_list|(
parameter_list|()
lambda|->
name|assertThat
argument_list|(
name|threads
argument_list|,
name|hasSize
argument_list|(
literal|2
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Thread
name|thread
range|:
name|threads
control|)
block|{
name|assertEquals
argument_list|(
literal|"es-client-"
operator|+
name|taskId
operator|+
literal|"-"
operator|+
name|i
argument_list|,
name|thread
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
block|}
finally|finally
block|{
name|client
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

