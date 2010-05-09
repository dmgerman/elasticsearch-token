begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
package|;
end_package

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
name|fs
operator|.
name|MmapFsStoreModule
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
name|fs
operator|.
name|NioFsStoreModule
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
name|fs
operator|.
name|SimpleFsStoreModule
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
name|memory
operator|.
name|MemoryStoreModule
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
name|ram
operator|.
name|RamStoreModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|OsUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|guice
operator|.
name|ModulesFactory
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|AbstractModule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|inject
operator|.
name|Module
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|util
operator|.
name|settings
operator|.
name|Settings
import|;
end_import

begin_comment
comment|/**  * @author kimchy (Shay Banon)  */
end_comment

begin_class
DECL|class|StoreModule
specifier|public
class|class
name|StoreModule
extends|extends
name|AbstractModule
block|{
DECL|field|settings
specifier|private
specifier|final
name|Settings
name|settings
decl_stmt|;
DECL|method|StoreModule
specifier|public
name|StoreModule
parameter_list|(
name|Settings
name|settings
parameter_list|)
block|{
name|this
operator|.
name|settings
operator|=
name|settings
expr_stmt|;
block|}
DECL|method|configure
annotation|@
name|Override
specifier|protected
name|void
name|configure
parameter_list|()
block|{
name|Class
argument_list|<
name|?
extends|extends
name|Module
argument_list|>
name|storeModule
init|=
name|NioFsStoreModule
operator|.
name|class
decl_stmt|;
if|if
condition|(
name|OsUtils
operator|.
name|WINDOWS
condition|)
block|{
name|storeModule
operator|=
name|SimpleFsStoreModule
operator|.
name|class
expr_stmt|;
block|}
name|String
name|storeType
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"index.store.type"
argument_list|)
decl_stmt|;
if|if
condition|(
literal|"ram"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
condition|)
block|{
name|storeModule
operator|=
name|RamStoreModule
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"memory"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
condition|)
block|{
name|storeModule
operator|=
name|MemoryStoreModule
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"fs"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
condition|)
block|{
comment|// nothing to set here ... (we default to fs)
block|}
elseif|else
if|if
condition|(
literal|"simplefs"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
operator|||
literal|"simple_fs"
operator|.
name|equals
argument_list|(
name|storeType
argument_list|)
condition|)
block|{
name|storeModule
operator|=
name|SimpleFsStoreModule
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"niofs"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
operator|||
literal|"nio_fs"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
condition|)
block|{
name|storeModule
operator|=
name|NioFsStoreModule
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
literal|"mmapfs"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
operator|||
literal|"mmap_fs"
operator|.
name|equalsIgnoreCase
argument_list|(
name|storeType
argument_list|)
condition|)
block|{
name|storeModule
operator|=
name|MmapFsStoreModule
operator|.
name|class
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|storeType
operator|!=
literal|null
condition|)
block|{
name|storeModule
operator|=
name|settings
operator|.
name|getAsClass
argument_list|(
literal|"index.store.type"
argument_list|,
name|storeModule
argument_list|,
literal|"org.elasticsearch.index.store."
argument_list|,
literal|"StoreModule"
argument_list|)
expr_stmt|;
block|}
name|ModulesFactory
operator|.
name|createModule
argument_list|(
name|storeModule
argument_list|,
name|settings
argument_list|)
operator|.
name|configure
argument_list|(
name|binder
argument_list|()
argument_list|)
expr_stmt|;
name|bind
argument_list|(
name|StoreManagement
operator|.
name|class
argument_list|)
operator|.
name|asEagerSingleton
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

