begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.stresstest.leaks
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|stresstest
operator|.
name|leaks
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
operator|.
name|JvmStats
import|;
end_import

begin_comment
comment|/**  * This test mainly comes to check the native memory leak with getLastGCInfo (which is now  * disabled by default).  */
end_comment

begin_class
DECL|class|JvmStatsLeak
specifier|public
class|class
name|JvmStatsLeak
block|{
DECL|method|main
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
block|{
while|while
condition|(
literal|true
condition|)
block|{
name|JvmStats
operator|.
name|jvmStats
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit
