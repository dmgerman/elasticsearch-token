begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline.movavg
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
name|ParsingException
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
name|ParseFieldRegistry
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
name|index
operator|.
name|query
operator|.
name|QueryParseContext
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
name|DocValueFormat
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
name|AggregatorFactory
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
name|PipelineAggregationBuilder
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
name|bucket
operator|.
name|histogram
operator|.
name|DateHistogramAggregatorFactory
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
name|bucket
operator|.
name|histogram
operator|.
name|HistogramAggregatorFactory
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
name|AbstractPipelineAggregationBuilder
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
name|BucketHelpers
operator|.
name|GapPolicy
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
name|PipelineAggregator
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
name|models
operator|.
name|MovAvgModel
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
name|models
operator|.
name|MovAvgModelBuilder
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
name|models
operator|.
name|SimpleModel
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

begin_import
import|import static
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
name|PipelineAggregator
operator|.
name|Parser
operator|.
name|BUCKETS_PATH
import|;
end_import

begin_import
import|import static
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
name|PipelineAggregator
operator|.
name|Parser
operator|.
name|FORMAT
import|;
end_import

begin_import
import|import static
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
name|PipelineAggregator
operator|.
name|Parser
operator|.
name|GAP_POLICY
import|;
end_import

begin_class
DECL|class|MovAvgPipelineAggregationBuilder
specifier|public
class|class
name|MovAvgPipelineAggregationBuilder
extends|extends
name|AbstractPipelineAggregationBuilder
argument_list|<
name|MovAvgPipelineAggregationBuilder
argument_list|>
block|{
DECL|field|NAME
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"moving_avg"
decl_stmt|;
DECL|field|MODEL
specifier|public
specifier|static
specifier|final
name|ParseField
name|MODEL
init|=
operator|new
name|ParseField
argument_list|(
literal|"model"
argument_list|)
decl_stmt|;
DECL|field|WINDOW
specifier|private
specifier|static
specifier|final
name|ParseField
name|WINDOW
init|=
operator|new
name|ParseField
argument_list|(
literal|"window"
argument_list|)
decl_stmt|;
DECL|field|SETTINGS
specifier|public
specifier|static
specifier|final
name|ParseField
name|SETTINGS
init|=
operator|new
name|ParseField
argument_list|(
literal|"settings"
argument_list|)
decl_stmt|;
DECL|field|PREDICT
specifier|private
specifier|static
specifier|final
name|ParseField
name|PREDICT
init|=
operator|new
name|ParseField
argument_list|(
literal|"predict"
argument_list|)
decl_stmt|;
DECL|field|MINIMIZE
specifier|private
specifier|static
specifier|final
name|ParseField
name|MINIMIZE
init|=
operator|new
name|ParseField
argument_list|(
literal|"minimize"
argument_list|)
decl_stmt|;
DECL|field|format
specifier|private
name|String
name|format
decl_stmt|;
DECL|field|gapPolicy
specifier|private
name|GapPolicy
name|gapPolicy
init|=
name|GapPolicy
operator|.
name|SKIP
decl_stmt|;
DECL|field|window
specifier|private
name|int
name|window
init|=
literal|5
decl_stmt|;
DECL|field|model
specifier|private
name|MovAvgModel
name|model
init|=
operator|new
name|SimpleModel
argument_list|()
decl_stmt|;
DECL|field|predict
specifier|private
name|int
name|predict
init|=
literal|0
decl_stmt|;
DECL|field|minimize
specifier|private
name|Boolean
name|minimize
decl_stmt|;
DECL|method|MovAvgPipelineAggregationBuilder
specifier|public
name|MovAvgPipelineAggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|,
name|String
name|bucketsPath
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|,
name|NAME
argument_list|,
operator|new
name|String
index|[]
block|{
name|bucketsPath
block|}
argument_list|)
expr_stmt|;
block|}
comment|/**      * Read from a stream.      */
DECL|method|MovAvgPipelineAggregationBuilder
specifier|public
name|MovAvgPipelineAggregationBuilder
parameter_list|(
name|StreamInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|in
argument_list|,
name|NAME
argument_list|)
expr_stmt|;
name|format
operator|=
name|in
operator|.
name|readOptionalString
argument_list|()
expr_stmt|;
name|gapPolicy
operator|=
name|GapPolicy
operator|.
name|readFrom
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|window
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|model
operator|=
name|in
operator|.
name|readNamedWriteable
argument_list|(
name|MovAvgModel
operator|.
name|class
argument_list|)
expr_stmt|;
name|predict
operator|=
name|in
operator|.
name|readVInt
argument_list|()
expr_stmt|;
name|minimize
operator|=
name|in
operator|.
name|readOptionalBoolean
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
DECL|method|doWriteTo
specifier|protected
name|void
name|doWriteTo
parameter_list|(
name|StreamOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeOptionalString
argument_list|(
name|format
argument_list|)
expr_stmt|;
name|gapPolicy
operator|.
name|writeTo
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|window
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeNamedWriteable
argument_list|(
name|model
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeVInt
argument_list|(
name|predict
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeOptionalBoolean
argument_list|(
name|minimize
argument_list|)
expr_stmt|;
block|}
comment|/**      * Sets the format to use on the output of this aggregation.      */
DECL|method|format
specifier|public
name|MovAvgPipelineAggregationBuilder
name|format
parameter_list|(
name|String
name|format
parameter_list|)
block|{
if|if
condition|(
name|format
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[format] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|format
operator|=
name|format
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the format to use on the output of this aggregation.      */
DECL|method|format
specifier|public
name|String
name|format
parameter_list|()
block|{
return|return
name|format
return|;
block|}
comment|/**      * Sets the GapPolicy to use on the output of this aggregation.      */
DECL|method|gapPolicy
specifier|public
name|MovAvgPipelineAggregationBuilder
name|gapPolicy
parameter_list|(
name|GapPolicy
name|gapPolicy
parameter_list|)
block|{
if|if
condition|(
name|gapPolicy
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[gapPolicy] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|gapPolicy
operator|=
name|gapPolicy
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the GapPolicy to use on the output of this aggregation.      */
DECL|method|gapPolicy
specifier|public
name|GapPolicy
name|gapPolicy
parameter_list|()
block|{
return|return
name|gapPolicy
return|;
block|}
DECL|method|formatter
specifier|protected
name|DocValueFormat
name|formatter
parameter_list|()
block|{
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
return|return
operator|new
name|DocValueFormat
operator|.
name|Decimal
argument_list|(
name|format
argument_list|)
return|;
block|}
else|else
block|{
return|return
name|DocValueFormat
operator|.
name|RAW
return|;
block|}
block|}
comment|/**      * Sets the window size for the moving average. This window will "slide"      * across the series, and the values inside that window will be used to      * calculate the moving avg value      *      * @param window      *            Size of window      */
DECL|method|window
specifier|public
name|MovAvgPipelineAggregationBuilder
name|window
parameter_list|(
name|int
name|window
parameter_list|)
block|{
if|if
condition|(
name|window
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[window] must be a positive integer: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|window
operator|=
name|window
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the window size for the moving average. This window will "slide"      * across the series, and the values inside that window will be used to      * calculate the moving avg value      */
DECL|method|window
specifier|public
name|int
name|window
parameter_list|()
block|{
return|return
name|window
return|;
block|}
comment|/**      * Sets a MovAvgModel for the Moving Average. The model is used to      * define what type of moving average you want to use on the series      *      * @param model      *            A MovAvgModel which has been prepopulated with settings      */
DECL|method|modelBuilder
specifier|public
name|MovAvgPipelineAggregationBuilder
name|modelBuilder
parameter_list|(
name|MovAvgModelBuilder
name|model
parameter_list|)
block|{
if|if
condition|(
name|model
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[model] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|model
operator|=
name|model
operator|.
name|build
argument_list|()
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets a MovAvgModel for the Moving Average. The model is used to      * define what type of moving average you want to use on the series      *      * @param model      *            A MovAvgModel which has been prepopulated with settings      */
DECL|method|model
specifier|public
name|MovAvgPipelineAggregationBuilder
name|model
parameter_list|(
name|MovAvgModel
name|model
parameter_list|)
block|{
if|if
condition|(
name|model
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[model] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|model
operator|=
name|model
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets a MovAvgModel for the Moving Average. The model is used to      * define what type of moving average you want to use on the series      */
DECL|method|model
specifier|public
name|MovAvgModel
name|model
parameter_list|()
block|{
return|return
name|model
return|;
block|}
comment|/**      * Sets the number of predictions that should be returned. Each      * prediction will be spaced at the intervals specified in the      * histogram. E.g "predict: 2" will return two new buckets at the end of      * the histogram with the predicted values.      *      * @param predict      *            Number of predictions to make      */
DECL|method|predict
specifier|public
name|MovAvgPipelineAggregationBuilder
name|predict
parameter_list|(
name|int
name|predict
parameter_list|)
block|{
if|if
condition|(
name|predict
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"predict must be greater than 0. Found ["
operator|+
name|predict
operator|+
literal|"] in ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|predict
operator|=
name|predict
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets the number of predictions that should be returned. Each      * prediction will be spaced at the intervals specified in the      * histogram. E.g "predict: 2" will return two new buckets at the end of      * the histogram with the predicted values.      */
DECL|method|predict
specifier|public
name|int
name|predict
parameter_list|()
block|{
return|return
name|predict
return|;
block|}
comment|/**      * Sets whether the model should be fit to the data using a cost      * minimizing algorithm.      *      * @param minimize      *            If the model should be fit to the underlying data      */
DECL|method|minimize
specifier|public
name|MovAvgPipelineAggregationBuilder
name|minimize
parameter_list|(
name|boolean
name|minimize
parameter_list|)
block|{
name|this
operator|.
name|minimize
operator|=
name|minimize
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Gets whether the model should be fit to the data using a cost      * minimizing algorithm.      */
DECL|method|minimize
specifier|public
name|Boolean
name|minimize
parameter_list|()
block|{
return|return
name|minimize
return|;
block|}
annotation|@
name|Override
DECL|method|createInternal
specifier|protected
name|PipelineAggregator
name|createInternal
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
throws|throws
name|IOException
block|{
comment|// If the user doesn't set a preference for cost minimization, ask
comment|// what the model prefers
name|boolean
name|minimize
init|=
name|this
operator|.
name|minimize
operator|==
literal|null
condition|?
name|model
operator|.
name|minimizeByDefault
argument_list|()
else|:
name|this
operator|.
name|minimize
decl_stmt|;
return|return
operator|new
name|MovAvgPipelineAggregator
argument_list|(
name|name
argument_list|,
name|bucketsPaths
argument_list|,
name|formatter
argument_list|()
argument_list|,
name|gapPolicy
argument_list|,
name|window
argument_list|,
name|predict
argument_list|,
name|model
argument_list|,
name|minimize
argument_list|,
name|metaData
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doValidate
specifier|public
name|void
name|doValidate
parameter_list|(
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
index|[]
name|aggFactories
parameter_list|,
name|List
argument_list|<
name|PipelineAggregationBuilder
argument_list|>
name|pipelineAggregatoractories
parameter_list|)
block|{
if|if
condition|(
name|minimize
operator|!=
literal|null
operator|&&
name|minimize
operator|&&
operator|!
name|model
operator|.
name|canBeMinimized
argument_list|()
condition|)
block|{
comment|// If the user asks to minimize, but this model doesn't support
comment|// it, throw exception
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"The ["
operator|+
name|model
operator|+
literal|"] model cannot be minimized for aggregation ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|bucketsPaths
operator|.
name|length
operator|!=
literal|1
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
name|PipelineAggregator
operator|.
name|Parser
operator|.
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" must contain a single entry for aggregation ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|parent
operator|instanceof
name|HistogramAggregatorFactory
condition|)
block|{
name|HistogramAggregatorFactory
name|histoParent
init|=
operator|(
name|HistogramAggregatorFactory
operator|)
name|parent
decl_stmt|;
if|if
condition|(
name|histoParent
operator|.
name|minDocCount
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"parent histogram of moving average aggregation ["
operator|+
name|name
operator|+
literal|"] must have min_doc_count of 0"
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|parent
operator|instanceof
name|DateHistogramAggregatorFactory
condition|)
block|{
name|DateHistogramAggregatorFactory
name|histoParent
init|=
operator|(
name|DateHistogramAggregatorFactory
operator|)
name|parent
decl_stmt|;
if|if
condition|(
name|histoParent
operator|.
name|minDocCount
argument_list|()
operator|!=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"parent histogram of moving average aggregation ["
operator|+
name|name
operator|+
literal|"] must have min_doc_count of 0"
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"moving average aggregation ["
operator|+
name|name
operator|+
literal|"] must have a histogram or date_histogram as parent"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
DECL|method|internalXContent
specifier|protected
name|XContentBuilder
name|internalXContent
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
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|FORMAT
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|format
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
name|GAP_POLICY
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|gapPolicy
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|model
operator|.
name|toXContent
argument_list|(
name|builder
argument_list|,
name|params
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
name|WINDOW
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|window
argument_list|)
expr_stmt|;
if|if
condition|(
name|predict
operator|>
literal|0
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|PREDICT
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|predict
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minimize
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
name|MINIMIZE
operator|.
name|getPreferredName
argument_list|()
argument_list|,
name|minimize
argument_list|)
expr_stmt|;
block|}
return|return
name|builder
return|;
block|}
DECL|method|parse
specifier|public
specifier|static
name|MovAvgPipelineAggregationBuilder
name|parse
parameter_list|(
name|ParseFieldRegistry
argument_list|<
name|MovAvgModel
operator|.
name|AbstractModelParser
argument_list|>
name|movingAverageMdelParserRegistry
parameter_list|,
name|String
name|pipelineAggregatorName
parameter_list|,
name|QueryParseContext
name|context
parameter_list|)
throws|throws
name|IOException
block|{
name|XContentParser
name|parser
init|=
name|context
operator|.
name|parser
argument_list|()
decl_stmt|;
name|XContentParser
operator|.
name|Token
name|token
decl_stmt|;
name|String
name|currentFieldName
init|=
literal|null
decl_stmt|;
name|String
index|[]
name|bucketsPaths
init|=
literal|null
decl_stmt|;
name|String
name|format
init|=
literal|null
decl_stmt|;
name|GapPolicy
name|gapPolicy
init|=
literal|null
decl_stmt|;
name|Integer
name|window
init|=
literal|null
decl_stmt|;
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|settings
init|=
literal|null
decl_stmt|;
name|String
name|model
init|=
literal|null
decl_stmt|;
name|Integer
name|predict
init|=
literal|null
decl_stmt|;
name|Boolean
name|minimize
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_OBJECT
condition|)
block|{
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|FIELD_NAME
condition|)
block|{
name|currentFieldName
operator|=
name|parser
operator|.
name|currentName
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_NUMBER
condition|)
block|{
if|if
condition|(
name|WINDOW
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|window
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|window
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|currentFieldName
operator|+
literal|"] value must be a positive, "
operator|+
literal|"non-zero integer.  Value supplied was ["
operator|+
name|predict
operator|+
literal|"] in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|PREDICT
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|predict
operator|=
name|parser
operator|.
name|intValue
argument_list|()
expr_stmt|;
if|if
condition|(
name|predict
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"["
operator|+
name|currentFieldName
operator|+
literal|"] value must be a positive integer."
operator|+
literal|"  Value supplied was ["
operator|+
name|predict
operator|+
literal|"] in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_STRING
condition|)
block|{
if|if
condition|(
name|FORMAT
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|format
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|BUCKETS_PATH
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|bucketsPaths
operator|=
operator|new
name|String
index|[]
block|{
name|parser
operator|.
name|text
argument_list|()
block|}
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|GAP_POLICY
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|gapPolicy
operator|=
name|GapPolicy
operator|.
name|parse
argument_list|(
name|context
argument_list|,
name|parser
operator|.
name|text
argument_list|()
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|MODEL
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|model
operator|=
name|parser
operator|.
name|text
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_ARRAY
condition|)
block|{
if|if
condition|(
name|BUCKETS_PATH
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|paths
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
while|while
condition|(
operator|(
name|token
operator|=
name|parser
operator|.
name|nextToken
argument_list|()
operator|)
operator|!=
name|XContentParser
operator|.
name|Token
operator|.
name|END_ARRAY
condition|)
block|{
name|String
name|path
init|=
name|parser
operator|.
name|text
argument_list|()
decl_stmt|;
name|paths
operator|.
name|add
argument_list|(
name|path
argument_list|)
expr_stmt|;
block|}
name|bucketsPaths
operator|=
name|paths
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|paths
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|START_OBJECT
condition|)
block|{
if|if
condition|(
name|SETTINGS
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|settings
operator|=
name|parser
operator|.
name|map
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
elseif|else
if|if
condition|(
name|token
operator|==
name|XContentParser
operator|.
name|Token
operator|.
name|VALUE_BOOLEAN
condition|)
block|{
if|if
condition|(
name|MINIMIZE
operator|.
name|match
argument_list|(
name|currentFieldName
argument_list|)
condition|)
block|{
name|minimize
operator|=
name|parser
operator|.
name|booleanValue
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unknown key for a "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]: ["
operator|+
name|currentFieldName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
else|else
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Unexpected token "
operator|+
name|token
operator|+
literal|" in ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]."
argument_list|)
throw|;
block|}
block|}
if|if
condition|(
name|bucketsPaths
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Missing required field ["
operator|+
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|"] for movingAvg aggregation ["
operator|+
name|pipelineAggregatorName
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|MovAvgPipelineAggregationBuilder
name|factory
init|=
operator|new
name|MovAvgPipelineAggregationBuilder
argument_list|(
name|pipelineAggregatorName
argument_list|,
name|bucketsPaths
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|format
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|format
argument_list|(
name|format
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|gapPolicy
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|gapPolicy
argument_list|(
name|gapPolicy
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|window
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|window
argument_list|(
name|window
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|predict
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|predict
argument_list|(
name|predict
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|model
operator|!=
literal|null
condition|)
block|{
name|MovAvgModel
operator|.
name|AbstractModelParser
name|modelParser
init|=
name|movingAverageMdelParserRegistry
operator|.
name|lookup
argument_list|(
name|model
argument_list|,
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|)
decl_stmt|;
name|MovAvgModel
name|movAvgModel
decl_stmt|;
try|try
block|{
name|movAvgModel
operator|=
name|modelParser
operator|.
name|parse
argument_list|(
name|settings
argument_list|,
name|pipelineAggregatorName
argument_list|,
name|factory
operator|.
name|window
argument_list|()
argument_list|,
name|context
operator|.
name|getParseFieldMatcher
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ParseException
name|exception
parameter_list|)
block|{
throw|throw
operator|new
name|ParsingException
argument_list|(
name|parser
operator|.
name|getTokenLocation
argument_list|()
argument_list|,
literal|"Could not parse settings for model ["
operator|+
name|model
operator|+
literal|"]."
argument_list|,
name|exception
argument_list|)
throw|;
block|}
name|factory
operator|.
name|model
argument_list|(
name|movAvgModel
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|minimize
operator|!=
literal|null
condition|)
block|{
name|factory
operator|.
name|minimize
argument_list|(
name|minimize
argument_list|)
expr_stmt|;
block|}
return|return
name|factory
return|;
block|}
annotation|@
name|Override
DECL|method|doHashCode
specifier|protected
name|int
name|doHashCode
parameter_list|()
block|{
return|return
name|Objects
operator|.
name|hash
argument_list|(
name|format
argument_list|,
name|gapPolicy
argument_list|,
name|window
argument_list|,
name|model
argument_list|,
name|predict
argument_list|,
name|minimize
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|doEquals
specifier|protected
name|boolean
name|doEquals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
name|MovAvgPipelineAggregationBuilder
name|other
init|=
operator|(
name|MovAvgPipelineAggregationBuilder
operator|)
name|obj
decl_stmt|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|format
argument_list|,
name|other
operator|.
name|format
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|gapPolicy
argument_list|,
name|other
operator|.
name|gapPolicy
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|window
argument_list|,
name|other
operator|.
name|window
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|model
argument_list|,
name|other
operator|.
name|model
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|predict
argument_list|,
name|other
operator|.
name|predict
argument_list|)
operator|&&
name|Objects
operator|.
name|equals
argument_list|(
name|minimize
argument_list|,
name|other
operator|.
name|minimize
argument_list|)
return|;
block|}
annotation|@
name|Override
DECL|method|getWriteableName
specifier|public
name|String
name|getWriteableName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
end_class

end_unit

