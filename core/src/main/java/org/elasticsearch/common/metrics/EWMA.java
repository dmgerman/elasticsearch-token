begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.common.metrics
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|common
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|LongAdder
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * An exponentially-weighted moving average.  *   *<p>  * Taken from codahale metric module, changed to use LongAdder  *  * @see<a href="http://www.teamquest.com/pdfs/whitepaper/ldavg1.pdf">UNIX Load Average Part 1: How It Works</a>  * @see<a href="http://www.teamquest.com/pdfs/whitepaper/ldavg2.pdf">UNIX Load Average Part 2: Not Your Average Average</a>  */
end_comment

begin_class
DECL|class|EWMA
specifier|public
class|class
name|EWMA
block|{
DECL|field|M1_ALPHA
specifier|private
specifier|static
specifier|final
name|double
name|M1_ALPHA
init|=
literal|1
operator|-
name|Math
operator|.
name|exp
argument_list|(
operator|-
literal|5
operator|/
literal|60.0
argument_list|)
decl_stmt|;
DECL|field|M5_ALPHA
specifier|private
specifier|static
specifier|final
name|double
name|M5_ALPHA
init|=
literal|1
operator|-
name|Math
operator|.
name|exp
argument_list|(
operator|-
literal|5
operator|/
literal|60.0
operator|/
literal|5
argument_list|)
decl_stmt|;
DECL|field|M15_ALPHA
specifier|private
specifier|static
specifier|final
name|double
name|M15_ALPHA
init|=
literal|1
operator|-
name|Math
operator|.
name|exp
argument_list|(
operator|-
literal|5
operator|/
literal|60.0
operator|/
literal|15
argument_list|)
decl_stmt|;
DECL|field|initialized
specifier|private
specifier|volatile
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
DECL|field|rate
specifier|private
specifier|volatile
name|double
name|rate
init|=
literal|0.0
decl_stmt|;
DECL|field|uncounted
specifier|private
specifier|final
name|LongAdder
name|uncounted
init|=
operator|new
name|LongAdder
argument_list|()
decl_stmt|;
DECL|field|alpha
DECL|field|interval
specifier|private
specifier|final
name|double
name|alpha
decl_stmt|,
name|interval
decl_stmt|;
comment|/**      * Creates a new EWMA which is equivalent to the UNIX one minute load average and which expects to be ticked every      * 5 seconds.      *      * @return a one-minute EWMA      */
DECL|method|oneMinuteEWMA
specifier|public
specifier|static
name|EWMA
name|oneMinuteEWMA
parameter_list|()
block|{
return|return
operator|new
name|EWMA
argument_list|(
name|M1_ALPHA
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
comment|/**      * Creates a new EWMA which is equivalent to the UNIX five minute load average and which expects to be ticked every      * 5 seconds.      *      * @return a five-minute EWMA      */
DECL|method|fiveMinuteEWMA
specifier|public
specifier|static
name|EWMA
name|fiveMinuteEWMA
parameter_list|()
block|{
return|return
operator|new
name|EWMA
argument_list|(
name|M5_ALPHA
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
comment|/**      * Creates a new EWMA which is equivalent to the UNIX fifteen minute load average and which expects to be ticked      * every 5 seconds.      *      * @return a fifteen-minute EWMA      */
DECL|method|fifteenMinuteEWMA
specifier|public
specifier|static
name|EWMA
name|fifteenMinuteEWMA
parameter_list|()
block|{
return|return
operator|new
name|EWMA
argument_list|(
name|M15_ALPHA
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
return|;
block|}
comment|/**      * Create a new EWMA with a specific smoothing constant.      *      * @param alpha        the smoothing constant      * @param interval     the expected tick interval      * @param intervalUnit the time unit of the tick interval      */
DECL|method|EWMA
specifier|public
name|EWMA
parameter_list|(
name|double
name|alpha
parameter_list|,
name|long
name|interval
parameter_list|,
name|TimeUnit
name|intervalUnit
parameter_list|)
block|{
name|this
operator|.
name|interval
operator|=
name|intervalUnit
operator|.
name|toNanos
argument_list|(
name|interval
argument_list|)
expr_stmt|;
name|this
operator|.
name|alpha
operator|=
name|alpha
expr_stmt|;
block|}
comment|/**      * Update the moving average with a new value.      *      * @param n the new value      */
DECL|method|update
specifier|public
name|void
name|update
parameter_list|(
name|long
name|n
parameter_list|)
block|{
name|uncounted
operator|.
name|add
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
comment|/**      * Mark the passage of time and decay the current rate accordingly.      */
DECL|method|tick
specifier|public
name|void
name|tick
parameter_list|()
block|{
specifier|final
name|long
name|count
init|=
name|uncounted
operator|.
name|sumThenReset
argument_list|()
decl_stmt|;
name|double
name|instantRate
init|=
name|count
operator|/
name|interval
decl_stmt|;
if|if
condition|(
name|initialized
condition|)
block|{
name|rate
operator|+=
operator|(
name|alpha
operator|*
operator|(
name|instantRate
operator|-
name|rate
operator|)
operator|)
expr_stmt|;
block|}
else|else
block|{
name|rate
operator|=
name|instantRate
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
block|}
comment|/**      * Returns the rate in the given units of time.      *      * @param rateUnit the unit of time      * @return the rate      */
DECL|method|rate
specifier|public
name|double
name|rate
parameter_list|(
name|TimeUnit
name|rateUnit
parameter_list|)
block|{
return|return
name|rate
operator|*
operator|(
name|double
operator|)
name|rateUnit
operator|.
name|toNanos
argument_list|(
literal|1
argument_list|)
return|;
block|}
block|}
end_class

end_unit

