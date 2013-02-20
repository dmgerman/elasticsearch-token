begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to ElasticSearch and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. ElasticSearch licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.store.distributor
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|store
operator|.
name|distributor
package|;
end_package

begin_import
import|import
name|jsr166y
operator|.
name|ThreadLocalRandom
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
name|store
operator|.
name|Directory
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
name|store
operator|.
name|FSDirectory
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
name|index
operator|.
name|store
operator|.
name|DirectoryService
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

begin_comment
comment|/**  * Implements directory distributor that always return the directory is the most available space  */
end_comment

begin_class
DECL|class|LeastUsedDistributor
specifier|public
class|class
name|LeastUsedDistributor
extends|extends
name|AbstractDistributor
block|{
annotation|@
name|Inject
DECL|method|LeastUsedDistributor
specifier|public
name|LeastUsedDistributor
parameter_list|(
name|DirectoryService
name|directoryService
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|directoryService
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doAny
specifier|public
name|Directory
name|doAny
parameter_list|()
block|{
name|Directory
name|directory
init|=
literal|null
decl_stmt|;
name|long
name|size
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
for|for
control|(
name|Directory
name|delegate
range|:
name|delegates
control|)
block|{
if|if
condition|(
name|delegate
operator|instanceof
name|FSDirectory
condition|)
block|{
name|long
name|currentSize
init|=
operator|(
operator|(
name|FSDirectory
operator|)
name|delegate
operator|)
operator|.
name|getDirectory
argument_list|()
operator|.
name|getUsableSpace
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentSize
operator|>
name|size
condition|)
block|{
name|size
operator|=
name|currentSize
expr_stmt|;
name|directory
operator|=
name|delegate
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|currentSize
operator|==
name|size
operator|&&
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
operator|.
name|nextBoolean
argument_list|()
condition|)
block|{
name|directory
operator|=
name|delegate
expr_stmt|;
block|}
else|else
block|{                 }
block|}
else|else
block|{
name|directory
operator|=
name|delegate
expr_stmt|;
comment|// really, make sense to have multiple directories for FS
block|}
block|}
return|return
name|directory
return|;
block|}
block|}
end_class

end_unit

