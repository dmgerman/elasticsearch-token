begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.translog
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|translog
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|cluster
operator|.
name|metadata
operator|.
name|IndexMetaData
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
name|util
operator|.
name|BigArrays
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
name|IndexSettings
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
name|IndexSettingsModule
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|file
operator|.
name|Path
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

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|BufferedTranslogTests
specifier|public
class|class
name|BufferedTranslogTests
extends|extends
name|TranslogTests
block|{
annotation|@
name|Override
DECL|method|create
specifier|protected
name|Translog
name|create
parameter_list|(
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|Settings
name|build
init|=
name|Settings
operator|.
name|settingsBuilder
argument_list|()
operator|.
name|put
argument_list|(
literal|"index.translog.fs.type"
argument_list|,
name|TranslogWriter
operator|.
name|Type
operator|.
name|BUFFERED
operator|.
name|name
argument_list|()
argument_list|)
operator|.
name|put
argument_list|(
literal|"index.translog.fs.buffer_size"
argument_list|,
literal|10
operator|+
name|randomInt
argument_list|(
literal|128
operator|*
literal|1024
argument_list|)
argument_list|,
name|ByteSizeUnit
operator|.
name|BYTES
argument_list|)
operator|.
name|put
argument_list|(
name|IndexMetaData
operator|.
name|SETTING_VERSION_CREATED
argument_list|,
name|org
operator|.
name|elasticsearch
operator|.
name|Version
operator|.
name|CURRENT
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TranslogConfig
name|translogConfig
init|=
operator|new
name|TranslogConfig
argument_list|(
name|shardId
argument_list|,
name|path
argument_list|,
name|IndexSettingsModule
operator|.
name|newIndexSettings
argument_list|(
name|shardId
operator|.
name|index
argument_list|()
argument_list|,
name|build
argument_list|,
name|Collections
operator|.
name|EMPTY_LIST
argument_list|)
argument_list|,
name|Translog
operator|.
name|Durabilty
operator|.
name|REQUEST
argument_list|,
name|BigArrays
operator|.
name|NON_RECYCLING_INSTANCE
argument_list|,
literal|null
argument_list|)
decl_stmt|;
return|return
operator|new
name|Translog
argument_list|(
name|translogConfig
argument_list|)
return|;
block|}
block|}
end_class

end_unit

