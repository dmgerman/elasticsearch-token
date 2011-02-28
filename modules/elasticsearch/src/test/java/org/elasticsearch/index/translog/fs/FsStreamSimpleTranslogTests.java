begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog.fs
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
operator|.
name|fs
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
name|FileSystemUtils
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
name|translog
operator|.
name|AbstractSimpleTranslogTests
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
name|translog
operator|.
name|Translog
import|;
end_import

begin_import
import|import
name|org
operator|.
name|testng
operator|.
name|annotations
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|File
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|settings
operator|.
name|ImmutableSettings
operator|.
name|Builder
operator|.
name|*
import|;
end_import

begin_comment
comment|/**  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|FsStreamSimpleTranslogTests
specifier|public
class|class
name|FsStreamSimpleTranslogTests
extends|extends
name|AbstractSimpleTranslogTests
block|{
DECL|method|create
annotation|@
name|Override
specifier|protected
name|Translog
name|create
parameter_list|()
block|{
return|return
operator|new
name|FsTranslog
argument_list|(
name|shardId
argument_list|,
name|EMPTY_SETTINGS
argument_list|,
operator|new
name|File
argument_list|(
literal|"work/fs-translog"
argument_list|)
argument_list|,
literal|true
argument_list|)
return|;
block|}
DECL|method|cleanup
annotation|@
name|AfterClass
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
name|FileSystemUtils
operator|.
name|deleteRecursively
argument_list|(
operator|new
name|File
argument_list|(
literal|"work/fs-translog"
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

