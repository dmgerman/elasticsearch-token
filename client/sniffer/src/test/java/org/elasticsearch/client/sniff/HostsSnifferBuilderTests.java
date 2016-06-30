begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.client.sniff
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|client
operator|.
name|sniff
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
name|com
operator|.
name|carrotsearch
operator|.
name|randomizedtesting
operator|.
name|generators
operator|.
name|RandomPicks
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
name|client
operator|.
name|RestClientTestCase
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_class
DECL|class|HostsSnifferBuilderTests
specifier|public
class|class
name|HostsSnifferBuilderTests
extends|extends
name|RestClientTestCase
block|{
DECL|method|testBuild
specifier|public
name|void
name|testBuild
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|HostsSniffer
operator|.
name|builder
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
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
literal|"restClient cannot be null"
argument_list|)
expr_stmt|;
block|}
name|int
name|numNodes
init|=
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|HttpHost
index|[]
name|hosts
init|=
operator|new
name|HttpHost
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
name|hosts
index|[
name|i
index|]
operator|=
operator|new
name|HttpHost
argument_list|(
literal|"localhost"
argument_list|,
literal|9200
operator|+
name|i
argument_list|)
expr_stmt|;
block|}
try|try
init|(
name|RestClient
name|client
init|=
name|RestClient
operator|.
name|builder
argument_list|(
name|hosts
argument_list|)
operator|.
name|build
argument_list|()
init|)
block|{
try|try
block|{
name|HostsSniffer
operator|.
name|builder
argument_list|(
name|client
argument_list|)
operator|.
name|setScheme
argument_list|(
literal|null
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
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
literal|"scheme cannot be null"
argument_list|)
expr_stmt|;
block|}
try|try
block|{
name|HostsSniffer
operator|.
name|builder
argument_list|(
name|client
argument_list|)
operator|.
name|setSniffRequestTimeoutMillis
argument_list|(
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|Integer
operator|.
name|MIN_VALUE
argument_list|,
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have failed"
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
literal|"sniffRequestTimeoutMillis must be greater than 0"
argument_list|)
expr_stmt|;
block|}
name|HostsSniffer
operator|.
name|Builder
name|builder
init|=
name|HostsSniffer
operator|.
name|builder
argument_list|(
name|client
argument_list|)
decl_stmt|;
if|if
condition|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setScheme
argument_list|(
name|RandomPicks
operator|.
name|randomFrom
argument_list|(
name|getRandom
argument_list|()
argument_list|,
name|HostsSniffer
operator|.
name|Scheme
operator|.
name|values
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|getRandom
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|builder
operator|.
name|setSniffRequestTimeoutMillis
argument_list|(
name|RandomInts
operator|.
name|randomIntBetween
argument_list|(
name|getRandom
argument_list|()
argument_list|,
literal|1
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|assertNotNull
argument_list|(
name|builder
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

