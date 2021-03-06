begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.index.snapshots.blobstore
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|index
operator|.
name|snapshots
operator|.
name|blobstore
package|;
end_package

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
name|RateLimiter
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilterInputStream
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
name|io
operator|.
name|InputStream
import|;
end_import

begin_comment
comment|/**  * Rate limiting wrapper for InputStream  */
end_comment

begin_class
DECL|class|RateLimitingInputStream
specifier|public
class|class
name|RateLimitingInputStream
extends|extends
name|FilterInputStream
block|{
DECL|field|rateLimiter
specifier|private
specifier|final
name|RateLimiter
name|rateLimiter
decl_stmt|;
DECL|field|listener
specifier|private
specifier|final
name|Listener
name|listener
decl_stmt|;
DECL|field|bytesSinceLastRateLimit
specifier|private
name|long
name|bytesSinceLastRateLimit
decl_stmt|;
DECL|interface|Listener
specifier|public
interface|interface
name|Listener
block|{
DECL|method|onPause
name|void
name|onPause
parameter_list|(
name|long
name|nanos
parameter_list|)
function_decl|;
block|}
DECL|method|RateLimitingInputStream
specifier|public
name|RateLimitingInputStream
parameter_list|(
name|InputStream
name|delegate
parameter_list|,
name|RateLimiter
name|rateLimiter
parameter_list|,
name|Listener
name|listener
parameter_list|)
block|{
name|super
argument_list|(
name|delegate
argument_list|)
expr_stmt|;
name|this
operator|.
name|rateLimiter
operator|=
name|rateLimiter
expr_stmt|;
name|this
operator|.
name|listener
operator|=
name|listener
expr_stmt|;
block|}
DECL|method|maybePause
specifier|private
name|void
name|maybePause
parameter_list|(
name|int
name|bytes
parameter_list|)
throws|throws
name|IOException
block|{
name|bytesSinceLastRateLimit
operator|+=
name|bytes
expr_stmt|;
if|if
condition|(
name|bytesSinceLastRateLimit
operator|>=
name|rateLimiter
operator|.
name|getMinPauseCheckBytes
argument_list|()
condition|)
block|{
name|long
name|pause
init|=
name|rateLimiter
operator|.
name|pause
argument_list|(
name|bytesSinceLastRateLimit
argument_list|)
decl_stmt|;
name|bytesSinceLastRateLimit
operator|=
literal|0
expr_stmt|;
if|if
condition|(
name|pause
operator|>
literal|0
condition|)
block|{
name|listener
operator|.
name|onPause
argument_list|(
name|pause
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
name|int
name|b
init|=
name|super
operator|.
name|read
argument_list|()
decl_stmt|;
name|maybePause
argument_list|(
literal|1
argument_list|)
expr_stmt|;
return|return
name|b
return|;
block|}
annotation|@
name|Override
DECL|method|read
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|n
init|=
name|super
operator|.
name|read
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|n
operator|>
literal|0
condition|)
block|{
name|maybePause
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
return|return
name|n
return|;
block|}
block|}
end_class

end_unit

