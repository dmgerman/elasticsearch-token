begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Iterables
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
name|IOUtils
import|;
end_import

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ExceptionsHelper
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
name|concurrent
operator|.
name|AbstractRefCounted
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
name|concurrent
operator|.
name|ConcurrentCollections
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
name|TranslogStream
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
name|TranslogStreams
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
name|channels
operator|.
name|FileChannel
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
name|OpenOption
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|IdentityHashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
DECL|class|ChannelReference
class|class
name|ChannelReference
extends|extends
name|AbstractRefCounted
block|{
DECL|field|file
specifier|private
specifier|final
name|Path
name|file
decl_stmt|;
DECL|field|channel
specifier|private
specifier|final
name|FileChannel
name|channel
decl_stmt|;
DECL|field|stream
specifier|private
specifier|final
name|TranslogStream
name|stream
decl_stmt|;
DECL|method|ChannelReference
specifier|public
name|ChannelReference
parameter_list|(
name|Path
name|file
parameter_list|,
name|OpenOption
modifier|...
name|openOptions
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|file
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|file
operator|=
name|file
expr_stmt|;
name|this
operator|.
name|channel
operator|=
name|FileChannel
operator|.
name|open
argument_list|(
name|file
argument_list|,
name|openOptions
argument_list|)
expr_stmt|;
try|try
block|{
name|this
operator|.
name|stream
operator|=
name|TranslogStreams
operator|.
name|translogStreamFor
argument_list|(
name|file
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|channel
argument_list|)
expr_stmt|;
throw|throw
name|t
throw|;
block|}
block|}
DECL|method|file
specifier|public
name|Path
name|file
parameter_list|()
block|{
return|return
name|this
operator|.
name|file
return|;
block|}
DECL|method|channel
specifier|public
name|FileChannel
name|channel
parameter_list|()
block|{
return|return
name|this
operator|.
name|channel
return|;
block|}
DECL|method|stream
specifier|public
name|TranslogStream
name|stream
parameter_list|()
block|{
return|return
name|this
operator|.
name|stream
return|;
block|}
annotation|@
name|Override
DECL|method|toString
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"channel: file ["
operator|+
name|file
operator|+
literal|"], ref count ["
operator|+
name|refCount
argument_list|()
operator|+
literal|"]"
return|;
block|}
annotation|@
name|Override
DECL|method|closeInternal
specifier|protected
name|void
name|closeInternal
parameter_list|()
block|{
name|IOUtils
operator|.
name|closeWhileHandlingException
argument_list|(
name|channel
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

