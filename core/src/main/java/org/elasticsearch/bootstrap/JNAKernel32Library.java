begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.bootstrap
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|bootstrap
package|;
end_package

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jna
operator|.
name|IntegerType
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jna
operator|.
name|Native
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jna
operator|.
name|NativeLong
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jna
operator|.
name|Pointer
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jna
operator|.
name|Structure
import|;
end_import

begin_import
import|import
name|com
operator|.
name|sun
operator|.
name|jna
operator|.
name|win32
operator|.
name|StdCallLibrary
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|logging
operator|.
name|log4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|lucene
operator|.
name|util
operator|.
name|Constants
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
name|logging
operator|.
name|Loggers
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * Library for Windows/Kernel32  */
end_comment

begin_class
DECL|class|JNAKernel32Library
specifier|final
class|class
name|JNAKernel32Library
block|{
DECL|field|logger
specifier|private
specifier|static
specifier|final
name|Logger
name|logger
init|=
name|Loggers
operator|.
name|getLogger
argument_list|(
name|JNAKernel32Library
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// Callbacks must be kept around in order to be able to be called later,
comment|// when the Windows ConsoleCtrlHandler sends an event.
DECL|field|callbacks
specifier|private
name|List
argument_list|<
name|NativeHandlerCallback
argument_list|>
name|callbacks
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
comment|// Native library instance must be kept around for the same reason.
DECL|class|Holder
specifier|private
specifier|static
specifier|final
class|class
name|Holder
block|{
DECL|field|instance
specifier|private
specifier|static
specifier|final
name|JNAKernel32Library
name|instance
init|=
operator|new
name|JNAKernel32Library
argument_list|()
decl_stmt|;
block|}
DECL|method|JNAKernel32Library
specifier|private
name|JNAKernel32Library
parameter_list|()
block|{
if|if
condition|(
name|Constants
operator|.
name|WINDOWS
condition|)
block|{
try|try
block|{
name|Native
operator|.
name|register
argument_list|(
literal|"kernel32"
argument_list|)
expr_stmt|;
name|logger
operator|.
name|debug
argument_list|(
literal|"windows/Kernel32 library loaded"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|NoClassDefFoundError
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"JNA not found. native methods and handlers will be disabled."
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsatisfiedLinkError
name|e
parameter_list|)
block|{
name|logger
operator|.
name|warn
argument_list|(
literal|"unable to link Windows/Kernel32 library. native methods and handlers will be disabled."
argument_list|)
expr_stmt|;
block|}
block|}
block|}
DECL|method|getInstance
specifier|static
name|JNAKernel32Library
name|getInstance
parameter_list|()
block|{
return|return
name|Holder
operator|.
name|instance
return|;
block|}
comment|/**      * Adds a Console Ctrl Handler.      *      * @return true if the handler is correctly set      * @throws java.lang.UnsatisfiedLinkError if the Kernel32 library is not loaded or if the native function is not found      * @throws java.lang.NoClassDefFoundError if the library for native calls is missing      */
DECL|method|addConsoleCtrlHandler
name|boolean
name|addConsoleCtrlHandler
parameter_list|(
name|ConsoleCtrlHandler
name|handler
parameter_list|)
block|{
name|boolean
name|result
init|=
literal|false
decl_stmt|;
if|if
condition|(
name|handler
operator|!=
literal|null
condition|)
block|{
name|NativeHandlerCallback
name|callback
init|=
operator|new
name|NativeHandlerCallback
argument_list|(
name|handler
argument_list|)
decl_stmt|;
name|result
operator|=
name|SetConsoleCtrlHandler
argument_list|(
name|callback
argument_list|,
literal|true
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
condition|)
block|{
name|callbacks
operator|.
name|add
argument_list|(
name|callback
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
DECL|method|getCallbacks
name|List
argument_list|<
name|Object
argument_list|>
name|getCallbacks
parameter_list|()
block|{
return|return
name|Collections
operator|.
expr|<
name|Object
operator|>
name|unmodifiableList
argument_list|(
name|callbacks
argument_list|)
return|;
block|}
comment|/**      * Native call to the Kernel32 API to set a new Console Ctrl Handler.      *      * @return true if the handler is correctly set      * @throws java.lang.UnsatisfiedLinkError if the Kernel32 library is not loaded or if the native function is not found      * @throws java.lang.NoClassDefFoundError if the library for native calls is missing      */
DECL|method|SetConsoleCtrlHandler
specifier|native
name|boolean
name|SetConsoleCtrlHandler
parameter_list|(
name|StdCallLibrary
operator|.
name|StdCallCallback
name|handler
parameter_list|,
name|boolean
name|add
parameter_list|)
function_decl|;
comment|/**      * Handles consoles event with WIN API      *<p>      * See http://msdn.microsoft.com/en-us/library/windows/desktop/ms683242%28v=vs.85%29.aspx      */
DECL|class|NativeHandlerCallback
class|class
name|NativeHandlerCallback
implements|implements
name|StdCallLibrary
operator|.
name|StdCallCallback
block|{
DECL|field|handler
specifier|private
specifier|final
name|ConsoleCtrlHandler
name|handler
decl_stmt|;
DECL|method|NativeHandlerCallback
name|NativeHandlerCallback
parameter_list|(
name|ConsoleCtrlHandler
name|handler
parameter_list|)
block|{
name|this
operator|.
name|handler
operator|=
name|handler
expr_stmt|;
block|}
DECL|method|callback
specifier|public
name|boolean
name|callback
parameter_list|(
name|long
name|dwCtrlType
parameter_list|)
block|{
name|int
name|event
init|=
operator|(
name|int
operator|)
name|dwCtrlType
decl_stmt|;
if|if
condition|(
name|logger
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|logger
operator|.
name|debug
argument_list|(
literal|"console control handler receives event [{}@{}]"
argument_list|,
name|event
argument_list|,
name|dwCtrlType
argument_list|)
expr_stmt|;
block|}
return|return
name|handler
operator|.
name|handle
argument_list|(
name|event
argument_list|)
return|;
block|}
block|}
comment|/**      * Memory protection constraints      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/aa366786%28v=vs.85%29.aspx      */
DECL|field|PAGE_NOACCESS
specifier|public
specifier|static
specifier|final
name|int
name|PAGE_NOACCESS
init|=
literal|0x0001
decl_stmt|;
DECL|field|PAGE_GUARD
specifier|public
specifier|static
specifier|final
name|int
name|PAGE_GUARD
init|=
literal|0x0100
decl_stmt|;
DECL|field|MEM_COMMIT
specifier|public
specifier|static
specifier|final
name|int
name|MEM_COMMIT
init|=
literal|0x1000
decl_stmt|;
comment|/**      * Contains information about a range of pages in the virtual address space of a process.      * The VirtualQuery and VirtualQueryEx functions use this structure.      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/aa366775%28v=vs.85%29.aspx      */
DECL|class|MemoryBasicInformation
specifier|public
specifier|static
class|class
name|MemoryBasicInformation
extends|extends
name|Structure
block|{
DECL|field|BaseAddress
specifier|public
name|Pointer
name|BaseAddress
decl_stmt|;
DECL|field|AllocationBase
specifier|public
name|Pointer
name|AllocationBase
decl_stmt|;
DECL|field|AllocationProtect
specifier|public
name|NativeLong
name|AllocationProtect
decl_stmt|;
DECL|field|RegionSize
specifier|public
name|SizeT
name|RegionSize
decl_stmt|;
DECL|field|State
specifier|public
name|NativeLong
name|State
decl_stmt|;
DECL|field|Protect
specifier|public
name|NativeLong
name|Protect
decl_stmt|;
DECL|field|Type
specifier|public
name|NativeLong
name|Type
decl_stmt|;
annotation|@
name|Override
DECL|method|getFieldOrder
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getFieldOrder
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
literal|"BaseAddress"
argument_list|,
literal|"AllocationBase"
argument_list|,
literal|"AllocationProtect"
argument_list|,
literal|"RegionSize"
argument_list|,
literal|"State"
argument_list|,
literal|"Protect"
argument_list|,
literal|"Type"
argument_list|)
return|;
block|}
block|}
DECL|class|SizeT
specifier|public
specifier|static
class|class
name|SizeT
extends|extends
name|IntegerType
block|{
DECL|method|SizeT
name|SizeT
parameter_list|()
block|{
name|this
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
DECL|method|SizeT
name|SizeT
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|super
argument_list|(
name|Native
operator|.
name|SIZE_T_SIZE
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**      * Locks the specified region of the process's virtual address space into physical      * memory, ensuring that subsequent access to the region will not incur a page fault.      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/aa366895%28v=vs.85%29.aspx      *      * @param address A pointer to the base address of the region of pages to be locked.      * @param size The size of the region to be locked, in bytes.      * @return true if the function succeeds      */
DECL|method|VirtualLock
specifier|native
name|boolean
name|VirtualLock
parameter_list|(
name|Pointer
name|address
parameter_list|,
name|SizeT
name|size
parameter_list|)
function_decl|;
comment|/**      * Retrieves information about a range of pages within the virtual address space of a specified process.      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/aa366907%28v=vs.85%29.aspx      *      * @param handle A handle to the process whose memory information is queried.      * @param address A pointer to the base address of the region of pages to be queried.      * @param memoryInfo A pointer to a structure in which information about the specified page range is returned.      * @param length The size of the buffer pointed to by the memoryInfo parameter, in bytes.      * @return the actual number of bytes returned in the information buffer.      */
DECL|method|VirtualQueryEx
specifier|native
name|int
name|VirtualQueryEx
parameter_list|(
name|Pointer
name|handle
parameter_list|,
name|Pointer
name|address
parameter_list|,
name|MemoryBasicInformation
name|memoryInfo
parameter_list|,
name|int
name|length
parameter_list|)
function_decl|;
comment|/**      * Sets the minimum and maximum working set sizes for the specified process.      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms686234%28v=vs.85%29.aspx      *      * @param handle A handle to the process whose working set sizes is to be set.      * @param minSize The minimum working set size for the process, in bytes.      * @param maxSize The maximum working set size for the process, in bytes.      * @return true if the function succeeds.      */
DECL|method|SetProcessWorkingSetSize
specifier|native
name|boolean
name|SetProcessWorkingSetSize
parameter_list|(
name|Pointer
name|handle
parameter_list|,
name|SizeT
name|minSize
parameter_list|,
name|SizeT
name|maxSize
parameter_list|)
function_decl|;
comment|/**      * Retrieves a pseudo handle for the current process.      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms683179%28v=vs.85%29.aspx      *      * @return a pseudo handle to the current process.      */
DECL|method|GetCurrentProcess
specifier|native
name|Pointer
name|GetCurrentProcess
parameter_list|()
function_decl|;
comment|/**      * Closes an open object handle.      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms724211%28v=vs.85%29.aspx      *      * @param handle A valid handle to an open object.      * @return true if the function succeeds.      */
DECL|method|CloseHandle
specifier|native
name|boolean
name|CloseHandle
parameter_list|(
name|Pointer
name|handle
parameter_list|)
function_decl|;
comment|/**      * Creates or opens a new job object      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms682409%28v=vs.85%29.aspx      *      * @param jobAttributes security attributes      * @param name job name      * @return job handle if the function succeeds      */
DECL|method|CreateJobObjectW
specifier|native
name|Pointer
name|CreateJobObjectW
parameter_list|(
name|Pointer
name|jobAttributes
parameter_list|,
name|String
name|name
parameter_list|)
function_decl|;
comment|/**      * Associates a process with an existing job      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms681949%28v=vs.85%29.aspx      *      * @param job job handle      * @param process process handle      * @return true if the function succeeds      */
DECL|method|AssignProcessToJobObject
specifier|native
name|boolean
name|AssignProcessToJobObject
parameter_list|(
name|Pointer
name|job
parameter_list|,
name|Pointer
name|process
parameter_list|)
function_decl|;
comment|/**      * Basic limit information for a job object      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms684147%28v=vs.85%29.aspx      */
DECL|class|JOBOBJECT_BASIC_LIMIT_INFORMATION
specifier|public
specifier|static
class|class
name|JOBOBJECT_BASIC_LIMIT_INFORMATION
extends|extends
name|Structure
implements|implements
name|Structure
operator|.
name|ByReference
block|{
DECL|field|PerProcessUserTimeLimit
specifier|public
name|long
name|PerProcessUserTimeLimit
decl_stmt|;
DECL|field|PerJobUserTimeLimit
specifier|public
name|long
name|PerJobUserTimeLimit
decl_stmt|;
DECL|field|LimitFlags
specifier|public
name|int
name|LimitFlags
decl_stmt|;
DECL|field|MinimumWorkingSetSize
specifier|public
name|SizeT
name|MinimumWorkingSetSize
decl_stmt|;
DECL|field|MaximumWorkingSetSize
specifier|public
name|SizeT
name|MaximumWorkingSetSize
decl_stmt|;
DECL|field|ActiveProcessLimit
specifier|public
name|int
name|ActiveProcessLimit
decl_stmt|;
DECL|field|Affinity
specifier|public
name|Pointer
name|Affinity
decl_stmt|;
DECL|field|PriorityClass
specifier|public
name|int
name|PriorityClass
decl_stmt|;
DECL|field|SchedulingClass
specifier|public
name|int
name|SchedulingClass
decl_stmt|;
annotation|@
name|Override
DECL|method|getFieldOrder
specifier|protected
name|List
argument_list|<
name|String
argument_list|>
name|getFieldOrder
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
literal|"PerProcessUserTimeLimit"
argument_list|,
literal|"PerJobUserTimeLimit"
argument_list|,
literal|"LimitFlags"
argument_list|,
literal|"MinimumWorkingSetSize"
argument_list|,
literal|"MaximumWorkingSetSize"
argument_list|,
literal|"ActiveProcessLimit"
argument_list|,
literal|"Affinity"
argument_list|,
literal|"PriorityClass"
argument_list|,
literal|"SchedulingClass"
argument_list|)
return|;
block|}
block|}
comment|/**      * Constant for JOBOBJECT_BASIC_LIMIT_INFORMATION in Query/Set InformationJobObject      */
DECL|field|JOBOBJECT_BASIC_LIMIT_INFORMATION_CLASS
specifier|static
specifier|final
name|int
name|JOBOBJECT_BASIC_LIMIT_INFORMATION_CLASS
init|=
literal|2
decl_stmt|;
comment|/**      * Constant for LimitFlags, indicating a process limit has been set      */
DECL|field|JOB_OBJECT_LIMIT_ACTIVE_PROCESS
specifier|static
specifier|final
name|int
name|JOB_OBJECT_LIMIT_ACTIVE_PROCESS
init|=
literal|8
decl_stmt|;
comment|/**      * Get job limit and state information      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms684925%28v=vs.85%29.aspx      *      * @param job job handle      * @param infoClass information class constant      * @param info pointer to information structure      * @param infoLength size of information structure      * @param returnLength length of data written back to structure (or null if not wanted)      * @return true if the function succeeds      */
DECL|method|QueryInformationJobObject
specifier|native
name|boolean
name|QueryInformationJobObject
parameter_list|(
name|Pointer
name|job
parameter_list|,
name|int
name|infoClass
parameter_list|,
name|Pointer
name|info
parameter_list|,
name|int
name|infoLength
parameter_list|,
name|Pointer
name|returnLength
parameter_list|)
function_decl|;
comment|/**      * Set job limit and state information      *      * https://msdn.microsoft.com/en-us/library/windows/desktop/ms686216%28v=vs.85%29.aspx      *      * @param job job handle      * @param infoClass information class constant      * @param info pointer to information structure      * @param infoLength size of information structure      * @return true if the function succeeds      */
DECL|method|SetInformationJobObject
specifier|native
name|boolean
name|SetInformationJobObject
parameter_list|(
name|Pointer
name|job
parameter_list|,
name|int
name|infoClass
parameter_list|,
name|Pointer
name|info
parameter_list|,
name|int
name|infoLength
parameter_list|)
function_decl|;
block|}
end_class

end_unit

