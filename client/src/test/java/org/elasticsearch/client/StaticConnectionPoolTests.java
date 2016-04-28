begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
package|;
end_package

begin_import
import|import
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomInts
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|HttpHost
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|client
operator|.
name|config
operator|.
name|RequestConfig
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|CloseableHttpClient
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|http
operator|.
name|impl
operator|.
name|client
operator|.
name|HttpClientBuilder
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
name|LuceneTestCase
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|logging
operator|.
name|LogManager
import|;
end_import

begin_class
DECL|class|StaticConnectionPoolTests
specifier|public
class|class
name|StaticConnectionPoolTests
extends|extends
name|LuceneTestCase
block|{
static|static
block|{
name|LogManager
operator|.
name|getLogManager
argument_list|()
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
DECL|method|testConstructor
specifier|public
name|void
name|testConstructor
parameter_list|()
block|{
name|CloseableHttpClient
name|httpClient
init|=
name|HttpClientBuilder
operator|.
name|create
argument_list|()
operator|.
name|build
argument_list|()
decl_stmt|;
name|int
name|numNodes
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|random
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|Node
index|[]
name|nodes
init|=
operator|new
name|Node
index|[
name|numNodes
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
name|numNodes
condition|;
name|i
operator|++
control|)
block|{
name|nodes
index|[
name|i
index|]
operator|=
operator|new
name|Node
argument_list|(
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
argument_list|)
argument_list|)
expr_stmt|;
block|}
try|try
block|{
operator|new
name|StaticConnectionPool
argument_list|(
literal|null
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|RequestConfig
operator|.
name|DEFAULT
argument_list|,
name|connection
lambda|->
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"client cannot be null"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
operator|new
name|StaticConnectionPool
argument_list|(
name|httpClient
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
literal|null
argument_list|,
name|connection
lambda|->
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"pingRequestConfig cannot be null"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
operator|new
name|StaticConnectionPool
argument_list|(
name|httpClient
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|RequestConfig
operator|.
name|DEFAULT
argument_list|,
literal|null
argument_list|,
name|nodes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"connection selector predicate cannot be null"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
operator|new
name|StaticConnectionPool
argument_list|(
name|httpClient
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|RequestConfig
operator|.
name|DEFAULT
argument_list|,
name|connection
lambda|->
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
operator|(
name|Node
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NullPointerException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"node cannot be null"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
operator|new
name|StaticConnectionPool
argument_list|(
name|httpClient
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|RequestConfig
operator|.
name|DEFAULT
argument_list|,
name|connection
lambda|->
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
operator|(
name|Node
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"no nodes provided"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
operator|new
name|StaticConnectionPool
argument_list|(
name|httpClient
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|RequestConfig
operator|.
name|DEFAULT
argument_list|,
name|connection
lambda|->
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertEquals
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
argument_list|,
literal|"no nodes provided"
argument_list|)
expr_stmt|;
block|}
name|StaticConnectionPool
name|staticConnectionPool
init|=
operator|new
name|StaticConnectionPool
argument_list|(
name|httpClient
argument_list|,
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|RequestConfig
operator|.
name|DEFAULT
argument_list|,
name|connection
lambda|->
name|random
argument_list|()
operator|.
name|nextBoolean
argument_list|()
argument_list|,
name|nodes
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|staticConnectionPool
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

