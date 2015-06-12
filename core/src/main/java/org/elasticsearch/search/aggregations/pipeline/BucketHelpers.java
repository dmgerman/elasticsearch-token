begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations.pipeline
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
name|XContentLocation
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
name|SearchParseException
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
name|InternalMultiBucketAggregation
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
name|InvalidAggregationPathException
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
name|metrics
operator|.
name|InternalNumericMetricsAggregation
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
name|derivative
operator|.
name|DerivativeParser
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
name|support
operator|.
name|AggregationPath
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
name|internal
operator|.
name|SearchContext
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
name|List
import|;
end_import

begin_comment
comment|/**  * A set of static helpers to simplify working with aggregation buckets, in  * particular providing utilities that help pipeline aggregations.  */
end_comment

begin_class
DECL|class|BucketHelpers
specifier|public
class|class
name|BucketHelpers
block|{
comment|/**      * A gap policy determines how "holes" in a set of buckets should be handled.  For example,      * a date_histogram might have empty buckets due to no data existing for that time interval.      * This can cause problems for operations like a derivative, which relies on a continuous      * function.      *      * "insert_zeros": empty buckets will be filled with zeros for all metrics      * "ignore": empty buckets will simply be ignored      */
DECL|enum|GapPolicy
specifier|public
specifier|static
enum|enum
name|GapPolicy
block|{
DECL|enum constant|INSERT_ZEROS
DECL|enum constant|SKIP
name|INSERT_ZEROS
argument_list|(
operator|(
name|byte
operator|)
literal|0
argument_list|,
literal|"insert_zeros"
argument_list|)
block|,
name|SKIP
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"skip"
argument_list|)
block|;
comment|/**          * Parse a string GapPolicy into the byte enum          *          * @param context SearchContext this is taking place in          * @param text    GapPolicy in string format (e.g. "ignore")          * @return        GapPolicy enum          */
DECL|method|parse
specifier|public
specifier|static
name|GapPolicy
name|parse
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|String
name|text
parameter_list|,
name|XContentLocation
name|tokenLocation
parameter_list|)
block|{
name|GapPolicy
name|result
init|=
literal|null
decl_stmt|;
for|for
control|(
name|GapPolicy
name|policy
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|policy
operator|.
name|parseField
operator|.
name|match
argument_list|(
name|text
argument_list|)
condition|)
block|{
if|if
condition|(
name|result
operator|==
literal|null
condition|)
block|{
name|result
operator|=
name|policy
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Text can be parsed to 2 different gap policies: text=["
operator|+
name|text
operator|+
literal|"], "
operator|+
literal|"policies="
operator|+
name|Arrays
operator|.
name|asList
argument_list|(
name|result
argument_list|,
name|policy
argument_list|)
argument_list|)
throw|;
block|}
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
name|GapPolicy
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
name|SearchParseException
argument_list|(
name|context
argument_list|,
literal|"Invalid gap policy: ["
operator|+
name|text
operator|+
literal|"], accepted values: "
operator|+
name|validNames
argument_list|,
name|tokenLocation
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
DECL|method|GapPolicy
specifier|private
name|GapPolicy
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
comment|/**          * Serialize the GapPolicy to the output stream          *          * @param out          * @throws IOException          */
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
comment|/**          * Deserialize the GapPolicy from the input stream          *          * @param in          * @return    GapPolicy Enum          * @throws IOException          */
DECL|method|readFrom
specifier|public
specifier|static
name|GapPolicy
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
name|GapPolicy
name|gapPolicy
range|:
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|id
operator|==
name|gapPolicy
operator|.
name|id
condition|)
block|{
return|return
name|gapPolicy
return|;
block|}
block|}
throw|throw
operator|new
name|IllegalStateException
argument_list|(
literal|"Unknown GapPolicy with id ["
operator|+
name|id
operator|+
literal|"]"
argument_list|)
throw|;
block|}
comment|/**          * Return the english-formatted name of the GapPolicy          *          * @return English representation of GapPolicy          */
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
comment|/**      * Given a path and a set of buckets, this method will return the value      * inside the agg at that path. This is used to extract values for use by      * pipeline aggregations (e.g. a derivative might need the price for each      * bucket). If the bucket is empty, the configured GapPolicy is invoked to      * resolve the missing bucket      *      * @param histo      *            A series of agg buckets in the form of a histogram      * @param bucket      *            A specific bucket that a value needs to be extracted from.      *            This bucket should be present in the<code>histo</code>      *            parameter      * @param aggPath      *            The path to a particular value that needs to be extracted.      *            This path should point to a metric inside the      *<code>bucket</code>      * @param gapPolicy      *            The gap policy to apply if empty buckets are found      * @return The value extracted from<code>bucket</code> found at      *<code>aggPath</code>      */
DECL|method|resolveBucketValue
specifier|public
specifier|static
name|Double
name|resolveBucketValue
parameter_list|(
name|InternalMultiBucketAggregation
argument_list|<
name|?
argument_list|,
name|?
extends|extends
name|InternalMultiBucketAggregation
operator|.
name|Bucket
argument_list|>
name|agg
parameter_list|,
name|InternalMultiBucketAggregation
operator|.
name|Bucket
name|bucket
parameter_list|,
name|String
name|aggPath
parameter_list|,
name|GapPolicy
name|gapPolicy
parameter_list|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|aggPathsList
init|=
name|AggregationPath
operator|.
name|parse
argument_list|(
name|aggPath
argument_list|)
operator|.
name|getPathElementsAsStringList
argument_list|()
decl_stmt|;
return|return
name|resolveBucketValue
argument_list|(
name|agg
argument_list|,
name|bucket
argument_list|,
name|aggPathsList
argument_list|,
name|gapPolicy
argument_list|)
return|;
block|}
DECL|method|resolveBucketValue
specifier|public
specifier|static
name|Double
name|resolveBucketValue
parameter_list|(
name|InternalMultiBucketAggregation
argument_list|<
name|?
argument_list|,
name|?
extends|extends
name|InternalMultiBucketAggregation
operator|.
name|Bucket
argument_list|>
name|agg
parameter_list|,
name|InternalMultiBucketAggregation
operator|.
name|Bucket
name|bucket
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|aggPathAsList
parameter_list|,
name|GapPolicy
name|gapPolicy
parameter_list|)
block|{
try|try
block|{
name|Object
name|propertyValue
init|=
name|bucket
operator|.
name|getProperty
argument_list|(
name|agg
operator|.
name|getName
argument_list|()
argument_list|,
name|aggPathAsList
argument_list|)
decl_stmt|;
if|if
condition|(
name|propertyValue
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
name|DerivativeParser
operator|.
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" must reference either a number value or a single value numeric metric aggregation"
argument_list|)
throw|;
block|}
else|else
block|{
name|double
name|value
decl_stmt|;
if|if
condition|(
name|propertyValue
operator|instanceof
name|Number
condition|)
block|{
name|value
operator|=
operator|(
operator|(
name|Number
operator|)
name|propertyValue
operator|)
operator|.
name|doubleValue
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|propertyValue
operator|instanceof
name|InternalNumericMetricsAggregation
operator|.
name|SingleValue
condition|)
block|{
name|value
operator|=
operator|(
operator|(
name|InternalNumericMetricsAggregation
operator|.
name|SingleValue
operator|)
name|propertyValue
operator|)
operator|.
name|value
argument_list|()
expr_stmt|;
block|}
else|else
block|{
throw|throw
operator|new
name|AggregationExecutionException
argument_list|(
name|DerivativeParser
operator|.
name|BUCKETS_PATH
operator|.
name|getPreferredName
argument_list|()
operator|+
literal|" must reference either a number value or a single value numeric metric aggregation"
argument_list|)
throw|;
block|}
comment|// doc count never has missing values so gap policy doesn't apply here
name|boolean
name|isDocCountProperty
init|=
name|aggPathAsList
operator|.
name|size
argument_list|()
operator|==
literal|1
operator|&&
literal|"_count"
operator|.
name|equals
argument_list|(
name|aggPathAsList
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|Double
operator|.
name|isInfinite
argument_list|(
name|value
argument_list|)
operator|||
name|Double
operator|.
name|isNaN
argument_list|(
name|value
argument_list|)
operator|||
operator|(
name|bucket
operator|.
name|getDocCount
argument_list|()
operator|==
literal|0
operator|&&
operator|!
name|isDocCountProperty
operator|)
condition|)
block|{
switch|switch
condition|(
name|gapPolicy
condition|)
block|{
case|case
name|INSERT_ZEROS
case|:
return|return
literal|0.0
return|;
case|case
name|SKIP
case|:
default|default:
return|return
name|Double
operator|.
name|NaN
return|;
block|}
block|}
else|else
block|{
return|return
name|value
return|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|InvalidAggregationPathException
name|e
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
block|}
block|}
end_class

end_unit
