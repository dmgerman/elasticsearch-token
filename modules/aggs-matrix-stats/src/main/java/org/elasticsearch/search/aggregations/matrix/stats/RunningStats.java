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
name|ElasticsearchException
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
name|io
operator|.
name|stream
operator|.
name|StreamInput
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
name|io
operator|.
name|stream
operator|.
name|StreamOutput
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
name|io
operator|.
name|stream
operator|.
name|Writeable
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
name|Arrays
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
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_comment
comment|/**  * Descriptive stats gathered per shard. Coordinating node computes final correlation and covariance stats  * based on these descriptive stats. This single pass, parallel approach is based on:  *  * http://prod.sandia.gov/techlib/access-control.cgi/2008/086212.pdf  *  * @internal  */
end_comment

begin_class
DECL|class|RunningStats
specifier|public
class|class
name|RunningStats
implements|implements
name|Writeable
implements|,
name|Cloneable
block|{
comment|/** count of observations (same number of observations per field) */
DECL|field|docCount
specifier|protected
name|long
name|docCount
init|=
literal|0
decl_stmt|;
comment|/** per field sum of observations */
DECL|field|fieldSum
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fieldSum
decl_stmt|;
comment|/** counts */
DECL|field|counts
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|counts
decl_stmt|;
comment|/** mean values (first moment) */
DECL|field|means
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|means
decl_stmt|;
comment|/** variance values (second moment) */
DECL|field|variances
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|variances
decl_stmt|;
comment|/** skewness values (third moment) */
DECL|field|skewness
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|skewness
decl_stmt|;
comment|/** kurtosis values (fourth moment) */
DECL|field|kurtosis
specifier|protected
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|kurtosis
decl_stmt|;
comment|/** covariance values */
DECL|field|covariances
specifier|protected
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
decl_stmt|;
DECL|method|RunningStats
specifier|public
name|RunningStats
parameter_list|()
block|{
name|init
argument_list|()
expr_stmt|;
block|}
DECL|method|RunningStats
specifier|public
name|RunningStats
parameter_list|(
specifier|final
name|String
index|[]
name|fieldNames
parameter_list|,
specifier|final
name|double
index|[]
name|fieldVals
parameter_list|)
block|{
if|if
condition|(
name|fieldVals
operator|!=
literal|null
operator|&&
name|fieldVals
operator|.
name|length
operator|>
literal|0
condition|)
block|{
name|init
argument_list|()
expr_stmt|;
name|this
operator|.
name|add
argument_list|(
name|fieldNames
argument_list|,
name|fieldVals
argument_list|)
expr_stmt|;
block|}
block|}
DECL|method|init
specifier|private
name|void
name|init
parameter_list|()
block|{
name|counts
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|fieldSum
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|means
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|skewness
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|kurtosis
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|covariances
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
name|variances
operator|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
expr_stmt|;
block|}
comment|/** Ctor to create an instance of running statistics */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
DECL|method|RunningStats
specifier|public
name|RunningStats
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
argument_list|()
expr_stmt|;
comment|// read fieldSum
name|fieldSum
operator|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
comment|// counts
name|counts
operator|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
comment|// means
name|means
operator|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
comment|// variances
name|variances
operator|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
comment|// skewness
name|skewness
operator|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
comment|// kurtosis
name|kurtosis
operator|=
operator|(
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
comment|// read covariances
name|covariances
operator|=
operator|(
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
operator|)
name|in
operator|.
name|readGenericValue
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|writeTo
specifier|public
name|void
name|writeTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
comment|// marshall fieldSum
name|out
operator|.
name|writeGenericValue
argument_list|(
name|fieldSum
argument_list|)
expr_stmt|;
comment|// counts
name|out
operator|.
name|writeGenericValue
argument_list|(
name|counts
argument_list|)
expr_stmt|;
comment|// mean
name|out
operator|.
name|writeGenericValue
argument_list|(
name|means
argument_list|)
expr_stmt|;
comment|// variances
name|out
operator|.
name|writeGenericValue
argument_list|(
name|variances
argument_list|)
expr_stmt|;
comment|// skewness
name|out
operator|.
name|writeGenericValue
argument_list|(
name|skewness
argument_list|)
expr_stmt|;
comment|// kurtosis
name|out
operator|.
name|writeGenericValue
argument_list|(
name|kurtosis
argument_list|)
expr_stmt|;
comment|// covariances
name|out
operator|.
name|writeGenericValue
argument_list|(
name|covariances
argument_list|)
expr_stmt|;
block|}
comment|/** updates running statistics with a documents field values **/
DECL|method|add
specifier|public
name|void
name|add
parameter_list|(
specifier|final
name|String
index|[]
name|fieldNames
parameter_list|,
specifier|final
name|double
index|[]
name|fieldVals
parameter_list|)
block|{
if|if
condition|(
name|fieldNames
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot add statistics without field names."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|fieldVals
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Cannot add statistics without field values."
argument_list|)
throw|;
block|}
elseif|else
if|if
condition|(
name|fieldNames
operator|.
name|length
operator|!=
name|fieldVals
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Number of field values do not match number of field names."
argument_list|)
throw|;
block|}
comment|// update total, mean, and variance
operator|++
name|docCount
expr_stmt|;
name|String
name|fieldName
decl_stmt|;
name|double
name|fieldValue
decl_stmt|;
name|double
name|m1
decl_stmt|,
name|m2
decl_stmt|,
name|m3
decl_stmt|,
name|m4
decl_stmt|;
comment|// moments
name|double
name|d
decl_stmt|,
name|dn
decl_stmt|,
name|dn2
decl_stmt|,
name|t1
decl_stmt|;
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|deltas
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldNames
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fieldName
operator|=
name|fieldNames
index|[
name|i
index|]
expr_stmt|;
name|fieldValue
operator|=
name|fieldVals
index|[
name|i
index|]
expr_stmt|;
comment|// update counts
name|counts
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
literal|1
operator|+
operator|(
name|counts
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
condition|?
name|counts
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
else|:
literal|0
operator|)
argument_list|)
expr_stmt|;
comment|// update running sum
name|fieldSum
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
operator|+
operator|(
name|fieldSum
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
condition|?
name|fieldSum
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
else|:
literal|0
operator|)
argument_list|)
expr_stmt|;
comment|// update running deltas
name|deltas
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
operator|*
name|docCount
operator|-
name|fieldSum
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
comment|// update running mean, variance, skewness, kurtosis
if|if
condition|(
name|means
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
operator|==
literal|true
condition|)
block|{
comment|// update running means
name|m1
operator|=
name|means
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|d
operator|=
name|fieldValue
operator|-
name|m1
expr_stmt|;
name|means
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|m1
operator|+
name|d
operator|/
name|docCount
argument_list|)
expr_stmt|;
comment|// update running variances
name|dn
operator|=
name|d
operator|/
name|docCount
expr_stmt|;
name|t1
operator|=
name|d
operator|*
name|dn
operator|*
operator|(
name|docCount
operator|-
literal|1
operator|)
expr_stmt|;
name|m2
operator|=
name|variances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|variances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|m2
operator|+
name|t1
argument_list|)
expr_stmt|;
name|m3
operator|=
name|skewness
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|skewness
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|m3
operator|+
operator|(
name|t1
operator|*
name|dn
operator|*
operator|(
name|docCount
operator|-
literal|2D
operator|)
operator|-
literal|3D
operator|*
name|dn
operator|*
name|m2
operator|)
argument_list|)
expr_stmt|;
name|dn2
operator|=
name|dn
operator|*
name|dn
expr_stmt|;
name|m4
operator|=
name|t1
operator|*
name|dn2
operator|*
operator|(
name|docCount
operator|*
name|docCount
operator|-
literal|3D
operator|*
name|docCount
operator|+
literal|3D
operator|)
operator|+
literal|6D
operator|*
name|dn2
operator|*
name|m2
operator|-
literal|4D
operator|*
name|dn
operator|*
name|m3
expr_stmt|;
name|kurtosis
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|kurtosis
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|+
name|m4
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|means
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fieldValue
argument_list|)
expr_stmt|;
name|variances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
name|skewness
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
name|kurtosis
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|updateCovariance
argument_list|(
name|fieldNames
argument_list|,
name|deltas
argument_list|)
expr_stmt|;
block|}
comment|/** Update covariance matrix */
DECL|method|updateCovariance
specifier|private
name|void
name|updateCovariance
parameter_list|(
specifier|final
name|String
index|[]
name|fieldNames
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|deltas
parameter_list|)
block|{
comment|// deep copy of hash keys (field names)
name|ArrayList
argument_list|<
name|String
argument_list|>
name|cFieldNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|fieldNames
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|fieldName
decl_stmt|;
name|double
name|dR
decl_stmt|,
name|newVal
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|fieldNames
operator|.
name|length
condition|;
operator|++
name|i
control|)
block|{
name|fieldName
operator|=
name|fieldNames
index|[
name|i
index|]
expr_stmt|;
name|cFieldNames
operator|.
name|remove
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
comment|// update running covariances
name|dR
operator|=
name|deltas
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|cFieldVals
init|=
operator|(
name|covariances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|!=
literal|null
operator|)
condition|?
name|covariances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
else|:
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|String
name|cFieldName
range|:
name|cFieldNames
control|)
block|{
if|if
condition|(
name|cFieldVals
operator|.
name|containsKey
argument_list|(
name|cFieldName
argument_list|)
operator|==
literal|true
condition|)
block|{
name|newVal
operator|=
name|cFieldVals
operator|.
name|get
argument_list|(
name|cFieldName
argument_list|)
operator|+
literal|1.0
operator|/
operator|(
name|docCount
operator|*
operator|(
name|docCount
operator|-
literal|1.0
operator|)
operator|)
operator|*
name|dR
operator|*
name|deltas
operator|.
name|get
argument_list|(
name|cFieldName
argument_list|)
expr_stmt|;
name|cFieldVals
operator|.
name|put
argument_list|(
name|cFieldName
argument_list|,
name|newVal
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cFieldVals
operator|.
name|put
argument_list|(
name|cFieldName
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
name|cFieldVals
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|covariances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|cFieldVals
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**      * Merges the descriptive statistics of a second data set (e.g., per shard)      *      * running computations taken from: http://prod.sandia.gov/techlib/access-control.cgi/2008/086212.pdf      **/
DECL|method|merge
specifier|public
name|void
name|merge
parameter_list|(
specifier|final
name|RunningStats
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
literal|null
condition|)
block|{
return|return;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|docCount
operator|==
literal|0
condition|)
block|{
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fs
range|:
name|other
operator|.
name|means
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|fs
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|this
operator|.
name|means
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fs
operator|.
name|getValue
argument_list|()
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|counts
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|counts
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|.
name|longValue
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|fieldSum
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|fieldSum
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|variances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|variances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|skewness
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|skewness
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|kurtosis
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|kurtosis
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|.
name|doubleValue
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|other
operator|.
name|covariances
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
operator|==
literal|true
condition|)
block|{
name|this
operator|.
name|covariances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|covariances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|docCount
operator|=
name|other
operator|.
name|docCount
expr_stmt|;
block|}
return|return;
block|}
specifier|final
name|double
name|nA
init|=
name|docCount
decl_stmt|;
specifier|final
name|double
name|nB
init|=
name|other
operator|.
name|docCount
decl_stmt|;
comment|// merge count
name|docCount
operator|+=
name|other
operator|.
name|docCount
expr_stmt|;
specifier|final
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|deltas
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|double
name|meanA
decl_stmt|,
name|varA
decl_stmt|,
name|skewA
decl_stmt|,
name|kurtA
decl_stmt|,
name|meanB
decl_stmt|,
name|varB
decl_stmt|,
name|skewB
decl_stmt|,
name|kurtB
decl_stmt|;
name|double
name|d
decl_stmt|,
name|d2
decl_stmt|,
name|d3
decl_stmt|,
name|d4
decl_stmt|,
name|n2
decl_stmt|,
name|nA2
decl_stmt|,
name|nB2
decl_stmt|;
name|double
name|newSkew
decl_stmt|,
name|nk
decl_stmt|;
comment|// across fields
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fs
range|:
name|other
operator|.
name|means
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|fs
operator|.
name|getKey
argument_list|()
decl_stmt|;
name|meanA
operator|=
name|means
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|varA
operator|=
name|variances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|skewA
operator|=
name|skewness
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|kurtA
operator|=
name|kurtosis
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|meanB
operator|=
name|other
operator|.
name|means
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|varB
operator|=
name|other
operator|.
name|variances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|skewB
operator|=
name|other
operator|.
name|skewness
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
name|kurtB
operator|=
name|other
operator|.
name|kurtosis
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
comment|// merge counts of two sets
name|counts
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|counts
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|+
name|other
operator|.
name|counts
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
comment|// merge means of two sets
name|means
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
operator|(
name|nA
operator|*
name|means
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|+
name|nB
operator|*
name|other
operator|.
name|means
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|)
operator|/
operator|(
name|nA
operator|+
name|nB
operator|)
argument_list|)
expr_stmt|;
comment|// merge deltas
name|deltas
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|other
operator|.
name|fieldSum
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|/
name|nB
operator|-
name|fieldSum
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|/
name|nA
argument_list|)
expr_stmt|;
comment|// merge totals
name|fieldSum
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|fieldSum
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|+
name|other
operator|.
name|fieldSum
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
comment|// merge variances, skewness, and kurtosis of two sets
name|d
operator|=
name|meanB
operator|-
name|meanA
expr_stmt|;
comment|// delta mean
name|d2
operator|=
name|d
operator|*
name|d
expr_stmt|;
comment|// delta mean squared
name|d3
operator|=
name|d
operator|*
name|d2
expr_stmt|;
comment|// delta mean cubed
name|d4
operator|=
name|d2
operator|*
name|d2
expr_stmt|;
comment|// delta mean 4th power
name|n2
operator|=
name|docCount
operator|*
name|docCount
expr_stmt|;
comment|// num samples squared
name|nA2
operator|=
name|nA
operator|*
name|nA
expr_stmt|;
comment|// doc A num samples squared
name|nB2
operator|=
name|nB
operator|*
name|nB
expr_stmt|;
comment|// doc B num samples squared
comment|// variance
name|variances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|varA
operator|+
name|varB
operator|+
name|d2
operator|*
name|nA
operator|*
name|other
operator|.
name|docCount
operator|/
name|docCount
argument_list|)
expr_stmt|;
comment|// skeewness
name|newSkew
operator|=
name|skewA
operator|+
name|skewB
operator|+
name|d3
operator|*
name|nA
operator|*
name|nB
operator|*
operator|(
name|nA
operator|-
name|nB
operator|)
operator|/
name|n2
expr_stmt|;
name|skewness
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|newSkew
operator|+
literal|3D
operator|*
name|d
operator|*
operator|(
name|nA
operator|*
name|varB
operator|-
name|nB
operator|*
name|varA
operator|)
operator|/
name|docCount
argument_list|)
expr_stmt|;
comment|// kurtosis
name|nk
operator|=
name|kurtA
operator|+
name|kurtB
operator|+
name|d4
operator|*
name|nA
operator|*
name|nB
operator|*
operator|(
name|nA2
operator|-
name|nA
operator|*
name|nB
operator|+
name|nB2
operator|)
operator|/
operator|(
name|n2
operator|*
name|docCount
operator|)
expr_stmt|;
name|kurtosis
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|nk
operator|+
literal|6D
operator|*
name|d2
operator|*
operator|(
name|nA2
operator|*
name|varB
operator|+
name|nB2
operator|*
name|varA
operator|)
operator|/
name|n2
operator|+
literal|4D
operator|*
name|d
operator|*
operator|(
name|nA
operator|*
name|skewB
operator|-
name|nB
operator|*
name|skewA
operator|)
operator|/
name|docCount
argument_list|)
expr_stmt|;
block|}
name|this
operator|.
name|mergeCovariance
argument_list|(
name|other
argument_list|,
name|deltas
argument_list|)
expr_stmt|;
block|}
comment|/** Merges two covariance matrices */
DECL|method|mergeCovariance
specifier|private
name|void
name|mergeCovariance
parameter_list|(
specifier|final
name|RunningStats
name|other
parameter_list|,
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|deltas
parameter_list|)
block|{
specifier|final
name|double
name|countA
init|=
name|docCount
operator|-
name|other
operator|.
name|docCount
decl_stmt|;
name|double
name|f
decl_stmt|,
name|dR
decl_stmt|,
name|newVal
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|fs
range|:
name|other
operator|.
name|means
operator|.
name|entrySet
argument_list|()
control|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|fs
operator|.
name|getKey
argument_list|()
decl_stmt|;
comment|// merge covariances of two sets
name|f
operator|=
name|countA
operator|*
name|other
operator|.
name|docCount
operator|/
name|this
operator|.
name|docCount
expr_stmt|;
name|dR
operator|=
name|deltas
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
expr_stmt|;
comment|// merge covariances
if|if
condition|(
name|covariances
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
condition|)
block|{
name|HashMap
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|cFieldVals
init|=
name|covariances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|cFieldName
range|:
name|cFieldVals
operator|.
name|keySet
argument_list|()
control|)
block|{
name|newVal
operator|=
name|cFieldVals
operator|.
name|get
argument_list|(
name|cFieldName
argument_list|)
expr_stmt|;
if|if
condition|(
name|other
operator|.
name|covariances
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
operator|&&
name|other
operator|.
name|covariances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|.
name|containsKey
argument_list|(
name|cFieldName
argument_list|)
condition|)
block|{
name|newVal
operator|+=
name|other
operator|.
name|covariances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|.
name|get
argument_list|(
name|cFieldName
argument_list|)
operator|+
name|f
operator|*
name|dR
operator|*
name|deltas
operator|.
name|get
argument_list|(
name|cFieldName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|newVal
operator|+=
name|other
operator|.
name|covariances
operator|.
name|get
argument_list|(
name|cFieldName
argument_list|)
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
operator|+
name|f
operator|*
name|dR
operator|*
name|deltas
operator|.
name|get
argument_list|(
name|cFieldName
argument_list|)
expr_stmt|;
block|}
name|cFieldVals
operator|.
name|put
argument_list|(
name|cFieldName
argument_list|,
name|newVal
argument_list|)
expr_stmt|;
block|}
name|covariances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|cFieldVals
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|RunningStats
name|clone
parameter_list|()
block|{
try|try
block|{
return|return
operator|(
name|RunningStats
operator|)
name|super
operator|.
name|clone
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|CloneNotSupportedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|ElasticsearchException
argument_list|(
literal|"Error trying to create a copy of RunningStats"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

