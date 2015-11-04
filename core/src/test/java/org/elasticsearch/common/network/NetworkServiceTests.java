begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.network
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|network
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
name|settings
operator|.
name|Settings
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
name|net
operator|.
name|InetAddress
import|;
end_import

begin_comment
comment|/**  * Tests for network service... try to keep them safe depending upon configuration  * please don't actually bind to anything, just test the addresses.  */
end_comment

begin_class
DECL|class|NetworkServiceTests
specifier|public
class|class
name|NetworkServiceTests
extends|extends
name|ESTestCase
block|{
comment|/**       * ensure exception if we bind to multicast ipv4 address       */
DECL|method|testBindMulticastV4
specifier|public
name|void
name|testBindMulticastV4
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
try|try
block|{
name|service
operator|.
name|resolveBindHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"239.1.1.1"
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"invalid: multicast"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**       * ensure exception if we bind to multicast ipv6 address       */
DECL|method|testBindMulticastV6
specifier|public
name|void
name|testBindMulticastV6
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
try|try
block|{
name|service
operator|.
name|resolveBindHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"FF08::108"
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"invalid: multicast"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**       * ensure exception if we publish to multicast ipv4 address       */
DECL|method|testPublishMulticastV4
specifier|public
name|void
name|testPublishMulticastV4
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
try|try
block|{
name|service
operator|.
name|resolvePublishHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"239.1.1.1"
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"invalid: multicast"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**       * ensure exception if we publish to multicast ipv6 address       */
DECL|method|testPublishMulticastV6
specifier|public
name|void
name|testPublishMulticastV6
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
try|try
block|{
name|service
operator|.
name|resolvePublishHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"FF08::108"
block|}
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have hit exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|e
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|e
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"invalid: multicast"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**       * ensure specifying wildcard ipv4 address will bind to all interfaces       */
DECL|method|testBindAnyLocalV4
specifier|public
name|void
name|testBindAnyLocalV4
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
literal|"0.0.0.0"
argument_list|)
argument_list|,
name|service
operator|.
name|resolveBindHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"0.0.0.0"
block|}
argument_list|)
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**       * ensure specifying wildcard ipv6 address will bind to all interfaces       */
DECL|method|testBindAnyLocalV6
specifier|public
name|void
name|testBindAnyLocalV6
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|InetAddress
operator|.
name|getByName
argument_list|(
literal|"::"
argument_list|)
argument_list|,
name|service
operator|.
name|resolveBindHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"::"
block|}
argument_list|)
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
comment|/**       * ensure specifying wildcard ipv4 address selects reasonable publish address       */
DECL|method|testPublishAnyLocalV4
specifier|public
name|void
name|testPublishAnyLocalV4
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|InetAddress
name|address
init|=
name|service
operator|.
name|resolvePublishHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"0.0.0.0"
block|}
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|address
operator|.
name|isAnyLocalAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**       * ensure specifying wildcard ipv6 address selects reasonable publish address       */
DECL|method|testPublishAnyLocalV6
specifier|public
name|void
name|testPublishAnyLocalV6
parameter_list|()
throws|throws
name|Exception
block|{
name|NetworkService
name|service
init|=
operator|new
name|NetworkService
argument_list|(
name|Settings
operator|.
name|EMPTY
argument_list|)
decl_stmt|;
name|InetAddress
name|address
init|=
name|service
operator|.
name|resolvePublishHostAddresses
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"::"
block|}
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|address
operator|.
name|isAnyLocalAddress
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

