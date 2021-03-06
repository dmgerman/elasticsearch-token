begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elasticsearch under one or more contributor  * license agreements. See the NOTICE file distributed with  * this work for additional information regarding copyright  * ownership. Elasticsearch licenses this file to you under  * the Apache License, Version 2.0 (the "License"); you may  * not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.aggregations
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|aggregations
package|;
end_package

begin_import
import|import
name|org
operator|.
name|elasticsearch
operator|.
name|action
operator|.
name|support
operator|.
name|ToXContentToBytes
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
name|io
operator|.
name|stream
operator|.
name|NamedWriteable
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
name|ToXContent
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

begin_comment
comment|/**  * A factory that knows how to create an {@link Aggregator} of a specific type.  */
end_comment

begin_class
DECL|class|AggregationBuilder
specifier|public
specifier|abstract
class|class
name|AggregationBuilder
extends|extends
name|ToXContentToBytes
implements|implements
name|NamedWriteable
implements|,
name|ToXContent
implements|,
name|BaseAggregationBuilder
block|{
DECL|field|name
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
DECL|field|factoriesBuilder
specifier|protected
name|AggregatorFactories
operator|.
name|Builder
name|factoriesBuilder
init|=
name|AggregatorFactories
operator|.
name|builder
argument_list|()
decl_stmt|;
comment|/**      * Constructs a new aggregation builder.      *      * @param name  The aggregation name      */
DECL|method|AggregationBuilder
specifier|protected
name|AggregationBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
name|name
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"[name] must not be null: ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
comment|/** Return this aggregation's name. */
DECL|method|getName
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
comment|/** Internal: build an {@link AggregatorFactory} based on the configuration of this builder. */
DECL|method|build
specifier|protected
specifier|abstract
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|build
parameter_list|(
name|SearchContext
name|context
parameter_list|,
name|AggregatorFactory
argument_list|<
name|?
argument_list|>
name|parent
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/** Associate metadata with this {@link AggregationBuilder}. */
annotation|@
name|Override
DECL|method|setMetaData
specifier|public
specifier|abstract
name|AggregationBuilder
name|setMetaData
parameter_list|(
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|metaData
parameter_list|)
function_decl|;
comment|/** Return any associated metadata with this {@link AggregationBuilder}. */
DECL|method|getMetaData
specifier|public
specifier|abstract
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|getMetaData
parameter_list|()
function_decl|;
comment|/** Add a sub aggregation to this builder. */
DECL|method|subAggregation
specifier|public
specifier|abstract
name|AggregationBuilder
name|subAggregation
parameter_list|(
name|AggregationBuilder
name|aggregation
parameter_list|)
function_decl|;
comment|/** Add a sub aggregation to this builder. */
DECL|method|subAggregation
specifier|public
specifier|abstract
name|AggregationBuilder
name|subAggregation
parameter_list|(
name|PipelineAggregationBuilder
name|aggregation
parameter_list|)
function_decl|;
comment|/** Return the configured set of subaggregations **/
DECL|method|getSubAggregations
specifier|public
name|List
argument_list|<
name|AggregationBuilder
argument_list|>
name|getSubAggregations
parameter_list|()
block|{
return|return
name|factoriesBuilder
operator|.
name|getAggregatorFactories
argument_list|()
return|;
block|}
comment|/** Return the configured set of pipeline aggregations **/
DECL|method|getPipelineAggregations
specifier|public
name|List
argument_list|<
name|PipelineAggregationBuilder
argument_list|>
name|getPipelineAggregations
parameter_list|()
block|{
return|return
name|factoriesBuilder
operator|.
name|getPipelineAggregatorFactories
argument_list|()
return|;
block|}
comment|/**      * Internal: Registers sub-factories with this factory. The sub-factory will be      * responsible for the creation of sub-aggregators under the aggregator      * created by this factory. This is only for use by {@link AggregatorFactories#parseAggregators(QueryParseContext)}.      *      * @param subFactories      *            The sub-factories      * @return this factory (fluent interface)      */
annotation|@
name|Override
DECL|method|subAggregations
specifier|public
specifier|abstract
name|AggregationBuilder
name|subAggregations
parameter_list|(
name|AggregatorFactories
operator|.
name|Builder
name|subFactories
parameter_list|)
function_decl|;
comment|/** Common xcontent fields shared among aggregator builders */
DECL|class|CommonFields
specifier|public
specifier|static
specifier|final
class|class
name|CommonFields
extends|extends
name|ParseField
operator|.
name|CommonFields
block|{
DECL|field|VALUE_TYPE
specifier|public
specifier|static
specifier|final
name|ParseField
name|VALUE_TYPE
init|=
operator|new
name|ParseField
argument_list|(
literal|"value_type"
argument_list|)
decl_stmt|;
block|}
block|}
end_class

end_unit

