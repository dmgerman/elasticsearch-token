begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticSearchInterruptedException
import|;
end_import

begin_comment
comment|/**  */
end_comment

begin_comment
comment|// LUCENE MONITOR: Taken from trunk of Lucene at 06-09-11
end_comment

begin_class
DECL|class|RateLimiter
specifier|public
class|class
name|RateLimiter
block|{
DECL|field|nsPerByte
specifier|private
specifier|volatile
name|double
name|nsPerByte
decl_stmt|;
DECL|field|lastNS
specifier|private
specifier|volatile
name|long
name|lastNS
decl_stmt|;
comment|// TODO: we could also allow eg a sub class to dynamically
comment|// determine the allowed rate, eg if an app wants to
comment|// change the allowed rate over time or something
comment|/**      * mbPerSec is the MB/sec max IO rate      */
DECL|method|RateLimiter
specifier|public
name|RateLimiter
parameter_list|(
name|double
name|mbPerSec
parameter_list|)
block|{
name|setMaxRate
argument_list|(
name|mbPerSec
argument_list|)
expr_stmt|;
block|}
DECL|method|setMaxRate
specifier|public
name|void
name|setMaxRate
parameter_list|(
name|double
name|mbPerSec
parameter_list|)
block|{
name|nsPerByte
operator|=
literal|1000000000.
operator|/
operator|(
literal|1024
operator|*
literal|1024
operator|*
name|mbPerSec
operator|)
expr_stmt|;
block|}
comment|/**      * Pauses, if necessary, to keep the instantaneous IO      * rate at or below the target. NOTE: multiple threads      * may safely use this, however the implementation is      * not perfectly thread safe but likely in practice this      * is harmless (just means in some rare cases the rate      * might exceed the target).  It's best to call this      * with a biggish count, not one byte at a time.      */
DECL|method|pause
specifier|public
name|void
name|pause
parameter_list|(
name|long
name|bytes
parameter_list|)
block|{
comment|// TODO: this is purely instantenous rate; maybe we
comment|// should also offer decayed recent history one?
specifier|final
name|long
name|targetNS
init|=
name|lastNS
operator|=
name|lastNS
operator|+
operator|(
call|(
name|long
call|)
argument_list|(
name|bytes
operator|*
name|nsPerByte
argument_list|)
operator|)
decl_stmt|;
name|long
name|curNS
init|=
name|System
operator|.
name|nanoTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|lastNS
operator|<
name|curNS
condition|)
block|{
name|lastNS
operator|=
name|curNS
expr_stmt|;
block|}
comment|// While loop because Thread.sleep doesn't alway sleep
comment|// enough:
while|while
condition|(
literal|true
condition|)
block|{
specifier|final
name|long
name|pauseNS
init|=
name|targetNS
operator|-
name|curNS
decl_stmt|;
if|if
condition|(
name|pauseNS
operator|>
literal|0
condition|)
block|{
try|try
block|{
name|Thread
operator|.
name|sleep
argument_list|(
call|(
name|int
call|)
argument_list|(
name|pauseNS
operator|/
literal|1000000
argument_list|)
argument_list|,
call|(
name|int
call|)
argument_list|(
name|pauseNS
operator|%
literal|1000000
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|ie
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticSearchInterruptedException
argument_list|(
literal|"interrupted while rate limiting"
argument_list|,
name|ie
argument_list|)
throw|;
block|}
name|curNS
operator|=
name|System
operator|.
name|nanoTime
argument_list|()
expr_stmt|;
continue|continue;
block|}
break|break;
block|}
block|}
block|}
end_class

end_unit

