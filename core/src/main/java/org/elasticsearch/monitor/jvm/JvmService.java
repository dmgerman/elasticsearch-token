begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.monitor.jvm
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|jvm
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
name|component
operator|.
name|AbstractComponent
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
name|inject
operator|.
name|Inject
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
name|common
operator|.
name|unit
operator|.
name|TimeValue
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|JvmService
specifier|public
class|class
name|JvmService
extends|extends
name|AbstractComponent
block|{
DECL|field|jvmInfo
specifier|private
specifier|final
name|JvmInfo
name|jvmInfo
decl_stmt|;
DECL|field|refreshInterval
specifier|private
specifier|final
name|TimeValue
name|refreshInterval
decl_stmt|;
DECL|field|jvmStats
specifier|private
name|JvmStats
name|jvmStats
decl_stmt|;
annotation|@
name|Inject
DECL|method|JvmService
specifier|public
name|JvmService
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|super
argument_list|(
name|settings
argument_list|)
expr_stmt|;
name|this
operator|.
name|jvmInfo
operator|=
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
expr_stmt|;
name|this
operator|.
name|jvmStats
operator|=
name|JvmStats
operator|.
name|jvmStats
argument_list|()
expr_stmt|;
name|this
operator|.
name|refreshInterval
operator|=
name|this
operator|.
name|settings
operator|.
name|getAsTime
argument_list|(
literal|"refresh_interval"
argument_list|,
name|TimeValue
operator|.
name|timeValueSeconds
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"Using refresh_interval [{}]"
argument_list|,
name|refreshInterval
argument_list|)
expr_stmt|;
block|}
DECL|method|info
specifier|public
name|JvmInfo
name|info
parameter_list|()
block|{
return|return
name|this
operator|.
name|jvmInfo
return|;
block|}
DECL|method|stats
specifier|public
specifier|synchronized
name|JvmStats
name|stats
parameter_list|()
block|{
if|if
condition|(
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|jvmStats
operator|.
name|getTimestamp
argument_list|()
operator|)
operator|>
name|refreshInterval
operator|.
name|millis
argument_list|()
condition|)
block|{
name|jvmStats
operator|=
name|JvmStats
operator|.
name|jvmStats
argument_list|()
expr_stmt|;
block|}
return|return
name|jvmStats
return|;
block|}
block|}
end_class

end_unit
