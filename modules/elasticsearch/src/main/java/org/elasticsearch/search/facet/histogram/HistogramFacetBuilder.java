begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to Elastic Search and Shay Banon under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. Elastic Search licenses this  * file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *    http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied.  See the License for the  * specific language governing permissions and limitations  * under the License.  */
end_comment

begin_package
DECL|package|org.elasticsearch.search.facet.histogram
package|package
name|org
operator|.
name|elasticsearch
operator|.
name|search
operator|.
name|facet
operator|.
name|histogram
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
name|index
operator|.
name|query
operator|.
name|FilterBuilder
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
name|builder
operator|.
name|SearchSourceBuilderException
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
name|facet
operator|.
name|AbstractFacetBuilder
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
name|concurrent
operator|.
name|TimeUnit
import|;
end_import

begin_comment
comment|/**  * A facet builder of histogram facets.  *  * @author kimchy (shay.banon)  */
end_comment

begin_class
DECL|class|HistogramFacetBuilder
specifier|public
class|class
name|HistogramFacetBuilder
extends|extends
name|AbstractFacetBuilder
block|{
DECL|field|keyFieldName
specifier|private
name|String
name|keyFieldName
decl_stmt|;
DECL|field|valueFieldName
specifier|private
name|String
name|valueFieldName
decl_stmt|;
DECL|field|interval
specifier|private
name|long
name|interval
init|=
operator|-
literal|1
decl_stmt|;
DECL|field|comparatorType
specifier|private
name|HistogramFacet
operator|.
name|ComparatorType
name|comparatorType
decl_stmt|;
DECL|field|from
specifier|private
name|Object
name|from
decl_stmt|;
DECL|field|to
specifier|private
name|Object
name|to
decl_stmt|;
comment|/**      * Constructs a new histogram facet with the provided facet logical name.      *      * @param name The logical name of the facet      */
DECL|method|HistogramFacetBuilder
specifier|public
name|HistogramFacetBuilder
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|super
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
comment|/**      * The field name to perform the histogram facet. Translates to perform the histogram facet      * using the provided field as both the {@link #keyField(String)} and {@link #valueField(String)}.      */
DECL|method|field
specifier|public
name|HistogramFacetBuilder
name|field
parameter_list|(
name|String
name|field
parameter_list|)
block|{
name|this
operator|.
name|keyFieldName
operator|=
name|field
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The field name to use in order to control where the hit will "fall into" within the histogram      * entries. Essentially, using the key field numeric value, the hit will be "rounded" into the relevant      * bucket controlled by the interval.      */
DECL|method|keyField
specifier|public
name|HistogramFacetBuilder
name|keyField
parameter_list|(
name|String
name|keyField
parameter_list|)
block|{
name|this
operator|.
name|keyFieldName
operator|=
name|keyField
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The field name to use as the value of the hit to compute data based on values within the interval      * (for example, total).      */
DECL|method|valueField
specifier|public
name|HistogramFacetBuilder
name|valueField
parameter_list|(
name|String
name|valueField
parameter_list|)
block|{
name|this
operator|.
name|valueFieldName
operator|=
name|valueField
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The interval used to control the bucket "size" where each key value of a hit will fall into.      */
DECL|method|interval
specifier|public
name|HistogramFacetBuilder
name|interval
parameter_list|(
name|long
name|interval
parameter_list|)
block|{
name|this
operator|.
name|interval
operator|=
name|interval
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * The interval used to control the bucket "size" where each key value of a hit will fall into.      */
DECL|method|interval
specifier|public
name|HistogramFacetBuilder
name|interval
parameter_list|(
name|long
name|interval
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|interval
argument_list|(
name|unit
operator|.
name|toMillis
argument_list|(
name|interval
argument_list|)
argument_list|)
return|;
block|}
comment|/**      * Sets the bounds from and to for the facet. Both performs bounds check and includes only      * values within the bounds, and improves performance.      */
DECL|method|bounds
specifier|public
name|HistogramFacetBuilder
name|bounds
parameter_list|(
name|Object
name|from
parameter_list|,
name|Object
name|to
parameter_list|)
block|{
name|this
operator|.
name|from
operator|=
name|from
expr_stmt|;
name|this
operator|.
name|to
operator|=
name|to
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|comparator
specifier|public
name|HistogramFacetBuilder
name|comparator
parameter_list|(
name|HistogramFacet
operator|.
name|ComparatorType
name|comparatorType
parameter_list|)
block|{
name|this
operator|.
name|comparatorType
operator|=
name|comparatorType
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Should the facet run in global mode (not bounded by the search query) or not (bounded by      * the search query). Defaults to<tt>false</tt>.      */
DECL|method|global
specifier|public
name|HistogramFacetBuilder
name|global
parameter_list|(
name|boolean
name|global
parameter_list|)
block|{
name|super
operator|.
name|global
argument_list|(
name|global
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Marks the facet to run in a specific scope.      */
DECL|method|scope
annotation|@
name|Override
specifier|public
name|HistogramFacetBuilder
name|scope
parameter_list|(
name|String
name|scope
parameter_list|)
block|{
name|super
operator|.
name|scope
argument_list|(
name|scope
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * An additional filter used to further filter down the set of documents the facet will run on.      */
DECL|method|facetFilter
specifier|public
name|HistogramFacetBuilder
name|facetFilter
parameter_list|(
name|FilterBuilder
name|filter
parameter_list|)
block|{
name|this
operator|.
name|facetFilter
operator|=
name|filter
expr_stmt|;
return|return
name|this
return|;
block|}
comment|/**      * Sets the nested path the facet will execute on. A match (root object) will then cause all the      * nested objects matching the path to be computed into the facet.      */
DECL|method|nested
specifier|public
name|HistogramFacetBuilder
name|nested
parameter_list|(
name|String
name|nested
parameter_list|)
block|{
name|this
operator|.
name|nested
operator|=
name|nested
expr_stmt|;
return|return
name|this
return|;
block|}
DECL|method|toXContent
annotation|@
name|Override
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
if|if
condition|(
name|keyFieldName
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"field must be set on histogram facet for facet ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
if|if
condition|(
name|interval
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|SearchSourceBuilderException
argument_list|(
literal|"interval must be set on histogram facet for facet ["
operator|+
name|name
operator|+
literal|"]"
argument_list|)
throw|;
block|}
name|builder
operator|.
name|startObject
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|builder
operator|.
name|startObject
argument_list|(
name|HistogramFacet
operator|.
name|TYPE
argument_list|)
expr_stmt|;
if|if
condition|(
name|valueFieldName
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"key_field"
argument_list|,
name|keyFieldName
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"value_field"
argument_list|,
name|valueFieldName
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"field"
argument_list|,
name|keyFieldName
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|field
argument_list|(
literal|"interval"
argument_list|,
name|interval
argument_list|)
expr_stmt|;
if|if
condition|(
name|from
operator|!=
literal|null
operator|&&
name|to
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"from"
argument_list|,
name|from
argument_list|)
expr_stmt|;
name|builder
operator|.
name|field
argument_list|(
literal|"to"
argument_list|,
name|to
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|comparatorType
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|field
argument_list|(
literal|"comparator"
argument_list|,
name|comparatorType
operator|.
name|description
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|builder
operator|.
name|endObject
argument_list|()
expr_stmt|;
name|addFilterFacetAndGlobal
argument_list|(
name|builder
argument_list|,
name|params
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
block|}
end_class

end_unit

