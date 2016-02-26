begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.movavg.models
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
operator|.
name|pipeline
operator|.
name|movavg
operator|.
name|models
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|ElasticsearchParseException
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
name|Nullable
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
name|ParseFieldMatcher
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
name|search
operator|.
name|aggregations
operator|.
name|AggregationExecutionException
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
name|pipeline
operator|.
name|movavg
operator|.
name|MovAvgParser
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
name|text
operator|.
name|ParseException
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
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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

begin_comment
comment|/**  * Calculate a triple exponential weighted moving average  */
end_comment

begin_class
DECL|class|HoltWintersModel
specifier|public
class|class
name|HoltWintersModel
extends|extends
name|MovAvgModel
block|{
DECL|field|NAME_FIELD
specifier|protected
specifier|static
specifier|final
name|ParseField
name|NAME_FIELD
init|=
operator|new
name|ParseField
argument_list|(
literal|"holt_winters"
argument_list|)
decl_stmt|;
DECL|field|DEFAULT_ALPHA
specifier|public
specifier|static
specifier|final
name|double
name|DEFAULT_ALPHA
init|=
literal|0.3
decl_stmt|;
DECL|field|DEFAULT_BETA
specifier|public
specifier|static
specifier|final
name|double
name|DEFAULT_BETA
init|=
literal|0.1
decl_stmt|;
DECL|field|DEFAULT_GAMMA
specifier|public
specifier|static
specifier|final
name|double
name|DEFAULT_GAMMA
init|=
literal|0.3
decl_stmt|;
DECL|field|DEFAULT_PERIOD
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_PERIOD
init|=
literal|1
decl_stmt|;
DECL|field|DEFAULT_SEASONALITY_TYPE
specifier|public
specifier|static
specifier|final
name|SeasonalityType
name|DEFAULT_SEASONALITY_TYPE
init|=
name|SeasonalityType
operator|.
name|ADDITIVE
decl_stmt|;
DECL|field|DEFAULT_PAD
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_PAD
init|=
literal|false
decl_stmt|;
DECL|field|PROTOTYPE
specifier|private
specifier|static
specifier|final
name|HoltWintersModel
name|PROTOTYPE
init|=
operator|new
name|HoltWintersModel
argument_list|()
decl_stmt|;
comment|/**      * Controls smoothing of data.  Also known as "level" value.      * Alpha = 1 retains no memory of past values      * (e.g. random walk), while alpha = 0 retains infinite memory of past values (e.g.      * mean of the series).      */
DECL|field|alpha
specifier|private
specifier|final
name|double
name|alpha
decl_stmt|;
comment|/**      * Controls smoothing of trend.      * Beta = 1 retains no memory of past values      * (e.g. random walk), while alpha = 0 retains infinite memory of past values (e.g.      * mean of the series).      */
DECL|field|beta
specifier|private
specifier|final
name|double
name|beta
decl_stmt|;
comment|/**      * Controls smoothing of seasonality.      * Gamma = 1 retains no memory of past values      * (e.g. random walk), while alpha = 0 retains infinite memory of past values (e.g.      * mean of the series).      */
DECL|field|gamma
specifier|private
specifier|final
name|double
name|gamma
decl_stmt|;
comment|/**      * Periodicity of the data      */
DECL|field|period
specifier|private
specifier|final
name|int
name|period
decl_stmt|;
comment|/**      * Whether this is a multiplicative or additive HW      */
DECL|field|seasonalityType
specifier|private
specifier|final
name|SeasonalityType
name|seasonalityType
decl_stmt|;
comment|/**      * Padding is used to add a very small amount to values, so that zeroes do not interfere      * with multiplicative seasonality math (e.g. division by zero)      */
DECL|field|pad
specifier|private
specifier|final
name|boolean
name|pad
decl_stmt|;
DECL|field|padding
specifier|private
specifier|final
name|double
name|padding
decl_stmt|;
DECL|enum|SeasonalityType
specifier|public
enum|enum
name|SeasonalityType
block|{
DECL|enum constant|ADDITIVE
DECL|enum constant|MULTIPLICATIVE
name|ADDITIVE
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|,
literal|"add"
argument_list|)
block|,
name|MULTIPLICATIVE
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"mult"
argument_list|)
block|;
comment|/**          * Parse a string SeasonalityType into the byte enum          *          * @param text                SeasonalityType in string format (e.g. "add")          * @param parseFieldMatcher   Matcher for field names          * @return                    SeasonalityType enum          */
annotation|@
name|Nullable
DECL|method|parse
specifier|public
specifier|static
name|SeasonalityType
name|parse
parameter_list|(
name|String
name|text
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
block|{
if|if
condition|(
name|text
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
name|SeasonalityType
name|result
init|=
literal|null
decl_stmt|;
for|for
control|(
name|SeasonalityType
name|policy
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|parseFieldMatcher
operator|.
name|match
argument_list|(
name|text
argument_list|,
name|policy
operator|.
name|parseField
argument_list|)
condition|)
block|{
name|result
operator|=
name|policy
expr_stmt|;
break|break;
block|}
block|}
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|validNames
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|SeasonalityType
name|policy
range|:
name|values
argument_list|()
control|)
block|{
name|validNames
operator|.
name|add
argument_list|(
name|policy
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
throw|throw
operator|new
name|ElasticsearchParseException
argument_list|(
literal|"failed to parse seasonality type [{}]. accepted values are [{}]"
argument_list|,
name|text
argument_list|,
name|validNames
argument_list|)
throw|;
block|}
return|return
name|result
return|;
block|}
DECL|field|id
specifier|private
specifier|final
name|byte
name|id
decl_stmt|;
DECL|field|parseField
specifier|private
specifier|final
name|ParseField
name|parseField
decl_stmt|;
DECL|method|SeasonalityType
name|SeasonalityType
parameter_list|(
name|byte
name|id
parameter_list|,
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|parseField
operator|=
operator|new
name|ParseField
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**          * Serialize the SeasonalityType to the output stream          */
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
name|out
operator|.
name|writeByte
argument_list|(
name|id
argument_list|)
expr_stmt|;
block|}
comment|/**          * Deserialize the SeasonalityType from the input stream          *          * @param in  the input stream          * @return    SeasonalityType Enum          */
DECL|method|readFrom
specifier|public
specifier|static
name|SeasonalityType
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|byte
name|id
init|=
name|in
operator|.
name|readByte
argument_list|()
decl_stmt|;
for|for
control|(
name|SeasonalityType
name|seasonalityType
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|id
operator|==
name|seasonalityType
operator|.
name|id
condition|)
block|{
return|return
name|seasonalityType
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unknown Seasonality Type with id ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|/**          * Return the english-formatted name of the SeasonalityType          *          * @return English representation of SeasonalityType          */
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|parseField
operator|.
name|getPreferredName
argument_list|()
return|;
block|}
block|}
DECL|method|HoltWintersModel
specifier|public
name|HoltWintersModel
parameter_list|()
block|{
name|this
argument_list|(
name|DEFAULT_ALPHA
argument_list|,
name|DEFAULT_BETA
argument_list|,
name|DEFAULT_GAMMA
argument_list|,
name|DEFAULT_PERIOD
argument_list|,
name|DEFAULT_SEASONALITY_TYPE
argument_list|,
name|DEFAULT_PAD
argument_list|)
expr_stmt|;
block|}
DECL|method|HoltWintersModel
specifier|public
name|HoltWintersModel
parameter_list|(
name|double
name|alpha
parameter_list|,
name|double
name|beta
parameter_list|,
name|double
name|gamma
parameter_list|,
name|int
name|period
parameter_list|,
name|SeasonalityType
name|seasonalityType
parameter_list|,
name|boolean
name|pad
parameter_list|)
block|{
name|this
operator|.
name|alpha
operator|=
name|alpha
expr_stmt|;
name|this
operator|.
name|beta
operator|=
name|beta
expr_stmt|;
name|this
operator|.
name|gamma
operator|=
name|gamma
expr_stmt|;
name|this
operator|.
name|period
operator|=
name|period
expr_stmt|;
name|this
operator|.
name|seasonalityType
operator|=
name|seasonalityType
expr_stmt|;
name|this
operator|.
name|pad
operator|=
name|pad
expr_stmt|;
comment|// Only pad if we are multiplicative and padding is enabled
comment|// The padding amount is not currently user-configurable...i don't see a reason to expose it?
name|this
operator|.
name|padding
operator|=
name|seasonalityType
operator|.
name|equals
argument_list|(
name|SeasonalityType
operator|.
name|MULTIPLICATIVE
argument_list|)
operator|&&
name|pad
condition|?
literal|0.0000000001
else|:
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|minimizeByDefault
specifier|public
name|boolean
name|minimizeByDefault
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|canBeMinimized
specifier|public
name|boolean
name|canBeMinimized
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
DECL|method|neighboringModel
specifier|public
name|MovAvgModel
name|neighboringModel
parameter_list|()
block|{
name|double
name|newValue
init|=
name|Math
operator|.
name|random
argument_list|()
decl_stmt|;
switch|switch
condition|(
call|(
name|int
call|)
argument_list|(
name|Math
operator|.
name|random
argument_list|()
operator|*
literal|3
argument_list|)
condition|)
block|{
case|case
literal|0
case|:
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|newValue
argument_list|,
name|beta
argument_list|,
name|gamma
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
case|case
literal|1
case|:
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|alpha
argument_list|,
name|newValue
argument_list|,
name|gamma
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
case|case
literal|2
case|:
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|alpha
argument_list|,
name|beta
argument_list|,
name|newValue
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
default|default:
assert|assert
operator|(
literal|false
operator|)
operator|:
literal|"Random value fell outside of range [0-2]"
assert|;
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|newValue
argument_list|,
name|beta
argument_list|,
name|gamma
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
comment|// This should never technically happen...
block|}
block|}
annotation|@
name|Override
DECL|method|clone
specifier|public
name|MovAvgModel
name|clone
parameter_list|()
block|{
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|alpha
argument_list|,
name|beta
argument_list|,
name|gamma
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|hasValue
specifier|public
name|boolean
name|hasValue
parameter_list|(
name|int
name|valuesAvailable
parameter_list|)
block|{
comment|// We need at least (period * 2) data-points (e.g. two "seasons")
return|return
name|valuesAvailable
operator|>=
name|period
operator|*
literal|2
return|;
block|}
comment|/**      * Predicts the next `n` values in the series, using the smoothing model to generate new values.      * Unlike the other moving averages, HoltWinters has forecasting/prediction built into the algorithm.      * Prediction is more than simply adding the next prediction to the window and repeating.  HoltWinters      * will extrapolate into the future by applying the trend and seasonal information to the smoothed data.      *      * @param values            Collection of numerics to movingAvg, usually windowed      * @param numPredictions    Number of newly generated predictions to return      * @param<T>               Type of numeric      * @return                  Returns an array of doubles, since most smoothing methods operate on floating points      */
annotation|@
name|Override
DECL|method|doPredict
specifier|protected
parameter_list|<
name|T
extends|extends
name|Number
parameter_list|>
name|double
index|[]
name|doPredict
parameter_list|(
name|Collection
argument_list|<
name|T
argument_list|>
name|values
parameter_list|,
name|int
name|numPredictions
parameter_list|)
block|{
return|return
name|next
argument_list|(
name|values
argument_list|,
name|numPredictions
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|next
specifier|public
parameter_list|<
name|T
extends|extends
name|Number
parameter_list|>
name|double
name|next
parameter_list|(
name|Collection
argument_list|<
name|T
argument_list|>
name|values
parameter_list|)
block|{
return|return
name|next
argument_list|(
name|values
argument_list|,
literal|1
argument_list|)
index|[
literal|0
index|]
return|;
block|}
comment|/**      * Calculate a doubly exponential weighted moving average      *      * @param values Collection of values to calculate avg for      * @param numForecasts number of forecasts into the future to return      *      * @param<T>    Type T extending Number      * @return       Returns a Double containing the moving avg for the window      */
DECL|method|next
specifier|public
parameter_list|<
name|T
extends|extends
name|Number
parameter_list|>
name|double
index|[]
name|next
parameter_list|(
name|Collection
argument_list|<
name|T
argument_list|>
name|values
parameter_list|,
name|int
name|numForecasts
parameter_list|)
block|{
if|if
condition|(
name|values
operator|.
name|size
argument_list|()
operator|<
name|period
operator|*
literal|2
condition|)
block|{
comment|// We need at least two full "seasons" to use HW
comment|// This should have been caught earlier, we can't do anything now...bail
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
literal|"Holt-Winters aggregation requires at least (2 * period == 2 * "
operator|+
name|period
operator|+
literal|" == "
operator|+
operator|(
literal|2
operator|*
name|period
operator|)
operator|+
literal|") data-points to function.  Only ["
operator|+
name|values
operator|.
name|size
argument_list|()
operator|+
literal|"] were provided."
argument_list|)
throw|;
block|}
comment|// Smoothed value
name|double
name|s
init|=
literal|0
decl_stmt|;
name|double
name|last_s
decl_stmt|;
comment|// Trend value
name|double
name|b
init|=
literal|0
decl_stmt|;
name|double
name|last_b
init|=
literal|0
decl_stmt|;
comment|// Seasonal value
name|double
index|[]
name|seasonal
init|=
operator|new
name|double
index|[
name|values
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
name|int
name|counter
init|=
literal|0
decl_stmt|;
name|double
index|[]
name|vs
init|=
operator|new
name|double
index|[
name|values
operator|.
name|size
argument_list|()
index|]
decl_stmt|;
for|for
control|(
name|T
name|v
range|:
name|values
control|)
block|{
name|vs
index|[
name|counter
index|]
operator|=
name|v
operator|.
name|doubleValue
argument_list|()
operator|+
name|padding
expr_stmt|;
name|counter
operator|+=
literal|1
expr_stmt|;
block|}
comment|// Initial level value is average of first season
comment|// Calculate the slopes between first and second season for each period
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|period
condition|;
name|i
operator|++
control|)
block|{
name|s
operator|+=
name|vs
index|[
name|i
index|]
expr_stmt|;
name|b
operator|+=
operator|(
name|vs
index|[
name|i
operator|+
name|period
index|]
operator|-
name|vs
index|[
name|i
index|]
operator|)
operator|/
name|period
expr_stmt|;
block|}
name|s
operator|/=
name|period
expr_stmt|;
name|b
operator|/=
name|period
expr_stmt|;
name|last_s
operator|=
name|s
expr_stmt|;
comment|// Calculate first seasonal
if|if
condition|(
name|Double
operator|.
name|compare
argument_list|(
name|s
argument_list|,
literal|0.0
argument_list|)
operator|==
literal|0
operator|||
name|Double
operator|.
name|compare
argument_list|(
name|s
argument_list|,
operator|-
literal|0.0
argument_list|)
operator|==
literal|0
condition|)
block|{
name|Arrays
operator|.
name|fill
argument_list|(
name|seasonal
argument_list|,
literal|0.0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|period
condition|;
name|i
operator|++
control|)
block|{
name|seasonal
index|[
name|i
index|]
operator|=
name|vs
index|[
name|i
index|]
operator|/
name|s
expr_stmt|;
block|}
block|}
for|for
control|(
name|int
name|i
init|=
name|period
init|;
name|i
operator|<
name|vs
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
comment|// TODO if perf is a problem, we can specialize a subclass to avoid conditionals on each iteration
if|if
condition|(
name|seasonalityType
operator|.
name|equals
argument_list|(
name|SeasonalityType
operator|.
name|MULTIPLICATIVE
argument_list|)
condition|)
block|{
name|s
operator|=
name|alpha
operator|*
operator|(
name|vs
index|[
name|i
index|]
operator|/
name|seasonal
index|[
name|i
operator|-
name|period
index|]
operator|)
operator|+
operator|(
literal|1.0d
operator|-
name|alpha
operator|)
operator|*
operator|(
name|last_s
operator|+
name|last_b
operator|)
expr_stmt|;
block|}
else|else
block|{
name|s
operator|=
name|alpha
operator|*
operator|(
name|vs
index|[
name|i
index|]
operator|-
name|seasonal
index|[
name|i
operator|-
name|period
index|]
operator|)
operator|+
operator|(
literal|1.0d
operator|-
name|alpha
operator|)
operator|*
operator|(
name|last_s
operator|+
name|last_b
operator|)
expr_stmt|;
block|}
name|b
operator|=
name|beta
operator|*
operator|(
name|s
operator|-
name|last_s
operator|)
operator|+
operator|(
literal|1
operator|-
name|beta
operator|)
operator|*
name|last_b
expr_stmt|;
if|if
condition|(
name|seasonalityType
operator|.
name|equals
argument_list|(
name|SeasonalityType
operator|.
name|MULTIPLICATIVE
argument_list|)
condition|)
block|{
name|seasonal
index|[
name|i
index|]
operator|=
name|gamma
operator|*
operator|(
name|vs
index|[
name|i
index|]
operator|/
operator|(
name|last_s
operator|+
name|last_b
operator|)
operator|)
operator|+
operator|(
literal|1
operator|-
name|gamma
operator|)
operator|*
name|seasonal
index|[
name|i
operator|-
name|period
index|]
expr_stmt|;
block|}
else|else
block|{
name|seasonal
index|[
name|i
index|]
operator|=
name|gamma
operator|*
operator|(
name|vs
index|[
name|i
index|]
operator|-
operator|(
name|last_s
operator|-
name|last_b
operator|)
operator|)
operator|+
operator|(
literal|1
operator|-
name|gamma
operator|)
operator|*
name|seasonal
index|[
name|i
operator|-
name|period
index|]
expr_stmt|;
block|}
name|last_s
operator|=
name|s
expr_stmt|;
name|last_b
operator|=
name|b
expr_stmt|;
block|}
name|double
index|[]
name|forecastValues
init|=
operator|new
name|double
index|[
name|numForecasts
index|]
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|numForecasts
condition|;
name|i
operator|++
control|)
block|{
name|int
name|idx
init|=
name|values
operator|.
name|size
argument_list|()
operator|-
name|period
operator|+
operator|(
operator|(
name|i
operator|-
literal|1
operator|)
operator|%
name|period
operator|)
decl_stmt|;
comment|// TODO perhaps pad out seasonal to a power of 2 and use a mask instead of modulo?
if|if
condition|(
name|seasonalityType
operator|.
name|equals
argument_list|(
name|SeasonalityType
operator|.
name|MULTIPLICATIVE
argument_list|)
condition|)
block|{
name|forecastValues
index|[
name|i
operator|-
literal|1
index|]
operator|=
operator|(
name|s
operator|+
operator|(
name|i
operator|*
name|b
operator|)
operator|)
operator|*
name|seasonal
index|[
name|idx
index|]
expr_stmt|;
block|}
else|else
block|{
name|forecastValues
index|[
name|i
operator|-
literal|1
index|]
operator|=
name|s
operator|+
operator|(
name|i
operator|*
name|b
operator|)
operator|+
name|seasonal
index|[
name|idx
index|]
expr_stmt|;
block|}
block|}
return|return
name|forecastValues
return|;
block|}
DECL|field|STREAM
specifier|public
specifier|static
specifier|final
name|MovAvgModelStreams
operator|.
name|Stream
name|STREAM
init|=
operator|new
name|MovAvgModelStreams
operator|.
name|Stream
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|MovAvgModel
name|readResult
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|PROTOTYPE
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAME_FIELD
operator|.
name|getPreferredName
argument_list|()
return|;
block|}
block|}
decl_stmt|;
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|MovAvgParser
operator|.
name|MODEL
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|NAME_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|MovAvgParser
operator|.
name|SETTINGS
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"alpha"
argument_list|,
name|alpha
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"beta"
argument_list|,
name|beta
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"gamma"
argument_list|,
name|gamma
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"period"
argument_list|,
name|period
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"pad"
argument_list|,
name|pad
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|seasonalityType
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|readFrom
specifier|public
name|MovAvgModel
name|readFrom
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readDouble
argument_list|()
argument_list|,
name|in
operator|.
name|readVInt
argument_list|()
argument_list|,
name|SeasonalityType
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
argument_list|,
name|in
operator|.
name|readBoolean
argument_list|()
argument_list|)
return|;
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
name|out
operator|.
name|writeString
argument_list|(
name|STREAM
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|alpha
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|beta
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeDouble
argument_list|(
name|gamma
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|period
argument_list|)
expr_stmt|;
name|seasonalityType
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|pad
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|hashCode
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|alpha
argument_list|,
name|beta
argument_list|,
name|gamma
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|equals
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
if|if
condition|(
name|obj
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|getClass
argument_list|()
operator|!=
name|obj
operator|.
name|getClass
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
name|HoltWintersModel
name|other
init|=
operator|(
name|HoltWintersModel
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|alpha
argument_list|,
name|other
operator|.
name|alpha
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|beta
argument_list|,
name|other
operator|.
name|beta
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|gamma
argument_list|,
name|other
operator|.
name|gamma
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|period
argument_list|,
name|other
operator|.
name|period
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|seasonalityType
argument_list|,
name|other
operator|.
name|seasonalityType
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|pad
argument_list|,
name|other
operator|.
name|pad
argument_list|)
return|;
block|}
DECL|class|HoltWintersModelParser
specifier|public
specifier|static
class|class
name|HoltWintersModelParser
extends|extends
name|AbstractModelParser
block|{
annotation|@
name|Override
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|NAME_FIELD
operator|.
name|getPreferredName
argument_list|()
return|;
block|}
annotation|@
name|Override
DECL|method|parse
specifier|public
name|MovAvgModel
name|parse
parameter_list|(
annotation|@
name|Nullable
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
parameter_list|,
name|String
name|pipelineName
parameter_list|,
name|int
name|windowSize
parameter_list|,
name|ParseFieldMatcher
name|parseFieldMatcher
parameter_list|)
throws|throws
name|ParseException
block|{
name|double
name|alpha
init|=
name|parseDoubleParam
argument_list|(
name|settings
argument_list|,
literal|"alpha"
argument_list|,
name|DEFAULT_ALPHA
argument_list|)
decl_stmt|;
name|double
name|beta
init|=
name|parseDoubleParam
argument_list|(
name|settings
argument_list|,
literal|"beta"
argument_list|,
name|DEFAULT_BETA
argument_list|)
decl_stmt|;
name|double
name|gamma
init|=
name|parseDoubleParam
argument_list|(
name|settings
argument_list|,
literal|"gamma"
argument_list|,
name|DEFAULT_GAMMA
argument_list|)
decl_stmt|;
name|int
name|period
init|=
name|parseIntegerParam
argument_list|(
name|settings
argument_list|,
literal|"period"
argument_list|,
name|DEFAULT_PERIOD
argument_list|)
decl_stmt|;
if|if
condition|(
name|windowSize
operator|<
literal|2
operator|*
name|period
condition|)
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Field [window] must be at least twice as large as the period when "
operator|+
literal|"using Holt-Winters.  Value provided was ["
operator|+
name|windowSize
operator|+
literal|"], which is less than (2*period) == "
operator|+
operator|(
literal|2
operator|*
name|period
operator|)
argument_list|,
literal|0
argument_list|)
throw|;
block|}
name|SeasonalityType
name|seasonalityType
init|=
name|DEFAULT_SEASONALITY_TYPE
decl_stmt|;
if|if
condition|(
name|settings
operator|!=
literal|null
condition|)
block|{
name|Object
name|value
init|=
name|settings
operator|.
name|get
argument_list|(
literal|"type"
argument_list|)
decl_stmt|;
if|if
condition|(
name|value
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|value
operator|instanceof
name|String
condition|)
block|{
name|seasonalityType
operator|=
name|SeasonalityType
operator|.
name|parse
argument_list|(
operator|(
name|String
operator|)
name|value
argument_list|,
name|parseFieldMatcher
argument_list|)
expr_stmt|;
name|settings
operator|.
name|remove
argument_list|(
literal|"type"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParseException
argument_list|(
literal|"Parameter [type] must be a String, type `"
operator|+
name|value
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"` provided instead"
argument_list|,
literal|0
argument_list|)
throw|;
block|}
block|}
block|}
name|boolean
name|pad
init|=
name|parseBoolParam
argument_list|(
name|settings
argument_list|,
literal|"pad"
argument_list|,
name|seasonalityType
operator|.
name|equals
argument_list|(
name|SeasonalityType
operator|.
name|MULTIPLICATIVE
argument_list|)
argument_list|)
decl_stmt|;
name|checkUnrecognizedParams
argument_list|(
name|settings
argument_list|)
expr_stmt|;
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|alpha
argument_list|,
name|beta
argument_list|,
name|gamma
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
block|}
block|}
DECL|class|HoltWintersModelBuilder
specifier|public
specifier|static
class|class
name|HoltWintersModelBuilder
implements|implements
name|MovAvgModelBuilder
block|{
DECL|field|alpha
specifier|private
name|double
name|alpha
init|=
name|DEFAULT_ALPHA
decl_stmt|;
DECL|field|beta
specifier|private
name|double
name|beta
init|=
name|DEFAULT_BETA
decl_stmt|;
DECL|field|gamma
specifier|private
name|double
name|gamma
init|=
name|DEFAULT_GAMMA
decl_stmt|;
DECL|field|period
specifier|private
name|int
name|period
init|=
name|DEFAULT_PERIOD
decl_stmt|;
DECL|field|seasonalityType
specifier|private
name|SeasonalityType
name|seasonalityType
init|=
name|DEFAULT_SEASONALITY_TYPE
decl_stmt|;
DECL|field|pad
specifier|private
name|Boolean
name|pad
init|=
literal|null
decl_stmt|;
comment|/**          * Alpha controls the smoothing of the data.  Alpha = 1 retains no memory of past values          * (e.g. a random walk), while alpha = 0 retains infinite memory of past values (e.g.          * the series mean).  Useful values are somewhere in between.  Defaults to 0.5.          *          * @param alpha A double between 0-1 inclusive, controls data smoothing          *          * @return The builder to continue chaining          */
DECL|method|alpha
specifier|public
name|HoltWintersModelBuilder
name|alpha
parameter_list|(
name|double
name|alpha
parameter_list|)
block|{
name|this
operator|.
name|alpha
operator|=
name|alpha
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**          * Equivalent to<code>alpha</code>, but controls the smoothing of the trend instead of the data          *          * @param beta a double between 0-1 inclusive, controls trend smoothing          *          * @return The builder to continue chaining          */
DECL|method|beta
specifier|public
name|HoltWintersModelBuilder
name|beta
parameter_list|(
name|double
name|beta
parameter_list|)
block|{
name|this
operator|.
name|beta
operator|=
name|beta
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|gamma
specifier|public
name|HoltWintersModelBuilder
name|gamma
parameter_list|(
name|double
name|gamma
parameter_list|)
block|{
name|this
operator|.
name|gamma
operator|=
name|gamma
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|period
specifier|public
name|HoltWintersModelBuilder
name|period
parameter_list|(
name|int
name|period
parameter_list|)
block|{
name|this
operator|.
name|period
operator|=
name|period
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|seasonalityType
specifier|public
name|HoltWintersModelBuilder
name|seasonalityType
parameter_list|(
name|SeasonalityType
name|type
parameter_list|)
block|{
name|this
operator|.
name|seasonalityType
operator|=
name|type
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|pad
specifier|public
name|HoltWintersModelBuilder
name|pad
parameter_list|(
name|boolean
name|pad
parameter_list|)
block|{
name|this
operator|.
name|pad
operator|=
name|pad
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
DECL|method|toXContent
specifier|public
name|XContentBuilder
name|toXContent
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
name|MovAvgParser
operator|.
name|MODEL
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|NAME_FIELD
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|MovAvgParser
operator|.
name|SETTINGS
operator|.
name|getPreferredName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"alpha"
argument_list|,
name|alpha
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"beta"
argument_list|,
name|beta
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"gamma"
argument_list|,
name|gamma
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"period"
argument_list|,
name|period
argument_list|)
expr_stmt|;
if|if
condition|(
name|pad
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"pad"
argument_list|,
name|pad
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"type"
argument_list|,
name|seasonalityType
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
return|return
name|builder
return|;
block|}
annotation|@
name|Override
DECL|method|build
specifier|public
name|MovAvgModel
name|build
parameter_list|()
block|{
name|boolean
name|pad
init|=
name|this
operator|.
name|pad
operator|==
literal|null
condition|?
operator|(
name|seasonalityType
operator|==
name|SeasonalityType
operator|.
name|MULTIPLICATIVE
operator|)
else|:
name|this
operator|.
name|pad
decl_stmt|;
return|return
operator|new
name|HoltWintersModel
argument_list|(
name|alpha
argument_list|,
name|beta
argument_list|,
name|gamma
argument_list|,
name|period
argument_list|,
name|seasonalityType
argument_list|,
name|pad
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

