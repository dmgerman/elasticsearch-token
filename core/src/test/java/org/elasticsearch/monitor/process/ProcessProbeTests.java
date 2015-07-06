begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.process
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|process
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
operator|.
name|Bootstrap
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
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
operator|.
name|JvmInfo
operator|.
name|jvmInfo
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

begin_class
DECL|class|ProcessProbeTests
specifier|public
class|class
name|ProcessProbeTests
extends|extends
name|ElasticsearchTestCase
block|{
DECL|field|probe
name|ProcessProbe
name|probe
init|=
name|ProcessProbe
operator|.
name|getInstance
argument_list|()
decl_stmt|;
annotation|@
name|Test
DECL|method|testProcessInfo
specifier|public
name|void
name|testProcessInfo
parameter_list|()
block|{
name|ProcessInfo
name|info
init|=
name|probe
operator|.
name|processInfo
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|info
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
operator|.
name|getRefreshInterval
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
operator|.
name|getId
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|info
operator|.
name|isMlockall
argument_list|()
argument_list|,
name|equalTo
argument_list|(
name|Bootstrap
operator|.
name|isMemoryLocked
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
DECL|method|testProcessStats
specifier|public
name|void
name|testProcessStats
parameter_list|()
block|{
name|ProcessStats
name|stats
init|=
name|probe
operator|.
name|processStats
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|stats
argument_list|)
expr_stmt|;
name|ProcessStats
operator|.
name|Cpu
name|cpu
init|=
name|stats
operator|.
name|getCpu
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|cpu
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cpu
operator|.
name|getPercent
argument_list|()
argument_list|,
name|greaterThanOrEqualTo
argument_list|(
operator|(
name|short
operator|)
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|cpu
operator|.
name|total
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
operator|-
literal|1L
argument_list|)
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ProcessStats
operator|.
name|Mem
name|mem
init|=
name|stats
operator|.
name|getMem
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|mem
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|mem
operator|.
name|totalVirtual
argument_list|,
name|anyOf
argument_list|(
name|equalTo
argument_list|(
operator|-
literal|1L
argument_list|)
argument_list|,
name|greaterThan
argument_list|(
literal|0L
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

