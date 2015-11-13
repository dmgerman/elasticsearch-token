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
name|BootstrapInfo
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|monitor
operator|.
name|Probes
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

begin_class
DECL|class|ProcessProbe
specifier|public
class|class
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
DECL|field|getProcessCpuLoad
specifier|private
specifier|static
specifier|final
name|Method
name|getProcessCpuLoad
decl_stmt|;
DECL|field|getProcessCpuTime
specifier|private
specifier|static
specifier|final
name|Method
name|getProcessCpuTime
decl_stmt|;
DECL|field|getCommittedVirtualMemorySize
specifier|private
specifier|static
specifier|final
name|Method
name|getCommittedVirtualMemorySize
decl_stmt|;
static|static
block|{
name|getMaxFileDescriptorCountField
operator|=
name|getUnixMethod
argument_list|(
literal|"getMaxFileDescriptorCount"
argument_list|)
expr_stmt|;
name|getOpenFileDescriptorCountField
operator|=
name|getUnixMethod
argument_list|(
literal|"getOpenFileDescriptorCount"
argument_list|)
expr_stmt|;
name|getProcessCpuLoad
operator|=
name|getMethod
argument_list|(
literal|"getProcessCpuLoad"
argument_list|)
expr_stmt|;
name|getProcessCpuTime
operator|=
name|getMethod
argument_list|(
literal|"getProcessCpuTime"
argument_list|)
expr_stmt|;
name|getCommittedVirtualMemorySize
operator|=
name|getMethod
argument_list|(
literal|"getCommittedVirtualMemorySize"
argument_list|)
expr_stmt|;
block|}
DECL|class|ProcessProbeHolder
specifier|private
specifier|static
class|class
name|ProcessProbeHolder
block|{
DECL|field|INSTANCE
specifier|private
specifier|final
specifier|static
name|ProcessProbe
name|INSTANCE
init|=
operator|new
name|ProcessProbe
argument_list|()
decl_stmt|;
block|}
DECL|method|getInstance
specifier|public
specifier|static
name|ProcessProbe
name|getInstance
parameter_list|()
block|{
return|return
name|ProcessProbeHolder
operator|.
name|INSTANCE
return|;
block|}
DECL|method|ProcessProbe
specifier|private
name|ProcessProbe
parameter_list|()
block|{     }
comment|/**      * Returns the maximum number of file descriptors allowed on the system, or -1 if not supported.      */
DECL|method|getMaxFileDescriptorCount
specifier|public
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
name|Throwable
name|t
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
comment|/**      * Returns the number of opened file descriptors associated with the current process, or -1 if not supported.      */
DECL|method|getOpenFileDescriptorCount
specifier|public
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
name|Throwable
name|t
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
comment|/**      * Returns the process CPU usage in percent      */
DECL|method|getProcessCpuPercent
specifier|public
name|short
name|getProcessCpuPercent
parameter_list|()
block|{
return|return
name|Probes
operator|.
name|getLoadAndScaleToPercent
argument_list|(
name|getProcessCpuLoad
argument_list|,
name|osMxBean
argument_list|)
return|;
block|}
comment|/**      * Returns the CPU time (in milliseconds) used by the process on which the Java virtual machine is running, or -1 if not supported.      */
DECL|method|getProcessCpuTotalTime
specifier|public
name|long
name|getProcessCpuTotalTime
parameter_list|()
block|{
if|if
condition|(
name|getProcessCpuTime
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|long
name|time
init|=
operator|(
name|long
operator|)
name|getProcessCpuTime
operator|.
name|invoke
argument_list|(
name|osMxBean
argument_list|)
decl_stmt|;
if|if
condition|(
name|time
operator|>=
literal|0
condition|)
block|{
return|return
operator|(
name|time
operator|/
literal|1_000_000L
operator|)
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
comment|/**      * Returns the size (in bytes) of virtual memory that is guaranteed to be available to the running process      */
DECL|method|getTotalVirtualMemorySize
specifier|public
name|long
name|getTotalVirtualMemorySize
parameter_list|()
block|{
if|if
condition|(
name|getCommittedVirtualMemorySize
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|long
name|virtual
init|=
operator|(
name|long
operator|)
name|getCommittedVirtualMemorySize
operator|.
name|invoke
argument_list|(
name|osMxBean
argument_list|)
decl_stmt|;
if|if
condition|(
name|virtual
operator|>=
literal|0
condition|)
block|{
return|return
name|virtual
return|;
block|}
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
return|return
operator|-
literal|1
return|;
block|}
block|}
return|return
operator|-
literal|1
return|;
block|}
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
name|BootstrapInfo
operator|.
name|isMemoryLocked
argument_list|()
argument_list|)
return|;
block|}
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
name|stats
operator|.
name|maxFileDescriptors
operator|=
name|getMaxFileDescriptorCount
argument_list|()
expr_stmt|;
name|ProcessStats
operator|.
name|Cpu
name|cpu
init|=
operator|new
name|ProcessStats
operator|.
name|Cpu
argument_list|()
decl_stmt|;
name|cpu
operator|.
name|percent
operator|=
name|getProcessCpuPercent
argument_list|()
expr_stmt|;
name|cpu
operator|.
name|total
operator|=
name|getProcessCpuTotalTime
argument_list|()
expr_stmt|;
name|stats
operator|.
name|cpu
operator|=
name|cpu
expr_stmt|;
name|ProcessStats
operator|.
name|Mem
name|mem
init|=
operator|new
name|ProcessStats
operator|.
name|Mem
argument_list|()
decl_stmt|;
name|mem
operator|.
name|totalVirtual
operator|=
name|getTotalVirtualMemorySize
argument_list|()
expr_stmt|;
name|stats
operator|.
name|mem
operator|=
name|mem
expr_stmt|;
return|return
name|stats
return|;
block|}
comment|/**      * Returns a given method of the OperatingSystemMXBean,      * or null if the method is not found or unavailable.      */
DECL|method|getMethod
specifier|private
specifier|static
name|Method
name|getMethod
parameter_list|(
name|String
name|methodName
parameter_list|)
block|{
try|try
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
literal|"com.sun.management.OperatingSystemMXBean"
argument_list|)
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// not available
return|return
literal|null
return|;
block|}
block|}
comment|/**      * Returns a given method of the UnixOperatingSystemMXBean,      * or null if the method is not found or unavailable.      */
DECL|method|getUnixMethod
specifier|private
specifier|static
name|Method
name|getUnixMethod
parameter_list|(
name|String
name|methodName
parameter_list|)
block|{
try|try
block|{
return|return
name|Class
operator|.
name|forName
argument_list|(
literal|"com.sun.management.UnixOperatingSystemMXBean"
argument_list|)
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
comment|// not available
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit

