begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.memcached.test
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|memcached
operator|.
name|test
package|;
end_package

begin_import
import|import
name|net
operator|.
name|spy
operator|.
name|memcached
operator|.
name|MemcachedClient
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
name|admin
operator|.
name|cluster
operator|.
name|health
operator|.
name|ClusterHealthResponse
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
name|network
operator|.
name|NetworkUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|Node
import|;
end_import

begin_import
import|import
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|BeforeMethod
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
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
name|concurrent
operator|.
name|Future
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
name|TimeUnit
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|xcontent
operator|.
name|XContentFactory
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|node
operator|.
name|NodeBuilder
operator|.
name|*
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|MatcherAssert
operator|.
name|*
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
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|AbstractMemcachedActionsTests
specifier|public
specifier|abstract
class|class
name|AbstractMemcachedActionsTests
block|{
DECL|field|node
specifier|private
name|Node
name|node
decl_stmt|;
DECL|field|memcachedClient
specifier|private
name|MemcachedClient
name|memcachedClient
decl_stmt|;
annotation|@
name|BeforeMethod
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|IOException
block|{
name|node
operator|=
name|nodeBuilder
argument_list|()
operator|.
name|settings
argument_list|(
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"cluster.name"
argument_list|,
literal|"test-cluster-"
operator|+
name|NetworkUtils
operator|.
name|getLocalAddress
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"gateway.type"
argument_list|,
literal|"none"
argument_list|)
argument_list|)
operator|.
name|node
argument_list|()
expr_stmt|;
name|memcachedClient
operator|=
name|createMemcachedClient
argument_list|()
expr_stmt|;
block|}
DECL|method|createMemcachedClient
specifier|protected
specifier|abstract
name|MemcachedClient
name|createMemcachedClient
parameter_list|()
throws|throws
name|IOException
function_decl|;
annotation|@
name|AfterMethod
DECL|method|tearDown
specifier|public
name|void
name|tearDown
parameter_list|()
block|{
name|memcachedClient
operator|.
name|shutdown
argument_list|()
expr_stmt|;
name|node
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
DECL|method|testSimpleOperations
annotation|@
name|Test
specifier|public
name|void
name|testSimpleOperations
parameter_list|()
throws|throws
name|Exception
block|{
comment|// TODO seems to use SetQ, which is not really supported yet
comment|//        List<Future<Boolean>> setResults = Lists.newArrayList();
comment|//
comment|//        for (int i = 0; i< 10; i++) {
comment|//            setResults.add(memcachedClient.set("/test/person/" + i, 0, jsonBuilder().startObject().field("test", "value").endObject().copiedBytes()));
comment|//        }
comment|//
comment|//        for (Future<Boolean> setResult : setResults) {
comment|//            assertThat(setResult.get(10, TimeUnit.SECONDS), equalTo(true));
comment|//        }
name|Future
argument_list|<
name|Boolean
argument_list|>
name|setResult
init|=
name|memcachedClient
operator|.
name|set
argument_list|(
literal|"/test/person/1"
argument_list|,
literal|0
argument_list|,
name|jsonBuilder
argument_list|()
operator|.
name|startObject
argument_list|()
operator|.
name|field
argument_list|(
literal|"test"
argument_list|,
literal|"value"
argument_list|)
operator|.
name|endObject
argument_list|()
operator|.
name|copiedBytes
argument_list|()
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|setResult
operator|.
name|get
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|ClusterHealthResponse
name|health
init|=
name|node
operator|.
name|client
argument_list|()
operator|.
name|admin
argument_list|()
operator|.
name|cluster
argument_list|()
operator|.
name|prepareHealth
argument_list|()
operator|.
name|setWaitForYellowStatus
argument_list|()
operator|.
name|execute
argument_list|()
operator|.
name|actionGet
argument_list|()
decl_stmt|;
name|assertThat
argument_list|(
name|health
operator|.
name|timedOut
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|getResult
init|=
operator|(
name|String
operator|)
name|memcachedClient
operator|.
name|get
argument_list|(
literal|"/_refresh"
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"REFRESH "
operator|+
name|getResult
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"total\":10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"successful\":5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"failed\":0"
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
operator|(
name|String
operator|)
name|memcachedClient
operator|.
name|get
argument_list|(
literal|"/test/person/1"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"GET "
operator|+
name|getResult
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"_index\":\"test\""
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"_type\":\"person\""
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"_id\":\"1\""
argument_list|)
argument_list|)
expr_stmt|;
name|Future
argument_list|<
name|Boolean
argument_list|>
name|deleteResult
init|=
name|memcachedClient
operator|.
name|delete
argument_list|(
literal|"/test/person/1"
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|deleteResult
operator|.
name|get
argument_list|(
literal|10
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
operator|(
name|String
operator|)
name|memcachedClient
operator|.
name|get
argument_list|(
literal|"/_refresh"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"REFRESH "
operator|+
name|getResult
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"total\":10"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"successful\":5"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|getResult
argument_list|,
name|Matchers
operator|.
name|containsString
argument_list|(
literal|"\"failed\":0"
argument_list|)
argument_list|)
expr_stmt|;
name|getResult
operator|=
operator|(
name|String
operator|)
name|memcachedClient
operator|.
name|get
argument_list|(
literal|"/test/person/1"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"GET "
operator|+
name|getResult
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

