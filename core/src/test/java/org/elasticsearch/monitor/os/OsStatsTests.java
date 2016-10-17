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
DECL|class|OsStatsTests
specifier|public
class|class
name|OsStatsTests
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
name|numLoadAverages
init|=
name|randomIntBetween
argument_list|(
literal|1
argument_list|,
literal|5
argument_list|)
decl_stmt|;
name|double
name|loadAverages
index|[]
init|=
operator|new
name|double
index|[
name|numLoadAverages
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
name|loadAverages
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|loadAverages
index|[
name|i
index|]
operator|=
name|randomDouble
argument_list|()
expr_stmt|;
block|}
name|OsStats
operator|.
name|Cpu
name|cpu
init|=
operator|new
name|OsStats
operator|.
name|Cpu
argument_list|(
name|randomShort
argument_list|()
argument_list|,
name|loadAverages
argument_list|)
decl_stmt|;
name|OsStats
operator|.
name|Mem
name|mem
init|=
operator|new
name|OsStats
operator|.
name|Mem
argument_list|(
name|randomLong
argument_list|()
argument_list|,
name|randomLong
argument_list|()
argument_list|)
decl_stmt|;
name|OsStats
operator|.
name|Swap
name|swap
init|=
operator|new
name|OsStats
operator|.
name|Swap
argument_list|(
name|randomLong
argument_list|()
argument_list|,
name|randomLong
argument_list|()
argument_list|)
decl_stmt|;
name|OsStats
name|osStats
init|=
operator|new
name|OsStats
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|cpu
argument_list|,
name|mem
argument_list|,
name|swap
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
name|osStats
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
name|OsStats
name|deserializedOsStats
init|=
operator|new
name|OsStats
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|osStats
operator|.
name|getTimestamp
argument_list|()
argument_list|,
name|deserializedOsStats
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osStats
operator|.
name|getCpu
argument_list|()
operator|.
name|getPercent
argument_list|()
argument_list|,
name|deserializedOsStats
operator|.
name|getCpu
argument_list|()
operator|.
name|getPercent
argument_list|()
argument_list|)
expr_stmt|;
name|assertArrayEquals
argument_list|(
name|osStats
operator|.
name|getCpu
argument_list|()
operator|.
name|getLoadAverage
argument_list|()
argument_list|,
name|deserializedOsStats
operator|.
name|getCpu
argument_list|()
operator|.
name|getLoadAverage
argument_list|()
argument_list|,
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osStats
operator|.
name|getMem
argument_list|()
operator|.
name|getFree
argument_list|()
argument_list|,
name|deserializedOsStats
operator|.
name|getMem
argument_list|()
operator|.
name|getFree
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osStats
operator|.
name|getMem
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|,
name|deserializedOsStats
operator|.
name|getMem
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osStats
operator|.
name|getSwap
argument_list|()
operator|.
name|getFree
argument_list|()
argument_list|,
name|deserializedOsStats
operator|.
name|getSwap
argument_list|()
operator|.
name|getFree
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|osStats
operator|.
name|getSwap
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|,
name|deserializedOsStats
operator|.
name|getSwap
argument_list|()
operator|.
name|getTotal
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit
