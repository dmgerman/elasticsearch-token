begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.action.bench
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|bench
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|metrics
operator|.
name|percentiles
operator|.
name|tdigest
operator|.
name|TDigestState
import|;
end_import

begin_comment
comment|/**  * Utility class for accurately measuring statistical variance in cases where  * it is not possible or sensible to retain an entire data set in memory.  *  * Mean and variance algorithms taken from Donald Knuth's Art of Computer Programming, Vol 2, page 232, 3rd edition.  * Based on reference implementation http://www.johndcook.com/standard_deviation.html.  *  * For each input, the running mean and running sum-of-squares variance is calculated according  * to:  *      Running mean:     Mk = Mk-1+ (xk - Mk-1)/k  *      Running variance: Sk = Sk-1 + (xk - Mk-1)*(xk - Mk)  *  * Percentile computations use T-Digest: https://github.com/tdunning/t-digest  */
end_comment

begin_class
DECL|class|SinglePassStatistics
specifier|public
class|class
name|SinglePassStatistics
block|{
DECL|field|count
specifier|private
name|long
name|count
init|=
literal|0
decl_stmt|;
DECL|field|runningMean
specifier|private
name|double
name|runningMean
init|=
literal|0.0
decl_stmt|;
DECL|field|runningVariance
specifier|private
name|double
name|runningVariance
init|=
literal|0.0
decl_stmt|;
DECL|field|runningSum
specifier|private
name|long
name|runningSum
init|=
literal|0
decl_stmt|;
DECL|field|tdigest
name|TDigestState
name|tdigest
init|=
operator|new
name|TDigestState
argument_list|(
literal|100.0
argument_list|)
decl_stmt|;
comment|/**      * Adds a new value onto the running calculation      *      * @param value     New value to add to calculation      */
DECL|method|push
specifier|public
name|void
name|push
parameter_list|(
name|long
name|value
parameter_list|)
block|{
name|count
operator|++
expr_stmt|;
if|if
condition|(
name|count
operator|==
literal|1
condition|)
block|{
name|runningMean
operator|=
name|value
expr_stmt|;
name|runningVariance
operator|=
literal|0.0
expr_stmt|;
block|}
else|else
block|{
name|double
name|newMean
init|=
name|runningMean
operator|+
operator|(
operator|(
name|value
operator|-
name|runningMean
operator|)
operator|/
name|count
operator|)
decl_stmt|;
comment|// Mk = Mk-1 + ((xk - Mk-1) / k)
name|double
name|newVariance
init|=
name|runningVariance
operator|+
operator|(
operator|(
name|value
operator|-
name|runningMean
operator|)
operator|*
operator|(
name|value
operator|-
name|newMean
operator|)
operator|)
decl_stmt|;
comment|// Sk = Sk-1 + (xk - Mk-1)*(xk - Mk)
name|runningMean
operator|=
name|newMean
expr_stmt|;
name|runningVariance
operator|=
name|newVariance
expr_stmt|;
block|}
name|runningSum
operator|+=
name|value
expr_stmt|;
name|tdigest
operator|.
name|add
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**      * Current value for the running mean      *      * @return      Current value of running mean      */
DECL|method|mean
specifier|public
name|double
name|mean
parameter_list|()
block|{
return|return
name|runningMean
return|;
block|}
comment|/**      * Current value for the running variance from the mean      *      * @return      Current value for the running variance      */
DECL|method|variance
specifier|public
name|double
name|variance
parameter_list|()
block|{
if|if
condition|(
name|count
operator|>
literal|1
condition|)
block|{
return|return
name|runningVariance
operator|/
operator|(
name|count
operator|-
literal|1
operator|)
return|;
block|}
else|else
block|{
return|return
literal|0.0
return|;
block|}
block|}
comment|/**      * Current running value of standard deviation      *      * @return      Current running value of standard deviation      */
DECL|method|stddev
specifier|public
name|double
name|stddev
parameter_list|()
block|{
return|return
name|Math
operator|.
name|sqrt
argument_list|(
name|variance
argument_list|()
argument_list|)
return|;
block|}
comment|/**      * Minimum value in data set      *      * @return      Minimum value in data set      */
DECL|method|min
specifier|public
name|long
name|min
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|tdigest
operator|.
name|quantile
argument_list|(
literal|0.0
argument_list|)
return|;
block|}
comment|/**      * Maximum value in data set      * @return      Maximum value in data set      */
DECL|method|max
specifier|public
name|long
name|max
parameter_list|()
block|{
return|return
operator|(
name|long
operator|)
name|tdigest
operator|.
name|quantile
argument_list|(
literal|1.0
argument_list|)
return|;
block|}
comment|/**      * Total number of values seen      *      * @return      Total number of values seen      */
DECL|method|count
specifier|public
name|long
name|count
parameter_list|()
block|{
return|return
name|count
return|;
block|}
comment|/**      * Running sum of all values      *      * @return      Running sum of all values      */
DECL|method|sum
specifier|public
name|long
name|sum
parameter_list|()
block|{
return|return
name|runningSum
return|;
block|}
comment|/**      * Running percentile      * @param q     Percentile to calculate      * @return      Running percentile      */
DECL|method|percentile
specifier|public
name|double
name|percentile
parameter_list|(
name|double
name|q
parameter_list|)
block|{
return|return
name|tdigest
operator|.
name|quantile
argument_list|(
name|q
argument_list|)
return|;
block|}
block|}
end_class

end_unit

