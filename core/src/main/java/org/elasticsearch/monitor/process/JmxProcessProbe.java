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
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|OperatingSystemMXBean
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|JmxProcessProbe
specifier|public
class|class
name|JmxProcessProbe
extends|extends
name|AbstractComponent
implements|implements
name|ProcessProbe
block|{
DECL|field|osMxBean
specifier|private
specifier|static
specifier|final
name|OperatingSystemMXBean
name|osMxBean
init|=
name|ManagementFactory
operator|.
name|getOperatingSystemMXBean
argument_list|()
decl_stmt|;
DECL|field|getMaxFileDescriptorCountField
specifier|private
specifier|static
specifier|final
name|Method
name|getMaxFileDescriptorCountField
decl_stmt|;
DECL|field|getOpenFileDescriptorCountField
specifier|private
specifier|static
specifier|final
name|Method
name|getOpenFileDescriptorCountField
decl_stmt|;
static|static
block|{
name|Method
name|method
init|=
literal|null
decl_stmt|;
try|try
block|{
name|method
operator|=
name|osMxBean
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getMaxFileDescriptorCount"
argument_list|)
expr_stmt|;
name|method
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// not available
block|}
name|getMaxFileDescriptorCountField
operator|=
name|method
expr_stmt|;
name|method
operator|=
literal|null
expr_stmt|;
try|try
block|{
name|method
operator|=
name|osMxBean
operator|.
name|getClass
argument_list|()
operator|.
name|getDeclaredMethod
argument_list|(
literal|"getOpenFileDescriptorCount"
argument_list|)
expr_stmt|;
name|method
operator|.
name|setAccessible
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
comment|// not available
block|}
name|getOpenFileDescriptorCountField
operator|=
name|method
expr_stmt|;
block|}
DECL|method|getMaxFileDescriptorCount
specifier|public
specifier|static
name|long
name|getMaxFileDescriptorCount
parameter_list|()
block|{
if|if
condition|(
name|getMaxFileDescriptorCountField
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
try|try
block|{
return|return
operator|(
name|Long
operator|)
name|getMaxFileDescriptorCountField
operator|.
name|invoke
argument_list|(
name|osMxBean
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
DECL|method|getOpenFileDescriptorCount
specifier|public
specifier|static
name|long
name|getOpenFileDescriptorCount
parameter_list|()
block|{
if|if
condition|(
name|getOpenFileDescriptorCountField
operator|==
literal|null
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
try|try
block|{
return|return
operator|(
name|Long
operator|)
name|getOpenFileDescriptorCountField
operator|.
name|invoke
argument_list|(
name|osMxBean
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
annotation|@
name|Inject
DECL|method|JmxProcessProbe
specifier|public
name|JmxProcessProbe
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
block|}
annotation|@
name|Override
DECL|method|processInfo
specifier|public
name|ProcessInfo
name|processInfo
parameter_list|()
block|{
return|return
operator|new
name|ProcessInfo
argument_list|(
name|jvmInfo
argument_list|()
operator|.
name|pid
argument_list|()
argument_list|,
name|getMaxFileDescriptorCount
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|processStats
specifier|public
name|ProcessStats
name|processStats
parameter_list|()
block|{
name|ProcessStats
name|stats
init|=
operator|new
name|ProcessStats
argument_list|()
decl_stmt|;
name|stats
operator|.
name|timestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
name|stats
operator|.
name|openFileDescriptors
operator|=
name|getOpenFileDescriptorCount
argument_list|()
expr_stmt|;
return|return
name|stats
return|;
block|}
block|}
end_class

end_unit
