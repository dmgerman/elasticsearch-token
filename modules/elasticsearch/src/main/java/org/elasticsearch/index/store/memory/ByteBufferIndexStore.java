begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.memory
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|memory
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
name|ByteSizeUnit
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
name|ByteSizeValue
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|AbstractIndexComponent
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|Index
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|settings
operator|.
name|IndexSettings
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|IndexStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|Store
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
name|jvm
operator|.
name|JvmInfo
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
name|jvm
operator|.
name|JvmStats
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|ByteBufferIndexStore
specifier|public
class|class
name|ByteBufferIndexStore
extends|extends
name|AbstractIndexComponent
implements|implements
name|IndexStore
block|{
DECL|field|direct
specifier|private
specifier|final
name|boolean
name|direct
decl_stmt|;
DECL|method|ByteBufferIndexStore
annotation|@
name|Inject
specifier|public
name|ByteBufferIndexStore
parameter_list|(
name|Index
name|index
parameter_list|,
annotation|@
name|IndexSettings
name|Settings
name|indexSettings
parameter_list|)
block|{
name|super
argument_list|(
name|index
argument_list|,
name|indexSettings
argument_list|)
expr_stmt|;
name|this
operator|.
name|direct
operator|=
name|componentSettings
operator|.
name|getAsBoolean
argument_list|(
literal|"direct"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
DECL|method|persistent
annotation|@
name|Override
specifier|public
name|boolean
name|persistent
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
DECL|method|shardStoreClass
annotation|@
name|Override
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|Store
argument_list|>
name|shardStoreClass
parameter_list|()
block|{
return|return
name|ByteBufferStore
operator|.
name|class
return|;
block|}
DECL|method|backingStoreTotalSpace
annotation|@
name|Override
specifier|public
name|ByteSizeValue
name|backingStoreTotalSpace
parameter_list|()
block|{
if|if
condition|(
name|direct
condition|)
block|{
comment|// TODO, we can use sigar...
return|return
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
return|;
block|}
return|return
name|JvmInfo
operator|.
name|jvmInfo
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|heapMax
argument_list|()
return|;
block|}
DECL|method|backingStoreFreeSpace
annotation|@
name|Override
specifier|public
name|ByteSizeValue
name|backingStoreFreeSpace
parameter_list|()
block|{
if|if
condition|(
name|direct
condition|)
block|{
return|return
operator|new
name|ByteSizeValue
argument_list|(
operator|-
literal|1
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
return|;
block|}
return|return
name|JvmStats
operator|.
name|jvmStats
argument_list|()
operator|.
name|mem
argument_list|()
operator|.
name|heapUsed
argument_list|()
return|;
block|}
block|}
end_class

end_unit

