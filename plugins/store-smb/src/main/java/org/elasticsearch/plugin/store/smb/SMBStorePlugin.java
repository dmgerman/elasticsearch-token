begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.plugin.store.smb
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|plugin
operator|.
name|store
operator|.
name|smb
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
name|IndexModule
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
name|smbmmapfs
operator|.
name|SmbMmapFsIndexStore
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
name|smbsimplefs
operator|.
name|SmbSimpleFsIndexStore
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|plugins
operator|.
name|Plugin
import|;
end_import

begin_class
DECL|class|SMBStorePlugin
specifier|public
class|class
name|SMBStorePlugin
extends|extends
name|Plugin
block|{
annotation|@
name|Override
DECL|method|onIndexModule
specifier|public
name|void
name|onIndexModule
parameter_list|(
name|IndexModule
name|indexModule
parameter_list|)
block|{
name|indexModule
operator|.
name|addIndexStore
argument_list|(
literal|"smb_mmap_fs"
argument_list|,
name|SmbMmapFsIndexStore
operator|::
operator|new
argument_list|)
expr_stmt|;
name|indexModule
operator|.
name|addIndexStore
argument_list|(
literal|"smb_simple_fs"
argument_list|,
name|SmbSimpleFsIndexStore
operator|::
operator|new
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

