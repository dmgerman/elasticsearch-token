begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.matrix.stats
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|matrix
operator|.
name|stats
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|test
operator|.
name|ESTestCase
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|Matchers
operator|.
name|equalTo
import|;
end_import

begin_comment
comment|/**  *  */
end_comment

begin_class
DECL|class|BaseMatrixStatsTestCase
specifier|public
specifier|abstract
class|class
name|BaseMatrixStatsTestCase
extends|extends
name|ESTestCase
block|{
DECL|field|numObs
specifier|protected
specifier|final
name|int
name|numObs
init|=
name|atLeast
argument_list|(
literal|10000
argument_list|)
decl_stmt|;
DECL|field|fieldA
specifier|protected
specifier|final
name|ArrayList
argument_list|<
name|Double
argument_list|>
name|fieldA
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numObs
argument_list|)
decl_stmt|;
DECL|field|fieldB
specifier|protected
specifier|final
name|ArrayList
argument_list|<
name|Double
argument_list|>
name|fieldB
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|numObs
argument_list|)
decl_stmt|;
DECL|field|actualStats
specifier|protected
specifier|final
name|MultiPassStats
name|actualStats
init|=
operator|new
name|MultiPassStats
argument_list|()
decl_stmt|;
DECL|field|fieldAKey
specifier|protected
specifier|final
specifier|static
name|String
name|fieldAKey
init|=
literal|"fieldA"
decl_stmt|;
DECL|field|fieldBKey
specifier|protected
specifier|final
specifier|static
name|String
name|fieldBKey
init|=
literal|"fieldB"
decl_stmt|;
annotation|@
name|Before
DECL|method|setup
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|createStats
argument_list|()
expr_stmt|;
block|}
DECL|method|createStats
specifier|public
name|void
name|createStats
parameter_list|()
block|{
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|numObs
condition|;
operator|++
name|n
control|)
block|{
name|fieldA
operator|.
name|add
argument_list|(
name|randomDouble
argument_list|()
argument_list|)
expr_stmt|;
name|fieldB
operator|.
name|add
argument_list|(
name|randomDouble
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|actualStats
operator|.
name|computeStats
argument_list|(
name|fieldA
argument_list|,
name|fieldB
argument_list|)
expr_stmt|;
block|}
DECL|class|MultiPassStats
specifier|static
class|class
name|MultiPassStats
block|{
DECL|field|count
name|long
name|count
decl_stmt|;
DECL|field|means
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|means
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|variances
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|variances
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|skewness
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|skewness
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|kurtosis
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|kurtosis
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|covariances
name|HashMap
argument_list|<
name|String
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
argument_list|>
name|covariances
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|correlations
name|HashMap
argument_list|<
name|String
argument_list|,
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
argument_list|>
name|correlations
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|computeStats
name|void
name|computeStats
parameter_list|(
specifier|final
name|ArrayList
argument_list|<
name|Double
argument_list|>
name|fieldA
parameter_list|,
specifier|final
name|ArrayList
argument_list|<
name|Double
argument_list|>
name|fieldB
parameter_list|)
block|{
comment|// set count
name|count
operator|=
name|fieldA
operator|.
name|size
argument_list|()
expr_stmt|;
name|double
name|meanA
init|=
literal|0d
decl_stmt|;
name|double
name|meanB
init|=
literal|0d
decl_stmt|;
comment|// compute mean
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|count
condition|;
operator|++
name|n
control|)
block|{
comment|// fieldA
name|meanA
operator|+=
name|fieldA
operator|.
name|get
argument_list|(
name|n
argument_list|)
expr_stmt|;
name|meanB
operator|+=
name|fieldB
operator|.
name|get
argument_list|(
name|n
argument_list|)
expr_stmt|;
block|}
name|means
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|meanA
operator|/
name|count
argument_list|)
expr_stmt|;
name|means
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|meanB
operator|/
name|count
argument_list|)
expr_stmt|;
comment|// compute variance, skewness, and kurtosis
name|double
name|dA
decl_stmt|;
name|double
name|dB
decl_stmt|;
name|double
name|skewA
init|=
literal|0d
decl_stmt|;
name|double
name|skewB
init|=
literal|0d
decl_stmt|;
name|double
name|kurtA
init|=
literal|0d
decl_stmt|;
name|double
name|kurtB
init|=
literal|0d
decl_stmt|;
name|double
name|varA
init|=
literal|0d
decl_stmt|;
name|double
name|varB
init|=
literal|0d
decl_stmt|;
name|double
name|cVar
init|=
literal|0d
decl_stmt|;
for|for
control|(
name|int
name|n
init|=
literal|0
init|;
name|n
operator|<
name|count
condition|;
operator|++
name|n
control|)
block|{
name|dA
operator|=
name|fieldA
operator|.
name|get
argument_list|(
name|n
argument_list|)
operator|-
name|means
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
expr_stmt|;
name|varA
operator|+=
name|dA
operator|*
name|dA
expr_stmt|;
name|skewA
operator|+=
name|dA
operator|*
name|dA
operator|*
name|dA
expr_stmt|;
name|kurtA
operator|+=
name|dA
operator|*
name|dA
operator|*
name|dA
operator|*
name|dA
expr_stmt|;
name|dB
operator|=
name|fieldB
operator|.
name|get
argument_list|(
name|n
argument_list|)
operator|-
name|means
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
expr_stmt|;
name|varB
operator|+=
name|dB
operator|*
name|dB
expr_stmt|;
name|skewB
operator|+=
name|dB
operator|*
name|dB
operator|*
name|dB
expr_stmt|;
name|kurtB
operator|+=
name|dB
operator|*
name|dB
operator|*
name|dB
operator|*
name|dB
expr_stmt|;
name|cVar
operator|+=
name|dA
operator|*
name|dB
expr_stmt|;
block|}
name|variances
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|varA
operator|/
operator|(
name|count
operator|-
literal|1
operator|)
argument_list|)
expr_stmt|;
specifier|final
name|double
name|stdA
init|=
name|Math
operator|.
name|sqrt
argument_list|(
name|variances
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
argument_list|)
decl_stmt|;
name|variances
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|varB
operator|/
operator|(
name|count
operator|-
literal|1
operator|)
argument_list|)
expr_stmt|;
specifier|final
name|double
name|stdB
init|=
name|Math
operator|.
name|sqrt
argument_list|(
name|variances
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
argument_list|)
decl_stmt|;
name|skewness
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|skewA
operator|/
operator|(
operator|(
name|count
operator|-
literal|1
operator|)
operator|*
name|variances
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
operator|*
name|stdA
operator|)
argument_list|)
expr_stmt|;
name|skewness
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|skewB
operator|/
operator|(
operator|(
name|count
operator|-
literal|1
operator|)
operator|*
name|variances
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
operator|*
name|stdB
operator|)
argument_list|)
expr_stmt|;
name|kurtosis
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|kurtA
operator|/
operator|(
operator|(
name|count
operator|-
literal|1
operator|)
operator|*
name|variances
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
operator|*
name|variances
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
operator|)
argument_list|)
expr_stmt|;
name|kurtosis
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|kurtB
operator|/
operator|(
operator|(
name|count
operator|-
literal|1
operator|)
operator|*
name|variances
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
operator|*
name|variances
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
operator|)
argument_list|)
expr_stmt|;
comment|// compute covariance
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fieldACovar
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|fieldACovar
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
literal|1d
argument_list|)
expr_stmt|;
name|cVar
operator|/=
name|count
operator|-
literal|1
expr_stmt|;
name|fieldACovar
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|cVar
argument_list|)
expr_stmt|;
name|covariances
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|fieldACovar
argument_list|)
expr_stmt|;
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fieldBCovar
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|fieldBCovar
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|cVar
argument_list|)
expr_stmt|;
name|fieldBCovar
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
literal|1d
argument_list|)
expr_stmt|;
name|covariances
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|fieldBCovar
argument_list|)
expr_stmt|;
comment|// compute correlation
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fieldACorr
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|fieldACorr
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
literal|1d
argument_list|)
expr_stmt|;
name|double
name|corr
init|=
name|covariances
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
decl_stmt|;
name|corr
operator|/=
name|stdA
operator|*
name|stdB
expr_stmt|;
name|fieldACorr
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|corr
argument_list|)
expr_stmt|;
name|correlations
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|fieldACorr
argument_list|)
expr_stmt|;
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fieldBCorr
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|fieldBCorr
operator|.
name|put
argument_list|(
name|fieldAKey
argument_list|,
name|corr
argument_list|)
expr_stmt|;
name|fieldBCorr
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
literal|1d
argument_list|)
expr_stmt|;
name|correlations
operator|.
name|put
argument_list|(
name|fieldBKey
argument_list|,
name|fieldBCorr
argument_list|)
expr_stmt|;
block|}
DECL|method|assertNearlyEqual
specifier|public
name|void
name|assertNearlyEqual
parameter_list|(
name|MatrixStatsResults
name|stats
parameter_list|)
block|{
name|assertThat
argument_list|(
name|count
argument_list|,
name|equalTo
argument_list|(
name|stats
operator|.
name|getDocCount
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|,
name|equalTo
argument_list|(
name|stats
operator|.
name|getFieldCount
argument_list|(
name|fieldAKey
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
name|count
argument_list|,
name|equalTo
argument_list|(
name|stats
operator|.
name|getFieldCount
argument_list|(
name|fieldBKey
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// means
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|means
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getMean
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|means
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getMean
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
comment|// variances
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|variances
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getVariance
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|variances
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getVariance
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
comment|// skewness (multi-pass is more susceptible to round-off error so we need to slightly relax the tolerance)
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|skewness
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getSkewness
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
literal|1e-4
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|skewness
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getSkewness
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
literal|1e-4
argument_list|)
argument_list|)
expr_stmt|;
comment|// kurtosis (multi-pass is more susceptible to round-off error so we need to slightly relax the tolerance)
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|kurtosis
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getKurtosis
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
literal|1e-4
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|kurtosis
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getKurtosis
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
literal|1e-4
argument_list|)
argument_list|)
expr_stmt|;
comment|// covariances
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|covariances
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getCovariance
argument_list|(
name|fieldAKey
argument_list|,
name|fieldBKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|covariances
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getCovariance
argument_list|(
name|fieldBKey
argument_list|,
name|fieldAKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
comment|// correlation
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|correlations
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getCorrelation
argument_list|(
name|fieldAKey
argument_list|,
name|fieldBKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nearlyEqual
argument_list|(
name|correlations
operator|.
name|get
argument_list|(
name|fieldBKey
argument_list|)
operator|.
name|get
argument_list|(
name|fieldAKey
argument_list|)
argument_list|,
name|stats
operator|.
name|getCorrelation
argument_list|(
name|fieldBKey
argument_list|,
name|fieldAKey
argument_list|)
argument_list|,
literal|1e-7
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|nearlyEqual
specifier|private
specifier|static
name|boolean
name|nearlyEqual
parameter_list|(
name|double
name|a
parameter_list|,
name|double
name|b
parameter_list|,
name|double
name|epsilon
parameter_list|)
block|{
specifier|final
name|double
name|absA
init|=
name|Math
operator|.
name|abs
argument_list|(
name|a
argument_list|)
decl_stmt|;
specifier|final
name|double
name|absB
init|=
name|Math
operator|.
name|abs
argument_list|(
name|b
argument_list|)
decl_stmt|;
specifier|final
name|double
name|diff
init|=
name|Math
operator|.
name|abs
argument_list|(
name|a
operator|-
name|b
argument_list|)
decl_stmt|;
if|if
condition|(
name|a
operator|==
name|b
condition|)
block|{
comment|// shortcut, handles infinities
return|return
literal|true
return|;
block|}
elseif|else
if|if
condition|(
name|a
operator|==
literal|0
operator|||
name|b
operator|==
literal|0
operator|||
name|diff
operator|<
name|Double
operator|.
name|MIN_NORMAL
condition|)
block|{
comment|// a or b is zero or both are extremely close to it
comment|// relative error is less meaningful here
return|return
name|diff
operator|<
operator|(
name|epsilon
operator|*
name|Double
operator|.
name|MIN_NORMAL
operator|)
return|;
block|}
else|else
block|{
comment|// use relative error
return|return
name|diff
operator|/
name|Math
operator|.
name|min
argument_list|(
operator|(
name|absA
operator|+
name|absB
operator|)
argument_list|,
name|Double
operator|.
name|MAX_VALUE
argument_list|)
operator|<
name|epsilon
return|;
block|}
block|}
block|}
end_class

end_unit

