begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.transport.netty
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|transport
operator|.
name|netty
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
name|transport
operator|.
name|TransportAddress
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

begin_comment
comment|/** Unit tests for NettyTransport */
end_comment

begin_class
DECL|class|NettyTransportTests
specifier|public
class|class
name|NettyTransportTests
extends|extends
name|ESTestCase
block|{
comment|/** Test ipv4 host with a default port works */
DECL|method|testParseV4DefaultPort
specifier|public
name|void
name|testParseV4DefaultPort
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|"1234"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1234
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test ipv4 host with a default port range works */
DECL|method|testParseV4DefaultRange
specifier|public
name|void
name|testParseV4DefaultRange
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"127.0.0.1"
argument_list|,
literal|"1234-1235"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1234
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1235
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test ipv4 host with port works */
DECL|method|testParseV4WithPort
specifier|public
name|void
name|testParseV4WithPort
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"127.0.0.1:2345"
argument_list|,
literal|"1234"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2345
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test ipv4 host with port range works */
DECL|method|testParseV4WithPortRange
specifier|public
name|void
name|testParseV4WithPortRange
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"127.0.0.1:2345-2346"
argument_list|,
literal|"1234"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2345
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"127.0.0.1"
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2346
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test unbracketed ipv6 hosts in configuration fail. Leave no ambiguity */
DECL|method|testParseV6UnBracketed
specifier|public
name|void
name|testParseV6UnBracketed
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"::1"
argument_list|,
literal|"1234"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"should have gotten exception"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalArgumentException
name|expected
parameter_list|)
block|{
name|assertTrue
argument_list|(
name|expected
operator|.
name|getMessage
argument_list|()
operator|.
name|contains
argument_list|(
literal|"must be bracketed"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** Test ipv6 host with a default port works */
DECL|method|testParseV6DefaultPort
specifier|public
name|void
name|testParseV6DefaultPort
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"[::1]"
argument_list|,
literal|"1234"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"::1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1234
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test ipv6 host with a default port range works */
DECL|method|testParseV6DefaultRange
specifier|public
name|void
name|testParseV6DefaultRange
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"[::1]"
argument_list|,
literal|"1234-1235"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"::1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1234
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"::1"
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1235
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test ipv6 host with port works */
DECL|method|testParseV6WithPort
specifier|public
name|void
name|testParseV6WithPort
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"[::1]:2345"
argument_list|,
literal|"1234"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"::1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2345
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test ipv6 host with port range works */
DECL|method|testParseV6WithPortRange
specifier|public
name|void
name|testParseV6WithPortRange
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"[::1]:2345-2346"
argument_list|,
literal|"1234"
argument_list|,
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"::1"
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2345
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"::1"
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getAddress
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2346
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/** Test per-address limit */
DECL|method|testAddressLimit
specifier|public
name|void
name|testAddressLimit
parameter_list|()
throws|throws
name|Exception
block|{
name|TransportAddress
index|[]
name|addresses
init|=
name|NettyTransport
operator|.
name|parse
argument_list|(
literal|"[::1]:100-200"
argument_list|,
literal|"1000"
argument_list|,
literal|3
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|addresses
operator|.
name|length
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|100
argument_list|,
name|addresses
index|[
literal|0
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|101
argument_list|,
name|addresses
index|[
literal|1
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|102
argument_list|,
name|addresses
index|[
literal|2
index|]
operator|.
name|getPort
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

