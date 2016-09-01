begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.os
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|os
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
name|test
operator|.
name|ESTestCase
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

begin_class
DECL|class|OsInfoTests
specifier|public
class|class
name|OsInfoTests
extends|extends
name|ESTestCase
block|{
DECL|method|testSerialization
specifier|public
name|void
name|testSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|availableProcessors
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|64
argument_list|)
decl_stmt|;
name|int
name|allocatedProcessors
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
name|availableProcessors
argument_list|)
decl_stmt|;
name|long
name|refreshInterval
decl_stmt|;
if|if
condition|(
name|randomBoolean
argument_list|()
condition|)
block|{
name|refreshInterval
operator|=
operator|-
literal|1
expr_stmt|;
block|}
else|else
block|{
name|refreshInterval
operator|=
name|randomLong
argument_list|()
expr_stmt|;
while|while
condition|(
name|refreshInterval
operator|==
name|Long
operator|.
name|MIN_VALUE
condition|)
block|{
name|refreshInterval
operator|=
name|randomLong
argument_list|()
expr_stmt|;
block|}
name|refreshInterval
operator|=
name|Math
operator|.
name|abs
argument_list|(
name|refreshInterval
argument_list|)
expr_stmt|;
block|}
name|String
name|name
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|arch
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|String
name|version
init|=
name|randomAsciiOfLengthBetween
argument_list|(
literal|3
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|OsInfo
name|osInfo
init|=
operator|new
name|OsInfo
argument_list|(
name|refreshInterval
argument_list|,
name|availableProcessors
argument_list|,
name|allocatedProcessors
argument_list|,
name|name
argument_list|,
name|arch
argument_list|,
name|version
argument_list|)
decl_stmt|;
try|try
init|(
name|BytesStreamOutput
name|out
init|=
operator|new
name|BytesStreamOutput
argument_list|()
init|)
block|{
name|osInfo
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
try|try
init|(
name|StreamInput
name|in
init|=
name|out
operator|.
name|bytes
argument_list|()
operator|.
name|streamInput
argument_list|()
init|)
block|{
name|OsInfo
name|deserializedOsInfo
init|=
operator|new
name|OsInfo
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|osInfo
operator|.
name|getRefreshInterval
argument_list|()
argument_list|,
name|deserializedOsInfo
operator|.
name|getRefreshInterval
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osInfo
operator|.
name|getAvailableProcessors
argument_list|()
argument_list|,
name|deserializedOsInfo
operator|.
name|getAvailableProcessors
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osInfo
operator|.
name|getAllocatedProcessors
argument_list|()
argument_list|,
name|deserializedOsInfo
operator|.
name|getAllocatedProcessors
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osInfo
operator|.
name|getName
argument_list|()
argument_list|,
name|deserializedOsInfo
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osInfo
operator|.
name|getArch
argument_list|()
argument_list|,
name|deserializedOsInfo
operator|.
name|getArch
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osInfo
operator|.
name|getVersion
argument_list|()
argument_list|,
name|deserializedOsInfo
operator|.
name|getVersion
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

