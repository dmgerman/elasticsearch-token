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
name|common
operator|.
name|ParseField
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
name|xcontent
operator|.
name|ObjectParser
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
name|xcontent
operator|.
name|XContentBuilder
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
name|xcontent
operator|.
name|XContentParser
import|;
end_import

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
name|ParsedAggregation
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|LinkedHashMap
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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
import|;
end_import

begin_class
DECL|class|ParsedMatrixStats
specifier|public
class|class
name|ParsedMatrixStats
extends|extends
name|ParsedAggregation
implements|implements
name|MatrixStats
block|{
DECL|field|counts
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|counts
init|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|()
decl_stmt|;
DECL|field|means
specifier|private
specifier|final
name|Map
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
specifier|private
specifier|final
name|Map
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
specifier|private
specifier|final
name|Map
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
specifier|private
specifier|final
name|Map
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
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Map
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
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Map
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
DECL|field|docCount
specifier|private
name|long
name|docCount
decl_stmt|;
annotation|@
name|Override
DECL|method|getType
specifier|public
name|String
name|getType
parameter_list|()
block|{
return|return
name|MatrixStatsAggregationBuilder
operator|.
name|NAME
return|;
block|}
DECL|method|setDocCount
specifier|private
name|void
name|setDocCount
parameter_list|(
name|long
name|docCount
parameter_list|)
block|{
name|this
operator|.
name|docCount
operator|=
name|docCount
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|getDocCount
specifier|public
name|long
name|getDocCount
parameter_list|()
block|{
return|return
name|docCount
return|;
block|}
annotation|@
name|Override
DECL|method|getFieldCount
specifier|public
name|long
name|getFieldCount
parameter_list|(
name|String
name|field
parameter_list|)
block|{
if|if
condition|(
name|counts
operator|.
name|containsKey
argument_list|(
name|field
argument_list|)
operator|==
literal|false
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|counts
operator|.
name|get
argument_list|(
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getMean
specifier|public
name|double
name|getMean
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|checkedGet
argument_list|(
name|means
argument_list|,
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getVariance
specifier|public
name|double
name|getVariance
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|checkedGet
argument_list|(
name|variances
argument_list|,
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getSkewness
specifier|public
name|double
name|getSkewness
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|checkedGet
argument_list|(
name|skewness
argument_list|,
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getKurtosis
specifier|public
name|double
name|getKurtosis
parameter_list|(
name|String
name|field
parameter_list|)
block|{
return|return
name|checkedGet
argument_list|(
name|kurtosis
argument_list|,
name|field
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getCovariance
specifier|public
name|double
name|getCovariance
parameter_list|(
name|String
name|fieldX
parameter_list|,
name|String
name|fieldY
parameter_list|)
block|{
if|if
condition|(
name|fieldX
operator|.
name|equals
argument_list|(
name|fieldY
argument_list|)
condition|)
block|{
return|return
name|checkedGet
argument_list|(
name|variances
argument_list|,
name|fieldX
argument_list|)
return|;
block|}
return|return
name|MatrixStatsResults
operator|.
name|getValFromUpperTriangularMatrix
argument_list|(
name|covariances
argument_list|,
name|fieldX
argument_list|,
name|fieldY
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getCorrelation
specifier|public
name|double
name|getCorrelation
parameter_list|(
name|String
name|fieldX
parameter_list|,
name|String
name|fieldY
parameter_list|)
block|{
if|if
condition|(
name|fieldX
operator|.
name|equals
argument_list|(
name|fieldY
argument_list|)
condition|)
block|{
return|return
literal|1.0
return|;
block|}
return|return
name|MatrixStatsResults
operator|.
name|getValFromUpperTriangularMatrix
argument_list|(
name|correlations
argument_list|,
name|fieldX
argument_list|,
name|fieldY
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doXContentBody
specifier|protected
name|XContentBuilder
name|doXContentBody
parameter_list|(
name|XContentBuilder
name|builder
parameter_list|,
name|Params
name|params
parameter_list|)
throws|throws
name|IOException
block|{
name|builder
operator|.
name|field
argument_list|(
name|CommonFields
operator|.
name|DOC_COUNT
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|getDocCount
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|counts
operator|!=
literal|null
operator|&&
name|counts
operator|.
name|isEmpty
argument_list|()
operator|==
literal|false
condition|)
block|{
name|builder
operator|.
name|startArray
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|FIELDS
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|fieldName
range|:
name|counts
operator|.
name|keySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|startObject
argument_list|()
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|NAME
argument_list|,
name|fieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|COUNT
argument_list|,
name|getFieldCount
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|MEAN
argument_list|,
name|getMean
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|VARIANCE
argument_list|,
name|getVariance
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|SKEWNESS
argument_list|,
name|getSkewness
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|KURTOSIS
argument_list|,
name|getKurtosis
argument_list|(
name|fieldName
argument_list|)
argument_list|)
expr_stmt|;
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|COVARIANCE
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|covars
init|=
name|covariances
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|covars
operator|!=
literal|null
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
name|covar
range|:
name|covars
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|covar
operator|.
name|getKey
argument_list|()
argument_list|,
name|covar
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
block|{
name|builder
operator|.
name|startObject
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|CORRELATION
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|correls
init|=
name|correlations
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
decl_stmt|;
if|if
condition|(
name|correls
operator|!=
literal|null
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
name|correl
range|:
name|correls
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|correl
operator|.
name|getKey
argument_list|()
argument_list|,
name|correl
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
block|}
name|builder
operator|.
name|endArray
argument_list|()
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|checkedGet
specifier|private
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|checkedGet
parameter_list|(
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|T
argument_list|>
name|values
parameter_list|,
specifier|final
name|String
name|fieldName
parameter_list|)
block|{
if|if
condition|(
name|fieldName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field name cannot be null"
argument_list|)
throw|;
block|}
if|if
condition|(
name|values
operator|.
name|containsKey
argument_list|(
name|fieldName
argument_list|)
operator|==
literal|false
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"field "
operator|+
name|fieldName
operator|+
literal|" does not exist"
argument_list|)
throw|;
block|}
return|return
name|values
operator|.
name|get
argument_list|(
name|fieldName
argument_list|)
return|;
block|}
DECL|field|PARSER
specifier|private
specifier|static
name|ObjectParser
argument_list|<
name|ParsedMatrixStats
argument_list|,
name|Void
argument_list|>
name|PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedMatrixStats
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedMatrixStats
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|declareAggregationFields
argument_list|(
name|PARSER
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareLong
argument_list|(
name|ParsedMatrixStats
operator|::
name|setDocCount
argument_list|,
name|CommonFields
operator|.
name|DOC_COUNT
argument_list|)
expr_stmt|;
name|PARSER
operator|.
name|declareObjectArray
argument_list|(
parameter_list|(
name|matrixStats
parameter_list|,
name|results
parameter_list|)
lambda|->
block|{
for|for
control|(
name|ParsedMatrixStatsResult
name|result
range|:
name|results
control|)
block|{
specifier|final
name|String
name|fieldName
init|=
name|result
operator|.
name|name
decl_stmt|;
name|matrixStats
operator|.
name|counts
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|result
operator|.
name|count
argument_list|)
expr_stmt|;
name|matrixStats
operator|.
name|means
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|result
operator|.
name|mean
argument_list|)
expr_stmt|;
name|matrixStats
operator|.
name|variances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|result
operator|.
name|variance
argument_list|)
expr_stmt|;
name|matrixStats
operator|.
name|skewness
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|result
operator|.
name|skewness
argument_list|)
expr_stmt|;
name|matrixStats
operator|.
name|kurtosis
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|result
operator|.
name|kurtosis
argument_list|)
expr_stmt|;
name|matrixStats
operator|.
name|covariances
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|result
operator|.
name|covariances
argument_list|)
expr_stmt|;
name|matrixStats
operator|.
name|correlations
operator|.
name|put
argument_list|(
name|fieldName
argument_list|,
name|result
operator|.
name|correlations
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|ParsedMatrixStatsResult
operator|.
name|fromXContent
argument_list|(
name|p
argument_list|)
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|FIELDS
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|fromXContent
specifier|public
specifier|static
name|ParsedMatrixStats
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|,
name|String
name|name
parameter_list|)
throws|throws
name|IOException
block|{
name|ParsedMatrixStats
name|aggregation
init|=
name|PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|aggregation
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
return|return
name|aggregation
return|;
block|}
DECL|class|ParsedMatrixStatsResult
specifier|static
class|class
name|ParsedMatrixStatsResult
block|{
DECL|field|name
name|String
name|name
decl_stmt|;
DECL|field|count
name|Long
name|count
decl_stmt|;
DECL|field|mean
name|Double
name|mean
decl_stmt|;
DECL|field|variance
name|Double
name|variance
decl_stmt|;
DECL|field|skewness
name|Double
name|skewness
decl_stmt|;
DECL|field|kurtosis
name|Double
name|kurtosis
decl_stmt|;
DECL|field|covariances
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|covariances
decl_stmt|;
DECL|field|correlations
name|Map
argument_list|<
name|String
argument_list|,
name|Double
argument_list|>
name|correlations
decl_stmt|;
DECL|field|RESULT_PARSER
specifier|private
specifier|static
name|ObjectParser
argument_list|<
name|ParsedMatrixStatsResult
argument_list|,
name|Void
argument_list|>
name|RESULT_PARSER
init|=
operator|new
name|ObjectParser
argument_list|<>
argument_list|(
name|ParsedMatrixStatsResult
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|,
literal|true
argument_list|,
name|ParsedMatrixStatsResult
operator|::
operator|new
argument_list|)
decl_stmt|;
static|static
block|{
name|RESULT_PARSER
operator|.
name|declareString
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|name
parameter_list|)
lambda|->
name|result
operator|.
name|name
operator|=
name|name
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|NAME
argument_list|)
argument_list|)
expr_stmt|;
name|RESULT_PARSER
operator|.
name|declareLong
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|count
parameter_list|)
lambda|->
name|result
operator|.
name|count
operator|=
name|count
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|COUNT
argument_list|)
argument_list|)
expr_stmt|;
name|RESULT_PARSER
operator|.
name|declareDouble
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|mean
parameter_list|)
lambda|->
name|result
operator|.
name|mean
operator|=
name|mean
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|MEAN
argument_list|)
argument_list|)
expr_stmt|;
name|RESULT_PARSER
operator|.
name|declareDouble
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|variance
parameter_list|)
lambda|->
name|result
operator|.
name|variance
operator|=
name|variance
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|VARIANCE
argument_list|)
argument_list|)
expr_stmt|;
name|RESULT_PARSER
operator|.
name|declareDouble
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|skewness
parameter_list|)
lambda|->
name|result
operator|.
name|skewness
operator|=
name|skewness
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|SKEWNESS
argument_list|)
argument_list|)
expr_stmt|;
name|RESULT_PARSER
operator|.
name|declareDouble
argument_list|(
parameter_list|(
name|result
parameter_list|,
name|kurtosis
parameter_list|)
lambda|->
name|result
operator|.
name|kurtosis
operator|=
name|kurtosis
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|KURTOSIS
argument_list|)
argument_list|)
expr_stmt|;
name|RESULT_PARSER
operator|.
name|declareObject
argument_list|(
parameter_list|(
name|ParsedMatrixStatsResult
name|result
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|covars
parameter_list|)
lambda|->
block|{
name|result
operator|.
name|covariances
operator|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|covars
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|covar
range|:
name|covars
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|result
operator|.
name|covariances
operator|.
name|put
argument_list|(
name|covar
operator|.
name|getKey
argument_list|()
argument_list|,
name|mapValueAsDouble
argument_list|(
name|covar
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|p
operator|.
name|mapOrdered
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|COVARIANCE
argument_list|)
argument_list|)
expr_stmt|;
name|RESULT_PARSER
operator|.
name|declareObject
argument_list|(
parameter_list|(
name|ParsedMatrixStatsResult
name|result
parameter_list|,
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|correls
parameter_list|)
lambda|->
block|{
name|result
operator|.
name|correlations
operator|=
operator|new
name|LinkedHashMap
argument_list|<>
argument_list|(
name|correls
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|correl
range|:
name|correls
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|result
operator|.
name|correlations
operator|.
name|put
argument_list|(
name|correl
operator|.
name|getKey
argument_list|()
argument_list|,
name|mapValueAsDouble
argument_list|(
name|correl
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|,
parameter_list|(
name|p
parameter_list|,
name|c
parameter_list|)
lambda|->
name|p
operator|.
name|mapOrdered
argument_list|()
argument_list|,
operator|new
name|ParseField
argument_list|(
name|InternalMatrixStats
operator|.
name|Fields
operator|.
name|CORRELATION
argument_list|)
argument_list|)
expr_stmt|;
block|}
DECL|method|mapValueAsDouble
specifier|private
specifier|static
name|Double
name|mapValueAsDouble
parameter_list|(
name|Object
name|value
parameter_list|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|Double
condition|)
block|{
return|return
operator|(
name|Double
operator|)
name|value
return|;
block|}
return|return
name|Double
operator|.
name|valueOf
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
block|}
DECL|method|fromXContent
specifier|static
name|ParsedMatrixStatsResult
name|fromXContent
parameter_list|(
name|XContentParser
name|parser
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|RESULT_PARSER
operator|.
name|parse
argument_list|(
name|parser
argument_list|,
literal|null
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

